-- liquibase formatted sql
-- changeset pos:202605262001
-- comment: add seller_name and payment_status to orders
ALTER TABLE orders ADD COLUMN IF NOT EXISTS seller_name    VARCHAR(255) NULL;
ALTER TABLE orders ADD COLUMN IF NOT EXISTS payment_status VARCHAR(50)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING | SUCCESS | FAILED';

