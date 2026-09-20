package ru.mirea.dancestudio.ui;

import ru.mirea.dancestudio.model.DanceStyle;
import ru.mirea.dancestudio.model.EnrollmentStatus;
import ru.mirea.dancestudio.service.EnrollmentQueryService;

import java.io.PrintStream;
import java.time.LocalDate;
import java.util.List;

/** Меню «Фильтрация»: пять фильтров записей на занятия (реализованы через Stream API в сервисе). */
public class FilterMenu extends AbstractMenu {

    private final EnrollmentQueryService queries;

    public FilterMenu(ConsoleInput in, PrintStream out, Printer printer, EnrollmentQueryService queries) {
        super(in, out, printer);
        this.queries = queries;
    }

    @Override
    public String title() {
        return "ФИЛЬТРАЦИЯ ЗАПИСЕЙ НА ЗАНЯТИЯ";
    }

    @Override
    protected List<Item> items() {
        return List.of(
                new Item("По статусу", this::byStatus),
                new Item("По оплате", this::byPaid),
                new Item("По направлению танца", this::byStyle),
                new Item("По периоду дат занятия", this::byDateRange),
                new Item("По клиенту", this::byClient));
    }

    private void byStatus() {
        EnrollmentStatus status = in.choose("Статус записи:", List.of(EnrollmentStatus.values()),
                EnrollmentStatus::getLabel);
        printer.enrollments(queries.filterByStatus(status));
    }

    private void byPaid() {
        boolean paid = in.confirm("Показать оплаченные записи (нет - показать неоплаченные)?");
        printer.enrollments(queries.filterByPaid(paid));
    }

    private void byStyle() {
        DanceStyle style = in.choose("Направление:", List.of(DanceStyle.values()), DanceStyle::getLabel);
        printer.enrollments(queries.filterByStyle(style));
    }

    private void byDateRange() {
        LocalDate from = in.readOptional("Занятия с даты (дд.мм.гггг, Enter - без ограничения): ",
                InputParsers.date("Начало периода"));
        LocalDate to = in.readOptional("Занятия по дату (дд.мм.гггг, Enter - без ограничения): ",
                InputParsers.date("Конец периода"));
        printer.enrollments(queries.filterByClassDateRange(from, to));
    }

    private void byClient() {
        long clientId = in.read("Введите ID клиента: ", InputParsers.id("ID клиента"));
        printer.enrollments(queries.filterByClient(clientId));
    }
}
