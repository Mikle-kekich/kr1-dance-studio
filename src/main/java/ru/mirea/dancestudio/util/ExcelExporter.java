package ru.mirea.dancestudio.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.mirea.dancestudio.exception.ExportException;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Экспорт данных в книгу Excel (.xlsx): один лист на каждую таблицу. Использует Apache POI. */
public final class ExcelExporter {

    private static final int MAX_COLUMN_WIDTH_CHARS = 50;

    static {
        // Apache POI пишет журнал через Log4j API. Без реализации Log4j в консоль попадает лишнее
        // сообщение об ошибке, поэтому заранее выбираем простой встроенный вариант (пишет только ошибки).
        System.setProperty("log4j2.loggerContextFactory",
                "org.apache.logging.log4j.simple.SimpleLoggerContextFactory");
    }

    private ExcelExporter() {
    }

    public static void export(Path file, List<ExportTable> tables) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Styles styles = new Styles(workbook);
            for (ExportTable table : tables) {
                writeSheet(workbook, styles, table);
            }
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (OutputStream out = Files.newOutputStream(file)) {
                workbook.write(out);
            }
        } catch (IOException e) {
            throw new ExportException("Не удалось сохранить файл Excel: " + file.toAbsolutePath(), e);
        }
    }

    private static void writeSheet(Workbook workbook, Styles styles, ExportTable table) {
        Sheet sheet = workbook.createSheet(table.title());
        int columns = table.headers().size();

        Row headerRow = sheet.createRow(0);
        for (int c = 0; c < columns; c++) {
            Cell cell = headerRow.createCell(c);
            cell.setCellValue(table.headers().get(c));
            cell.setCellStyle(styles.header);
        }
        int rowIndex = 1;
        for (List<Object> values : table.rows()) {
            Row row = sheet.createRow(rowIndex++);
            for (int c = 0; c < values.size(); c++) {
                writeCell(row.createCell(c), values.get(c), styles);
            }
        }
        sheet.createFreezePane(0, 1);
        if (columns > 0) {
            sheet.setAutoFilter(new CellRangeAddress(0, Math.max(0, rowIndex - 1), 0, columns - 1));
        }
        for (int c = 0; c < columns; c++) {
            fitColumn(sheet, c);
        }
    }

    private static void writeCell(Cell cell, Object value, Styles styles) {
        if (value == null) {
            cell.setBlank();
        } else if (value instanceof LocalDateTime dateTime) {
            cell.setCellValue(dateTime);
            cell.setCellStyle(styles.dateTime);
        } else if (value instanceof LocalDate date) {
            cell.setCellValue(date);
            cell.setCellStyle(styles.date);
        } else if (value instanceof BigDecimal money) {
            cell.setCellValue(money.doubleValue());
            cell.setCellStyle(styles.money);
        } else if (value instanceof Number number) {
            cell.setCellValue(number.doubleValue());
            cell.setCellStyle(styles.integer);
        } else if (value instanceof Boolean flag) {
            cell.setCellValue(flag ? "Да" : "Нет");
        } else {
            cell.setCellValue(String.valueOf(value));
        }
    }

    private static void fitColumn(Sheet sheet, int column) {
        try {
            sheet.autoSizeColumn(column);
            int limit = MAX_COLUMN_WIDTH_CHARS * 256;
            sheet.setColumnWidth(column, Math.min(sheet.getColumnWidth(column) + 512, limit));
        } catch (RuntimeException | Error e) {
            // на серверах без шрифтов автоподбор ширины недоступен - ставим ширину по умолчанию
            sheet.setColumnWidth(column, 20 * 256);
        }
    }

    /** Стили ячеек, созданные один раз на книгу. */
    private static final class Styles {
        private final CellStyle header;
        private final CellStyle date;
        private final CellStyle dateTime;
        private final CellStyle money;
        private final CellStyle integer;

        private Styles(Workbook workbook) {
            CreationHelper helper = workbook.getCreationHelper();
            DataFormat format = workbook.createDataFormat();

            Font bold = workbook.createFont();
            bold.setBold(true);
            header = workbook.createCellStyle();
            header.setFont(bold);
            header.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            header.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            date = workbook.createCellStyle();
            date.setDataFormat(helper.createDataFormat().getFormat("dd.mm.yyyy"));
            date.setAlignment(HorizontalAlignment.LEFT);

            dateTime = workbook.createCellStyle();
            dateTime.setDataFormat(format.getFormat("dd.mm.yyyy hh:mm"));
            dateTime.setAlignment(HorizontalAlignment.LEFT);

            money = workbook.createCellStyle();
            money.setDataFormat(format.getFormat("#,##0.00"));

            integer = workbook.createCellStyle();
            integer.setDataFormat(format.getFormat("0"));
            integer.setAlignment(HorizontalAlignment.LEFT);
        }
    }
}
