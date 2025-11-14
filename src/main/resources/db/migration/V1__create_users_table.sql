CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(10) DEFAULT 'GUEST',
    bio TEXT,
    image_url VARCHAR(512),
    image_public_id VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);