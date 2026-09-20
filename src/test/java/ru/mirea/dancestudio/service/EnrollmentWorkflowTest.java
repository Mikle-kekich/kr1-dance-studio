package ru.mirea.dancestudio.service;

import org.junit.jupiter.api.Test;
import ru.mirea.dancestudio.exception.BusinessException;
import ru.mirea.dancestudio.model.Enrollment;
import ru.mirea.dancestudio.model.EnrollmentStatus;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Изменение записи, смена статуса и удаление: правила БП-9...БП-14. */
class EnrollmentWorkflowTest extends ServiceTestBase {

    @Test
    void updateChangesEditableFields() {
        Enrollment e = enrollmentService.create(adult.getId(), upcoming.getId(), null, false, "старый");
        Enrollment saved = enrollmentService.update(e.getId(), upcoming.getId(), new BigDecimal("750"), true, null);
        assertThat(saved.getPrice()).isEqualByComparingTo("750.00");
        assertThat(saved.isPaid()).isTrue();
        assertThat(saved.getNote()).isNull();
        assertThat(enrollmentService.getById(e.getId()).isPaid()).isTrue();
    }

    @Test
    void updateRejectsFinalStatusAndRemovingPaymentFromConfirmed() {
        Enrollment completed = seed(adult, past, EnrollmentStatus.COMPLETED, true);
        assertThatThrownBy(() -> enrollmentService.update(completed.getId(), past.getId(),
                new BigDecimal("1"), true, null)).isInstanceOf(BusinessException.class)
                .hasMessageContaining("изменять нельзя");

        Enrollment confirmed = seed(adult, upcoming, EnrollmentStatus.CONFIRMED, true);
        assertThatThrownBy(() -> enrollmentService.update(confirmed.getId(), upcoming.getId(),
                new BigDecimal("800"), false, null)).isInstanceOf(BusinessException.class)
                .hasMessageContaining("снять отметку об оплате");
    }

    @Test
    void updateToAnotherClassRechecksRules() {
        Enrollment e = enrollmentService.create(teen.getId(), upcoming.getId(), null, false, null);
        assertThatThrownBy(() -> enrollmentService.update(e.getId(), adultsOnly.getId(),
                new BigDecimal("800"), false, null)).isInstanceOf(BusinessException.class)
                .hasMessageContaining("по возрасту");
        assertThatThrownBy(() -> enrollmentService.update(e.getId(), past.getId(),
                new BigDecimal("800"), false, null)).isInstanceOf(BusinessException.class)
                .hasMessageContaining("уже началось");
        assertThat(enrollmentService.update(e.getId(), singleSeat.getId(), new BigDecimal("800"), false, null)
                .getDanceClass().getId()).isEqualTo(singleSeat.getId());
    }

    @Test
    void forbiddenTransitionsAreRejectedWithHint() {
        Enrollment e = enrollmentService.create(adult.getId(), upcoming.getId(), null, true, null);
        assertThatThrownBy(() -> enrollmentService.changeStatus(e.getId(), EnrollmentStatus.COMPLETED))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Недопустимый переход").hasMessageContaining("Разрешено: Подтверждена");
    }

    @Test
    void confirmRequiresPaymentAndNotStartedClass() {
        Enrollment unpaid = enrollmentService.create(adult.getId(), upcoming.getId(), null, false, null);
        assertThatThrownBy(() -> enrollmentService.changeStatus(unpaid.getId(), EnrollmentStatus.CONFIRMED))
                .isInstanceOf(BusinessException.class).hasMessageContaining("неоплаченную");

        Enrollment stale = seed(teen, past, EnrollmentStatus.CREATED, true);
        assertThatThrownBy(() -> enrollmentService.changeStatus(stale.getId(), EnrollmentStatus.CONFIRMED))
                .isInstanceOf(BusinessException.class).hasMessageContaining("уже началось");

        Enrollment paid = enrollmentService.create(teen.getId(), upcoming.getId(), null, true, null);
        assertThat(enrollmentService.changeStatus(paid.getId(), EnrollmentStatus.CONFIRMED).getStatus())
                .isEqualTo(EnrollmentStatus.CONFIRMED);
    }

    @Test
    void completeOnlyAfterClassStarted() {
        Enrollment future = seed(adult, upcoming, EnrollmentStatus.CONFIRMED, true);
        assertThatThrownBy(() -> enrollmentService.changeStatus(future.getId(), EnrollmentStatus.COMPLETED))
                .isInstanceOf(BusinessException.class).hasMessageContaining("ещё не началось");

        Enrollment held = seed(adult, past, EnrollmentStatus.CONFIRMED, true);
        assertThat(enrollmentService.changeStatus(held.getId(), EnrollmentStatus.COMPLETED).getStatus())
                .isEqualTo(EnrollmentStatus.COMPLETED);
    }

    @Test
    void finalStatusesHaveNoTransitions() {
        Enrollment done = seed(adult, past, EnrollmentStatus.COMPLETED, true);
        assertThatThrownBy(() -> enrollmentService.changeStatus(done.getId(), EnrollmentStatus.CANCELLED))
                .isInstanceOf(BusinessException.class).hasMessageContaining("переходов нет");
    }

    @Test
    void onlyCreatedAndCancelledCanBeDeleted() {
        Enrollment created = enrollmentService.create(adult.getId(), upcoming.getId(), null, false, null);
        enrollmentService.delete(created.getId());
        assertThat(enrollments.findById(created.getId())).isEmpty();

        Enrollment confirmed = seed(adult, upcoming, EnrollmentStatus.CONFIRMED, true);
        Enrollment completed = seed(adult, past, EnrollmentStatus.COMPLETED, true);
        assertThatThrownBy(() -> enrollmentService.delete(confirmed.getId()))
                .isInstanceOf(BusinessException.class).hasMessageContaining("удалить нельзя");
        assertThatThrownBy(() -> enrollmentService.delete(completed.getId()))
                .isInstanceOf(BusinessException.class).hasMessageContaining("удалить нельзя");

        enrollmentService.changeStatus(confirmed.getId(), EnrollmentStatus.CANCELLED);
        enrollmentService.delete(confirmed.getId());
        assertThat(enrollments.findById(confirmed.getId())).isEmpty();
    }
}
