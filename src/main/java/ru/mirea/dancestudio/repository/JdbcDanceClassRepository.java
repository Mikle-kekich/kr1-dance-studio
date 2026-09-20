package ru.mirea.dancestudio.repository;

import ru.mirea.dancestudio.exception.EntityNotFoundException;
import ru.mirea.dancestudio.model.DanceClass;
import ru.mirea.dancestudio.model.DanceLevel;
import ru.mirea.dancestudio.model.DanceStyle;
import ru.mirea.dancestudio.util.DatabaseManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Хранилище занятий в таблице dance_classes. */
public class JdbcDanceClassRepository extends JdbcSupport<DanceClass> implements DanceClassRepository {

    private static final String COLUMNS =
            "id, title, style, level, instructor, start_time, duration_minutes, capacity, min_age, price";
    private static final String SQL_INSERT = "INSERT INTO dance_classes "
            + "(title, style, level, instructor, start_time, duration_minutes, capacity, min_age, price) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_SELECT_ALL =
            "SELECT " + COLUMNS + " FROM dance_classes ORDER BY start_time, id";
    private static final String SQL_SELECT_BY_ID = "SELECT " + COLUMNS + " FROM dance_classes WHERE id = ?";
    private static final String SQL_UPDATE = "UPDATE dance_classes SET title = ?, style = ?, level = ?, "
            + "instructor = ?, start_time = ?, duration_minutes = ?, capacity = ?, min_age = ?, price = ? "
            + "WHERE id = ?";
    private static final String SQL_DELETE = "DELETE FROM dance_classes WHERE id = ?";
    private static final String SQL_COUNT = "SELECT COUNT(*) FROM dance_classes";

    public JdbcDanceClassRepository(DatabaseManager db) {
        super(db);
    }

    @Override
    protected DanceClass mapRow(ResultSet rs) throws SQLException {
        return new DanceClass(
                rs.getLong("id"),
                rs.getString("title"),
                DanceStyle.valueOf(rs.getString("style")),
                DanceLevel.valueOf(rs.getString("level")),
                rs.getString("instructor"),
                rs.getObject("start_time", LocalDateTime.class),
                rs.getInt("duration_minutes"),
                rs.getInt("capacity"),
                rs.getInt("min_age"),
                rs.getBigDecimal("price"));
    }

    @Override
    public DanceClass save(DanceClass danceClass) {
        long id = insert(SQL_INSERT, ps -> bindFields(ps, danceClass));
        danceClass.setId(id);
        return danceClass;
    }

    @Override
    public Optional<DanceClass> findById(long id) {
        return queryOne(SQL_SELECT_BY_ID, ps -> ps.setLong(1, id));
    }

    @Override
    public List<DanceClass> findAll() {
        return queryList(SQL_SELECT_ALL, NO_PARAMS);
    }

    @Override
    public void update(DanceClass danceClass) {
        int rows = executeUpdate(SQL_UPDATE, ps -> {
            bindFields(ps, danceClass);
            ps.setLong(10, danceClass.getId());
        });
        if (rows == 0) {
            throw EntityNotFoundException.danceClass(danceClass.getId());
        }
    }

    @Override
    public boolean deleteById(long id) {
        return executeUpdate(SQL_DELETE, ps -> ps.setLong(1, id)) > 0;
    }

    @Override
    public long count() {
        return queryLong(SQL_COUNT, NO_PARAMS);
    }

    /** Параметры 1-9 одинаковы для INSERT и UPDATE. В базе enum хранится по имени константы. */
    private static void bindFields(PreparedStatement ps, DanceClass c) throws SQLException {
        ps.setString(1, c.getTitle());
        ps.setString(2, c.getStyle().name());
        ps.setString(3, c.getLevel().name());
        ps.setString(4, c.getInstructor());
        ps.setObject(5, c.getStartTime());
        ps.setInt(6, c.getDurationMinutes());
        ps.setInt(7, c.getCapacity());
        ps.setInt(8, c.getMinAge());
        ps.setBigDecimal(9, c.getPrice());
    }
}
