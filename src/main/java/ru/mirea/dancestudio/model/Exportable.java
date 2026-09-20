package ru.mirea.dancestudio.model;

import java.util.Arrays;
import java.util.List;

/**
 * Объект, который умеет представить себя строкой таблицы для экспорта в Excel/CSV.
 * Реализуется разными классами модели, а экспортёр работает с ними единообразно (полиморфизм).
 * Значения могут быть String, Number, LocalDate, LocalDateTime, Boolean или null.
 */
public interface Exportable {

    List<Object> toExportRow();

    /** Удобная фабрика строки: допускает null-значения, в отличие от List.of. */
    static List<Object> row(Object... values) {
        return Arrays.asList(values);
    }
}
