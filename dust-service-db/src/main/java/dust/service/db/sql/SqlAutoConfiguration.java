package dust.service.db.sql;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * @author huangshengtao on 2018-4-9.
 */
@Configuration
public class SqlAutoConfiguration {

    @Bean("readSqlAdapterImpl")
    @Scope("prototype")
    public ISqlAdapter getReadSqlAdapter() {
        return new ReadSqlAdapterImpl();
    }

    @Bean("writeSqlAdapterImpl")
    @Scope("prototype")
    public ISqlAdapter getWriteSqlAdapter() {
        return new WriteSqlAdapterImpl();
    }

    @Bean("sqlAdapter")
    @Scope("prototype")
    public ISqlAdapter getSqlAdapter() {
        return new SqlAdapterImpl();
    }
}
