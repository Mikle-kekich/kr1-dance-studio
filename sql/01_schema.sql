-- =====================================================================
-- Танцевальная студия (вариант 89): схема базы данных, PostgreSQL.
-- Основная сущность: «Запись на занятие» (enrollments).
-- Связи: clients 1 --- N enrollments N --- 1 dance_classes
-- Скрипт можно запускать повторно: старые таблицы удаляются.
-- =====================================================================

DROP TABLE IF EXISTS enrollments CASCADE;
DROP TABLE IF EXISTS dance_classes CASCADE;
DROP TABLE IF EXISTS clients CASCADE;

-- ---------------------------------------------------------------------
-- Клиенты студии (участники предметной области)
-- ---------------------------------------------------------------------
CREATE TABLE clients (
    id            BIGSERIAL    PRIMARY KEY,
    full_name     VARCHAR(100) NOT NULL,
    phone         VARCHAR(20)  NOT NULL,
    email         VARCHAR(100) NOT NULL,
    birth_date    DATE         NOT NULL,
    registered_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_clients_phone       UNIQUE (phone),
    CONSTRAINT uq_clients_email       UNIQUE (email),
    CONSTRAINT chk_clients_full_name  CHECK (char_length(trim(full_name)) >= 2),
    CONSTRAINT chk_clients_phone      CHECK (phone ~ '^\+[0-9]{11,15}\Z'),
    CONSTRAINT chk_clients_email      CHECK (email ~ '^[^@[:space:]]+@[^@[:space:]]+\.[^@[:space:]]+\Z'),
    CONSTRAINT chk_clients_birth_date CHECK (birth_date >= DATE '1900-01-01')
);

-- ---------------------------------------------------------------------
-- Занятия (расписание студии)
-- ---------------------------------------------------------------------
CREATE TABLE dance_classes (
    id               BIGSERIAL     PRIMARY KEY,
    title            VARCHAR(100)  NOT NULL,
    style            VARCHAR(20)   NOT NULL,
    level            VARCHAR(20)   NOT NULL,
    instructor       VARCHAR(100)  NOT NULL,
    start_time       TIMESTAMP     NOT NULL,
    duration_minutes INTEGER       NOT NULL,
    capacity         INTEGER       NOT NULL,
    min_age          INTEGER       NOT NULL DEFAULT 0,
    price            NUMERIC(10,2) NOT NULL,
    CONSTRAINT chk_classes_title      CHECK (char_length(trim(title)) >= 2),
    CONSTRAINT chk_classes_style      CHECK (style IN ('HIP_HOP', 'SALSA', 'BACHATA', 'BALLET',
                                                       'CONTEMPORARY', 'LATINA', 'WALTZ', 'JAZZ_FUNK')),
    CONSTRAINT chk_classes_level      CHECK (level IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED')),
    CONSTRAINT chk_classes_instructor CHECK (char_length(trim(instructor)) >= 2),
    CONSTRAINT chk_classes_duration   CHECK (duration_minutes BETWEEN 15 AND 240),
    CONSTRAINT chk_classes_capacity   CHECK (capacity BETWEEN 1 AND 100),
    CONSTRAINT chk_classes_min_age    CHECK (min_age BETWEEN 0 AND 99),
    CONSTRAINT chk_classes_price      CHECK (price >= 0)
);

-- ---------------------------------------------------------------------
-- Записи на занятия (основная сущность варианта)
-- ---------------------------------------------------------------------
CREATE TABLE enrollments (
    id         BIGSERIAL     PRIMARY KEY,
    client_id  BIGINT        NOT NULL,
    class_id   BIGINT        NOT NULL,
    status     VARCHAR(20)   NOT NULL DEFAULT 'CREATED',
    price      NUMERIC(10,2) NOT NULL,
    paid       BOOLEAN       NOT NULL DEFAULT FALSE,
    note       VARCHAR(500),
    created_at TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_enrollments_client FOREIGN KEY (client_id)
        REFERENCES clients (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_enrollments_class  FOREIGN KEY (class_id)
        REFERENCES dance_classes (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_enrollments_status CHECK (status IN ('CREATED', 'CONFIRMED', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT chk_enrollments_price  CHECK (price >= 0),
    -- подтверждённая или завершённая запись обязательно оплачена
    CONSTRAINT chk_enrollments_paid   CHECK (status NOT IN ('CONFIRMED', 'COMPLETED') OR paid = TRUE)
);

-- Клиент не может иметь две действующие записи на одно и то же занятие
-- (отменённые записи не учитываются, поэтому после отмены можно записаться снова).
CREATE UNIQUE INDEX uq_enrollments_active_client_class
    ON enrollments (client_id, class_id)
    WHERE status <> 'CANCELLED';

CREATE INDEX idx_enrollments_client_id ON enrollments (client_id);
CREATE INDEX idx_enrollments_class_id  ON enrollments (class_id);
CREATE INDEX idx_enrollments_status    ON enrollments (status);
CREATE INDEX idx_classes_start_time    ON dance_classes (start_time);

COMMENT ON TABLE  clients       IS 'Клиенты танцевальной студии';
COMMENT ON TABLE  dance_classes IS 'Занятия в расписании студии';
COMMENT ON TABLE  enrollments   IS 'Записи клиентов на занятия (основная сущность)';
COMMENT ON COLUMN enrollments.status IS 'CREATED, CONFIRMED, COMPLETED, CANCELLED';
COMMENT ON COLUMN enrollments.paid   IS 'Признак оплаты записи';
