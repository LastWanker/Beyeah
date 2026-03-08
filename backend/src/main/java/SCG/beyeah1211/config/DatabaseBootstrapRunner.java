package SCG.beyeah1211.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class DatabaseBootstrapRunner implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseBootstrapRunner.class);

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        ensureAddressBookTable();
        ensureOrderRefundColumns();
        ensureAfterSaleTables();
        ensureAdminRbacTables();
    }

    private void ensureAddressBookTable() {
        final String sql = "CREATE TABLE IF NOT EXISTS beyeah_mall_user_address ("
                + "address_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'address id',"
                + "user_id BIGINT NOT NULL COMMENT 'user id',"
                + "consignee VARCHAR(50) NOT NULL DEFAULT '' COMMENT 'receiver name',"
                + "phone VARCHAR(20) NOT NULL DEFAULT '' COMMENT 'receiver phone',"
                + "province VARCHAR(32) NOT NULL DEFAULT '' COMMENT 'province',"
                + "city VARCHAR(32) NOT NULL DEFAULT '' COMMENT 'city',"
                + "district VARCHAR(32) NOT NULL DEFAULT '' COMMENT 'district',"
                + "detail VARCHAR(120) NOT NULL DEFAULT '' COMMENT 'detail address',"
                + "is_default TINYINT NOT NULL DEFAULT 0 COMMENT 'is default: 0 no, 1 yes',"
                + "is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'is deleted: 0 no, 1 yes',"
                + "create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',"
                + "update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',"
                + "PRIMARY KEY (address_id),"
                + "KEY idx_user_deleted_default (user_id, is_deleted, is_default),"
                + "KEY idx_user_deleted_time (user_id, is_deleted, update_time)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='user address book'";

        try {
            jdbcTemplate.execute(sql);
        } catch (Exception exception) {
            logger.error("failed to ensure table beyeah_mall_user_address, address api may be unavailable", exception);
        }
    }

    private void ensureOrderRefundColumns() {
        try {
            Integer refundStatusColumnCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'beyeah_mall_order' AND column_name = 'refund_status'",
                    Integer.class
            );
            if (refundStatusColumnCount != null && refundStatusColumnCount == 0) {
                jdbcTemplate.execute(
                        "ALTER TABLE beyeah_mall_order " +
                                "ADD COLUMN refund_status TINYINT NOT NULL DEFAULT 0 COMMENT 'refund aggregate status:0 none,1 processing,2 success,-1 rejected' AFTER order_status"
                );
            }

            Integer refundTimeColumnCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'beyeah_mall_order' AND column_name = 'refund_time'",
                    Integer.class
            );
            if (refundTimeColumnCount != null && refundTimeColumnCount == 0) {
                jdbcTemplate.execute(
                        "ALTER TABLE beyeah_mall_order " +
                                "ADD COLUMN refund_time DATETIME NULL DEFAULT NULL COMMENT 'latest refund finish time' AFTER refund_status"
                );
            }
        } catch (Exception exception) {
            logger.error("failed to ensure refund columns on beyeah_mall_order", exception);
        }
    }

    private void ensureAfterSaleTables() {
        final String createAfterSaleSql = "CREATE TABLE IF NOT EXISTS beyeah_mall_after_sale ("
                + "after_sale_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'after-sale id',"
                + "after_sale_no VARCHAR(32) NOT NULL DEFAULT '' COMMENT 'after-sale no',"
                + "user_id BIGINT NOT NULL COMMENT 'user id',"
                + "order_id BIGINT NOT NULL COMMENT 'order id',"
                + "order_no VARCHAR(20) NOT NULL DEFAULT '' COMMENT 'order no snapshot',"
                + "order_item_id BIGINT NOT NULL COMMENT 'order item id',"
                + "goods_id BIGINT NOT NULL DEFAULT 0 COMMENT 'goods id snapshot',"
                + "goods_name VARCHAR(200) NOT NULL DEFAULT '' COMMENT 'goods name snapshot',"
                + "refund_amount INT NOT NULL DEFAULT 0 COMMENT 'refund amount',"
                + "reason VARCHAR(120) NOT NULL DEFAULT '' COMMENT 'after-sale reason',"
                + "description VARCHAR(255) NOT NULL DEFAULT '' COMMENT 'after-sale detail description',"
                + "reject_reason VARCHAR(120) NOT NULL DEFAULT '' COMMENT 'reject reason by admin',"
                + "after_sale_status TINYINT NOT NULL DEFAULT 0 COMMENT '0 pending,1 approved,2 refunding,3 refunded,-1 rejected',"
                + "refund_no VARCHAR(32) DEFAULT NULL COMMENT 'refund no once created',"
                + "handle_time DATETIME NULL DEFAULT NULL COMMENT 'audit handle time',"
                + "is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'soft delete flag',"
                + "create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',"
                + "update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',"
                + "PRIMARY KEY (after_sale_id),"
                + "UNIQUE KEY uk_after_sale_no (after_sale_no),"
                + "UNIQUE KEY uk_after_sale_order_item (order_item_id),"
                + "KEY idx_after_sale_user_time (user_id, create_time),"
                + "KEY idx_after_sale_status_time (after_sale_status, update_time),"
                + "KEY idx_after_sale_order (order_id, order_item_id)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='after-sale order'";

        final String createRefundSql = "CREATE TABLE IF NOT EXISTS beyeah_mall_refund ("
                + "refund_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'refund id',"
                + "refund_no VARCHAR(32) NOT NULL DEFAULT '' COMMENT 'refund no',"
                + "after_sale_id BIGINT NOT NULL COMMENT 'after-sale id',"
                + "after_sale_no VARCHAR(32) NOT NULL DEFAULT '' COMMENT 'after-sale no',"
                + "user_id BIGINT NOT NULL COMMENT 'user id',"
                + "order_id BIGINT NOT NULL COMMENT 'order id',"
                + "order_no VARCHAR(20) NOT NULL DEFAULT '' COMMENT 'order no snapshot',"
                + "refund_amount INT NOT NULL DEFAULT 0 COMMENT 'refund amount',"
                + "refund_status TINYINT NOT NULL DEFAULT 0 COMMENT '0 pending,1 success,-1 failed',"
                + "refund_channel TINYINT NOT NULL DEFAULT 0 COMMENT '0 mock,1 alipay,2 wechat',"
                + "idempotency_key VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'idempotency key for callback/execute',"
                + "callback_payload VARCHAR(1024) NOT NULL DEFAULT '' COMMENT 'callback payload snapshot',"
                + "fail_reason VARCHAR(120) NOT NULL DEFAULT '' COMMENT 'failed reason',"
                + "success_time DATETIME NULL DEFAULT NULL COMMENT 'refund success time',"
                + "create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',"
                + "update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',"
                + "PRIMARY KEY (refund_id),"
                + "UNIQUE KEY uk_refund_no (refund_no),"
                + "UNIQUE KEY uk_refund_after_sale (after_sale_id),"
                + "UNIQUE KEY uk_refund_idempotency (idempotency_key),"
                + "KEY idx_refund_order_time (order_id, create_time),"
                + "KEY idx_refund_status_time (refund_status, update_time)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='refund record'";
        try {
            jdbcTemplate.execute(createAfterSaleSql);
            jdbcTemplate.execute(createRefundSql);
        } catch (Exception exception) {
            logger.error("failed to ensure after-sale/refund tables", exception);
        }
    }

    private void ensureAdminRbacTables() {
        final String createRoleSql = "CREATE TABLE IF NOT EXISTS beyeah_admin_role ("
                + "role_id BIGINT NOT NULL AUTO_INCREMENT,"
                + "role_code VARCHAR(64) NOT NULL,"
                + "role_name VARCHAR(64) NOT NULL DEFAULT '',"
                + "is_deleted TINYINT NOT NULL DEFAULT 0,"
                + "create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                + "PRIMARY KEY (role_id),"
                + "UNIQUE KEY uk_admin_role_code (role_code)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

        final String createPermissionSql = "CREATE TABLE IF NOT EXISTS beyeah_admin_permission ("
                + "permission_id BIGINT NOT NULL AUTO_INCREMENT,"
                + "permission_code VARCHAR(128) NOT NULL,"
                + "permission_name VARCHAR(128) NOT NULL DEFAULT '',"
                + "resource VARCHAR(64) NOT NULL DEFAULT '',"
                + "action VARCHAR(64) NOT NULL DEFAULT '',"
                + "is_deleted TINYINT NOT NULL DEFAULT 0,"
                + "create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                + "PRIMARY KEY (permission_id),"
                + "UNIQUE KEY uk_admin_permission_code (permission_code)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

        final String createUserRoleSql = "CREATE TABLE IF NOT EXISTS beyeah_admin_user_role ("
                + "id BIGINT NOT NULL AUTO_INCREMENT,"
                + "admin_user_id INT NOT NULL,"
                + "role_id BIGINT NOT NULL,"
                + "create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "PRIMARY KEY (id),"
                + "UNIQUE KEY uk_admin_user_role (admin_user_id, role_id),"
                + "KEY idx_admin_user_role_role (role_id)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

        final String createRolePermissionSql = "CREATE TABLE IF NOT EXISTS beyeah_admin_role_permission ("
                + "id BIGINT NOT NULL AUTO_INCREMENT,"
                + "role_id BIGINT NOT NULL,"
                + "permission_id BIGINT NOT NULL,"
                + "create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "PRIMARY KEY (id),"
                + "UNIQUE KEY uk_admin_role_permission (role_id, permission_id),"
                + "KEY idx_admin_role_permission_permission (permission_id)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

        final String createOperationLogSql = "CREATE TABLE IF NOT EXISTS beyeah_admin_operation_log ("
                + "log_id BIGINT NOT NULL AUTO_INCREMENT,"
                + "operator_admin_id INT NOT NULL,"
                + "action VARCHAR(64) NOT NULL DEFAULT '',"
                + "resource_type VARCHAR(64) NOT NULL DEFAULT '',"
                + "resource_id VARCHAR(64) NOT NULL DEFAULT '',"
                + "request_json VARCHAR(2048) NOT NULL DEFAULT '',"
                + "response_code INT NOT NULL DEFAULT 0,"
                + "trace_id VARCHAR(64) NOT NULL DEFAULT '',"
                + "ip VARCHAR(64) NOT NULL DEFAULT '',"
                + "create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "PRIMARY KEY (log_id),"
                + "KEY idx_admin_operation_admin_time (operator_admin_id, create_time),"
                + "KEY idx_admin_operation_trace_id (trace_id)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

        try {
            jdbcTemplate.execute(createRoleSql);
            jdbcTemplate.execute(createPermissionSql);
            jdbcTemplate.execute(createUserRoleSql);
            jdbcTemplate.execute(createRolePermissionSql);
            jdbcTemplate.execute(createOperationLogSql);

            jdbcTemplate.execute("INSERT IGNORE INTO beyeah_admin_role(role_code, role_name) VALUES "
                    + "('SUPER_ADMIN','Super Admin'),"
                    + "('MERCHANDISER','Merchandiser'),"
                    + "('WAREHOUSE','Warehouse'),"
                    + "('CUSTOMER_SERVICE','Customer Service'),"
                    + "('RISK_OPERATOR','Risk Operator'),"
                    + "('AUDITOR','Auditor')");

            jdbcTemplate.execute("INSERT IGNORE INTO beyeah_admin_permission(permission_code, permission_name, resource, action) VALUES "
                    + "('admin:dashboard:read','Read dashboard','admin','dashboard.read'),"
                    + "('goods:read','Read goods','goods','read'),"
                    + "('goods:write','Write goods','goods','write'),"
                    + "('order:read','Read orders','order','read'),"
                    + "('order:fulfill','Fulfill orders','order','fulfill'),"
                    + "('order:close','Close orders','order','close'),"
                    + "('user:read','Read users','user','read'),"
                    + "('user:lock','Lock users','user','lock'),"
                    + "('after_sale:read','Read after-sales','after_sale','read'),"
                    + "('after_sale:approve','Approve after-sales','after_sale','approve'),"
                    + "('audit:read','Read audit logs','audit','read')");

            jdbcTemplate.execute("INSERT IGNORE INTO beyeah_admin_role_permission(role_id, permission_id) "
                    + "SELECT r.role_id, p.permission_id FROM beyeah_admin_role r JOIN beyeah_admin_permission p ON p.is_deleted = 0 "
                    + "WHERE r.role_code = 'SUPER_ADMIN' AND r.is_deleted = 0");

            jdbcTemplate.execute("INSERT IGNORE INTO beyeah_admin_role_permission(role_id, permission_id) "
                    + "SELECT r.role_id, p.permission_id FROM beyeah_admin_role r JOIN beyeah_admin_permission p "
                    + "ON p.permission_code IN ('goods:read', 'goods:write') WHERE r.role_code = 'MERCHANDISER'");

            jdbcTemplate.execute("INSERT IGNORE INTO beyeah_admin_role_permission(role_id, permission_id) "
                    + "SELECT r.role_id, p.permission_id FROM beyeah_admin_role r JOIN beyeah_admin_permission p "
                    + "ON p.permission_code IN ('order:read', 'order:fulfill', 'order:close') WHERE r.role_code = 'WAREHOUSE'");

            jdbcTemplate.execute("INSERT IGNORE INTO beyeah_admin_role_permission(role_id, permission_id) "
                    + "SELECT r.role_id, p.permission_id FROM beyeah_admin_role r JOIN beyeah_admin_permission p "
                    + "ON p.permission_code IN ('after_sale:read', 'after_sale:approve', 'order:read') WHERE r.role_code = 'CUSTOMER_SERVICE'");

            jdbcTemplate.execute("INSERT IGNORE INTO beyeah_admin_role_permission(role_id, permission_id) "
                    + "SELECT r.role_id, p.permission_id FROM beyeah_admin_role r JOIN beyeah_admin_permission p "
                    + "ON p.permission_code IN ('user:read', 'user:lock') WHERE r.role_code = 'RISK_OPERATOR'");

            jdbcTemplate.execute("INSERT IGNORE INTO beyeah_admin_role_permission(role_id, permission_id) "
                    + "SELECT r.role_id, p.permission_id FROM beyeah_admin_role r JOIN beyeah_admin_permission p "
                    + "ON p.permission_code IN ('goods:read', 'order:read', 'user:read', 'after_sale:read', 'audit:read') "
                    + "WHERE r.role_code = 'AUDITOR'");

            jdbcTemplate.execute("INSERT IGNORE INTO beyeah_admin_user_role(admin_user_id, role_id) "
                    + "SELECT u.admin_user_id, r.role_id FROM beyeah_mall_admin_user u "
                    + "JOIN beyeah_admin_role r ON r.role_code = 'SUPER_ADMIN' WHERE u.locked = 0");
        } catch (Exception exception) {
            logger.error("failed to ensure admin rbac tables", exception);
        }
    }
}

