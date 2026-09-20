package ru.mirea.dancestudio.ui;

import ru.mirea.dancestudio.model.Client;
import ru.mirea.dancestudio.service.ClientService;
import ru.mirea.dancestudio.util.Formats;

import java.io.PrintStream;
import java.time.LocalDate;
import java.util.List;

/** Меню «Клиенты»: просмотр, добавление, изменение, удаление и поиск клиентов. */
public class ClientMenu extends AbstractMenu {

    private final ClientService service;

    public ClientMenu(ConsoleInput in, PrintStream out, Printer printer, ClientService service) {
        super(in, out, printer);
        this.service = service;
    }

    @Override
    public String title() {
        return "КЛИЕНТЫ";
    }

    @Override
    protected List<Item> items() {
        return List.of(
                new Item("Показать всех клиентов", () -> printer.clients(service.findAll())),
                new Item("Найти клиента по ID", this::showById),
                new Item("Добавить клиента", this::add),
                new Item("Изменить клиента", this::edit),
                new Item("Удалить клиента", this::delete),
                new Item("Поиск по ФИО, телефону или email", this::search));
    }

    private void showById() {
        long id = in.read("Введите ID клиента: ", InputParsers.id("ID"));
        printer.client(service.getById(id));
    }

    private void add() {
        out.println("Добавление клиента (для отмены введите «отмена»)");
        String name = in.readRequired("ФИО: ");
        String phone = in.readRequired("Телефон: ");
        String email = in.readRequired("Email: ");
        LocalDate birthDate = in.read("Дата рождения (дд.мм.гггг): ", InputParsers.date("Дата рождения"));
        Client created = service.create(new Client(name, phone, email, birthDate));
        out.println("Клиент добавлен. ID = " + created.getId());
        printer.client(created);
    }

    private void edit() {
        long id = in.read("Введите ID клиента: ", InputParsers.id("ID"));
        Client client = service.getById(id);
        printer.client(client);
        out.println("Введите новые значения (Enter - оставить прежнее, «отмена» - выйти)");
        client.setFullName(in.readTextOrKeep("ФИО", client.getFullName()));
        client.setPhone(in.readTextOrKeep("Телефон", client.getPhone()));
        client.setEmail(in.readTextOrKeep("Email", client.getEmail()));
        client.setBirthDate(in.readOrKeep(
                "Дата рождения [" + Formats.date(client.getBirthDate()) + "]: ",
                client.getBirthDate(), InputParsers.date("Дата рождения")));
        Client saved = service.update(client);
        out.println("Данные клиента сохранены.");
        printer.client(saved);
    }

    private void delete() {
        long id = in.read("Введите ID клиента: ", InputParsers.id("ID"));
        Client client = service.getById(id);
        printer.client(client);
        if (in.confirm("Удалить клиента «" + client.getFullName() + "»?")) {
            service.delete(id);
            out.println("Клиент удалён.");
        } else {
            out.println("Удаление отменено.");
        }
    }

    private void search() {
        String term = in.readRequired("Строка поиска (ФИО, телефон или email): ");
        printer.clients(service.search(term));
    }
}
