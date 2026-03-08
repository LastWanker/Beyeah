package SCG.beyeah1211.security;

import SCG.beyeah1211.util.Result;
import SCG.beyeah1211.util.ResultGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {
    @Resource
    private AdminTokenService adminTokenService;
    @Resource
    private AdminRbacService adminRbacService;
    @Resource
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        Integer adminId = adminTokenService.resolveAdminIdFromAuthorization(request.getHeader("Authorization"));
        if (adminId == null) {
            return writeError(response, HttpServletResponse.SC_UNAUTHORIZED, ResultGenerator.genErrorResult(401, "UNAUTHORIZED_ADMIN"));
        }

        AdminPrincipal principal = adminRbacService.loadPrincipal(adminId);
        if (principal == null) {
            return writeError(response, HttpServletResponse.SC_UNAUTHORIZED, ResultGenerator.genErrorResult(401, "UNAUTHORIZED_ADMIN"));
        }

        request.setAttribute(AdminAuthContext.AUTH_ADMIN_ID_ATTR, adminId);
        request.setAttribute(AdminAuthContext.AUTH_ADMIN_PRINCIPAL_ATTR, principal);
        return true;
    }

    private boolean writeError(HttpServletResponse response, int status, Result<?> body) throws Exception {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(body));
        return false;
    }
}
