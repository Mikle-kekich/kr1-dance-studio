package ru.mirea.dancestudio.model;

import java.util.EnumSet;
import java.util.Set;

/**
 * Статус записи на занятие.
 * <pre>
 * CREATED --> CONFIRMED --> COMPLETED
 *    |            |
 *    +------------+--> CANCELLED
 * </pre>
 * COMPLETED и CANCELLED — конечные статусы: из них переходов нет.
 */
public enum EnrollmentStatus {
    CREATED("Создана"),
    CONFIRMED("Подтверждена"),
    COMPLETED("Завершена"),
    CANCELLED("Отменена");

    private final String label;

    EnrollmentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** Статусы, в которые разрешён переход из текущего. */
    public Set<EnrollmentStatus> allowedNext() {
        return switch (this) {
            case CREATED -> EnumSet.of(CONFIRMED, CANCELLED);
            case CONFIRMED -> EnumSet.of(COMPLETED, CANCELLED);
            case COMPLETED, CANCELLED -> EnumSet.noneOf(EnrollmentStatus.class);
        };
    }

    public boolean canTransitionTo(EnrollmentStatus next) {
        return allowedNext().contains(next);
    }

    /** Конечный статус: запись больше нельзя изменять. */
    public boolean isFinal() {
        return allowedNext().isEmpty();
    }

    /** Запись занимает место в группе (отменённые записи место не занимают). */
    public boolean occupiesSeat() {
        return this != CANCELLED;
    }

    @Override
    public String toString() {
        return label;
    }
}
