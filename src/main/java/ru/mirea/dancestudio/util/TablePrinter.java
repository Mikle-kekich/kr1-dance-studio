package ru.mirea.dancestudio.util;

import java.util.List;

/** Строит аккуратную текстовую таблицу (только ASCII-рамка, чтобы работало в любой кодировке консоли). */
public final class TablePrinter {

    private static final int DEFAULT_MAX_CELL_WIDTH = 34;

    private TablePrinter() {
    }

    public static String render(List<String> headers, List<List<String>> rows) {
        return render(headers, rows, DEFAULT_MAX_CELL_WIDTH);
    }

    public static String render(List<String> headers, List<List<String>> rows, int maxCellWidth) {
        int columns = headers.size();
        int[] widths = new int[columns];
        for (int i = 0; i < columns; i++) {
            widths[i] = headers.get(i).length();
        }
        for (List<String> row : rows) {
            for (int i = 0; i < columns; i++) {
                widths[i] = Math.max(widths[i], Math.min(cell(row, i).length(), maxCellWidth));
            }
        }
        String separator = separator(widths);
        StringBuilder sb = new StringBuilder();
        sb.append(separator).append('\n');
        sb.append(line(headers, widths)).append('\n');
        sb.append(separator).append('\n');
        for (List<String> row : rows) {
            sb.append(line(row, widths)).append('\n');
        }
        sb.append(separator);
        return sb.toString();
    }

    private static String cell(List<String> row, int index) {
        if (index >= row.size() || row.get(index) == null) {
            return "";
        }
        return row.get(index).replace('\n', ' ').replace('\r', ' ');
    }

    private static String line(List<String> row, int[] widths) {
        StringBuilder sb = new StringBuilder("|");
        for (int i = 0; i < widths.length; i++) {
            sb.append(' ').append(pad(fit(cell(row, i), widths[i]), widths[i])).append(" |");
        }
        return sb.toString();
    }

    private static String separator(int[] widths) {
        StringBuilder sb = new StringBuilder("+");
        for (int width : widths) {
            sb.append("-".repeat(width + 2)).append('+');
        }
        return sb.toString();
    }

    private static String fit(String text, int width) {
        if (text.length() <= width) {
            return text;
        }
        return width <= 3 ? text.substring(0, width) : text.substring(0, width - 3) + "...";
    }

    private static String pad(String text, int width) {
        return text + " ".repeat(Math.max(0, width - text.length()));
    }
}
