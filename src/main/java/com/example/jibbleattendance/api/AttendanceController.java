package com.example.jibbleattendance.api;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.jibbleattendance.attendance.AttendanceSyncService;
import com.example.jibbleattendance.attendance.AttendanceView;
import com.example.jibbleattendance.config.JibbleProperties;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceSyncService syncService;
    private final JibbleProperties properties;

    public AttendanceController(
            AttendanceSyncService syncService,
            JibbleProperties properties) {

        this.syncService = syncService;
        this.properties = properties;
    }

    @GetMapping
    public List<AttendanceView> getAttendance(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to) {

        DateRange range = normalize(from, to);
        return syncService.find(range.from(), range.to());
    }

    @GetMapping("/person/{personId}")
    public List<AttendanceView> getAttendanceForPerson(
            @PathVariable String personId,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to) {

        DateRange range = normalize(from, to);
        return syncService.findForPerson(personId, range.from(), range.to());
    }

    private DateRange normalize(LocalDate from, LocalDate to) {
        LocalDate today = LocalDate.now(ZoneId.of(properties.getZoneId()));

        LocalDate effectiveTo = to == null ? today : to;
        LocalDate effectiveFrom = from == null ? effectiveTo.minusDays(7) : from;

        if (effectiveFrom.isAfter(effectiveTo)) {
            throw new IllegalArgumentException("'from' cannot be after 'to'.");
        }

        return new DateRange(effectiveFrom, effectiveTo);
    }

    private record DateRange(LocalDate from, LocalDate to) {
    }
}
