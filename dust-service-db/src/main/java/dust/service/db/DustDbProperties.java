package dust.service.db;

import com.google.common.collect.Lists;
import dust.service.db.pool.DataSourceContext;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * dustDb的配置文件
 *
 * @author huangshengtao
 */
@ConfigurationProperties("dust.db")
public class DustDbProperties {
    private String dateTimeFormatter;
    private String urlParameters = "useUnicode=true&characterEncoding=utf8&characterSetResults=utf8&useLegacyDatetimeCode=false&serverTimezone=GMT%2B8";
    private List<DataSourceContext> dbList = Lists.newArrayList();
    private boolean initWhenCreate;
    private String poolName;
    private boolean autoAdapterDestroy;
    private Tenant tenant = new Tenant();
    private Dict dict = new Dict();
    private boolean single;

    public boolean isSingle() {
        return single;
    }

    public void setSingle(boolean single) {
        this.single = single;
    }

    public boolean isAutoAdapterDestroy() {
        return autoAdapterDestroy;
    }

    public void setAutoAdapterDestroy(boolean autoAdapterDestroy) {
        this.autoAdapterDestroy = autoAdapterDestroy;
    }

    public String getPoolName() {
        return poolName;
    }

    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }

    public List<DataSourceContext> getDbList() {
        return dbList;
    }

    public void setDbList(List<DataSourceContext> dbList) {
        this.dbList = dbList;
    }

    public boolean isInitWhenCreate() {
        return initWhenCreate;
    }

    public void setInitWhenCreate(boolean initWhenCreate) {
        this.initWhenCreate = initWhenCreate;
    }

    public String getDateTimeFormatter() {
        return dateTimeFormatter;
    }

    public void setDateTimeFormatter(String dateTimeFormatter) {
        this.dateTimeFormatter = dateTimeFormatter;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public String getUrlParameters() {
        return urlParameters;
    }

    public void setUrlParameters(String urlParameters) {
        this.urlParameters = urlParameters;
    }

    public Dict getDict() {
        return dict;
    }

    public void setDict(Dict dict) {
        this.dict = dict;
    }

    public static class Tenant {
        private String datasourceName;
        private boolean enable;
        private boolean admin;
        private String defaultAppTenant = "*";

        public String getDefaultAppTenant() {
            return defaultAppTenant;
        }

        public void setDefaultAppTenant(String defaultAppTenant) {
            this.defaultAppTenant = defaultAppTenant;
        }

        public String getDatasourceName() {
            return datasourceName;
        }

        public void setDatasourceName(String datasourceName) {
            this.datasourceName = datasourceName;
        }

        public boolean isEnable() {
            return enable;
        }

        public void setEnable(boolean enable) {
            this.enable = enable;
        }

        public boolean isAdmin() {
            return admin;
        }

        public void setAdmin(boolean admin) {
            this.admin = admin;
        }
    }

    public static class Dict {
        private String dataSourceName;
        private boolean autoInitAdapter = true;
        private boolean enableObjId = false;
        private boolean allowColumnNameOutOfUnderscore = true;
        private String containerClass;

        public boolean isAllowColumnNameOutOfUnderscore() {
            return allowColumnNameOutOfUnderscore;
        }

        public void setAllowColumnNameOutOfUnderscore(boolean allowColumnNameOutOfUnderscore) {
            this.allowColumnNameOutOfUnderscore = allowColumnNameOutOfUnderscore;
        }

        public String getDataSourceName() {
            return dataSourceName;
        }

        public void setDataSourceName(String dataSourceName) {
            this.dataSourceName = dataSourceName;
        }

        public boolean isAutoInitAdapter() {
            return autoInitAdapter;
        }

        public void setAutoInitAdapter(boolean autoInitAdapter) {
            this.autoInitAdapter = autoInitAdapter;
        }

        public boolean isEnableObjId() {
            return enableObjId;
        }

        public void setEnableObjId(boolean enableObjId) {
            this.enableObjId = enableObjId;
        }

        public String getContainerClass() {
            return containerClass;
        }

        public void setContainerClass(String containerClass) {
            this.containerClass = containerClass;
        }
    }


}
