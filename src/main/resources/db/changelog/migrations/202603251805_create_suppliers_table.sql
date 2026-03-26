-- liquibase formatted sql

-- changeset ltc:202603251805
-- comment: create_suppliers_table
CREATE TABLE suppliers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,        -- ID số tự tăng
    code VARCHAR(50) NOT NULL UNIQUE,            -- Mã nhà cung cấp (duy nhất)
    name VARCHAR(255) NOT NULL,                  -- Tên nhà cung cấp
    address VARCHAR(500),                        -- Địa chỉ
    phone_number VARCHAR(20),                    -- Số điện thoại
    email VARCHAR(100),                          -- Email
    provided_product VARCHAR(255),               -- Sản phẩm cung cấp
    quantity INT DEFAULT 0,                      -- Số lượng
    unit_price DECIMAL(19, 2) DEFAULT 0.00       -- Giá tiền 1 sản phẩm
);
-- rollback DROP TABLE suppliers;
