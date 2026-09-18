package com.example.jibbleattendance.attendance;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import tools.jackson.databind.JsonNode;

@Component
public class JibbleEntryMapper {

    public AttendanceRecord merge(JsonNode source, AttendanceRecord target) {
        target.setJibbleEntryId(getRequiredEntryId(source));
        target.setPersonId(firstString(source, "personId", "person.id"));
        target.setPersonName(extractPersonName(source));
        target.setEntryType(firstString(source, "type"));
        target.setEntryTime(parseOffsetDateTime(firstString(source, "time", "timestamp", "createdAt")));
        target.setBelongsToDate(parseLocalDate(firstString(source, "belongsToDate", "date")));
        target.setNote(firstString(source, "note", "notes"));
        target.setActivityId(firstString(source, "activityId", "activity.id"));
        target.setProjectId(firstString(source, "projectId", "project.id"));
        target.setLocationId(firstString(source, "locationId", "location.id"));
        target.setDeleted(firstBoolean(source, "isDeleted", "deleted", "isArchived", "archived"));

        // Retain the source payload so newly introduced Jibble fields are not lost.
        target.setRawPayload(source.toString());
        target.setLastSyncedAt(Instant.now());
        return target;
    }

    public String getRequiredEntryId(JsonNode source) {
        String id = firstString(source, "id");
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("A Jibble time entry did not contain an id: " + source);
        }
        return id;
    }

    private String extractPersonName(JsonNode source) {
        String direct = firstString(
                source,
                "person.fullName",
                "person.name",
                "person.displayName",
                "personName");

        if (direct != null && !direct.isBlank()) {
            return direct;
        }

        String firstName = firstString(source, "person.firstName", "firstName");
        String lastName = firstString(source, "person.lastName", "lastName");
        String combined = ((firstName == null ? "" : firstName) + " "
                + (lastName == null ? "" : lastName)).trim();

        return combined.isBlank() ? null : combined;
    }

    /**
     * Jackson 3 renamed the preferred textual coercion method from asText()
     * to asString(). This helper also safely handles missing nested objects.
     */
    private String firstString(JsonNode source, String... paths) {
        if (source == null || source.isNull()) {
            return null;
        }

        for (String path : paths) {
            JsonNode current = nodeAt(source, path);
            if (current == null || current.isNull() || current.isContainer()) {
                continue;
            }

            String value = current.asString(null);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }

        return null;
    }

    private boolean firstBoolean(JsonNode source, String... paths) {
        if (source == null || source.isNull()) {
            return false;
        }

        for (String path : paths) {
            JsonNode current = nodeAt(source, path);
            if (current != null && current.isBoolean()) {
                return current.booleanValue();
            }
        }

        return false;
    }

    private JsonNode nodeAt(JsonNode source, String dottedPath) {
        JsonNode current = source;

        for (String part : dottedPath.split("\\.")) {
            if (current == null || current.isNull() || !current.isObject()) {
                return null;
            }
            current = current.get(part);
        }

        return current;
    }

    private LocalDate parseLocalDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            if (value.length() >= 10) {
                return LocalDate.parse(value.substring(0, 10));
            }
            return LocalDate.parse(value);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private OffsetDateTime parseOffsetDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return OffsetDateTime.parse(value);
        } catch (RuntimeException firstAttempt) {
            try {
                return Instant.parse(value).atOffset(ZoneOffset.UTC);
            } catch (RuntimeException ignored) {
                return null;
            }
        }
    }
}
