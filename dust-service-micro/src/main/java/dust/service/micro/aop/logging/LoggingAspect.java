package dust.service.micro.aop.logging;

import dust.service.micro.config.Constants;
import dust.service.micro.util.DbLogUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

/**
 * AOP切入service，web和third，记录所有的异常信息到数据库
 * @author huangshengtao
 */
@Aspect
public class LoggingAspect {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private Environment env;

    @Autowired
    private DbLogUtil dbLogUtil;

    @Pointcut("(@annotation(org.springframework.web.bind.annotation.RequestMapping) " +
            "|| @annotation(dust.service.micro.annotation.DustMapping) " +
            "|| @annotation(dust.service.micro.annotation.CustomMapping))")
    @Order(1)
    public void loggingPointcut() {
    }

    @AfterThrowing(pointcut = "loggingPointcut()", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
        String msg = String.format("Exception in %s.%s() with cause = \'%s\'", joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(), e.getCause() != null? e.getCause() : "NULL");
        if (!env.acceptsProfiles(Constants.SPRING_PROFILE_DEVELOPMENT)) {
            dbLogUtil.write(msg, e);
        }

        log.error(msg);
    }
}
