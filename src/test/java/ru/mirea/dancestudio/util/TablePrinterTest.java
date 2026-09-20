package ru.mirea.dancestudio.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TablePrinterTest {

    @Test
    void rendersAlignedTableWithBorders() {
        String table = TablePrinter.render(List.of("ID", "Имя"), List.of(List.of("1", "Анна"), List.of("22", "Ян")));
        assertThat(table.split("\n")).containsExactly(
                "+----+------+",
                "| ID | Имя  |",
                "+----+------+",
                "| 1  | Анна |",
                "| 22 | Ян   |",
                "+----+------+");
    }

    @Test
    void longCellsAreCutAndNullsAreEmpty() {
        String table = TablePrinter.render(List.of("Текст", "Пусто"),
                List.of(Arrays.asList("очень-очень длинное значение", null)), 10);
        assertThat(table).contains("| очень-о... |");
        assertThat(table.split("\n")[3]).endsWith("|       |");
    }

    @Test
    void pluralFollowsRussianRules() {
        assertThat(Formats.plural(1, "запись", "записи", "записей")).isEqualTo("запись");
        assertThat(Formats.plural(3, "запись", "записи", "записей")).isEqualTo("записи");
        assertThat(Formats.plural(5, "запись", "записи", "записей")).isEqualTo("записей");
        assertThat(Formats.plural(11, "запись", "записи", "записей")).isEqualTo("записей");
        assertThat(Formats.plural(21, "запись", "записи", "записей")).isEqualTo("запись");
        assertThat(Formats.plural(112, "запись", "записи", "записей")).isEqualTo("записей");
    }
}
