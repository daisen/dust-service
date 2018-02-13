package dust.service.micro.security;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.security.Principal;
import java.util.Collection;

/**
 * 微服务的认证信息
 * @author huangshengtao
 */
public class DustAuthentication extends AbstractAuthenticationToken {

    public static final String ORGS= "orgs";
    public static final String ROLES= "roles";
    public static final String ORG_CODE = "orgCode";
    private static final String USER_KEY = "userId";
    private JSONObject credential;
    private Principal principal;
    private SysParam sysParam;


    public DustAuthentication(JSONObject userInfo) {
        super(null);
        if (userInfo != null) {
            String user = userInfo.getString(USER_KEY);
            if (!StringUtils.isEmpty(user)) {
                this.credential = (JSONObject) userInfo.clone();
                this.principal = new Principal() {
                    @Override
                    public boolean equals(Object another) {
                        if (another instanceof String) {
                            return user.equals(another);
                        }

                        if (another instanceof Principal) {
                            return user.equals(((Principal) another).getName());
                        }
                        return false;
                    }

                    @Override
                    public String toString() {
                        return user;
                    }

                    @Override
                    public int hashCode() {
                        return 0;
                    }

                    @Override
                    public String getName() {
                        return user;
                    }
                };

                if (userInfo.containsKey(SysParam.APP_ID)) {
                    if (sysParam == null) {
                        sysParam = new SysParam(userInfo.getString(SysParam.APP_ID),
                                userInfo.getString(SysParam.TENANT_ID),
                                userInfo.getLong(SysParam.TIMESTAMP));
                    }
                    sysParam.setAppId(userInfo.getString(SysParam.APP_ID));
                }
            }
        }
    }

    public DustAuthentication(Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
    }

    @Override
    public Object getCredentials() {
        return credential;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    public String getOrgCode() {
        if (credential != null) {
            return credential.getString(ORG_CODE);
        }
        return "";
    }

    public JSONArray getRoles() {
        if (credential != null && credential.containsKey(ROLES)) {
            return credential.getJSONArray(ROLES);
        }
        return new JSONArray();
    }

    public JSONArray getOrgs() {
        if (credential != null && credential.containsKey(ORGS)) {
            return credential.getJSONArray(ORGS);
        }
        return new JSONArray();
    }

    public SysParam getSysParam() {
        return sysParam;
    }

    public void setSysParam(SysParam sysParam) {
        this.sysParam = sysParam;
    }

    public JSONObject getUserInfo() {
        return this.credential;
    }
}
