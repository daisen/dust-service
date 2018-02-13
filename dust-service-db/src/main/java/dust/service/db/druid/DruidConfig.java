package dust.service.db.druid;

import dust.service.db.DustDbProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 引入druid连接池，需配置druid信息
 */
@Configuration
@ConditionalOnProperty(value = "dust.db.poolName", havingValue = "druid")
@EnableConfigurationProperties({DruidProperties.class})
public class DruidConfig {
    @Autowired
    DustDbProperties dustDbProperties;

    @Autowired
    DruidProperties druidProperties;

    @Bean(initMethod = "init")
    public DruidTemplate druidTemplate(ApplicationContext context) {
        return new DruidTemplate(dustDbProperties, druidProperties, context);
    }
}
