package ru.mirea.dancestudio.exception;

/**
 * Пользователь отменил текущую операцию (ввёл слово «отмена»).
 * Это не ошибка, поэтому исключение не наследуется от {@link DanceStudioException}.
 */
public class UserCancelledException extends RuntimeException {

    public UserCancelledException() {
        super("Операция отменена пользователем");
    }
}
