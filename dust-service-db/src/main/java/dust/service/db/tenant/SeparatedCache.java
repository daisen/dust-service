package dust.service.db.tenant;

import com.google.common.collect.Maps;
import dust.service.db.tenant.pojo.AppConfig;
import dust.service.db.tenant.pojo.DbAccess;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 配置缓存管理器
 *
 * @author huangshengtao
 */
@Component
public class SeparatedCache {
    private Map<String, DbAccess> dbs = Maps.newHashMap();
    private Map<String, AppConfig> apps = Maps.newHashMap();

    /**
     * 根据id获取App配置
     *
     * @param id
     * @return
     */
    public AppConfig getApp(String id) {
        return getLocalAppConfig(id);
    }

    /**
     * 根据租户id和Appid获取App配置
     * @param tenantId
     * @param appId
     * @return
     */
    public AppConfig getApp(String tenantId, String appId) {
        return getLocalAppConfig(getAppKey(tenantId, appId));
    }

    /**
     * 根据数据库id获取接入点配置
     * @param id
     * @return
     */
    public DbAccess getDB(String id) {
        return getLocalDb(id);
    }

    public DbAccess getAdminDB(String cluster) {
        for (Map.Entry<String, DbAccess> keyValue : dbs.entrySet()) {
            if (StringUtils.equals(keyValue.getValue().getCluster(), cluster)
                    && StringUtils.equals(keyValue.getValue().getAccessLevel(), TenantConsts.DB_LEVEL_ADMIN)) {
                return keyValue.getValue();
            }
        }
        return null;
    }

    /**
     * 增加db
     * @param db
     */
    public void setDb(DbAccess db) {
        setLocalDb(db);
    }

    /**
     * 增加App
     * @param app
     */
    public void setApp(AppConfig app) {
        setLocalAppConfig(app);
    }

    public void clear() {
        dbs.clear();
        apps.clear();
    }


    private void setLocalDb(DbAccess db) {
        dbs.put(db.getId(), db);
    }

    private DbAccess getLocalDb(String key) {
        return dbs.get(key);
    }

    private AppConfig getLocalAppConfig(String key) {
        return apps.get(key);
    }

    private void setLocalAppConfig(AppConfig localAppConfig) {
        if (StringUtils.isEmpty(localAppConfig.getId())
                || StringUtils.isEmpty(localAppConfig.getAppId())
                || StringUtils.isEmpty(localAppConfig.getTenantId())) {
            return;
        }

        apps.put(localAppConfig.getId(), localAppConfig);
        apps.put(getAppKey(localAppConfig.getTenantId(), localAppConfig.getAppId()), localAppConfig);
    }

    private String getAppKey(String tenantId, String appId) {
        return tenantId + ";" + appId;
    }

    //TODO 对接各方缓存数据库
}
