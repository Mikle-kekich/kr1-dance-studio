package ru.mirea.dancestudio.model;

import java.util.List;

/**
 * Занятие вместе с числом занятых мест (занятость считается запросом к записям).
 *
 * @param danceClass занятие
 * @param booked     число записей, занимающих место (все, кроме отменённых)
 */
public record ClassOccupancy(DanceClass danceClass, int booked) implements Exportable {

    public static final List<String> EXPORT_HEADERS = List.of(
            "ID", "Название", "Направление", "Уровень", "Преподаватель", "Начало", "Длительность, мин",
            "Мест всего", "Занято", "Свободно", "Мин. возраст", "Цена, руб.");

    public int capacity() {
        return danceClass.getCapacity();
    }

    public int free() {
        return Math.max(0, capacity() - booked);
    }

    public boolean isFull() {
        return free() == 0;
    }

    public double fillPercent() {
        return capacity() == 0 ? 0.0 : 100.0 * booked / capacity();
    }

    @Override
    public List<Object> toExportRow() {
        DanceClass c = danceClass;
        return Exportable.row(c.getId(), c.getTitle(), c.getStyle().getLabel(), c.getLevel().getLabel(),
                c.getInstructor(), c.getStartTime(), c.getDurationMinutes(), capacity(), booked, free(),
                c.getMinAge(), c.getPrice());
    }
}
