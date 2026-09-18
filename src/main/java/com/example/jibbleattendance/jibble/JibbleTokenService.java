package com.example.jibbleattendance.jibble;

import java.time.Instant;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.example.jibbleattendance.config.JibbleProperties;
import com.fasterxml.jackson.databind.JsonNode;

@Service
public class JibbleTokenService {

    private final JibbleProperties properties;
    private final RestClient restClient;

    private String cachedAccessToken;
    private Instant tokenExpiresAt = Instant.EPOCH;

    public JibbleTokenService(JibbleProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClient = restClientBuilder.build();
    }

    public boolean isConfigured() {
        return hasText(properties.getPersonalAccessToken())
                || (hasText(properties.getClientId()) && hasText(properties.getClientSecret()));
    }

    public synchronized String getAccessToken() {
        /*
         * Jibble currently exposes two credential patterns in its public guidance:
         *
         * 1) A personal access token for custom integrations.
         * 2) Client ID + Client Secret, exchanged for a bearer token.
         *
         * This application supports both. If a personal token exists, it is used first.
         */
        if (hasText(properties.getPersonalAccessToken())) {
            return properties.getPersonalAccessToken().trim();
        }

        if (!hasText(properties.getClientId()) || !hasText(properties.getClientSecret())) {
            throw new IllegalStateException(
                    "Jibble credentials are missing. Set JIBBLE_PERSONAL_ACCESS_TOKEN, "
                    + "or set both JIBBLE_CLIENT_ID and JIBBLE_CLIENT_SECRET.");
        }

        // Re-use a still-valid OAuth token instead of requesting a new one for every API call.
        if (cachedAccessToken != null && Instant.now().isBefore(tokenExpiresAt.minusSeconds(30))) {
            return cachedAccessToken;
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", properties.getClientId());
        form.add("client_secret", properties.getClientSecret());

        JsonNode response = restClient.post()
                .uri(properties.getIdentityUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .body(form)
                .retrieve()
                .body(JsonNode.class);

        if (response == null || response.path("access_token").asText().isBlank()) {
            throw new IllegalStateException("Jibble did not return an access_token.");
        }

        cachedAccessToken = response.path("access_token").asText();

        long expiresIn = response.path("expires_in").asLong(3600);
        tokenExpiresAt = Instant.now().plusSeconds(Math.max(expiresIn, 60));

        return cachedAccessToken;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
