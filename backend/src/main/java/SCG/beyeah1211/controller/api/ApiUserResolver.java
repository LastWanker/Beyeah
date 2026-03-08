package SCG.beyeah1211.controller.api;

import SCG.beyeah1211.common.beyeahException;
import SCG.beyeah1211.security.AuthContext;

import javax.servlet.http.HttpServletRequest;

final class ApiUserResolver {
    private ApiUserResolver() {
    }

    static Long resolveUserId(Long userId, HttpServletRequest request) {
        Object authenticated = request.getAttribute(AuthContext.AUTH_USER_ID_ATTR);
        if (authenticated == null) {
            beyeahException.fail("unauthorized");
        }
        long resolved;
        if (authenticated instanceof Number) {
            resolved = ((Number) authenticated).longValue();
        } else {
            try {
                resolved = Long.parseLong(String.valueOf(authenticated));
            } catch (NumberFormatException e) {
                beyeahException.fail("unauthorized");
                return 0L;
            }
        }
        if (resolved <= 0) {
            beyeahException.fail("unauthorized");
        }
        if (userId != null && userId > 0 && !userId.equals(resolved)) {
            beyeahException.fail("userId mismatch");
        }
        return resolved;
    }

    static Long asLong(Object value, String fieldName) {
        if (value == null) {
            beyeahException.fail(fieldName + " is required");
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            beyeahException.fail("invalid " + fieldName);
            return 0L;
        }
    }

    static Integer asInt(Object value, String fieldName) {
        if (value == null) {
            beyeahException.fail(fieldName + " is required");
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            beyeahException.fail("invalid " + fieldName);
            return 0;
        }
    }
}

