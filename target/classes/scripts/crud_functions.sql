-- добавление функции
INSERT INTO functions (name, expression, user_id) VALUES ('Function Name', 'x^2 + 2*x + 1', 1);

-- поиск
-- получить все функции
SELECT id, name, expression, user_id FROM functions;
-- получить функцию по ID
SELECT id, name, expression, user_id FROM functions WHERE id = 1;

-- обновление
-- обновить имя функции
UPDATE functions SET name = 'New Name' WHERE id = 1;

-- удаление
DELETE FROM functions WHERE id = 1;
