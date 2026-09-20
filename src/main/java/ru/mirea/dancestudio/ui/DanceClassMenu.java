package ru.mirea.dancestudio.ui;

import ru.mirea.dancestudio.model.ClassOccupancy;
import ru.mirea.dancestudio.model.DanceClass;
import ru.mirea.dancestudio.model.DanceLevel;
import ru.mirea.dancestudio.model.DanceStyle;
import ru.mirea.dancestudio.service.DanceClassService;
import ru.mirea.dancestudio.util.Formats;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Меню «Занятия»: расписание студии. Правила проверки данных находятся в DanceClassService. */
public class DanceClassMenu extends AbstractMenu {

    private final DanceClassService service;

    public DanceClassMenu(ConsoleInput in, PrintStream out, Printer printer, DanceClassService service) {
        super(in, out, printer);
        this.service = service;
    }

    @Override
    public String title() {
        return "ЗАНЯТИЯ (РАСПИСАНИЕ)";
    }

    @Override
    protected List<Item> items() {
        return List.of(
                new Item("Показать всё расписание", () -> printer.classes(service.listWithOccupancy())),
                new Item("Показать предстоящие занятия", () -> printer.classes(service.listUpcomingWithOccupancy())),
                new Item("Найти занятие по ID", this::showById),
                new Item("Добавить занятие", this::add),
                new Item("Изменить занятие", this::edit),
                new Item("Удалить занятие", this::delete));
    }

    private void showById() {
        long id = in.read("Введите ID занятия: ", InputParsers.id("ID"));
        printer.danceClass(service.getOccupancy(id));
    }

    private void add() {
        out.println("Добавление занятия (для отмены введите «отмена»)");
        String title = in.readRequired("Название: ");
        DanceStyle style = in.choose("Направление:", List.of(DanceStyle.values()), DanceStyle::getLabel);
        DanceLevel level = in.choose("Уровень:", List.of(DanceLevel.values()), DanceLevel::getLabel);
        String instructor = in.readRequired("Преподаватель: ");
        LocalDateTime start = in.read("Начало (дд.мм.гггг чч:мм): ", InputParsers.dateTime("Начало"));
        int duration = in.read("Длительность, минут: ", InputParsers.integer("Длительность"));
        int capacity = in.read("Вместимость (мест): ", InputParsers.integer("Вместимость"));
        int minAge = in.read("Минимальный возраст (0 - без ограничений): ",
                InputParsers.integer("Минимальный возраст"));
        BigDecimal price = in.read("Цена, руб.: ", InputParsers.money("Цена"));
        DanceClass created = service.create(
                new DanceClass(title, style, level, instructor, start, duration, capacity, minAge, price));
        out.println("Занятие добавлено. ID = " + created.getId());
        printer.danceClass(service.getOccupancy(created.getId()));
    }

    private void edit() {
        long id = in.read("Введите ID занятия: ", InputParsers.id("ID"));
        ClassOccupancy occupancy = service.getOccupancy(id);
        printer.danceClass(occupancy);
        DanceClass c = occupancy.danceClass();
        out.println("Введите новые значения (Enter - оставить прежнее, «отмена» - выйти)");
        c.setTitle(in.readTextOrKeep("Название", c.getTitle()));
        c.setStyle(in.chooseOrKeep("Направление:", List.of(DanceStyle.values()), DanceStyle::getLabel, c.getStyle()));
        c.setLevel(in.chooseOrKeep("Уровень:", List.of(DanceLevel.values()), DanceLevel::getLabel, c.getLevel()));
        c.setInstructor(in.readTextOrKeep("Преподаватель", c.getInstructor()));
        c.setStartTime(in.readOrKeep("Начало [" + Formats.dateTime(c.getStartTime()) + "]: ",
                c.getStartTime(), InputParsers.dateTime("Начало")));
        c.setDurationMinutes(in.readOrKeep("Длительность, минут [" + c.getDurationMinutes() + "]: ",
                c.getDurationMinutes(), InputParsers.integer("Длительность")));
        c.setCapacity(in.readOrKeep("Вместимость [" + c.getCapacity() + "]: ",
                c.getCapacity(), InputParsers.integer("Вместимость")));
        c.setMinAge(in.readOrKeep("Минимальный возраст [" + c.getMinAge() + "]: ",
                c.getMinAge(), InputParsers.integer("Минимальный возраст")));
        c.setPrice(in.readOrKeep("Цена [" + Formats.money(c.getPrice()) + "]: ",
                c.getPrice(), InputParsers.money("Цена")));
        service.update(c);
        out.println("Занятие сохранено.");
        printer.danceClass(service.getOccupancy(id));
    }

    private void delete() {
        long id = in.read("Введите ID занятия: ", InputParsers.id("ID"));
        ClassOccupancy occupancy = service.getOccupancy(id);
        printer.danceClass(occupancy);
        if (in.confirm("Удалить занятие «" + occupancy.danceClass().getTitle() + "»?")) {
            service.delete(id);
            out.println("Занятие удалено.");
        } else {
            out.println("Удаление отменено.");
        }
    }
}
