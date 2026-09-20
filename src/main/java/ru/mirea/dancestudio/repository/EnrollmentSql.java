package ru.mirea.dancestudio.repository;

/**
 * SQL-запросы к таблице enrollments (записи на занятия).
 * Все значения передаются параметрами (?), конкатенации пользовательского ввода нет.
 * Условие status <> 'CANCELLED' совпадает с частичным уникальным индексом в схеме БД:
 * отменённая запись не занимает место и не мешает записаться повторно.
 */
final class EnrollmentSql {

    private EnrollmentSql() {
    }

    /** Запись вместе с клиентом и занятием (JOIN), чтобы не делать по запросу на каждую строку. */
    static final String BASE_SELECT = """
            SELECT e.id AS e_id, e.status, e.price AS e_price, e.paid, e.note, e.created_at,
                   c.id AS c_id, c.full_name, c.phone, c.email, c.birth_date, c.registered_at,
                   d.id AS d_id, d.title, d.style, d.level, d.instructor, d.start_time,
                   d.duration_minutes, d.capacity, d.min_age, d.price AS d_price
            FROM enrollments e
            JOIN clients c ON c.id = e.client_id
            JOIN dance_classes d ON d.id = e.class_id
            """;

    static final String SELECT_ALL = BASE_SELECT + "ORDER BY e.id";
    static final String SELECT_BY_ID = BASE_SELECT + "WHERE e.id = ?";
    static final String SELECT_BY_CLIENT = BASE_SELECT + "WHERE e.client_id = ? ORDER BY e.id";
    static final String SEARCH_BY_CLIENT_NAME = BASE_SELECT + "WHERE c.full_name ILIKE ? ORDER BY e.id";
    static final String SEARCH_BY_CLASS =
            BASE_SELECT + "WHERE d.title ILIKE ? OR d.instructor ILIKE ? ORDER BY e.id";
    static final String SEARCH_BY_NOTE = BASE_SELECT + "WHERE e.note ILIKE ? ORDER BY e.id";
    static final String SELECT_BY_CLASS_DATE =
            BASE_SELECT + "WHERE d.start_time >= ? AND d.start_time < ? ORDER BY d.start_time, e.id";

    static final String INSERT = "INSERT INTO enrollments "
            + "(client_id, class_id, status, price, paid, note, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
    static final String UPDATE = "UPDATE enrollments "
            + "SET client_id = ?, class_id = ?, status = ?, price = ?, paid = ?, note = ? WHERE id = ?";
    static final String DELETE = "DELETE FROM enrollments WHERE id = ?";

    static final String COUNT = "SELECT COUNT(*) FROM enrollments";
    static final String COUNT_BY_CLIENT = "SELECT COUNT(*) FROM enrollments WHERE client_id = ?";
    static final String COUNT_BY_CLASS = "SELECT COUNT(*) FROM enrollments WHERE class_id = ?";
    static final String COUNT_SEATS_TAKEN =
            "SELECT COUNT(*) FROM enrollments WHERE class_id = ? AND status <> 'CANCELLED'";
    static final String COUNT_ACTIVE_DUPLICATES = "SELECT COUNT(*) FROM enrollments "
            + "WHERE client_id = ? AND class_id = ? AND status <> 'CANCELLED' AND id <> ?";

    static final String COUNT_BY_STATUS = "SELECT status, COUNT(*) AS cnt FROM enrollments GROUP BY status";
    static final String SEATS_BY_CLASS = "SELECT class_id, COUNT(*) AS cnt FROM enrollments "
            + "WHERE status <> 'CANCELLED' GROUP BY class_id";
    static final String SUM_REVENUE =
            "SELECT COALESCE(SUM(price), 0) FROM enrollments WHERE paid = TRUE AND status <> 'CANCELLED'";
    static final String AVG_PRICE = "SELECT COALESCE(AVG(price), 0) FROM enrollments";
}
