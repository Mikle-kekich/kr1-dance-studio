package ru.mirea.dancestudio.service;

import ru.mirea.dancestudio.exception.BusinessException;
import ru.mirea.dancestudio.exception.EntityNotFoundException;
import ru.mirea.dancestudio.model.ClassOccupancy;
import ru.mirea.dancestudio.model.DanceClass;
import ru.mirea.dancestudio.repository.DanceClassRepository;
import ru.mirea.dancestudio.repository.EnrollmentRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** Бизнес-логика расписания: проверки данных занятия, вместимость, правила удаления. */
public class DanceClassService {

    private final DanceClassRepository classes;
    private final EnrollmentRepository enrollments;
    private final Clock clock;

    public DanceClassService(DanceClassRepository classes, EnrollmentRepository enrollments, Clock clock) {
        this.classes = classes;
        this.enrollments = enrollments;
        this.clock = clock;
    }

    public DanceClass getById(long id) {
        return classes.findById(id).orElseThrow(() -> EntityNotFoundException.danceClass(id));
    }

    /** Все занятия с числом занятых мест (занятость считается одним SQL-запросом с GROUP BY). */
    public List<ClassOccupancy> listWithOccupancy() {
        Map<Long, Long> taken = enrollments.countSeatsTakenByClass();
        return classes.findAll().stream()
                .map(c -> new ClassOccupancy(c, taken.getOrDefault(c.getId(), 0L).intValue()))
                .toList();
    }

    /** Только предстоящие занятия (Stream API: фильтрация по времени начала). */
    public List<ClassOccupancy> listUpcomingWithOccupancy() {
        LocalDateTime now = LocalDateTime.now(clock);
        return listWithOccupancy().stream()
                .filter(o -> o.danceClass().getStartTime().isAfter(now))
                .toList();
    }

    public ClassOccupancy getOccupancy(long id) {
        DanceClass danceClass = getById(id);
        return new ClassOccupancy(danceClass, (int) enrollments.countSeatsTaken(id));
    }

    /** БП-15: данные занятия корректны, новое занятие нельзя назначить на прошедшее время. */
    public DanceClass create(DanceClass danceClass) {
        validate(danceClass);
        requireFuture(danceClass.getStartTime(), "Нельзя создать занятие в прошлом.");
        return classes.save(danceClass);
    }

    /**
     * БП-15: при переносе занятие должно оставаться в будущем;
     * БП-16: вместимость нельзя сделать меньше числа уже занятых мест.
     */
    public DanceClass update(DanceClass changed) {
        if (changed.getId() == null) {
            throw new BusinessException("Нельзя изменить занятие без ID.");
        }
        DanceClass stored = getById(changed.getId());
        validate(changed);
        if (!changed.getStartTime().equals(stored.getStartTime())) {
            requireFuture(changed.getStartTime(), "Нельзя перенести занятие на прошедшее время.");
        }
        long taken = enrollments.countSeatsTaken(changed.getId());
        if (changed.getCapacity() < taken) {
            throw new BusinessException("Вместимость нельзя сделать меньше числа занятых мест (" + taken + ").");
        }
        classes.update(changed);
        return changed;
    }

    /** БП-17: нельзя удалить занятие, на которое есть записи. */
    public void delete(long id) {
        DanceClass danceClass = getById(id);
        long count = enrollments.countByClassId(id);
        if (count > 0) {
            throw new BusinessException("Нельзя удалить занятие «" + danceClass.getTitle()
                    + "»: на него есть записи (" + count + "). Удалять можно только занятия без записей.");
        }
        classes.deleteById(id);
    }

    private void validate(DanceClass c) {
        c.setTitle(Validation.requireText(c.getTitle(), "Название", 2, 100));
        c.setInstructor(Validation.requireText(c.getInstructor(), "Преподаватель", 2, 100));
        Validation.requireNotNull(c.getStyle(), "Направление");
        Validation.requireNotNull(c.getLevel(), "Уровень");
        Validation.requireNotNull(c.getStartTime(), "Дата и время начала");
        Validation.requireRange(c.getDurationMinutes(), 15, 240, "Длительность (мин)");
        Validation.requireRange(c.getCapacity(), 1, 100, "Вместимость");
        Validation.requireRange(c.getMinAge(), 0, 99, "Минимальный возраст");
        c.setPrice(Validation.requireMoney(c.getPrice(), "Цена"));
    }

    private void requireFuture(LocalDateTime time, String message) {
        if (!time.isAfter(LocalDateTime.now(clock))) {
            throw new BusinessException(message);
        }
    }
}
