package ru.mirea.dancestudio.util;

import ru.mirea.dancestudio.exception.BusinessException;
import ru.mirea.dancestudio.exception.DanceStudioException;
import ru.mirea.dancestudio.exception.DatabaseException;

import java.sql.SQLException;

/** Преобразует технические SQLException в понятные пользователю исключения приложения. */
public final class SqlErrors {

    private SqlErrors() {
    }

    /**
     * @param context что делала программа, когда произошла ошибка (начало сообщения)
     * @param e       исходное исключение JDBC
     */
    public static DanceStudioException translate(String context, SQLException e) {
        String state = e.getSQLState();
        if (state == null) {
            return new DatabaseException(context + ": " + firstLine(e.getMessage()), null, e);
        }
        if (state.startsWith("08")) {
            return new DatabaseException(context + ": нет соединения с сервером PostgreSQL. "
                    + "Проверьте, что сервер запущен, а адрес и порт указаны верно.", state, e);
        }
        return switch (state) {
            case "28P01", "28000" -> new DatabaseException(
                    context + ": неверное имя пользователя или пароль PostgreSQL.", state, e);
            case "3D000" -> new DatabaseException(
                    context + ": база данных не найдена.", state, e);
            case "42P01" -> new DatabaseException(
                    context + ": таблицы не найдены. Создайте их скриптами из каталога sql/ "
                            + "или запустите программу с параметром --init-db.", state, e);
            case "23505" -> new BusinessException(
                    "Нарушено ограничение уникальности: такая запись уже существует.");
            case "23503" -> new BusinessException(
                    "Нарушена связь между таблицами: есть связанные записи или указана несуществующая запись.");
            case "23502", "23514" -> new BusinessException(
                    "Данные не удовлетворяют ограничениям базы данных.");
            default -> new DatabaseException(
                    context + ": ошибка SQL (" + state + "): " + firstLine(e.getMessage()), state, e);
        };
    }

    private static String firstLine(String message) {
        if (message == null || message.isBlank()) {
            return "неизвестная ошибка";
        }
        int newLine = message.indexOf('\n');
        return (newLine >= 0 ? message.substring(0, newLine) : message).trim();
    }
}
