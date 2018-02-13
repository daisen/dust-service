package dust.service.db.tenant.pojo;


import com.google.common.collect.Lists;
import dust.service.core.util.ClassBuildUtils;

import java.util.Date;
import java.util.List;

/**
 * 暂不支持对租户的管理
 *
 * 租户信息，支持租户名称，id，签名等的租户选择
 * 可配置默认的数据库，方便后续App的Schema的处理
 * @author huangshengtao
 */
@Deprecated
public class Tenant {
    private String id;
    private String name;
    //用户不想用租户信息进行访问的操作
    private String sign;
    //默认数据库实例
    private String defaultGroup;
    private String status;
    private Date gmtCreate;
    private Date gmtModified;
    private String remark;

    //租户下设的App数据库
    private List<AppConfig> appList = Lists.newArrayList();

    public Tenant clone() {
        return (Tenant) ClassBuildUtils.copyClass(this);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDefaultGroup() {
        return defaultGroup;
    }

    public void setDefaultGroup(String defaultGroup) {
        this.defaultGroup = defaultGroup;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<AppConfig> getAppList() {
        return appList;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\n\tname:\"");
        sb.append(this.getName());
        sb.append("\"");

        sb.append("\n\tid:\"");
        sb.append(this.getId());
        sb.append("\"");

        sb.append("\n\tstatus:\"");
        sb.append(this.getStatus());
        sb.append("\"");

        sb.append("\n\t}");
        return sb.toString();
    }
}

