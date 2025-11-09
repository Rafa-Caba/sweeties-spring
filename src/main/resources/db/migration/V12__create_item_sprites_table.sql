CREATE TABLE item_sprites (
    item_id BIGINT NOT NULL,
    sprite_url VARCHAR(512),
    FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE
);
