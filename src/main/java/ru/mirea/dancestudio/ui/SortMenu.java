package ru.mirea.dancestudio.ui;

import ru.mirea.dancestudio.model.EnrollmentSortField;
import ru.mirea.dancestudio.service.EnrollmentQueryService;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/** Меню «Сортировка»: записи на занятия можно упорядочить по шести признакам. */
public class SortMenu extends AbstractMenu {

    private final EnrollmentQueryService queries;

    public SortMenu(ConsoleInput in, PrintStream out, Printer printer, EnrollmentQueryService queries) {
        super(in, out, printer);
        this.queries = queries;
    }

    @Override
    public String title() {
        return "СОРТИРОВКА ЗАПИСЕЙ НА ЗАНЯТИЯ";
    }

    @Override
    protected List<Item> items() {
        List<Item> items = new ArrayList<>();
        for (EnrollmentSortField field : EnrollmentSortField.values()) {
            items.add(new Item("По " + field.getLabel(), () -> sortBy(field)));
        }
        return items;
    }

    private void sortBy(EnrollmentSortField field) {
        boolean ascending = in.choose("Порядок сортировки:", List.of(Boolean.TRUE, Boolean.FALSE),
                asc -> asc ? "По возрастанию" : "По убыванию");
        out.println("Записи, отсортированные по " + field.getLabel()
                + (ascending ? " (по возрастанию):" : " (по убыванию):"));
        printer.enrollments(queries.findAllSorted(field, ascending));
    }
}
