package com.bear.core.web;

import com.bear.core.exception.ServiceException;
import com.bear.core.rest.CodeMsg;
import com.bear.core.rest.Result;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * @author shomop
 * @date 2019/7/1 11:44
 */
@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {

    private static final Log logger = LogFactory.getLog(GlobalExceptionHandler.class);

    private final Environment env;

    @Autowired
    public GlobalExceptionHandler(Environment env) {
        this.env = env;
    }

    @ExceptionHandler(Exception.class)
    public Result exceptionHandler(HttpServletRequest request, Exception e) {
        logger.error("Unhandled error.", e);
        if (e instanceof ServiceException) {
            return Result.error(((ServiceException) e).getCodeMsg());
        } else if (e instanceof MethodArgumentNotValidException){
            MethodArgumentNotValidException me = (MethodArgumentNotValidException) e;
            return Result.error(CodeMsg.PARAMS_ERROR.fillArgs(
                    me.getBindingResult().getAllErrors().get(0).getDefaultMessage()));
        } else {
            Result error = Result.error(CodeMsg.SERVER_ERROR);
            if (env.acceptsProfiles(Profiles.DEV, Profiles.TEST)) {
                error.setStack(ExceptionUtils.getStackFrames(e));
            }
            return error;
        }
    }

}
