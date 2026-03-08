
package SCG.beyeah1211.controller.common;

//import cn.hutool.captcha.CaptchaUtil;
//import cn.hutool.captcha.ShearCaptcha;
import SCG.beyeah1211.common.Constants;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Controller
public class CommonController {
//
//    @GetMapping("/common/kaptcha")
//    public void defaultKaptcha(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
//        httpServletResponse.setHeader("Cache-Control", "no-store");
//        httpServletResponse.setHeader("Pragma", "no-cache");
//        httpServletResponse.setDateHeader("Expires", 0);
//        httpServletResponse.setContentType("image/png");
//
//        ShearCaptcha shearCaptcha= CaptchaUtil.createShearCaptcha(150, 30, 4, 2);
//
//        // 楠岃瘉鐮佸瓨鍏ession
//        httpServletRequest.getSession().setAttribute("verifyCode", shearCaptcha);
//
//        // 杈撳嚭鍥剧墖娴?//        shearCaptcha.write(httpServletResponse.getOutputStream());
//    }
//
//    @GetMapping("/common/mall/kaptcha")
//    public void mallKaptcha(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
//        httpServletResponse.setHeader("Cache-Control", "no-store");
//        httpServletResponse.setHeader("Pragma", "no-cache");
//        httpServletResponse.setDateHeader("Expires", 0);
//        httpServletResponse.setContentType("image/png");
//
//        ShearCaptcha shearCaptcha= CaptchaUtil.createShearCaptcha(110, 40, 4, 2);
//
//        // 楠岃瘉鐮佸瓨鍏ession
//        httpServletRequest.getSession().setAttribute(Constants.MALL_VERIFY_CODE_KEY, shearCaptcha);
//
//        // 杈撳嚭鍥剧墖娴?//        shearCaptcha.write(httpServletResponse.getOutputStream());
//    }
}

