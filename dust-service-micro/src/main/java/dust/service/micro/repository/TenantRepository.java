package dust.service.micro.repository;

import dust.db.DbAdapterManager;
import dust.db.sql.ISqlAdapter;
import dust.db.tenant.TenantAdapterManager;
import dust.service.micro.common.DustMsException;
import dust.service.micro.security.DustAuthentication;
import dust.service.micro.security.SysParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 用于访问数据库的库
 * @author huangshengtao
 */
public class TenantRepository {

    @Autowired(required = false)
    TenantAdapterManager tenantAdapterManager;

    @Autowired
    DbAdapterManager dbAdapterManager;

    public ISqlAdapter getAdapter(String defaultSourceName) throws DustMsException {
        //单体应用使用DbAdapterManager的逻辑
        if (tenantAdapterManager.isSingle() || tenantAdapterManager == null) {
            return dbAdapterManager.getAdapter(defaultSourceName);
        }

        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        if(authentication != null && authentication instanceof DustAuthentication) {
            SysParam sysParam = ((DustAuthentication) authentication).getSysParam();
            if (sysParam == null || StringUtils.isEmpty(sysParam.getAppId()) || StringUtils.isEmpty(sysParam.getTenantId())) {
                throw new RepositoryException("服务公共参数异常");
            }

            return tenantAdapterManager.getAdapter(sysParam.getTenantId(), sysParam.getAppId());
        }

        return dbAdapterManager.getAdapter(defaultSourceName);
    }
}
