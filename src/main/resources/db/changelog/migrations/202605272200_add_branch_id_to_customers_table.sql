-- liquibase formatted sql
-- changeset author:202605272200
-- comment: add branch_id to customers
ALTER TABLE customers ADD COLUMN branch_id VARCHAR(50);
