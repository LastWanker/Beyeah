package SCG.beyeah1211.controller.common;

import SCG.beyeah1211.common.beyeahException;
import SCG.beyeah1211.util.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class beyeahExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(beyeahExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public Object handleException(Exception e, HttpServletRequest req) {
        Result<Object> result = new Result<>();
        result.setResultCode(500);
        if (e instanceof beyeahException) {
            result.setResultCode(400);
            result.setMessage(e.getMessage());
        } else {
            logger.error("request failed, method={}, uri={}", req.getMethod(), req.getRequestURI(), e);
            result.setMessage("internal server error");
        }

        String contentTypeHeader = req.getHeader("Content-Type");
        String acceptHeader = req.getHeader("Accept");
        String xRequestedWith = req.getHeader("X-Requested-With");
        boolean isJsonRequest = (contentTypeHeader != null && contentTypeHeader.contains("application/json"))
                || (acceptHeader != null && acceptHeader.contains("application/json"))
                || "XMLHttpRequest".equalsIgnoreCase(xRequestedWith)
                || req.getRequestURI().startsWith("/api/");
        if (isJsonRequest) {
            return result;
        }

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("message", e.getMessage());
        modelAndView.addObject("url", req.getRequestURL());
        modelAndView.setViewName("error/error");
        return modelAndView;
    }
}

