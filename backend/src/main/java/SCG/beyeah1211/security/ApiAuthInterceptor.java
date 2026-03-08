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
public class ApiAuthInterceptor implements HandlerInterceptor {
    @Resource
    private AuthTokenService authTokenService;
    @Resource
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        Long userId = authTokenService.resolveUserIdFromAuthorization(request.getHeader("Authorization"));
        if (userId == null) {
            Result<?> unauthorized = ResultGenerator.genErrorResult(401, "UNAUTHORIZED");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(unauthorized));
            return false;
        }
        request.setAttribute(AuthContext.AUTH_USER_ID_ATTR, userId);
        return true;
    }
}
