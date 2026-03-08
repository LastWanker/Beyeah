/**
 * 涓ヨ們澹版槑锛? * 寮€婧愮増鏈鍔″繀淇濈暀姝ゆ敞閲婂ご淇℃伅锛岃嫢鍒犻櫎鎴戞柟灏嗕繚鐣欐墍鏈夋硶寰嬭矗浠昏拷绌讹紒
 * 鏈郴缁熷凡鐢宠杞欢钁椾綔鏉冿紝鍙楀浗瀹剁増鏉冨眬鐭ヨ瘑浜ф潈浠ュ強鍥藉璁＄畻鏈鸿蒋浠惰憲浣滄潈淇濇姢锛? * 鍙甯稿垎浜拰瀛︿範婧愮爜锛屼笉寰楃敤浜庤繚娉曠姱缃椿鍔紝杩濊€呭繀绌讹紒
 * Copyright (c) 2019-2020 鍗佷笁 all rights reserved.
 * 鐗堟潈鎵€鏈夛紝渚垫潈蹇呯┒锛? */
package SCG.beyeah1211.controller.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
public class ErrorPageController implements ErrorViewResolver {

    private static ErrorPageController errorPageController;

    @Autowired
    private ErrorAttributes errorAttributes;

    public ErrorPageController(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    public ErrorPageController() {
        if (errorPageController == null) {
            errorPageController = new ErrorPageController(errorAttributes);
        }
    }

    @Override
    public ModelAndView resolveErrorView(HttpServletRequest request, HttpStatus status, Map<String, Object> model) {
        if (HttpStatus.BAD_REQUEST == status) {
            return new ModelAndView("error/error_400");
        } else if (HttpStatus.NOT_FOUND == status) {
            return new ModelAndView("error/error_404");
        } else {
            return new ModelAndView("error/error_5xx");
        }
    }
}

