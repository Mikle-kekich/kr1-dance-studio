package ru.mirea.dancestudio.service;

import ru.mirea.dancestudio.exception.BusinessException;
import ru.mirea.dancestudio.exception.EntityNotFoundException;
import ru.mirea.dancestudio.model.Client;
import ru.mirea.dancestudio.model.DanceClass;
import ru.mirea.dancestudio.model.Enrollment;
import ru.mirea.dancestudio.model.EnrollmentStatus;
import ru.mirea.dancestudio.repository.ClientRepository;
import ru.mirea.dancestudio.repository.DanceClassRepository;
import ru.mirea.dancestudio.repository.EnrollmentRepository;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Создание, изменение, смена статуса и удаление записей на занятия.
 * Сама проверка правил вынесена в {@link EnrollmentRules}; сервис только координирует шаги.
 */
public class EnrollmentService {

    private static final int MAX_NOTE_LENGTH = 500;

    private final EnrollmentRepository enrollments;
    private final ClientRepository clients;
    private final DanceClassRepository classes;
    private final EnrollmentRules rules;
    private final Clock clock;

    public EnrollmentService(EnrollmentRepository enrollments, ClientRepository clients,
                             DanceClassRepository classes, Clock clock) {
        this.enrollments = enrollments;
        this.clients = clients;
        this.classes = classes;
        this.rules = new EnrollmentRules(enrollments);
        this.clock = clock;
    }

    public List<Enrollment> findAll() {
        return enrollments.findAll();
    }

    public Enrollment getById(long id) {
        return enrollments.findById(id).orElseThrow(() -> EntityNotFoundException.enrollment(id));
    }

    /**
     * Создаёт запись со статусом CREATED.
     * БП-3: клиент и занятие существуют; БП-4: занятие ещё не началось; БП-5: возраст подходит;
     * БП-6: нет дубля записи; БП-7: есть свободные места; БП-8: стоимость и комментарий корректны.
     *
     * @param price стоимость; null означает «по прайсу занятия»
     */
    public Enrollment create(long clientId, long classId, BigDecimal price, boolean paid, String note) {
        Client client = clients.findById(clientId).orElseThrow(() -> EntityNotFoundException.client(clientId));
        DanceClass danceClass = classes.findById(classId)
                .orElseThrow(() -> EntityNotFoundException.danceClass(classId));
        BigDecimal finalPrice = Validation.requireMoney(price != null ? price : danceClass.getPrice(), "Стоимость");
        String finalNote = Validation.optionalText(note, "Комментарий", MAX_NOTE_LENGTH);

        LocalDateTime now = LocalDateTime.now(clock);
        rules.classNotStarted(danceClass, now, "Нельзя записаться на занятие, которое уже началось или прошло.");
        rules.ageAllowed(client, danceClass, now.toLocalDate());
        rules.noActiveDuplicate(clientId, classId, null);
        rules.seatAvailable(danceClass);

        Enrollment enrollment = new Enrollment(client, danceClass, finalPrice, paid, finalNote);
        enrollment.setCreatedAt(now);
        return enrollments.save(enrollment);
    }

    /**
     * Изменяет занятие, стоимость, оплату и комментарий (статус меняется отдельно).
     * БП-9: завершённую и отменённую запись изменять нельзя; при переносе на другое занятие
     * заново проверяются БП-4...БП-7; БП-10: у подтверждённой записи нельзя снять отметку об оплате.
     */
    public Enrollment update(long id, long classId, BigDecimal price, boolean paid, String note) {
        Enrollment enrollment = getById(id);
        EnrollmentStatus status = enrollment.getStatus();
        if (status.isFinal()) {
            throw new BusinessException("Запись со статусом «" + status.getLabel() + "» изменять нельзя.");
        }
        BigDecimal finalPrice = Validation.requireMoney(price, "Стоимость");
        String finalNote = Validation.optionalText(note, "Комментарий", MAX_NOTE_LENGTH);
        if (status == EnrollmentStatus.CONFIRMED && !paid) {
            throw new BusinessException("Нельзя снять отметку об оплате у подтверждённой записи: "
                    + "сначала отмените запись.");
        }
        DanceClass target = enrollment.getDanceClass();
        if (target.getId() != classId) {
            target = classes.findById(classId).orElseThrow(() -> EntityNotFoundException.danceClass(classId));
            LocalDateTime now = LocalDateTime.now(clock);
            rules.classNotStarted(target, now, "Нельзя перенести запись на занятие, которое уже началось.");
            rules.ageAllowed(enrollment.getClient(), target, now.toLocalDate());
            rules.noActiveDuplicate(enrollment.getClient().getId(), classId, id);
            rules.seatAvailable(target);
        }
        enrollment.setDanceClass(target);
        enrollment.setPrice(finalPrice);
        enrollment.setPaid(paid);
        enrollment.setNote(finalNote);
        enrollments.update(enrollment);
        return enrollment;
    }

    /** Меняет статус записи по правилам БП-11...БП-13 (см. {@link EnrollmentRules#statusChange}). */
    public Enrollment changeStatus(long id, EnrollmentStatus target) {
        Enrollment enrollment = getById(id);
        rules.statusChange(enrollment, target, LocalDateTime.now(clock));
        enrollment.setStatus(target);
        enrollments.update(enrollment);
        return enrollment;
    }

    /** Удаляет запись по правилу БП-14. */
    public void delete(long id) {
        rules.deletable(getById(id));
        enrollments.deleteById(id);
    }
}
