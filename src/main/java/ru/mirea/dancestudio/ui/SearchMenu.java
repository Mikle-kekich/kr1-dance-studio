package ru.mirea.dancestudio.ui;

import ru.mirea.dancestudio.service.EnrollmentQueryService;

import java.io.PrintStream;
import java.time.LocalDate;
import java.util.List;

/** Меню «Поиск»: четыре способа поиска записей на занятия. */
public class SearchMenu extends AbstractMenu {

    private final EnrollmentQueryService queries;

    public SearchMenu(ConsoleInput in, PrintStream out, Printer printer, EnrollmentQueryService queries) {
        super(in, out, printer);
        this.queries = queries;
    }

    @Override
    public String title() {
        return "ПОИСК ЗАПИСЕЙ НА ЗАНЯТИЯ";
    }

    @Override
    protected List<Item> items() {
        return List.of(
                new Item("По ФИО клиента", () -> printer.enrollments(
                        queries.searchByClientName(in.readRequired("ФИО клиента (или его часть): ")))),
                new Item("По названию занятия или преподавателю", () -> printer.enrollments(
                        queries.searchByClassOrInstructor(in.readRequired("Название занятия или преподаватель: ")))),
                new Item("По дате занятия", this::byDate),
                new Item("По комментарию", () -> printer.enrollments(
                        queries.searchByNote(in.readRequired("Текст комментария (или его часть): ")))));
    }

    private void byDate() {
        LocalDate date = in.read("Дата занятия (дд.мм.гггг): ", InputParsers.date("Дата занятия"));
        printer.enrollments(queries.searchByClassDate(date));
    }
}
