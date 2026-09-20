package ru.mirea.dancestudio.util;

import ru.mirea.dancestudio.model.Exportable;

import java.util.List;

/**
 * Таблица для экспорта: заголовок листа/файла, названия столбцов и строки со значениями.
 * Значения: String, Number, LocalDate, LocalDateTime, Boolean или null.
 */
public record ExportTable(String title, List<String> headers, List<List<Object>> rows) {

    /** Строит таблицу из любых объектов Exportable (клиентов, записей, занятий) - полиморфно. */
    public static ExportTable of(String title, List<String> headers, List<? extends Exportable> items) {
        List<List<Object>> rows = items.stream().map(Exportable::toExportRow).toList();
        return new ExportTable(title, headers, rows);
    }
}
