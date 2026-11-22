ALTER TABLE owners
    ADD COLUMN IF NOT EXISTS profile_image_url VARCHAR(500);
