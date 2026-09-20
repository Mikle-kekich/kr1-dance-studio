package ru.mirea.dancestudio.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.mirea.dancestudio.exception.BusinessException;
import ru.mirea.dancestudio.exception.EntityNotFoundException;
import ru.mirea.dancestudio.model.DanceClass;
import ru.mirea.dancestudio.model.DanceLevel;
import ru.mirea.dancestudio.model.DanceStyle;
import ru.mirea.dancestudio.model.Enrollment;
import ru.mirea.dancestudio.model.EnrollmentSortField;
import ru.mirea.dancestudio.model.EnrollmentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Поиск, фильтрация и сортировка записей. */
class EnrollmentQueryServiceTest extends ServiceTestBase {

    private Enrollment first;
    private Enrollment second;
    private Enrollment third;

    @BeforeEach
    void seedEnrollments() {
        DanceClass salsa = classes.save(new DanceClass("Сальса", DanceStyle.SALSA, DanceLevel.BEGINNER,
                "Орлова Дарья", TestData.NOW.plusDays(5), 90, 10, 0, new BigDecimal("1200.00")));
        first = seed(adult, upcoming, EnrollmentStatus.CONFIRMED, true);
        second = seed(teen, upcoming, EnrollmentStatus.CREATED, false);
        third = seed(adult, salsa, EnrollmentStatus.CANCELLED, false);
        third.setPrice(new BigDecimal("1200.00"));
        third.setNote("Отмена из-за болезни");
        enrollments.update(third);
    }

    private static List<Long> ids(List<Enrollment> list) {
        return list.stream().map(Enrollment::getId).toList();
    }

    @Test
    void searchIsCaseInsensitiveAndRequiresText() {
        assertThat(ids(queryService.searchByClientName("иванова"))).containsExactly(first.getId(), third.getId());
        assertThat(ids(queryService.searchByClassOrInstructor("ОРЛОВА"))).containsExactly(third.getId());
        assertThat(ids(queryService.searchByNote("болезни"))).containsExactly(third.getId());
        assertThat(ids(queryService.searchByClassDate(upcoming.getStartTime().toLocalDate())))
                .containsExactly(first.getId(), second.getId());
        assertThatThrownBy(() -> queryService.searchByClientName("   "))
                .isInstanceOf(BusinessException.class).hasMessageContaining("непустую");
    }

    @Test
    void filtersUseStreams() {
        assertThat(ids(queryService.filterByStatus(EnrollmentStatus.CREATED))).containsExactly(second.getId());
        assertThat(ids(queryService.filterByPaid(true))).containsExactly(first.getId());
        assertThat(ids(queryService.filterByStyle(DanceStyle.SALSA))).containsExactly(third.getId());
        assertThat(ids(queryService.filterByClient(teen.getId()))).containsExactly(second.getId());
    }

    @Test
    void dateRangeAcceptsOpenEndsAndRejectsWrongOrder() {
        LocalDate day = upcoming.getStartTime().toLocalDate();
        assertThat(ids(queryService.filterByClassDateRange(day, day))).containsExactly(first.getId(), second.getId());
        assertThat(ids(queryService.filterByClassDateRange(day.plusDays(1), null))).containsExactly(third.getId());
        assertThat(ids(queryService.filterByClassDateRange(null, day))).containsExactly(first.getId(), second.getId());
        assertThatThrownBy(() -> queryService.filterByClassDateRange(day.plusDays(1), day))
                .isInstanceOf(BusinessException.class).hasMessageContaining("не может быть позже");
        assertThatThrownBy(() -> queryService.filterByClassDateRange(null, null))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void filterByUnknownClientFails() {
        assertThatThrownBy(() -> queryService.filterByClient(999)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void sortingByEveryFieldInBothDirections() {
        assertThat(ids(queryService.findAllSorted(EnrollmentSortField.PRICE, true)))
                .containsExactly(first.getId(), second.getId(), third.getId());
        assertThat(ids(queryService.findAllSorted(EnrollmentSortField.PRICE, false)).get(0))
                .isEqualTo(third.getId());
        assertThat(ids(queryService.findAllSorted(EnrollmentSortField.CLIENT_NAME, true)).get(2))
                .isEqualTo(second.getId());
        assertThat(ids(queryService.findAllSorted(EnrollmentSortField.STATUS, true)))
                .containsExactly(second.getId(), first.getId(), third.getId());
        assertThat(ids(queryService.findAllSorted(EnrollmentSortField.CLASS_DATE, false)).get(0))
                .isEqualTo(third.getId());
        assertThat(ids(queryService.findAllSorted(EnrollmentSortField.ID, false)))
                .containsExactly(third.getId(), second.getId(), first.getId());
    }
}
