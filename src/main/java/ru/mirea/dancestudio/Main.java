package ru.mirea.dancestudio;

import ru.mirea.dancestudio.ui.ConsoleInput;
import ru.mirea.dancestudio.ui.ConsoleSupport;

import java.io.PrintStream;
import java.nio.charset.Charset;

/**
 * Точка входа. Здесь только настройка консоли и запуск приложения:
 * вся логика находится в слоях ui, service и repository.
 * Запуск с параметром --init-db пересоздаёт таблицы и загружает тестовые данные.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        Charset charset = ConsoleSupport.detectCharset();
        PrintStream out = ConsoleSupport.createOut(charset);
        ConsoleInput in = new ConsoleInput(System.in, out, charset);
        int exitCode = new Application(in, out).run(args);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }
}
