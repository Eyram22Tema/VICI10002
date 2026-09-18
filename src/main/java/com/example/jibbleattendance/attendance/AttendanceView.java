package com.example.jibbleattendance.attendance;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record AttendanceView(
        Long id,
        String jibbleEntryId,
        String personId,
        String personName,
        String entryType,
        OffsetDateTime entryTime,
        LocalDate belongsToDate,
        String note,
        String activityId,
        String projectId,
        String locationId,
        boolean deleted,
        Instant lastSyncedAt) {

    public static AttendanceView from(AttendanceRecord record) {
        return new AttendanceView(
                record.getId(),
                record.getJibbleEntryId(),
                record.getPersonId(),
                record.getPersonName(),
                record.getEntryType(),
                record.getEntryTime(),
                record.getBelongsToDate(),
                record.getNote(),
                record.getActivityId(),
                record.getProjectId(),
                record.getLocationId(),
                record.isDeleted(),
                record.getLastSyncedAt());
    }
}
