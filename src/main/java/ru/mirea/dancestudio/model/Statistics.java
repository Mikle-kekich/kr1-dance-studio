package ru.mirea.dancestudio.model;

import ru.mirea.dancestudio.util.Formats;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Сводные показатели работы студии.
 *
 * @param popularStyle название самого популярного направления (или null, если записей нет)
 */
public record Statistics(
        long totalClients,
        double averageClientAge,
        long totalClasses,
        long upcomingClasses,
        long totalEnrollments,
        Map<EnrollmentStatus, Long> enrollmentsByStatus,
        long paidEnrollments,
        long unpaidEnrollments,
        BigDecimal revenue,
        BigDecimal averageEnrollmentPrice,
        String popularStyle,
        long popularStyleEnrollments,
        double averageOccupancyPercent,
        String mostActiveClient,
        long mostActiveClientEnrollments) {

    /** Показатели в виде пар «название - значение» (для вывода в консоль и в Excel). */
    public Map<String, String> toRows() {
        Map<String, String> rows = new LinkedHashMap<>();
        rows.put("Всего клиентов", String.valueOf(totalClients));
        rows.put("Средний возраст клиентов", String.format(Locale.ROOT, "%.1f лет", averageClientAge));
        rows.put("Всего занятий в расписании", String.valueOf(totalClasses));
        rows.put("Из них предстоящих", String.valueOf(upcomingClasses));
        rows.put("Всего записей на занятия", String.valueOf(totalEnrollments));
        for (EnrollmentStatus status : EnrollmentStatus.values()) {
            rows.put("  записей со статусом «" + status.getLabel() + "»",
                    String.valueOf(enrollmentsByStatus.getOrDefault(status, 0L)));
        }
        rows.put("Оплаченных записей", String.valueOf(paidEnrollments));
        rows.put("Неоплаченных записей", String.valueOf(unpaidEnrollments));
        rows.put("Выручка (оплаченные, без отменённых)", Formats.money(revenue));
        rows.put("Средняя стоимость записи", Formats.money(averageEnrollmentPrice));
        rows.put("Самое популярное направление", popularStyle == null
                ? "нет данных"
                : popularStyle + " (" + popularStyleEnrollments + " "
                + Formats.plural(popularStyleEnrollments, "запись", "записи", "записей") + ")");
        rows.put("Средняя заполненность предстоящих занятий",
                String.format(Locale.ROOT, "%.0f%%", averageOccupancyPercent));
        rows.put("Самый активный клиент", mostActiveClient == null
                ? "нет данных"
                : mostActiveClient + " (" + mostActiveClientEnrollments + " "
                + Formats.plural(mostActiveClientEnrollments, "запись", "записи", "записей") + ")");
        return rows;
    }
}
