package dust.service.db.dict;

import dust.service.db.dict.builder.MySqlBuilder;
import dust.service.db.support.DataBaseFactory;
import org.apache.commons.lang3.StringUtils;

/**
 * @author huangshengtao on 2018-1-10.
 */
public class SqlBuilderFactory {
    public static SqlBuilder build() {
        if (StringUtils.isEmpty(DictGlobalConfig.getSqlAdapter().getDbType())) {
            return new MySqlBuilder();
        }

        return build(DictGlobalConfig.getSqlAdapter().getDbType());
    }

    public static SqlBuilder build(String dbType) {
        switch (dbType) {
            case DataBaseFactory.JdbcConstants.MYSQL:
                return new MySqlBuilder();
        }
        return new MySqlBuilder();
    }

}
