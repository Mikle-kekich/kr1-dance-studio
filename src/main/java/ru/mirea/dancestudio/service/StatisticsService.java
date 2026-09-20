package ru.mirea.dancestudio.service;

import ru.mirea.dancestudio.model.Client;
import ru.mirea.dancestudio.model.DanceClass;
import ru.mirea.dancestudio.model.DanceStyle;
import ru.mirea.dancestudio.model.Enrollment;
import ru.mirea.dancestudio.model.EnrollmentStatus;
import ru.mirea.dancestudio.model.Statistics;
import ru.mirea.dancestudio.repository.ClientRepository;
import ru.mirea.dancestudio.repository.DanceClassRepository;
import ru.mirea.dancestudio.repository.EnrollmentRepository;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Подсчёт сводных показателей. Часть показателей считает база данных (COUNT, SUM, AVG, GROUP BY),
 * часть - Java с помощью Stream API (группировка, среднее, максимум).
 */
public class StatisticsService {

    private final ClientRepository clients;
    private final DanceClassRepository classes;
    private final EnrollmentRepository enrollments;
    private final Clock clock;

    public StatisticsService(ClientRepository clients, DanceClassRepository classes,
                             EnrollmentRepository enrollments, Clock clock) {
        this.clients = clients;
        this.classes = classes;
        this.enrollments = enrollments;
        this.clock = clock;
    }

    public Statistics collect() {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDate today = now.toLocalDate();

        // показатели, посчитанные в базе данных
        long totalClients = clients.count();
        long totalClasses = classes.count();
        Map<EnrollmentStatus, Long> byStatus = enrollments.countByStatus();
        long totalEnrollments = byStatus.values().stream().mapToLong(Long::longValue).sum();
        BigDecimal revenue = enrollments.sumRevenue();
        BigDecimal averagePrice = enrollments.averagePrice();
        Map<Long, Long> seatsTaken = enrollments.countSeatsTakenByClass();

        // показатели, посчитанные в Java (Stream API)
        double averageAge = clients.findAll().stream().mapToInt(c -> c.getAge(today)).average().orElse(0.0);
        List<Enrollment> all = enrollments.findAll();
        long paid = all.stream().filter(Enrollment::isPaid).count();
        List<Enrollment> seatHolders = all.stream().filter(e -> e.getStatus().occupiesSeat()).toList();

        Map<DanceStyle, Long> byStyle = seatHolders.stream().collect(Collectors.groupingBy(
                e -> e.getDanceClass().getStyle(), () -> new EnumMap<>(DanceStyle.class), Collectors.counting()));
        Comparator<Map.Entry<DanceStyle, Long>> styleByCount = Map.Entry.comparingByValue();
        Optional<Map.Entry<DanceStyle, Long>> popular = byStyle.entrySet().stream()
                .max(styleByCount.thenComparing(entry -> -entry.getKey().ordinal()));

        Map<Client, Long> byClient = seatHolders.stream()
                .collect(Collectors.groupingBy(Enrollment::getClient, Collectors.counting()));
        Comparator<Map.Entry<Client, Long>> clientByCount = Map.Entry.comparingByValue();
        Optional<Map.Entry<Client, Long>> topClient = byClient.entrySet().stream()
                .max(clientByCount.thenComparing(entry -> -entry.getKey().getId()));

        List<DanceClass> upcoming = classes.findAll().stream()
                .filter(c -> c.getStartTime().isAfter(now)).toList();
        double occupancy = upcoming.stream()
                .mapToDouble(c -> 100.0 * seatsTaken.getOrDefault(c.getId(), 0L) / c.getCapacity())
                .average().orElse(0.0);

        return new Statistics(
                totalClients, averageAge, totalClasses, upcoming.size(),
                totalEnrollments, byStatus, paid, all.size() - paid,
                revenue, averagePrice,
                popular.map(entry -> entry.getKey().getLabel()).orElse(null),
                popular.map(Map.Entry::getValue).orElse(0L),
                occupancy,
                topClient.map(entry -> entry.getKey().getFullName()).orElse(null),
                topClient.map(Map.Entry::getValue).orElse(0L));
    }
}
