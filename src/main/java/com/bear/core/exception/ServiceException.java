package com.bear.core.exception;


import com.bear.core.rest.CodeMsg;

/**
 * @author shomop
 * @date 2019/7/1 11:44
 */
public class ServiceException extends RuntimeException{

    protected CodeMsg codeMsg;

    public ServiceException(CodeMsg codeMsg) {
        super(codeMsg.getMsg());
        this.codeMsg = codeMsg;
    }

    public ServiceException(String message) {
        super(message);
    }

    public CodeMsg getCodeMsg() {
        return codeMsg;
    }
}
