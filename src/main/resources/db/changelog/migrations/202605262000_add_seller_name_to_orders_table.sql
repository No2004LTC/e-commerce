--liquibase formatted sql

--changeset pos:202605262001
--preconditions onFail:MARK_RAN onError:HALT
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'orders' AND column_name = 'seller_name'
ALTER TABLE orders ADD COLUMN seller_name VARCHAR(255) NULL;

--changeset pos:202605262002
--preconditions onFail:MARK_RAN onError:HALT
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'orders' AND column_name = 'payment_status'
ALTER TABLE orders ADD COLUMN payment_status VARCHAR(50) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING | SUCCESS | FAILED';

