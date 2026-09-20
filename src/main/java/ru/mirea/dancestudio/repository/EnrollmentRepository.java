package ru.mirea.dancestudio.repository;

import ru.mirea.dancestudio.model.Enrollment;
import ru.mirea.dancestudio.model.EnrollmentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface EnrollmentRepository extends CrudRepository<Enrollment> {

    List<Enrollment> findByClientId(long clientId);

    // ---- поиск (SQL с параметром ILIKE) ----

    List<Enrollment> searchByClientName(String term);

    List<Enrollment> searchByClassTitleOrInstructor(String term);

    List<Enrollment> searchByNote(String term);

    /** Записи на занятия, которые проходят в указанный день. */
    List<Enrollment> findByClassDate(LocalDate date);

    // ---- проверки для бизнес-правил ----

    long countByClientId(long clientId);

    long countByClassId(long classId);

    /** Сколько мест на занятии занято (записи, кроме отменённых). */
    long countSeatsTaken(long classId);

    /** Есть ли у клиента действующая (не отменённая) запись на занятие; excludeId - запись, которую не учитывать. */
    boolean existsActive(long clientId, long classId, Long excludeId);

    // ---- агрегаты для статистики (SQL: COUNT, SUM, AVG, GROUP BY) ----

    Map<EnrollmentStatus, Long> countByStatus();

    /** Занятые места по каждому занятию: ID занятия - число записей, кроме отменённых. */
    Map<Long, Long> countSeatsTakenByClass();

    /** Сумма стоимости оплаченных записей, кроме отменённых. */
    BigDecimal sumRevenue();

    BigDecimal averagePrice();
}
