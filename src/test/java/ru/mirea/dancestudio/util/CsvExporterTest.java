package ru.mirea.dancestudio.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CsvExporterTest {

    @Test
    void valuesAreFormattedForRussianExcel() {
        assertThat(CsvExporter.format(null)).isEmpty();
        assertThat(CsvExporter.format(LocalDate.of(2026, 9, 5))).isEqualTo("05.09.2026");
        assertThat(CsvExporter.format(LocalDateTime.of(2026, 9, 5, 7, 30))).isEqualTo("05.09.2026 07:30");
        assertThat(CsvExporter.format(new BigDecimal("1500.50"))).isEqualTo("1500,50");
        assertThat(CsvExporter.format(true)).isEqualTo("Да");
    }

    @Test
    void specialCharactersAreQuoted() {
        assertThat(CsvExporter.escape("обычный текст")).isEqualTo("обычный текст");
        assertThat(CsvExporter.escape("а;б")).isEqualTo("\"а;б\"");
        assertThat(CsvExporter.escape("он сказал \"привет\"")).isEqualTo("\"он сказал \"\"привет\"\"\"");
    }

    @Test
    void fileHasBomHeaderAndRows(@TempDir Path dir) throws Exception {
        List<List<Object>> rows = List.of(Arrays.asList(1L, "Иванова; Анна", null));
        Path file = dir.resolve("out.csv");
        CsvExporter.export(file, new ExportTable("Тест", List.of("ID", "Имя", "Заметка"), rows));

        byte[] bytes = Files.readAllBytes(file);
        assertThat(bytes[0]).isEqualTo((byte) 0xEF);
        String text = new String(bytes, StandardCharsets.UTF_8).substring(1);
        assertThat(text.split("\r\n")).containsExactly("ID;Имя;Заметка", "1;\"Иванова; Анна\";");
    }
}
