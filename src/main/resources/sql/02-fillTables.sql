INSERT INTO mythology (name) VALUES
('Греческая'),
('Норвежская'),
('Египетская');

INSERT INTO category (name, hazard, rarity) VALUES
('Зелье', 'Безопасно', 'Common'),
('Оружие', 'Опасно', 'Rare'),
('Артефакт', 'С осторожностью', 'Uncommon');

INSERT INTO city (name, delivery_time) VALUES
('Воронеж', 23),
('Казань', 30),
('Челябинск', 23);

INSERT INTO step (name, description) VALUES
('Сформирован', 'Клиент сформировал заказ'),
('Оплачен', 'Клиент оплатил заказ'),
('Отправлен', 'Горгулья несет заказ'),
('Доставлен', 'Заказ доставлен');

INSERT INTO customer (city_id, name, email, password_hash) VALUES
((SELECT id FROM city WHERE name = 'Воронеж'), 'Test Oleg', 'OLEG@gmail.com', '$2a$10$zoYqOM8e44PYtJSdbYg7DeULcbmqEnDvIr448O1gLF5YQJ6DIkK3q');

INSERT INTO buy_step (step_id, date_start, date_end) VALUES
((SELECT id FROM step WHERE name = 'Сформирован'), '2023-10-26', '2023-10-26'),
((SELECT id FROM step WHERE name = 'Оплачен'), '2023-10-26', '2023-10-26'),
((SELECT id FROM step WHERE name = 'Отправлен'), '2023-10-27', '2024-01-02');

INSERT INTO product (category_id, mythology_id, name, price, description, pic) VALUES
((SELECT id FROM category WHERE name = 'Зелье'), (SELECT id FROM mythology WHERE name = 'Греческая'), 'Лечебное зелье', 10.00, 'Залечивает небольшие раны', 'https://i.pinimg.com/1200x/d9/f5/2c/d9f52c759fc1ae8945620f6ad04cff4f.jpg'),
((SELECT id FROM category WHERE name = 'Оружие'), (SELECT id FROM mythology WHERE name = 'Норвежская'), 'Молот Тора', 1000.00, 'Легендарный молот', 'https://i.pinimg.com/736x/e0/30/ad/e030ad2d7b6760baf4cec49e52344d7c.jpg'),
((SELECT id FROM category WHERE name = 'Артефакт'), (SELECT id FROM mythology WHERE name = 'Египетская'), 'Амулет Анубиса', 500.00, 'Защищает от зла', 'https://i.pinimg.com/736x/74/0c/76/740c76e37e4234de064863b414520c34.jpg');
