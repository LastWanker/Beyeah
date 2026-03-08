package SCG.beyeah1211.controller.api;

import SCG.beyeah1211.entity.beyeahUserAddress;
import SCG.beyeah1211.service.beyeahAddressService;
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
@RequestMapping("/api/v1/addresses")
public class AddressApiController {
    @Resource
    private beyeahAddressService beyeahAddressService;

    @GetMapping
    public Result list(@RequestParam(value = "userId", required = false) Long userId,
                       HttpServletRequest request) {
        Long resolvedUserId = ApiUserResolver.resolveUserId(userId, request);
        List<beyeahUserAddress> addresses = beyeahAddressService.getMyAddresses(resolvedUserId);
        return ResultGenerator.genSuccessResult(addresses);
    }

    @PostMapping
    public Result create(@RequestParam(value = "userId", required = false) Long userId,
                         @RequestBody Map<String, Object> payload,
                         HttpServletRequest request) {
        Long resolvedUserId = ApiUserResolver.resolveUserId(userId, request);
        beyeahUserAddress created = beyeahAddressService.createAddress(resolvedUserId, toAddress(payload));
        return ResultGenerator.genSuccessResult(created);
    }

    @PutMapping("/{addressId}")
    public Result update(@PathVariable("addressId") Long addressId,
                         @RequestParam(value = "userId", required = false) Long userId,
                         @RequestBody Map<String, Object> payload,
                         HttpServletRequest request) {
        Long resolvedUserId = ApiUserResolver.resolveUserId(userId, request);
        beyeahUserAddress updated = beyeahAddressService.updateAddress(resolvedUserId, addressId, toAddress(payload));
        return ResultGenerator.genSuccessResult(updated);
    }

    @DeleteMapping("/{addressId}")
    public Result delete(@PathVariable("addressId") Long addressId,
                         @RequestParam(value = "userId", required = false) Long userId,
                         HttpServletRequest request) {
        Long resolvedUserId = ApiUserResolver.resolveUserId(userId, request);
        beyeahAddressService.deleteAddress(resolvedUserId, addressId);
        return ResultGenerator.genSuccessResult();
    }

    @PutMapping("/{addressId}/default")
    public Result setDefault(@PathVariable("addressId") Long addressId,
                             @RequestParam(value = "userId", required = false) Long userId,
                             HttpServletRequest request) {
        Long resolvedUserId = ApiUserResolver.resolveUserId(userId, request);
        beyeahUserAddress updated = beyeahAddressService.setDefaultAddress(resolvedUserId, addressId);
        return ResultGenerator.genSuccessResult(updated);
    }

    private beyeahUserAddress toAddress(Map<String, Object> payload) {
        beyeahUserAddress address = new beyeahUserAddress();
        address.setConsignee(text(payload.get("consignee")));
        address.setPhone(text(payload.get("phone")));
        address.setProvince(text(payload.get("province")));
        address.setCity(text(payload.get("city")));
        address.setDistrict(text(payload.get("district")));
        address.setDetail(text(payload.get("detail")));
        Object defaultRaw = payload.get("isDefault");
        if (defaultRaw != null) {
            address.setIsDefault((byte) (ApiUserResolver.asInt(defaultRaw, "isDefault") > 0 ? 1 : 0));
        }
        return address;
    }

    private String text(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
