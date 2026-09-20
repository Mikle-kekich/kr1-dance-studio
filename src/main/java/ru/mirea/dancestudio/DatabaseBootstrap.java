package ru.mirea.dancestudio;

import ru.mirea.dancestudio.exception.DanceStudioException;
import ru.mirea.dancestudio.exception.DatabaseException;
import ru.mirea.dancestudio.exception.UserCancelledException;
import ru.mirea.dancestudio.repository.JdbcDatabaseInfoRepository;
import ru.mirea.dancestudio.service.DatabaseInfoService;
import ru.mirea.dancestudio.ui.ConsoleInput;
import ru.mirea.dancestudio.util.DatabaseManager;
import ru.mirea.dancestudio.util.ScriptRunner;

import java.io.PrintStream;

/**
 * Подготовка базы данных при запуске: проверка соединения, создание базы и таблиц при первом запуске.
 * Ошибки подключения обрабатываются диалогом «повторить попытку?», а не аварийным завершением.
 */
class DatabaseBootstrap {

    private static final String SCHEMA_SCRIPT = "/sql/01_schema.sql";
    private static final String DATA_SCRIPT = "/sql/02_test_data.sql";
    private static final String DATABASE_NOT_FOUND = "3D000";

    private final DatabaseManager db;
    private final ConsoleInput in;
    private final PrintStream out;

    DatabaseBootstrap(DatabaseManager db, ConsoleInput in, PrintStream out) {
        this.db = db;
        this.in = in;
        this.out = out;
    }

    /**
     * @param forceInit true, если программа запущена с параметром --init-db
     * @return true, если база данных готова к работе
     */
    boolean prepare(boolean forceInit) {
        out.println("Подключение к базе данных: " + db.getUrl());
        if (!connect()) {
            return false;
        }
        if (forceInit) {
            initialize();
        }
        DatabaseInfoService info = new DatabaseInfoService(new JdbcDatabaseInfoRepository(db));
        if (info.isSchemaReady()) {
            return true;
        }
        out.println("В базе данных не найдены таблицы студии.");
        if (ask("Создать таблицы и загрузить тестовые данные?")) {
            initialize();
        }
        if (info.isSchemaReady()) {
            return true;
        }
        out.println("Без таблиц программа работать не может. Выполните скрипты sql/01_schema.sql и "
                + "sql/02_test_data.sql либо запустите программу с параметром --init-db.");
        return false;
    }

    private boolean connect() {
        while (true) {
            try {
                db.checkConnection();
                out.println("Соединение установлено.");
                return true;
            } catch (DatabaseException e) {
                out.println("Ошибка: " + e.getMessage());
                if (DATABASE_NOT_FOUND.equals(e.getSqlState()) && ask("Создать базу данных автоматически?")) {
                    createDatabase();
                    continue;
                }
                out.println("Проверьте адрес, имя пользователя и пароль в файле database.properties "
                        + "(или в переменных окружения DB_URL, DB_USER, DB_PASSWORD).");
                if (!ask("Повторить попытку подключения?")) {
                    return false;
                }
            }
        }
    }

    private void createDatabase() {
        try {
            if (db.createDatabaseIfMissing()) {
                out.println("База данных создана.");
            }
        } catch (DanceStudioException e) {
            out.println("Ошибка: " + e.getMessage());
        }
    }

    /** Пересоздаёт таблицы и загружает тестовые данные (в одной транзакции). */
    private void initialize() {
        out.println("ВНИМАНИЕ: таблицы clients, dance_classes и enrollments будут пересозданы, "
                + "все их данные удалены.");
        if (!ask("Продолжить?")) {
            out.println("Инициализация отменена.");
            return;
        }
        try {
            ScriptRunner.runClasspathScripts(db, SCHEMA_SCRIPT, DATA_SCRIPT);
            out.println("База данных инициализирована: таблицы созданы, тестовые данные загружены.");
        } catch (DanceStudioException e) {
            out.println("Ошибка: " + e.getMessage());
        }
    }

    private boolean ask(String question) {
        try {
            return in.confirm(question);
        } catch (UserCancelledException e) {
            return false;
        }
    }
}
