package SCG.beyeah1211.controller.api;

import SCG.beyeah1211.common.ServiceResultEnum;
import SCG.beyeah1211.controller.vo.beyeahShoppingCartItemVO;
import SCG.beyeah1211.entity.beyeahShoppingCartItem;
import SCG.beyeah1211.service.beyeahShoppingCartService;
import SCG.beyeah1211.util.Result;
import SCG.beyeah1211.util.ResultGenerator;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/cart")
public class CartApiController {
    @Resource
    private beyeahShoppingCartService beyeahShoppingCartService;

    @GetMapping
    public Result list(@RequestParam(value = "userId", required = false) Long userId,
                       HttpServletRequest request) {
        Long resolvedUserId = ApiUserResolver.resolveUserId(userId, request);
        List<beyeahShoppingCartItemVO> cartItems = beyeahShoppingCartService.getMyShoppingCartItems(resolvedUserId);
        return ResultGenerator.genSuccessResult(cartItems);
    }

    @PostMapping("/items")
    public Result add(@RequestParam(value = "userId", required = false) Long userId,
                      @RequestBody Map<String, Object> payload,
                      HttpServletRequest request) {
        Long resolvedUserId = ApiUserResolver.resolveUserId(userId, request);
        beyeahShoppingCartItem item = new beyeahShoppingCartItem();
        item.setUserId(resolvedUserId);
        item.setGoodsId(ApiUserResolver.asLong(payload.get("goodsId"), "goodsId"));
        item.setGoodsCount(ApiUserResolver.asInt(payload.get("goodsCount"), "goodsCount"));
        String saveResult = beyeahShoppingCartService.savebeyeahCartItem(item);
        if (ServiceResultEnum.SUCCESS.getResult().equals(saveResult)) {
            return ResultGenerator.genSuccessResult();
        }
        return ResultGenerator.genFailResult(saveResult);
    }

    @PutMapping("/items/{cartItemId}")
    public Result update(@PathVariable("cartItemId") Long cartItemId,
                         @RequestParam(value = "userId", required = false) Long userId,
                         @RequestBody Map<String, Object> payload,
                         HttpServletRequest request) {
        Long resolvedUserId = ApiUserResolver.resolveUserId(userId, request);
        beyeahShoppingCartItem item = new beyeahShoppingCartItem();
        item.setCartItemId(cartItemId);
        item.setUserId(resolvedUserId);
        item.setGoodsCount(ApiUserResolver.asInt(payload.get("goodsCount"), "goodsCount"));
        String updateResult = beyeahShoppingCartService.updatebeyeahCartItem(item);
        if (ServiceResultEnum.SUCCESS.getResult().equals(updateResult)) {
            return ResultGenerator.genSuccessResult();
        }
        return ResultGenerator.genFailResult(updateResult);
    }

    @DeleteMapping("/items/{cartItemId}")
    public Result delete(@PathVariable("cartItemId") Long cartItemId,
                         @RequestParam(value = "userId", required = false) Long userId,
                         HttpServletRequest request) {
        Long resolvedUserId = ApiUserResolver.resolveUserId(userId, request);
        boolean deleted = beyeahShoppingCartService.deleteById(cartItemId, resolvedUserId);
        if (deleted) {
            return ResultGenerator.genSuccessResult();
        }
        return ResultGenerator.genFailResult(ServiceResultEnum.OPERATE_ERROR.getResult());
    }
}

