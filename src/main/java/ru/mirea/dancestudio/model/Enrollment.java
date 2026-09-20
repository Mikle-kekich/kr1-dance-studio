package ru.mirea.dancestudio.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Запись клиента на занятие — основная сущность варианта.
 * Связывает клиента и занятие (связь «многие ко многим» с собственными атрибутами).
 */
public class Enrollment extends BaseEntity implements Exportable {

    public static final List<String> EXPORT_HEADERS = List.of(
            "ID", "Клиент", "Телефон клиента", "Занятие", "Направление", "Преподаватель",
            "Начало занятия", "Статус", "Оплачено", "Стоимость, руб.", "Комментарий", "Дата создания записи");

    private Client client;
    private DanceClass danceClass;
    private EnrollmentStatus status;
    private BigDecimal price;
    private boolean paid;
    private String note;
    private LocalDateTime createdAt;

    /** Новая запись (статус CREATED), ещё не сохранённая в базе данных. */
    public Enrollment(Client client, DanceClass danceClass, BigDecimal price, boolean paid, String note) {
        this(null, client, danceClass, EnrollmentStatus.CREATED, price, paid, note, null);
    }

    /** Запись, загруженная из базы данных. */
    public Enrollment(Long id, Client client, DanceClass danceClass, EnrollmentStatus status,
                      BigDecimal price, boolean paid, String note, LocalDateTime createdAt) {
        super(id);
        this.client = client;
        this.danceClass = danceClass;
        this.status = status;
        this.price = price;
        this.paid = paid;
        this.note = note;
        this.createdAt = createdAt;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public DanceClass getDanceClass() {
        return danceClass;
    }

    public void setDanceClass(DanceClass danceClass) {
        this.danceClass = danceClass;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public void setStatus(EnrollmentStatus status) {
        this.status = status;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String getShortDescription() {
        return "Запись №" + getId() + ": " + client.getFullName() + " -> " + danceClass.getTitle();
    }

    @Override
    public List<Object> toExportRow() {
        return Exportable.row(getId(), client.getFullName(), client.getPhone(), danceClass.getTitle(),
                danceClass.getStyle().getLabel(), danceClass.getInstructor(), danceClass.getStartTime(),
                status.getLabel(), paid ? "Да" : "Нет", price, note, createdAt);
    }
}
