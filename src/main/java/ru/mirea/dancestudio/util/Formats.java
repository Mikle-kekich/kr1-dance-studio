package ru.mirea.dancestudio.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Форматирование значений для вывода пользователю. */
public final class Formats {

    public static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    public static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    public static final DateTimeFormatter FILE_STAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private Formats() {
    }

    public static String date(LocalDate value) {
        return value == null ? "" : DATE.format(value);
    }

    public static String dateTime(LocalDateTime value) {
        return value == null ? "" : DATE_TIME.format(value);
    }

    /** Сумма в рублях с двумя знаками после точки, например «1500.00 руб.». */
    public static String money(BigDecimal value) {
        return value == null ? "" : value.setScale(2, RoundingMode.HALF_UP).toPlainString() + " руб.";
    }

    public static String yesNo(boolean value) {
        return value ? "да" : "нет";
    }

    /** Пустое значение заменяется прочерком. */
    public static String orDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    public static String decimal(double value, int scale) {
        return String.format(Locale.ROOT, "%." + scale + "f", value);
    }

    /** Склонение существительного по числу: 1 запись, 2 записи, 5 записей. */
    public static String plural(long n, String one, String few, String many) {
        long lastTwo = Math.abs(n) % 100;
        long last = lastTwo % 10;
        if (lastTwo >= 11 && lastTwo <= 14) {
            return many;
        }
        if (last == 1) {
            return one;
        }
        if (last >= 2 && last <= 4) {
            return few;
        }
        return many;
    }
}
