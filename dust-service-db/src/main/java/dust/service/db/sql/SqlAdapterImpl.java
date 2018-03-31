package dust.service.db.sql;

import dust.service.db.pool.DataSourceTemplate;
import dust.service.db.support.DataBaseFactory;
import dust.service.db.pool.DataSourceCache;
import dust.service.db.pool.DataSourceContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * 默认数据库适配器
 * 管理本次使用的数据源，连接
 *
 * @author huangshengtao
 */
@Service("sqlAdapter")
@Scope("prototype")
public class SqlAdapterImpl implements ISqlAdapter {
    static Logger logger = LoggerFactory.getLogger(SqlAdapterImpl.class);

    @Autowired
    private ApplicationContext context;

    @Autowired
    private DataSourceTemplate dataSourceTemplate;

    private IDataBase dataBase;
    private String name;
    private Connection connection;

    /**
     * 获取数据库连接
     * <ul>
     * <li>连接已关闭时,会报出异常</li>
     * <li>{@link SqlAdapterImpl#init(String)}调用前,执行方法,会报异常</li>
     * <li>{@link SqlAdapterImpl#close()}执行后, 返回null</li>
     * <li>异常需自行处理</li>
     * </ul>
     *
     *
     * @return
     * @throws SQLException 连接异常或者未初始化
     */
    public Connection getConnection() throws SQLException {
        if (dataBase == null) {
            throw new SQLException("sql adapter not init");
        }

        if (connection != null && connection.isClosed()) {
            throw new SQLException("sql adapter connect had close, please check code ");
        }

        return connection;
    }

    /**
     * 提交事务，异常抛出
     *
     * @throws SQLException
     */
    @Override
    public void commit() throws SQLException {
        if (connection == null) return;
        connection.commit();
    }

    /**
     * 回滚事务，不考虑是否成功
     * 失败记录日志
     */
    public void rollbackQuiet() {
        try {
            rollback();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    /**
     * 回滚事务，失败报出异常
     *
     * @throws SQLException
     */
    @Override
    public void rollback() throws SQLException {
        if (connection == null) return;
        connection.rollback();
    }

    /**
     * 关闭连接，不考虑是否成功
     * 失败记录日志
     */
    @Override
    public void closeQuiet() {
        try {
            close();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    @Override
    public void commitAndCloseQuiet() {
        if (connection == null) return;

        try {
            connection.commit();
        } catch (SQLException e) {
            logger.error("提交关闭连接异常", e);
            rollbackQuiet();
            closeQuiet();
        }
    }

    /**
     * 关闭连接，不管是否成功，都会置空connect属性
     *
     * @throws SQLException
     */
    @Override
    public void close() throws SQLException {
        if (connection == null) return;
        try {
            connection.close();
        } finally {
            connection = null;
        }
    }

    /**
     * 初始化适配器，适配器需搭配数据源名称才能生效
     * 通过参数dbName，确定适配器关联的数据库以及数据库操作
     * dbName为空时，使用默认数据源，对应名称“default”
     * 默认会根据数据库类型来工厂产生默认数据库访问类，也可通过配置文件自定义访问类
     *
     * @param datasourceName 数据源名称
     * @throws SQLException
     */
    @Override
    public void init(String datasourceName) throws SQLException {
        if (dataBase != null) {
            return;
        }

        this.name = datasourceName;

        try {
            DataSource ds = dataSourceTemplate.getDataSource(name);
            if (ds == null) {
                throw new SQLException("数据源" + name + "未能找到");
            }
            connection = ds.getConnection();
            openTransaction();

            DataSourceContext ctx = DataSourceCache.getInstance().getContext(this.name);
            if (ctx != null) {
                if (StringUtils.isNoneEmpty(ctx.getDataBase())) {
                    dataBase = (IDataBase) this.context.getBean(ctx.getDataBase());
                }
                if (dataBase == null) {
                    dataBase = DataBaseFactory.create(ctx.getUrl());
                }
            }

            if (dataBase == null) {
                dataBase = DataBaseFactory.create(null);
            }

        } catch (Exception ex) {
            closeQuiet();
            throw ex;
        }

        dataBase.setConnection(connection);
    }

    /**
     * 根据Command返回相应的查询结果
     *
     * @param sqlcommand
     * @return {@link DataTable}
     * @throws SQLException
     */
    @Override
    public DataTable query(SqlCommand sqlcommand) throws SQLException {
        Assert.notNull(sqlcommand, "sqlCommand不允许为空");
        return dataBase.query(sqlcommand);
    }

    /**
     * 直接执行Sql，params的key使用:key的方式存放参数
     *
     * @param sql
     * @param params
     * @return
     * @throws SQLException
     */
    @Override
    public DataTable query(String sql, Map<String, Object> params) throws SQLException {
        Assert.notNull(sql, "sql不允许为空");

        SqlCommand sqlCommand = new SqlCommand(sql);
        if (params != null) {
            sqlCommand.appendParameters(params);
        }

        return query(sqlCommand);
    }

    /**
     * 执行非查询操作
     * <strong>注意：</strong>开启事务才能使用该方法，否则会爆出异常
     *
     * @param sql
     * @param params
     * @return
     * @throws Exception
     */
    @Override
    public int[] update(String sql, Map<String, Object> params) throws SQLException {
        Assert.notNull(sql, "sql不允许为空");
        SqlCommand sqlCommand = new SqlCommand(sql);
        if (params != null) {
            sqlCommand.appendParameters(params);
        }

        return update(sqlCommand);
    }

    /**
     * @param sqlCommand
     * @return
     * @throws SQLException
     * @see #update(String, Map)
     */
    @Override
    public int[] update(SqlCommand sqlCommand) throws SQLException {
        Assert.notNull(sqlCommand, "sqlCommand不允许为空");

        return dataBase.update(sqlCommand);
    }

    /**
     * 事务标识，决定连接是否开启事务
     *
     * @return
     */
    protected void openTransaction() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        }
    }

    /**
     * 切换至其他数据库
     *
     * @param dbName
     * @throws SQLException
     */
    @Override
    public void useDbName(String dbName) throws SQLException {
        if (dataBase == null) {
            throw new SQLException("sql adapter not init");
        }

        if (dataBase.getClass().getSimpleName().contains("MySql")) {
            this.update("use " + dbName, null);
        }
    }

    @Override
    public String getDbType() {
        if (dataBase == null) {
            throw new IllegalStateException("sql adapter not init");
        }

        return this.dataBase.getDbType();
    }
}
