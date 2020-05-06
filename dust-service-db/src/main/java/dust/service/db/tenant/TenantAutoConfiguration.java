package dust.service.db.tenant;

import dust.db.DbAdapterManager;
import dust.db.DustDbProperties;
import dust.db.pool.DataSourceTemplate;
import dust.db.tenant.DbManager;
import dust.db.tenant.SeparatedCache;
import dust.db.tenant.SeparatedConfig;
import dust.db.tenant.TenantAdapterManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author huangshengtao on 2018-4-9.
 */
@Configuration
@ConditionalOnProperty("dust.db.tenant.enable")
public class TenantAutoConfiguration {

    @Bean
    public SeparatedConfig separatedConfig(DbAdapterManager dbAdapterManager, DustDbProperties dustDbProperties) {
        return new SeparatedConfig(dustDbProperties, dbAdapterManager);
    }

    @Bean
    public DbManager dbManager(DbAdapterManager dbAdapterManager, DustDbProperties dustDbProperties,
                               SeparatedConfig separatedConfig, DataSourceTemplate dataSourceTemplate) {
        return new DbManager(dbAdapterManager, dustDbProperties, separatedConfig, dataSourceTemplate);
    }

    @Bean
    public SeparatedCache separatedCache() {
        return new SeparatedCache();
    }


    @Bean
    public TenantAdapterManager tenantAdapterManager(DbAdapterManager dbAdapterManager, DustDbProperties dustDbProperties, DbManager dbManager) {
        return new TenantAdapterManager(dbAdapterManager, dustDbProperties, dbManager);
    }

}
