-- добавление
-- добавить одну точку
INSERT INTO points (function_id, x_value, y_value) VALUES (1, 0.0, 1.0);

-- поиск
-- получить все точки функции
SELECT x_value, y_value FROM points WHERE function_id = 1;
-- получить конкретную точку
SELECT x_value, y_value FROM points WHERE function_id = 1 AND x_value = 0.0;

-- обновление
-- обновить значение Y для конкретной точки
UPDATE points SET y_value = 2.5 WHERE function_id = 1 AND x_value = 0.0;

-- удаление
-- удалить конкретную точку
DELETE FROM points WHERE function_id = 1 AND x_value = 0.0;
