--liquibase formatted sql

--changeset pos:202605272101
--preconditions onFail:MARK_RAN onError:HALT
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'orders' AND column_name = 'payment_method'
ALTER TABLE orders ADD COLUMN payment_method VARCHAR(50) NOT NULL DEFAULT 'CHUYỂN KHOẢN' COMMENT 'TIỀN MẶT | CHUYỂN KHOẢN | MOMO | VNPAY | VIETQR';

