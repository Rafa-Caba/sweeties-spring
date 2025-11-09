CREATE TABLE item_sprite_public_ids (
    item_id BIGINT NOT NULL,
    sprite_public_id VARCHAR(255),
    FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE
);
