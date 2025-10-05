-- Example: add a column to items (run only if needed)
ALTER TABLE items ADD COLUMN IF NOT EXISTS sprites TEXT[];
ALTER TABLE items ADD COLUMN IF NOT EXISTS sprites_public_ids TEXT[];
