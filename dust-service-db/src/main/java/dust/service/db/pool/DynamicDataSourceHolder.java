package dust.service.db.pool;

/**
 * 动态数据源切换类
 * 用于识别当前生效的数据源
 * @author huangshengtao
 */
public class DynamicDataSourceHolder {
    private static final ThreadLocal<DataSourceContext> DATASOURCE_LOCAL = new ThreadLocal<>();

    public static void setContext(DataSourceContext context) {
        if (context == null) {
            context = new DataSourceContext();
        }

        DATASOURCE_LOCAL.set(context);
    }

    public static DataSourceContext getContext() {
        DataSourceContext context = DATASOURCE_LOCAL.get();
        if (context == null) {
            context = new DataSourceContext();
        }

        return context;
    }
}
