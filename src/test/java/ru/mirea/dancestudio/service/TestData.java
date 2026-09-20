package ru.mirea.dancestudio.service;

import ru.mirea.dancestudio.model.Client;
import ru.mirea.dancestudio.model.DanceClass;
import ru.mirea.dancestudio.model.DanceLevel;
import ru.mirea.dancestudio.model.DanceStyle;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/** Общие тестовые данные: фиксированные «часы», чтобы результаты не зависели от текущей даты. */
final class TestData {

    static final LocalDateTime NOW = LocalDateTime.of(2026, 9, 20, 12, 0);
    static final Clock CLOCK = Clock.fixed(Instant.parse("2026-09-20T12:00:00Z"), ZoneOffset.UTC);

    private TestData() {
    }

    static Client adult(String name, String phone, String email) {
        return new Client(name, phone, email, LocalDate.of(1995, 5, 5));
    }

    static Client child(String name, String phone, String email) {
        return new Client(name, phone, email, LocalDate.of(2012, 5, 18));
    }

    /** Занятие через указанное число дней от NOW; capacity и minAge задаются параметрами. */
    static DanceClass classIn(int days, int capacity, int minAge) {
        return new DanceClass("Хип-хоп", DanceStyle.HIP_HOP, DanceLevel.BEGINNER, "Кузнецов Алексей",
                NOW.plusDays(days), 60, capacity, minAge, new BigDecimal("800.00"));
    }
}
