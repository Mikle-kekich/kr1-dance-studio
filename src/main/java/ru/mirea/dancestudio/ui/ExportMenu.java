package ru.mirea.dancestudio.ui;

import ru.mirea.dancestudio.service.ExportService;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;

/** Меню «Экспорт данных»: выгрузка из базы в Excel (.xlsx) и CSV. */
public class ExportMenu extends AbstractMenu {

    private final ExportService service;

    public ExportMenu(ConsoleInput in, PrintStream out, Printer printer, ExportService service) {
        super(in, out, printer);
        this.service = service;
    }

    @Override
    public String title() {
        return "ЭКСПОРТ ДАННЫХ";
    }

    @Override
    protected List<Item> items() {
        return List.of(
                new Item("Все данные в Excel (.xlsx): клиенты, занятия, записи, статистика", this::toExcel),
                new Item("Все таблицы в CSV (отдельные файлы)", this::toCsv));
    }

    private void toExcel() {
        Path defaultFile = ExportService.defaultExcelPath();
        Path file = in.readOrKeep("Файл для сохранения (Enter - " + defaultFile + "): ",
                defaultFile, InputParsers.filePath(".xlsx"));
        Path saved = service.exportToExcel(file);
        out.println("Данные экспортированы в файл: " + saved);
    }

    private void toCsv() {
        Path defaultDir = ExportService.defaultCsvDirectory();
        Path directory = in.readOrKeep("Папка для сохранения (Enter - " + defaultDir + "): ",
                defaultDir, InputParsers.directoryPath());
        List<Path> files = service.exportToCsv(directory);
        out.println("Созданы файлы:");
        files.forEach(file -> out.println("  " + file));
    }
}
