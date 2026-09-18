package com.example.jibbleattendance.attendance;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRepository extends JpaRepository<AttendanceRecord, Long> {

    Optional<AttendanceRecord> findByJibbleEntryId(String jibbleEntryId);

    List<AttendanceRecord> findByBelongsToDateBetweenOrderByBelongsToDateAscEntryTimeAsc(
            LocalDate from,
            LocalDate to);

    List<AttendanceRecord> findByPersonIdAndBelongsToDateBetweenOrderByBelongsToDateAscEntryTimeAsc(
            String personId,
            LocalDate from,
            LocalDate to);
}
