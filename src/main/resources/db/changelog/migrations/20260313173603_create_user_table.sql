-- liquibase formatted sql

-- changeset ltc:2026031301
-- comment: Create users table with Role as Enum (No Foreign Key)
CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,         -- @Id
                       username VARCHAR(255) NOT NULL UNIQUE,        -- @Column(unique = true, nullable = false)
                       email VARCHAR(255) NOT NULL UNIQUE,           -- @Column(unique = true, nullable = false)
                       password VARCHAR(255) NOT NULL,               -- @Column(nullable = false)
                       role VARCHAR(50) NOT NULL                     -- Enum Role được lưu dưới dạng String
);

-- rollback DROP TABLE users;