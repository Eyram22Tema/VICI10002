package com.example.jibbleattendance.attendance;

import java.time.LocalDate;
import java.time.ZoneId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.jibbleattendance.config.JibbleProperties;
import com.example.jibbleattendance.jibble.JibbleTokenService;

@Component
public class AttendanceScheduler {

    private static final Logger log = LoggerFactory.getLogger(AttendanceScheduler.class);

    private final AttendanceSyncService syncService;
    private final JibbleTokenService tokenService;
    private final JibbleProperties properties;

    public AttendanceScheduler(
            AttendanceSyncService syncService,
            JibbleTokenService tokenService,
            JibbleProperties properties) {

        this.syncService = syncService;
        this.tokenService = tokenService;
        this.properties = properties;
    }

    @Scheduled(
            initialDelayString = "${jibble.sync.initial-delay-ms:15000}",
            fixedDelayString = "${jibble.sync.fixed-delay-ms:300000}")
    public void synchronizeRecentAttendance() {
        if (!tokenService.isConfigured()) {
            log.debug("Jibble sync skipped because credentials are not configured.");
            return;
        }

        try {
            ZoneId zoneId = ZoneId.of(properties.getZoneId());
            LocalDate today = LocalDate.now(zoneId);

            int lookbackDays = Math.max(properties.getSync().getLookbackDays(), 0);
            LocalDate from = today.minusDays(lookbackDays);

            SyncResult result = syncService.sync(from, today);

            log.info(
                    "Jibble sync complete. Received={}, inserted={}, updated={}, skipped={}",
                    result.receivedFromJibble(),
                    result.inserted(),
                    result.updated(),
                    result.skipped());

        } catch (Exception exception) {
            // The scheduler should try again at the next interval instead of
            // crashing the whole Spring Boot application.
            log.error("Automatic Jibble attendance sync failed.", exception);
        }
    }
}
