package com.example.jibbleattendance.api;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.jibbleattendance.attendance.AttendanceSyncService;
import com.example.jibbleattendance.attendance.SyncResult;
import com.example.jibbleattendance.config.JibbleProperties;
import com.example.jibbleattendance.jibble.JibbleApiClient;
import com.example.jibbleattendance.jibble.JibbleTokenService;
import tools.jackson.databind.JsonNode;

@RestController
@RequestMapping("/api/jibble")
public class JibbleController {

    private final JibbleApiClient jibbleApiClient;
    private final JibbleTokenService tokenService;
    private final AttendanceSyncService syncService;
    private final JibbleProperties properties;

    public JibbleController(
            JibbleApiClient jibbleApiClient,
            JibbleTokenService tokenService,
            AttendanceSyncService syncService,
            JibbleProperties properties) {

        this.jibbleApiClient = jibbleApiClient;
        this.tokenService = tokenService;
        this.syncService = syncService;
        this.properties = properties;
    }

    /**
     * Does not write to the database.
     * It simply proves that the Spring Boot application can authenticate
     * and read today's Jibble time entries.
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testConnection() {
        if (!tokenService.isConfigured()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "connected", false,
                    "message", "Jibble credentials are not configured."));
        }

        LocalDate today = LocalDate.now(ZoneId.of(properties.getZoneId()));
        List<JsonNode> entries = jibbleApiClient.fetchTimeEntries(today, today);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("connected", true);
        response.put("date", today);
        response.put("entriesReturned", entries.size());

        if (!entries.isEmpty()) {
            response.put("sampleEntry", entries.get(0));
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/sync")
    public SyncResult sync(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to) {

        return syncService.sync(from, to);
    }
}
