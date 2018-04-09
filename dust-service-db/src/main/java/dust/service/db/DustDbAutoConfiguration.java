package dust.service.db;

import dust.service.db.dict.DataObjBuilder;
import dust.service.db.dict.DictGlobalConfig;
import dust.service.db.pool.DataSourceCache;
import dust.service.db.pool.DynamicDataSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
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
@SuppressWarnings("SpringJavaAutowiringInspection")
@Configuration
@EnableConfigurationProperties({DustDbProperties.class})
public class DustDbAutoConfiguration implements InitializingBean {

    @Autowired
    DustDbProperties dustDbProperties;

    @Bean
    @Primary
    public DataSource dataSource(ApplicationContext context) {
        return new DynamicDataSource(context);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        DictGlobalConfig.setEnableObjId(dustDbProperties.getDict().isEnableObjId());
        DictGlobalConfig.setAutoInitAdapter(dustDbProperties.getDict().isAutoInitAdapter());
        DictGlobalConfig.setDataSourceName(dustDbProperties.getDict().getDataSourceName());
        DictGlobalConfig.setAllowColumnNameOutOfUnderscore(dustDbProperties.getDict().isAllowColumnNameOutOfUnderscore());
        DictGlobalConfig.setContainerClass(dustDbProperties.getDict().getContainerClass());
    }

    @Bean
    public TenantAdapterManager tenantAdapterManager() {
        return new TenantAdapterManager();
    }

    @Bean
    public DbAdapterManager dbAdapterManager() {
        return new DbAdapterManager();
    }

    @Bean
    public DataObjBuilder dataObjBuilder() {
        return new DataObjBuilder();
    }
}
