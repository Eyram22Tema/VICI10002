package com.example.jibbleattendance.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jibble")
public class JibbleProperties {

    private String personalAccessToken;
    private String clientId;
    private String clientSecret;
    private String identityUrl = "https://identity.prod.jibble.io/connect/token";
    private String timeTrackingBaseUrl = "https://time-tracking.prod.jibble.io/v1";
    private int pageSize = 200;
    private String zoneId = "Africa/Accra";
    private final Sync sync = new Sync();

    public String getPersonalAccessToken() {
        return personalAccessToken;
    }

    public void setPersonalAccessToken(String personalAccessToken) {
        this.personalAccessToken = personalAccessToken;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getIdentityUrl() {
        return identityUrl;
    }

    public void setIdentityUrl(String identityUrl) {
        this.identityUrl = identityUrl;
    }

    public String getTimeTrackingBaseUrl() {
        return timeTrackingBaseUrl;
    }

    public void setTimeTrackingBaseUrl(String timeTrackingBaseUrl) {
        this.timeTrackingBaseUrl = timeTrackingBaseUrl;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }

    public Sync getSync() {
        return sync;
    }

    public static class Sync {
        private long fixedDelayMs = 300000;
        private long initialDelayMs = 15000;
        private int lookbackDays = 2;

        public long getFixedDelayMs() {
            return fixedDelayMs;
        }

        public void setFixedDelayMs(long fixedDelayMs) {
            this.fixedDelayMs = fixedDelayMs;
        }

        public long getInitialDelayMs() {
            return initialDelayMs;
        }

        public void setInitialDelayMs(long initialDelayMs) {
            this.initialDelayMs = initialDelayMs;
        }

        public int getLookbackDays() {
            return lookbackDays;
        }

        public void setLookbackDays(int lookbackDays) {
            this.lookbackDays = lookbackDays;
        }
    }
}
