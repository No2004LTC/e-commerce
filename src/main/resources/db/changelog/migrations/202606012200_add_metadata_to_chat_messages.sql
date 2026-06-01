--liquibase formatted sql
--changeset dev:202606012200_add_metadata_to_chat_messages

ALTER TABLE chat_messages
    ADD COLUMN IF NOT EXISTS sender_name    VARCHAR(255) NULL COMMENT 'Tên hiển thị của người gửi',
    ADD COLUMN IF NOT EXISTS sender_role    VARCHAR(100) NULL COMMENT 'Role của người gửi: ROLE_ADMIN, ROLE_BRANCH, v.v.',
    ADD COLUMN IF NOT EXISTS branch_label   VARCHAR(255) NULL COMMENT 'Tên chi nhánh hoặc nhãn nhận dạng';
