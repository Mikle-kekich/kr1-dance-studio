package ru.mirea.dancestudio.exception;

/** Ошибка подключения к базе данных или выполнения SQL-запроса. */
public class DatabaseException extends DanceStudioException {

    private final String sqlState;

    public DatabaseException(String message) {
        this(message, null, null);
    }

    public DatabaseException(String message, String sqlState, Throwable cause) {
        super(message, cause);
        this.sqlState = sqlState;
    }

    /** Код SQLSTATE, если ошибку вызвал SQLException, иначе null. */
    public String getSqlState() {
        return sqlState;
    }
}
