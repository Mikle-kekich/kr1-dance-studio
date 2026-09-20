package ru.mirea.dancestudio.util;

import org.junit.jupiter.api.Test;
import ru.mirea.dancestudio.exception.BusinessException;
import ru.mirea.dancestudio.exception.DanceStudioException;
import ru.mirea.dancestudio.exception.DatabaseException;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

/** Технические ошибки JDBC превращаются в понятные пользователю сообщения. */
class SqlErrorsTest {

    private static DanceStudioException translate(String sqlState) {
        return SqlErrors.translate("Не удалось выполнить запрос", new SQLException("технические подробности", sqlState));
    }

    @Test
    void connectionErrorsMentionServer() {
        DanceStudioException e = translate("08001");
        assertThat(e).isInstanceOf(DatabaseException.class);
        assertThat(e.getMessage()).contains("нет соединения с сервером PostgreSQL").doesNotContain("технические");
    }

    @Test
    void authenticationAndMissingObjectsHaveHints() {
        assertThat(translate("28P01").getMessage()).contains("пароль");
        assertThat(translate("42P01").getMessage()).contains("--init-db");
        DatabaseException missing = (DatabaseException) translate("3D000");
        assertThat(missing.getSqlState()).isEqualTo("3D000");
    }

    @Test
    void constraintViolationsBecomeBusinessErrors() {
        assertThat(translate("23505")).isInstanceOf(BusinessException.class);
        assertThat(translate("23503")).isInstanceOf(BusinessException.class);
        assertThat(translate("23514")).isInstanceOf(BusinessException.class);
    }

    @Test
    void unknownErrorsKeepSqlStateAndFirstLine() {
        DanceStudioException e = SqlErrors.translate("Запрос", new SQLException("первая строка\nвторая строка", "XX000"));
        assertThat(e).isInstanceOf(DatabaseException.class);
        assertThat(e.getMessage()).contains("XX000").contains("первая строка").doesNotContain("вторая");
        assertThat(e.getCause()).isInstanceOf(SQLException.class);
    }
}
