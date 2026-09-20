package ru.mirea.dancestudio.service;

import ru.mirea.dancestudio.model.TableData;
import ru.mirea.dancestudio.repository.DatabaseInfoRepository;

import java.util.List;

/** Просмотр таблиц базы данных «как есть» и проверка, что схема создана. */
public class DatabaseInfoService {

    static final List<String> REQUIRED_TABLES = List.of("clients", "dance_classes", "enrollments");
    private static final int ROW_LIMIT = 100;

    private final DatabaseInfoRepository repository;

    public DatabaseInfoService(DatabaseInfoRepository repository) {
        this.repository = repository;
    }

    public List<TableData> readAllTables() {
        return repository.listTables().stream()
                .map(name -> repository.readTable(name, ROW_LIMIT))
                .toList();
    }

    /** Созданы ли все таблицы, необходимые программе. */
    public boolean isSchemaReady() {
        return repository.listTables().containsAll(REQUIRED_TABLES);
    }
}
