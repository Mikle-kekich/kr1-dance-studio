package ru.mirea.dancestudio.service;

import ru.mirea.dancestudio.model.ClassOccupancy;
import ru.mirea.dancestudio.model.Client;
import ru.mirea.dancestudio.model.Enrollment;
import ru.mirea.dancestudio.model.Exportable;
import ru.mirea.dancestudio.util.CsvExporter;
import ru.mirea.dancestudio.util.ExcelExporter;
import ru.mirea.dancestudio.util.ExportTable;
import ru.mirea.dancestudio.util.Formats;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Экспорт данных из базы в Excel (.xlsx, основной формат) и CSV (дополнительно). */
public class ExportService {

    private final ClientService clients;
    private final DanceClassService classes;
    private final EnrollmentService enrollments;
    private final StatisticsService statistics;

    public ExportService(ClientService clients, DanceClassService classes,
                         EnrollmentService enrollments, StatisticsService statistics) {
        this.clients = clients;
        this.classes = classes;
        this.enrollments = enrollments;
        this.statistics = statistics;
    }

    /** Путь по умолчанию: export/dance_studio_ГГГГ-ММ-ДД_ЧЧ-ММ-СС.xlsx относительно текущей папки. */
    public static Path defaultExcelPath() {
        return Path.of("export", "dance_studio_" + Formats.FILE_STAMP.format(LocalDateTime.now()) + ".xlsx");
    }

    public static Path defaultCsvDirectory() {
        return Path.of("export", "csv_" + Formats.FILE_STAMP.format(LocalDateTime.now()));
    }

    /** Все данные в одной книге Excel: листы «Клиенты», «Занятия», «Записи», «Статистика». */
    public Path exportToExcel(Path file) {
        List<ExportTable> tables = new ArrayList<>(loadDataTables().values());
        tables.add(statisticsTable());
        ExcelExporter.export(file, tables);
        return file.toAbsolutePath();
    }

    /** Каждая таблица - в отдельный CSV-файл в указанной папке. */
    public List<Path> exportToCsv(Path directory) {
        List<Path> files = new ArrayList<>();
        for (Map.Entry<String, ExportTable> entry : loadDataTables().entrySet()) {
            Path file = directory.resolve(entry.getKey() + ".csv");
            CsvExporter.export(file, entry.getValue());
            files.add(file.toAbsolutePath());
        }
        return files;
    }

    /** Ключ - имя CSV-файла без расширения, значение - таблица (порядок сохраняется). */
    private Map<String, ExportTable> loadDataTables() {
        List<Client> clientList = clients.findAll();
        List<ClassOccupancy> classList = classes.listWithOccupancy();
        List<Enrollment> enrollmentList = enrollments.findAll();
        Map<String, ExportTable> tables = new LinkedHashMap<>();
        tables.put("clients", ExportTable.of("Клиенты", Client.EXPORT_HEADERS, clientList));
        tables.put("dance_classes", ExportTable.of("Занятия", ClassOccupancy.EXPORT_HEADERS, classList));
        tables.put("enrollments", ExportTable.of("Записи", Enrollment.EXPORT_HEADERS, enrollmentList));
        return tables;
    }

    private ExportTable statisticsTable() {
        List<List<Object>> rows = new ArrayList<>();
        statistics.collect().toRows().forEach((name, value) -> rows.add(Exportable.row(name.strip(), value)));
        return new ExportTable("Статистика", List.of("Показатель", "Значение"), rows);
    }
}
