-- добавление
INSERT INTO users (username, password_hash) VALUES ('username_value', 'hash_value');

-- поиск
-- получить всех пользователей
SELECT id, username, password_hash FROM users;
-- найти пользователя по ID
SELECT id, username, password_hash FROM users WHERE id = 1;

-- обновление
-- обновить имя пользователя
UPDATE users SET username = 'new_username' WHERE id = 1;

-- удаление
DELETE FROM users WHERE id = 1;
