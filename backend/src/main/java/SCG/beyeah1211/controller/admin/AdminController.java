/**
 * 涓ヨ們澹版槑锛? * 寮€婧愮増鏈鍔″繀淇濈暀姝ゆ敞閲婂ご淇℃伅锛岃嫢鍒犻櫎鎴戞柟灏嗕繚鐣欐墍鏈夋硶寰嬭矗浠昏拷绌讹紒
 * 鏈郴缁熷凡鐢宠杞欢钁椾綔鏉冿紝鍙楀浗瀹剁増鏉冨眬鐭ヨ瘑浜ф潈浠ュ強鍥藉璁＄畻鏈鸿蒋浠惰憲浣滄潈淇濇姢锛? * 鍙甯稿垎浜拰瀛︿範婧愮爜锛屼笉寰楃敤浜庤繚娉曠姱缃椿鍔紝杩濊€呭繀绌讹紒
 * Copyright (c) 2019-2020 鍗佷笁 all rights reserved.
 * 鐗堟潈鎵€鏈夛紝渚垫潈蹇呯┒锛? */
package SCG.beyeah1211.controller.admin;

//import cn.hutool.captcha.ShearCaptcha;
import SCG.beyeah1211.common.ServiceResultEnum;
import SCG.beyeah1211.entity.AdminUser;
import SCG.beyeah1211.service.AdminUserService;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


@Controller
@RequestMapping("/admin")
public class AdminController {

    @Resource
    private AdminUserService adminUserService;

    @GetMapping({"/login"})
    public String login() {
        return "admin/login";
    }

    @GetMapping({"/test"})
    public String test() {
        return "admin/test";
    }


    @GetMapping({"", "/", "/index", "/index.html"})
    public String index(HttpServletRequest request) {
        request.setAttribute("path", "index");
        return "admin/index";
    }

    @PostMapping(value = "/login")
    public String login(@RequestParam("userName") String userName,
                        @RequestParam("password") String password,
                       /* @RequestParam("verifyCode") String verifyCode,*/
                        HttpSession session) {
      /*  if (!StringUtils.hasText(verifyCode)) {
            session.setAttribute("errorMsg", "楠岃瘉鐮佷笉鑳戒负绌");
            return "admin/login";
        }*/
        if (!StringUtils.hasText(userName) || !StringUtils.hasText(password)) {
            session.setAttribute("errorMsg", "鐢ㄦ埛鍚嶆垨瀵嗙爜涓嶈兘涓虹┖");
            return "admin/login";
        }
//        ShearCaptcha shearCaptcha = (ShearCaptcha) session.getAttribute("verifyCode");
//        if (shearCaptcha == null || !shearCaptcha.verify(verifyCode)) {
//            session.setAttribute("errorMsg", "楠岃瘉鐮侀敊璇");
//            return "admin/login";
//        }
        AdminUser adminUser = adminUserService.login(userName, password);
        if (adminUser != null) {
            session.setAttribute("loginUser", adminUser.getNickName());
            session.setAttribute("loginUserId", adminUser.getAdminUserId());
            //session杩囨湡鏃堕棿璁剧疆涓?200绉?鍗充袱灏忔椂
            //session.setMaxInactiveInterval(60 * 60 * 2);
            return "redirect:/admin/index";
        } else {
            session.setAttribute("errorMsg", "鐧诲綍澶辫触");
            return "admin/login";
        }
    }

    @GetMapping("/profile")
    public String profile(HttpServletRequest request) {
        Integer loginUserId = (int) request.getSession().getAttribute("loginUserId");
        AdminUser adminUser = adminUserService.getUserDetailById(loginUserId);
        if (adminUser == null) {
            return "admin/login";
        }
        request.setAttribute("path", "profile");
        request.setAttribute("loginUserName", adminUser.getLoginUserName());
        request.setAttribute("nickname", adminUser.getNickName());
        return "admin/profile";
    }

    @PostMapping("/profile/password")
    @ResponseBody
    public String passwordUpdate(HttpServletRequest request, @RequestParam("originalPassword") String originalPassword,
                                 @RequestParam("newPassword") String newPassword) {
        if (!StringUtils.hasText(originalPassword) || !StringUtils.hasText(newPassword)) {
            return "鍙傛暟涓嶈兘涓虹┖";
        }
        Integer loginUserId = (int) request.getSession().getAttribute("loginUserId");
        if (adminUserService.updatePassword(loginUserId, originalPassword, newPassword)) {
            //淇敼鎴愬姛鍚庢竻绌簊ession涓殑鏁版嵁锛屽墠绔帶鍒惰烦杞嚦鐧诲綍椤?            request.getSession().removeAttribute("loginUserId");
            request.getSession().removeAttribute("loginUser");
            request.getSession().removeAttribute("errorMsg");
            return ServiceResultEnum.SUCCESS.getResult();
        } else {
            return "淇敼澶辫触";
        }
    }

    @PostMapping("/profile/name")
    @ResponseBody
    public String nameUpdate(HttpServletRequest request, @RequestParam("loginUserName") String loginUserName,
                             @RequestParam("nickname") String nickname) {
        if (!StringUtils.hasText(loginUserName) || !StringUtils.hasText(nickname)) {
            return "鍙傛暟涓嶈兘涓虹┖";
        }
        Integer loginUserId = (int) request.getSession().getAttribute("loginUserId");
        if (adminUserService.updateName(loginUserId, loginUserName, nickname)) {
            return ServiceResultEnum.SUCCESS.getResult();
        } else {
            return "淇敼澶辫触";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        request.getSession().removeAttribute("loginUserId");
        request.getSession().removeAttribute("loginUser");
        request.getSession().removeAttribute("errorMsg");
        return "admin/login";
    }
}

