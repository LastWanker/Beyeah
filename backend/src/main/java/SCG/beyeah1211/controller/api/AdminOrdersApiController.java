package SCG.beyeah1211.controller.api;

import SCG.beyeah1211.common.AdminPermissionCodes;
import SCG.beyeah1211.common.ServiceResultEnum;
import SCG.beyeah1211.security.RequirePermission;
import SCG.beyeah1211.service.AdminOperationLogService;
import SCG.beyeah1211.service.beyeahOrderService;
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
@RequestMapping("/api/v1/admin/orders")
public class AdminOrdersApiController {
    @Resource
    private beyeahOrderService beyeahOrderService;
    @Resource
    private AdminOperationLogService adminOperationLogService;

    @GetMapping
    @RequirePermission(AdminPermissionCodes.ORDER_READ)
    public Result list(@RequestParam(value = "page", defaultValue = "1") Integer page,
                       @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
                       @RequestParam(value = "orderNo", required = false) String orderNo) {
        Map<String, Object> params = new HashMap<>();
        params.put("page", page);
        params.put("limit", pageSize);
        if (orderNo != null && !orderNo.trim().isEmpty()) {
            params.put("orderNo", orderNo.trim());
        }
        PageResult result = beyeahOrderService.getbeyeahOrdersPage(new PageQueryUtil(params));
        return ResultGenerator.genSuccessResult(result);
    }

    @PutMapping("/{orderId}/check-done")
    @RequirePermission(AdminPermissionCodes.ORDER_FULFILL)
    public Result checkDone(@PathVariable("orderId") Long orderId, HttpServletRequest request) {
        if (orderId == null || orderId <= 0) {
            return ResultGenerator.genFailResult("invalid orderId");
        }
        return handleOrderAction(request, "order.check_done", "order", orderId, null, beyeahOrderService.checkDone(new Long[]{orderId}));
    }

    @PutMapping("/{orderId}/check-out")
    @RequirePermission(AdminPermissionCodes.ORDER_FULFILL)
    public Result checkOut(@PathVariable("orderId") Long orderId, HttpServletRequest request) {
        if (orderId == null || orderId <= 0) {
            return ResultGenerator.genFailResult("invalid orderId");
        }
        return handleOrderAction(request, "order.check_out", "order", orderId, null, beyeahOrderService.checkOut(new Long[]{orderId}));
    }

    @PutMapping("/{orderId}/close")
    @RequirePermission(AdminPermissionCodes.ORDER_CLOSE)
    public Result closeOrder(@PathVariable("orderId") Long orderId,
                             @RequestBody(required = false) Map<String, Object> payload,
                             HttpServletRequest request) {
        if (orderId == null || orderId <= 0) {
            return ResultGenerator.genFailResult("invalid orderId");
        }
        return handleOrderAction(request, "order.close", "order", orderId, payload, beyeahOrderService.closeOrder(new Long[]{orderId}));
    }

    private Result handleOrderAction(HttpServletRequest request,
                                     String action,
                                     String resourceType,
                                     Long resourceId,
                                     Object payload,
                                     String serviceResult) {
        Result result = ServiceResultEnum.SUCCESS.getResult().equals(serviceResult)
                ? ResultGenerator.genSuccessResult()
                : ResultGenerator.genFailResult(serviceResult);
        adminOperationLogService.log(
                request,
                action,
                resourceType,
                resourceId == null ? "" : String.valueOf(resourceId),
                payload,
                result.getResultCode()
        );
        return result;
    }
}
