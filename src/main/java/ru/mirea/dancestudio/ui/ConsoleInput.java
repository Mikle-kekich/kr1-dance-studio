package ru.mirea.dancestudio.ui;

import ru.mirea.dancestudio.exception.InputClosedException;
import ru.mirea.dancestudio.exception.InvalidInputException;
import ru.mirea.dancestudio.exception.UserCancelledException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.function.Function;

/**
 * Чтение данных из консоли. Ошибки формата (текст вместо числа и т. п.) не прерывают программу:
 * выводится сообщение и вопрос задаётся снова. Слово «отмена» отменяет текущую операцию.
 */
public class ConsoleInput {

    private static final String CANCEL_WORD = "отмена";

    private final BufferedReader reader;
    private final PrintStream out;

    public ConsoleInput(InputStream in, PrintStream out, Charset charset) {
        this.reader = new BufferedReader(new InputStreamReader(in, charset));
        this.out = out;
    }

    /** Читает строку без специальной обработки слова «отмена» (для выбора пункта меню). */
    private String readRaw(String prompt) {
        out.print(prompt);
        out.flush();
        try {
            String line = reader.readLine();
            if (line == null) {
                throw new InputClosedException();
            }
            return line.strip();
        } catch (IOException e) {
            throw new InputClosedException();
        }
    }

    /** Читает строку; слово «отмена» бросает UserCancelledException. */
    public String readLine(String prompt) {
        String line = readRaw(prompt);
        if (line.equalsIgnoreCase(CANCEL_WORD)) {
            throw new UserCancelledException();
        }
        return line;
    }

    /** Номер пункта меню от 0 до maxOption; при неверном вводе вопрос повторяется. */
    public int readMenuChoice(int maxOption) {
        while (true) {
            String line = readRaw("Выберите действие: ");
            try {
                int value = Integer.parseInt(line);
                if (value >= 0 && value <= maxOption) {
                    return value;
                }
            } catch (NumberFormatException e) {
                // сообщение об ошибке выводится ниже
            }
            out.println("Ошибка: выберите пункт меню от 0 до " + maxOption + ".");
        }
    }

    /** Читает значение, пока оно не пройдёт разбор; ошибки формата выводятся пользователю. */
    public <T> T read(String prompt, Function<String, T> parser) {
        while (true) {
            String line = readLine(prompt);
            try {
                return parser.apply(line);
            } catch (InvalidInputException e) {
                out.println("Ошибка: " + e.getMessage());
            }
        }
    }

    /** Как read, но пустой ввод означает «оставить текущее значение». */
    public <T> T readOrKeep(String prompt, T current, Function<String, T> parser) {
        while (true) {
            String line = readLine(prompt);
            if (line.isEmpty()) {
                return current;
            }
            try {
                return parser.apply(line);
            } catch (InvalidInputException e) {
                out.println("Ошибка: " + e.getMessage());
            }
        }
    }

    /** Необязательное значение: пустой ввод даёт null. */
    public <T> T readOptional(String prompt, Function<String, T> parser) {
        return readOrKeep(prompt, null, parser);
    }

    /** Обязательная непустая строка. */
    public String readRequired(String prompt) {
        return read(prompt, InputParsers.nonBlank(prompt.replace(":", "").trim()));
    }

    /** Необязательная строка; пустой ввод даёт пустую строку. */
    public String readOptionalText(String prompt) {
        return readLine(prompt);
    }

    /** Текстовое поле с текущим значением: Enter оставляет его без изменений. */
    public String readTextOrKeep(String label, String current) {
        String line = readLine(label + " [" + current + "]: ");
        return line.isEmpty() ? current : line;
    }

    /** Выбор одного варианта из пронумерованного списка. */
    public <T> T choose(String title, List<T> options, Function<T, String> label) {
        printOptions(title, options, label);
        int index = read("Введите номер: ", InputParsers.integer("Номер", 1, options.size()));
        return options.get(index - 1);
    }

    /** Выбор варианта; пустой ввод оставляет текущий. */
    public <T> T chooseOrKeep(String title, List<T> options, Function<T, String> label, T current) {
        printOptions(title, options, label);
        int index = readOrKeep("Введите номер (Enter - оставить «" + label.apply(current) + "»): ", 0,
                InputParsers.integer("Номер", 1, options.size()));
        return index == 0 ? current : options.get(index - 1);
    }

    /** Вопрос «да/нет». */
    public boolean confirm(String question) {
        return read(question + " (да/нет): ", InputParsers.yesNo());
    }

    private <T> void printOptions(String title, List<T> options, Function<T, String> label) {
        out.println(title);
        for (int i = 0; i < options.size(); i++) {
            out.println("  " + (i + 1) + ". " + label.apply(options.get(i)));
        }
    }
}
