package dust.service.db.tenant.pojo;

import java.util.Date;

/**
 * App的配置信息
 * 此处App是对租户下的App实例的特质，不是产品的概念，而是产品+租户限定下的App
 *
 * @see #appId
 * 外部App的id，通常是产品id
 * @see #appAlias
 * 产品设计过程中原则上使用appAlias作为系统的代号或者简称。
 * 在{@link #dbName}为空的情况下，dustdb会自动根据该字段以及{@link #tenantId}来构建dbName和连接数据库Schema
 * @see #dbName
 * 尽量留空，让dustdb自动匹配Schema名称，{@link #appAlias} + {@link #tenantId}
 * @author huangshengtao
 */
public class AppConfig {
    private String id;
    private String status;
    private Date gmtCreate;
    private Date gmtModified;
    private String remark;

    private String appId;
    private String appAlias;
    private String tenantId;
    private String dbName;
    private String dbAccessId;

    private DbAccess dbAccess;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public Date getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public Date getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getDbAccessId() {
        return dbAccessId;
    }

    public void setDbAccessId(String dbAccessId) {
        this.dbAccessId = dbAccessId;
    }

    public String getAppAlias() {
        return appAlias;
    }

    public void setAppAlias(String appAlias) {
        this.appAlias = appAlias;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public DbAccess getDbAccess() {
        return dbAccess;
    }

    public void setDbAccess(DbAccess dbAccess) {
        this.dbAccess = dbAccess;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\n\tid:\"");
        sb.append(this.getId());
        sb.append("\"");

        sb.append(",\n\tdbName:\"");
        sb.append(this.getDbName());
        sb.append("\"");

        sb.append(",\n\tappId:\"");
        sb.append(this.getAppId());
        sb.append("\"");

        sb.append(",\n\tdbAccessId:\"");
        sb.append(this.getDbAccessId());
        sb.append("\"");

        sb.append(",\n\t}");

        return sb.toString();
    }
}
