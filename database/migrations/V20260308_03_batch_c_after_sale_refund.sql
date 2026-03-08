-- Phase 3 Batch C: after-sale + refund
-- Target DB: MySQL 8.x
-- Date: 2026-03-08

SET NAMES utf8mb4;

-- 1) extend order with refund aggregate fields
SET @col_order_refund_status := (
    SELECT COUNT(1)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'beyeah_mall_order'
      AND column_name = 'refund_status'
);
SET @sql_add_order_refund_status := IF(
    @col_order_refund_status = 0,
    'ALTER TABLE beyeah_mall_order ADD COLUMN refund_status TINYINT NOT NULL DEFAULT 0 COMMENT ''refund aggregate status:0 none,1 processing,2 success,-1 rejected'' AFTER order_status',
    'SELECT 1'
);
PREPARE stmt_add_order_refund_status FROM @sql_add_order_refund_status;
EXECUTE stmt_add_order_refund_status;
DEALLOCATE PREPARE stmt_add_order_refund_status;

SET @col_order_refund_time := (
    SELECT COUNT(1)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'beyeah_mall_order'
      AND column_name = 'refund_time'
);
SET @sql_add_order_refund_time := IF(
    @col_order_refund_time = 0,
    'ALTER TABLE beyeah_mall_order ADD COLUMN refund_time DATETIME NULL DEFAULT NULL COMMENT ''latest refund finish time'' AFTER refund_status',
    'SELECT 1'
);
PREPARE stmt_add_order_refund_time FROM @sql_add_order_refund_time;
EXECUTE stmt_add_order_refund_time;
DEALLOCATE PREPARE stmt_add_order_refund_time;

SET @idx_order_refund_status := (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'beyeah_mall_order'
      AND index_name = 'idx_order_refund_status'
);
SET @sql_create_order_refund_status_idx := IF(
    @idx_order_refund_status = 0,
    'CREATE INDEX idx_order_refund_status ON beyeah_mall_order (refund_status, update_time)',
    'SELECT 1'
);
PREPARE stmt_create_order_refund_status_idx FROM @sql_create_order_refund_status_idx;
EXECUTE stmt_create_order_refund_status_idx;
DEALLOCATE PREPARE stmt_create_order_refund_status_idx;

-- 2) after-sale table
SET @tbl_after_sale := (
    SELECT COUNT(1)
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name = 'beyeah_mall_after_sale'
);
SET @sql_create_after_sale := IF(
    @tbl_after_sale = 0,
    'CREATE TABLE beyeah_mall_after_sale (
        after_sale_id BIGINT NOT NULL AUTO_INCREMENT COMMENT ''after-sale id'',
        after_sale_no VARCHAR(32) NOT NULL DEFAULT '''' COMMENT ''after-sale no'',
        user_id BIGINT NOT NULL COMMENT ''user id'',
        order_id BIGINT NOT NULL COMMENT ''order id'',
        order_no VARCHAR(20) NOT NULL DEFAULT '''' COMMENT ''order no snapshot'',
        order_item_id BIGINT NOT NULL COMMENT ''order item id'',
        goods_id BIGINT NOT NULL DEFAULT 0 COMMENT ''goods id snapshot'',
        goods_name VARCHAR(200) NOT NULL DEFAULT '''' COMMENT ''goods name snapshot'',
        refund_amount INT NOT NULL DEFAULT 0 COMMENT ''refund amount'',
        reason VARCHAR(120) NOT NULL DEFAULT '''' COMMENT ''after-sale reason'',
        description VARCHAR(255) NOT NULL DEFAULT '''' COMMENT ''after-sale detail description'',
        reject_reason VARCHAR(120) NOT NULL DEFAULT '''' COMMENT ''reject reason by admin'',
        after_sale_status TINYINT NOT NULL DEFAULT 0 COMMENT ''0 pending,1 approved,2 refunding,3 refunded,-1 rejected'',
        refund_no VARCHAR(32) DEFAULT NULL COMMENT ''refund no once created'',
        handle_time DATETIME NULL DEFAULT NULL COMMENT ''audit handle time'',
        is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT ''soft delete flag'',
        create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT ''create time'',
        update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''update time'',
        PRIMARY KEY (after_sale_id),
        UNIQUE KEY uk_after_sale_no (after_sale_no),
        UNIQUE KEY uk_after_sale_order_item (order_item_id),
        KEY idx_after_sale_user_time (user_id, create_time),
        KEY idx_after_sale_status_time (after_sale_status, update_time),
        KEY idx_after_sale_order (order_id, order_item_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT=''after-sale order''',
    'SELECT 1'
);
PREPARE stmt_create_after_sale FROM @sql_create_after_sale;
EXECUTE stmt_create_after_sale;
DEALLOCATE PREPARE stmt_create_after_sale;

-- 3) refund table
SET @tbl_refund := (
    SELECT COUNT(1)
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name = 'beyeah_mall_refund'
);
SET @sql_create_refund := IF(
    @tbl_refund = 0,
    'CREATE TABLE beyeah_mall_refund (
        refund_id BIGINT NOT NULL AUTO_INCREMENT COMMENT ''refund id'',
        refund_no VARCHAR(32) NOT NULL DEFAULT '''' COMMENT ''refund no'',
        after_sale_id BIGINT NOT NULL COMMENT ''after-sale id'',
        after_sale_no VARCHAR(32) NOT NULL DEFAULT '''' COMMENT ''after-sale no'',
        user_id BIGINT NOT NULL COMMENT ''user id'',
        order_id BIGINT NOT NULL COMMENT ''order id'',
        order_no VARCHAR(20) NOT NULL DEFAULT '''' COMMENT ''order no snapshot'',
        refund_amount INT NOT NULL DEFAULT 0 COMMENT ''refund amount'',
        refund_status TINYINT NOT NULL DEFAULT 0 COMMENT ''0 pending,1 success,-1 failed'',
        refund_channel TINYINT NOT NULL DEFAULT 0 COMMENT ''0 mock,1 alipay,2 wechat'',
        idempotency_key VARCHAR(64) NOT NULL DEFAULT '''' COMMENT ''idempotency key for callback/execute'',
        callback_payload VARCHAR(1024) NOT NULL DEFAULT '''' COMMENT ''callback payload snapshot'',
        fail_reason VARCHAR(120) NOT NULL DEFAULT '''' COMMENT ''failed reason'',
        success_time DATETIME NULL DEFAULT NULL COMMENT ''refund success time'',
        create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT ''create time'',
        update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''update time'',
        PRIMARY KEY (refund_id),
        UNIQUE KEY uk_refund_no (refund_no),
        UNIQUE KEY uk_refund_after_sale (after_sale_id),
        UNIQUE KEY uk_refund_idempotency (idempotency_key),
        KEY idx_refund_order_time (order_id, create_time),
        KEY idx_refund_status_time (refund_status, update_time)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT=''refund record''',
    'SELECT 1'
);
PREPARE stmt_create_refund FROM @sql_create_refund;
EXECUTE stmt_create_refund;
DEALLOCATE PREPARE stmt_create_refund;
