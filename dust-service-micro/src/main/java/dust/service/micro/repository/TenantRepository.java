package dust.service.micro.repository;

import dust.service.db.DbAdapterManager;
import dust.service.db.TenantAdapterManager;
import dust.service.db.sql.ISqlAdapter;
import dust.service.micro.common.DustMsException;
import dust.service.micro.security.DustAuthentication;
import dust.service.micro.security.SysParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 用于访问数据库的库
 * @author huangshengtao
 */
@Component
public class TenantRepository {
    @Autowired
    TenantAdapterManager tenantAdapterManager;

    @Autowired
    DbAdapterManager dbAdapterManager;

    public ISqlAdapter getAdapter(String defaultSourceName) throws DustMsException {
        //单体应用使用DbAdapterManager的逻辑
        if (tenantAdapterManager.isSingle()) {
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

        ISqlAdapter adapter = dbAdapterManager.getAdapter(defaultSourceName);
        if (adapter == null) {
            throw new RepositoryException("没有找到当前用户对应的数据库");
        }
        return adapter;
    }
}
