package com.example.jibbleattendance.jibble;

import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.jibbleattendance.config.JibbleProperties;

import tools.jackson.databind.JsonNode;

@Component
public class JibbleApiClient {

    private static final int MAX_PAGES_PER_SYNC = 10_000;

    private final JibbleProperties properties;
    private final JibbleTokenService tokenService;
    private final RestClient restClient;

    public JibbleApiClient(
            JibbleProperties properties,
            JibbleTokenService tokenService,
            RestClient.Builder restClientBuilder) {

        this.properties = properties;
        this.tokenService = tokenService;
        this.restClient = restClientBuilder.build();
    }

    /**
     * Downloads raw Jibble time entries for the requested inclusive date range.
     */
    public List<JsonNode> fetchTimeEntries(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("from and to dates are required.");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("'from' cannot be after 'to'.");
        }

        List<JsonNode> allEntries = new ArrayList<>();

        int pageSize = Math.max(properties.getPageSize(), 1);
        int skip = 0;

        for (int page = 0; page < MAX_PAGES_PER_SYNC; page++) {
            URI uri = buildTimeEntriesUri(from, to, skip, pageSize);

            JsonNode response = restClient.get()
                    .uri(uri)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenService.getAccessToken())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null || response.isNull()) {
                break;
            }

            JsonNode valueNode = response.get("value");
            JsonNode entriesNode = valueNode != null && valueNode.isArray() ? valueNode : response;

            if (!entriesNode.isArray() || entriesNode.isEmpty()) {
                break;
            }

            int numberReceived = 0;
            for (JsonNode entry : entriesNode) {
                allEntries.add(entry);
                numberReceived++;
            }

            if (numberReceived < pageSize) {
                break;
            }

            skip += numberReceived;
        }

        return List.copyOf(allEntries);
    }

    private URI buildTimeEntriesUri(LocalDate from, LocalDate to, int skip, int pageSize) {
        String baseUrl = properties.getTimeTrackingBaseUrl().replaceAll("/+$", "");
        String filter = "(belongsToDate ge " + from + ") and (belongsToDate le " + to + ")";

        return UriComponentsBuilder
                .fromUriString(baseUrl + "/TimeEntries")
                .queryParam("$count", "true")
                .queryParam("$expand", "person")
                .queryParam("$filter", filter)
                .queryParam("$orderby", "time asc")
                .queryParam("$skip", skip)
                .queryParam("$top", pageSize)
                .build()
                .encode()
                .toUri();
    }
}
