package dust.service.db.tenant.pojo;

import dust.service.core.util.ClassBuildUtils;

import java.util.Date;

/**
 * 分布式环境下的数据库接入口
 * 配置相应的数据库实例，不含Scheme。通过{@link #accessLevel}来区分实例的不同账户接入
 * @see #cluster
 * 对应于数据库实例
 * @see #accessLevel
 * 分为管理员Admin，读写账户ReadWrite,读账户Read,开发者Dev等，此处是数据库账户权限的区分，不决定后续的读写分离
 * @see #name
 * 对应于连接池的名字
 * @see #permission
 * 预留字段，后续纳入租户的数据控制管理
 * @author huangshengtao
 * @version 2017.3.8
 */
public class DbAccess {
    private String id;
    private String status;
    private Date gmtCreate;
    private Date gmtModified;
    private String remark;

    private String name;
    private String accessLevel;
    private String cluster;
    private String host;
    private String user;
    private String password;
    private String permission;

    public String getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(String accessLevel) {
        this.accessLevel = accessLevel;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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

    public DbAccess clone() {
        return (DbAccess) ClassBuildUtils.copyClass(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\n\tname:\"");
        sb.append(this.getName());
        sb.append("\"");

        sb.append("\n\thost:\"");
        sb.append(this.getHost());
        sb.append("\"");

        sb.append("\n\taccessLevel:\"");
        sb.append(this.getAccessLevel());
        sb.append("\"");

        sb.append("\n\tcluster:\"");
        sb.append(this.getCluster());
        sb.append("\"");

        sb.append("\n\t}");
        return sb.toString();
    }
}
