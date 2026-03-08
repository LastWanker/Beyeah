package SCG.beyeah1211.api;

import SCG.beyeah1211.service.beyeahOrderService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiIntegrationTest {

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0.36")
            .withDatabaseName("beyeah")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("sql/test_schema.sql")
            .withStartupTimeout(Duration.ofMinutes(3));

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.datasource.driverClassName", MYSQL::getDriverClassName);
        registry.add("app.auth.token-secret", () -> "integration-test-secret");
        registry.add("app.auth.token-expire-seconds", () -> "3600");
        registry.add("app.admin-auth.token-secret", () -> "integration-test-admin-secret");
        registry.add("app.admin-auth.token-expire-seconds", () -> "3600");
    }

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    beyeahOrderService beyeahOrderService;

    @BeforeEach
    void resetBusinessTables() {
        jdbcTemplate.update("DELETE FROM beyeah_admin_operation_log");
        jdbcTemplate.update("DELETE FROM beyeah_mall_refund");
        jdbcTemplate.update("DELETE FROM beyeah_mall_after_sale");
        jdbcTemplate.update("DELETE FROM beyeah_mall_order_item");
        jdbcTemplate.update("DELETE FROM beyeah_mall_order");
        jdbcTemplate.update("DELETE FROM beyeah_mall_shopping_cart_item");
        jdbcTemplate.update("DELETE FROM beyeah_mall_user_address");
        jdbcTemplate.update("DELETE FROM beyeah_mall_user");
    }

    @Test
    void auth_register_and_login_should_return_token() throws Exception {
        String username = "u" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        String password = "Pass1234";

        ResponseEntity<String> registerResp = exchange(HttpMethod.POST, "/api/v1/auth/register",
                Map.of("username", username, "password", password), null);
        assertEquals(HttpStatus.OK, registerResp.getStatusCode());
        JsonNode registerJson = objectMapper.readTree(registerResp.getBody());
        assertEquals(200, registerJson.path("resultCode").asInt());
        assertTrue(registerJson.path("data").path("token").asText().length() > 10);

        ResponseEntity<String> loginResp = exchange(HttpMethod.POST, "/api/v1/auth/login",
                Map.of("username", username, "password", password), null);
        assertEquals(HttpStatus.OK, loginResp.getStatusCode());
        JsonNode loginJson = objectMapper.readTree(loginResp.getBody());
        assertEquals(200, loginJson.path("resultCode").asInt());
        assertTrue(loginJson.path("data").path("token").asText().length() > 10);
    }

    @Test
    void goods_query_should_return_list_and_detail() throws Exception {
        ResponseEntity<String> listResp = exchange(HttpMethod.GET, "/api/v1/goods?page=1&pageSize=10", null, null);
        assertEquals(HttpStatus.OK, listResp.getStatusCode());
        JsonNode listJson = objectMapper.readTree(listResp.getBody());
        assertEquals(200, listJson.path("resultCode").asInt());
        JsonNode list = listJson.path("data").path("list");
        assertTrue(list.isArray());
        assertTrue(list.size() > 0);

        long goodsId = list.get(0).path("goodsId").asLong();
        ResponseEntity<String> detailResp = exchange(HttpMethod.GET, "/api/v1/goods/" + goodsId, null, null);
        assertEquals(HttpStatus.OK, detailResp.getStatusCode());
        JsonNode detailJson = objectMapper.readTree(detailResp.getBody());
        assertEquals(200, detailJson.path("resultCode").asInt());
        assertEquals(goodsId, detailJson.path("data").path("goodsId").asLong());
    }

    @Test
    void protected_api_without_token_should_return_401() throws Exception {
        ResponseEntity<String> resp = exchange(HttpMethod.GET, "/api/v1/cart", null, null);
        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
        JsonNode json = objectMapper.readTree(resp.getBody());
        assertEquals(401, json.path("resultCode").asInt());
    }

    @Test
    void cart_and_order_flow_should_cover_success_and_failure_paths() throws Exception {
        LoginContext login = registerAndLogin();
        String token = login.token;

        ResponseEntity<String> addCartResp = exchange(HttpMethod.POST, "/api/v1/cart/items",
                Map.of("goodsId", 10001, "goodsCount", 2), token);
        assertEquals(HttpStatus.OK, addCartResp.getStatusCode());
        JsonNode addCartJson = objectMapper.readTree(addCartResp.getBody());
        assertEquals(200, addCartJson.path("resultCode").asInt());

        ResponseEntity<String> cartListResp = exchange(HttpMethod.GET, "/api/v1/cart", null, token);
        JsonNode cartListJson = objectMapper.readTree(cartListResp.getBody());
        assertEquals(200, cartListJson.path("resultCode").asInt());
        JsonNode cartItems = cartListJson.path("data");
        assertTrue(cartItems.isArray());
        assertEquals(1, cartItems.size());
        long cartItemId = cartItems.get(0).path("cartItemId").asLong();
        assertNotNull(cartItemId);

        Map<String, Object> orderPayload = Map.of(
                "address", "Shanghai Pudong",
                "goods", List.of(Map.of("goodsId", 10001, "goodsCount", 1)),
                "idempotencyKey", "it-key-" + UUID.randomUUID()
        );
        ResponseEntity<String> createOrderResp = exchange(HttpMethod.POST, "/api/v1/orders", orderPayload, token);
        JsonNode createOrderJson = objectMapper.readTree(createOrderResp.getBody());
        assertEquals(200, createOrderJson.path("resultCode").asInt());
        String orderNo = createOrderJson.path("data").path("orderNo").asText();
        assertTrue(orderNo.length() > 5);

        ResponseEntity<String> duplicateCreateOrderResp = exchange(HttpMethod.POST, "/api/v1/orders", orderPayload, token);
        JsonNode duplicateCreateOrderJson = objectMapper.readTree(duplicateCreateOrderResp.getBody());
        assertEquals(200, duplicateCreateOrderJson.path("resultCode").asInt());
        assertEquals(orderNo, duplicateCreateOrderJson.path("data").path("orderNo").asText());
        Integer orderCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM beyeah_mall_order WHERE order_no = ?", Integer.class, orderNo);
        assertEquals(1, orderCount);

        ResponseEntity<String> failOrderResp = exchange(HttpMethod.POST, "/api/v1/orders",
                Map.of(
                        "address", "Shanghai Pudong",
                        "goods", List.of(Map.of("goodsId", 99999, "goodsCount", 1)),
                        "idempotencyKey", "it-key-fail-" + UUID.randomUUID()
                ), token);
        JsonNode failOrderJson = objectMapper.readTree(failOrderResp.getBody());
        assertEquals(500, failOrderJson.path("resultCode").asInt());

        ResponseEntity<String> orderListResp = exchange(HttpMethod.GET, "/api/v1/orders?page=1&pageSize=10", null, token);
        JsonNode orderListJson = objectMapper.readTree(orderListResp.getBody());
        assertEquals(200, orderListJson.path("resultCode").asInt());
        assertTrue(orderListJson.path("data").path("list").size() >= 1);

        ResponseEntity<String> orderDetailResp = exchange(HttpMethod.GET, "/api/v1/orders/" + orderNo, null, token);
        JsonNode orderDetailJson = objectMapper.readTree(orderDetailResp.getBody());
        assertEquals(200, orderDetailJson.path("resultCode").asInt());
        assertEquals(orderNo, orderDetailJson.path("data").path("orderNo").asText());

        ResponseEntity<String> payResp = exchange(HttpMethod.POST, "/api/v1/orders/" + orderNo + "/pay",
                Map.of("payType", 2), token);
        JsonNode payJson = objectMapper.readTree(payResp.getBody());
        assertEquals(200, payJson.path("resultCode").asInt());
        assertEquals(orderNo, payJson.path("data").path("orderNo").asText());
        assertEquals(2, payJson.path("data").path("payType").asInt());

        ResponseEntity<String> paidOrderDetailResp = exchange(HttpMethod.GET, "/api/v1/orders/" + orderNo, null, token);
        JsonNode paidOrderDetailJson = objectMapper.readTree(paidOrderDetailResp.getBody());
        assertEquals(200, paidOrderDetailJson.path("resultCode").asInt());
        assertEquals(1, paidOrderDetailJson.path("data").path("orderStatus").asInt());
        assertEquals(1, paidOrderDetailJson.path("data").path("payStatus").asInt());
        assertEquals(2, paidOrderDetailJson.path("data").path("payType").asInt());
    }

    @Test
    void address_book_crud_and_order_snapshot_should_work() throws Exception {
        LoginContext login = registerAndLogin();
        String token = login.token;

        ResponseEntity<String> createAddress1Resp = exchange(HttpMethod.POST, "/api/v1/addresses",
                Map.of(
                        "consignee", "Alice",
                        "phone", "13800138000",
                        "province", "Shanghai",
                        "city", "Shanghai",
                        "district", "Pudong",
                        "detail", "Lane 1",
                        "isDefault", 1
                ), token);
        JsonNode address1Json = objectMapper.readTree(createAddress1Resp.getBody());
        assertEquals(200, address1Json.path("resultCode").asInt());
        long addressId1 = address1Json.path("data").path("addressId").asLong();
        assertTrue(addressId1 > 0);
        assertEquals(1, address1Json.path("data").path("isDefault").asInt());

        ResponseEntity<String> createAddress2Resp = exchange(HttpMethod.POST, "/api/v1/addresses",
                Map.of(
                        "consignee", "Bob",
                        "phone", "13900139000",
                        "province", "Zhejiang",
                        "city", "Hangzhou",
                        "district", "Xihu",
                        "detail", "No. 88",
                        "isDefault", 1
                ), token);
        JsonNode address2Json = objectMapper.readTree(createAddress2Resp.getBody());
        assertEquals(200, address2Json.path("resultCode").asInt());
        long addressId2 = address2Json.path("data").path("addressId").asLong();
        assertTrue(addressId2 > 0);
        assertEquals(1, address2Json.path("data").path("isDefault").asInt());

        ResponseEntity<String> listResp = exchange(HttpMethod.GET, "/api/v1/addresses", null, token);
        JsonNode listJson = objectMapper.readTree(listResp.getBody());
        assertEquals(200, listJson.path("resultCode").asInt());
        JsonNode addresses = listJson.path("data");
        assertEquals(2, addresses.size());
        int defaultCount = 0;
        for (JsonNode item : addresses) {
            if (item.path("isDefault").asInt() == 1) {
                defaultCount++;
            }
        }
        assertEquals(1, defaultCount);

        ResponseEntity<String> setDefaultResp = exchange(HttpMethod.PUT, "/api/v1/addresses/" + addressId1 + "/default", null, token);
        JsonNode setDefaultJson = objectMapper.readTree(setDefaultResp.getBody());
        assertEquals(200, setDefaultJson.path("resultCode").asInt());
        assertEquals(addressId1, setDefaultJson.path("data").path("addressId").asLong());

        ResponseEntity<String> addCartResp = exchange(HttpMethod.POST, "/api/v1/cart/items",
                Map.of("goodsId", 10001, "goodsCount", 1), token);
        JsonNode addCartJson = objectMapper.readTree(addCartResp.getBody());
        assertEquals(200, addCartJson.path("resultCode").asInt());

        Map<String, Object> orderPayload = Map.of(
                "addressId", addressId1,
                "goods", List.of(Map.of("goodsId", 10001, "goodsCount", 1)),
                "idempotencyKey", "addr-it-key-" + UUID.randomUUID()
        );
        ResponseEntity<String> createOrderResp = exchange(HttpMethod.POST, "/api/v1/orders", orderPayload, token);
        JsonNode createOrderJson = objectMapper.readTree(createOrderResp.getBody());
        assertEquals(200, createOrderJson.path("resultCode").asInt());
        String orderNo = createOrderJson.path("data").path("orderNo").asText();
        assertTrue(orderNo.length() > 5);

        String expectedSnapshotAddress = "Shanghai Shanghai Pudong Lane 1";
        Map<String, Object> snapshotRow = jdbcTemplate.queryForMap(
                "SELECT user_name, user_phone, user_address FROM beyeah_mall_order WHERE order_no = ?",
                orderNo
        );
        assertEquals("Alice", snapshotRow.get("user_name"));
        assertEquals("13800138000", snapshotRow.get("user_phone"));
        assertEquals(expectedSnapshotAddress, snapshotRow.get("user_address"));

        ResponseEntity<String> updateAddressResp = exchange(HttpMethod.PUT, "/api/v1/addresses/" + addressId1,
                Map.of("detail", "Lane 999", "consignee", "Alice"), token);
        JsonNode updateAddressJson = objectMapper.readTree(updateAddressResp.getBody());
        assertEquals(200, updateAddressJson.path("resultCode").asInt());
        assertEquals("Lane 999", updateAddressJson.path("data").path("detail").asText());

        ResponseEntity<String> detailResp = exchange(HttpMethod.GET, "/api/v1/orders/" + orderNo, null, token);
        JsonNode detailJson = objectMapper.readTree(detailResp.getBody());
        assertEquals(200, detailJson.path("resultCode").asInt());
        assertEquals(expectedSnapshotAddress, detailJson.path("data").path("userAddress").asText());

        ResponseEntity<String> deleteResp = exchange(HttpMethod.DELETE, "/api/v1/addresses/" + addressId1, null, token);
        JsonNode deleteJson = objectMapper.readTree(deleteResp.getBody());
        assertEquals(200, deleteJson.path("resultCode").asInt());

        ResponseEntity<String> listAfterDeleteResp = exchange(HttpMethod.GET, "/api/v1/addresses", null, token);
        JsonNode listAfterDeleteJson = objectMapper.readTree(listAfterDeleteResp.getBody());
        assertEquals(200, listAfterDeleteJson.path("resultCode").asInt());
        JsonNode listAfterDelete = listAfterDeleteJson.path("data");
        assertEquals(1, listAfterDelete.size());
        assertEquals(addressId2, listAfterDelete.get(0).path("addressId").asLong());
        assertEquals(1, listAfterDelete.get(0).path("isDefault").asInt());
    }

    @Test
    void auto_close_expired_order_should_close_and_recover_stock() {
        jdbcTemplate.update(
                "INSERT INTO beyeah_mall_user (nick_name, login_name, password_md5, introduce_sign, address, is_deleted, locked_flag, create_time) " +
                        "VALUES ('u1', ?, 'x', '', 'Shanghai', 0, 0, NOW())",
                "138" + UUID.randomUUID().toString().replace("-", "").substring(0, 8)
        );
        Long userId = jdbcTemplate.queryForObject("SELECT MAX(user_id) FROM beyeah_mall_user", Long.class);

        jdbcTemplate.update(
                "INSERT INTO beyeah_mall_order (order_no, idempotency_key, user_id, total_price, pay_status, pay_type, order_status, extra_info, user_name, user_phone, user_address, is_deleted, create_time, update_time) " +
                        "VALUES (?, ?, ?, 1499, 0, 0, 0, '', '', '', 'Shanghai', 0, DATE_SUB(NOW(), INTERVAL 2 HOUR), NOW())",
                "AUTO" + System.currentTimeMillis(),
                "auto-key-" + UUID.randomUUID(),
                userId
        );
        Long orderId = jdbcTemplate.queryForObject("SELECT MAX(order_id) FROM beyeah_mall_order", Long.class);
        jdbcTemplate.update(
                "INSERT INTO beyeah_mall_order_item (order_id, goods_id, goods_name, goods_cover_img, selling_price, goods_count, create_time) " +
                        "VALUES (?, 10001, 'E2E Phone', '/test/phone.png', 1499, 1, NOW())",
                orderId
        );
        jdbcTemplate.update("UPDATE beyeah_mall_goods_info SET stock_num = 99 WHERE goods_id = 10001");

        int closed = beyeahOrderService.autoCloseExpiredOrders(60, 10);
        assertTrue(closed >= 1);
        Integer orderStatus = jdbcTemplate.queryForObject("SELECT order_status FROM beyeah_mall_order WHERE order_id = ?", Integer.class, orderId);
        Integer payStatus = jdbcTemplate.queryForObject("SELECT pay_status FROM beyeah_mall_order WHERE order_id = ?", Integer.class, orderId);
        Integer stock = jdbcTemplate.queryForObject("SELECT stock_num FROM beyeah_mall_goods_info WHERE goods_id = 10001", Integer.class);
        assertEquals(-2, orderStatus);
        assertEquals(-1, payStatus);
        assertEquals(100, stock);
    }

    @Test
    void after_sale_apply_and_approve_should_work_with_idempotent_approve() throws Exception {
        LoginContext login = registerAndLogin();
        String token = login.token;
        String adminToken = adminLogin("admin", "123456");

        ResponseEntity<String> addCartResp = exchange(HttpMethod.POST, "/api/v1/cart/items",
                Map.of("goodsId", 10001, "goodsCount", 1), token);
        JsonNode addCartJson = objectMapper.readTree(addCartResp.getBody());
        assertEquals(200, addCartJson.path("resultCode").asInt());

        Map<String, Object> orderPayload = Map.of(
                "address", "Shanghai Pudong",
                "goods", List.of(Map.of("goodsId", 10001, "goodsCount", 1)),
                "idempotencyKey", "after-sale-order-" + UUID.randomUUID()
        );
        ResponseEntity<String> createOrderResp = exchange(HttpMethod.POST, "/api/v1/orders", orderPayload, token);
        JsonNode createOrderJson = objectMapper.readTree(createOrderResp.getBody());
        assertEquals(200, createOrderJson.path("resultCode").asInt());
        String orderNo = createOrderJson.path("data").path("orderNo").asText();
        assertTrue(orderNo.length() > 5);

        ResponseEntity<String> payResp = exchange(HttpMethod.POST, "/api/v1/orders/" + orderNo + "/pay",
                Map.of("payType", 2), token);
        JsonNode payJson = objectMapper.readTree(payResp.getBody());
        assertEquals(200, payJson.path("resultCode").asInt());

        ResponseEntity<String> orderDetailResp = exchange(HttpMethod.GET, "/api/v1/orders/" + orderNo, null, token);
        JsonNode orderDetailJson = objectMapper.readTree(orderDetailResp.getBody());
        assertEquals(200, orderDetailJson.path("resultCode").asInt());
        long orderItemId = orderDetailJson.path("data").path("beyeahOrderItemVOS").get(0).path("orderItemId").asLong();
        assertTrue(orderItemId > 0);

        Map<String, Object> applyPayload = Map.of(
                "orderNo", orderNo,
                "orderItemId", orderItemId,
                "refundAmount", 1499,
                "reason", "changed mind",
                "description", "apply from integration test"
        );
        ResponseEntity<String> applyResp = exchange(HttpMethod.POST, "/api/v1/after-sales", applyPayload, token);
        JsonNode applyJson = objectMapper.readTree(applyResp.getBody());
        assertEquals(200, applyJson.path("resultCode").asInt());
        long afterSaleId = applyJson.path("data").path("afterSaleId").asLong();
        assertTrue(afterSaleId > 0);
        assertEquals(0, applyJson.path("data").path("afterSaleStatus").asInt());

        ResponseEntity<String> duplicateApplyResp = exchange(HttpMethod.POST, "/api/v1/after-sales", applyPayload, token);
        JsonNode duplicateApplyJson = objectMapper.readTree(duplicateApplyResp.getBody());
        assertEquals(400, duplicateApplyJson.path("resultCode").asInt());

        ResponseEntity<String> userListResp = exchange(HttpMethod.GET, "/api/v1/after-sales?page=1&pageSize=10", null, token);
        JsonNode userListJson = objectMapper.readTree(userListResp.getBody());
        assertEquals(200, userListJson.path("resultCode").asInt());
        assertTrue(userListJson.path("data").path("list").size() >= 1);

        ResponseEntity<String> approveResp = exchange(HttpMethod.PUT, "/api/v1/admin/after-sales/" + afterSaleId + "/approve", null, adminToken);
        JsonNode approveJson = objectMapper.readTree(approveResp.getBody());
        assertEquals(200, approveJson.path("resultCode").asInt());
        assertEquals(3, approveJson.path("data").path("afterSaleStatus").asInt());
        assertEquals(1, approveJson.path("data").path("refundStatus").asInt());
        assertTrue(approveJson.path("data").path("refundNo").asText().length() > 5);

        ResponseEntity<String> approveAgainResp = exchange(HttpMethod.PUT, "/api/v1/admin/after-sales/" + afterSaleId + "/approve", null, adminToken);
        JsonNode approveAgainJson = objectMapper.readTree(approveAgainResp.getBody());
        assertEquals(200, approveAgainJson.path("resultCode").asInt());
        assertEquals(3, approveAgainJson.path("data").path("afterSaleStatus").asInt());
        assertEquals(1, approveAgainJson.path("data").path("refundStatus").asInt());

        ResponseEntity<String> afterOrderDetailResp = exchange(HttpMethod.GET, "/api/v1/orders/" + orderNo, null, token);
        JsonNode afterOrderDetailJson = objectMapper.readTree(afterOrderDetailResp.getBody());
        assertEquals(200, afterOrderDetailJson.path("resultCode").asInt());
        assertEquals(2, afterOrderDetailJson.path("data").path("refundStatus").asInt());
    }

    @Test
    void after_sale_reject_should_update_status() throws Exception {
        LoginContext login = registerAndLogin();
        String token = login.token;
        String adminToken = adminLogin("admin", "123456");

        ResponseEntity<String> addCartResp = exchange(HttpMethod.POST, "/api/v1/cart/items",
                Map.of("goodsId", 10001, "goodsCount", 1), token);
        JsonNode addCartJson = objectMapper.readTree(addCartResp.getBody());
        assertEquals(200, addCartJson.path("resultCode").asInt());

        Map<String, Object> orderPayload = Map.of(
                "address", "Shanghai Pudong",
                "goods", List.of(Map.of("goodsId", 10001, "goodsCount", 1)),
                "idempotencyKey", "after-sale-reject-" + UUID.randomUUID()
        );
        ResponseEntity<String> createOrderResp = exchange(HttpMethod.POST, "/api/v1/orders", orderPayload, token);
        JsonNode createOrderJson = objectMapper.readTree(createOrderResp.getBody());
        assertEquals(200, createOrderJson.path("resultCode").asInt());
        String orderNo = createOrderJson.path("data").path("orderNo").asText();

        ResponseEntity<String> payResp = exchange(HttpMethod.POST, "/api/v1/orders/" + orderNo + "/pay",
                Map.of("payType", 2), token);
        JsonNode payJson = objectMapper.readTree(payResp.getBody());
        assertEquals(200, payJson.path("resultCode").asInt());

        ResponseEntity<String> orderDetailResp = exchange(HttpMethod.GET, "/api/v1/orders/" + orderNo, null, token);
        JsonNode orderDetailJson = objectMapper.readTree(orderDetailResp.getBody());
        long orderItemId = orderDetailJson.path("data").path("beyeahOrderItemVOS").get(0).path("orderItemId").asLong();
        assertTrue(orderItemId > 0);

        ResponseEntity<String> applyResp = exchange(HttpMethod.POST, "/api/v1/after-sales",
                Map.of(
                        "orderNo", orderNo,
                        "orderItemId", orderItemId,
                        "refundAmount", 1499,
                        "reason", "size mismatch",
                        "description", "reject path"
                ),
                token);
        JsonNode applyJson = objectMapper.readTree(applyResp.getBody());
        assertEquals(200, applyJson.path("resultCode").asInt());
        long afterSaleId = applyJson.path("data").path("afterSaleId").asLong();
        assertTrue(afterSaleId > 0);

        ResponseEntity<String> rejectResp = exchange(HttpMethod.PUT, "/api/v1/admin/after-sales/" + afterSaleId + "/reject",
                Map.of("rejectReason", "evidence not enough"), adminToken);
        JsonNode rejectJson = objectMapper.readTree(rejectResp.getBody());
        assertEquals(200, rejectJson.path("resultCode").asInt());
        assertEquals(-1, rejectJson.path("data").path("afterSaleStatus").asInt());
        assertEquals("evidence not enough", rejectJson.path("data").path("rejectReason").asText());

        ResponseEntity<String> afterOrderDetailResp = exchange(HttpMethod.GET, "/api/v1/orders/" + orderNo, null, token);
        JsonNode afterOrderDetailJson = objectMapper.readTree(afterOrderDetailResp.getBody());
        assertEquals(200, afterOrderDetailJson.path("resultCode").asInt());
        assertEquals(-1, afterOrderDetailJson.path("data").path("refundStatus").asInt());
    }

    @Test
    void admin_auth_and_rbac_should_return_401_or_403() throws Exception {
        ResponseEntity<String> unauthorizedResp = exchange(HttpMethod.GET, "/api/v1/admin/goods?page=1&pageSize=10", null, null);
        assertEquals(HttpStatus.UNAUTHORIZED, unauthorizedResp.getStatusCode());
        JsonNode unauthorizedJson = objectMapper.readTree(unauthorizedResp.getBody());
        assertEquals(401, unauthorizedJson.path("resultCode").asInt());

        String superAdminToken = adminLogin("admin", "123456");
        ResponseEntity<String> meResp = exchange(HttpMethod.GET, "/api/v1/admin/auth/me", null, superAdminToken);
        assertEquals(HttpStatus.OK, meResp.getStatusCode());
        JsonNode meJson = objectMapper.readTree(meResp.getBody());
        assertEquals(200, meJson.path("resultCode").asInt());
        assertEquals("admin", meJson.path("data").path("userName").asText());

        LoginContext userLogin = registerAndLogin();
        ResponseEntity<String> userCallAdminResp = exchange(HttpMethod.GET, "/api/v1/admin/auth/me", null, userLogin.token);
        assertEquals(HttpStatus.UNAUTHORIZED, userCallAdminResp.getStatusCode());

        String goodsAdminToken = adminLogin("goods-admin", "123456");
        ResponseEntity<String> forbiddenResp = exchange(HttpMethod.PUT, "/api/v1/admin/after-sales/1/approve", null, goodsAdminToken);
        assertEquals(HttpStatus.FORBIDDEN, forbiddenResp.getStatusCode());
        JsonNode forbiddenJson = objectMapper.readTree(forbiddenResp.getBody());
        assertEquals(403, forbiddenJson.path("resultCode").asInt());
    }

    @Test
    void admin_write_operations_should_persist_audit_logs() throws Exception {
        LoginContext buyer = registerAndLogin();
        String buyerToken = buyer.token;
        String adminToken = adminLogin("admin", "123456");
        LoginContext userToLock = registerAndLogin();

        ResponseEntity<String> addCartResp = exchange(HttpMethod.POST, "/api/v1/cart/items",
                Map.of("goodsId", 10001, "goodsCount", 1), buyerToken);
        assertEquals(200, objectMapper.readTree(addCartResp.getBody()).path("resultCode").asInt());

        Map<String, Object> orderPayload = Map.of(
                "address", "Shanghai Pudong",
                "goods", List.of(Map.of("goodsId", 10001, "goodsCount", 1)),
                "idempotencyKey", "admin-audit-" + UUID.randomUUID()
        );
        ResponseEntity<String> createOrderResp = exchange(HttpMethod.POST, "/api/v1/orders", orderPayload, buyerToken);
        JsonNode createOrderJson = objectMapper.readTree(createOrderResp.getBody());
        assertEquals(200, createOrderJson.path("resultCode").asInt());
        String orderNo = createOrderJson.path("data").path("orderNo").asText();
        Long orderId = jdbcTemplate.queryForObject("SELECT order_id FROM beyeah_mall_order WHERE order_no = ?", Long.class, orderNo);
        assertNotNull(orderId);

        ResponseEntity<String> payResp = exchange(HttpMethod.POST, "/api/v1/orders/" + orderNo + "/pay",
                Map.of("payType", 2), buyerToken);
        assertEquals(200, objectMapper.readTree(payResp.getBody()).path("resultCode").asInt());

        ResponseEntity<String> orderDetailResp = exchange(HttpMethod.GET, "/api/v1/orders/" + orderNo, null, buyerToken);
        JsonNode orderDetailJson = objectMapper.readTree(orderDetailResp.getBody());
        long orderItemId = orderDetailJson.path("data").path("beyeahOrderItemVOS").get(0).path("orderItemId").asLong();
        assertTrue(orderItemId > 0);

        ResponseEntity<String> applyResp = exchange(HttpMethod.POST, "/api/v1/after-sales",
                Map.of(
                        "orderNo", orderNo,
                        "orderItemId", orderItemId,
                        "refundAmount", 1499,
                        "reason", "audit",
                        "description", "admin audit test"
                ),
                buyerToken);
        JsonNode applyJson = objectMapper.readTree(applyResp.getBody());
        assertEquals(200, applyJson.path("resultCode").asInt());
        long afterSaleId = applyJson.path("data").path("afterSaleId").asLong();
        assertTrue(afterSaleId > 0);

        Long lockUserId = jdbcTemplate.queryForObject(
                "SELECT user_id FROM beyeah_mall_user WHERE login_name = ?",
                Long.class,
                userToLock.username
        );
        assertNotNull(lockUserId);

        ResponseEntity<String> goodsStatusResp = exchange(HttpMethod.PUT, "/api/v1/admin/goods/10001/status",
                Map.of("sellStatus", 1), adminToken);
        assertEquals(200, objectMapper.readTree(goodsStatusResp.getBody()).path("resultCode").asInt());

        ResponseEntity<String> checkDoneResp = exchange(HttpMethod.PUT, "/api/v1/admin/orders/" + orderId + "/check-done",
                null, adminToken);
        assertEquals(200, objectMapper.readTree(checkDoneResp.getBody()).path("resultCode").asInt());

        ResponseEntity<String> lockResp = exchange(HttpMethod.PUT, "/api/v1/admin/users/" + lockUserId + "/lock",
                Map.of("lockStatus", 1), adminToken);
        assertEquals(200, objectMapper.readTree(lockResp.getBody()).path("resultCode").asInt());

        ResponseEntity<String> approveResp = exchange(HttpMethod.PUT, "/api/v1/admin/after-sales/" + afterSaleId + "/approve",
                null, adminToken);
        assertEquals(200, objectMapper.readTree(approveResp.getBody()).path("resultCode").asInt());

        assertTrue(countAdminAction("goods.status.update") >= 1);
        assertTrue(countAdminAction("order.check_done") >= 1);
        assertTrue(countAdminAction("user.lock") >= 1);
        assertTrue(countAdminAction("after_sale.approve") >= 1);
    }

    private LoginContext registerAndLogin() throws Exception {
        String username = "u" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        String password = "Pass1234";
        ResponseEntity<String> registerResp = exchange(HttpMethod.POST, "/api/v1/auth/register",
                Map.of("username", username, "password", password), null);
        JsonNode registerJson = objectMapper.readTree(registerResp.getBody());
        assertEquals(200, registerJson.path("resultCode").asInt());

        ResponseEntity<String> loginResp = exchange(HttpMethod.POST, "/api/v1/auth/login",
                Map.of("username", username, "password", password), null);
        JsonNode loginJson = objectMapper.readTree(loginResp.getBody());
        assertEquals(200, loginJson.path("resultCode").asInt());
        return new LoginContext(loginJson.path("data").path("token").asText(), username);
    }

    private String adminLogin(String userName, String password) throws Exception {
        ResponseEntity<String> loginResp = exchange(HttpMethod.POST, "/api/v1/admin/auth/login",
                Map.of("userName", userName, "password", password), null);
        JsonNode loginJson = objectMapper.readTree(loginResp.getBody());
        assertEquals(200, loginJson.path("resultCode").asInt());
        String token = loginJson.path("data").path("token").asText();
        assertTrue(token.length() > 10);
        return token;
    }

    private int countAdminAction(String action) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM beyeah_admin_operation_log WHERE action = ?",
                Integer.class,
                action
        );
        return count == null ? 0 : count;
    }

    private ResponseEntity<String> exchange(HttpMethod method, String path, Object body, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null && !token.isBlank()) {
            headers.setBearerAuth(token);
        }
        HttpEntity<?> entity = body == null ? new HttpEntity<>(headers) : new HttpEntity<>(body, headers);
        return restTemplate.exchange(url(path), method, entity, String.class);
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private static final class LoginContext {
        private final String token;
        private final String username;

        private LoginContext(String token, String username) {
            this.token = token;
            this.username = username;
        }
    }
}
