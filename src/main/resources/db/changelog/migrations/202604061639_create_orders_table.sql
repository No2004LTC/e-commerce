-- liquibase formatted sql

-- changeset ltc:202604061639
-- comment: oder
    CREATE TABLE IF NOT EXISTS orders (
    id VARCHAR(36) PRIMARY KEY,
    buyer_id VARCHAR(36) NOT NULL,
    seller_id VARCHAR(36) NOT NULL, -- UUID của chủ shop
    total_amount DECIMAL(15,2) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING', -- PENDING, PAID, SHIPPED, DELIVERED, CANCELLED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_orders_buyer (buyer_id),
    INDEX idx_orders_seller (seller_id)
);
-- rollback DROP TABLE oder;
