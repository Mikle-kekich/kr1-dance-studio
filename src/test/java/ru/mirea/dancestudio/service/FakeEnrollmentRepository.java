package ru.mirea.dancestudio.service;

import ru.mirea.dancestudio.exception.EntityNotFoundException;
import ru.mirea.dancestudio.model.Enrollment;
import ru.mirea.dancestudio.model.EnrollmentStatus;
import ru.mirea.dancestudio.repository.EnrollmentRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/** Хранилище записей в памяти для модульных тестов (см. {@link FakeRepositories}). */
final class FakeEnrollmentRepository implements EnrollmentRepository {

    private final Map<Long, Enrollment> data = new LinkedHashMap<>();
    private long sequence = 0;

    @Override
    public Enrollment save(Enrollment enrollment) {
        enrollment.setId(++sequence);
        data.put(enrollment.getId(), FakeRepositories.copy(enrollment));
        return enrollment;
    }

    @Override
    public Optional<Enrollment> findById(long id) {
        return Optional.ofNullable(data.get(id)).map(FakeRepositories::copy);
    }

    @Override
    public List<Enrollment> findAll() {
        return select(e -> true);
    }

    @Override
    public void update(Enrollment enrollment) {
        if (!data.containsKey(enrollment.getId())) {
            throw EntityNotFoundException.enrollment(enrollment.getId());
        }
        data.put(enrollment.getId(), FakeRepositories.copy(enrollment));
    }

    @Override
    public boolean deleteById(long id) {
        return data.remove(id) != null;
    }

    @Override
    public long count() {
        return data.size();
    }

    private List<Enrollment> select(Predicate<Enrollment> condition) {
        return data.values().stream().filter(condition).map(FakeRepositories::copy).toList();
    }

    private static boolean contains(String text, String term) {
        return text != null && text.toLowerCase().contains(term.toLowerCase());
    }

    @Override
    public List<Enrollment> findByClientId(long clientId) {
        return select(e -> e.getClient().getId() == clientId);
    }

    @Override
    public List<Enrollment> searchByClientName(String term) {
        return select(e -> contains(e.getClient().getFullName(), term));
    }

    @Override
    public List<Enrollment> searchByClassTitleOrInstructor(String term) {
        return select(e -> contains(e.getDanceClass().getTitle(), term)
                || contains(e.getDanceClass().getInstructor(), term));
    }

    @Override
    public List<Enrollment> searchByNote(String term) {
        return select(e -> contains(e.getNote(), term));
    }

    @Override
    public List<Enrollment> findByClassDate(LocalDate date) {
        return select(e -> e.getDanceClass().getStartTime().toLocalDate().equals(date));
    }

    @Override
    public long countByClientId(long clientId) {
        return findByClientId(clientId).size();
    }

    @Override
    public long countByClassId(long classId) {
        return select(e -> e.getDanceClass().getId() == classId).size();
    }

    @Override
    public long countSeatsTaken(long classId) {
        return select(e -> e.getDanceClass().getId() == classId && e.getStatus().occupiesSeat()).size();
    }

    @Override
    public boolean existsActive(long clientId, long classId, Long excludeId) {
        return data.values().stream().anyMatch(e -> e.getClient().getId() == clientId
                && e.getDanceClass().getId() == classId && e.getStatus().occupiesSeat()
                && (excludeId == null || !e.getId().equals(excludeId)));
    }

    @Override
    public Map<EnrollmentStatus, Long> countByStatus() {
        Map<EnrollmentStatus, Long> result = new EnumMap<>(EnrollmentStatus.class);
        for (EnrollmentStatus status : EnrollmentStatus.values()) {
            result.put(status, data.values().stream().filter(e -> e.getStatus() == status).count());
        }
        return result;
    }

    @Override
    public Map<Long, Long> countSeatsTakenByClass() {
        Map<Long, Long> result = new HashMap<>();
        data.values().stream().filter(e -> e.getStatus().occupiesSeat())
                .forEach(e -> result.merge(e.getDanceClass().getId(), 1L, Long::sum));
        return result;
    }

    @Override
    public BigDecimal sumRevenue() {
        return data.values().stream().filter(e -> e.isPaid() && e.getStatus().occupiesSeat())
                .map(Enrollment::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal averagePrice() {
        if (data.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal sum = data.values().stream().map(Enrollment::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(data.size()), 2, RoundingMode.HALF_UP);
    }
}
