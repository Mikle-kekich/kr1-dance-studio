package ru.mirea.dancestudio.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ru.mirea.dancestudio.exception.BusinessException;
import ru.mirea.dancestudio.exception.EntityNotFoundException;
import ru.mirea.dancestudio.model.Client;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Проверки данных клиента, уникальность телефона и email, правило удаления. */
class ClientServiceTest extends ServiceTestBase {

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "8 (900) 123-45-67|+79001234567",
            "+7 900 123 45 67|+79001234567",
            "9001234567|+79001234567",
            "+375 29 123-45-67|+375291234567"})
    void phoneIsNormalized(String raw, String expected) {
        assertThat(ClientService.normalizePhone(raw)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"12345", "телефон", "+7 900 abc"})
    void invalidPhoneIsRejected(String raw) {
        assertThatThrownBy(() -> ClientService.normalizePhone(raw)).isInstanceOf(BusinessException.class);
    }

    @Test
    void nameMustHaveTwoWordsAndOnlyLetters() {
        assertThat(ClientService.normalizeName("  Иванов    Иван ")).isEqualTo("Иванов Иван");
        assertThatThrownBy(() -> ClientService.normalizeName("Иван")).hasMessageContaining("двух слов");
        assertThatThrownBy(() -> ClientService.normalizeName("Иван 123")).hasMessageContaining("только буквы");
    }

    @Test
    void createNormalizesDataAndSetsRegistrationTime() {
        Client created = clientService.create(new Client("Тестов  Тест", "8 900 111 22 33",
                "TEST@Example.COM", LocalDate.of(1999, 1, 1)));
        assertThat(created.getId()).isNotNull();
        assertThat(created.getFullName()).isEqualTo("Тестов Тест");
        assertThat(created.getPhone()).isEqualTo("+79001112233");
        assertThat(created.getEmail()).isEqualTo("test@example.com");
        assertThat(created.getRegisteredAt()).isEqualTo(TestData.NOW);
    }

    @Test
    void createRejectsDuplicatePhoneAndEmail() {
        assertThatThrownBy(() -> clientService.create(new Client("Другая Анна", "+7 (901) 111-22-33",
                "other@example.com", LocalDate.of(1990, 1, 1))))
                .isInstanceOf(BusinessException.class).hasMessageContaining("с телефоном");
        assertThatThrownBy(() -> clientService.create(new Client("Другая Анна", "+79990000000",
                "ANNA@example.com", LocalDate.of(1990, 1, 1))))
                .isInstanceOf(BusinessException.class).hasMessageContaining("с email");
    }

    @Test
    void createRejectsBadEmailAndBirthDates() {
        assertThatThrownBy(() -> clientService.create(new Client("Иван Иванов", "+79990000000",
                "bad-email", LocalDate.of(1990, 1, 1)))).hasMessageContaining("Email");
        assertThatThrownBy(() -> clientService.create(new Client("Иван Иванов", "+79990000000",
                "ok@example.com", LocalDate.of(2030, 1, 1)))).hasMessageContaining("в будущем");
        assertThatThrownBy(() -> clientService.create(new Client("Иван Иванов", "+79990000000",
                "ok@example.com", LocalDate.of(2025, 1, 1)))).hasMessageContaining("от 3 лет");
        assertThatThrownBy(() -> clientService.create(new Client("Иван Иванов", "+79990000000",
                "ok@example.com", LocalDate.of(1900, 1, 1)))).hasMessageContaining("слишком большой");
    }

    @Test
    void updateAllowsOwnPhoneButNotSomeoneElses() {
        Client changed = clientService.getById(adult.getId());
        changed.setFullName("Иванова Анна Сергеевна");
        assertThat(clientService.update(changed).getFullName()).isEqualTo("Иванова Анна Сергеевна");

        changed.setPhone(teen.getPhone());
        assertThatThrownBy(() -> clientService.update(changed))
                .isInstanceOf(BusinessException.class).hasMessageContaining("уже существует");
    }

    @Test
    void deleteIsForbiddenForClientsWithEnrollments() {
        enrollmentService.create(adult.getId(), upcoming.getId(), null, false, null);
        assertThatThrownBy(() -> clientService.delete(adult.getId()))
                .isInstanceOf(BusinessException.class).hasMessageContaining("есть записи на занятия (1)");

        clientService.delete(teen.getId());
        assertThatThrownBy(() -> clientService.getById(teen.getId()))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
