package dust.service.db;

import dust.service.core.util.BeanUtils;
import dust.service.db.sql.ISqlAdapter;
import dust.service.db.tenant.DbManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 租户数据适配管理器
 *
 * @author huangshengtao
 */
public class TenantAdapterManager {

    static Logger logger = LoggerFactory.getLogger(TenantAdapterManager.class);
    @Autowired
    DbAdapterManager dbAdapterManager;

    @Autowired
    DustDbProperties dustDbProperties;

    @Autowired
    DbManager dbManager;

    /**
     * 通过租户或者App返回数据库适配器
     * appId通常对应一个产品，加上租户信息可以返回租户下的App业务数据适配器
     *
     * @param tenantId
     * @param appId
     * @return
     */
    public ISqlAdapter getAdapter(String tenantId, String appId) {
        if (!dustDbProperties.getTenant().isEnable()) {
            return dbAdapterManager.getAdapter(null);
        }

        return dbManager.getAdapter(tenantId, appId);
    }

    public boolean isSingle() {
        return dustDbProperties.isSingle();
    }

    /**
     * 获取租户的管理适配器
     *
     * @param tenantId
     * @param appId
     * @return
     * @throws Exception
     * @see #getAdapter(String, String)
     * 通常返回App业务Schema所属数据库组（实例）的管理账户
     * 如果App没有配置group，则定位租户的默认group
     */
    public ISqlAdapter getAdapterAdmin(String tenantId, String appId) {
        if (!dustDbProperties.getTenant().isEnable()) {
            return null;
        }

        if (!dustDbProperties.getTenant().isAdmin()) {
            throw new DustDbRuntimeException("没有开启租户的管理权限");
        }

        return dbManager.getAdminAdapter(tenantId, appId);
    }

    /**
     * 获取存放租户信息的服务器，需开发权限才能操作
     *
     * @return
     */
    public ISqlAdapter getTenantConfigAdapter() {
        if (!dustDbProperties.getTenant().isEnable()) {
            return null;
        }

        if (!dustDbProperties.getTenant().isAdmin()) {
            throw new DustDbRuntimeException("没有开启租户的管理权限");
        }
        return dbAdapterManager.getAdapter(dustDbProperties.getTenant().getDatasourceName());
    }

    public static TenantAdapterManager instance() {
        return (TenantAdapterManager) BeanUtils.getBean("tenantManager");
    }
}
