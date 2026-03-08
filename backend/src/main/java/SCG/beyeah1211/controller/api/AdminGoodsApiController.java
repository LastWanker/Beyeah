package SCG.beyeah1211.controller.api;

import SCG.beyeah1211.common.AdminPermissionCodes;
import SCG.beyeah1211.common.Constants;
import SCG.beyeah1211.common.ServiceResultEnum;
import SCG.beyeah1211.common.beyeahException;
import SCG.beyeah1211.entity.beyeahGoods;
import SCG.beyeah1211.security.RequirePermission;
import SCG.beyeah1211.service.AdminOperationLogService;
import SCG.beyeah1211.service.beyeahGoodsService;
import SCG.beyeah1211.util.PageQueryUtil;
import SCG.beyeah1211.util.PageResult;
import SCG.beyeah1211.util.Result;
import SCG.beyeah1211.util.ResultGenerator;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
@RequestMapping("/api/v1/admin/goods")
public class AdminGoodsApiController {
    @Resource
    private beyeahGoodsService beyeahGoodsService;
    @Resource
    private AdminOperationLogService adminOperationLogService;

    @GetMapping
    @RequirePermission(AdminPermissionCodes.GOODS_READ)
    public Result list(@RequestParam(value = "page", defaultValue = "1") Integer page,
                       @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        Map<String, Object> params = new HashMap<>();
        params.put("page", page);
        params.put("limit", pageSize);
        PageResult pageResult = beyeahGoodsService.getbeyeahGoodsPage(new PageQueryUtil(params));
        return ResultGenerator.genSuccessResult(pageResult);
    }

    @PostMapping
    @RequirePermission(AdminPermissionCodes.GOODS_WRITE)
    public Result create(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
        Integer adminId = AdminApiResolver.resolveAdminId(request);
        beyeahGoods goods = parseAndValidateGoods(payload, null, adminId);
        String serviceResult = beyeahGoodsService.savebeyeahGoods(goods);
        Result result = ServiceResultEnum.SUCCESS.getResult().equals(serviceResult)
                ? ResultGenerator.genSuccessResult()
                : ResultGenerator.genFailResult(serviceResult);
        adminOperationLogService.log(
                request,
                "goods.create",
                "goods",
                "",
                payload,
                result.getResultCode()
        );
        return result;
    }

    @PutMapping("/{goodsId}")
    @RequirePermission(AdminPermissionCodes.GOODS_WRITE)
    public Result update(@PathVariable("goodsId") Long goodsId,
                         @RequestBody Map<String, Object> payload,
                         HttpServletRequest request) {
        Integer adminId = AdminApiResolver.resolveAdminId(request);
        beyeahGoods goods = parseAndValidateGoods(payload, goodsId, adminId);
        String serviceResult = beyeahGoodsService.updatebeyeahGoods(goods);
        Result result = ServiceResultEnum.SUCCESS.getResult().equals(serviceResult)
                ? ResultGenerator.genSuccessResult()
                : ResultGenerator.genFailResult(serviceResult);
        adminOperationLogService.log(
                request,
                "goods.update",
                "goods",
                String.valueOf(goodsId),
                payload,
                result.getResultCode()
        );
        return result;
    }

    @PutMapping("/{goodsId}/status")
    @RequirePermission(AdminPermissionCodes.GOODS_WRITE)
    public Result updateStatus(@PathVariable("goodsId") Long goodsId,
                               @RequestBody Map<String, Object> payload,
                               HttpServletRequest request) {
        Integer sellStatus = payload.get("sellStatus") == null ? null : ApiUserResolver.asInt(payload.get("sellStatus"), "sellStatus");
        if (goodsId == null || goodsId <= 0 || sellStatus == null
                || (sellStatus != Constants.SELL_STATUS_UP && sellStatus != Constants.SELL_STATUS_DOWN)) {
            return ResultGenerator.genFailResult("invalid goodsId or sellStatus");
        }
        boolean success = beyeahGoodsService.batchUpdateSellStatus(new Long[]{goodsId}, sellStatus);
        Result result = success ? ResultGenerator.genSuccessResult() : ResultGenerator.genFailResult("update sell status failed");
        adminOperationLogService.log(
                request,
                "goods.status.update",
                "goods",
                String.valueOf(goodsId),
                payload,
                result.getResultCode()
        );
        return result;
    }

    private beyeahGoods parseAndValidateGoods(Map<String, Object> payload, Long goodsId, Integer adminId) {
        String goodsName = payload.get("goodsName") == null ? "" : String.valueOf(payload.get("goodsName")).trim();
        String goodsIntro = payload.get("goodsIntro") == null ? "" : String.valueOf(payload.get("goodsIntro")).trim();
        Long goodsCategoryId = payload.get("goodsCategoryId") == null ? null : ApiUserResolver.asLong(payload.get("goodsCategoryId"), "goodsCategoryId");
        String goodsCoverImg = payload.get("goodsCoverImg") == null ? "" : String.valueOf(payload.get("goodsCoverImg")).trim();
        String goodsCarousel = payload.get("goodsCarousel") == null ? goodsCoverImg : String.valueOf(payload.get("goodsCarousel")).trim();
        Integer originalPrice = payload.get("originalPrice") == null ? null : ApiUserResolver.asInt(payload.get("originalPrice"), "originalPrice");
        Integer sellingPrice = payload.get("sellingPrice") == null ? null : ApiUserResolver.asInt(payload.get("sellingPrice"), "sellingPrice");
        Integer stockNum = payload.get("stockNum") == null ? null : ApiUserResolver.asInt(payload.get("stockNum"), "stockNum");
        String tag = payload.get("tag") == null ? "" : String.valueOf(payload.get("tag")).trim();
        Integer goodsSellStatus = payload.get("goodsSellStatus") == null ? null : ApiUserResolver.asInt(payload.get("goodsSellStatus"), "goodsSellStatus");
        String goodsDetailContent = payload.get("goodsDetailContent") == null ? "" : String.valueOf(payload.get("goodsDetailContent")).trim();

        boolean invalid = !StringUtils.hasText(goodsName)
                || !StringUtils.hasText(goodsIntro)
                || !StringUtils.hasText(goodsCoverImg)
                || !StringUtils.hasText(goodsDetailContent)
                || !StringUtils.hasText(tag)
                || goodsCategoryId == null || goodsCategoryId <= 0
                || originalPrice == null || originalPrice <= 0
                || sellingPrice == null || sellingPrice <= 0
                || stockNum == null || stockNum < 0
                || goodsSellStatus == null
                || (goodsSellStatus != Constants.SELL_STATUS_UP && goodsSellStatus != Constants.SELL_STATUS_DOWN);
        if (invalid) {
            beyeahException.fail("invalid goods payload");
        }

        beyeahGoods goods = new beyeahGoods();
        goods.setGoodsId(goodsId);
        goods.setGoodsName(goodsName);
        goods.setGoodsIntro(goodsIntro);
        goods.setGoodsCategoryId(goodsCategoryId);
        goods.setGoodsCoverImg(goodsCoverImg);
        goods.setGoodsCarousel(goodsCarousel);
        goods.setOriginalPrice(originalPrice);
        goods.setSellingPrice(sellingPrice);
        goods.setStockNum(stockNum);
        goods.setTag(tag);
        goods.setGoodsSellStatus(goodsSellStatus.byteValue());
        goods.setGoodsDetailContent(goodsDetailContent);
        if (adminId != null && adminId > 0) {
            goods.setCreateUser(adminId);
            goods.setUpdateUser(adminId);
        }
        return goods;
    }
}
