package SCG.beyeah1211.security;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class AdminPrincipal {
    private Integer adminUserId;
    private String loginUserName;
    private String nickName;
    private Set<String> roles = new HashSet<>();
    private Set<String> permissions = new HashSet<>();

    public Integer getAdminUserId() {
        return adminUserId;
    }

    public void setAdminUserId(Integer adminUserId) {
        this.adminUserId = adminUserId;
    }

    public String getLoginUserName() {
        return loginUserName;
    }

    public void setLoginUserName(String loginUserName) {
        this.loginUserName = loginUserName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public Set<String> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles == null ? new HashSet<>() : new HashSet<>(roles);
    }

    public Set<String> getPermissions() {
        return Collections.unmodifiableSet(permissions);
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions == null ? new HashSet<>() : new HashSet<>(permissions);
    }

    public boolean hasRole(String roleCode) {
        return roles.contains(roleCode);
    }

    public boolean hasPermission(String permissionCode) {
        return permissions.contains(permissionCode);
    }

    public boolean isSuperAdmin() {
        return hasRole("SUPER_ADMIN");
    }
}
