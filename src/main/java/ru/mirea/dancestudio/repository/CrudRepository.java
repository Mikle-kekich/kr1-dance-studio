package ru.mirea.dancestudio.repository;

import ru.mirea.dancestudio.model.BaseEntity;

import java.util.List;
import java.util.Optional;

/**
 * Общий контракт хранилища сущностей (интерфейс уровня доступа к данным).
 * Реализации работают с PostgreSQL через JDBC, но сервисы зависят только от интерфейса.
 *
 * @param <T> тип сущности
 */
public interface CrudRepository<T extends BaseEntity> {

    /** Добавляет новую запись и записывает в сущность присвоенный базой ID. */
    T save(T entity);

    Optional<T> findById(long id);

    List<T> findAll();

    /** Обновляет существующую запись; если её нет - бросает EntityNotFoundException. */
    void update(T entity);

    /** @return true, если запись существовала и была удалена */
    boolean deleteById(long id);

    long count();
}
