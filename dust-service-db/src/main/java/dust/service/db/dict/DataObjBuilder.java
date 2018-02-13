package dust.service.db.dict;

import dust.service.core.util.BeanUtils;
import dust.service.db.DbAdapterManager;
import dust.service.db.DustDbRuntimeException;
import dust.service.db.dict.builder.DataObjContainer4Mysql;
import dust.service.db.sql.ISqlAdapter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author huangshengtao on 2018-1-22.
 */
@Component
public class DataObjBuilder {
    @Autowired(required = false)
    DbAdapterManager dbAdapterManager;

    static DataObjBuilder instance;
    private boolean inited = false;
    private IDataObjContainer dataObjContainer;

    public DataObjBuilder() {
        if (instance == null) {
            instance = this;
        }
    }

    private void init() {
        if (this.inited) {
            return;
        }

        this.inited = true;

        String clzName = DictGlobalConfig.getContainerClass();
        if (StringUtils.isNotEmpty(clzName)) {
            Object containerObj = BeanUtils.getBean(clzName);
            if (containerObj == null || containerObj instanceof IDataObjContainer) {
                throw new DustDbRuntimeException("container class not compatible");
            }
            instance.dataObjContainer = (IDataObjContainer) containerObj;
        } else {
            instance.dataObjContainer = new DataObjContainer4Mysql();
        }
    }

    public static ISqlAdapter getSqlAdapter() {
        if (instance.dbAdapterManager == null) {
            throw new DustDbRuntimeException("bean dbAdapterManager not found");
        }

        String dsName = DictGlobalConfig.getDataSourceName();
        ISqlAdapter sqlAdapter = instance.dbAdapterManager.getAdapter(dsName);
        if (sqlAdapter == null) {
            throw new DustDbRuntimeException("DataSource of " + dsName + "  not found");
        }
        return sqlAdapter;
    }

    public static DataObj create(String app, String module, String key) {
        if (instance == null) {
            throw new NullPointerException("DataObjBuilder have not init");
        }

        instance.init();
        return instance.dataObjContainer.create(app, module, key);
    }
}
