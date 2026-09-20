package ru.mirea.dancestudio.ui;

import ru.mirea.dancestudio.model.DanceClass;
import ru.mirea.dancestudio.model.Enrollment;
import ru.mirea.dancestudio.model.EnrollmentStatus;
import ru.mirea.dancestudio.service.ClientService;
import ru.mirea.dancestudio.service.DanceClassService;
import ru.mirea.dancestudio.service.EnrollmentService;
import ru.mirea.dancestudio.util.Formats;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.List;

/** Меню «Записи на занятия» (основная сущность): CRUD и смена статуса. Бизнес-правила - в EnrollmentService. */
public class EnrollmentMenu extends AbstractMenu {

    private final EnrollmentService service;
    private final ClientService clientService;
    private final DanceClassService classService;

    public EnrollmentMenu(ConsoleInput in, PrintStream out, Printer printer, EnrollmentService service,
                          ClientService clientService, DanceClassService classService) {
        super(in, out, printer);
        this.service = service;
        this.clientService = clientService;
        this.classService = classService;
    }

    @Override
    public String title() {
        return "ЗАПИСИ НА ЗАНЯТИЯ";
    }

    @Override
    protected List<Item> items() {
        return List.of(
                new Item("Показать все записи", () -> printer.enrollments(service.findAll())),
                new Item("Найти запись по ID", this::showById),
                new Item("Создать запись", this::create),
                new Item("Изменить запись", this::edit),
                new Item("Изменить статус записи", this::changeStatus),
                new Item("Удалить запись", this::delete));
    }

    private void showById() {
        long id = in.read("Введите ID записи: ", InputParsers.id("ID"));
        printer.enrollment(service.getById(id));
    }

    private void create() {
        out.println("Создание записи на занятие (для отмены введите «отмена»)");
        printer.clients(clientService.findAll());
        long clientId = in.read("Введите ID клиента: ", InputParsers.id("ID клиента"));
        clientService.getById(clientId);
        printer.classes(classService.listUpcomingWithOccupancy());
        long classId = in.read("Введите ID занятия: ", InputParsers.id("ID занятия"));
        DanceClass danceClass = classService.getById(classId);
        BigDecimal price = in.readOrKeep("Стоимость [" + Formats.money(danceClass.getPrice()) + "]: ",
                danceClass.getPrice(), InputParsers.money("Стоимость"));
        boolean paid = in.readOrKeep("Оплачено? (да/нет) [нет]: ", false, InputParsers.yesNo());
        String note = in.readOptionalText("Комментарий (Enter - без комментария): ");
        Enrollment created = service.create(clientId, classId, price, paid, note);
        out.println("Запись создана. ID = " + created.getId());
        printer.enrollment(created);
    }

    private void edit() {
        long id = in.read("Введите ID записи: ", InputParsers.id("ID"));
        Enrollment enrollment = service.getById(id);
        printer.enrollment(enrollment);
        out.println("Введите новые значения (Enter - оставить прежнее, «отмена» - выйти)");
        long classId = in.readOrKeep("ID занятия [" + enrollment.getDanceClass().getId() + "]: ",
                enrollment.getDanceClass().getId(), InputParsers.id("ID занятия"));
        BigDecimal price = in.readOrKeep("Стоимость [" + Formats.money(enrollment.getPrice()) + "]: ",
                enrollment.getPrice(), InputParsers.money("Стоимость"));
        boolean paid = in.readOrKeep("Оплачено? (да/нет) [" + Formats.yesNo(enrollment.isPaid()) + "]: ",
                enrollment.isPaid(), InputParsers.yesNo());
        String line = in.readOptionalText("Комментарий [" + Formats.orDash(enrollment.getNote())
                + "] (Enter - оставить, «-» - очистить): ");
        String note = line.isEmpty() ? enrollment.getNote() : ("-".equals(line) ? null : line);
        Enrollment saved = service.update(id, classId, price, paid, note);
        out.println("Запись сохранена.");
        printer.enrollment(saved);
    }

    private void changeStatus() {
        long id = in.read("Введите ID записи: ", InputParsers.id("ID"));
        Enrollment enrollment = service.getById(id);
        printer.enrollment(enrollment);
        EnrollmentStatus target = in.choose("Новый статус:", List.of(EnrollmentStatus.values()),
                EnrollmentStatus::getLabel);
        Enrollment saved = service.changeStatus(id, target);
        out.println("Статус изменён на «" + saved.getStatus().getLabel() + "».");
    }

    private void delete() {
        long id = in.read("Введите ID записи: ", InputParsers.id("ID"));
        Enrollment enrollment = service.getById(id);
        printer.enrollment(enrollment);
        if (in.confirm("Удалить эту запись?")) {
            service.delete(id);
            out.println("Запись удалена.");
        } else {
            out.println("Удаление отменено.");
        }
    }
}
