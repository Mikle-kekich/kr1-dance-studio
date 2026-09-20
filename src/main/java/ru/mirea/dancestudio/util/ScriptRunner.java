package ru.mirea.dancestudio.util;

import ru.mirea.dancestudio.exception.DatabaseException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Выполняет SQL-скрипты, упакованные в ресурсы приложения (каталог sql/). */
public final class ScriptRunner {

    private ScriptRunner() {
    }

    /** Выполняет скрипты по порядку в одной транзакции: при ошибке база остаётся без изменений. */
    public static void runClasspathScripts(DatabaseManager db, String... resources) {
        List<String> scripts = new ArrayList<>();
        for (String resource : resources) {
            scripts.add(readResource(resource));
        }
        try (Connection connection = db.getConnection()) {
            connection.setAutoCommit(false);
            try (Statement statement = connection.createStatement()) {
                statement.setEscapeProcessing(false);
                for (String script : scripts) {
                    statement.execute(script);
                }
                connection.commit();
            } catch (SQLException e) {
                rollbackQuietly(connection);
                throw SqlErrors.translate("Не удалось выполнить SQL-скрипт", e);
            }
        } catch (SQLException e) {
            throw SqlErrors.translate("Ошибка при работе с базой данных", e);
        }
    }

    private static String readResource(String path) {
        try (InputStream in = ScriptRunner.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new DatabaseException("SQL-скрипт " + path + " не найден в приложении. "
                        + "Выполните скрипты из каталога sql/ вручную.");
            }
            String text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            return text.startsWith("\uFEFF") ? text.substring(1) : text;
        } catch (IOException e) {
            throw new DatabaseException("Не удалось прочитать SQL-скрипт " + path, null, e);
        }
    }

    private static void rollbackQuietly(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
            // соединение уже потеряно: откатывать нечего
        }
    }
}
