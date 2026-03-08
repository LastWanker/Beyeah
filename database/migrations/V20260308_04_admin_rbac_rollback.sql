-- Admin RBAC + operation audit rollback
-- Note: this script only drops RBAC/audit objects and does not touch C-end business tables.

SET NAMES utf8mb4;

DROP TABLE IF EXISTS beyeah_admin_operation_log;
DROP TABLE IF EXISTS beyeah_admin_role_permission;
DROP TABLE IF EXISTS beyeah_admin_user_role;
DROP TABLE IF EXISTS beyeah_admin_permission;
DROP TABLE IF EXISTS beyeah_admin_role;
