package ru.mirea.dancestudio.ui;

import org.junit.jupiter.api.Test;
import ru.mirea.dancestudio.exception.InvalidInputException;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InputParsersTest {

    @Test
    void textInsteadOfIdGivesTheRequiredMessage() {
        assertThatThrownBy(() -> InputParsers.id("ID").apply("abc"))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("ID должен быть целым числом.");
        assertThatThrownBy(() -> InputParsers.id("ID").apply("1.5")).hasMessageContaining("целым числом");
        assertThatThrownBy(() -> InputParsers.id("ID клиента").apply("0")).hasMessageContaining("положительным");
        assertThat(InputParsers.id("ID").apply(" 42 ")).isEqualTo(42L);
    }

    @Test
    void integerRespectsRange() {
        assertThat(InputParsers.integer("Номер", 1, 3).apply("2")).isEqualTo(2);
        assertThatThrownBy(() -> InputParsers.integer("Номер", 1, 3).apply("4")).hasMessageContaining("от 1 до 3");
        assertThatThrownBy(() -> InputParsers.integer("Возраст").apply("много")).hasMessageContaining("целое число");
    }

    @Test
    void moneyAcceptsCommaAndDot() {
        assertThat(InputParsers.money("Цена").apply("1500,50")).isEqualByComparingTo(new BigDecimal("1500.50"));
        assertThat(InputParsers.money("Цена").apply("800")).isEqualByComparingTo("800");
        assertThatThrownBy(() -> InputParsers.money("Цена").apply("сто")).hasMessageContaining("введите число");
    }

    @Test
    void datesAreParsedStrictly() {
        assertThat(InputParsers.date("Дата").apply("25.09.2026")).isEqualTo(LocalDate.of(2026, 9, 25));
        assertThatThrownBy(() -> InputParsers.date("Дата").apply("31.02.2026")).hasMessageContaining("дд.мм.гггг");
        assertThatThrownBy(() -> InputParsers.date("Дата").apply("2026-09-25")).hasMessageContaining("дд.мм.гггг");
        assertThat(InputParsers.dateTime("Начало").apply("25.09.2026 19:00"))
                .isEqualTo(LocalDateTime.of(2026, 9, 25, 19, 0));
        assertThatThrownBy(() -> InputParsers.dateTime("Начало").apply("25.09.2026 25:00"))
                .hasMessageContaining("чч:мм");
    }

    @Test
    void yesNoAndPaths() {
        assertThat(InputParsers.yesNo().apply("Да")).isTrue();
        assertThat(InputParsers.yesNo().apply("н")).isFalse();
        assertThatThrownBy(() -> InputParsers.yesNo().apply("может быть")).hasMessageContaining("да");
        assertThat(InputParsers.filePath(".xlsx").apply("отчёт")).isEqualTo(Path.of("отчёт.xlsx"));
        assertThat(InputParsers.filePath(".xlsx").apply("a.XLSX")).isEqualTo(Path.of("a.XLSX"));
        assertThatThrownBy(() -> InputParsers.nonBlank("ФИО").apply("  ")).hasMessageContaining("не может быть пустым");
    }
}
