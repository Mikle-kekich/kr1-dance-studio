package ru.mirea.dancestudio.model;

/**
 * Базовый класс для всех сущностей, хранящихся в базе данных.
 * Хранит идентификатор (null, пока запись не сохранена) и задаёт общий контракт.
 */
public abstract class BaseEntity {

    private Long id;

    protected BaseEntity(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /** Краткое описание для списков и сообщений; в каждом наследнике своё (полиморфизм). */
    public abstract String getShortDescription();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BaseEntity other = (BaseEntity) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

    @Override
    public String toString() {
        return getShortDescription();
    }
}
