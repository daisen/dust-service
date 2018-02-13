package dust.service.db.pool;

import javax.sql.DataSource;

/**
 * 数据源接口，用于获取相应的数据源
 */
public interface DataSourceTemplate {
    DataSource getDataSource(String name);

    DataSource createDataSource(DataSourceContext db);
}
