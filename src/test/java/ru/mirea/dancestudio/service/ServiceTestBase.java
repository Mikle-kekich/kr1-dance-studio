package ru.mirea.dancestudio.service;

import org.junit.jupiter.api.BeforeEach;
import ru.mirea.dancestudio.model.Client;
import ru.mirea.dancestudio.model.DanceClass;
import ru.mirea.dancestudio.model.Enrollment;
import ru.mirea.dancestudio.model.EnrollmentStatus;

import java.math.BigDecimal;

/** Общая заготовка тестов сервисов: хранилища в памяти, сервисы и несколько готовых объектов. */
abstract class ServiceTestBase {

    protected FakeRepositories.Clients clients;
    protected FakeRepositories.Classes classes;
    protected FakeEnrollmentRepository enrollments;

    protected ClientService clientService;
    protected DanceClassService classService;
    protected EnrollmentService enrollmentService;
    protected EnrollmentQueryService queryService;
    protected StatisticsService statisticsService;

    /** Взрослая клиентка (31 год) и подросток (14 лет). */
    protected Client adult;
    protected Client teen;
    /** Занятие через 2 дня (от 12 лет), прошедшее занятие, занятие для 18+ и занятие на одно место. */
    protected DanceClass upcoming;
    protected DanceClass past;
    protected DanceClass adultsOnly;
    protected DanceClass singleSeat;

    @BeforeEach
    void setUpBase() {
        clients = new FakeRepositories.Clients();
        classes = new FakeRepositories.Classes();
        enrollments = new FakeEnrollmentRepository();
        clientService = new ClientService(clients, enrollments, TestData.CLOCK);
        classService = new DanceClassService(classes, enrollments, TestData.CLOCK);
        enrollmentService = new EnrollmentService(enrollments, clients, classes, TestData.CLOCK);
        queryService = new EnrollmentQueryService(enrollments, clients);
        statisticsService = new StatisticsService(clients, classes, enrollments, TestData.CLOCK);

        adult = clients.save(TestData.adult("Иванова Анна", "+79011112233", "anna@example.com"));
        teen = clients.save(TestData.child("Новиков Илья", "+79066667788", "ilya@example.com"));
        upcoming = classes.save(TestData.classIn(2, 10, 12));
        past = classes.save(TestData.classIn(-2, 10, 0));
        adultsOnly = classes.save(TestData.classIn(3, 10, 18));
        singleSeat = classes.save(TestData.classIn(4, 1, 0));
    }

    /** Кладёт запись прямо в хранилище (в обход бизнес-правил), чтобы подготовить нужное состояние. */
    protected Enrollment seed(Client client, DanceClass danceClass, EnrollmentStatus status, boolean paid) {
        Enrollment enrollment = new Enrollment(null, client, danceClass, status,
                new BigDecimal("800.00"), paid, null, TestData.NOW);
        return enrollments.save(enrollment);
    }
}
