CREATE TABLE item_materials (
    item_id BIGINT NOT NULL,
    material VARCHAR(255) NOT NULL,
    FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE
);