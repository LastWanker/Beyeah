package SCG.beyeah1211.controller.api;

import SCG.beyeah1211.common.AdminPermissionCodes;
import SCG.beyeah1211.security.RequirePermission;
import SCG.beyeah1211.service.AdminOperationLogService;
import SCG.beyeah1211.service.beyeahUserService;
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
@RequestMapping("/api/v1/admin/users")
public class AdminUsersApiController {
    @Resource
    private beyeahUserService beyeahUserService;
    @Resource
    private AdminOperationLogService adminOperationLogService;

    @GetMapping
    @RequirePermission(AdminPermissionCodes.USER_READ)
    public Result list(@RequestParam(value = "page", defaultValue = "1") Integer page,
                       @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        Map<String, Object> params = new HashMap<>();
        params.put("page", page);
        params.put("limit", pageSize);
        PageResult result = beyeahUserService.getbeyeahUsersPage(new PageQueryUtil(params));
        return ResultGenerator.genSuccessResult(result);
    }

    @PutMapping("/{userId}/lock")
    @RequirePermission(AdminPermissionCodes.USER_LOCK)
    public Result lock(@PathVariable("userId") Integer userId,
                       @RequestBody Map<String, Object> payload,
                       HttpServletRequest request) {
        if (userId == null || userId <= 0) {
            return ResultGenerator.genFailResult("invalid userId");
        }
        int lockStatus = ApiUserResolver.asInt(payload.get("lockStatus"), "lockStatus");
        if (lockStatus != 0 && lockStatus != 1) {
            return ResultGenerator.genFailResult("invalid lockStatus");
        }
        boolean success = beyeahUserService.lockUsers(new Integer[]{userId}, lockStatus);
        Result result = success ? ResultGenerator.genSuccessResult() : ResultGenerator.genFailResult("lock user failed");
        adminOperationLogService.log(
                request,
                lockStatus == 1 ? "user.lock" : "user.unlock",
                "user",
                String.valueOf(userId),
                payload,
                result.getResultCode()
        );
        return result;
    }
}
