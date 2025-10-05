-- Many-to-many join table for Items ↔ Materials
CREATE TABLE IF NOT EXISTS item_materials (
  item_id      BIGINT NOT NULL,
  material_id  BIGINT NOT NULL,
  PRIMARY KEY (item_id, material_id),
  CONSTRAINT fk_item_materials_item
    FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE,
  CONSTRAINT fk_item_materials_material
    FOREIGN KEY (material_id) REFERENCES materials(id) ON DELETE CASCADE
);
