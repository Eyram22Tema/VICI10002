package com.example.jibbleattendance.jibble;

import java.time.Instant;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.example.jibbleattendance.config.JibbleProperties;

@Service
public class JibbleTokenService {

    private static final long DEFAULT_TOKEN_LIFETIME_SECONDS = 3600;
    private static final long TOKEN_EXPIRY_SAFETY_MARGIN_SECONDS = 30;

    private final JibbleProperties properties;
    private final RestClient restClient;

    private String cachedAccessToken;
    private Instant tokenExpiresAt = Instant.EPOCH;

    public JibbleTokenService(
            JibbleProperties properties,
            RestClient.Builder restClientBuilder) {

        this.properties = properties;
        this.restClient = restClientBuilder.build();
    }

    public boolean isConfigured() {
        return hasText(properties.getPersonalAccessToken())
                || (hasText(properties.getClientId()) && hasText(properties.getClientSecret()));
    }

    public synchronized String getAccessToken() {
        // Jibble recommends a personal access token for a custom system or testing.
        if (hasText(properties.getPersonalAccessToken())) {
            return properties.getPersonalAccessToken().trim();
        }

        if (!hasText(properties.getClientId()) || !hasText(properties.getClientSecret())) {
            throw new IllegalStateException(
                    "Jibble credentials are missing. Set JIBBLE_PERSONAL_ACCESS_TOKEN, "
                    + "or set both JIBBLE_CLIENT_ID and JIBBLE_CLIENT_SECRET.");
        }

        if (cachedAccessToken != null
                && Instant.now().isBefore(tokenExpiresAt.minusSeconds(TOKEN_EXPIRY_SAFETY_MARGIN_SECONDS))) {
            return cachedAccessToken;
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", properties.getClientId().trim());
        form.add("client_secret", properties.getClientSecret().trim());

        TokenResponse response = restClient.post()
                .uri(properties.getIdentityUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .body(form)
                .retrieve()
                .body(TokenResponse.class);

        if (response == null || !hasText(response.access_token())) {
            throw new IllegalStateException(
                    "Jibble authentication did not return an access_token. "
                    + "Verify the configured authentication method and API credentials.");
        }

        cachedAccessToken = response.access_token().trim();

        long expiresIn = response.expires_in() != null && response.expires_in() > 0
                ? response.expires_in()
                : DEFAULT_TOKEN_LIFETIME_SECONDS;

        tokenExpiresAt = Instant.now().plusSeconds(expiresIn);
        return cachedAccessToken;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    /**
     * Matches the OAuth-style JSON returned by the Jibble identity endpoint.
     * Snake-case component names deliberately match the wire-format field names,
     * so no legacy Jackson naming configuration is required.
     */
    public record TokenResponse(
            String access_token,
            Long expires_in,
            String token_type) {
    }
}
