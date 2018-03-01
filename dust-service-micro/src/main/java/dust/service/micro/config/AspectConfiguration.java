package dust.service.micro.config;

import dust.service.micro.aop.logging.LoggingAspect;
import dust.service.micro.aop.web.RequestLocalAspect;
import dust.service.micro.aop.web.WebAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 启用AOP功能
 * @author huangshengtao
 */
@Configuration
@EnableAspectJAutoProxy
public class AspectConfiguration {

    @Bean
    public LoggingAspect loggingAspect() {
        return new LoggingAspect();
    }

    @Bean
    public WebAspect webAspect() {
        return new WebAspect();
    }

    @Bean
    public RequestLocalAspect adapterAspect() {
        return new RequestLocalAspect();
    }
}
