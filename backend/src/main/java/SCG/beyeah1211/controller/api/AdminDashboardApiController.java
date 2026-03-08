package SCG.beyeah1211.controller.api;

import SCG.beyeah1211.common.AdminPermissionCodes;
import SCG.beyeah1211.security.RequirePermission;
import SCG.beyeah1211.util.Result;
import SCG.beyeah1211.util.ResultGenerator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
public class AdminDashboardApiController {
    @Resource
    private JdbcTemplate jdbcTemplate;

    @GetMapping
    @RequirePermission(AdminPermissionCodes.DASHBOARD_READ)
    public Result summary() {
        Map<String, Object> data = new HashMap<>();
        data.put("goodsCount", count("select count(*) from beyeah_mall_goods_info"));
        data.put("orderCount", count("select count(*) from beyeah_mall_order where is_deleted = 0"));
        data.put("userCount", count("select count(*) from beyeah_mall_user where is_deleted = 0"));
        data.put("afterSalePendingCount", count("select count(*) from beyeah_mall_after_sale where is_deleted = 0 and after_sale_status = 0"));
        data.put("afterSaleTotalCount", count("select count(*) from beyeah_mall_after_sale where is_deleted = 0"));
        return ResultGenerator.genSuccessResult(data);
    }

    private long count(String sql) {
        Long value = jdbcTemplate.queryForObject(sql, Long.class);
        return value == null ? 0L : value;
    }
}
