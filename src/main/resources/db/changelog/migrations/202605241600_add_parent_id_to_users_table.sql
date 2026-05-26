-- liquibase formatted sql

-- changeset nguyenvulong:2026052401
-- comment: Add parent_id column to users table for multi-tenant hierarchy management
ALTER TABLE users ADD COLUMN parent_id VARCHAR(36) NULL;

-- rollback ALTER TABLE users DROP COLUMN parent_id;