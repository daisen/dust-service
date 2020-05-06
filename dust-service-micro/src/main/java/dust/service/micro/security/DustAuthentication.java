package dust.service.micro.security;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

import javax.security.auth.Subject;
import java.security.Principal;
import java.util.Collection;

/**
 * 微服务的认证信息
 *
 * @author huangshengtao
 */
public class DustAuthentication extends AbstractAuthenticationToken {

    public static final String ORGS = "orgs";
    public static final String ROLES = "roles";
    public static final String ORG_CODE = "orgCode";
    public static final String USER_KEY = "userId";

    private String credential;
    private Principal principal;
    private SysParam sysParam;

    public DustAuthentication(String userId, JSONObject userInfo) {
        this(userId, userInfo, null);
    }

    public DustAuthentication(String userId, JSONObject userInfo, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        Assert.notNull(userId, "userId cannot be null");


        this.principal = new DustPrincipal(userId);
        this.setDetails(userInfo);

        if (userInfo.containsKey(SysParam.APP_ID)) {
            if (sysParam == null) {
                sysParam = new SysParam(userInfo.getString(SysParam.APP_ID),
                        userInfo.getString(SysParam.TENANT_ID),
                        userInfo.getLong(SysParam.TIMESTAMP));
            }
            sysParam.setAppId(userInfo.getString(SysParam.APP_ID));
        }

        setAuthenticated(true);
    }

    public DustAuthentication(Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
    }

    @Override
    public Object getCredentials() {
        return credential;
    }

    public void setCredential(String credential) {
        this.credential = credential;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    public String getOrgCode() {
        JSONObject userInfo = (JSONObject) getDetails();
        if (userInfo != null) {
            return userInfo.getString(ORG_CODE);
        }
        return "";
    }

    public JSONArray getRoles() {
        JSONObject userInfo = (JSONObject) getDetails();
        if (userInfo != null && userInfo.containsKey(ROLES)) {
            return userInfo.getJSONArray(ROLES);
        }
        return new JSONArray();
    }

    public JSONArray getOrgs() {
        JSONObject userInfo = (JSONObject) getDetails();
        if (userInfo != null && userInfo.containsKey(ORGS)) {
            return userInfo.getJSONArray(ORGS);
        }
        return new JSONArray();
    }

    public SysParam getSysParam() {
        return sysParam;
    }

    public void setSysParam(SysParam sysParam) {
        this.sysParam = sysParam;
    }

    @Override
    public Object getDetails() {
        return super.getDetails();
    }

    public class DustPrincipal implements Principal {
        private final String name;

        public DustPrincipal(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return this.name;
        }
    }
}
