package ru.mirea.dancestudio.service;

import ru.mirea.dancestudio.exception.BusinessException;
import ru.mirea.dancestudio.model.Client;
import ru.mirea.dancestudio.model.DanceClass;
import ru.mirea.dancestudio.model.Enrollment;
import ru.mirea.dancestudio.model.EnrollmentStatus;
import ru.mirea.dancestudio.repository.EnrollmentRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Проверки бизнес-правил (БП) для записей на занятия.
 * Каждый метод молча возвращает управление, если правило соблюдено,
 * и бросает BusinessException с понятным текстом, если нарушено.
 */
final class EnrollmentRules {

    private final EnrollmentRepository enrollments;

    EnrollmentRules(EnrollmentRepository enrollments) {
        this.enrollments = enrollments;
    }

    /** БП-4: занятие ещё не началось. */
    void classNotStarted(DanceClass danceClass, LocalDateTime now, String message) {
        if (!danceClass.getStartTime().isAfter(now)) {
            throw new BusinessException(message);
        }
    }

    /** БП-5: возраст клиента не ниже минимального для занятия. */
    void ageAllowed(Client client, DanceClass danceClass, LocalDate today) {
        int age = client.getAge(today);
        if (age < danceClass.getMinAge()) {
            throw new BusinessException("Клиент не подходит по возрасту: занятие «" + danceClass.getTitle()
                    + "» с " + danceClass.getMinAge() + " лет, клиенту " + age + ".");
        }
    }

    /** БП-6: у клиента нет другой действующей записи на это же занятие. */
    void noActiveDuplicate(long clientId, long classId, Long ownId) {
        if (enrollments.existsActive(clientId, classId, ownId)) {
            throw new BusinessException("Клиент уже записан на это занятие (действующая запись существует).");
        }
    }

    /** БП-7: на занятии есть свободные места. */
    void seatAvailable(DanceClass danceClass) {
        long taken = enrollments.countSeatsTaken(danceClass.getId());
        if (taken >= danceClass.getCapacity()) {
            throw new BusinessException("На занятии «" + danceClass.getTitle() + "» нет свободных мест ("
                    + taken + " из " + danceClass.getCapacity() + ").");
        }
    }

    /**
     * БП-11: разрешены только переходы CREATED -> CONFIRMED | CANCELLED и CONFIRMED -> COMPLETED | CANCELLED.
     * БП-12: подтвердить можно только оплаченную запись и только до начала занятия.
     * БП-13: завершить запись можно только после начала занятия.
     */
    void statusChange(Enrollment enrollment, EnrollmentStatus target, LocalDateTime now) {
        EnrollmentStatus current = enrollment.getStatus();
        if (!current.canTransitionTo(target)) {
            String allowed = current.allowedNext().isEmpty()
                    ? "Из этого статуса переходов нет."
                    : "Разрешено: " + current.allowedNext().stream()
                            .map(EnrollmentStatus::getLabel).collect(Collectors.joining(", ")) + ".";
            throw new BusinessException("Недопустимый переход статуса: «" + current.getLabel()
                    + "» -> «" + target.getLabel() + "». " + allowed);
        }
        if (target == EnrollmentStatus.CONFIRMED) {
            if (!enrollment.isPaid()) {
                throw new BusinessException("Нельзя подтвердить неоплаченную запись: сначала отметьте оплату.");
            }
            classNotStarted(enrollment.getDanceClass(), now,
                    "Нельзя подтвердить запись на занятие, которое уже началось.");
        }
        if (target == EnrollmentStatus.COMPLETED && enrollment.getDanceClass().getStartTime().isAfter(now)) {
            throw new BusinessException("Нельзя завершить запись: занятие ещё не началось.");
        }
    }

    /** БП-14: удалять можно только записи «Создана» и «Отменена» (история посещений сохраняется). */
    void deletable(Enrollment enrollment) {
        EnrollmentStatus status = enrollment.getStatus();
        if (status != EnrollmentStatus.CREATED && status != EnrollmentStatus.CANCELLED) {
            throw new BusinessException("Запись со статусом «" + status.getLabel() + "» удалить нельзя: "
                    + "удаляются только записи «Создана» и «Отменена». Подтверждённую запись сначала отмените.");
        }
    }
}
