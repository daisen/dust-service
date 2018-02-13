package dust.service.db.pool;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;

import javax.sql.DataSource;
import java.util.Map;

/**
 * 数据源缓存类，简单的缓存处理，用于缓存当前运行时使用过的数据源，避免类创建的开销
 *
 * @author huangshengtao
 */
public class DataSourceCache {
    static DataSourceCache instance = new DataSourceCache();

    public static DataSourceCache getInstance() {
        return instance;
    }

    private Map<String, DataSource> dataSourceMap = Maps.newHashMap();
    private DataSource defaultDataSource;
    private Map<String, DataSourceContext> dataSourceContextMap = Maps.newHashMap();

    public DataSourceCache() {

    }

    /**
     * 通过参数key获取对应的数据源（连接池）
     * @param key key为{@link Integer}时，直接返回所在地址的数据源，会有IOB错误；key为{@link String}时，先按照数据源名称匹配，
     *            再使用连接信息匹配；key为{@link DataSourceContext}时，按照连接信息匹配
     * @return
     */
    public DataSource get(String key) {
        if (StringUtils.isEmpty(key) || StringUtils.equals(key, "default")) {
            return getDefault();
        }
        return dataSourceMap.get(key);
    }

    public DataSourceContext getContext(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }

        return dataSourceContextMap.get(key);
    }

    public DataSourceCache set(String key, DataSource ds) {
        synchronized (dataSourceMap) {
            dataSourceMap.put(key, ds);
            if (this.defaultDataSource == null) {
                this.defaultDataSource = ds;
            }

            if (StringUtils.isEmpty(key) || StringUtils.equals(key, "default")) {
                this.defaultDataSource = ds;
            }
        }

        return this;
    }

    public DataSourceCache set(DataSourceContext key, DataSource ds) {
        synchronized (dataSourceMap) {
            dataSourceMap.put(key.getName(), ds);
            dataSourceContextMap.put(key.getName(), key);

            if (this.defaultDataSource == null) {
                this.defaultDataSource = ds;
            }

            if (StringUtils.isEmpty(key.getName()) || StringUtils.equals(key.getName(), "default")) {
                this.defaultDataSource = ds;
            }
        }

        return this;
    }




    public DataSource getDefault() {
        return defaultDataSource;
    }

}
