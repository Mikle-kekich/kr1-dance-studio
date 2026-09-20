package ru.mirea.dancestudio.util;

/** Небольшие помощники для безопасной работы с SQL. */
public final class SqlUtil {

    /** Символ экранирования в LIKE/ILIKE PostgreSQL по умолчанию - обратная косая черта. */
    private static final String BACKSLASH = "\\";

    private SqlUtil() {
    }

    /**
     * Готовит значение параметра для LIKE/ILIKE: экранирует служебные символы % и _
     * и добавляет подстановки по краям, то есть ищется вхождение подстроки.
     */
    public static String containsPattern(String term) {
        String escaped = term.trim()
                .replace(BACKSLASH, BACKSLASH + BACKSLASH)
                .replace("%", BACKSLASH + "%")
                .replace("_", BACKSLASH + "_");
        return "%" + escaped + "%";
    }

    /** Экранирует идентификатор (имя таблицы или столбца), полученный из метаданных БД. */
    public static String quoteIdentifier(String name) {
        return "\"" + name.replace("\"", "\"\"") + "\"";
    }
}
