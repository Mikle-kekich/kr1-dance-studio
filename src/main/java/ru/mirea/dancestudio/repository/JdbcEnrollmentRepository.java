package ru.mirea.dancestudio.repository;

import ru.mirea.dancestudio.exception.EntityNotFoundException;
import ru.mirea.dancestudio.model.Client;
import ru.mirea.dancestudio.model.DanceClass;
import ru.mirea.dancestudio.model.DanceLevel;
import ru.mirea.dancestudio.model.DanceStyle;
import ru.mirea.dancestudio.model.Enrollment;
import ru.mirea.dancestudio.model.EnrollmentStatus;
import ru.mirea.dancestudio.util.DatabaseManager;
import ru.mirea.dancestudio.util.SqlUtil;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Хранилище записей на занятия (таблица enrollments, JOIN с clients и dance_classes). */
public class JdbcEnrollmentRepository extends JdbcSupport<Enrollment> implements EnrollmentRepository {

    public JdbcEnrollmentRepository(DatabaseManager db) {
        super(db);
    }

    @Override
    protected Enrollment mapRow(ResultSet rs) throws SQLException {
        Client client = new Client(
                rs.getLong("c_id"), rs.getString("full_name"), rs.getString("phone"), rs.getString("email"),
                rs.getObject("birth_date", LocalDate.class), rs.getObject("registered_at", LocalDateTime.class));
        DanceClass danceClass = new DanceClass(
                rs.getLong("d_id"), rs.getString("title"),
                DanceStyle.valueOf(rs.getString("style")), DanceLevel.valueOf(rs.getString("level")),
                rs.getString("instructor"), rs.getObject("start_time", LocalDateTime.class),
                rs.getInt("duration_minutes"), rs.getInt("capacity"), rs.getInt("min_age"),
                rs.getBigDecimal("d_price"));
        return new Enrollment(rs.getLong("e_id"), client, danceClass,
                EnrollmentStatus.valueOf(rs.getString("status")), rs.getBigDecimal("e_price"),
                rs.getBoolean("paid"), rs.getString("note"), rs.getObject("created_at", LocalDateTime.class));
    }

    @Override
    public Enrollment save(Enrollment enrollment) {
        LocalDateTime createdAt = enrollment.getCreatedAt() != null
                ? enrollment.getCreatedAt() : LocalDateTime.now();
        long id = insert(EnrollmentSql.INSERT, ps -> {
            ps.setLong(1, enrollment.getClient().getId());
            ps.setLong(2, enrollment.getDanceClass().getId());
            ps.setString(3, enrollment.getStatus().name());
            ps.setBigDecimal(4, enrollment.getPrice());
            ps.setBoolean(5, enrollment.isPaid());
            ps.setString(6, enrollment.getNote());
            ps.setObject(7, createdAt);
        });
        enrollment.setId(id);
        enrollment.setCreatedAt(createdAt);
        return enrollment;
    }

    @Override
    public Optional<Enrollment> findById(long id) {
        return queryOne(EnrollmentSql.SELECT_BY_ID, ps -> ps.setLong(1, id));
    }

    @Override
    public List<Enrollment> findAll() {
        return queryList(EnrollmentSql.SELECT_ALL, NO_PARAMS);
    }

    @Override
    public void update(Enrollment enrollment) {
        int rows = executeUpdate(EnrollmentSql.UPDATE, ps -> {
            ps.setLong(1, enrollment.getClient().getId());
            ps.setLong(2, enrollment.getDanceClass().getId());
            ps.setString(3, enrollment.getStatus().name());
            ps.setBigDecimal(4, enrollment.getPrice());
            ps.setBoolean(5, enrollment.isPaid());
            ps.setString(6, enrollment.getNote());
            ps.setLong(7, enrollment.getId());
        });
        if (rows == 0) {
            throw EntityNotFoundException.enrollment(enrollment.getId());
        }
    }

    @Override
    public boolean deleteById(long id) {
        return executeUpdate(EnrollmentSql.DELETE, ps -> ps.setLong(1, id)) > 0;
    }

    @Override
    public long count() {
        return queryLong(EnrollmentSql.COUNT, NO_PARAMS);
    }

    @Override
    public List<Enrollment> findByClientId(long clientId) {
        return queryList(EnrollmentSql.SELECT_BY_CLIENT, ps -> ps.setLong(1, clientId));
    }

    @Override
    public List<Enrollment> searchByClientName(String term) {
        String pattern = SqlUtil.containsPattern(term);
        return queryList(EnrollmentSql.SEARCH_BY_CLIENT_NAME, ps -> ps.setString(1, pattern));
    }

    @Override
    public List<Enrollment> searchByClassTitleOrInstructor(String term) {
        String pattern = SqlUtil.containsPattern(term);
        return queryList(EnrollmentSql.SEARCH_BY_CLASS, ps -> {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
        });
    }

    @Override
    public List<Enrollment> searchByNote(String term) {
        String pattern = SqlUtil.containsPattern(term);
        return queryList(EnrollmentSql.SEARCH_BY_NOTE, ps -> ps.setString(1, pattern));
    }

    @Override
    public List<Enrollment> findByClassDate(LocalDate date) {
        return queryList(EnrollmentSql.SELECT_BY_CLASS_DATE, ps -> {
            ps.setObject(1, date.atStartOfDay());
            ps.setObject(2, date.plusDays(1).atStartOfDay());
        });
    }

    @Override
    public long countByClientId(long clientId) {
        return queryLong(EnrollmentSql.COUNT_BY_CLIENT, ps -> ps.setLong(1, clientId));
    }

    @Override
    public long countByClassId(long classId) {
        return queryLong(EnrollmentSql.COUNT_BY_CLASS, ps -> ps.setLong(1, classId));
    }

    @Override
    public long countSeatsTaken(long classId) {
        return queryLong(EnrollmentSql.COUNT_SEATS_TAKEN, ps -> ps.setLong(1, classId));
    }

    @Override
    public boolean existsActive(long clientId, long classId, Long excludeId) {
        return queryLong(EnrollmentSql.COUNT_ACTIVE_DUPLICATES, ps -> {
            ps.setLong(1, clientId);
            ps.setLong(2, classId);
            ps.setLong(3, excludeId == null ? -1L : excludeId);
        }) > 0;
    }

    @Override
    public Map<EnrollmentStatus, Long> countByStatus() {
        return query(EnrollmentSql.COUNT_BY_STATUS, NO_PARAMS, rs -> {
            Map<EnrollmentStatus, Long> result = new EnumMap<>(EnrollmentStatus.class);
            for (EnrollmentStatus status : EnrollmentStatus.values()) {
                result.put(status, 0L);
            }
            while (rs.next()) {
                result.put(EnrollmentStatus.valueOf(rs.getString("status")), rs.getLong("cnt"));
            }
            return result;
        });
    }

    @Override
    public Map<Long, Long> countSeatsTakenByClass() {
        return query(EnrollmentSql.SEATS_BY_CLASS, NO_PARAMS, rs -> {
            Map<Long, Long> result = new HashMap<>();
            while (rs.next()) {
                result.put(rs.getLong("class_id"), rs.getLong("cnt"));
            }
            return result;
        });
    }

    @Override
    public BigDecimal sumRevenue() {
        return queryDecimal(EnrollmentSql.SUM_REVENUE, NO_PARAMS);
    }

    @Override
    public BigDecimal averagePrice() {
        return queryDecimal(EnrollmentSql.AVG_PRICE, NO_PARAMS);
    }
}
