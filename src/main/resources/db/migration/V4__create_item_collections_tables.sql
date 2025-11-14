-- For Item.materials
CREATE TABLE item_materials (
    item_id BIGINT NOT NULL,
    material VARCHAR(255) NOT NULL,
    FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE
);

-- For Item.size
CREATE TABLE item_sizes (
    item_id BIGINT NOT NULL,
    alto DOUBLE PRECISION,
    ancho DOUBLE PRECISION,
    FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE
);

-- For Item.sprites
CREATE TABLE item_sprites (
    item_id BIGINT NOT NULL,
    sprite_url VARCHAR(512),
    FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE
);

-- For Item.spritesPublicIds
CREATE TABLE item_sprite_public_ids (
    item_id BIGINT NOT NULL,
    sprite_public_id VARCHAR(255),
    FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE
);