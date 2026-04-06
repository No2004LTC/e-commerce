-- liquibase formatted sql

-- changeset ltc:202604061639
-- comment: oder_item
    CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    price_at_purchase DECIMAL(15,2) NOT NULL, -- Lưu giá lúc mua để tránh shop đổi giá sau này
    quantity INT NOT NULL,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);
-- rollback DROP TABLE oder_item;
