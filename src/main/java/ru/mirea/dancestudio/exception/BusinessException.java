package ru.mirea.dancestudio.exception;

/** Нарушено бизнес-правило или переданы некорректные данные. */
public class BusinessException extends DanceStudioException {

    public BusinessException(String message) {
        super(message);
    }
}
