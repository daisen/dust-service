package dust.service.db;

import dust.service.core.thread.LocalHolder;
import dust.service.db.sql.ISqlAdapter;
import dust.service.db.sql.SqlAdapterContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.List;

/**
 * 数据源适配管理器
 * @author huangshengtao
 * @version 2017.3.9
 */
public class DbAdapterManager {
    private static Logger logger = LoggerFactory.getLogger(DbAdapterManager.class);
    //private static ThreadLocal<SqlAdapterContext> localSqlAdapterContext = new ThreadLocal<>();
    private static String LOCAL_SQL_ADAPTER_CONTEXT = "DbAdapterManager.LOCAL_SQL_ADAPTER_CONTEXT";

    @Autowired
    ApplicationContext context;

    @Autowired
    DustDbProperties dustDbProperties;
    private boolean inited;

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
            if (dustDbProperties.isAutoAdapterDestroy()) {
                SqlAdapterContext ctx = (SqlAdapterContext) LocalHolder.get(LOCAL_SQL_ADAPTER_CONTEXT).get();
                if (ctx == null) {
                    ctx = new SqlAdapterContext();
                    LocalHolder.get(LOCAL_SQL_ADAPTER_CONTEXT).set(ctx);
                }
                ctx.getAdapterList().add(adapter);
            }
            return adapter;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new DustDbRuntimeException(ex);
        }
    }

    /**
     * 请在释放ThreadLocal资源前关闭连接
     */
    public static void destroy() {
        SqlAdapterContext ctx = (SqlAdapterContext) LocalHolder.get(LOCAL_SQL_ADAPTER_CONTEXT).get();
        if (ctx == null) {
            return;
        }

        List<ISqlAdapter> adapterList = ctx.getAdapterList();
        for(ISqlAdapter adapter : adapterList) {
            adapter.closeQuiet();
        }

        adapterList.clear();
    }
}
