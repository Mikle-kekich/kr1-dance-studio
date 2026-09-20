package ru.mirea.dancestudio.ui;

import ru.mirea.dancestudio.exception.InvalidInputException;

import java.math.BigDecimal;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;

/**
 * Разбор введённых строк. Каждая фабрика возвращает функцию «строка -> значение»,
 * которая при неверном формате бросает InvalidInputException с понятным сообщением.
 */
public final class InputParsers {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.uuuu HH:mm").withResolverStyle(ResolverStyle.STRICT);
    private static final Set<String> YES = Set.of("да", "д", "yes", "y", "1");
    private static final Set<String> NO = Set.of("нет", "н", "no", "n", "0");

    private InputParsers() {
    }

    /** Идентификатор: целое положительное число. Для ID сообщение: «ID должен быть целым числом.» */
    public static Function<String, Long> id(String fieldName) {
        return text -> {
            long value;
            try {
                value = Long.parseLong(text.trim());
            } catch (NumberFormatException e) {
                throw new InvalidInputException(fieldName + " должен быть целым числом.");
            }
            if (value <= 0) {
                throw new InvalidInputException(fieldName + " должен быть положительным числом.");
            }
            return value;
        };
    }

    public static Function<String, Integer> integer(String fieldName, int min, int max) {
        return text -> {
            String message = "Поле «" + fieldName + "»: введите целое число от " + min + " до " + max + ".";
            int value;
            try {
                value = Integer.parseInt(text.trim());
            } catch (NumberFormatException e) {
                throw new InvalidInputException(message);
            }
            if (value < min || value > max) {
                throw new InvalidInputException(message);
            }
            return value;
        };
    }

    /** Любое целое число; допустимый диапазон проверяет сервис (правила хранятся в одном месте). */
    public static Function<String, Integer> integer(String fieldName) {
        return text -> {
            try {
                return Integer.parseInt(text.trim());
            } catch (NumberFormatException e) {
                throw new InvalidInputException("Поле «" + fieldName + "»: введите целое число.");
            }
        };
    }

    /** Денежная сумма; допускается запятая или точка: 1500, 1500.50, 1500,50. */
    public static Function<String, BigDecimal> money(String fieldName) {
        return text -> {
            try {
                return new BigDecimal(text.trim().replace(',', '.'));
            } catch (NumberFormatException e) {
                throw new InvalidInputException("Поле «" + fieldName + "»: введите число, например 1500 или 1500.50.");
            }
        };
    }

    public static Function<String, LocalDate> date(String fieldName) {
        return text -> {
            try {
                return LocalDate.parse(text.trim(), DATE_FORMAT);
            } catch (DateTimeParseException e) {
                throw new InvalidInputException("Поле «" + fieldName
                        + "»: введите существующую дату в формате дд.мм.гггг, например 25.09.2026.");
            }
        };
    }

    public static Function<String, LocalDateTime> dateTime(String fieldName) {
        return text -> {
            try {
                return LocalDateTime.parse(text.trim(), DATE_TIME_FORMAT);
            } catch (DateTimeParseException e) {
                throw new InvalidInputException("Поле «" + fieldName
                        + "»: введите дату и время в формате дд.мм.гггг чч:мм, например 25.09.2026 19:00.");
            }
        };
    }

    public static Function<String, Boolean> yesNo() {
        return text -> {
            String value = text.trim().toLowerCase(Locale.ROOT);
            if (YES.contains(value)) {
                return true;
            }
            if (NO.contains(value)) {
                return false;
            }
            throw new InvalidInputException("Ответьте «да» или «нет».");
        };
    }

    public static Function<String, String> nonBlank(String fieldName) {
        return text -> {
            if (text.isBlank()) {
                throw new InvalidInputException("Поле «" + fieldName + "» не может быть пустым.");
            }
            return text.trim();
        };
    }

    /** Путь к файлу; если расширение не указано, добавляется заданное (например, .xlsx). */
    public static Function<String, Path> filePath(String extension) {
        return text -> {
            try {
                String name = text.trim();
                return Path.of(name.toLowerCase(Locale.ROOT).endsWith(extension) ? name : name + extension);
            } catch (InvalidPathException e) {
                throw new InvalidInputException("Некорректный путь к файлу: " + text.trim());
            }
        };
    }

    public static Function<String, Path> directoryPath() {
        return text -> {
            try {
                return Path.of(text.trim());
            } catch (InvalidPathException e) {
                throw new InvalidInputException("Некорректный путь к папке: " + text.trim());
            }
        };
    }
}
