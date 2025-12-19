INSERT INTO auto_personal (first_name, last_name, father_name) VALUES
('Александр', 'Никифоров', 'Вячеславович'),
('Александр', 'Псарев', 'Сергеевич'),
('Семён', 'Зинаков', 'Сергеевич'),
('Михаил', 'Соболевский', 'Андреевич'),
('Артем', 'Ананьев', 'Максимович');


INSERT INTO auto (num, color, mark, personal_id) VALUES
('а587те', 'Белый', 'Фольксваген', 1),
('г647тн', 'Белый', 'Лисян', 1),
('у901ов', 'Белый', 'Тесла', 2),
('р001мр', 'Черный', 'УАЗ', 2),
('р555аа', 'Черный', 'Фредлайнер', 3),
('а777мр', 'Черный', 'Мерседес', 4),
('у321тт', 'Синий', 'АвтоВАЗ', 4),
('а522ам', 'Голубой', 'АвтоВАЗ', 5),
('е456то', 'Красный', 'Ниссан', 5);


INSERT INTO routes (name) VALUES
('Центр - Крестовский остров'),
('Ленинградский Вокзал - Аэропорт'),
('Балтийский вокзал - Финляндский вокзал'),
('Общежитие №6 - СПБПУ'),
('Кузьмолово - СПБПУ'),
('Академ-парк - СПБПУ'),
('Купчино - СПБПУ'),
('Девяткино - СПБПУ');


INSERT INTO journal (auto_id, route_id, time_out, time_in) VALUES
(1, 1, '2025-09-30 08:00:00', '2025-09-30 08:45:00'),
(2, 2, '2025-09-30 09:15:00', '2025-09-30 10:00:00'),
(3, 1, '2025-09-30 10:30:00', '2025-09-30 11:15:00'),
(4, 3, '2025-09-30 12:00:00', '2025-09-30 12:40:00'),
(1, 1, '2025-09-21 08:00:00', '2025-09-21 08:35:00'),

(2, 2, '2024-05-21 14:00:00', NULL),
(3, 1, '2024-05-21 14:30:00', NULL),

(5, 4, '2024-05-20 11:00:00', '2024-05-20 11:50:00'),
(6, 4, '2024-05-20 13:00:00', '2024-05-20 13:45:00'),
(1, 2, '2024-05-21 09:00:00', '2024-05-21 09:50:00'),
(2, 1, '2024-05-21 09:00:00', '2024-05-21 09:30:00');


CREATE OR REPLACE FUNCTION verify_car_time_in()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM journal
        WHERE auto_id = NEW.auto_id AND time_in IS NULL
    ) THEN
        RAISE EXCEPTION 'Автомобиль с id % еще не вернулся из рейса!', NEW.auto_id;
    END IF;

RETURN NEW;
END;
$$;

CREATE TRIGGER verify_car_time_in
    BEFORE INSERT ON journal
    FOR EACH ROW
    EXECUTE FUNCTION verify_car_time_in();



CREATE OR REPLACE FUNCTION verify_car_time_out()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN

	IF NEW.time_in IS NOT NULL THEN
        IF NEW.time_in < NEW.time_out THEN
            RAISE EXCEPTION 'Время прибытия (%) не может быть меньше времени отправления (%)!', NEW.time_in, NEW.time_out;
        END IF;

        IF NEW.time_in > CURRENT_TIMESTAMP THEN
            RAISE EXCEPTION 'Время прибытия (%) не может быть в будущем!', NEW.time_in;
        END IF;
    END IF;

    IF NEW.time_out > CURRENT_TIMESTAMP THEN
        RAISE EXCEPTION 'Время отправления (%) не может быть в будущем!', NEW.time_out;
    END IF;

RETURN NEW;
END;
$$;

CREATE TRIGGER verify_car_time_out
    BEFORE UPDATE ON journal
    FOR EACH ROW
    EXECUTE FUNCTION verify_car_time_out();




CREATE OR REPLACE FUNCTION get_active_trips_count_by_route(route_id_param INTEGER)
RETURNS INTEGER AS $$
DECLARE
active_count INTEGER;
BEGIN
SELECT COUNT(*) INTO active_count
FROM journal
WHERE route_id = route_id_param AND time_in IS NULL;

RETURN COALESCE(active_count, 0);
END;
$$ LANGUAGE plpgsql;




CREATE OR REPLACE FUNCTION get_fastest_trip_by_route(route_id_param INTEGER)
RETURNS TABLE(
    auto_id INTEGER,
    auto_num VARCHAR,
    auto_mark VARCHAR,
    route_name VARCHAR,
    trip_duration TEXT,
    time_out TIMESTAMP,
    time_in TIMESTAMP
) AS $$
BEGIN
RETURN QUERY
SELECT
    j.auto_id,
    a.num as auto_num,
    a.mark as auto_mark,
    r.name as route_name,
    (j.time_in - j.time_out)::text as trip_duration,
    j.time_out,
    j.time_in
FROM journal j
         JOIN auto a ON j.auto_id = a.id
         JOIN routes r ON j.route_id = r.id
WHERE j.route_id = route_id_param
  AND j.time_in IS NOT NULL
  AND j.time_out IS NOT NULL
ORDER BY (j.time_in - j.time_out) ASC
LIMIT 1;
END;
$$ LANGUAGE plpgsql;
