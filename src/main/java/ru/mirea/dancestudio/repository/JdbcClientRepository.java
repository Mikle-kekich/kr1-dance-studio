package ru.mirea.dancestudio.repository;

import ru.mirea.dancestudio.exception.EntityNotFoundException;
import ru.mirea.dancestudio.model.Client;
import ru.mirea.dancestudio.util.DatabaseManager;
import ru.mirea.dancestudio.util.SqlUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Хранилище клиентов в таблице clients (JDBC + PreparedStatement). */
public class JdbcClientRepository extends JdbcSupport<Client> implements ClientRepository {

    private static final String COLUMNS = "id, full_name, phone, email, birth_date, registered_at";
    private static final String SQL_INSERT =
            "INSERT INTO clients (full_name, phone, email, birth_date, registered_at) VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_SELECT_ALL = "SELECT " + COLUMNS + " FROM clients ORDER BY id";
    private static final String SQL_SELECT_BY_ID = "SELECT " + COLUMNS + " FROM clients WHERE id = ?";
    private static final String SQL_SELECT_BY_PHONE = "SELECT " + COLUMNS + " FROM clients WHERE phone = ?";
    private static final String SQL_SELECT_BY_EMAIL = "SELECT " + COLUMNS + " FROM clients WHERE email = ?";
    private static final String SQL_SEARCH = "SELECT " + COLUMNS + " FROM clients "
            + "WHERE full_name ILIKE ? OR phone ILIKE ? OR email ILIKE ? ORDER BY id";
    private static final String SQL_UPDATE =
            "UPDATE clients SET full_name = ?, phone = ?, email = ?, birth_date = ? WHERE id = ?";
    private static final String SQL_DELETE = "DELETE FROM clients WHERE id = ?";
    private static final String SQL_COUNT = "SELECT COUNT(*) FROM clients";

    public JdbcClientRepository(DatabaseManager db) {
        super(db);
    }

    @Override
    protected Client mapRow(ResultSet rs) throws SQLException {
        return new Client(
                rs.getLong("id"),
                rs.getString("full_name"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getObject("birth_date", LocalDate.class),
                rs.getObject("registered_at", LocalDateTime.class));
    }

    @Override
    public Client save(Client client) {
        LocalDateTime registeredAt = client.getRegisteredAt() != null
                ? client.getRegisteredAt() : LocalDateTime.now();
        long id = insert(SQL_INSERT, ps -> {
            ps.setString(1, client.getFullName());
            ps.setString(2, client.getPhone());
            ps.setString(3, client.getEmail());
            ps.setObject(4, client.getBirthDate());
            ps.setObject(5, registeredAt);
        });
        client.setId(id);
        client.setRegisteredAt(registeredAt);
        return client;
    }

    @Override
    public Optional<Client> findById(long id) {
        return queryOne(SQL_SELECT_BY_ID, ps -> ps.setLong(1, id));
    }

    @Override
    public List<Client> findAll() {
        return queryList(SQL_SELECT_ALL, NO_PARAMS);
    }

    @Override
    public void update(Client client) {
        int rows = executeUpdate(SQL_UPDATE, ps -> {
            ps.setString(1, client.getFullName());
            ps.setString(2, client.getPhone());
            ps.setString(3, client.getEmail());
            ps.setObject(4, client.getBirthDate());
            ps.setLong(5, client.getId());
        });
        if (rows == 0) {
            throw EntityNotFoundException.client(client.getId());
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

    @Override
    public Optional<Client> findByPhone(String phone) {
        return queryOne(SQL_SELECT_BY_PHONE, ps -> ps.setString(1, phone));
    }

    @Override
    public Optional<Client> findByEmail(String email) {
        return queryOne(SQL_SELECT_BY_EMAIL, ps -> ps.setString(1, email));
    }

    @Override
    public List<Client> search(String term) {
        String pattern = SqlUtil.containsPattern(term);
        return queryList(SQL_SEARCH, ps -> {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
        });
    }
}
