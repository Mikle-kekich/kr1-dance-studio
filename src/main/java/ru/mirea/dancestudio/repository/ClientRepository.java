package ru.mirea.dancestudio.repository;

import ru.mirea.dancestudio.model.Client;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends CrudRepository<Client> {

    Optional<Client> findByPhone(String phone);

    Optional<Client> findByEmail(String email);

    /** Поиск по вхождению подстроки в ФИО, телефон или email (без учёта регистра). */
    List<Client> search(String term);
}
