package com.example.jibbleattendance.attendance;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

@Component
public class JibbleEntryMapper {

    public AttendanceRecord merge(JsonNode source, AttendanceRecord target) {
        target.setJibbleEntryId(firstText(source, "id"));

        String personId = firstText(source, "personId", "person.id");
        target.setPersonId(personId);

        target.setPersonName(extractPersonName(source));
        target.setEntryType(firstText(source, "type"));
        target.setEntryTime(parseOffsetDateTime(firstText(source, "time", "timestamp", "createdAt")));
        target.setBelongsToDate(parseLocalDate(firstText(source, "belongsToDate", "date")));
        target.setNote(firstText(source, "note", "notes"));
        target.setActivityId(firstText(source, "activityId", "activity.id"));
        target.setProjectId(firstText(source, "projectId", "project.id"));
        target.setLocationId(firstText(source, "locationId", "location.id"));

        boolean deleted = firstBoolean(
                source,
                "isDeleted",
                "deleted",
                "isArchived",
                "archived");
        target.setDeleted(deleted);

        /*
         * Keep the entire JSON record too.
         * This is a "safety net": if Jibble adds a field later, you still have
         * the original payload and do not lose that information.
         */
        target.setRawPayload(source.toString());
        target.setLastSyncedAt(Instant.now());

        return target;
    }

    public String getRequiredEntryId(JsonNode source) {
        String id = firstText(source, "id");
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("A Jibble time entry did not contain an id: " + source);
        }
        return id;
    }

    private String extractPersonName(JsonNode source) {
        String direct = firstText(
                source,
                "person.fullName",
                "person.name",
                "person.displayName",
                "personName");

        if (direct != null && !direct.isBlank()) {
            return direct;
        }

        String firstName = firstText(source, "person.firstName", "firstName");
        String lastName = firstText(source, "person.lastName", "lastName");

        String combined = ((firstName == null ? "" : firstName) + " "
                + (lastName == null ? "" : lastName)).trim();

        return combined.isBlank() ? null : combined;
    }

    private String firstText(JsonNode source, String... paths) {
        for (String path : paths) {
            JsonNode current = source;

            for (String part : path.split("\\.")) {
                current = current.path(part);
            }

            if (!current.isMissingNode() && !current.isNull()) {
                String value = current.asText();
                if (value != null && !value.isBlank()) {
                    return value;
                }
            }
        }

        return null;
    }

    private boolean firstBoolean(JsonNode source, String... paths) {
        for (String path : paths) {
            JsonNode current = source;

            for (String part : path.split("\\.")) {
                current = current.path(part);
            }

            if (current.isBoolean()) {
                return current.asBoolean();
            }
        }

        return false;
    }

    private LocalDate parseLocalDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            // Works for "2026-09-16" and also safely takes the date portion
            // of longer ISO-style values.
            return LocalDate.parse(value.substring(0, Math.min(value.length(), 10)));
        } catch (Exception ignored) {
            return null;
        }
    }

    private OffsetDateTime parseOffsetDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return OffsetDateTime.parse(value);
        } catch (Exception firstAttempt) {
            try {
                return Instant.parse(value).atOffset(java.time.ZoneOffset.UTC);
            } catch (Exception ignored) {
                return null;
            }
        }
    }
}
