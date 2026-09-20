package ru.mirea.dancestudio.model;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

/**
 * Поле, по которому можно отсортировать записи на занятия.
 * У каждой константы свой компаратор, поэтому сервису не нужен длинный switch.
 */
public enum EnrollmentSortField {

    CLASS_DATE("дате занятия",
            Comparator.comparing((Enrollment e) -> e.getDanceClass().getStartTime())),
    CLIENT_NAME("ФИО клиента",
            Comparator.comparing((Enrollment e) -> e.getClient().getFullName(), russianCollator())),
    PRICE("стоимости",
            Comparator.comparing(Enrollment::getPrice)),
    STATUS("статусу",
            Comparator.comparing(Enrollment::getStatus)),
    CREATED_AT("дате создания записи",
            Comparator.comparing(Enrollment::getCreatedAt)),
    ID("номеру записи (ID)",
            Comparator.comparing(Enrollment::getId));

    private final String label;
    private final Comparator<Enrollment> comparator;

    EnrollmentSortField(String label, Comparator<Enrollment> comparator) {
        this.label = label;
        this.comparator = comparator;
    }

    public String getLabel() {
        return label;
    }

    /** Компаратор по возрастанию; для убывания используйте {@code comparator(false)}. */
    public Comparator<Enrollment> comparator(boolean ascending) {
        return ascending ? comparator : comparator.reversed();
    }

    private static Collator russianCollator() {
        return Collator.getInstance(Locale.forLanguageTag("ru-RU"));
    }

    @Override
    public String toString() {
        return label;
    }
}
