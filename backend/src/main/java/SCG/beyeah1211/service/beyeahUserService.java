package SCG.beyeah1211.service;

import SCG.beyeah1211.controller.vo.beyeahUserVO;
import SCG.beyeah1211.entity.MallUser;
import SCG.beyeah1211.util.PageQueryUtil;
import SCG.beyeah1211.util.PageResult;
import javax.servlet.http.HttpSession;
import java.sql.SQLException;

public interface beyeahUserService {

    /**
     * 后台分页
     *
     * @param pageUtil
     * @return
     */
    PageResult getbeyeahUsersPage(PageQueryUtil pageUtil);


    /**
     * 用户注册
     *
     * @param username
     * @param password
     * @return
     */
    String register(String username, String password) throws SQLException;

    /**
     * 登录
     *
     * @param username
     * @param passwordMD5
     * @param httpSession
     * @return
     */
    String login(String username, String passwordMD5, HttpSession httpSession);

    /**
     * 用户信息修改并返回最新的用户信息
     *
     * @param mallUser
     * @return
     */
    beyeahUserVO updateUserInfo(MallUser mallUser, HttpSession httpSession);

    /**
     * 用户禁用与解除禁用(0-未锁定 1-已锁定)
     *
     * @param ids
     * @param lockStatus
     * @return
     */
    Boolean lockUsers(Integer[] ids, int lockStatus);

    MallUser getUserInfoFromDatabase(Long userIDLong);
}
