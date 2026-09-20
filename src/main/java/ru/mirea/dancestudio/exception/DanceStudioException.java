package ru.mirea.dancestudio.exception;

/**
 * Базовое исключение приложения. Сообщение всегда написано для пользователя
 * (без технических подробностей), поэтому его можно выводить в консоль как есть.
 */
public class DanceStudioException extends RuntimeException {

    public DanceStudioException(String message) {
        super(message);
    }

    public DanceStudioException(String message, Throwable cause) {
        super(message, cause);
    }
}
