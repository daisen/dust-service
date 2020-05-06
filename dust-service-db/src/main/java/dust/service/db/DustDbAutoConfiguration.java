package dust.service.db;

import dust.db.*;
import dust.db.dict.*;
import dust.db.druid.DruidProperties;
import dust.db.tenant.DbManager;
import dust.service.db.pool.DynamicDataSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * DustDB公共配置类
 * @author huangshengtao
 */
@Configuration
public class DustDbAutoConfiguration {

    @Bean
    @Primary
    public DataSource dataSource(ApplicationContext context) {
        return new DynamicDataSource(context);
    }


    @Bean
    public DbAdapterManager dbAdapterManager(DustDbProperties dustDbProperties) {
        return new DbAdapterManager(dustDbProperties);
    }

    @Bean
    public DataObjBuilder dataObjBuilder(DbAdapterManager dbAdapterManager) {
        return new DataObjBuilder(dbAdapterManager);
    }

    @Bean
    @ConfigurationProperties("dust.db")
    public DustDbProperties dustDbProperties() {
        return new DustDbProperties();
    }

}
