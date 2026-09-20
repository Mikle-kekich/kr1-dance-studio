package ru.mirea.dancestudio.service;

import org.junit.jupiter.api.Test;
import ru.mirea.dancestudio.exception.BusinessException;
import ru.mirea.dancestudio.model.ClassOccupancy;
import ru.mirea.dancestudio.model.DanceClass;
import ru.mirea.dancestudio.model.EnrollmentStatus;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Правила расписания: данные занятия, вместимость, удаление, подсчёт занятых мест. */
class DanceClassServiceTest extends ServiceTestBase {

    @Test
    void createRejectsPastStartAndBadNumbers() {
        assertThatThrownBy(() -> classService.create(TestData.classIn(-1, 10, 0)))
                .isInstanceOf(BusinessException.class).hasMessageContaining("в прошлом");

        DanceClass shortClass = TestData.classIn(1, 10, 0);
        shortClass.setDurationMinutes(5);
        assertThatThrownBy(() -> classService.create(shortClass)).hasMessageContaining("от 15 до 240");

        DanceClass empty = TestData.classIn(1, 0, 0);
        assertThatThrownBy(() -> classService.create(empty)).hasMessageContaining("Вместимость");

        DanceClass expensive = TestData.classIn(1, 10, 0);
        expensive.setPrice(new BigDecimal("1000000000"));
        assertThatThrownBy(() -> classService.create(expensive)).hasMessageContaining("слишком большое");
    }

    @Test
    void createTrimsTextAndKeepsScaleOfPrice() {
        DanceClass c = TestData.classIn(1, 10, 0);
        c.setTitle("  Сальса   для   пар ");
        c.setPrice(new BigDecimal("999.5"));
        DanceClass saved = classService.create(c);
        assertThat(saved.getTitle()).isEqualTo("Сальса для пар");
        assertThat(saved.getPrice()).isEqualByComparingTo("999.50");
    }

    @Test
    void capacityCannotBeLowerThanTakenSeats() {
        enrollmentService.create(adult.getId(), upcoming.getId(), null, false, null);
        enrollmentService.create(teen.getId(), upcoming.getId(), null, false, null);
        DanceClass changed = classService.getById(upcoming.getId());
        changed.setCapacity(1);
        assertThatThrownBy(() -> classService.update(changed))
                .isInstanceOf(BusinessException.class).hasMessageContaining("занятых мест (2)");
        changed.setCapacity(2);
        assertThat(classService.update(changed).getCapacity()).isEqualTo(2);
    }

    @Test
    void movingClassToThePastIsRejected() {
        DanceClass changed = classService.getById(upcoming.getId());
        changed.setStartTime(TestData.NOW.minusDays(1));
        assertThatThrownBy(() -> classService.update(changed))
                .isInstanceOf(BusinessException.class).hasMessageContaining("прошедшее время");
    }

    @Test
    void classWithEnrollmentsCannotBeDeleted() {
        enrollmentService.create(adult.getId(), upcoming.getId(), null, false, null);
        assertThatThrownBy(() -> classService.delete(upcoming.getId()))
                .isInstanceOf(BusinessException.class).hasMessageContaining("на него есть записи (1)");
        classService.delete(adultsOnly.getId());
        assertThat(classes.findById(adultsOnly.getId())).isEmpty();
    }

    @Test
    void occupancyCountsEverythingExceptCancelled() {
        seed(adult, upcoming, EnrollmentStatus.CONFIRMED, true);
        seed(teen, upcoming, EnrollmentStatus.CANCELLED, false);
        ClassOccupancy occupancy = classService.getOccupancy(upcoming.getId());
        assertThat(occupancy.booked()).isEqualTo(1);
        assertThat(occupancy.free()).isEqualTo(9);
        assertThat(occupancy.isFull()).isFalse();
        assertThat(classService.listUpcomingWithOccupancy()).extracting(o -> o.danceClass().getId())
                .containsExactlyInAnyOrder(upcoming.getId(), adultsOnly.getId(), singleSeat.getId());
    }
}
