package ru.mirea.dancestudio.repository;

import ru.mirea.dancestudio.exception.DatabaseException;
import ru.mirea.dancestudio.util.DatabaseManager;
import ru.mirea.dancestudio.util.SqlErrors;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Базовый класс JDBC-репозиториев (шаблонный метод): содержит всю рутину JDBC -
 * получение соединения, PreparedStatement, ResultSet, try-with-resources и обработку SQLException.
 * Наследники передают только SQL-текст с параметрами (?) и способ заполнения параметров.
 *
 * @param <T> тип сущности, в которую преобразуется строка ResultSet
 */
abstract class JdbcSupport<T> {

    private static final String CONTEXT = "Ошибка выполнения запроса к базе данных";

    /** Пустой набор параметров для запросов без знаков вопроса. */
    protected static final Binder NO_PARAMS = statement -> { };

    protected final DatabaseManager db;

    protected JdbcSupport(DatabaseManager db) {
        this.db = db;
    }

    /** Заполняет параметры PreparedStatement. */
    @FunctionalInterface
    protected interface Binder {
        void bind(PreparedStatement statement) throws SQLException;
    }

    /** Читает результат запроса целиком. */
    @FunctionalInterface
    protected interface ResultReader<R> {
        R read(ResultSet resultSet) throws SQLException;
    }

    /** Преобразует текущую строку ResultSet в сущность. */
    protected abstract T mapRow(ResultSet resultSet) throws SQLException;

    protected <R> R query(String sql, Binder binder, ResultReader<R> reader) {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            binder.bind(statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                return reader.read(resultSet);
            }
        } catch (SQLException e) {
            throw SqlErrors.translate(CONTEXT, e);
        }
    }

    protected List<T> queryList(String sql, Binder binder) {
        return query(sql, binder, resultSet -> {
            List<T> result = new ArrayList<>();
            while (resultSet.next()) {
                result.add(mapRow(resultSet));
            }
            return result;
        });
    }

    protected Optional<T> queryOne(String sql, Binder binder) {
        return query(sql, binder, resultSet ->
                resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty());
    }

    protected long queryLong(String sql, Binder binder) {
        return query(sql, binder, resultSet -> resultSet.next() ? resultSet.getLong(1) : 0L);
    }

    protected BigDecimal queryDecimal(String sql, Binder binder) {
        return query(sql, binder, resultSet -> {
            if (!resultSet.next()) {
                return BigDecimal.ZERO;
            }
            BigDecimal value = resultSet.getBigDecimal(1);
            return value == null ? BigDecimal.ZERO : value;
        });
    }

    /** Выполняет UPDATE или DELETE и возвращает число изменённых строк. */
    protected int executeUpdate(String sql, Binder binder) {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            binder.bind(statement);
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw SqlErrors.translate(CONTEXT, e);
        }
    }

    /** Выполняет INSERT и возвращает ID, сгенерированный базой данных. */
    protected long insert(String sql, Binder binder) {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, new String[] {"id"})) {
            binder.bind(statement);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
            throw new DatabaseException("База данных не вернула ID созданной записи.");
        } catch (SQLException e) {
            throw SqlErrors.translate(CONTEXT, e);
        }
    }
}
