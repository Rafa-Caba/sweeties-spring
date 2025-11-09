INSERT INTO users (username, email, password, role)
VALUES
('rafael', 'rafael@example.com', '$2a$10$Bxh6sTzH1s4f9m1rPrdG6eM1r1IYB7n1F8QO0CjF2U4mQ4QZ4nZz2', 'ADMIN'),
('admin', 'admin@sweeties.com', '$2a$10$Bxh6sTzH1s4f9m1rPrdG6eM1r1IYB7n1F8QO0CjF2U4mQ4QZ4nZz2', 'ADMIN')
ON CONFLICT (email) DO NOTHING;