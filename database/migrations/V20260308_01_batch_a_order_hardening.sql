-- Phase 3 Batch A: anti-oversell + idempotency + auto-close support
-- Target DB: MySQL 8.x
-- Date: 2026-03-08

SET NAMES utf8mb4;

-- 1) idempotency key for order submit deduplication
SET @col_idempotency_key := (
    SELECT COUNT(1)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'beyeah_mall_order'
      AND column_name = 'idempotency_key'
);
SET @sql_add_idempotency_key := IF(
    @col_idempotency_key = 0,
    'ALTER TABLE beyeah_mall_order ADD COLUMN idempotency_key VARCHAR(64) NULL DEFAULT NULL COMMENT ''request idempotency key'' AFTER order_no',
    'SELECT 1'
);
PREPARE stmt_add_idempotency_key FROM @sql_add_idempotency_key;
EXECUTE stmt_add_idempotency_key;
DEALLOCATE PREPARE stmt_add_idempotency_key;

-- 2) unique key: one order per (user, idempotency_key)
SET @idx_user_idempotency := (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'beyeah_mall_order'
      AND index_name = 'uk_order_user_idempotency'
);
SET @sql_user_idempotency := IF(
    @idx_user_idempotency = 0,
    'ALTER TABLE beyeah_mall_order ADD UNIQUE INDEX uk_order_user_idempotency (user_id, idempotency_key)',
    'SELECT 1'
);
PREPARE stmt_user_idempotency FROM @sql_user_idempotency;
EXECUTE stmt_user_idempotency;
DEALLOCATE PREPARE stmt_user_idempotency;

-- 3) query index for auto-close scan
SET @idx_order_unpaid_timeout := (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'beyeah_mall_order'
      AND index_name = 'idx_order_unpaid_timeout'
);
SET @sql_order_unpaid_timeout := IF(
    @idx_order_unpaid_timeout = 0,
    'CREATE INDEX idx_order_unpaid_timeout ON beyeah_mall_order (order_status, pay_status, create_time)',
    'SELECT 1'
);
PREPARE stmt_order_unpaid_timeout FROM @sql_order_unpaid_timeout;
EXECUTE stmt_order_unpaid_timeout;
DEALLOCATE PREPARE stmt_order_unpaid_timeout;
