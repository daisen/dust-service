package dust.service.db.pool;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import static com.google.common.base.Preconditions.checkNotNull;

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
    private final String DEFAULT = "default";
    private ReentrantLock lock = new ReentrantLock();

    private DataSourceCache() {
    }


    /**
     * 通过参数key获取对应的数据源（连接池）
     * @param key key为{@link Integer}时，直接返回所在地址的数据源，会有IOB错误；key为{@link String}时，先按照数据源名称匹配，
     *            再使用连接信息匹配；key为{@link DataSourceContext}时，按照连接信息匹配
     * @return
     */
    public DataSource get(String key) {
        if (StringUtils.isEmpty(key)) {
            key = DEFAULT;
        }

        return dataSourceMap.get(key);
    }

    public DataSourceContext getContext(String key) {
        if (StringUtils.isEmpty(key)) {
            key = DEFAULT;
        }

        return dataSourceContextMap.get(key);
    }

    public DataSourceCache set(String key, DataSource ds) {
        synchronized (dataSourceMap) {
            if (StringUtils.isEmpty(key)) {
                key = DEFAULT;
            }

            dataSourceMap.put(key, ds);
        }

        return this;
    }

    public DataSourceCache set(DataSourceContext key, DataSource ds) {
        checkNotNull(key);
        lock.lock();
        try {
            if (StringUtils.isEmpty(key.getName())) {
                key.setName(DEFAULT);
            }

            dataSourceMap.put(key.getName(), ds);
            dataSourceContextMap.put(key.getName(), key);
        } finally {
            lock.unlock();
        }

        return this;
    }
}
