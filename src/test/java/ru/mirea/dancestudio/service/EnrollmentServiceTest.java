package ru.mirea.dancestudio.service;

import org.junit.jupiter.api.Test;
import ru.mirea.dancestudio.exception.BusinessException;
import ru.mirea.dancestudio.exception.EntityNotFoundException;
import ru.mirea.dancestudio.model.Enrollment;
import ru.mirea.dancestudio.model.EnrollmentStatus;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Бизнес-правила создания и изменения записей на занятия. */
class EnrollmentServiceTest extends ServiceTestBase {

    @Test
    void createUsesClassPriceAndTrimsNote() {
        Enrollment e = enrollmentService.create(adult.getId(), upcoming.getId(), null, false, "  Нужна разминка  ");
        assertThat(e.getId()).isNotNull();
        assertThat(e.getStatus()).isEqualTo(EnrollmentStatus.CREATED);
        assertThat(e.getPrice()).isEqualByComparingTo("800.00");
        assertThat(e.getNote()).isEqualTo("Нужна разминка");
        assertThat(e.getCreatedAt()).isEqualTo(TestData.NOW);
    }

    @Test
    void createRejectsUnknownClientOrClass() {
        assertThatThrownBy(() -> enrollmentService.create(999, upcoming.getId(), null, false, null))
                .isInstanceOf(EntityNotFoundException.class).hasMessageContaining("Клиент с ID 999");
        assertThatThrownBy(() -> enrollmentService.create(adult.getId(), 999, null, false, null))
                .isInstanceOf(EntityNotFoundException.class).hasMessageContaining("Занятие с ID 999");
    }

    @Test
    void createRejectsClassThatAlreadyStarted() {
        assertThatThrownBy(() -> enrollmentService.create(adult.getId(), past.getId(), null, false, null))
                .isInstanceOf(BusinessException.class).hasMessageContaining("уже началось");
    }

    @Test
    void createRejectsClientBelowMinimumAge() {
        assertThatThrownBy(() -> enrollmentService.create(teen.getId(), adultsOnly.getId(), null, false, null))
                .isInstanceOf(BusinessException.class).hasMessageContaining("по возрасту");
    }

    @Test
    void createRejectsDuplicateButAllowsAfterCancellation() {
        Enrollment first = enrollmentService.create(adult.getId(), upcoming.getId(), null, false, null);
        assertThatThrownBy(() -> enrollmentService.create(adult.getId(), upcoming.getId(), null, false, null))
                .isInstanceOf(BusinessException.class).hasMessageContaining("уже записан");

        enrollmentService.changeStatus(first.getId(), EnrollmentStatus.CANCELLED);
        Enrollment again = enrollmentService.create(adult.getId(), upcoming.getId(), null, false, null);
        assertThat(again.getId()).isNotEqualTo(first.getId());
    }

    @Test
    void createRejectsWhenNoFreeSeatsAndCancelledDoesNotHoldSeat() {
        Enrollment first = enrollmentService.create(adult.getId(), singleSeat.getId(), null, false, null);
        assertThatThrownBy(() -> enrollmentService.create(teen.getId(), singleSeat.getId(), null, false, null))
                .isInstanceOf(BusinessException.class).hasMessageContaining("нет свободных мест");

        enrollmentService.changeStatus(first.getId(), EnrollmentStatus.CANCELLED);
        assertThat(enrollmentService.create(teen.getId(), singleSeat.getId(), null, false, null).getId())
                .isNotNull();
    }

    @Test
    void createRejectsInvalidPriceAndLongNote() {
        assertThatThrownBy(() -> enrollmentService.create(adult.getId(), upcoming.getId(),
                new BigDecimal("-1"), false, null)).isInstanceOf(BusinessException.class)
                .hasMessageContaining("отрицательным");
        assertThatThrownBy(() -> enrollmentService.create(adult.getId(), upcoming.getId(),
                new BigDecimal("10.999"), false, null)).isInstanceOf(BusinessException.class)
                .hasMessageContaining("двух знаков");
        assertThatThrownBy(() -> enrollmentService.create(adult.getId(), upcoming.getId(),
                null, false, "x".repeat(501))).isInstanceOf(BusinessException.class)
                .hasMessageContaining("не должно быть длиннее 500");
    }
}
