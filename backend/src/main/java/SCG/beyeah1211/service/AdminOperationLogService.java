package SCG.beyeah1211.service;

import SCG.beyeah1211.dao.AdminOperationLogMapper;
import SCG.beyeah1211.entity.AdminOperationLog;
import SCG.beyeah1211.security.AdminAuthContext;
import SCG.beyeah1211.security.AdminPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Service
public class AdminOperationLogService {
    @Resource
    private AdminOperationLogMapper adminOperationLogMapper;
    @Resource
    private ObjectMapper objectMapper;

    public void log(HttpServletRequest request,
                    String action,
                    String resourceType,
                    String resourceId,
                    Object requestPayload,
                    int responseCode) {
        try {
            Object principalObj = request.getAttribute(AdminAuthContext.AUTH_ADMIN_PRINCIPAL_ATTR);
            if (!(principalObj instanceof AdminPrincipal)) {
                return;
            }
            AdminPrincipal principal = (AdminPrincipal) principalObj;

            AdminOperationLog log = new AdminOperationLog();
            log.setOperatorAdminId(principal.getAdminUserId());
            log.setAction(defaultText(action));
            log.setResourceType(defaultText(resourceType));
            log.setResourceId(defaultText(resourceId));
            log.setRequestJson(toJson(requestPayload));
            log.setResponseCode(responseCode);
            Object traceId = request.getAttribute("traceId");
            log.setTraceId(traceId == null ? "" : String.valueOf(traceId));
            log.setIp(resolveIp(request));
            log.setCreateTime(new Date());
            adminOperationLogMapper.insertSelective(log);
        } catch (Exception ignored) {
            // audit logging should never break business response
        }
    }

    private String toJson(Object payload) {
        if (payload == null) {
            return "";
        }
        try {
            String json = objectMapper.writeValueAsString(payload);
            return json.length() <= 2000 ? json : json.substring(0, 2000);
        } catch (Exception ignored) {
            return String.valueOf(payload);
        }
    }

    private String resolveIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.trim().isEmpty()) {
            int comma = forwarded.indexOf(',');
            return (comma > 0 ? forwarded.substring(0, comma) : forwarded).trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.trim().isEmpty()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    private String defaultText(String text) {
        return text == null ? "" : text.trim();
    }
}
