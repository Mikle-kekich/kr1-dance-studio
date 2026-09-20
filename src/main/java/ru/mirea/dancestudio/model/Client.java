package ru.mirea.dancestudio.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;

/** Клиент танцевальной студии (участник предметной области). */
public class Client extends BaseEntity implements Exportable {

    public static final List<String> EXPORT_HEADERS = List.of(
            "ID", "ФИО", "Телефон", "Email", "Дата рождения", "Возраст", "Дата регистрации");

    private String fullName;
    private String phone;
    private String email;
    private LocalDate birthDate;
    private LocalDateTime registeredAt;

    /** Новый клиент, ещё не сохранённый в базе данных. */
    public Client(String fullName, String phone, String email, LocalDate birthDate) {
        this(null, fullName, phone, email, birthDate, null);
    }

    /** Клиент, загруженный из базы данных. */
    public Client(Long id, String fullName, String phone, String email,
                  LocalDate birthDate, LocalDateTime registeredAt) {
        super(id);
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.birthDate = birthDate;
        this.registeredAt = registeredAt;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    /** Полных лет на указанную дату. */
    public int getAge(LocalDate today) {
        return Period.between(birthDate, today).getYears();
    }

    @Override
    public String getShortDescription() {
        return fullName + " (ID " + getId() + ")";
    }

    @Override
    public List<Object> toExportRow() {
        return Exportable.row(getId(), fullName, phone, email, birthDate, getAge(LocalDate.now()), registeredAt);
    }
}
