package SCG.beyeah1211.controller.api;

import SCG.beyeah1211.controller.vo.AfterSaleVO;
import SCG.beyeah1211.service.beyeahAfterSaleService;
import SCG.beyeah1211.util.PageQueryUtil;
import SCG.beyeah1211.util.PageResult;
import SCG.beyeah1211.util.Result;
import SCG.beyeah1211.util.ResultGenerator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/after-sales")
public class AfterSaleApiController {
    @Resource
    private beyeahAfterSaleService beyeahAfterSaleService;

    @PostMapping
    public Result create(@RequestParam(value = "userId", required = false) Long userId,
                         @RequestBody Map<String, Object> payload,
                         HttpServletRequest request) {
        Long resolvedUserId = ApiUserResolver.resolveUserId(userId, request);
        String orderNo = payload.get("orderNo") == null ? null : String.valueOf(payload.get("orderNo"));
        Long orderItemId = ApiUserResolver.asLong(payload.get("orderItemId"), "orderItemId");
        Integer refundAmount = ApiUserResolver.asInt(payload.get("refundAmount"), "refundAmount");
        String reason = payload.get("reason") == null ? null : String.valueOf(payload.get("reason"));
        String description = payload.get("description") == null ? null : String.valueOf(payload.get("description"));
        AfterSaleVO afterSale = beyeahAfterSaleService.createAfterSale(
                resolvedUserId,
                orderNo,
                orderItemId,
                refundAmount,
                reason,
                description
        );
        return ResultGenerator.genSuccessResult(afterSale);
    }

    @GetMapping
    public Result list(@RequestParam(value = "userId", required = false) Long userId,
                       @RequestParam(value = "page", defaultValue = "1") Integer page,
                       @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
                       @RequestParam(value = "afterSaleStatus", required = false) Integer afterSaleStatus,
                       @RequestParam(value = "orderNo", required = false) String orderNo,
                       HttpServletRequest request) {
        Long resolvedUserId = ApiUserResolver.resolveUserId(userId, request);
        Map<String, Object> params = new HashMap<>();
        params.put("userId", resolvedUserId);
        params.put("page", page);
        params.put("limit", pageSize);
        if (afterSaleStatus != null) {
            params.put("afterSaleStatus", afterSaleStatus);
        }
        if (orderNo != null && !orderNo.trim().isEmpty()) {
            params.put("orderNo", orderNo.trim());
        }
        PageResult result = beyeahAfterSaleService.getMyAfterSales(new PageQueryUtil(params));
        return ResultGenerator.genSuccessResult(result);
    }

    @GetMapping("/{afterSaleNo}")
    public Result detail(@PathVariable("afterSaleNo") String afterSaleNo,
                         @RequestParam(value = "userId", required = false) Long userId,
                         HttpServletRequest request) {
        Long resolvedUserId = ApiUserResolver.resolveUserId(userId, request);
        AfterSaleVO afterSale = beyeahAfterSaleService.getMyAfterSaleDetail(resolvedUserId, afterSaleNo);
        return ResultGenerator.genSuccessResult(afterSale);
    }
}
