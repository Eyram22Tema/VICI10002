package com.example.jibbleattendance.attendance;

import java.time.Instant;
import java.time.LocalDate;

public record SyncResult(
        LocalDate from,
        LocalDate to,
        int receivedFromJibble,
        int inserted,
        int updated,
        int skipped,
        Instant completedAt) {
}
