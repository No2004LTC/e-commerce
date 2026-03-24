-- liquibase formatted sql

-- changeset ltc:202603232036
-- comment: create_products_table
CREATE TABLE IF NOT EXISTS products (
    id VARCHAR(36) PRIMARY KEY, -- UUID

    product_code VARCHAR(100) NOT NULL UNIQUE, -- mã sản phẩm
    name VARCHAR(255) NOT NULL, -- tên sản phẩm
    description TEXT, -- mô tả
    product_image_url VARCHAR(500),
    warehouse VARCHAR(255), -- kho chứa
    supplier VARCHAR(255), -- nhà cung cấp

    price DECIMAL(15,2) NOT NULL, -- giá tiền

    stock_quantity INT DEFAULT 0, -- tồn kho
    sold_quantity INT DEFAULT 0, -- số lượng đã bán

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
-- rollback DROP TABLE products;
