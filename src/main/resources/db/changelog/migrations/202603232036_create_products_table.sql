-- liquibase formatted sql

-- changeset ltc:202603232036
-- comment: create_products_table_with_inventory_support
CREATE TABLE IF NOT EXISTS products (
    id VARCHAR(36) PRIMARY KEY, -- UUID của sản phẩm

    -- THÔNG TIN ĐỊNH DANH KHO
    owner_id VARCHAR(36) NOT NULL, -- UUID của người dùng (chủ shop/chủ kho)
    product_code VARCHAR(100) NOT NULL UNIQUE, -- Mã quản lý nội bộ
    
    -- THÔNG TIN SẢN PHẨM
    name VARCHAR(255) NOT NULL,
    description TEXT,
    product_image_url VARCHAR(500),
    warehouse VARCHAR(255), -- Tên kho (Ví dụ: Kho nhà riêng, Kho Quận 1...)
    supplier VARCHAR(255), -- Nhà cung cấp (nếu có)

    -- TÀI CHÍNH & SỐ LƯỢNG (KHO)
    price DECIMAL(15,2) NOT NULL, -- Giá bán hiện tại
    stock_quantity INT DEFAULT 0, -- Số lượng thực tế còn trong kho
    sold_quantity INT DEFAULT 0, -- Số lượng đã bán thành công
    
    -- TRẠNG THÁI
    status VARCHAR(50) DEFAULT 'AVAILABLE', -- AVAILABLE, OUT_OF_STOCK, HIDDEN

    -- THỜI GIAN
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- INDEX ĐỂ TÌM KIẾM NHANH THEO CHỦ KHO
    INDEX idx_products_owner (owner_id),
    INDEX idx_products_status (status)
);

-- rollback DROP TABLE products;