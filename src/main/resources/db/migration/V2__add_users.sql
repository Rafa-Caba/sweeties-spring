INSERT INTO users (username, email, password, role)
VALUES
('rafael', 'rafael@example.com', '$2a$12$FakeHashHere', 'USER'),
('admin', 'admin@sweeties.com', '$2a$12$FakeAdminHash', 'ADMIN')
ON CONFLICT (email) DO NOTHING;