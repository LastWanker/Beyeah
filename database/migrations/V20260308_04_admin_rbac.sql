-- Admin RBAC + operation audit
-- Target DB: MySQL 8.x
-- Date: 2026-03-08

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS beyeah_admin_role (
    role_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'role id',
    role_code VARCHAR(64) NOT NULL COMMENT 'role code',
    role_name VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'role name',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'soft delete flag',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    PRIMARY KEY (role_id),
    UNIQUE KEY uk_admin_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='admin role';

CREATE TABLE IF NOT EXISTS beyeah_admin_permission (
    permission_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'permission id',
    permission_code VARCHAR(128) NOT NULL COMMENT 'permission code',
    permission_name VARCHAR(128) NOT NULL DEFAULT '' COMMENT 'permission name',
    resource VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'resource',
    action VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'action',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'soft delete flag',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    PRIMARY KEY (permission_id),
    UNIQUE KEY uk_admin_permission_code (permission_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='admin permission';

CREATE TABLE IF NOT EXISTS beyeah_admin_user_role (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'pk id',
    admin_user_id INT NOT NULL COMMENT 'admin user id',
    role_id BIGINT NOT NULL COMMENT 'role id',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_admin_user_role (admin_user_id, role_id),
    KEY idx_admin_user_role_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='admin user role';

CREATE TABLE IF NOT EXISTS beyeah_admin_role_permission (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'pk id',
    role_id BIGINT NOT NULL COMMENT 'role id',
    permission_id BIGINT NOT NULL COMMENT 'permission id',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_admin_role_permission (role_id, permission_id),
    KEY idx_admin_role_permission_permission (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='admin role permission';

CREATE TABLE IF NOT EXISTS beyeah_admin_operation_log (
    log_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'log id',
    operator_admin_id INT NOT NULL COMMENT 'operator admin id',
    action VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'operation action',
    resource_type VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'resource type',
    resource_id VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'resource id',
    request_json VARCHAR(2048) NOT NULL DEFAULT '' COMMENT 'request body json',
    response_code INT NOT NULL DEFAULT 0 COMMENT 'response code',
    trace_id VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'trace id',
    ip VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'request ip',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    PRIMARY KEY (log_id),
    KEY idx_admin_operation_admin_time (operator_admin_id, create_time),
    KEY idx_admin_operation_trace_id (trace_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='admin operation audit log';

INSERT IGNORE INTO beyeah_admin_role (role_code, role_name)
VALUES
    ('SUPER_ADMIN', 'Super Admin'),
    ('MERCHANDISER', 'Merchandiser'),
    ('WAREHOUSE', 'Warehouse'),
    ('CUSTOMER_SERVICE', 'Customer Service'),
    ('RISK_OPERATOR', 'Risk Operator'),
    ('AUDITOR', 'Auditor');

INSERT IGNORE INTO beyeah_admin_permission (permission_code, permission_name, resource, action)
VALUES
    ('admin:dashboard:read', 'Read dashboard', 'admin', 'dashboard.read'),
    ('goods:read', 'Read goods', 'goods', 'read'),
    ('goods:write', 'Write goods', 'goods', 'write'),
    ('order:read', 'Read orders', 'order', 'read'),
    ('order:fulfill', 'Fulfill orders', 'order', 'fulfill'),
    ('order:close', 'Close orders', 'order', 'close'),
    ('user:read', 'Read users', 'user', 'read'),
    ('user:lock', 'Lock users', 'user', 'lock'),
    ('after_sale:read', 'Read after-sales', 'after_sale', 'read'),
    ('after_sale:approve', 'Approve after-sales', 'after_sale', 'approve'),
    ('audit:read', 'Read audit logs', 'audit', 'read');

-- SUPER_ADMIN binds all permissions
INSERT IGNORE INTO beyeah_admin_role_permission (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM beyeah_admin_role r
JOIN beyeah_admin_permission p ON p.is_deleted = 0
WHERE r.role_code = 'SUPER_ADMIN'
  AND r.is_deleted = 0;

-- Role-specific minimum permissions
INSERT IGNORE INTO beyeah_admin_role_permission (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM beyeah_admin_role r
JOIN beyeah_admin_permission p ON p.permission_code IN ('goods:read', 'goods:write')
WHERE r.role_code = 'MERCHANDISER';

INSERT IGNORE INTO beyeah_admin_role_permission (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM beyeah_admin_role r
JOIN beyeah_admin_permission p ON p.permission_code IN ('order:read', 'order:fulfill', 'order:close')
WHERE r.role_code = 'WAREHOUSE';

INSERT IGNORE INTO beyeah_admin_role_permission (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM beyeah_admin_role r
JOIN beyeah_admin_permission p ON p.permission_code IN ('after_sale:read', 'after_sale:approve', 'order:read')
WHERE r.role_code = 'CUSTOMER_SERVICE';

INSERT IGNORE INTO beyeah_admin_role_permission (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM beyeah_admin_role r
JOIN beyeah_admin_permission p ON p.permission_code IN ('user:read', 'user:lock')
WHERE r.role_code = 'RISK_OPERATOR';

INSERT IGNORE INTO beyeah_admin_role_permission (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM beyeah_admin_role r
JOIN beyeah_admin_permission p ON p.permission_code IN ('goods:read', 'order:read', 'user:read', 'after_sale:read', 'audit:read')
WHERE r.role_code = 'AUDITOR';

-- Backfill active existing admin users as SUPER_ADMIN
INSERT IGNORE INTO beyeah_admin_user_role (admin_user_id, role_id)
SELECT u.admin_user_id, r.role_id
FROM beyeah_mall_admin_user u
JOIN beyeah_admin_role r ON r.role_code = 'SUPER_ADMIN'
WHERE u.locked = 0;

