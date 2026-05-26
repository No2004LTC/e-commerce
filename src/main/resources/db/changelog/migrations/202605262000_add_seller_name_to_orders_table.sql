-- liquibase formatted sql
-- changeset author:202605262000
-- comment: add seller_name to orders
ALTER TABLE orders ADD COLUMN seller_name VARCHAR(100);
