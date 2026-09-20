package ru.mirea.dancestudio.exception;

/** Не удалось сформировать или сохранить файл экспорта. */
public class ExportException extends DanceStudioException {

    public ExportException(String message, Throwable cause) {
        super(message, cause);
    }
}
