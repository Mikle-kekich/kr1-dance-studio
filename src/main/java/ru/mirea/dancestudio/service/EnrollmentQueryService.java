package ru.mirea.dancestudio.service;

import ru.mirea.dancestudio.exception.BusinessException;
import ru.mirea.dancestudio.exception.EntityNotFoundException;
import ru.mirea.dancestudio.model.DanceStyle;
import ru.mirea.dancestudio.model.Enrollment;
import ru.mirea.dancestudio.model.EnrollmentSortField;
import ru.mirea.dancestudio.model.EnrollmentStatus;
import ru.mirea.dancestudio.repository.ClientRepository;
import ru.mirea.dancestudio.repository.EnrollmentRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;

/**
 * Поиск, фильтрация и сортировка записей на занятия.
 * Поиск по тексту выполняет база данных (параметризованный ILIKE), а фильтрация и сортировка
 * выполняются в Java с помощью Stream API, Predicate и Comparator.
 */
public class EnrollmentQueryService {

    private final EnrollmentRepository enrollments;
    private final ClientRepository clients;

    public EnrollmentQueryService(EnrollmentRepository enrollments, ClientRepository clients) {
        this.enrollments = enrollments;
        this.clients = clients;
    }

    // ---------------------------- поиск ----------------------------

    public List<Enrollment> searchByClientName(String term) {
        return enrollments.searchByClientName(Validation.requireSearchTerm(term));
    }

    /** Поиск по названию занятия или ФИО преподавателя. */
    public List<Enrollment> searchByClassOrInstructor(String term) {
        return enrollments.searchByClassTitleOrInstructor(Validation.requireSearchTerm(term));
    }

    public List<Enrollment> searchByNote(String term) {
        return enrollments.searchByNote(Validation.requireSearchTerm(term));
    }

    /** Записи на занятия, которые проходят в указанный день. */
    public List<Enrollment> searchByClassDate(LocalDate date) {
        Validation.requireNotNull(date, "Дата");
        return enrollments.findByClassDate(date);
    }

    // ------------------------- фильтрация -------------------------

    public List<Enrollment> filterByStatus(EnrollmentStatus status) {
        Validation.requireNotNull(status, "Статус");
        return filter(e -> e.getStatus() == status);
    }

    public List<Enrollment> filterByPaid(boolean paid) {
        return filter(e -> e.isPaid() == paid);
    }

    public List<Enrollment> filterByStyle(DanceStyle style) {
        Validation.requireNotNull(style, "Направление");
        return filter(e -> e.getDanceClass().getStyle() == style);
    }

    /** Период по дате занятия; любую из границ можно не указывать (null), но не обе сразу. */
    public List<Enrollment> filterByClassDateRange(LocalDate from, LocalDate to) {
        if (from == null && to == null) {
            throw new BusinessException("Укажите хотя бы одну границу периода.");
        }
        if (from != null && to != null && from.isAfter(to)) {
            throw new BusinessException("Начальная дата не может быть позже конечной.");
        }
        return filter(e -> {
            LocalDate day = e.getDanceClass().getStartTime().toLocalDate();
            return (from == null || !day.isBefore(from)) && (to == null || !day.isAfter(to));
        });
    }

    /** БП: нельзя указать несуществующего клиента - сначала проверяем, что он есть. */
    public List<Enrollment> filterByClient(long clientId) {
        clients.findById(clientId).orElseThrow(() -> EntityNotFoundException.client(clientId));
        return filter(e -> e.getClient().getId().equals(clientId));
    }

    private List<Enrollment> filter(Predicate<Enrollment> condition) {
        return enrollments.findAll().stream().filter(condition).toList();
    }

    // ------------------------- сортировка -------------------------

    public List<Enrollment> findAllSorted(EnrollmentSortField field, boolean ascending) {
        return sorted(enrollments.findAll(), field, ascending);
    }

    /** Возвращает новый отсортированный список, исходный не изменяется. */
    public List<Enrollment> sorted(List<Enrollment> source, EnrollmentSortField field, boolean ascending) {
        Validation.requireNotNull(field, "Поле сортировки");
        return source.stream().sorted(field.comparator(ascending)).toList();
    }
}
