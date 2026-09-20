package ru.mirea.dancestudio.service;

import ru.mirea.dancestudio.exception.BusinessException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Общие проверки данных для сервисов. Нарушение любой проверки - BusinessException с понятным текстом. */
final class Validation {

    private static final BigDecimal MAX_MONEY = new BigDecimal("99999999.99");
    private static final int MAX_SEARCH_LENGTH = 100;

    private Validation() {
    }

    /** Обязательная строка: обрезает пробелы, схлопывает повторные пробелы, проверяет длину. */
    static String requireText(String value, String fieldName, int min, int max) {
        String text = value == null ? "" : value.trim().replaceAll(" +", " ");
        if (text.isEmpty()) {
            throw new BusinessException("Поле «" + fieldName + "» обязательно для заполнения.");
        }
        if (text.length() < min || text.length() > max) {
            throw new BusinessException("Поле «" + fieldName + "»: длина должна быть от " + min
                    + " до " + max + " символов.");
        }
        return text;
    }

    /** Необязательная строка: пустое значение превращается в null. */
    static String optionalText(String value, String fieldName, int max) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String text = value.trim();
        if (text.length() > max) {
            throw new BusinessException("Поле «" + fieldName + "» не должно быть длиннее " + max + " символов.");
        }
        return text;
    }

    static void requireNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new BusinessException("Поле «" + fieldName + "» обязательно для заполнения.");
        }
    }

    static void requireRange(long value, long min, long max, String fieldName) {
        if (value < min || value > max) {
            throw new BusinessException("Поле «" + fieldName + "»: допустимые значения от " + min + " до " + max + ".");
        }
    }

    /** Денежная сумма: не отрицательная, не более двух знаков после запятой, в пределах NUMERIC(10,2). */
    static BigDecimal requireMoney(BigDecimal value, String fieldName) {
        requireNotNull(value, fieldName);
        if (value.signum() < 0) {
            throw new BusinessException("Поле «" + fieldName + "» не может быть отрицательным.");
        }
        if (value.stripTrailingZeros().scale() > 2) {
            throw new BusinessException("Поле «" + fieldName + "»: допускается не более двух знаков после запятой.");
        }
        if (value.compareTo(MAX_MONEY) > 0) {
            throw new BusinessException("Поле «" + fieldName + "» слишком большое (максимум " + MAX_MONEY + ").");
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    /** Строка поиска: не пустая и не слишком длинная. */
    static String requireSearchTerm(String term) {
        String text = term == null ? "" : term.trim();
        if (text.isEmpty()) {
            throw new BusinessException("Введите непустую строку для поиска.");
        }
        if (text.length() > MAX_SEARCH_LENGTH) {
            throw new BusinessException("Строка поиска не должна быть длиннее " + MAX_SEARCH_LENGTH + " символов.");
        }
        return text;
    }
}
