-- liquibase formatted sql

-- changeset ltc:202605101510
-- comment: create_product_reviews_table
CREATE TABLE IF NOT EXISTS product_reviews (
    id VARCHAR(36) PRIMARY KEY,
    product_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL, -- UUID của người mua
    
    rating TINYINT NOT NULL, -- Điểm từ 1 đến 5 sao
    comment TEXT,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Khóa ngoại liên kết với sản phẩm
    CONSTRAINT fk_review_product FOREIGN KEY (product_id) REFERENCES products(id),
    INDEX idx_reviews_product (product_id),
    INDEX idx_reviews_user (user_id)
);
-- rollback DROP TABLE users;
