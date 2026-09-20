package ru.mirea.dancestudio.exception;

/** Запись с указанным ID отсутствует в базе данных. */
public class EntityNotFoundException extends DanceStudioException {

    public EntityNotFoundException(String message) {
        super(message);
    }

    public static EntityNotFoundException client(long id) {
        return new EntityNotFoundException("Клиент с ID " + id + " не найден.");
    }

    public static EntityNotFoundException danceClass(long id) {
        return new EntityNotFoundException("Занятие с ID " + id + " не найдено.");
    }

    public static EntityNotFoundException enrollment(long id) {
        return new EntityNotFoundException("Запись на занятие с ID " + id + " не найдена.");
    }
}
