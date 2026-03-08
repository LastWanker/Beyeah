package SCG.beyeah1211.controller.api;

import SCG.beyeah1211.entity.AdminUser;
import SCG.beyeah1211.security.AdminPrincipal;
import SCG.beyeah1211.security.AdminRbacService;
import SCG.beyeah1211.security.AdminTokenService;
import SCG.beyeah1211.service.AdminUserService;
import SCG.beyeah1211.util.Result;
import SCG.beyeah1211.util.ResultGenerator;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/auth")
public class AdminAuthApiController {
    @Resource
    private AdminUserService adminUserService;
    @Resource
    private AdminTokenService adminTokenService;
    @Resource
    private AdminRbacService adminRbacService;

    @PostMapping("/login")
    public Result login(@RequestBody Map<String, Object> payload) {
        String userName = payload.get("userName") == null ? "" : String.valueOf(payload.get("userName")).trim();
        String password = payload.get("password") == null ? "" : String.valueOf(payload.get("password"));
        if (!StringUtils.hasText(userName) || !StringUtils.hasText(password)) {
            return ResultGenerator.genFailResult("userName and password are required");
        }

        AdminUser adminUser = adminUserService.login(userName, password);
        if (adminUser == null) {
            return ResultGenerator.genErrorResult(401, "INVALID_ADMIN_CREDENTIALS");
        }

        AdminPrincipal principal = adminRbacService.loadPrincipal(adminUser.getAdminUserId());
        if (principal == null) {
            return ResultGenerator.genErrorResult(403, "ADMIN_ROLE_NOT_BOUND");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("adminId", adminUser.getAdminUserId());
        data.put("userName", adminUser.getLoginUserName());
        data.put("nickName", adminUser.getNickName());
        data.put("roles", principal.getRoles());
        data.put("permissions", principal.getPermissions());
        data.put("token", adminTokenService.issueToken(adminUser.getAdminUserId()));
        data.put("tokenType", "Bearer");
        return ResultGenerator.genSuccessResult(data);
    }

    @GetMapping("/me")
    public Result me(HttpServletRequest request) {
        AdminPrincipal principal = AdminApiResolver.resolvePrincipal(request);
        Map<String, Object> data = new HashMap<>();
        data.put("adminId", principal.getAdminUserId());
        data.put("userName", principal.getLoginUserName());
        data.put("nickName", principal.getNickName());
        data.put("roles", principal.getRoles());
        data.put("permissions", principal.getPermissions());
        return ResultGenerator.genSuccessResult(data);
    }
}
