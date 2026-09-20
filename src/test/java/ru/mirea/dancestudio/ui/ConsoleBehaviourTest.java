package ru.mirea.dancestudio.ui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.mirea.dancestudio.exception.BusinessException;
import ru.mirea.dancestudio.exception.DatabaseException;
import ru.mirea.dancestudio.exception.InputClosedException;
import ru.mirea.dancestudio.exception.UserCancelledException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Программа не падает при ошибках ввода и ошибках слоёв ниже: сообщение и возврат в меню. */
class ConsoleBehaviourTest {

    private ByteArrayOutputStream bytes;
    private PrintStream out;

    @BeforeEach
    void setUp() {
        bytes = new ByteArrayOutputStream();
        out = new PrintStream(bytes, true, StandardCharsets.UTF_8);
    }

    private ConsoleInput input(String text) {
        return new ConsoleInput(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)),
                out, StandardCharsets.UTF_8);
    }

    private String output() {
        return bytes.toString(StandardCharsets.UTF_8);
    }

    /** Меню, действия которого завершаются разными исключениями. */
    private final class FailingMenu extends AbstractMenu {
        FailingMenu(String userInput) {
            super(input(userInput), ConsoleBehaviourTest.this.out,
                    new Printer(ConsoleBehaviourTest.this.out, Clock.systemDefaultZone()));
        }

        @Override
        public String title() {
            return "ТЕСТОВОЕ МЕНЮ";
        }

        @Override
        protected List<Item> items() {
            return List.of(
                    new Item("бизнес-правило", () -> {
                        throw new BusinessException("нарушено правило");
                    }),
                    new Item("база данных", () -> {
                        throw new DatabaseException("сервер недоступен");
                    }),
                    new Item("непредвиденное", () -> {
                        throw new IllegalStateException("сбой");
                    }),
                    new Item("отмена", () -> {
                        throw new UserCancelledException();
                    }));
        }
    }

    @Test
    void errorsInActionsDoNotLeaveTheMenu() {
        new FailingMenu("1\n2\n3\n4\n0\n").run();
        assertThat(output()).contains("Ошибка: нарушено правило", "Ошибка: сервер недоступен",
                "Непредвиденная ошибка", "Операция отменена.");
    }

    @Test
    void invalidMenuChoiceIsRepeated() {
        new FailingMenu("x\n9\n\n0\n").run();
        assertThat(output().split("Ошибка: выберите пункт меню от 0 до 4.", -1)).hasSize(4);
    }

    @Test
    void endOfInputStopsTheProgramInsteadOfLooping() {
        assertThatThrownBy(() -> new FailingMenu("").run()).isInstanceOf(InputClosedException.class);
        assertThatThrownBy(() -> new FailingMenu("1\n").run()).isInstanceOf(InputClosedException.class);
    }

    @Test
    void readRepeatsQuestionUntilValueIsCorrect() {
        long id = input("abc\n-3\n7\n").read("Введите ID: ", InputParsers.id("ID"));
        assertThat(id).isEqualTo(7);
        assertThat(output()).contains("Ошибка: ID должен быть целым числом.", "Ошибка: ID должен быть положительным");
    }

    @Test
    void wordCancelAbortsTheOperation() {
        assertThatThrownBy(() -> input("ОтМеНа\n").readLine("Название: ")).isInstanceOf(UserCancelledException.class);
    }

    @Test
    void emptyLineKeepsCurrentValue() {
        ConsoleInput in = input("\nновое\n");
        assertThat(in.readTextOrKeep("ФИО", "Старое значение")).isEqualTo("Старое значение");
        assertThat(in.readTextOrKeep("ФИО", "Старое значение")).isEqualTo("новое");
    }

    @Test
    void chooseRepeatsUntilNumberIsInRange() {
        String picked = input("5\nтекст\n2\n").choose("Выберите:", List.of("первый", "второй"), s -> s);
        assertThat(picked).isEqualTo("второй");
        assertThat(output()).contains("1. первый", "2. второй", "от 1 до 2");
    }

    @Test
    void confirmUnderstandsYesAndNo() {
        ConsoleInput in = input("может\nда\nнет\n");
        assertThat(in.confirm("Удалить?")).isTrue();
        assertThat(in.confirm("Ещё раз?")).isFalse();
        assertThat(output()).contains("Ответьте «да» или «нет».");
    }
}
