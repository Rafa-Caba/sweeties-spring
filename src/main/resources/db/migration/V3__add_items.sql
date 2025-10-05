CREATE TABLE IF NOT EXISTS items (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    price NUMERIC(10,2) NOT NULL DEFAULT 0,
    image_url TEXT,
    image_public_id TEXT,
    materials TEXT[],
    is_featured BOOLEAN DEFAULT FALSE,
    is_visible BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Example initial item
INSERT INTO items (name, description, price, is_featured, is_visible)
VALUES ('Muñeco Gato', 'Tejido a mano', 350.00, TRUE, TRUE)
ON CONFLICT DO NOTHING;
