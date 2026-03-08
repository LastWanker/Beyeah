package SCG.beyeah1211.controller.api;

import SCG.beyeah1211.common.AdminPermissionCodes;
import SCG.beyeah1211.controller.vo.AfterSaleVO;
import SCG.beyeah1211.security.RequirePermission;
import SCG.beyeah1211.service.AdminOperationLogService;
import SCG.beyeah1211.service.beyeahAfterSaleService;
import SCG.beyeah1211.util.PageQueryUtil;
import SCG.beyeah1211.util.PageResult;
import SCG.beyeah1211.util.Result;
import SCG.beyeah1211.util.ResultGenerator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/after-sales")
public class AdminAfterSaleApiController {
    @Resource
    private beyeahAfterSaleService beyeahAfterSaleService;
    @Resource
    private AdminOperationLogService adminOperationLogService;

    @GetMapping
    @RequirePermission(AdminPermissionCodes.AFTER_SALE_READ)
    public Result list(@RequestParam(value = "page", defaultValue = "1") Integer page,
                       @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
                       @RequestParam(value = "afterSaleStatus", required = false) Integer afterSaleStatus,
                       @RequestParam(value = "orderNo", required = false) String orderNo,
                       HttpServletRequest request) {
        AdminApiResolver.resolveAdminId(request);
        Map<String, Object> params = new HashMap<>();
        params.put("page", page);
        params.put("limit", pageSize);
        if (afterSaleStatus != null) {
            params.put("afterSaleStatus", afterSaleStatus);
        }
        if (orderNo != null && !orderNo.trim().isEmpty()) {
            params.put("orderNo", orderNo.trim());
        }
        PageResult result = beyeahAfterSaleService.getAdminAfterSales(new PageQueryUtil(params));
        return ResultGenerator.genSuccessResult(result);
    }

    @PutMapping("/{afterSaleId}/approve")
    @RequirePermission(AdminPermissionCodes.AFTER_SALE_APPROVE)
    public Result approve(@PathVariable("afterSaleId") Long afterSaleId,
                          HttpServletRequest request) {
        if (afterSaleId == null || afterSaleId <= 0) {
            return ResultGenerator.genFailResult("invalid afterSaleId");
        }
        AdminApiResolver.resolveAdminId(request);
        AfterSaleVO result = beyeahAfterSaleService.approveAfterSale(afterSaleId);
        adminOperationLogService.log(
                request,
                "after_sale.approve",
                "after_sale",
                String.valueOf(afterSaleId),
                null,
                200
        );
        return ResultGenerator.genSuccessResult(result);
    }

    @PutMapping("/{afterSaleId}/reject")
    @RequirePermission(AdminPermissionCodes.AFTER_SALE_APPROVE)
    public Result reject(@PathVariable("afterSaleId") Long afterSaleId,
                         @RequestBody(required = false) Map<String, Object> payload,
                         HttpServletRequest request) {
        if (afterSaleId == null || afterSaleId <= 0) {
            return ResultGenerator.genFailResult("invalid afterSaleId");
        }
        AdminApiResolver.resolveAdminId(request);
        String rejectReason = null;
        if (payload != null && payload.get("rejectReason") != null) {
            rejectReason = String.valueOf(payload.get("rejectReason"));
        }
        AfterSaleVO result = beyeahAfterSaleService.rejectAfterSale(afterSaleId, rejectReason);
        adminOperationLogService.log(
                request,
                "after_sale.reject",
                "after_sale",
                String.valueOf(afterSaleId),
                payload,
                200
        );
        return ResultGenerator.genSuccessResult(result);
    }
}
