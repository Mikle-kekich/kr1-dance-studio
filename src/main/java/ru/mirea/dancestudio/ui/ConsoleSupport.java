package ru.mirea.dancestudio.ui;

import java.io.Console;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Настройка кодировки консоли, чтобы русские буквы выводились и вводились правильно.
 * <ul>
 *   <li>-Dapp.encoding=UTF-8 - явное указание кодировки;</li>
 *   <li>обычное окно cmd/PowerShell - кодировка консоли (например, IBM866);</li>
 *   <li>IDE, Maven, перенаправление ввода-вывода - UTF-8.</li>
 * </ul>
 */
public final class ConsoleSupport {

    private ConsoleSupport() {
    }

    public static Charset detectCharset() {
        String override = System.getProperty("app.encoding");
        if (override != null && !override.isBlank()) {
            try {
                return Charset.forName(override.trim());
            } catch (RuntimeException e) {
                // неизвестная кодировка - используем автоопределение
            }
        }
        Console console = System.console();
        if (console != null) {
            try {
                return console.charset();
            } catch (RuntimeException | LinkageError e) {
                // консоль не сообщила кодировку - используем UTF-8
            }
        }
        return StandardCharsets.UTF_8;
    }

    /** Поток вывода в выбранной кодировке; автосброс буфера включён. */
    public static PrintStream createOut(Charset charset) {
        return new PrintStream(new FileOutputStream(FileDescriptor.out), true, charset);
    }
}
