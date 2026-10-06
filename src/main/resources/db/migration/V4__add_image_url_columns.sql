ALTER TABLE customers ADD COLUMN IF NOT EXISTS profile_image_url VARCHAR(500);
ALTER TABLE employees ADD COLUMN IF NOT EXISTS profile_image_url VARCHAR(500);
ALTER TABLE owners ADD COLUMN IF NOT EXISTS profile_image_url VARCHAR(500);
ALTER TABLE workshops ADD COLUMN IF NOT EXISTS logo_url VARCHAR(500);
ALTER TABLE branches ADD COLUMN IF NOT EXISTS photo_url VARCHAR(500);
ALTER TABLE products ADD COLUMN IF NOT EXISTS image_url VARCHAR(500);
ALTER TABLE vehicles ADD COLUMN IF NOT EXISTS photo_url VARCHAR(500);

CREATE TABLE IF NOT EXISTS work_order_entry_inspection_images (
    work_order_id UUID NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    FOREIGN KEY (work_order_id) REFERENCES work_orders(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS work_order_task_evidence_images (
    work_order_task_id UUID NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    FOREIGN KEY (work_order_task_id) REFERENCES work_order_tasks(id) ON DELETE CASCADE
);
