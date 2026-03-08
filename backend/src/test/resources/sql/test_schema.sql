SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `beyeah_mall_order_item`;
DROP TABLE IF EXISTS `beyeah_mall_refund`;
DROP TABLE IF EXISTS `beyeah_mall_after_sale`;
DROP TABLE IF EXISTS `beyeah_admin_operation_log`;
DROP TABLE IF EXISTS `beyeah_admin_role_permission`;
DROP TABLE IF EXISTS `beyeah_admin_user_role`;
DROP TABLE IF EXISTS `beyeah_admin_permission`;
DROP TABLE IF EXISTS `beyeah_admin_role`;
DROP TABLE IF EXISTS `beyeah_mall_order`;
DROP TABLE IF EXISTS `beyeah_mall_admin_user`;
DROP TABLE IF EXISTS `beyeah_mall_shopping_cart_item`;
DROP TABLE IF EXISTS `beyeah_mall_goods_info`;
DROP TABLE IF EXISTS `beyeah_mall_user_address`;
DROP TABLE IF EXISTS `beyeah_mall_user`;

CREATE TABLE `beyeah_mall_user` (
  `user_id` bigint NOT NULL AUTO_INCREMENT,
  `nick_name` varchar(50) NOT NULL DEFAULT '',
  `login_name` varchar(32) NOT NULL DEFAULT '',
  `password_md5` varchar(32) NOT NULL DEFAULT '',
  `introduce_sign` varchar(100) NOT NULL DEFAULT '',
  `address` varchar(100) NOT NULL DEFAULT '',
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  `locked_flag` tinyint NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `beyeah_mall_admin_user` (
  `admin_user_id` int NOT NULL AUTO_INCREMENT,
  `login_user_name` varchar(50) NOT NULL,
  `login_password` varchar(50) NOT NULL,
  `nick_name` varchar(50) NOT NULL,
  `locked` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`admin_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `beyeah_mall_goods_info` (
  `goods_id` bigint NOT NULL AUTO_INCREMENT,
  `goods_name` varchar(200) NOT NULL DEFAULT '',
  `goods_intro` varchar(200) NOT NULL DEFAULT '',
  `goods_category_id` bigint NOT NULL DEFAULT 0,
  `goods_cover_img` varchar(200) NOT NULL DEFAULT '',
  `goods_carousel` varchar(500) NOT NULL DEFAULT '',
  `goods_detail_content` text NOT NULL,
  `original_price` int NOT NULL DEFAULT 1,
  `selling_price` int NOT NULL DEFAULT 1,
  `stock_num` int NOT NULL DEFAULT 0,
  `tag` varchar(20) NOT NULL DEFAULT '',
  `goods_sell_status` tinyint NOT NULL DEFAULT 0,
  `create_user` int NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_user` int NOT NULL DEFAULT 0,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`goods_id`),
  KEY `idx_goods_category_status` (`goods_category_id`,`goods_sell_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `beyeah_mall_shopping_cart_item` (
  `cart_item_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `goods_id` bigint NOT NULL DEFAULT 0,
  `goods_count` int NOT NULL DEFAULT 1,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`cart_item_id`),
  KEY `idx_cart_user_goods_deleted` (`user_id`,`goods_id`,`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `beyeah_mall_order` (
  `order_id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(20) NOT NULL DEFAULT '',
  `idempotency_key` varchar(64) DEFAULT NULL,
  `user_id` bigint NOT NULL DEFAULT 0,
  `total_price` int NOT NULL DEFAULT 1,
  `pay_status` tinyint NOT NULL DEFAULT 0,
  `pay_type` tinyint NOT NULL DEFAULT 0,
  `pay_time` datetime NULL DEFAULT NULL,
  `order_status` tinyint NOT NULL DEFAULT 0,
  `refund_status` tinyint NOT NULL DEFAULT 0,
  `refund_time` datetime NULL DEFAULT NULL,
  `extra_info` varchar(100) NOT NULL DEFAULT '',
  `user_name` varchar(30) NOT NULL DEFAULT '',
  `user_phone` varchar(11) NOT NULL DEFAULT '',
  `user_address` varchar(100) NOT NULL DEFAULT '',
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`order_id`),
  UNIQUE KEY `uk_beyeah_mall_order_order_no` (`order_no`),
  UNIQUE KEY `uk_order_user_idempotency` (`user_id`,`idempotency_key`),
  KEY `idx_order_unpaid_timeout` (`order_status`,`pay_status`,`create_time`),
  KEY `idx_order_refund_status` (`refund_status`,`update_time`),
  KEY `idx_order_user_time_deleted` (`user_id`,`create_time`,`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `beyeah_mall_after_sale` (
  `after_sale_id` bigint NOT NULL AUTO_INCREMENT,
  `after_sale_no` varchar(32) NOT NULL DEFAULT '',
  `user_id` bigint NOT NULL,
  `order_id` bigint NOT NULL,
  `order_no` varchar(20) NOT NULL DEFAULT '',
  `order_item_id` bigint NOT NULL,
  `goods_id` bigint NOT NULL DEFAULT 0,
  `goods_name` varchar(200) NOT NULL DEFAULT '',
  `refund_amount` int NOT NULL DEFAULT 0,
  `reason` varchar(120) NOT NULL DEFAULT '',
  `description` varchar(255) NOT NULL DEFAULT '',
  `reject_reason` varchar(120) NOT NULL DEFAULT '',
  `after_sale_status` tinyint NOT NULL DEFAULT 0,
  `refund_no` varchar(32) DEFAULT NULL,
  `handle_time` datetime DEFAULT NULL,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`after_sale_id`),
  UNIQUE KEY `uk_after_sale_no` (`after_sale_no`),
  UNIQUE KEY `uk_after_sale_order_item` (`order_item_id`),
  KEY `idx_after_sale_user_time` (`user_id`,`create_time`),
  KEY `idx_after_sale_status_time` (`after_sale_status`,`update_time`),
  KEY `idx_after_sale_order` (`order_id`,`order_item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `beyeah_mall_refund` (
  `refund_id` bigint NOT NULL AUTO_INCREMENT,
  `refund_no` varchar(32) NOT NULL DEFAULT '',
  `after_sale_id` bigint NOT NULL,
  `after_sale_no` varchar(32) NOT NULL DEFAULT '',
  `user_id` bigint NOT NULL,
  `order_id` bigint NOT NULL,
  `order_no` varchar(20) NOT NULL DEFAULT '',
  `refund_amount` int NOT NULL DEFAULT 0,
  `refund_status` tinyint NOT NULL DEFAULT 0,
  `refund_channel` tinyint NOT NULL DEFAULT 0,
  `idempotency_key` varchar(64) NOT NULL DEFAULT '',
  `callback_payload` varchar(1024) NOT NULL DEFAULT '',
  `fail_reason` varchar(120) NOT NULL DEFAULT '',
  `success_time` datetime DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`refund_id`),
  UNIQUE KEY `uk_refund_no` (`refund_no`),
  UNIQUE KEY `uk_refund_after_sale` (`after_sale_id`),
  UNIQUE KEY `uk_refund_idempotency` (`idempotency_key`),
  KEY `idx_refund_order_time` (`order_id`,`create_time`),
  KEY `idx_refund_status_time` (`refund_status`,`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `beyeah_mall_user_address` (
  `address_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `consignee` varchar(50) NOT NULL DEFAULT '',
  `phone` varchar(20) NOT NULL DEFAULT '',
  `province` varchar(32) NOT NULL DEFAULT '',
  `city` varchar(32) NOT NULL DEFAULT '',
  `district` varchar(32) NOT NULL DEFAULT '',
  `detail` varchar(120) NOT NULL DEFAULT '',
  `is_default` tinyint NOT NULL DEFAULT 0,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`address_id`),
  KEY `idx_user_deleted_default` (`user_id`,`is_deleted`,`is_default`),
  KEY `idx_user_deleted_time` (`user_id`,`is_deleted`,`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `beyeah_mall_order_item` (
  `order_item_id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL DEFAULT 0,
  `goods_id` bigint NOT NULL DEFAULT 0,
  `goods_name` varchar(200) NOT NULL DEFAULT '',
  `goods_cover_img` varchar(200) NOT NULL DEFAULT '',
  `selling_price` int NOT NULL DEFAULT 1,
  `goods_count` int NOT NULL DEFAULT 1,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`order_item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `beyeah_admin_role` (
  `role_id` bigint NOT NULL AUTO_INCREMENT,
  `role_code` varchar(64) NOT NULL,
  `role_name` varchar(64) NOT NULL DEFAULT '',
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`role_id`),
  UNIQUE KEY `uk_admin_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `beyeah_admin_permission` (
  `permission_id` bigint NOT NULL AUTO_INCREMENT,
  `permission_code` varchar(128) NOT NULL,
  `permission_name` varchar(128) NOT NULL DEFAULT '',
  `resource` varchar(64) NOT NULL DEFAULT '',
  `action` varchar(64) NOT NULL DEFAULT '',
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`permission_id`),
  UNIQUE KEY `uk_admin_permission_code` (`permission_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `beyeah_admin_user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_user_id` int NOT NULL,
  `role_id` bigint NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_user_role` (`admin_user_id`,`role_id`),
  KEY `idx_admin_user_role_role` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `beyeah_admin_role_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_id` bigint NOT NULL,
  `permission_id` bigint NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_role_permission` (`role_id`,`permission_id`),
  KEY `idx_admin_role_permission_permission` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `beyeah_admin_operation_log` (
  `log_id` bigint NOT NULL AUTO_INCREMENT,
  `operator_admin_id` int NOT NULL,
  `action` varchar(64) NOT NULL DEFAULT '',
  `resource_type` varchar(64) NOT NULL DEFAULT '',
  `resource_id` varchar(64) NOT NULL DEFAULT '',
  `request_json` varchar(2048) NOT NULL DEFAULT '',
  `response_code` int NOT NULL DEFAULT 0,
  `trace_id` varchar(64) NOT NULL DEFAULT '',
  `ip` varchar(64) NOT NULL DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`log_id`),
  KEY `idx_admin_operation_admin_time` (`operator_admin_id`,`create_time`),
  KEY `idx_admin_operation_trace_id` (`trace_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `beyeah_mall_goods_info` (
  `goods_id`, `goods_name`, `goods_intro`, `goods_category_id`, `goods_cover_img`, `goods_carousel`,
  `goods_detail_content`, `original_price`, `selling_price`, `stock_num`, `tag`, `goods_sell_status`,
  `create_user`, `create_time`, `update_user`, `update_time`
) VALUES
  (10001, 'E2E Phone', 'integration test goods', 45, '/test/phone.png', '/test/phone.png', '<p>detail</p>', 1999, 1499, 100, 'test', 0, 1, NOW(), 1, NOW()),
  (10002, 'E2E Disabled Goods', 'not selling', 45, '/test/disabled.png', '/test/disabled.png', '<p>detail</p>', 199, 99, 100, 'test', 1, 1, NOW(), 1, NOW());

INSERT INTO `beyeah_mall_admin_user` (`admin_user_id`, `login_user_name`, `login_password`, `nick_name`, `locked`) VALUES
  (1, 'admin', 'e10adc3949ba59abbe56e057f20f883e', 'super admin', 0),
  (2, 'goods-admin', 'e10adc3949ba59abbe56e057f20f883e', 'goods admin', 0);

INSERT INTO `beyeah_admin_role` (`role_id`, `role_code`, `role_name`, `is_deleted`) VALUES
  (1, 'SUPER_ADMIN', 'Super Admin', 0),
  (2, 'MERCHANDISER', 'Merchandiser', 0),
  (3, 'WAREHOUSE', 'Warehouse', 0),
  (4, 'CUSTOMER_SERVICE', 'Customer Service', 0),
  (5, 'RISK_OPERATOR', 'Risk Operator', 0),
  (6, 'AUDITOR', 'Auditor', 0);

INSERT INTO `beyeah_admin_permission` (`permission_id`, `permission_code`, `permission_name`, `resource`, `action`, `is_deleted`) VALUES
  (1, 'admin:dashboard:read', 'Read dashboard', 'admin', 'dashboard.read', 0),
  (2, 'goods:read', 'Read goods', 'goods', 'read', 0),
  (3, 'goods:write', 'Write goods', 'goods', 'write', 0),
  (4, 'order:read', 'Read orders', 'order', 'read', 0),
  (5, 'order:fulfill', 'Fulfill orders', 'order', 'fulfill', 0),
  (6, 'order:close', 'Close orders', 'order', 'close', 0),
  (7, 'user:read', 'Read users', 'user', 'read', 0),
  (8, 'user:lock', 'Lock users', 'user', 'lock', 0),
  (9, 'after_sale:read', 'Read after-sales', 'after_sale', 'read', 0),
  (10, 'after_sale:approve', 'Approve after-sales', 'after_sale', 'approve', 0),
  (11, 'audit:read', 'Read audit logs', 'audit', 'read', 0);

INSERT INTO `beyeah_admin_role_permission` (`role_id`, `permission_id`) VALUES
  (1, 1),(1, 2),(1, 3),(1, 4),(1, 5),(1, 6),(1, 7),(1, 8),(1, 9),(1, 10),(1, 11),
  (2, 2),(2, 3),
  (3, 4),(3, 5),(3, 6),
  (4, 4),(4, 9),(4, 10),
  (5, 7),(5, 8),
  (6, 2),(6, 4),(6, 7),(6, 9),(6, 11);

INSERT INTO `beyeah_admin_user_role` (`admin_user_id`, `role_id`) VALUES
  (1, 1),
  (2, 2);

SET FOREIGN_KEY_CHECKS = 1;

