package ru.mirea.dancestudio.util;

import ru.mirea.dancestudio.exception.ExportException;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Экспорт таблицы в CSV: кодировка UTF-8 с BOM (чтобы Excel правильно показал кириллицу),
 * разделитель - точка с запятой, десятичный разделитель - запятая (как в русской версии Excel).
 */
public final class CsvExporter {

    private static final char DELIMITER = ';';
    private static final String NEW_LINE = "\r\n";

    private CsvExporter() {
    }

    public static void export(Path file, ExportTable table) {
        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                writer.write('\uFEFF');
                writeLine(writer, table.headers().stream().map(h -> (Object) h).toList());
                for (List<Object> row : table.rows()) {
                    writeLine(writer, row);
                }
            }
        } catch (IOException e) {
            throw new ExportException("Не удалось сохранить файл CSV: " + file.toAbsolutePath(), e);
        }
    }

    private static void writeLine(Writer writer, List<Object> values) throws IOException {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                line.append(DELIMITER);
            }
            line.append(escape(format(values.get(i))));
        }
        writer.write(line.append(NEW_LINE).toString());
    }

    static String format(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof LocalDate date) {
            return Formats.date(date);
        }
        if (value instanceof LocalDateTime dateTime) {
            return Formats.dateTime(dateTime);
        }
        if (value instanceof Boolean flag) {
            return flag ? "Да" : "Нет";
        }
        if (value instanceof BigDecimal number) {
            return number.toPlainString().replace('.', ',');
        }
        if (value instanceof Double || value instanceof Float) {
            return String.valueOf(value).replace('.', ',');
        }
        return String.valueOf(value);
    }

    static String escape(String text) {
        boolean needsQuotes = text.indexOf(DELIMITER) >= 0 || text.indexOf('"') >= 0
                || text.indexOf('\n') >= 0 || text.indexOf('\r') >= 0;
        if (!needsQuotes) {
            return text;
        }
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }
}
