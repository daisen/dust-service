package dust.service.db.dict;

import dust.service.db.sql.ISqlAdapter;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author huangshengtao on 2017-12-28.
 */
public class DictGlobalConfig {
    private String dateFormat = "yyyy-MM-dd HH:mm:ss";
    private boolean enableObjId = false;
    private ThreadLocal<ISqlAdapter> sqlAdapterThreadLocal = new ThreadLocal<>();
    private boolean autoInitAdapter = true;
    private String dataSourceName;
    private String containerClass;
    private boolean strict = false;
    private boolean allowColumnNameOutOfUnderscore = true;

    private static final DictGlobalConfig instance = new DictGlobalConfig();

    private DictGlobalConfig() {

    }

    public static boolean isAllowColumnNameOutOfUnderscore() {
        return instance.allowColumnNameOutOfUnderscore;
    }

    public static void setAllowColumnNameOutOfUnderscore(boolean allowColumnNameOutOfUnderscore) {
        instance.allowColumnNameOutOfUnderscore = allowColumnNameOutOfUnderscore;
    }

    public static String getDateFormat() {
        return instance.dateFormat;
    }

    public static void setDateFormat(String dateFormat) {
        instance.dateFormat = dateFormat;
    }

    public static boolean isEnableObjId() {
        return instance.enableObjId;
    }

    public static void setEnableObjId(boolean enableObjId) {
        instance.enableObjId = enableObjId;
    }

    public static void setSqlAdapter(ISqlAdapter sqlAdapter) {
        checkNotNull(sqlAdapter);
        instance.sqlAdapterThreadLocal.set(sqlAdapter);
    }

    public static ISqlAdapter getSqlAdapter() {
        ISqlAdapter sqlAdapter = instance.sqlAdapterThreadLocal.get();
        if (sqlAdapter == null) {
            throw new IllegalStateException("not found correspond sql adapter");
        }
        return sqlAdapter;
    }

    public static void removeSqlAdapter() {
        instance.sqlAdapterThreadLocal.remove();
    }

    public static boolean isAutoInitAdapter() {
        return instance.autoInitAdapter;
    }

    public static void setAutoInitAdapter(boolean autoInitAdapter) {
        instance.autoInitAdapter = autoInitAdapter;
    }

    public static String getDataSourceName() {
        return instance.dataSourceName;
    }

    public static void setDataSourceName(String dataSourceName) {
        instance.dataSourceName = dataSourceName;
    }

    public static String getContainerClass() {
        return instance.containerClass;
    }

    public static void setContainerClass(String containerClass) {
        instance.containerClass = containerClass;
    }
}
