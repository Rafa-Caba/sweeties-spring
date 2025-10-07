-- 1) add as nullable
ALTER TABLE users ADD COLUMN IF NOT EXISTS name VARCHAR(255);

-- 2) backfill existing rows (pick something sensible for your domain)
UPDATE users SET name = COALESCE(name, username);

-- 3) enforce NOT NULL
ALTER TABLE users ALTER COLUMN name SET NOT NULL;