package practice.s3.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Aspect
@Component
@Slf4j
public class TimerAop {

    @Pointcut("@annotation(Timer)")
    private void timer() {

    }

    @Around("timer()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Object result = joinPoint.proceed();
        stopWatch.stop();
        printMessage(joinPoint, stopWatch);

        return result;
    }

    private void printMessage(ProceedingJoinPoint joinPoint, StopWatch stopWatch) {
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        log.info("[{}.{}] 실행시간(ms): {}", className, methodName, stopWatch.getTotalTimeMillis());
    }
}
