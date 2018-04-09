package dust.service.db.tenant;

import dust.service.db.DbAdapterManager;
import dust.service.db.DustDbProperties;
import dust.service.db.pool.DataSourceContext;
import dust.service.db.pool.DataSourceTemplate;
import dust.service.db.sql.ISqlAdapter;
import dust.service.db.sql.SqlCommand;
import dust.service.db.tenant.pojo.AppConfig;
import dust.service.db.tenant.pojo.DbAccess;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ResourceUtils;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.SQLException;

/**
 * 多租户模型中的分离数据库模型，不同租户使用不同的数据库来数据隔离
 * 默认提供获取连接API，调用时许传入租户标识
 * 通过租户标识和SeparatedConfig来定位数据库信息
 * 返回正确的jdbc连接
 *
 * @author huangshengtao
 *
 */
public class DbManager {

    static Logger logger = LoggerFactory.getLogger(DbManager.class);

    @Autowired
    DbAdapterManager dbAdapterManager;

    @Autowired
    DustDbProperties dustDbProperties;

    @Autowired
    private SeparatedConfig separatedConfig;

    @Autowired
    private DataSourceTemplate dataSourceTemplate;

    /**
     * 获取商户对应的SqlAdapter，用于访问租户的数据
     *
     * @param tenantId
     * @param appId
     * @return
     * @throws SQLException
     */
    public ISqlAdapter getAdapter(String tenantId, String appId) {
        AppConfig app = separatedConfig.getAppConfig(tenantId, appId);
        if (app == null) {
            //默认租户APP
            String defaultTenantId = dustDbProperties.getTenant().getDefaultAppTenant();
            app = separatedConfig.getAppConfig(defaultTenantId, appId);
            if (app == null) {
                logger.error("没有对应的数据库,tenantId:{}, appId", tenantId, appId);
                return null;
            }
        }

        DbAccess access = app.getDbAccess();
        if (getOrCreateDataSource(access)) {
            ISqlAdapter adapter = dbAdapterManager.getAdapter(access.getName());
            if (adapter != null) {
                try {
                    adapter.useDbName(app.getDbName());
                    return adapter;
                } catch (SQLException ex) {
                    logger.error("无法启用对应的App配置", ex);
                }
            }
        }

        return null;
    }

    /**
     * 获取数据库管理员的适配器，由租户和App锁定管理员
     *
     * @param tenantId
     * @param appId
     * @return
     */
    public ISqlAdapter getAdminAdapter(String tenantId, String appId) {
        DbAccess adminCfg = separatedConfig.getAdminDb(tenantId, appId);
        if (adminCfg == null) {
            return null;
        }
        if (getOrCreateDataSource(adminCfg)) {
            return dbAdapterManager.getAdapter(adminCfg.getName());
        }
        return null;
    }

    /**
     * 拼接数据库驱动Url，目前按照mysql进行拼接，后续扩展
     * 如果{@link DbAccess#getAccessLevel()}是{@link TenantConsts#DB_LEVEL_ADMIN}, 支持允许多语句执行
     *
     * @param access
     * @return
     */
    private String combineUrl(DbAccess access) {
        StringBuilder sb = new StringBuilder("jdbc:mysql://");
        sb.append(access.getHost());
        String parameters = dustDbProperties.getUrlParameters();
        if (StringUtils.equals(access.getAccessLevel(), TenantConsts.DB_LEVEL_ADMIN)) {
            sb.append("?");
            sb.append("allowMultiQueries=true&" + parameters);
        } else {
            sb.append("?" + parameters);
        }

        return sb.toString();
    }

    /**
     * 数据库检查语句，默认mySql语句，后续扩展
     *
     * @return
     */
    private String getValidateQuery() {
        return "Select 1";
    }

    /**
     * 按照用户的配置创建
     *
     * @param tenantId
     * @param appId
     * @return
     */
    public boolean createDb(String tenantId, String appId) {
        AppConfig app = separatedConfig.getAppConfig(tenantId, appId);
        if (app == null) {
            logger.warn("app数据库信息未找到：" + appId);
            return false;
        }

        DbAccess adminDb = separatedConfig.getAdminDb(tenantId, appId);
        if (adminDb == null) {
            logger.warn("创建租户数据库失败，缺失有效的组管理账户");
            return false;
        }

        if (!getOrCreateDataSource(adminDb)) {
            logger.error("数据库管理接入账户不存在：" + adminDb.toString());
            return false;
        }

        ISqlAdapter sqlDataAdapter = dbAdapterManager.getAdapter(adminDb.getName());
        if (sqlDataAdapter == null) {
            logger.warn("获取数据库适配器失败：" + adminDb.toString());
            return false;
        }
        try {
            createDb(sqlDataAdapter, app);
            sqlDataAdapter.commit();
            return true;
        } catch (Exception ex) {
            sqlDataAdapter.rollbackQuiet();
            logger.error("创建App数据库失败" + app.toString(), ex);
        } finally {
            sqlDataAdapter.closeQuiet();
        }
        return false;
    }

    /**
     * 根据数据库信息创建对应的数据库
     * 创建数据库后，赋予相应用户数据增删改查权限
     *
     * @param sqlAdapter
     * @param appConfig
     * @return
     * @throws SQLException
     */
    private void createDb(ISqlAdapter sqlAdapter, AppConfig appConfig) throws SQLException {
        if (StringUtils.isEmpty(appConfig.getId()) || StringUtils.isEmpty(appConfig.getDbAccessId())) {
            throw new IllegalArgumentException("没有找到相应的DbAccess信息");
        }

        if (appConfig.getDbAccess() == null) {
            appConfig.setDbAccess(separatedConfig.getDbConfig(appConfig.getDbAccessId()));
        }

        String dbName = getDbName(appConfig);

        String createSchemaSql = String.format(TenantConsts.SQL_CREATE_Schema, dbName);
        sqlAdapter.update(createSchemaSql, null);
        //参数第一个是数据库名称，第二是用户，第三个是密码，此处用户和数据库名称一致
        String grantSql = String.format(TenantConsts.SQL_GRANT_TENANT, getDbName(appConfig),
                appConfig.getDbAccess().getUser(), appConfig.getDbAccess().getPassword());
        sqlAdapter.update(grantSql, null);
    }

    /**
     * 根据AppConfig获取对应的数据库名称
     *
     * @param appConfig
     * @return
     */
    public String getDbName(AppConfig appConfig) {
        if (!StringUtils.isEmpty(appConfig.getDbName())) {
            return appConfig.getDbName();
        } else {
            return appConfig.getAppAlias() + appConfig.getTenantId();
        }
    }

    /**
     * 插入新增数据库的信息，不会提交事务，由外部sqlDataAdapter来管理
     *
     * @param sqlAdapter
     * @param dbAccess
     * @return true 执行成功
     */
    public void insertDbAccess(ISqlAdapter sqlAdapter, DbAccess dbAccess) throws SQLException {
        if (dbAccess == null || sqlAdapter == null) {
            throw new IllegalArgumentException("sqlDataAdapter和dbConfig不允许为null");
        }

        SqlCommand dbCmd = new SqlCommand(TenantConsts.SQL_INSERT_DB);
        dbCmd.setParameter("id", dbAccess.getId());
        dbCmd.setParameter("name", dbAccess.getName());
        dbCmd.setParameter("host", dbAccess.getHost());
        dbCmd.setParameter("accessLevel", dbAccess.getAccessLevel());
        dbCmd.setParameter("permission", dbAccess.getPermission());
        dbCmd.setParameter("user", dbAccess.getUser());
        dbCmd.setParameter("password", dbAccess.getPassword());
        dbCmd.setParameter("status", dbAccess.getStatus());
        dbCmd.setParameter("cluster", dbAccess.getCluster());
        dbCmd.setParameter("remark", dbAccess.getRemark());
        sqlAdapter.update(dbCmd);
    }

    public void updateStatusOfDbAccess(ISqlAdapter sqlAdapter, DbAccess dbAccess) throws SQLException {
        if (sqlAdapter == null) {
            throw new IllegalArgumentException("sqlDataAdapter不允许为null");
        }

        SqlCommand dbCmd = new SqlCommand(TenantConsts.SQL_UPDATE_DB);
        dbCmd.setParameter("id", dbAccess.getId());
        dbCmd.setParameter("status", dbAccess.getStatus());
        sqlAdapter.update(dbCmd);
    }

    public void updateStatusOfAppConfig(ISqlAdapter sqlAdapter, AppConfig app) throws SQLException {
        if (sqlAdapter == null) {
            throw new IllegalArgumentException("sqlDataAdapter不允许为null");
        }

        SqlCommand dbCmd = new SqlCommand(TenantConsts.SQL_UPDATE_APP_DB);
        dbCmd.setParameter("id", app.getId());
        dbCmd.setParameter("status", app.getStatus());
        sqlAdapter.update(dbCmd);
    }


    /**
     * 插入租户app对应的信息
     *
     * @param sqlAdapter
     * @param app
     * @return
     */
    public void insertApp(ISqlAdapter sqlAdapter, AppConfig app) throws SQLException {
        if (app == null || sqlAdapter == null) {
            throw new IllegalArgumentException("sqlDataAdapter和app不允许为null");
        }

        SqlCommand cmd = new SqlCommand(TenantConsts.SQL_INSERT_APP_DB);
        cmd.setParameter("id", app.getId());
        cmd.setParameter("tenantId", app.getTenantId());
        cmd.setParameter("dbName", app.getDbName());
        cmd.setParameter("dbAccessId", app.getDbAccessId());
        cmd.setParameter("appId", app.getAppId());
        cmd.setParameter("appAlias", app.getAppAlias());
        cmd.setParameter("status", app.getStatus());
        cmd.setParameter("remark", app.getRemark());
        sqlAdapter.update(cmd);
    }

    /**
     * 检查以及创建对应的数据源
     *
     * @param cfg
     * @return
     */
    private boolean getOrCreateDataSource(DbAccess cfg) {
        DataSource ds = dataSourceTemplate.getDataSource(cfg.getName());
        if (ds != null) {
            return true;
        }

        DataSourceContext dataSourceContext = new DataSourceContext();
        dataSourceContext.setName(cfg.getName());
        dataSourceContext.setUrl(combineUrl(cfg));
        dataSourceContext.setUsername(cfg.getUser());
        dataSourceContext.setPassword(cfg.getPassword());
        dataSourceContext.setValidationQuery(getValidateQuery());

        ds = dataSourceTemplate.createDataSource(dataSourceContext);
        if (ds == null) {
            logger.warn("租户管理库" + cfg.getName() + "没有找到可用连接");
            return false;
        }

        return true;
    }

    /**
     * 执行所需的建表以及数据初始化
     *
     * @param adapter
     * @param path
     */
    public void initSchema(ISqlAdapter adapter, String path) {
        try {
            if (StringUtils.isEmpty(path)) {
                path = "classpath:mysql.sql";
            }

            File file = ResourceUtils.getFile(path);
            if (file.exists()) {
                FileReader fr = new FileReader(file);
                BufferedReader br = new BufferedReader(fr);
                String tmp;
                StringBuilder sql = new StringBuilder();
                while ((tmp = br.readLine()) != null) {
                    sql.append(tmp);
                    sql.append("\n");
                }

                adapter.update(sql.toString(), null);
            }

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }


}
