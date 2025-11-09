-- 1. Main Admin Settings Table
-- We use a single table with a fixed ID (1) for the singleton pattern.
-- Embedded objects (social, features, etc.) are flattened into columns.
CREATE TABLE admin_settings (
    id BIGINT PRIMARY KEY,

    -- Branding
    site_name VARCHAR(255) NOT NULL DEFAULT 'Sweeties | Crochet Arts',
    site_tagline VARCHAR(255),
    logo_light_url VARCHAR(512),
    logo_light_public_id VARCHAR(255),
    logo_dark_url VARCHAR(512),
    logo_dark_public_id VARCHAR(255),
    favicon_url VARCHAR(512),
    favicon_public_id VARCHAR(255),

    -- Contact
    contact_email VARCHAR(255),
    contact_phone VARCHAR(50),
    contact_whats_app VARCHAR(50),
    contact_address VARCHAR(512),

    -- Social (Embedded)
    social_facebook VARCHAR(255),
    social_instagram VARCHAR(255),
    social_tiktok VARCHAR(255),
    social_youtube VARCHAR(255),
    social_threads VARCHAR(255),
    social_x VARCHAR(255),

    -- UI / Theme
    default_theme_mode VARCHAR(20) NOT NULL DEFAULT 'SYSTEM',
    public_theme_group VARCHAR(100),
    admin_theme_group VARCHAR(100),

    -- Features (Embedded)
    features_enable_orders BOOLEAN NOT NULL DEFAULT TRUE,
    features_enable_gallery BOOLEAN NOT NULL DEFAULT TRUE,
    features_enable_materials BOOLEAN NOT NULL DEFAULT TRUE,
    features_enable_contact_page BOOLEAN NOT NULL DEFAULT TRUE,
    features_enable_cart BOOLEAN NOT NULL DEFAULT TRUE,

    -- SEO (Embedded)
    seo_site_description TEXT,
    seo_og_title VARCHAR(255),
    seo_og_description TEXT,
    seo_og_image_url VARCHAR(512),
    seo_og_image_public_id VARCHAR(255),

    -- Visibility (Embedded)
    visibility_show_email BOOLEAN NOT NULL DEFAULT TRUE,
    visibility_show_phone BOOLEAN NOT NULL DEFAULT FALSE,
    visibility_show_whats_app BOOLEAN NOT NULL DEFAULT TRUE,
    visibility_show_address BOOLEAN NOT NULL DEFAULT FALSE,
    visibility_show_social BOOLEAN NOT NULL DEFAULT TRUE,

    -- Home (Embedded)
    home_hero_title VARCHAR(255) DEFAULT 'Catálogo de muñecos de crochet',
    home_hero_subtitle VARCHAR(512) DEFAULT 'Hecho con amor — pedidos personalizados',
    home_creator_name VARCHAR(255) DEFAULT 'por Sweeties | Crochet Arts',

    -- Gallery (Embedded)
    gallery_items_per_page INT NOT NULL DEFAULT 10,

    -- Footer (Embedded)
    footer_legal_text VARCHAR(512) DEFAULT '© Sweeties | Crochet Arts',

    -- Timestamps
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE
);

-- 2. SEO Meta Keywords Table (ElementCollection)
CREATE TABLE admin_settings_meta_keywords (
    admin_settings_id BIGINT NOT NULL REFERENCES admin_settings(id) ON DELETE CASCADE,
    meta_keyword VARCHAR(255)
);

-- 3. Footer Auxiliary Links Table (ElementCollection of Embeddables)
CREATE TABLE admin_settings_aux_links (
    admin_settings_id BIGINT NOT NULL REFERENCES admin_settings(id) ON DELETE CASCADE,
    label VARCHAR(100),
    url VARCHAR(512)
    -- 'sort_order' column is automatically added by JPA for List ordering
);

-- 4. CRITICAL: Insert the singleton row with ID 1 and all defaults
-- This ensures our service logic 'findById(1L)' always works.
INSERT INTO admin_settings (
    id,
    site_name,
    default_theme_mode,
    features_enable_orders,
    features_enable_gallery,
    features_enable_materials,
    features_enable_contact_page,
    features_enable_cart,
    visibility_show_email,
    visibility_show_phone,
    visibility_show_whats_app,
    visibility_show_address,
    visibility_show_social,
    home_hero_title,
    home_hero_subtitle,
    home_creator_name,
    gallery_items_per_page,
    footer_legal_text,
    created_at,
    updated_at
) VALUES (
    1,
    'Sweeties | Crochet Arts',
    'SYSTEM',
    TRUE,
    TRUE,
    TRUE,
    TRUE,
    TRUE,
    TRUE,
    FALSE,
    TRUE,
    FALSE,
    TRUE,
    'Catálogo de muñecos de crochet',
    'Hecho con amor — pedidos personalizados',
    'por Sweeties | Crochet Arts',
    10,
    '© Sweeties | Crochet Arts',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);