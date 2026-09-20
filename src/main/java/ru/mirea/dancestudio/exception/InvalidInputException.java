package ru.mirea.dancestudio.exception;

/** Пользователь ввёл данные в неверном формате (например, текст вместо числа). */
public class InvalidInputException extends DanceStudioException {

    public InvalidInputException(String message) {
        super(message);
    }
}
