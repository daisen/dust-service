package dust.service.db;

import dust.service.db.sql.ISqlAdapter;
import dust.service.db.sql.SqlAdapterContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 数据源适配管理器
 * @author huangshengtao
 * @version 2017.3.9
 */
@Component
public class DbAdapterManager {
    static Logger logger = LoggerFactory.getLogger(DbAdapterManager.class);

    static ThreadLocal<SqlAdapterContext> localSqlAdapterContext = new ThreadLocal<>();

    @Autowired
    ApplicationContext context;

    @Autowired
    DustDbProperties dustDbProperties;

    /**
     * 根据数据源名称获取对应的适配类
     * @param dataSourceName
     * @return
     */
    public ISqlAdapter getAdapter(String dataSourceName) {
        return getAdapter(dataSourceName, "sqlAdapter");
    }

    /**
     * 根据数据源名称获取对应的读适配类
     * @param dataSourceName
     * @return
     */
    public ISqlAdapter getReadAdapter(String dataSourceName) {
        return getAdapter(dataSourceName, "readSqlAdapterImpl");
    }

    /**
     * 根据数据源名称获取对应的写适配类
     * @param dataSourceName
     * @return
     */
    public ISqlAdapter getWriteAdapter(String dataSourceName) {
        return getAdapter(dataSourceName, "writeSqlAdapterImpl");
    }

    private ISqlAdapter getAdapter(String dataSourceName, String beanName) {
        try {
            ISqlAdapter adapter = (ISqlAdapter) context.getBean(beanName);
            adapter.init(dataSourceName);

            //压入线程，用于后续释放
            if (dustDbProperties.isAutoAdapterDestroy() && localSqlAdapterContext.get() != null) {
                localSqlAdapterContext.get().getAdapterList().add(adapter);
            }
            return adapter;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }

        return null;
    }

    /**
     * 用于全局初始化操作
     * 目前主要是对线程内的Adapter进行缓存的处理
     * @apiNote 请配合<code>DbAdapterManager.destroy</code>使用
     */
    public static void init() {
        localSqlAdapterContext.set(new SqlAdapterContext());
    }

    public static void destroy() {
        List<ISqlAdapter> adapterList = localSqlAdapterContext.get().getAdapterList();
        for(ISqlAdapter adapter : adapterList) {
            adapter.closeQuiet();
        }

        adapterList.clear();
        localSqlAdapterContext.remove();
    }
}
