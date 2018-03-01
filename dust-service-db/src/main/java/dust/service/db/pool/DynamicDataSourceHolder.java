package dust.service.db.pool;

import dust.service.core.thread.LocalHolder;

/**
 * 动态数据源切换类
 * 用于识别当前生效的数据源
 * @author huangshengtao
 */
public class DynamicDataSourceHolder {
    private static final String DATASOURCE_LOCAL = "DATASOURCE_LOCAL";

    public static void setContext(DataSourceContext context) {
        if (context == null) {
            context = new DataSourceContext();
        }

        LocalHolder.get(DATASOURCE_LOCAL).set(context);
    }

    public static DataSourceContext getContext() {
        DataSourceContext context = (DataSourceContext) LocalHolder.get(DATASOURCE_LOCAL).get();
        if (context == null) {
            context = new DataSourceContext();
        }

        return context;
    }
}