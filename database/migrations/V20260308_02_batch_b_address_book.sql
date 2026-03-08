-- Phase 3 Batch B: address book
-- Target DB: MySQL 8.x
-- Date: 2026-03-08

SET NAMES utf8mb4;

SET @tbl_user_address := (
    SELECT COUNT(1)
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name = 'beyeah_mall_user_address'
);
SET @sql_create_user_address := IF(
    @tbl_user_address = 0,
    'CREATE TABLE beyeah_mall_user_address (
        address_id BIGINT NOT NULL AUTO_INCREMENT COMMENT ''address id'',
        user_id BIGINT NOT NULL COMMENT ''user id'',
        consignee VARCHAR(50) NOT NULL DEFAULT '''' COMMENT ''receiver name'',
        phone VARCHAR(20) NOT NULL DEFAULT '''' COMMENT ''receiver phone'',
        province VARCHAR(32) NOT NULL DEFAULT '''' COMMENT ''province'',
        city VARCHAR(32) NOT NULL DEFAULT '''' COMMENT ''city'',
        district VARCHAR(32) NOT NULL DEFAULT '''' COMMENT ''district'',
        detail VARCHAR(120) NOT NULL DEFAULT '''' COMMENT ''detail address'',
        is_default TINYINT NOT NULL DEFAULT 0 COMMENT ''is default: 0 no, 1 yes'',
        is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT ''is deleted: 0 no, 1 yes'',
        create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT ''create time'',
        update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''update time'',
        PRIMARY KEY (address_id),
        KEY idx_user_deleted_default (user_id, is_deleted, is_default),
        KEY idx_user_deleted_time (user_id, is_deleted, update_time)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT=''user address book''',
    'SELECT 1'
);
PREPARE stmt_create_user_address FROM @sql_create_user_address;
EXECUTE stmt_create_user_address;
DEALLOCATE PREPARE stmt_create_user_address;
