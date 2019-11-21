package com.bear.core.retry;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author shomop
 * @date 2019/11/21 11:04
 */
@Aspect
@Component
public class RetryAspect {

    private Logger logger = LoggerFactory.getLogger(RetryAspect.class);

    @Pointcut("@annotation(com.bear.core.retry.Retryable)")
    private void retryMethodCall(){}

    @Around("retryMethodCall()")
    public Object retry(ProceedingJoinPoint joinPoint) throws InterruptedException {
        Retryable annotation = ((MethodSignature) joinPoint.getSignature()).getMethod().getAnnotation(Retryable.class);
        int times = annotation.retryTimes();
        int interval = annotation.retryInterval();

        Throwable error = new RuntimeException();
        for (int i = 1; i <= times; i++) {
            try {
                return joinPoint.proceed();
            }catch (Throwable e){
                error = e;
                logger.info("调用异常，开始重试，retryTimes:{}", i);
            }
            Thread.sleep(interval * 1000);
        }
        throw new RetryExhaustedException("重试次数耗尽", error);

    }
}
