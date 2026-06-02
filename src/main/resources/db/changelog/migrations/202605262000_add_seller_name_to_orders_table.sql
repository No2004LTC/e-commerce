--liquibase formatted sql

--changeset pos:202605262001
--preconditions onFail:MARK_RAN onError:HALT
--precondition-not column-exists tableName:orders columnName:seller_name
ALTER TABLE orders ADD COLUMN seller_name VARCHAR(255) NULL;

--changeset pos:202605262002
--preconditions onFail:MARK_RAN onError:HALT
--precondition-not column-exists tableName:orders columnName:payment_status
ALTER TABLE orders ADD COLUMN payment_status VARCHAR(50) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING | SUCCESS | FAILED';
