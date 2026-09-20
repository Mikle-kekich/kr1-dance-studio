package ru.mirea.dancestudio.ui;

import ru.mirea.dancestudio.exception.DanceStudioException;
import ru.mirea.dancestudio.exception.InputClosedException;
import ru.mirea.dancestudio.exception.UserCancelledException;

import java.io.PrintStream;
import java.util.List;

/**
 * Общая часть всех меню: вывод пунктов, чтение выбора и безопасное выполнение действия.
 * Любая ошибка внутри действия превращается в сообщение, и программа возвращается в меню.
 */
public abstract class AbstractMenu implements Menu {

    private static final String LINE = "=".repeat(48);

    protected final ConsoleInput in;
    protected final PrintStream out;
    protected final Printer printer;

    protected AbstractMenu(ConsoleInput in, PrintStream out, Printer printer) {
        this.in = in;
        this.out = out;
        this.printer = printer;
    }

    /** Пункт меню: подпись и действие. Номера присваиваются по порядку, начиная с 1. */
    protected record Item(String label, Runnable action) {
    }

    protected abstract List<Item> items();

    /** Подпись пункта 0. */
    protected String exitLabel() {
        return "Назад";
    }

    @Override
    public void run() {
        List<Item> items = items();
        while (true) {
            printMenu(items);
            int choice = in.readMenuChoice(items.size());
            if (choice == 0) {
                return;
            }
            out.println();
            execute(items.get(choice - 1).action());
            out.println();
        }
    }

    private void printMenu(List<Item> items) {
        out.println(LINE);
        out.println(" " + title());
        out.println(LINE);
        for (int i = 0; i < items.size(); i++) {
            out.println((i + 1) + ". " + items.get(i).label());
        }
        out.println("0. " + exitLabel());
    }

    /** Выполняет действие; ошибки не приводят к завершению программы. */
    protected void execute(Runnable action) {
        try {
            action.run();
        } catch (InputClosedException e) {
            throw e;
        } catch (UserCancelledException e) {
            out.println("Операция отменена.");
        } catch (DanceStudioException e) {
            out.println("Ошибка: " + e.getMessage());
        } catch (RuntimeException e) {
            out.println("Непредвиденная ошибка: " + e);
        }
    }
}
