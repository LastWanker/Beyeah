-- Phase 1 contract unify migration
-- Target DB: MySQL 8.x
-- Date: 2026-03-07

SET NAMES utf8mb4;

-- 1) user: login_name length extension (11 -> 32)
ALTER TABLE beyeah_mall_user
    MODIFY COLUMN login_name VARCHAR(32) NOT NULL;

-- 2) order_no unique index
SET @idx_order_no := (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'beyeah_mall_order'
      AND index_name = 'uk_beyeah_mall_order_order_no'
);
SET @sql_order_no := IF(
    @idx_order_no = 0,
    'ALTER TABLE beyeah_mall_order ADD UNIQUE INDEX uk_beyeah_mall_order_order_no (order_no)',
    'SELECT 1'
);
PREPARE stmt_order_no FROM @sql_order_no;
EXECUTE stmt_order_no;
DEALLOCATE PREPARE stmt_order_no;

-- 3) shopping cart composite index
SET @idx_cart_user_goods_deleted := (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'beyeah_mall_shopping_cart_item'
      AND index_name = 'idx_cart_user_goods_deleted'
);
SET @sql_cart_user_goods_deleted := IF(
    @idx_cart_user_goods_deleted = 0,
    'CREATE INDEX idx_cart_user_goods_deleted ON beyeah_mall_shopping_cart_item (user_id, goods_id, is_deleted)',
    'SELECT 1'
);
PREPARE stmt_cart_user_goods_deleted FROM @sql_cart_user_goods_deleted;
EXECUTE stmt_cart_user_goods_deleted;
DEALLOCATE PREPARE stmt_cart_user_goods_deleted;

-- 4) order list query index
SET @idx_order_user_time_deleted := (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'beyeah_mall_order'
      AND index_name = 'idx_order_user_time_deleted'
);
SET @sql_order_user_time_deleted := IF(
    @idx_order_user_time_deleted = 0,
    'CREATE INDEX idx_order_user_time_deleted ON beyeah_mall_order (user_id, create_time, is_deleted)',
    'SELECT 1'
);
PREPARE stmt_order_user_time_deleted FROM @sql_order_user_time_deleted;
EXECUTE stmt_order_user_time_deleted;
DEALLOCATE PREPARE stmt_order_user_time_deleted;

-- 5) goods category/status index
SET @idx_goods_category_status := (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'beyeah_mall_goods_info'
      AND index_name = 'idx_goods_category_status'
);
SET @sql_goods_category_status := IF(
    @idx_goods_category_status = 0,
    'CREATE INDEX idx_goods_category_status ON beyeah_mall_goods_info (goods_category_id, goods_sell_status)',
    'SELECT 1'
);
PREPARE stmt_goods_category_status FROM @sql_goods_category_status;
EXECUTE stmt_goods_category_status;
DEALLOCATE PREPARE stmt_goods_category_status;
