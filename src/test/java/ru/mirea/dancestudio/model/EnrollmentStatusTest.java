package ru.mirea.dancestudio.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.mirea.dancestudio.model.EnrollmentStatus.CANCELLED;
import static ru.mirea.dancestudio.model.EnrollmentStatus.COMPLETED;
import static ru.mirea.dancestudio.model.EnrollmentStatus.CONFIRMED;
import static ru.mirea.dancestudio.model.EnrollmentStatus.CREATED;

class EnrollmentStatusTest {

    @Test
    void allowedTransitionsFollowTheWorkflow() {
        assertThat(CREATED.allowedNext()).containsExactlyInAnyOrder(CONFIRMED, CANCELLED);
        assertThat(CONFIRMED.allowedNext()).containsExactlyInAnyOrder(COMPLETED, CANCELLED);
        assertThat(COMPLETED.allowedNext()).isEmpty();
        assertThat(CANCELLED.allowedNext()).isEmpty();
    }

    @Test
    void transitionCheckAndFinalFlag() {
        assertThat(CREATED.canTransitionTo(CONFIRMED)).isTrue();
        assertThat(CREATED.canTransitionTo(COMPLETED)).isFalse();
        assertThat(CONFIRMED.canTransitionTo(CREATED)).isFalse();
        assertThat(CREATED.isFinal()).isFalse();
        assertThat(COMPLETED.isFinal()).isTrue();
        assertThat(CANCELLED.isFinal()).isTrue();
    }

    @Test
    void onlyCancelledEnrollmentsReleaseTheSeat() {
        assertThat(CREATED.occupiesSeat()).isTrue();
        assertThat(CONFIRMED.occupiesSeat()).isTrue();
        assertThat(COMPLETED.occupiesSeat()).isTrue();
        assertThat(CANCELLED.occupiesSeat()).isFalse();
    }

    @Test
    void labelsAreRussianAndNamesMatchDatabaseValues() {
        assertThat(CREATED.toString()).isEqualTo("Создана");
        assertThat(CANCELLED.getLabel()).isEqualTo("Отменена");
        assertThat(EnrollmentStatus.valueOf("CONFIRMED")).isEqualTo(CONFIRMED);
    }
}
