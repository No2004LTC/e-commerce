-- liquibase formatted sql

-- changeset ltc:202603232036
-- comment: create_products_table_with_inventory_support
CREATE TABLE IF NOT EXISTS products (
    id VARCHAR(36) PRIMARY KEY,
    owner_id VARCHAR(36) NOT NULL, -- Chủ shop
    category_id VARCHAR(36),       -- Liên kết danh mục
    product_code VARCHAR(100) NOT NULL UNIQUE,
    
    name VARCHAR(255) NOT NULL,
    description TEXT,
    product_image_url VARCHAR(500),
    warehouse VARCHAR(255),
    supplier VARCHAR(255),

    price DECIMAL(15,2) NOT NULL,
    stock_quantity INT DEFAULT 0,
    sold_quantity INT DEFAULT 0,
    
    status VARCHAR(50) DEFAULT 'AVAILABLE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Khóa ngoại và Index
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES categories(id),
    INDEX idx_products_owner (owner_id),
    INDEX idx_products_category (category_id),
    INDEX idx_products_status (status)
);

-- rollback DROP TABLE products;