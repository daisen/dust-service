package dust.service.db.tenant;

import dust.service.core.util.ClassBuildUtils;
import dust.service.db.DbAdapterManager;
import dust.service.db.DustDbProperties;
import dust.service.db.DustDbRuntimeException;
import dust.service.db.pool.DataSourceTemplate;
import dust.service.db.sql.DataRow;
import dust.service.db.sql.DataTable;
import dust.service.db.sql.ISqlAdapter;
import dust.service.db.sql.SqlCommand;
import dust.service.db.tenant.pojo.AppConfig;
import dust.service.db.tenant.pojo.DbAccess;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;

/**
 * 配置管理器
 *
 * @author huangshengtao
 */
public class SeparatedConfig implements InitializingBean {

    static Logger logger = LoggerFactory.getLogger(SeparatedConfig.class);

    private Boolean inited = Boolean.FALSE;

    @Autowired
    DustDbProperties dustDbProperties;

    @Autowired
    DbAdapterManager dbAdapterManager;

    @Autowired
    private SeparatedCache separatedCache;

    @Autowired
    private DataSourceTemplate dataSourceTemplate;


    /**
     * Application启动后，加载租户信息以及租户的数据库
     *
     * @throws TenantException
     */
    public void init() {
        if (!dustDbProperties.getTenant().isEnable()) return;
        synchronized (inited) {
            if (inited) {
                return;
            }

            loadConfig();
            inited = true;
        }
    }

    private ISqlAdapter getTenantAdapter() {
        String datasourceName = dustDbProperties.getTenant().getDatasourceName();
        ISqlAdapter sqlDataAdapter = dbAdapterManager.getAdapter(datasourceName);
        return sqlDataAdapter;
    }

    private void loadConfig() {
        if (StringUtils.isEmpty(dustDbProperties.getTenant().getDatasourceName())) {
            throw new DustDbRuntimeException("DustDb默认数据源未指定");
        }

        ISqlAdapter sqlDataAdapter = getTenantAdapter();
        //如果找不到，则默认根据配置初始化数据源
        if (sqlDataAdapter == null) {
            throw new DustDbRuntimeException("[01]无法连接租户配置库");
        }

        //清空缓存
        separatedCache.clear();

        try {
            SqlCommand dbCmd = new SqlCommand(TenantConsts.SQL_DB);
            DataTable data = sqlDataAdapter.query(dbCmd);
//            sqlDataAdapter.closeQuiet();
            if (data.size() > 0) {
                for (int i = 0; i < data.size(); i++) {
                    DataRow row = data.getRows().get(i);
                    DbAccess db = null;
                    try {
                        db = ClassBuildUtils.mapToObject(row.toMap(), DbAccess.class);
                        if (db != null) {
                            separatedCache.setDb(db);
                            logger.info("SeparatedConfig.loadConfig加载" + db.toString());
                        }
                    } catch (Exception e) {
                        logger.error("DbAccess数据错误");
                    }

                }
            }
        } catch (SQLException ex) {
            logger.error("查询租户数据库失败", ex);
        }

        try {
            SqlCommand appCmd = new SqlCommand(TenantConsts.SQL_APP_DB);
            DataTable data = sqlDataAdapter.query(appCmd);
//            sqlDataAdapter.closeQuiet();
            if (data.size() > 0) {
                for (int i = 0; i < data.size(); i++) {
                    DataRow row = data.getRows().get(i);
                    AppConfig appConfig = ClassBuildUtils.mapToObject(row.toMap(), AppConfig.class);
                    if (appConfig != null) {
                        appConfig.setDbAccess(separatedCache.getDB(appConfig.getDbAccessId()));
                        separatedCache.setApp(appConfig);
                        logger.info("SeparatedConfig.loadConfig加载" + appConfig.toString());
                    }
                }
            }
        } catch (SQLException ex) {
            logger.error("查询租户信息失败", ex);
        }

        sqlDataAdapter.closeQuiet();
    }

    /**
     * 获取App使用的数据库配置
     *
     * @param tenantId
     * @param appId
     * @return
     */
    public AppConfig getAppConfig(String tenantId, String appId) {
        init();
        AppConfig app = separatedCache.getApp(tenantId, appId);
        if (app == null) {
            ISqlAdapter sqlDataAdapter = getTenantAdapter();
            SqlCommand dbCmd = new SqlCommand(TenantConsts.SQL_FIND_APP_DB);

            try {
                dbCmd.setParameter("tenantId", tenantId);
                dbCmd.setParameter("appId", appId);
                DataTable data = sqlDataAdapter.query(dbCmd);
                sqlDataAdapter.closeQuiet();
                if (data.size() > 0) {
                    AppConfig appConfig = ClassBuildUtils.mapToObject(data.getRows().get(0).toMap(), AppConfig.class);
                    if (appConfig != null) {
                        if (!StringUtils.isEmpty(appConfig.getDbAccessId())) {
                            appConfig.setDbAccess(this.getDbAccess(appConfig.getDbAccessId()));
                        }
                        saveAppConfig(appConfig);
                        return appConfig;
                    }
                }
            } catch (SQLException ex) {
                logger.error("获取App信息失败", ex);
            }
        }

        return app;
    }

    /**
     * 获取租户的数据库管理账户
     * 通过{@code tenantId}和{@code appId}获取相应的AppConfig，进一步获取{@link DbAccess}所在组
     * 根据组返回管理Level的{@lin DbAccess}
     * @param tenantId
     * @return
     */
    public DbAccess getAdminDb(String tenantId, String appId) {
        init();
        AppConfig app = getAppConfig(tenantId, appId);
        if (app == null) {
            return null;
        }
        return getAdminDb(app.getDbAccess().getCluster());
    }

    private DbAccess castToDbAccess(DataTable data) {
        if (data.size() > 0) {
            DbAccess dbAccess1 = ClassBuildUtils.mapToObject(data.getRows().get(0).toMap(), DbAccess.class);
            if (dbAccess1 != null) {
                saveDb(dbAccess1);
                return dbAccess1;
            }
        }
        return null;
    }

    public DbAccess getAdminDb(String cluster) {
        init();
        DbAccess dbAccess = separatedCache.getAdminDB(cluster);
        if (dbAccess == null) {
            ISqlAdapter sqlDataAdapter = getTenantAdapter();
            SqlCommand dbCmd = new SqlCommand(TenantConsts.SQL_FIND_ADMIN_DB);
            try {
                dbCmd.setParameter("cluster", cluster);
                DataTable data = sqlDataAdapter.query(dbCmd);
                sqlDataAdapter.closeQuiet();
                return castToDbAccess(data);
            } catch (SQLException ex) {
                logger.error("获取数据库管理配置信息失败", ex);
            }
        }
        return dbAccess;
    }

    public DbAccess getDbAccess(String id) {
        init();
        DbAccess dbAccess = separatedCache.getDB(id);
        if (dbAccess == null) {
            ISqlAdapter sqlDataAdapter = getTenantAdapter();
            SqlCommand dbCmd = new SqlCommand(TenantConsts.SQL_FIND_DB);
            try {
                dbCmd.setParameter("id", id);
                DataTable data = sqlDataAdapter.query(dbCmd);
                return castToDbAccess(data);
            } catch (SQLException ex) {
                logger.error("获取数据库接入配置信息失败", ex);
            }
        }
        return dbAccess;
    }

    /**
     * 根据id获取数据库信息
     *
     * @param id
     * @return
     */
    public DbAccess getDbConfig(String id) {
        init();
        return separatedCache.getDB(id);
    }

    /**
     * 设置或更新数据库信息
     * <strong>注意：</strong>尽量不要自行修改配置信息。把配置的更新交给dustdb自己控制
     * @param cfg
     */
    public void saveDb(DbAccess cfg) {
        separatedCache.setDb(cfg);
    }

    /**
     * 设置或更新App信息
     * <strong>注意：</strong>尽量不要自行修改配置信息。把配置的更新交给dustdb自己控制
     * @param appConfig
     */
    public void saveAppConfig(AppConfig appConfig) {
        separatedCache.setApp(appConfig);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }
}
