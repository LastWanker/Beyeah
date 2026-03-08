package SCG.beyeah1211.controller.api;

import SCG.beyeah1211.common.ServiceResultEnum;
import SCG.beyeah1211.dao.MallUserMapper;
import SCG.beyeah1211.entity.MallUser;
import SCG.beyeah1211.security.AuthTokenService;
import SCG.beyeah1211.security.LoginAttemptService;
import SCG.beyeah1211.service.beyeahUserService;
import SCG.beyeah1211.util.MD5Util;
import SCG.beyeah1211.util.Result;
import SCG.beyeah1211.util.ResultGenerator;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthApiController {
    private static final int PASSWORD_MIN_LENGTH = 8;

    @Resource
    private beyeahUserService beyeahUserService;
    @Resource
    private MallUserMapper mallUserMapper;
    @Resource
    private AuthTokenService authTokenService;
    @Resource
    private LoginAttemptService loginAttemptService;

    @PostMapping("/register")
    public Result register(@RequestBody Map<String, Object> payload) throws SQLException {
        String username = toText(payload.get("username"));
        String password = toText(payload.get("password"));
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            return ResultGenerator.genFailResult("username/password is required");
        }
        if (!isValidUsername(username)) {
            return ResultGenerator.genFailResult("invalid username");
        }
        if (!isStrongPassword(password)) {
            return ResultGenerator.genFailResult("password must be at least 8 chars with letters and numbers");
        }
        String registerResult = beyeahUserService.register(username, password);
        if (!ServiceResultEnum.SUCCESS.getResult().equals(registerResult)) {
            return ResultGenerator.genFailResult(registerResult);
        }
        MallUser user = mallUserMapper.selectByusername(username);
        Map<String, Object> data = new HashMap<>();
        if (user != null) {
            data.put("userId", user.getUserId());
            data.put("userid", user.getUserId());
            data.put("token", authTokenService.issueToken(user.getUserId()));
            data.put("tokenType", "Bearer");
        }
        return ResultGenerator.genSuccessResult(data);
    }

    @PostMapping("/login")
    public Result login(@RequestBody Map<String, Object> payload,
                        HttpServletRequest request,
                        HttpSession session) throws UnsupportedEncodingException {
        String username = toText(payload.get("username"));
        String password = toText(payload.get("password"));
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            return ResultGenerator.genFailResult("username/password is required");
        }
        String clientIp = resolveClientIp(request);
        if (loginAttemptService.isBlocked(clientIp, username)) {
            return ResultGenerator.genErrorResult(429, "too many failed attempts, retry later");
        }
        String loginResult = beyeahUserService.login(username, MD5Util.MD5Encode(password, "UTF-8"), session);
        if (!ServiceResultEnum.SUCCESS.getResult().equals(loginResult)) {
            loginAttemptService.onFailure(clientIp, username);
            return ResultGenerator.genFailResult(loginResult);
        }
        loginAttemptService.onSuccess(clientIp, username);
        MallUser user = mallUserMapper.selectByusername(username);
        Map<String, Object> data = new HashMap<>();
        if (user != null) {
            data.put("userId", user.getUserId());
            data.put("userid", user.getUserId());
            data.put("username", user.getusername());
            data.put("nickname", user.getNickName());
            data.put("address", user.getAddress());
            data.put("token", authTokenService.issueToken(user.getUserId()));
            data.put("tokenType", "Bearer");
        }
        return ResultGenerator.genSuccessResult(data);
    }

    private String toText(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }

    private boolean isStrongPassword(String password) {
        if (!StringUtils.hasText(password) || password.length() < PASSWORD_MIN_LENGTH) {
            return false;
        }
        boolean hasLetter = false;
        boolean hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }
        return hasLetter && hasDigit;
    }

    private boolean isValidUsername(String username) {
        return username.length() >= 3 && username.length() <= 32;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xff)) {
            int commaIndex = xff.indexOf(',');
            return commaIndex > 0 ? xff.substring(0, commaIndex).trim() : xff.trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(realIp)) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}

