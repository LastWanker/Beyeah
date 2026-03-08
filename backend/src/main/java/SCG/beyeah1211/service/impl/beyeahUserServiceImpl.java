package SCG.beyeah1211.service.impl;

import SCG.beyeah1211.common.Constants;
import SCG.beyeah1211.common.ServiceResultEnum;
import SCG.beyeah1211.controller.vo.beyeahUserVO;
import SCG.beyeah1211.dao.MallUserMapper;
import SCG.beyeah1211.entity.MallUser;
import SCG.beyeah1211.service.beyeahUserService;
import SCG.beyeah1211.util.BeanUtil;
import SCG.beyeah1211.util.MD5Util;
import SCG.beyeah1211.util.PageQueryUtil;
import SCG.beyeah1211.util.PageResult;
import SCG.beyeah1211.util.beyeahUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

@Service
public class beyeahUserServiceImpl implements beyeahUserService {

    @Autowired
    private MallUserMapper mallUserMapper;

    @Override
    public PageResult getbeyeahUsersPage(PageQueryUtil pageUtil) {
        List<MallUser> mallUsers = mallUserMapper.findMallUserList(pageUtil);
        int total = mallUserMapper.getTotalMallUsers(pageUtil);
        return new PageResult(mallUsers, total, pageUtil.getLimit(), pageUtil.getPage());
    }

    @Override
    public String register(String username, String password) throws SQLException {
        if (mallUserMapper.selectByusername(username) != null) {
            return ServiceResultEnum.SAME_LOGIN_NAME_EXIST.getResult();
        }
        MallUser registerUser = new MallUser();
        registerUser.setusername(username);
        registerUser.setNickName(username);
        registerUser.setIntroduceSign("default");
        registerUser.setAddress("default");
        registerUser.setIsDeleted((byte) 0);
        registerUser.setLockedFlag((byte) 0);
        registerUser.setCreateTime(new Date());
        registerUser.setPasswordMd5(MD5Util.MD5Encode(password, "UTF-8"));
        if (mallUserMapper.insertSelective(registerUser) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public String login(String username, String passwordMD5, HttpSession httpSession) {
        MallUser user = mallUserMapper.selectByusernameAndPasswd(username, passwordMD5);
        if (user != null && httpSession != null) {
            if (user.getLockedFlag() == 1) {
                return ServiceResultEnum.LOGIN_USER_LOCKED.getResult();
            }
            if (user.getNickName() != null && user.getNickName().length() > 10) {
                user.setNickName(user.getNickName().substring(0, 10) + "..");
            }
            beyeahUserVO beyeahUserVO = new beyeahUserVO();
            BeanUtil.copyProperties(user, beyeahUserVO);
            httpSession.setAttribute(Constants.MALL_USER_SESSION_KEY, beyeahUserVO);
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.LOGIN_ERROR.getResult();
    }

    @Override
    public beyeahUserVO updateUserInfo(MallUser mallUser, HttpSession httpSession) {
        beyeahUserVO userTemp = (beyeahUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        if (userTemp == null) {
            return null;
        }

        MallUser userFromDB = mallUserMapper.selectByPrimaryKey(userTemp.getUserId());
        if (userFromDB == null) {
            return null;
        }
        if (StringUtils.hasText(mallUser.getNickName())) {
            userFromDB.setNickName(beyeahUtils.cleanString(mallUser.getNickName()));
        }
        if (StringUtils.hasText(mallUser.getAddress())) {
            userFromDB.setAddress(beyeahUtils.cleanString(mallUser.getAddress()));
        }
        if (StringUtils.hasText(mallUser.getIntroduceSign())) {
            userFromDB.setIntroduceSign(beyeahUtils.cleanString(mallUser.getIntroduceSign()));
        }
        if (mallUserMapper.updateByPrimaryKeySelective(userFromDB) > 0) {
            beyeahUserVO beyeahUserVO = new beyeahUserVO();
            BeanUtil.copyProperties(userFromDB, beyeahUserVO);
            httpSession.setAttribute(Constants.MALL_USER_SESSION_KEY, beyeahUserVO);
            return beyeahUserVO;
        }
        return null;
    }

    @Override
    public Boolean lockUsers(Integer[] ids, int lockStatus) {
        if (ids.length < 1) {
            return false;
        }
        return mallUserMapper.lockUserBatch(ids, lockStatus) > 0;
    }

    @Override
    public MallUser getUserInfoFromDatabase(Long userID) {
        return mallUserMapper.selectByPrimaryKey(userID);
    }
}
