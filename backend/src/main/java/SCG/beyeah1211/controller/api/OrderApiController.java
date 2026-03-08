package SCG.beyeah1211.controller.api;

import SCG.beyeah1211.common.PayTypeEnum;
import SCG.beyeah1211.controller.vo.beyeahOrderDetailVO;
import SCG.beyeah1211.controller.vo.beyeahShoppingCartItemVO;
import SCG.beyeah1211.controller.vo.beyeahUserVO;
import SCG.beyeah1211.dao.MallUserMapper;
import SCG.beyeah1211.entity.MallUser;
import SCG.beyeah1211.entity.beyeahOrder;
import SCG.beyeah1211.entity.beyeahUserAddress;
import SCG.beyeah1211.service.beyeahAddressService;
import SCG.beyeah1211.service.beyeahOrderService;
import SCG.beyeah1211.service.beyeahShoppingCartService;
import SCG.beyeah1211.util.PageQueryUtil;
import SCG.beyeah1211.util.PageResult;
import SCG.beyeah1211.util.Result;
import SCG.beyeah1211.util.ResultGenerator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderApiController {
    @Resource
    private beyeahOrderService beyeahOrderService;
    @Resource
    private beyeahShoppingCartService beyeahShoppingCartService;
    @Resource
    private MallUserMapper mallUserMapper;
    @Resource
    private beyeahAddressService beyeahAddressService;

    @PostMapping
    public Result create(@RequestParam(value = "userId", required = false) Long userId,
                         @RequestBody Map<String, Object> payload,
                         @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKeyHeader,
                         HttpServletRequest request) {
        Long resolvedUserId = ApiUserResolver.resolveUserId(userId, request);
        MallUser user = mallUserMapper.selectByPrimaryKey(resolvedUserId);
        if (user == null) {
            return ResultGenerator.genFailResult("user not found");
        }

        String idempotencyKey = idempotencyKeyHeader;
        if (!StringUtils.hasText(idempotencyKey) && payload.get("idempotencyKey") != null) {
            idempotencyKey = String.valueOf(payload.get("idempotencyKey"));
        }
        if (!StringUtils.hasText(idempotencyKey)) {
            idempotencyKey = request.getHeader("X-Idempotency-Key");
        }
        if (StringUtils.hasText(idempotencyKey)) {
            beyeahOrder existingOrder = beyeahOrderService.getOrderByUserIdAndIdempotencyKey(resolvedUserId, idempotencyKey);
            if (existingOrder != null) {
                Map<String, Object> data = new HashMap<>();
                data.put("orderNo", existingOrder.getOrderNo());
                return ResultGenerator.genSuccessResult(data);
            }
        }

        Object goodsRaw = payload.get("goods");
        if (!(goodsRaw instanceof List) || ((List<?>) goodsRaw).isEmpty()) {
            return ResultGenerator.genFailResult("goods is required");
        }

        List<beyeahShoppingCartItemVO> userCartItems = beyeahShoppingCartService.getMyShoppingCartItems(resolvedUserId);
        Map<Long, beyeahShoppingCartItemVO> cartItemByGoodsId = userCartItems.stream()
                .collect(Collectors.toMap(beyeahShoppingCartItemVO::getGoodsId, item -> item, (a, b) -> a));

        List<beyeahShoppingCartItemVO> orderItems = new ArrayList<>();
        for (Object one : (List<?>) goodsRaw) {
            if (!(one instanceof Map)) {
                return ResultGenerator.genFailResult("invalid goods item");
            }
            Map<?, ?> row = (Map<?, ?>) one;
            Long goodsId = ApiUserResolver.asLong(row.get("goodsId"), "goodsId");
            Integer goodsCount = ApiUserResolver.asInt(row.get("goodsCount"), "goodsCount");
            beyeahShoppingCartItemVO cartItem = cartItemByGoodsId.get(goodsId);
            if (cartItem == null) {
                return ResultGenerator.genFailResult("goods " + goodsId + " not in cart");
            }
            beyeahShoppingCartItemVO orderItem = new beyeahShoppingCartItemVO();
            orderItem.setCartItemId(cartItem.getCartItemId());
            orderItem.setGoodsId(cartItem.getGoodsId());
            orderItem.setGoodsName(cartItem.getGoodsName());
            orderItem.setGoodsCoverImg(cartItem.getGoodsCoverImg());
            orderItem.setSellingPrice(cartItem.getSellingPrice());
            orderItem.setGoodsCount(goodsCount);
            orderItems.add(orderItem);
        }

        String receiverName;
        String receiverPhone;
        String receiverAddress;

        Long addressId = null;
        Object addressIdRaw = payload.get("addressId");
        if (addressIdRaw != null) {
            addressId = ApiUserResolver.asLong(addressIdRaw, "addressId");
        }

        beyeahUserAddress selectedAddress = null;
        if (addressId != null && addressId > 0) {
            selectedAddress = beyeahAddressService.getAddress(resolvedUserId, addressId);
            if (selectedAddress == null) {
                return ResultGenerator.genFailResult("address not found");
            }
        } else {
            selectedAddress = beyeahAddressService.getDefaultAddress(resolvedUserId);
        }

        if (selectedAddress != null) {
            receiverName = selectedAddress.getConsignee();
            receiverPhone = selectedAddress.getPhone();
            receiverAddress = formatAddress(selectedAddress);
        } else {
            Object addressRaw = payload.get("address");
            if (addressRaw == null || String.valueOf(addressRaw).trim().isEmpty()) {
                receiverAddress = user.getAddress();
            } else {
                receiverAddress = String.valueOf(addressRaw).trim();
            }
            receiverName = user.getNickName();
            receiverPhone = user.getusername();
        }

        if (!StringUtils.hasText(receiverAddress)) {
            return ResultGenerator.genFailResult("address is required");
        }
        if (receiverAddress.length() > 100) {
            return ResultGenerator.genFailResult("address is too long");
        }

        beyeahUserVO userVO = new beyeahUserVO();
        userVO.setUserId(user.getUserId());
        userVO.setNickName(receiverName);
        userVO.setusername(receiverPhone);
        userVO.setAddress(receiverAddress);

        String orderNo = beyeahOrderService.saveOrder(userVO, orderItems, idempotencyKey);
        Map<String, Object> data = new HashMap<>();
        data.put("orderNo", orderNo);
        return ResultGenerator.genSuccessResult(data);
    }

    @GetMapping
    public Result list(@RequestParam(value = "userId", required = false) Long userId,
                       @RequestParam(value = "page", defaultValue = "1") Integer page,
                       @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
                       HttpServletRequest request) {
        Long resolvedUserId = ApiUserResolver.resolveUserId(userId, request);
        Map<String, Object> params = new HashMap<>();
        params.put("userId", resolvedUserId);
        params.put("page", page);
        params.put("limit", pageSize);
        PageQueryUtil pageQueryUtil = new PageQueryUtil(params);
        PageResult pageResult = beyeahOrderService.getMyOrders(pageQueryUtil);
        return ResultGenerator.genSuccessResult(pageResult);
    }

    @GetMapping("/{orderNo}")
    public Result detail(@PathVariable("orderNo") String orderNo,
                         @RequestParam(value = "userId", required = false) Long userId,
                         HttpServletRequest request) {
        Long resolvedUserId = ApiUserResolver.resolveUserId(userId, request);
        beyeahOrderDetailVO detailVO = beyeahOrderService.getOrderDetailByOrderNo(orderNo, resolvedUserId);
        return ResultGenerator.genSuccessResult(detailVO);
    }

    @PostMapping("/{orderNo}/pay")
    public Result mockPay(@PathVariable("orderNo") String orderNo,
                          @RequestParam(value = "userId", required = false) Long userId,
                          @RequestBody(required = false) Map<String, Object> payload,
                          HttpServletRequest request) {
        Long resolvedUserId = ApiUserResolver.resolveUserId(userId, request);
        beyeahOrder order = beyeahOrderService.getbeyeahOrderByOrderNo(orderNo);
        if (order == null) {
            return ResultGenerator.genFailResult("order not found");
        }
        if (!resolvedUserId.equals(order.getUserId())) {
            return ResultGenerator.genFailResult("no permission");
        }

        int payType = 2;
        if (payload != null && payload.get("payType") != null) {
            payType = ApiUserResolver.asInt(payload.get("payType"), "payType");
        }
        if (PayTypeEnum.getPayTypeEnumByType(payType) == null) {
            return ResultGenerator.genFailResult("invalid payType");
        }

        String result = beyeahOrderService.paySuccess(orderNo, payType);
        if (!"success".equals(result)) {
            return ResultGenerator.genFailResult(result);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("orderNo", orderNo);
        data.put("payType", payType);
        data.put("paid", true);
        return ResultGenerator.genSuccessResult(data);
    }

    private String formatAddress(beyeahUserAddress address) {
        StringBuilder sb = new StringBuilder();
        appendAddressPart(sb, address.getProvince());
        appendAddressPart(sb, address.getCity());
        appendAddressPart(sb, address.getDistrict());
        appendAddressPart(sb, address.getDetail());
        return sb.toString();
    }

    private void appendAddressPart(StringBuilder sb, String part) {
        if (!StringUtils.hasText(part)) {
            return;
        }
        if (sb.length() > 0) {
            sb.append(' ');
        }
        sb.append(part.trim());
    }
}

