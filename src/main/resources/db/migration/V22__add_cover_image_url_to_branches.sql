ALTER TABLE branches
    ADD COLUMN IF NOT EXISTS cover_image_url VARCHAR(512);
