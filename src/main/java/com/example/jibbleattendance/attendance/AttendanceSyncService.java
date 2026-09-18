package com.example.jibbleattendance.attendance;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.jibbleattendance.jibble.JibbleApiClient;

import tools.jackson.databind.JsonNode;

@Service
public class AttendanceSyncService {

    private static final Logger log = LoggerFactory.getLogger(AttendanceSyncService.class);

    private final JibbleApiClient jibbleApiClient;
    private final AttendanceRepository attendanceRepository;
    private final JibbleEntryMapper mapper;

    public AttendanceSyncService(
            JibbleApiClient jibbleApiClient,
            AttendanceRepository attendanceRepository,
            JibbleEntryMapper mapper) {

        this.jibbleApiClient = jibbleApiClient;
        this.attendanceRepository = attendanceRepository;
        this.mapper = mapper;
    }

    @Transactional
    public SyncResult sync(LocalDate from, LocalDate to) {
        List<JsonNode> jibbleEntries = jibbleApiClient.fetchTimeEntries(from, to);

        int inserted = 0;
        int updated = 0;
        int skipped = 0;

        for (JsonNode source : jibbleEntries) {
            try {
                String jibbleEntryId = mapper.getRequiredEntryId(source);

                AttendanceRecord record = attendanceRepository
                        .findByJibbleEntryId(jibbleEntryId)
                        .orElse(null);

                if (record == null) {
                    record = new AttendanceRecord();
                    inserted++;
                } else {
                    updated++;
                }

                mapper.merge(source, record);
                attendanceRepository.save(record);

            } catch (RuntimeException exception) {
                skipped++;
                log.warn("Skipping an invalid Jibble time entry. Reason: {}", exception.getMessage());
            }
        }

        return new SyncResult(
                from,
                to,
                jibbleEntries.size(),
                inserted,
                updated,
                skipped,
                Instant.now());
    }

    @Transactional(readOnly = true)
    public List<AttendanceView> find(LocalDate from, LocalDate to) {
        return attendanceRepository
                .findByBelongsToDateBetweenOrderByBelongsToDateAscEntryTimeAsc(from, to)
                .stream()
                .map(AttendanceView::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AttendanceView> findForPerson(String personId, LocalDate from, LocalDate to) {
        return attendanceRepository
                .findByPersonIdAndBelongsToDateBetweenOrderByBelongsToDateAscEntryTimeAsc(
                        personId,
                        from,
                        to)
                .stream()
                .map(AttendanceView::from)
                .toList();
    }
}
