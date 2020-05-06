package dust.service.db.druid;

import dust.db.DustDbProperties;
import dust.db.druid.DruidProperties;
import dust.db.druid.DruidTemplate;
import dust.service.db.DustDbAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 引入druid连接池，需配置druid信息
 */
@Configuration
@ConditionalOnProperty(value = "dust.db.poolName", havingValue = "druid")
@AutoConfigureAfter(DustDbAutoConfiguration.class)
public class DruidAutoConfiguration {
    @Bean(initMethod = "init")
    public DruidTemplate druidTemplate(DustDbProperties dustDbProperties, DruidProperties druidProperties) {
        return new DruidTemplate(dustDbProperties, druidProperties);
    }

    @Bean
    @ConfigurationProperties("druid")
    public DruidProperties druidProperties() {
        return new DruidProperties();
    }
}
