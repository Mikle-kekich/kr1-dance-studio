-- =====================================================================
-- Танцевальная студия: начальные тестовые данные.
-- 8 клиентов, 10 занятий, 16 записей во всех четырёх статусах.
-- Даты занятий заданы относительно текущей даты, поэтому в любой день
-- есть и прошедшие, и предстоящие занятия.
-- =====================================================================

TRUNCATE TABLE enrollments, dance_classes, clients RESTART IDENTITY CASCADE;

INSERT INTO clients (full_name, phone, email, birth_date, registered_at) VALUES
    ('Иванова Анна Сергеевна',      '+79011112233', 'anna.ivanova@example.com',       DATE '1998-04-12', CURRENT_TIMESTAMP - INTERVAL '120 days'),
    ('Петров Дмитрий Алексеевич',   '+79022223344', 'dmitry.petrov@example.com',      DATE '1990-11-03', CURRENT_TIMESTAMP - INTERVAL '100 days'),
    ('Сидорова Мария Игоревна',     '+79033334455', 'maria.sidorova@example.com',     DATE '2005-07-21', CURRENT_TIMESTAMP - INTERVAL '80 days'),
    ('Козлов Артём Николаевич',     '+79044445566', 'artem.kozlov@example.com',       DATE '2001-02-14', CURRENT_TIMESTAMP - INTERVAL '60 days'),
    ('Смирнова Екатерина Павловна', '+79055556677', 'ekaterina.smirnova@example.com', DATE '1995-09-30', CURRENT_TIMESTAMP - INTERVAL '45 days'),
    ('Новиков Илья Владимирович',   '+79066667788', 'ilya.novikov@example.com',       DATE '2012-05-18', CURRENT_TIMESTAMP - INTERVAL '30 days'),
    ('Морозова Ольга Андреевна',    '+79077778899', 'olga.morozova@example.com',      DATE '1987-12-08', CURRENT_TIMESTAMP - INTERVAL '20 days'),
    ('Волков Максим Денисович',     '+79088889900', 'maxim.volkov@example.com',       DATE '2008-03-25', CURRENT_TIMESTAMP - INTERVAL '10 days');

INSERT INTO dance_classes (title, style, level, instructor, start_time, duration_minutes, capacity, min_age, price) VALUES
    ('Хип-хоп для начинающих',          'HIP_HOP',      'BEGINNER',     'Кузнецов Алексей', (CURRENT_DATE - 12) + TIME '19:00', 60, 12, 12,  800.00),
    ('Сальса: базовый курс',            'SALSA',        'BEGINNER',     'Орлова Дарья',     (CURRENT_DATE -  9) + TIME '20:00', 90, 10, 16, 1000.00),
    ('Бачата для пар',                  'BACHATA',      'INTERMEDIATE', 'Орлова Дарья',     (CURRENT_DATE -  6) + TIME '19:30', 90,  8, 16, 1100.00),
    ('Классический балет для взрослых', 'BALLET',       'BEGINNER',     'Соколова Елена',   (CURRENT_DATE -  3) + TIME '18:00', 75,  8, 16, 1200.00),
    ('Контемпорари: импровизация',      'CONTEMPORARY', 'INTERMEDIATE', 'Морозов Игорь',    (CURRENT_DATE +  2) + TIME '19:00', 90, 10, 14, 1100.00),
    ('Латина: соло',                    'LATINA',       'INTERMEDIATE', 'Петрова Ирина',    (CURRENT_DATE +  3) + TIME '20:00', 60, 12, 16,  900.00),
    ('Хип-хоп: продвинутый уровень',    'HIP_HOP',      'ADVANCED',     'Кузнецов Алексей', (CURRENT_DATE +  4) + TIME '19:00', 90,  3, 14, 1000.00),
    ('Вальс для начинающих',            'WALTZ',        'BEGINNER',     'Соколова Елена',   (CURRENT_DATE +  6) + TIME '18:30', 60,  8, 18, 1300.00),
    ('Джаз-фанк',                       'JAZZ_FUNK',    'INTERMEDIATE', 'Морозов Игорь',    (CURRENT_DATE +  7) + TIME '20:00', 60, 10, 14,  950.00),
    ('Хип-хоп для подростков',          'HIP_HOP',      'BEGINNER',     'Кузнецов Алексей', (CURRENT_DATE +  5) + TIME '17:00', 60, 10, 12,  700.00);

-- Записи: client_id, class_id, status, price, paid, note, created_at
INSERT INTO enrollments (client_id, class_id, status, price, paid, note, created_at) VALUES
    -- завершённые (прошедшие занятия, оплачены)
    (1,  1, 'COMPLETED',  800.00, TRUE,  NULL,                                 CURRENT_TIMESTAMP - INTERVAL '20 days'),
    (3,  1, 'COMPLETED',  800.00, TRUE,  'Первое занятие в студии',            CURRENT_TIMESTAMP - INTERVAL '19 days'),
    (2,  2, 'COMPLETED', 1000.00, TRUE,  NULL,                                 CURRENT_TIMESTAMP - INTERVAL '15 days'),
    (5,  3, 'COMPLETED', 1100.00, TRUE,  'Пришла с партнёром',                 CURRENT_TIMESTAMP - INTERVAL '12 days'),
    (7,  4, 'COMPLETED', 1200.00, TRUE,  NULL,                                 CURRENT_TIMESTAMP - INTERVAL '9 days'),
    -- отменённые
    (4,  2, 'CANCELLED', 1000.00, FALSE, 'Отмена: заболел',                    CURRENT_TIMESTAMP - INTERVAL '14 days'),
    (8,  3, 'CANCELLED', 1100.00, FALSE, 'Клиент передумал',                   CURRENT_TIMESTAMP - INTERVAL '11 days'),
    (6, 10, 'CANCELLED',  700.00, FALSE, 'Отмена по просьбе родителей',        CURRENT_TIMESTAMP - INTERVAL '6 days'),
    -- подтверждённые (предстоящие занятия, оплачены)
    (1,  5, 'CONFIRMED', 1100.00, TRUE,  'Нужна разминка для колена',          CURRENT_TIMESTAMP - INTERVAL '5 days'),
    (2,  7, 'CONFIRMED', 1000.00, TRUE,  NULL,                                 CURRENT_TIMESTAMP - INTERVAL '4 days'),
    (4,  7, 'CONFIRMED', 1000.00, TRUE,  NULL,                                 CURRENT_TIMESTAMP - INTERVAL '4 days'),
    (8,  7, 'CONFIRMED', 1000.00, TRUE,  'Занимается хип-хопом второй год',    CURRENT_TIMESTAMP - INTERVAL '3 days'),
    -- созданные (ожидают оплаты)
    (3,  6, 'CREATED',    900.00, FALSE, NULL,                                 CURRENT_TIMESTAMP - INTERVAL '2 days'),
    (5,  9, 'CREATED',    950.00, FALSE, 'Оплата на месте',                    CURRENT_TIMESTAMP - INTERVAL '2 days'),
    (6, 10, 'CREATED',    700.00, FALSE, 'Повторная запись после отмены',      CURRENT_TIMESTAMP - INTERVAL '1 day'),
    (7,  5, 'CREATED',   1100.00, FALSE, 'Хочет попробовать пробное занятие',  CURRENT_TIMESTAMP - INTERVAL '1 day');
