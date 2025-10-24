package com.matvey.cinema.aspect;

import java.util.Arrays;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Pointcut("within(com.matvey.cinema.controllers..*) || within(com.matvey.cinema.service..*)")
    public void applicationPackagePointcut() {
    }

    @Around("applicationPackagePointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        if (shouldLog(joinPoint)) {
            String arguments = shouldLogArguments(joinPoint) ? logArguments(joinPoint) :
                    "Arguments not logged";
            logger.debug("Entering: {}.{}() with arguments = {}",
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(),
                    arguments
            );
        }
        try {
            Object result = joinPoint.proceed();
            if (shouldLog(joinPoint)) {
                logger.debug("Exiting: {}.{}() with result = {}",
                        joinPoint.getSignature().getDeclaringTypeName(),
                        joinPoint.getSignature().getName(), result
                );
            }
            return result;
        } catch (Throwable e) {
            if (shouldLog(joinPoint)) {
                logger.error("Exception in {}.{}() with cause = {}",
                        joinPoint.getSignature().getDeclaringTypeName(),
                        joinPoint.getSignature().getName(),
                        e.getCause() != null ? e.getCause() : "NULL", e
                );
            }
            throw e;
        }
    }

    @AfterThrowing(pointcut = "applicationPackagePointcut()", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
        if (shouldLog(joinPoint)) {
            logger.error("Exception in {}.{}() with cause = {}",
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(), e.getCause() != null ? e.getCause() :
                            "NULL", e
            );
        }
    }

    private boolean shouldLog(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        return methodName.startsWith("get") || methodName.startsWith("post")
                || methodName.startsWith("put") || methodName.startsWith("delete");
    }

    private boolean shouldLogArguments(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        return methodName.startsWith("get") || methodName.startsWith("post")
                || methodName.startsWith("put") || methodName.startsWith("delete")
                || methodName.startsWith("patch");
    }

    private String logArguments(JoinPoint joinPoint) {
        return Arrays.toString(joinPoint.getArgs());
    }
}
