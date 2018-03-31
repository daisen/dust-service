package dust.service.db.dict;

import dust.service.db.dict.builder.MySqlBuilder;
import dust.service.db.dict.builder.OracleBuilder;
import dust.service.db.sql.ISqlAdapter;
import dust.service.db.support.DataBaseFactory;
import org.apache.commons.lang3.StringUtils;

/**
 * @author huangshengtao on 2018-1-10.
 */
public class SqlBuilderFactory {
    public static SqlBuilder build() {
        if (DictGlobalConfig.getSqlAdapter() == null) {
            return new MySqlBuilder();
        }

        return build(DictGlobalConfig.getSqlAdapter().getDbType());
    }

    public static SqlBuilder build(String dbType) {
        switch (dbType) {
            case DataBaseFactory.JdbcConstants.MYSQL:
                return new MySqlBuilder();
            case DataBaseFactory.JdbcConstants.ORACLE:
                return new OracleBuilder();
        }
        return new MySqlBuilder();
    }

    public static SqlBuilder build(ISqlAdapter adapter) {
        if (adapter == null) {
            return build();
        }

        String dbType = adapter.getDbType();
        return build(dbType);
    }

}
