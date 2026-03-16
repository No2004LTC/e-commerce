-- liquibase formatted sql

-- changeset ltc:202603161714
-- comment: create_role_table
CREATE TABLE roles (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       name VARCHAR(255) NOT NULL UNIQUE
);

-- rollback DROP TABLE users;
