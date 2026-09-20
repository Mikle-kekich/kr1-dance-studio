package ru.mirea.dancestudio.model;

import java.util.List;

/**
 * Содержимое таблицы базы данных «как есть» (для пункта меню «Вывести таблицы базы данных»).
 *
 * @param name      имя таблицы
 * @param columns   названия столбцов
 * @param rows      строки (значения уже преобразованы в текст)
 * @param totalRows общее число строк в таблице (rows может содержать только часть)
 */
public record TableData(String name, List<String> columns, List<List<String>> rows, long totalRows) {
}
