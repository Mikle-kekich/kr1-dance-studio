package ru.mirea.dancestudio.repository;

import ru.mirea.dancestudio.model.TableData;

import java.util.List;

/** Служебный доступ к метаданным и «сырому» содержимому таблиц базы данных. */
public interface DatabaseInfoRepository {

    /** Имена пользовательских таблиц базы данных. */
    List<String> listTables();

    boolean tableExists(String tableName);

    /** Читает первые limit строк таблицы; имя таблицы должно быть в списке listTables(). */
    TableData readTable(String tableName, int limit);
}
