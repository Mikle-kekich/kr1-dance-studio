package ru.mirea.dancestudio.ui;

import ru.mirea.dancestudio.model.TableData;
import ru.mirea.dancestudio.service.DatabaseInfoService;
import ru.mirea.dancestudio.service.StatisticsService;

import java.io.PrintStream;
import java.util.List;

/**
 * Главное меню. Подменю хранятся как объекты интерфейса {@link Menu}, поэтому пункт меню
 * просто вызывает run() - какое именно меню откроется, определяется полиморфно.
 */
public class MainMenu extends AbstractMenu {

    private final Menu clients;
    private final Menu classes;
    private final Menu enrollments;
    private final Menu search;
    private final Menu filter;
    private final Menu sort;
    private final Menu export;
    private final StatisticsService statisticsService;
    private final DatabaseInfoService databaseInfoService;

    public MainMenu(ConsoleInput in, PrintStream out, Printer printer,
                    Menu clients, Menu classes, Menu enrollments, Menu search, Menu filter, Menu sort,
                    Menu export, StatisticsService statisticsService, DatabaseInfoService databaseInfoService) {
        super(in, out, printer);
        this.clients = clients;
        this.classes = classes;
        this.enrollments = enrollments;
        this.search = search;
        this.filter = filter;
        this.sort = sort;
        this.export = export;
        this.statisticsService = statisticsService;
        this.databaseInfoService = databaseInfoService;
    }

    @Override
    public String title() {
        return "СИСТЕМА УПРАВЛЕНИЯ ТАНЦЕВАЛЬНОЙ СТУДИЕЙ";
    }

    @Override
    protected String exitLabel() {
        return "Выход";
    }

    @Override
    protected List<Item> items() {
        return List.of(
                new Item("Клиенты", clients::run),
                new Item("Занятия (расписание)", classes::run),
                new Item("Записи на занятия", enrollments::run),
                new Item("Поиск", search::run),
                new Item("Фильтрация", filter::run),
                new Item("Сортировка", sort::run),
                new Item("Статистика", this::showStatistics),
                new Item("Экспорт данных", export::run),
                new Item("Вывести таблицы базы данных", this::showTables));
    }

    private void showStatistics() {
        printer.statistics(statisticsService.collect());
    }

    private void showTables() {
        for (TableData table : databaseInfoService.readAllTables()) {
            printer.databaseTable(table);
            out.println();
        }
    }
}
