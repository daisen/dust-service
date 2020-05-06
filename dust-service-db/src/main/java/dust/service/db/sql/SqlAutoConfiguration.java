package dust.service.db.sql;

import dust.db.pool.DataSourceTemplate;
import dust.db.sql.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * @author huangshengtao on 2018-4-9.
 */
@Configuration
@ConditionalOnBean(DataSourceTemplate.class)
public class SqlAutoConfiguration {

    @Autowired
    DataSourceTemplate dataSourceTemplate;

    @Bean("sqlAdapter")
    @Scope("prototype")
    public ISqlAdapter sqlAdapter() {
        return new SqlAdapterImpl(dataSourceTemplate);
    }

    @Bean("readSqlAdapter")
    @Scope("prototype")
    public ISqlAdapter readSqlAdapter() {
        return new ReadSqlAdapterImpl(dataSourceTemplate);
    }

    @Bean("writeSqlAdapter")
    @Scope("prototype")
    public ISqlAdapter writeSqlAdapter() {
        return new WriteSqlAdapterImpl(dataSourceTemplate);
    }
}
