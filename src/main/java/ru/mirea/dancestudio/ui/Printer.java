package ru.mirea.dancestudio.ui;

import ru.mirea.dancestudio.model.ClassOccupancy;
import ru.mirea.dancestudio.model.Client;
import ru.mirea.dancestudio.model.DanceClass;
import ru.mirea.dancestudio.model.Enrollment;
import ru.mirea.dancestudio.model.Statistics;
import ru.mirea.dancestudio.model.TableData;
import ru.mirea.dancestudio.util.Formats;
import ru.mirea.dancestudio.util.TablePrinter;

import java.io.PrintStream;
import java.time.Clock;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Вывод списков (таблицами) и карточек объектов в консоль. Никакой бизнес-логики и SQL. */
public class Printer {

    private final PrintStream out;
    private final Clock clock;

    public Printer(PrintStream out, Clock clock) {
        this.out = out;
        this.clock = clock;
    }

    // ----------------------------- списки -----------------------------

    public void clients(List<Client> clients) {
        LocalDate today = LocalDate.now(clock);
        table(List.of("ID", "ФИО", "Телефон", "Email", "Дата рождения", "Возраст"),
                clients.stream().map(c -> List.of(String.valueOf(c.getId()), c.getFullName(), c.getPhone(),
                        c.getEmail(), Formats.date(c.getBirthDate()), String.valueOf(c.getAge(today)))).toList());
    }

    public void classes(List<ClassOccupancy> classes) {
        table(List.of("ID", "Название", "Направление", "Уровень", "Преподаватель", "Начало", "Места", "От (лет)", "Цена"),
                classes.stream().map(o -> {
                    DanceClass c = o.danceClass();
                    return List.of(String.valueOf(c.getId()), c.getTitle(), c.getStyle().getLabel(),
                            c.getLevel().getLabel(), c.getInstructor(), Formats.dateTime(c.getStartTime()),
                            o.booked() + "/" + o.capacity(), String.valueOf(c.getMinAge()),
                            Formats.money(c.getPrice()));
                }).toList());
    }

    public void enrollments(List<Enrollment> enrollments) {
        table(List.of("ID", "Клиент", "Занятие", "Начало занятия", "Цена", "Оплата", "Статус"),
                enrollments.stream().map(e -> List.of(String.valueOf(e.getId()), e.getClient().getFullName(),
                        e.getDanceClass().getTitle(), Formats.dateTime(e.getDanceClass().getStartTime()),
                        Formats.money(e.getPrice()), Formats.yesNo(e.isPaid()), e.getStatus().getLabel())).toList());
    }

    private void table(List<String> headers, List<List<String>> rows) {
        if (rows.isEmpty()) {
            out.println("Ничего не найдено.");
            return;
        }
        out.println(TablePrinter.render(headers, rows));
        out.println("Всего записей: " + rows.size());
    }

    // ----------------------------- карточки -----------------------------

    public void client(Client c) {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("ID", String.valueOf(c.getId()));
        fields.put("ФИО", c.getFullName());
        fields.put("Телефон", c.getPhone());
        fields.put("Email", c.getEmail());
        fields.put("Дата рождения", Formats.date(c.getBirthDate()) + " (" + c.getAge(LocalDate.now(clock)) + " лет)");
        fields.put("Зарегистрирован", Formats.dateTime(c.getRegisteredAt()));
        card("Клиент", fields);
    }

    public void danceClass(ClassOccupancy o) {
        DanceClass c = o.danceClass();
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("ID", String.valueOf(c.getId()));
        fields.put("Название", c.getTitle());
        fields.put("Направление", c.getStyle().getLabel());
        fields.put("Уровень", c.getLevel().getLabel());
        fields.put("Преподаватель", c.getInstructor());
        fields.put("Начало", Formats.dateTime(c.getStartTime()));
        fields.put("Длительность", c.getDurationMinutes() + " мин");
        fields.put("Места (занято/всего)", o.booked() + "/" + o.capacity() + ", свободно " + o.free());
        fields.put("Минимальный возраст", c.getMinAge() + " лет");
        fields.put("Цена", Formats.money(c.getPrice()));
        card("Занятие", fields);
    }

    public void enrollment(Enrollment e) {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("ID записи", String.valueOf(e.getId()));
        fields.put("Клиент", e.getClient().getFullName() + " (ID " + e.getClient().getId() + ")");
        fields.put("Телефон клиента", e.getClient().getPhone());
        fields.put("Занятие", e.getDanceClass().getTitle() + " (ID " + e.getDanceClass().getId() + ")");
        fields.put("Направление", e.getDanceClass().getStyle().getLabel());
        fields.put("Преподаватель", e.getDanceClass().getInstructor());
        fields.put("Начало занятия", Formats.dateTime(e.getDanceClass().getStartTime()));
        fields.put("Статус", e.getStatus().getLabel());
        fields.put("Оплачено", Formats.yesNo(e.isPaid()));
        fields.put("Стоимость", Formats.money(e.getPrice()));
        fields.put("Комментарий", Formats.orDash(e.getNote()));
        fields.put("Запись создана", Formats.dateTime(e.getCreatedAt()));
        card("Запись на занятие", fields);
    }

    private void card(String title, Map<String, String> fields) {
        out.println("--- " + title + " ---");
        fields.forEach((name, value) -> out.println(String.format("%-24s %s", name + ":", value)));
    }

    // ----------------------------- прочее -----------------------------

    public void statistics(Statistics statistics) {
        out.println("Статистика танцевальной студии");
        out.println("-".repeat(70));
        statistics.toRows().forEach((name, value) -> out.println(String.format("%-46s %s", name + ":", value)));
    }

    public void databaseTable(TableData data) {
        out.println("Таблица «" + data.name() + "» (строк: " + data.totalRows() + ")");
        if (data.rows().isEmpty()) {
            out.println("(таблица пуста)");
            return;
        }
        out.println(TablePrinter.render(data.columns(), data.rows(), 26));
        if (data.totalRows() > data.rows().size()) {
            out.println("Показаны первые " + data.rows().size() + " из " + data.totalRows() + " строк.");
        }
    }
}
