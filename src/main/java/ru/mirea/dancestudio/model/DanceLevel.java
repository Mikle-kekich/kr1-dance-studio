package ru.mirea.dancestudio.model;

/** Уровень подготовки, на который рассчитано занятие. */
public enum DanceLevel {
    BEGINNER("Начальный"),
    INTERMEDIATE("Средний"),
    ADVANCED("Продвинутый");

    private final String label;

    DanceLevel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
