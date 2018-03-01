package dust.service.micro.aop.web;

import dust.service.core.thread.LocalHolder;
import dust.service.core.util.BeanUtils;
import dust.service.db.DbAdapterManager;
import dust.service.db.dict.DictGlobalConfig;
import dust.service.micro.repository.RepositoryException;
import dust.service.micro.repository.TenantRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;


/**
 * @author huangshengtao on 2017-7-31.
 * 切入Rest服务，释放Db资源
 */
@Aspect
@Order(3)
public class RequestLocalAspect {

    final static Logger logger = LoggerFactory.getLogger(RequestLocalAspect.class);

    @Pointcut("@annotation(dust.service.micro.annotation.DustMapping) ")
    public void dustMappingPointcut() {
    }

    @Pointcut("@annotation(dust.service.micro.annotation.CustomMapping) ")
    public void customMappingPointcut() {
    }

    @Pointcut("@annotation(org.springframework.web.bind.annotation.RequestMapping) ")
    public void requestMappingPointcut() {
    }

    @Around("dustMappingPointcut() || requestMappingPointcut() || customMappingPointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        if (DictGlobalConfig.isAutoInitAdapter()) {
            TenantRepository repository = (TenantRepository) BeanUtils.getBean("tenantRepository");
            if (repository == null) {
                throw new RepositoryException("not found bean tenantRepository when dict init");
            }

            DictGlobalConfig.setSqlAdapter(repository.getAdapter(null));
        }

        try {
            return joinPoint.proceed();
        } finally {
            DbAdapterManager.destroy();
            LocalHolder.remove();
        }
    }

}
