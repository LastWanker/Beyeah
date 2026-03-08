package SCG.beyeah1211.controller.api;

import SCG.beyeah1211.dao.MallUserMapper;
import SCG.beyeah1211.entity.MallUser;
import SCG.beyeah1211.util.Result;
import SCG.beyeah1211.util.ResultGenerator;
import SCG.beyeah1211.util.beyeahUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/api/v1/users")
public class UserApiController {
    @Resource
    private MallUserMapper mallUserMapper;

    @GetMapping("/me")
    public Result me(@RequestParam(value = "userId", required = false) Long userId,
                     HttpServletRequest request) {
        Long resolvedUserId = ApiUserResolver.resolveUserId(userId, request);
        MallUser user = mallUserMapper.selectByPrimaryKey(resolvedUserId);
        if (user == null) {
            return ResultGenerator.genFailResult("user not found");
        }
        return ResultGenerator.genSuccessResult(toUserData(user));
    }

    @PutMapping("/me")
    public Result updateMe(@RequestParam(value = "userId", required = false) Long userId,
                           @RequestBody Map<String, Object> payload,
                           HttpServletRequest request) {
        Long resolvedUserId = ApiUserResolver.resolveUserId(userId, request);
        MallUser user = mallUserMapper.selectByPrimaryKey(resolvedUserId);
        if (user == null) {
            return ResultGenerator.genFailResult("user not found");
        }
        Object nicknameRaw = payload.get("nickname");
        if (nicknameRaw != null && StringUtils.hasText(String.valueOf(nicknameRaw))) {
            user.setNickName(beyeahUtils.cleanString(String.valueOf(nicknameRaw)));
        }
        Object addressRaw = payload.get("address");
        if (addressRaw != null && StringUtils.hasText(String.valueOf(addressRaw))) {
            user.setAddress(beyeahUtils.cleanString(String.valueOf(addressRaw)));
        }
        if (mallUserMapper.updateByPrimaryKeySelective(user) <= 0) {
            return ResultGenerator.genFailResult("update failed");
        }
        MallUser updated = mallUserMapper.selectByPrimaryKey(resolvedUserId);
        return ResultGenerator.genSuccessResult(toUserData(updated));
    }

    private Map<String, Object> toUserData(MallUser user) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getUserId());
        data.put("userid", user.getUserId());
        data.put("username", user.getusername());
        data.put("nickname", user.getNickName());
        data.put("address", user.getAddress());
        return data;
    }
}

