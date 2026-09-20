package ru.mirea.dancestudio.util;

import ru.mirea.dancestudio.exception.DatabaseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Единая точка получения JDBC-соединений с PostgreSQL.
 * <p>
 * Настройки берутся из database.properties (в classpath), затем из файла database.properties
 * рядом с программой (или файла, указанного параметром -Ddb.config=...), а переменные окружения
 * DB_URL, DB_USER и DB_PASSWORD имеют наивысший приоритет. Пароль в коде не хранится.
 * <p>
 * Каждый вызов {@link #getConnection()} открывает новое соединение; вызывающий код обязан
 * закрыть его через try-with-resources.
 */
public class DatabaseManager {

    private static final String CONFIG_FILE = "database.properties";
    private static final Pattern POSTGRES_URL = Pattern.compile("^(jdbc:postgresql://[^/]+/)([^?]+)([?].*)?$");
    private static final Pattern SAFE_DB_NAME = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");
    private static final int LOGIN_TIMEOUT_SECONDS = 5;

    private final String url;
    private final String user;
    private final String password;

    public DatabaseManager(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
        DriverManager.setLoginTimeout(LOGIN_TIMEOUT_SECONDS);
    }

    /** Создаёт менеджер по настройкам приложения (файл настроек и переменные окружения). */
    public static DatabaseManager fromConfiguration() {
        Properties props = new Properties();
        loadFromClasspath(props);
        loadFromFile(props);
        String url = firstNonBlank(System.getenv("DB_URL"), props.getProperty("db.url"));
        String user = firstNonBlank(System.getenv("DB_USER"), props.getProperty("db.user"));
        String password = firstNonBlank(System.getenv("DB_PASSWORD"), props.getProperty("db.password"));
        if (url == null) {
            throw new DatabaseException("В настройках не задан адрес базы данных (параметр db.url "
                    + "в файле database.properties или переменная окружения DB_URL).");
        }
        return new DatabaseManager(url, user == null ? "" : user, password == null ? "" : password);
    }

    /** Открывает новое соединение с базой данных. */
    public Connection getConnection() {
        return open(url);
    }

    /** Проверяет, что к базе данных можно подключиться. */
    public void checkConnection() {
        try (Connection ignored = getConnection()) {
            // соединение открылось и сразу закрывается - этого достаточно
        } catch (SQLException e) {
            throw SqlErrors.translate("Не удалось проверить соединение", e);
        }
    }

    /** Адрес базы данных (без имени пользователя и пароля) для сообщений пользователю. */
    public String getUrl() {
        return url;
    }

    /**
     * Создаёт базу данных, указанную в db.url, если её ещё нет. Подключается к служебной базе postgres.
     *
     * @return true, если база была создана, false, если уже существовала
     */
    public boolean createDatabaseIfMissing() {
        Matcher matcher = POSTGRES_URL.matcher(url);
        if (!matcher.matches()) {
            throw new DatabaseException("Не удалось определить имя базы данных из адреса " + url);
        }
        String dbName = matcher.group(2);
        if (!SAFE_DB_NAME.matcher(dbName).matches()) {
            throw new DatabaseException("Имя базы данных «" + dbName + "» нельзя создать автоматически. "
                    + "Создайте её вручную (см. sql/00_create_database.sql).");
        }
        String query = matcher.group(3) == null ? "" : matcher.group(3);
        String adminUrl = matcher.group(1) + "postgres" + query;
        try (Connection connection = open(adminUrl)) {
            try (PreparedStatement exists = connection.prepareStatement(
                    "SELECT 1 FROM pg_database WHERE datname = ?")) {
                exists.setString(1, dbName);
                try (ResultSet rs = exists.executeQuery()) {
                    if (rs.next()) {
                        return false;
                    }
                }
            }
            // Имя базы нельзя передать параметром, поэтому оно предварительно проверено регулярным выражением.
            try (Statement create = connection.createStatement()) {
                create.executeUpdate("CREATE DATABASE " + dbName + " WITH ENCODING 'UTF8' TEMPLATE template0");
            }
            return true;
        } catch (SQLException e) {
            throw SqlErrors.translate("Не удалось создать базу данных «" + dbName + "»", e);
        }
    }

    private Connection open(String jdbcUrl) {
        Properties info = new Properties();
        info.setProperty("user", user);
        info.setProperty("password", password);
        info.setProperty("ApplicationName", "dance-studio-console");
        try {
            return DriverManager.getConnection(jdbcUrl, info);
        } catch (SQLException e) {
            throw SqlErrors.translate("Не удалось подключиться к базе данных", e);
        }
    }

    private static void loadFromClasspath(Properties props) {
        try (InputStream in = DatabaseManager.class.getResourceAsStream("/" + CONFIG_FILE)) {
            if (in != null) {
                props.load(new java.io.InputStreamReader(in, StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            throw new DatabaseException("Не удалось прочитать файл настроек " + CONFIG_FILE, null, e);
        }
    }

    private static void loadFromFile(Properties props) {
        String custom = System.getProperty("db.config");
        Path path = Path.of(custom != null ? custom : CONFIG_FILE);
        if (!Files.isRegularFile(path)) {
            if (custom != null) {
                throw new DatabaseException("Файл настроек " + path.toAbsolutePath() + " не найден.");
            }
            return;
        }
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            props.load(reader);
        } catch (IOException e) {
            throw new DatabaseException("Не удалось прочитать файл настроек " + path.toAbsolutePath(), null, e);
        }
    }

    private static String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first.trim();
        }
        if (second != null && !second.isBlank()) {
            return second.trim();
        }
        return null;
    }
}
