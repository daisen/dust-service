package dust.service.micro.security;

/**
 * 系统参数类
 * 时间戳(timestamp)、应用ID(app_id)、签名(sign)、租户(tenantId)
 * 签名在安全处验证，该处只做标识
 * @author huangshengtao
 */
public class SysParam {
    public static final String TIMESTAMP= "timestamp";
    public static final String APP_ID= "app_id";
    public static final String TENANT_ID= "tenant_id";

    private Long timestamp;
    private String appId;
    private String tenantId;

    public SysParam() {

    }

    public SysParam(String appId, String tenantId, Long timestamp) {
        this.appId = appId;
        this.tenantId = tenantId;
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
