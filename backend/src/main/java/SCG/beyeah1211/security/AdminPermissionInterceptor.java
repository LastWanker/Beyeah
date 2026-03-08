package SCG.beyeah1211.security;

import SCG.beyeah1211.util.Result;
import SCG.beyeah1211.util.ResultGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

@Component
public class AdminPermissionInterceptor implements HandlerInterceptor {
    @Resource
    private AdminRbacService adminRbacService;
    @Resource
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequirePermission requirePermission = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), RequirePermission.class);
        if (requirePermission == null) {
            requirePermission = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), RequirePermission.class);
        }
        if (requirePermission == null) {
            return true;
        }

        Object principalObj = request.getAttribute(AdminAuthContext.AUTH_ADMIN_PRINCIPAL_ATTR);
        if (!(principalObj instanceof AdminPrincipal)) {
            return writeError(response, HttpServletResponse.SC_UNAUTHORIZED, ResultGenerator.genErrorResult(401, "UNAUTHORIZED_ADMIN"));
        }

        AdminPrincipal principal = (AdminPrincipal) principalObj;
        if (!adminRbacService.hasPermission(principal, requirePermission.value())) {
            return writeError(response, HttpServletResponse.SC_FORBIDDEN, ResultGenerator.genErrorResult(403, "FORBIDDEN"));
        }
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
