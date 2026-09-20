package ru.mirea.dancestudio.repository;

import ru.mirea.dancestudio.exception.BusinessException;
import ru.mirea.dancestudio.model.TableData;
import ru.mirea.dancestudio.util.DatabaseManager;
import ru.mirea.dancestudio.util.SqlErrors;
import ru.mirea.dancestudio.util.SqlUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Читает метаданные и «сырое» содержимое таблиц через DatabaseMetaData и ResultSetMetaData.
 * Имя таблицы в запрос подставляется только после проверки по списку реальных таблиц базы,
 * поскольку идентификаторы нельзя передать параметром PreparedStatement.
 */
public class JdbcDatabaseInfoRepository implements DatabaseInfoRepository {

    private static final String CONTEXT = "Не удалось прочитать таблицы базы данных";

    private final DatabaseManager db;

    public JdbcDatabaseInfoRepository(DatabaseManager db) {
        this.db = db;
    }

    @Override
    public List<String> listTables() {
        try (Connection connection = db.getConnection();
             ResultSet tables = connection.getMetaData()
                     .getTables(null, connection.getSchema(), "%", new String[] {"TABLE"})) {
            List<String> names = new ArrayList<>();
            while (tables.next()) {
                names.add(tables.getString("TABLE_NAME"));
            }
            Collections.sort(names);
            return names;
        } catch (SQLException e) {
            throw SqlErrors.translate(CONTEXT, e);
        }
    }

    @Override
    public boolean tableExists(String tableName) {
        return listTables().contains(tableName);
    }

    @Override
    public TableData readTable(String tableName, int limit) {
        if (!tableExists(tableName)) {
            throw new BusinessException("Таблица «" + tableName + "» не найдена в базе данных.");
        }
        String quoted = SqlUtil.quoteIdentifier(tableName);
        try (Connection connection = db.getConnection()) {
            long total = 0;
            try (PreparedStatement count = connection.prepareStatement("SELECT COUNT(*) FROM " + quoted);
                 ResultSet rs = count.executeQuery()) {
                if (rs.next()) {
                    total = rs.getLong(1);
                }
            }
            try (PreparedStatement select = connection.prepareStatement(
                    "SELECT * FROM " + quoted + " ORDER BY 1 LIMIT ?")) {
                select.setInt(1, limit);
                try (ResultSet rs = select.executeQuery()) {
                    return readRows(tableName, rs, total);
                }
            }
        } catch (SQLException e) {
            throw SqlErrors.translate(CONTEXT, e);
        }
    }

    private static TableData readRows(String tableName, ResultSet rs, long total) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int columnCount = meta.getColumnCount();
        List<String> columns = new ArrayList<>(columnCount);
        for (int i = 1; i <= columnCount; i++) {
            columns.add(meta.getColumnLabel(i));
        }
        List<List<String>> rows = new ArrayList<>();
        while (rs.next()) {
            List<String> row = new ArrayList<>(columnCount);
            for (int i = 1; i <= columnCount; i++) {
                Object value = rs.getObject(i);
                row.add(value == null ? "NULL" : value.toString());
            }
            rows.add(row);
        }
        return new TableData(tableName, columns, rows, total);
    }
}
