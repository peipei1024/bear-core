package com.xzpei.bear.core.retry;

/**
 * @author shomop
 * @date 2019/11/21 11:23
 */
public class RetryExhaustedException extends RuntimeException{

    public RetryExhaustedException(String message, Throwable cause) {
        super(message, cause);
    }
}
