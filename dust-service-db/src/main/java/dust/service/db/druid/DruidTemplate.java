package dust.service.db.druid;

import com.alibaba.druid.pool.DruidAbstractDataSource;
import com.alibaba.druid.pool.DruidDataSource;
import dust.service.db.pool.DataSourceCache;
import dust.service.db.sql.DataBaseImpl;
import dust.service.core.util.CamelNameUtils;
import dust.service.core.util.ClassBuildUtils;
import dust.service.db.DustDbProperties;
import dust.service.db.DustDbRuntimeException;
import dust.service.db.pool.DataSourceContext;
import dust.service.db.pool.DataSourceTemplate;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Druid连接池对应的Template类，并对应于前缀为dustdb.druid配置属性
 *
 * @author huangshengtao
 * @implNote 微服务框架下，很难把握配置的多变性，因此在实现数据库中间件的过程，很难确定何种方式的限制或者访问处理更合理。暂时按照
 * 数据源的角度去考虑，后续也可能会修改成配置优先的方式。同时分布式的布局，会导致数据库连接的浪费，因此推荐连接池空闲连接配置的越小越好
 */
public class DruidTemplate implements InitializingBean, DataSourceTemplate {

    Logger logger = LoggerFactory.getLogger(DruidTemplate.class);

    private final ApplicationContext context;
    private DustDbProperties dustDbProperties;
    private DruidProperties druidProperties;

    public DruidTemplate(DustDbProperties dustDbProperties, DruidProperties druidProperties, ApplicationContext context) {
        this.dustDbProperties = dustDbProperties;
        this.druidProperties = druidProperties;
        this.context = context;
    }

    /**
     * 初始化的过程相当于获取一遍所有的数据源
     */
    public void init() {
        if (dustDbProperties.getDbList() != null) {
            List<DataSourceContext> dbList = dustDbProperties.getDbList();
            for (DataSourceContext db : dbList) {
                if (!db.isEnable()) {
                    continue;
                }

                DruidDataSource ds = createDataSource(db);
                if (ds == null) {
                    throw new DustDbRuntimeException("数据库无法访问" + db.toString());
                }
            }

            createDefaultDataSource();
        }
    }

    /**
     * 创建一个连接池数据源，加入到druidDataSources缓存集合
     * <strong>注意：</strong>如果缓存集合已经存在连接信息完全一致的数据源，则从缓存读取
     * 配置属性{@link #dustDbProperties} 如果initWhenCreate为true则会调用初始化操作
     *
     * @param db
     * @return
     */
    @SuppressWarnings("Duplicates")
    public DruidDataSource createDataSource(DataSourceContext db) {
        //如果无法找到Spring的上下文，则直接返回null
        if (this.context == null) {
            return null;
        }

        DruidDataSource cacheDs = cacheToDruidDataSource(DataSourceCache.getInstance().get(db.getName()));
        if (cacheDs != null) {
            return cacheDs;
        }

        DruidDataSource ds = createInstance();
        ds.setUrl(db.getUrl());
        ds.setUsername(db.getUsername());
        ds.setPassword(db.getPassword());
        ds.setName(db.getName());
        if (!StringUtils.isEmpty(db.getValidationQuery())) {
            ds.setValidationQuery(db.getValidationQuery());
        }

        try {
            if (dustDbProperties.isInitWhenCreate()) {
                ds.init();
            }
            DataSourceCache.getInstance().set(db, ds);
            return ds;
        } catch (SQLException sqlEx) {
            logger.error("数据库无法访问: " + ds.toString(), sqlEx);
        }

        return null;
    }

    /**
     * 验证从缓存读取的数据源是否是druid数据源，并转化初始化
     *
     * @param cacheDs 缓存数据源
     * @return
     */
    private DruidDataSource cacheToDruidDataSource(DataSource cacheDs) {
        if (cacheDs != null && cacheDs instanceof DruidDataSource) {
            DruidDataSource druidDataSource = (DruidDataSource) cacheDs;
            if (dustDbProperties.isInitWhenCreate() && !druidDataSource.isInited()) {
                try {
                    druidDataSource.init();
                } catch (SQLException ex) {
                    logger.error("数据库无法访问: " + cacheDs.toString(), ex);
                }
            }
            return druidDataSource;
        }

        return null;
    }

    /**
     * 创建默认数据源
     * 当开启{@link DustDbProperties#isSingle()}时，支持默认数据源操作
     * 当druid配置{@link DruidProperties}了数据库url，username，password时，将作为默认数据源
     * @return
     */
    public DruidDataSource createDefaultDataSource() {
        //如果无法找到Spring的上下文，则直接返回null
        if (this.context == null) {
            return null;
        }

        //如果没有开启单体应用，则不处理默认数据源
        if (!dustDbProperties.isSingle()) {
            return null;
        }

        DruidDataSource ds = createInstance();
        if (StringUtils.isEmpty(ds.getUrl()) || StringUtils.isEmpty(ds.getUsername())
                || StringUtils.isEmpty(ds.getPassword())) {
            return null;
        }

        try {
            if (dustDbProperties.isInitWhenCreate()) {
                ds.init();
            }
            DataSourceCache.getInstance().set("", ds);
            return ds;
        } catch (SQLException sqlEx) {
            logger.error("数据库无法访问: " + ds.toString(), sqlEx);
        }

        return null;

    }

    /**
     * @param name          数据源标识，同一程序，需保证唯一性
     * @param url
     * @param userName
     * @param password
     * @param validateQuery
     * @return null表示数据库未找到
     * @see #createDataSource(DataSourceContext)
     * 根据数据库信息返回相应的数据源（连接池）
     */
    public DruidDataSource createDataSource(String name, String url, String userName, String password, String validateQuery) {
        DataSourceContext dbLink = new DataSourceContext();
        dbLink.setName(name);
        dbLink.setUrl(url);
        dbLink.setUsername(userName);
        dbLink.setPassword(password);
        dbLink.setValidationQuery(validateQuery);
        return createDataSource(dbLink);
    }

    /**
     * 根据name来查找，读取缓存中的数据源（连接池),不推荐使用，通常直接调用{@link #getConnection(String)}来进行数据库操作
     *
     * @param name
     * @return
     */
    public DruidDataSource getDataSource(String name) {
        return cacheToDruidDataSource(DataSourceCache.getInstance().get(name));
    }

    /**
     * 获取连接，对应name的数据库
     *
     * @param name 区分大小写
     * @return null表示找不到数据库或者开启失败，请检查日志
     */
    public Connection getConnection(String name) throws SQLException {
        DruidDataSource ds = getDataSource(name);
        if (ds != null) {
            return ds.getConnection();
        }
        throw new SQLException("找不到指定的数据源：" + name);
    }

    /**
     * 根据name返回对应的数据库的类型,通常用于区分不同数据库执行模块
     *
     * @param name
     * @return 如果没有找到匹配的，则返回空字符串""
     */
    public String getDbType(String name) {
        DruidDataSource ds = getDataSource(name);
        if (ds != null) {
            return ds.getDbType();
        }
        return "";
    }

    /**
     * 读取name对应的数据库处理类对应的Bean名称或者类名
     * 当需要定制数据库的访问操作时，可配置自己的处理类，需继承{@link com.hisense.dustdb.sql.DataBaseImpl}
     *
     * @param name
     * @return 如果没有找到匹配的，则返回空字符串""
     */
    public String getDataBase(String name) {
        List<DataSourceContext> dbList = dustDbProperties.getDbList();
        for (DataSourceContext dbInfo : dbList) {
            if (dbInfo.getName().equals(name)) {
                return dbInfo.getDataBase();
            }
        }
        return "";
    }


    @Override
    public void afterPropertiesSet() throws Exception {
    }

    private DruidDataSource createInstance() {
        try {
            DruidDataSource ds = new DruidDataSource();
            Method[] methods = DruidAbstractDataSource.class.getMethods();
            for (Method method : methods) {
                String key = fieldOfMethod(method);
                if (key != null && druidProperties.containsKey(key)) {
                    method.invoke(ds, ClassBuildUtils.typeConvert(druidProperties.get(key), method.getParameterTypes()[0]));
                }
            }

            return ds;
        } catch (Exception ex) {
            logger.error("DruidDataSource创建失败", ex);
        }

        return null;
    }

    private String fieldOfMethod(Method method) {
        if (method.getParameterTypes().length == 1) {
            String key = method.getName();
            if (key.startsWith("set") && key.length() > 3) {
                key = key.substring(3);
                return CamelNameUtils.toLowerCaseFirstOne(key);
            }

            if (key.startsWith("is") && key.length() > 2) {
                key = key.substring(2);
                return CamelNameUtils.toLowerCaseFirstOne(key);
            }
        }

        return null;
    }
}
