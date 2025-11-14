ALTER TABLE admin_settings
ADD COLUMN about_bio TEXT,
ADD COLUMN about_image_url VARCHAR(512),
ADD COLUMN about_image_public_id VARCHAR(255);