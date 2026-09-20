package ru.mirea.dancestudio.service;

import org.junit.jupiter.api.Test;
import ru.mirea.dancestudio.model.EnrollmentStatus;
import ru.mirea.dancestudio.model.Statistics;

import static org.assertj.core.api.Assertions.assertThat;

/** Показатели статистики на небольшом наборе записей. */
class StatisticsServiceTest extends ServiceTestBase {

    @Test
    void emptyStudioGivesZeroesWithoutErrors() {
        Statistics empty = new StatisticsService(new FakeRepositories.Clients(), new FakeRepositories.Classes(),
                new FakeEnrollmentRepository(), TestData.CLOCK).collect();
        assertThat(empty.totalClients()).isZero();
        assertThat(empty.totalEnrollments()).isZero();
        assertThat(empty.popularStyle()).isNull();
        assertThat(empty.mostActiveClient()).isNull();
        assertThat(empty.toRows().get("Самое популярное направление")).isEqualTo("нет данных");
    }

    @Test
    void countsRevenueAndLeaders() {
        seed(adult, upcoming, EnrollmentStatus.CONFIRMED, true);
        seed(adult, adultsOnly, EnrollmentStatus.COMPLETED, true);
        seed(teen, upcoming, EnrollmentStatus.CREATED, false);
        seed(teen, singleSeat, EnrollmentStatus.CANCELLED, true);

        Statistics s = statisticsService.collect();
        assertThat(s.totalClients()).isEqualTo(2);
        assertThat(s.totalClasses()).isEqualTo(4);
        assertThat(s.upcomingClasses()).isEqualTo(3);
        assertThat(s.totalEnrollments()).isEqualTo(4);
        assertThat(s.enrollmentsByStatus().get(EnrollmentStatus.CANCELLED)).isEqualTo(1L);
        assertThat(s.paidEnrollments()).isEqualTo(3);
        assertThat(s.unpaidEnrollments()).isEqualTo(1);
        assertThat(s.revenue()).isEqualByComparingTo("1600.00");
        assertThat(s.averageEnrollmentPrice()).isEqualByComparingTo("800.00");
        assertThat(s.popularStyle()).isEqualTo("Хип-хоп");
        assertThat(s.popularStyleEnrollments()).isEqualTo(3);
        assertThat(s.mostActiveClient()).isEqualTo("Иванова Анна");
        assertThat(s.mostActiveClientEnrollments()).isEqualTo(2);
        assertThat(s.averageClientAge()).isBetween(20.0, 30.0);
        assertThat(s.averageOccupancyPercent()).isBetween(0.0, 100.0);
    }

    @Test
    void reportContainsAtLeastFiveIndicators() {
        assertThat(statisticsService.collect().toRows()).hasSizeGreaterThanOrEqualTo(5);
    }
}
