package SCG.beyeah1211.controller.api;

import SCG.beyeah1211.common.beyeahException;
import SCG.beyeah1211.security.AdminAuthContext;
import SCG.beyeah1211.security.AdminPrincipal;

import javax.servlet.http.HttpServletRequest;

final class AdminApiResolver {
    private AdminApiResolver() {
    }

    static AdminPrincipal resolvePrincipal(HttpServletRequest request) {
        Object principal = request.getAttribute(AdminAuthContext.AUTH_ADMIN_PRINCIPAL_ATTR);
        if (!(principal instanceof AdminPrincipal)) {
            beyeahException.fail("unauthorized admin");
        }
        return (AdminPrincipal) principal;
    }

    static Integer resolveAdminId(HttpServletRequest request) {
        return resolvePrincipal(request).getAdminUserId();
    }
}
