-- ============================================================
-- MIGRATION: Thêm cột branch_name và seller_name vào bảng orders
-- Chạy script này trong phpMyAdmin hoặc MySQL Workbench
-- Database: ecommerce
-- ============================================================

USE ecommerce;

-- 1. Thêm cột branch_name (nhãn nhóm biểu đồ tròn doanh thu)
ALTER TABLE orders
  ADD COLUMN IF NOT EXISTS branch_name VARCHAR(255) NULL
  COMMENT 'Tên hiển thị chi nhánh lập đơn — dùng GROUP BY biểu đồ tròn';

-- 2. Thêm cột seller_name (nếu chưa có — tên hiển thị người bán)
ALTER TABLE orders
  ADD COLUMN IF NOT EXISTS seller_name VARCHAR(255) NULL
  COMMENT 'Tên hiển thị người bán / chi nhánh';

-- 3. Backfill branch_name từ seller_name (cho đơn hàng cũ chưa có branch_name)
UPDATE orders
SET branch_name = seller_name
WHERE branch_name IS NULL AND seller_name IS NOT NULL;

-- 4. Kiểm tra kết quả
SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'ecommerce'
  AND TABLE_NAME   = 'orders'
ORDER BY ORDINAL_POSITION;
