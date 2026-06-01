--liquibase formatted sql
-- ═══════════════════════════════════════════════════════════════════
-- MASTER SCHEMA — POS MarketHub
-- Replaces all 17 individual migration files with a single,
-- clean, fully-indexed baseline schema.
--
-- Run order: This file is self-contained. All tables created in
-- dependency order (no FK violations). Safe to run on a fresh DB.
-- ═══════════════════════════════════════════════════════════════════

-- ───────────────────────────────────────────────────────────────────
-- 1. ROLES
-- ───────────────────────────────────────────────────────────────────
--changeset pos:master-01-roles
CREATE TABLE IF NOT EXISTS roles (
    id   BIGINT       AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE COMMENT 'e.g. ROLE_ADMIN, ROLE_BRANCH, ROLE_STAFF'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Danh sách vai trò người dùng';

-- rollback DROP TABLE IF EXISTS roles;

-- ───────────────────────────────────────────────────────────────────
-- 2. USERS  (supports multi-tenant parent → child branch hierarchy)
-- ───────────────────────────────────────────────────────────────────
--changeset pos:master-02-users
CREATE TABLE IF NOT EXISTS users (
    id         VARCHAR(36)  NOT NULL PRIMARY KEY       COMMENT 'UUID',
    username   VARCHAR(100) NOT NULL UNIQUE,
    email      VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,

    -- Profile
    full_name  VARCHAR(255) NULL,
    phone      VARCHAR(20)  NULL,
    address    VARCHAR(500) NULL,
    avatar_url VARCHAR(500) NULL,

    -- Branch hierarchy: NULL = root shop owner, non-null = sub-branch
    parent_id  VARCHAR(36)  NULL                       COMMENT 'UUID của chi nhánh cha; NULL nếu là cửa hàng chính',

    -- Role FK (soft — no CASCADE so role deletion is safe)
    role_id    BIGINT       NULL,

    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_users_role   FOREIGN KEY (role_id)   REFERENCES roles(id) ON DELETE SET NULL,
    CONSTRAINT fk_users_parent FOREIGN KEY (parent_id) REFERENCES users(id) ON DELETE SET NULL,

    INDEX idx_users_role      (role_id),
    INDEX idx_users_parent    (parent_id),
    INDEX idx_users_email     (email),
    INDEX idx_users_username  (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Tài khoản người dùng — admin, chi nhánh chính, chi nhánh phụ';

-- rollback DROP TABLE IF EXISTS users;

-- ───────────────────────────────────────────────────────────────────
-- 3. CATEGORIES  (self-referencing tree)
-- ───────────────────────────────────────────────────────────────────
--changeset pos:master-03-categories
CREATE TABLE IF NOT EXISTS categories (
    id          VARCHAR(36)  NOT NULL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    slug        VARCHAR(255) NOT NULL UNIQUE,
    description TEXT         NULL,
    parent_id   VARCHAR(36)  NULL COMMENT 'Danh mục cha (đệ quy)',

    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_category_parent FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL,
    INDEX idx_categories_parent (parent_id),
    INDEX idx_categories_slug   (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Nhóm / Danh mục sản phẩm phân cấp';

-- rollback DROP TABLE IF EXISTS categories;

-- ───────────────────────────────────────────────────────────────────
-- 4. PRODUCTS
-- ───────────────────────────────────────────────────────────────────
--changeset pos:master-04-products
CREATE TABLE IF NOT EXISTS products (
    id                VARCHAR(36)    NOT NULL PRIMARY KEY,
    owner_id          VARCHAR(36)    NOT NULL                COMMENT 'UUID của chi nhánh sở hữu',
    category_id       VARCHAR(36)    NULL,

    product_code      VARCHAR(100)   NOT NULL UNIQUE,
    name              VARCHAR(255)   NOT NULL,
    description       TEXT           NULL,
    product_image_url VARCHAR(500)   NULL,
    warehouse         VARCHAR(255)   NULL,
    supplier          VARCHAR(255)   NULL,

    price             DECIMAL(15,2)  NOT NULL,
    stock_quantity    INT            NOT NULL DEFAULT 0,
    sold_quantity     INT            NOT NULL DEFAULT 0,

    status            VARCHAR(50)    NOT NULL DEFAULT 'AVAILABLE'
                                     COMMENT 'AVAILABLE | OUT_OF_STOCK | DISCONTINUED',

    created_at        TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_product_owner    FOREIGN KEY (owner_id)    REFERENCES users(id)       ON DELETE CASCADE,
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES categories(id)  ON DELETE SET NULL,

    INDEX idx_products_owner    (owner_id),
    INDEX idx_products_category (category_id),
    INDEX idx_products_status   (status),
    INDEX idx_products_code     (product_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Hàng hóa của từng chi nhánh';

-- rollback DROP TABLE IF EXISTS products;

-- ───────────────────────────────────────────────────────────────────
-- 5. SUPPLIERS
-- ───────────────────────────────────────────────────────────────────
--changeset pos:master-05-suppliers
CREATE TABLE IF NOT EXISTS suppliers (
    id           VARCHAR(36)   NOT NULL PRIMARY KEY,
    name         VARCHAR(255)  NOT NULL,
    contact_name VARCHAR(255)  NULL,
    phone        VARCHAR(20)   NULL,
    email        VARCHAR(255)  NULL,
    address      TEXT          NULL,

    created_at   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_suppliers_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Danh mục nhà cung cấp';

-- rollback DROP TABLE IF EXISTS suppliers;

-- ───────────────────────────────────────────────────────────────────
-- 6. CUSTOMERS  (CRM — quản lý khách hàng tại quầy POS)
-- ───────────────────────────────────────────────────────────────────
--changeset pos:master-06-customers
CREATE TABLE IF NOT EXISTS customers (
    id            VARCHAR(50)   NOT NULL PRIMARY KEY,
    phone         VARCHAR(15)   NOT NULL UNIQUE,
    full_name     VARCHAR(100)  NOT NULL,
    customer_type VARCHAR(30)   NOT NULL DEFAULT 'NEW'
                                COMMENT 'NEW | REGULAR | VIP',
    total_spent   DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    notes         TEXT          NULL,

    -- Which branch "owns" this customer record
    branch_id     VARCHAR(50)   NULL      COMMENT 'Chi nhánh quản lý khách hàng này',

    INDEX idx_customers_phone     (phone),
    INDEX idx_customers_branch    (branch_id),
    INDEX idx_customers_type      (customer_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='CRM — khách hàng tại quầy POS';

-- rollback DROP TABLE IF EXISTS customers;

-- ───────────────────────────────────────────────────────────────────
-- 7. ORDERS
-- ───────────────────────────────────────────────────────────────────
--changeset pos:master-07-orders
CREATE TABLE IF NOT EXISTS orders (
    id             VARCHAR(36)   NOT NULL PRIMARY KEY,
    buyer_id       VARCHAR(36)   NOT NULL           COMMENT 'Email hoặc UUID của người mua',
    seller_id      VARCHAR(36)   NOT NULL           COMMENT 'UUID của chi nhánh bán',
    seller_name    VARCHAR(255)  NULL               COMMENT 'Tên hiển thị chi nhánh bán (denorm)',
    customer_id    VARCHAR(50)   NULL               COMMENT 'FK đến customers (CRM)',

    total_amount   DECIMAL(15,2) NOT NULL DEFAULT 0,
    payment_method VARCHAR(50)   NOT NULL DEFAULT 'CHUYỂN KHOẢN'
                                 COMMENT 'TIỀN MẶT | CHUYỂN KHOẢN | MOMO | VNPAY | VIETQR',
    payment_status VARCHAR(50)   NOT NULL DEFAULT 'PENDING'
                                 COMMENT 'PENDING | SUCCESS | FAILED',
    status         VARCHAR(50)   NOT NULL DEFAULT 'PENDING'
                                 COMMENT 'PENDING | PAID | SHIPPED | DELIVERED | CANCELLED',

    created_at     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE SET NULL,

    INDEX idx_orders_buyer      (buyer_id),
    INDEX idx_orders_seller     (seller_id),
    INDEX idx_orders_status     (status),
    INDEX idx_orders_payment    (payment_status),
    INDEX idx_orders_created    (created_at),
    INDEX idx_orders_customer   (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Đơn hàng POS';

-- rollback DROP TABLE IF EXISTS orders;

-- ───────────────────────────────────────────────────────────────────
-- 8. ORDER ITEMS
-- ───────────────────────────────────────────────────────────────────
--changeset pos:master-08-order-items
CREATE TABLE IF NOT EXISTS order_items (
    id                  BIGINT        AUTO_INCREMENT PRIMARY KEY,
    order_id            VARCHAR(36)   NOT NULL,
    product_id          VARCHAR(36)   NOT NULL,
    product_name        VARCHAR(255)  NOT NULL  COMMENT 'Snapshot tên sản phẩm lúc mua',
    price_at_purchase   DECIMAL(15,2) NOT NULL  COMMENT 'Snapshot giá lúc mua',
    quantity            INT           NOT NULL  DEFAULT 1,

    CONSTRAINT fk_order_items_order   FOREIGN KEY (order_id)   REFERENCES orders(id)   ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT,

    INDEX idx_order_items_order   (order_id),
    INDEX idx_order_items_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Chi tiết dòng sản phẩm trong đơn hàng';

-- rollback DROP TABLE IF EXISTS order_items;

-- ───────────────────────────────────────────────────────────────────
-- 9. CHAT MESSAGES  (WebSocket chat history)
-- ───────────────────────────────────────────────────────────────────
--changeset pos:master-09-chat-messages
CREATE TABLE IF NOT EXISTS chat_messages (
    id           BIGINT       AUTO_INCREMENT PRIMARY KEY,
    sender_id    VARCHAR(255) NOT NULL  COMMENT 'Email / UUID người gửi hoặc "CHATBOT"/"ADMIN"',
    recipient_id VARCHAR(255) NOT NULL  COMMENT 'Email / UUID người nhận',
    content      TEXT         NOT NULL,
    timestamp    DATETIME     NOT NULL  DEFAULT CURRENT_TIMESTAMP,

    -- Rich identity metadata (enriched at save-time)
    sender_name  VARCHAR(255) NULL      COMMENT 'Tên hiển thị người gửi (fullName / username)',
    sender_role  VARCHAR(100) NULL      COMMENT 'ROLE_ADMIN | ROLE_BRANCH | ROLE_STAFF | CHATBOT',
    branch_label VARCHAR(255) NULL      COMMENT 'Nhãn chi nhánh, VD: "minh (Chi nhánh chính)" hoặc "minh / long"',

    INDEX idx_chat_sender     (sender_id),
    INDEX idx_chat_recipient  (recipient_id),
    INDEX idx_chat_timestamp  (timestamp),
    -- Composite for fast history queries: sender↔recipient conversation lookup
    INDEX idx_chat_convo      (sender_id, recipient_id, timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Lịch sử chat WebSocket — admin ↔ chi nhánh và AI chatbot';

-- rollback DROP TABLE IF EXISTS chat_messages;

-- ───────────────────────────────────────────────────────────────────
-- 10. PRODUCT REVIEWS
-- ───────────────────────────────────────────────────────────────────
--changeset pos:master-10-product-reviews
CREATE TABLE IF NOT EXISTS product_reviews (
    id         BIGINT       AUTO_INCREMENT PRIMARY KEY,
    product_id VARCHAR(36)  NOT NULL,
    user_id    VARCHAR(36)  NOT NULL,
    rating     TINYINT      NOT NULL DEFAULT 5  COMMENT '1–5',
    comment    TEXT         NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_reviews_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_user    FOREIGN KEY (user_id)    REFERENCES users(id)    ON DELETE CASCADE,

    INDEX idx_reviews_product (product_id),
    INDEX idx_reviews_user    (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Đánh giá sản phẩm';

-- rollback DROP TABLE IF EXISTS product_reviews;

-- ───────────────────────────────────────────────────────────────────
-- 11. WISHLISTS
-- ───────────────────────────────────────────────────────────────────
--changeset pos:master-11-wishlists
CREATE TABLE IF NOT EXISTS wishlists (
    id         BIGINT      AUTO_INCREMENT PRIMARY KEY,
    user_id    VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    added_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_wishlist_user    FOREIGN KEY (user_id)    REFERENCES users(id)    ON DELETE CASCADE,
    CONSTRAINT fk_wishlist_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    UNIQUE KEY uq_wishlist (user_id, product_id),

    INDEX idx_wishlists_user    (user_id),
    INDEX idx_wishlists_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Danh sách yêu thích của người dùng';

-- rollback DROP TABLE IF EXISTS wishlists;
