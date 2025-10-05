CREATE TABLE IF NOT EXISTS materials (
  id   BIGSERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL UNIQUE
  -- add other columns your entity needs (e.g., description TEXT, created_at TIMESTAMP, etc.)
);
