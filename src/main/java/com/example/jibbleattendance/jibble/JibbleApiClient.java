package com.example.jibbleattendance.jibble;

import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.jibbleattendance.config.JibbleProperties;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class JibbleApiClient {

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
     *
     * Jibble's TimeEntries endpoint is OData-style. We request:
     * - $expand=person so employee information is included where available
     * - $filter to keep the transfer limited to the requested dates
     * - $orderby=time asc for predictable ordering
     * - $skip/$top for pagination
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
        int safetyPageLimit = 10000;

        for (int page = 0; page < safetyPageLimit; page++) {
            URI uri = buildTimeEntriesUri(from, to, skip, pageSize);

            JsonNode response = restClient.get()
                    .uri(uri)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenService.getAccessToken())
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null) {
                break;
            }

            JsonNode value = response.path("value");

            // OData responses normally use {"value":[...]}. This fallback makes
            // the integration tolerant if Jibble returns a direct JSON array.
            JsonNode entriesNode = value.isArray() ? value : response;

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

            skip += pageSize;
        }

        return allEntries;
    }

    private URI buildTimeEntriesUri(LocalDate from, LocalDate to, int skip, int pageSize) {
        String baseUrl = properties.getTimeTrackingBaseUrl().replaceAll("/+$", "");

        // Inclusive date filter.
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
