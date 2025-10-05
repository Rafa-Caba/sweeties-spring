-- Users
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(200) NOT NULL,
    role VARCHAR(30) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Optionally: Admin user
INSERT INTO users (username, email, password, role)
VALUES ('admin', 'admin@sweeties.com', '$2a$12$DummyPasswordHashReplaceMe', 'ADMIN')
ON CONFLICT (email) DO NOTHING;