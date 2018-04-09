package dust.service.micro.config;

import dust.service.micro.repository.TenantRepository;
import dust.service.micro.util.DbLogUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 用于自定义的Component目录
 * @author huangshengtao
 */
@Configuration
//@ComponentScan({"com.dust.db"})
//@EnableAutoConfiguration
public class ComponentConfiguration {
    @Bean
    public TenantRepository tenantRepository() {
        return new TenantRepository();
    }

    @Bean
    public DbLogUtil dbLogUtil() {
        return new DbLogUtil();
    }
}
