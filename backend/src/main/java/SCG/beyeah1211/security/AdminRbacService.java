package SCG.beyeah1211.security;

import SCG.beyeah1211.dao.AdminRbacMapper;
import SCG.beyeah1211.dao.AdminUserMapper;
import SCG.beyeah1211.entity.AdminUser;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AdminRbacService {
    @Resource
    private AdminUserMapper adminUserMapper;
    @Resource
    private AdminRbacMapper adminRbacMapper;

    public AdminPrincipal loadPrincipal(Integer adminUserId) {
        if (adminUserId == null || adminUserId <= 0) {
            return null;
        }
        AdminUser adminUser = adminUserMapper.selectByPrimaryKey(adminUserId);
        if (adminUser == null || (adminUser.getLocked() != null && adminUser.getLocked() != 0)) {
            return null;
        }

        List<String> roleCodes = adminRbacMapper.selectRoleCodesByAdminUserId(adminUserId);
        List<String> permissionCodes = adminRbacMapper.selectPermissionCodesByAdminUserId(adminUserId);
        if (CollectionUtils.isEmpty(roleCodes)) {
            return null;
        }

        AdminPrincipal principal = new AdminPrincipal();
        principal.setAdminUserId(adminUser.getAdminUserId());
        principal.setLoginUserName(adminUser.getLoginUserName());
        principal.setNickName(adminUser.getNickName());
        principal.setRoles(new HashSet<>(roleCodes));
        principal.setPermissions(new HashSet<>(permissionCodes == null ? Set.of() : permissionCodes));
        return principal;
    }

    public boolean hasPermission(AdminPrincipal principal, String permissionCode) {
        if (principal == null || permissionCode == null || permissionCode.trim().isEmpty()) {
            return false;
        }
        if (principal.isSuperAdmin()) {
            return true;
        }
        return principal.hasPermission(permissionCode.trim());
    }
}
