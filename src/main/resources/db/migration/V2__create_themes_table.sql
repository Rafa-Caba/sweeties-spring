-- 1. Create Table
CREATE TABLE themes (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    is_dark BOOLEAN DEFAULT FALSE,
    primary_color VARCHAR(20),
    accent_color VARCHAR(20),
    background_color VARCHAR(20),
    text_color VARCHAR(20),
    card_color VARCHAR(20),
    button_color VARCHAR(20),
    nav_color VARCHAR(50)
);

-- 2. Add relation to Users
ALTER TABLE users ADD COLUMN theme_id BIGINT;
ALTER TABLE users ADD CONSTRAINT fk_user_theme FOREIGN KEY (theme_id) REFERENCES themes(id);

-- 3. Seed Data (The 3 Themes)
INSERT INTO themes (name, is_dark, primary_color, accent_color, background_color, text_color, card_color, button_color, nav_color)
VALUES ('Clásico', false, '#a88ff7', '#673ab7', '#fefefe', '#2a2a2a', '#ffffff', '#6a0dad', 'rgba(255, 255, 255, 0.85)');

INSERT INTO themes (name, is_dark, primary_color, accent_color, background_color, text_color, card_color, button_color, nav_color)
VALUES ('Noche', true, '#bca2ff', '#c792ea', '#1e1e1e', '#fefefe', '#2a2a2a', '#a774f9', 'rgba(28, 24, 36, 0.85)');

INSERT INTO themes (name, is_dark, primary_color, accent_color, background_color, text_color, card_color, button_color, nav_color)
VALUES ('Dulce', false, '#ff8fab', '#fb6f92', '#fff0f3', '#590d22', '#fff5f8', '#ff4d6d', 'rgba(255, 240, 243, 0.9)');