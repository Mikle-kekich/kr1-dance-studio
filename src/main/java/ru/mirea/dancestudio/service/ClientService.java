package ru.mirea.dancestudio.service;

import ru.mirea.dancestudio.exception.BusinessException;
import ru.mirea.dancestudio.exception.EntityNotFoundException;
import ru.mirea.dancestudio.model.Client;
import ru.mirea.dancestudio.repository.ClientRepository;
import ru.mirea.dancestudio.repository.EnrollmentRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/** Бизнес-логика работы с клиентами: проверки данных, уникальность телефона и email, правила удаления. */
public class ClientService {

    static final int MIN_CLIENT_AGE = 3;
    static final int MAX_CLIENT_AGE = 100;
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+[.][A-Za-z]{2,}$");

    private final ClientRepository clients;
    private final EnrollmentRepository enrollments;
    private final Clock clock;

    public ClientService(ClientRepository clients, EnrollmentRepository enrollments, Clock clock) {
        this.clients = clients;
        this.enrollments = enrollments;
        this.clock = clock;
    }

    public List<Client> findAll() {
        return clients.findAll();
    }

    public Client getById(long id) {
        return clients.findById(id).orElseThrow(() -> EntityNotFoundException.client(id));
    }

    public List<Client> search(String term) {
        return clients.search(Validation.requireSearchTerm(term));
    }

    /** БП-1: данные клиента корректны; БП-2: телефон и email уникальны. */
    public Client create(Client client) {
        prepare(client, null);
        client.setRegisteredAt(LocalDateTime.now(clock));
        return clients.save(client);
    }

    public Client update(Client client) {
        if (client.getId() == null) {
            throw new BusinessException("Нельзя изменить клиента без ID.");
        }
        getById(client.getId());
        prepare(client, client.getId());
        clients.update(client);
        return client;
    }

    /** БП-18: нельзя удалить клиента, у которого есть записи на занятия (история посещений сохраняется). */
    public void delete(long id) {
        Client client = getById(id);
        long count = enrollments.countByClientId(id);
        if (count > 0) {
            throw new BusinessException("Нельзя удалить клиента «" + client.getFullName()
                    + "»: у него есть записи на занятия (" + count + "). Удалять можно только клиентов без записей.");
        }
        clients.deleteById(id);
    }

    private void prepare(Client client, Long ownId) {
        client.setFullName(normalizeName(client.getFullName()));
        client.setPhone(normalizePhone(client.getPhone()));
        client.setEmail(normalizeEmail(client.getEmail()));
        validateBirthDate(client.getBirthDate());

        clients.findByPhone(client.getPhone())
                .filter(other -> !other.getId().equals(ownId))
                .ifPresent(other -> {
                    throw new BusinessException("Клиент с телефоном " + other.getPhone()
                            + " уже существует: " + other.getFullName() + " (ID " + other.getId() + ").");
                });
        clients.findByEmail(client.getEmail())
                .filter(other -> !other.getId().equals(ownId))
                .ifPresent(other -> {
                    throw new BusinessException("Клиент с email " + other.getEmail()
                            + " уже существует: " + other.getFullName() + " (ID " + other.getId() + ").");
                });
    }

    static String normalizeName(String raw) {
        String name = Validation.requireText(raw, "ФИО", 2, 100);
        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (!Character.isLetter(ch) && ch != ' ' && ch != '-' && ch != '.') {
                throw new BusinessException("ФИО может содержать только буквы, пробелы, дефис и точку.");
            }
        }
        if (name.split(" ").length < 2) {
            throw new BusinessException("ФИО должно содержать не менее двух слов: фамилию и имя.");
        }
        return name;
    }

    /** Приводит телефон к виду +79011234567 (8 в начале заменяется на +7). */
    static String normalizePhone(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new BusinessException("Поле «Телефон» обязательно для заполнения.");
        }
        if (raw.chars().anyMatch(Character::isLetter)) {
            throw new BusinessException("Телефон не должен содержать букв. Пример: +7 (901) 123-45-67.");
        }
        boolean international = raw.trim().startsWith("+");
        StringBuilder digits = new StringBuilder();
        for (char ch : raw.toCharArray()) {
            if (ch >= '0' && ch <= '9') {
                digits.append(ch);
            }
        }
        String number = digits.toString();
        if (!international) {
            if (number.length() == 11 && number.startsWith("8")) {
                number = "7" + number.substring(1);
            } else if (number.length() == 10) {
                number = "7" + number;
            }
        }
        if (number.length() < 11 || number.length() > 15) {
            throw new BusinessException("Телефон указан неверно. Пример: +7 (901) 123-45-67.");
        }
        return "+" + number;
    }

    static String normalizeEmail(String raw) {
        String email = Validation.requireText(raw, "Email", 5, 100).toLowerCase(Locale.ROOT);
        if (!EMAIL.matcher(email).matches()) {
            throw new BusinessException("Email указан неверно. Пример: name@example.com.");
        }
        return email;
    }

    private void validateBirthDate(LocalDate birthDate) {
        Validation.requireNotNull(birthDate, "Дата рождения");
        LocalDate today = LocalDate.now(clock);
        if (birthDate.isAfter(today)) {
            throw new BusinessException("Дата рождения не может быть в будущем.");
        }
        int age = Period.between(birthDate, today).getYears();
        if (age < MIN_CLIENT_AGE) {
            throw new BusinessException("Студия принимает клиентов от " + MIN_CLIENT_AGE + " лет.");
        }
        if (age > MAX_CLIENT_AGE) {
            throw new BusinessException("Указан слишком большой возраст: проверьте дату рождения.");
        }
    }
}
