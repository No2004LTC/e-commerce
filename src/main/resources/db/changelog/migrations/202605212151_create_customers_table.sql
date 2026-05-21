-- liquibase formatted sql

-- changeset ltc:202605212151
-- comment: tao bang customers va lien ket vao orders

-- 1. Tao bang quan ly khach hang CRM
CREATE TABLE IF NOT EXISTS customers (
    id VARCHAR(50) PRIMARY KEY,
    phone VARCHAR(15) UNIQUE NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    customer_type VARCHAR(30) DEFAULT 'NEW',
    total_spent DECIMAL(15,2) DEFAULT 0.00,
    notes TEXT
);

-- 2. Them cot customer_id va tao khoa ngoai cho ban orders
ALTER TABLE orders ADD COLUMN customer_id VARCHAR(50);
ALTER TABLE orders ADD CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE SET NULL;

-- rollback ALTER TABLE orders DROP FOREIGN KEY fk_orders_customer;
-- rollback ALTER TABLE orders DROP COLUMN customer_id;
-- rollback DROP TABLE customers;