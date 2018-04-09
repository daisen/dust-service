package dust.service.db.tenant;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author huangshengtao on 2018-4-9.
 */
@Configuration
public class TenantAutoConfiguration {

    @Bean
    public SeparatedConfig separatedConfig() {
        return new SeparatedConfig();
    }

    @Bean
    public DbManager dbManager() {
        return new DbManager();
    }

    @Bean
    public SeparatedCache separatedCache() {
        return new SeparatedCache();
    }
}
