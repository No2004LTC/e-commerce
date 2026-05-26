-- liquibase formatted sql

-- changeset nguyenvulong:2026052501
-- comment: Add fullName, phone, and address columns to users table for profile management
ALTER TABLE users ADD COLUMN full_name VARCHAR(255) NULL;
ALTER TABLE users ADD COLUMN phone VARCHAR(50) NULL;
ALTER TABLE users ADD COLUMN address VARCHAR(500) NULL;

-- rollback ALTER TABLE users DROP COLUMN full_name;
-- rollback ALTER TABLE users DROP COLUMN phone;
-- rollback ALTER TABLE users DROP COLUMN address;
