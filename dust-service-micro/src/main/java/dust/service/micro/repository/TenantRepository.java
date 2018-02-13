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
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        if(authentication != null && authentication instanceof DustAuthentication) {
            SysParam sysParam = ((DustAuthentication) authentication).getSysParam();
            if (sysParam == null || StringUtils.isEmpty(sysParam.getAppId()) || StringUtils.isEmpty(sysParam.getTenantId())) {
                throw new DustMsException("服务公共参数异常");
            }

            return tenantAdapterManager.getAdapter(sysParam.getTenantId(), sysParam.getAppId());
        }

        return dbAdapterManager.getAdapter(defaultSourceName);
    }
}
