--liquibase formatted sql
--changeset dev:202606012200_add_metadata_to_chat_messages
--preconditions onFail:MARK_RAN onError:HALT
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'chat_messages' AND column_name = 'sender_name'

ALTER TABLE chat_messages
    ADD COLUMN sender_name    VARCHAR(255) NULL COMMENT 'Tên hiển thị của người gửi',
    ADD COLUMN sender_role    VARCHAR(100) NULL COMMENT 'Role của người gửi: ROLE_ADMIN, ROLE_BRANCH, v.v.',
    ADD COLUMN branch_label   VARCHAR(255) NULL COMMENT 'Tên chi nhánh hoặc nhãn nhận dạng';
