package com.example.jibbleattendance.attendance;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "jibble_attendance_records",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_jibble_attendance_entry",
                        columnNames = "jibble_entry_id")
        },
        indexes = {
                @Index(name = "idx_attendance_person_id", columnList = "person_id"),
                @Index(name = "idx_attendance_date", columnList = "belongs_to_date"),
                @Index(name = "idx_attendance_time", columnList = "entry_time")
        }
)
public class AttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "jibble_entry_id", nullable = false, length = 150)
    private String jibbleEntryId;

    @Column(name = "person_id", length = 150)
    private String personId;

    @Column(name = "person_name", length = 300)
    private String personName;

    @Column(name = "entry_type", length = 50)
    private String entryType;

    @Column(name = "entry_time")
    private OffsetDateTime entryTime;

    @Column(name = "belongs_to_date")
    private LocalDate belongsToDate;

    @Column(name = "note", columnDefinition = "text")
    private String note;

    @Column(name = "activity_id", length = 150)
    private String activityId;

    @Column(name = "project_id", length = 150)
    private String projectId;

    @Column(name = "location_id", length = 150)
    private String locationId;

    @Column(name = "deleted", nullable = false)
    private boolean deleted;

    @Column(name = "raw_payload", columnDefinition = "text")
    private String rawPayload;

    @Column(name = "last_synced_at", nullable = false)
    private Instant lastSyncedAt;

    public Long getId() {
        return id;
    }

    public String getJibbleEntryId() {
        return jibbleEntryId;
    }

    public void setJibbleEntryId(String jibbleEntryId) {
        this.jibbleEntryId = jibbleEntryId;
    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public String getEntryType() {
        return entryType;
    }

    public void setEntryType(String entryType) {
        this.entryType = entryType;
    }

    public OffsetDateTime getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(OffsetDateTime entryTime) {
        this.entryTime = entryTime;
    }

    public LocalDate getBelongsToDate() {
        return belongsToDate;
    }

    public void setBelongsToDate(LocalDate belongsToDate) {
        this.belongsToDate = belongsToDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getRawPayload() {
        return rawPayload;
    }

    public void setRawPayload(String rawPayload) {
        this.rawPayload = rawPayload;
    }

    public Instant getLastSyncedAt() {
        return lastSyncedAt;
    }

    public void setLastSyncedAt(Instant lastSyncedAt) {
        this.lastSyncedAt = lastSyncedAt;
    }
}
