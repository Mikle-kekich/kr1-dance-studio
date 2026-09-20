package ru.mirea.dancestudio.exception;

/** Поток ввода закрыт (Ctrl+D / Ctrl+Z или конец файла): продолжать работу нельзя. */
public class InputClosedException extends RuntimeException {

    public InputClosedException() {
        super("Поток ввода закрыт");
    }
}
