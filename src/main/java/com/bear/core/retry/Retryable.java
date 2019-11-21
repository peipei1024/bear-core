package com.bear.core.retry;

import java.lang.annotation.*;

/**
 * @author shomop
 * @date 2019/11/21 11:05
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Retryable {
    /**
     * 重试次数
     * @return
     */
    int retryTimes() default 3;

    /**
     * 重试间隔
     * @return
     */
    int retryInterval() default 1;
}
