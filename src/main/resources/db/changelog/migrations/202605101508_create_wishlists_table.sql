-- liquibase formatted sql

-- changeset ltc:202605101508
-- comment: create_wishlists_table
CREATE TABLE IF NOT EXISTS wishlists (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Đảm bảo một người dùng không lưu trùng 1 sản phẩm 2 lần
    UNIQUE KEY uk_user_product_wishlist (user_id, product_id),
    CONSTRAINT fk_wishlist_product FOREIGN KEY (product_id) REFERENCES products(id),
    INDEX idx_wishlist_user (user_id)
);
-- rollback DROP TABLE users;
