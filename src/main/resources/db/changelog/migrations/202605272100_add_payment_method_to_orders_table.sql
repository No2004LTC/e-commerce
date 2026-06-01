-- liquibase formatted sql
-- changeset pos:202605272101
-- comment: add payment_method to orders
ALTER TABLE orders ADD COLUMN IF NOT EXISTS payment_method VARCHAR(50) NOT NULL DEFAULT 'CHUYỂN KHOẢN' COMMENT 'TIỀN MẶT | CHUYỂN KHOẢN | MOMO | VNPAY | VIETQR';

