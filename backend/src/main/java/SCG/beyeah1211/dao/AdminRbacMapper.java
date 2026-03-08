package SCG.beyeah1211.dao;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AdminRbacMapper {
    List<String> selectRoleCodesByAdminUserId(@Param("adminUserId") Integer adminUserId);

    List<String> selectPermissionCodesByAdminUserId(@Param("adminUserId") Integer adminUserId);
}
