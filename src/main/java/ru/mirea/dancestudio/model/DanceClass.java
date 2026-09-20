package ru.mirea.dancestudio.model;

import ru.mirea.dancestudio.util.Formats;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Занятие в расписании студии. */
public class DanceClass extends BaseEntity {

    private String title;
    private DanceStyle style;
    private DanceLevel level;
    private String instructor;
    private LocalDateTime startTime;
    private int durationMinutes;
    private int capacity;
    private int minAge;
    private BigDecimal price;

    /** Новое занятие, ещё не сохранённое в базе данных. */
    public DanceClass(String title, DanceStyle style, DanceLevel level, String instructor,
                      LocalDateTime startTime, int durationMinutes, int capacity, int minAge, BigDecimal price) {
        this(null, title, style, level, instructor, startTime, durationMinutes, capacity, minAge, price);
    }

    /** Занятие, загруженное из базы данных. */
    public DanceClass(Long id, String title, DanceStyle style, DanceLevel level, String instructor,
                      LocalDateTime startTime, int durationMinutes, int capacity, int minAge, BigDecimal price) {
        super(id);
        this.title = title;
        this.style = style;
        this.level = level;
        this.instructor = instructor;
        this.startTime = startTime;
        this.durationMinutes = durationMinutes;
        this.capacity = capacity;
        this.minAge = minAge;
        this.price = price;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public DanceStyle getStyle() {
        return style;
    }

    public void setStyle(DanceStyle style) {
        this.style = style;
    }

    public DanceLevel getLevel() {
        return level;
    }

    public void setLevel(DanceLevel level) {
        this.level = level;
    }

    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getMinAge() {
        return minAge;
    }

    public void setMinAge(int minAge) {
        this.minAge = minAge;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    @Override
    public String getShortDescription() {
        return title + ", " + Formats.dateTime(startTime) + " (ID " + getId() + ")";
    }
}
