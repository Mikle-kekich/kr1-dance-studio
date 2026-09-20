package ru.mirea.dancestudio.model;

/** Направление танца. В базе данных хранится имя константы (например, HIP_HOP). */
public enum DanceStyle {
    HIP_HOP("Хип-хоп"),
    SALSA("Сальса"),
    BACHATA("Бачата"),
    BALLET("Балет"),
    CONTEMPORARY("Контемпорари"),
    LATINA("Латина"),
    WALTZ("Вальс"),
    JAZZ_FUNK("Джаз-фанк");

    private final String label;

    DanceStyle(String label) {
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
