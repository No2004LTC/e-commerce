-- liquibase formatted sql

-- changeset ltc:2026031301
-- comment: Create users table with Role as Enum (No Foreign Key)
CREATE TABLE IF NOT EXISTS users (
    -- Đổi BIGINT thành VARCHAR(36) để chứa chuỗi UUID
    id VARCHAR(36) PRIMARY KEY, 
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(500),
    role_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- rollback DROP TABLE users;