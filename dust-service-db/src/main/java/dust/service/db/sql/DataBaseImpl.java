package dust.service.db.sql;

import com.google.common.collect.Lists;
import com.sun.rowset.CachedRowSetImpl;
import dust.service.db.util.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.RowSet;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * jdbc相关方法的封装类，主要是数据库操作封装，查询，更新，执行
 * 封装了不同关系数据库的公用方法,
 *
 * @author huangshengtao
 */
public class DataBaseImpl implements IDataBase {

    static Logger logger = LoggerFactory.getLogger(DataBaseImpl.class);

    private Connection connection;

    /**
     * 实现接口方法，设置连接，用于后续执行操作
     *
     * @param connection
     */
    @Override
    public IDataBase setConnection(Connection connection) {
        this.connection = connection;
        return this;
    }

    /**
     * 获取连接，DataBase中使用连接都会调用该方法
     * 该方法先检测连接是否存在以及连接是否被关闭，如果不满足条件则会爆出异常
     *
     * @return
     * @throws SQLException
     */
    public Connection getConnection() throws SQLException {
        if (this.connection == null) {
            throw new SQLException("连接未创建");
        }

        if (this.connection.isClosed()) {
            this.connection = null;
            throw new SQLException("连接已关闭，请重新创建");
        }
        return this.connection;
    }

    /**
     * 根据cmd，执行查询操作，返回结果集DataTable
     * <strong>注意：</strong>非Select操作，请勿调用该方法，有些操作可能导致异常
     * 存储过程返回游标的操作也可用该方法
     *
     * @param cmd
     * @return
     * @throws Exception
     */
    @Override
    public DataTable query(SqlCommand cmd) throws SQLException {
        RowSet rs = queryRowSet(cmd);
        if (rs.isClosed()) {
            if (cmd.getCommandType() == CommandTypeEnum.StoredProcedure) {
                Map<String, Object> procParams = cmd.getParameters();
                for (String p : procParams.keySet()) {
                    StoreProcParam proc = (StoreProcParam) procParams.get(p);
                    if (proc.getParamDataType() == DataTypeEnum.CURSOR) {
                        return (DataTable) proc.getValue();
                    }
                }
            }

            throw new SQLException("row set had be closed");
        } else {
            return DbUtils.resultSet2Map(queryRowSet(cmd));
        }
    }

    /**
     * 区别于{@link #query(SqlCommand)}方法，该方法直接返回数据集，无需格式化数据和转存，性能要高一些
     * <strong>注意：</strong>使用完毕，请关闭RowSet
     *
     * @param cmd
     * @return
     * @throws SQLException
     */
    @Override
    public RowSet queryRowSet(SqlCommand cmd) throws SQLException {
        if (cmd.getCommandType() == CommandTypeEnum.StoredProcedure) {
            return executeProcedure(cmd);
        } else {
            PreparedStatement statement = this.getConnection().prepareStatement(cmd.getJdbcSql());
            try {
                return executeQuery(statement, cmd.getJdbcParameters());
            } finally {
                DbUtils.closeStatement(statement);
            }
        }
    }

    /**
     * 更新数据，可执行insert, update, delete,以及有关数据库结构的操作（需要相应的权限）
     *
     * @param cmd
     * @return
     * @throws Exception
     */
    @Override
    public int[] update(SqlCommand cmd) throws SQLException {
        if (cmd.getCommandType() == CommandTypeEnum.StoredProcedure) {
            executeProcedure(cmd);
            return new int[]{0};
        } else {
            PreparedStatement stmt = null;
            int[] rows;
            try {
                stmt = this.getConnection().prepareStatement(cmd.getJdbcSql());
                List<Object[]> ps = cmd.getJdbcParameterList();
                for (Object[] item : ps) {
                    DbUtils.fillStatement(stmt, item);
                    stmt.addBatch();
                }
                rows = stmt.executeBatch();
            } catch (SQLException ex) {
                logger.info(cmd.getJdbcSql());
                throw ex;
            }
            finally {
                DbUtils.closeStatement(stmt);
            }
            return rows;
        }
    }

    @Override
    public String getDbType() {
        return null;
    }

    /**
     * 执行存储过程，执行结果，请从参数cmd中取
     *
     * @param cmd
     * @throws SQLException
     * @see SqlCommand#getJdbcParameters()
     * @see SqlCommand#getParameter(String)
     */
    public RowSet executeProcedure(SqlCommand cmd) throws SQLException {
        List<StoreProcParam> params = orderParams(cmd);
        CallableStatement cs = this.getConnection().prepareCall(getCallSql(cmd));
        setProcParams(cs, params);
        boolean hasResult = cs.execute();

        // 输出参数处理;
        handleOut(params, cs);

        if (hasResult) {
            CachedRowSetImpl rowSet = new CachedRowSetImpl();
            rowSet.populate(cs.getResultSet());
            return rowSet;
        }

        return null;

    }

    /**
     * 用于调整存储过程参数顺序，默认操作时按照
     *
     * @param cmd
     * @return
     * @throws SQLException
     */
    protected List<StoreProcParam> orderParams(SqlCommand cmd) throws SQLException {
        Map<String, Object> paramMap = cmd.getParameters();
        Map<String, StoreProcParam> paramMapUpper = new HashMap<>();
        for (String key : paramMap.keySet()) {
            StoreProcParam p = (StoreProcParam) paramMap.get(key);
            if (p.getParamIoType() == ProcParaTypeEnum.FUNCRESULT) {
                continue;
            }
            paramMapUpper.put(key.toUpperCase(), p);
        }

        return new ArrayList<>(paramMapUpper.values());
    }


    protected void handleOut(List<StoreProcParam> params, CallableStatement cs) throws SQLException {
        List<DataTable> ds = Lists.newArrayList();
        for (int i = 0; i < params.size(); i++) {
            StoreProcParam param = params.get(i);
            if (param.getParamIoType() == ProcParaTypeEnum.INPUT) {
                continue;
            }
            Object csTmp;
            // 游标
            if (DataTypeEnum.CURSOR == param.getParamDataType()) {
                try {
                    csTmp = cs.getObject(i + 1);
                    if (null != csTmp) {
                        CachedRowSetImpl crs = new CachedRowSetImpl();
                        crs.populate((ResultSet) csTmp);
                        DataTable dt = DbUtils.resultSet2Map(crs);
                        ds.add(dt);
                        param.setValue(dt);

                    }
                } catch (SQLException e) {
                    if (!(e.getMessage().contains("Cursor is closed"))) {
                        throw e;
                    }
                }
            } else {
                csTmp = cs.getObject(i + 1);
                param.setValue(csTmp);
            }
        }
    }


    private String getCallSql(SqlCommand cmd) {
        StringBuilder strCaller = new StringBuilder("{");
        StringBuilder strParams = new StringBuilder("");
        Map<String, Object> parameters = cmd.getParameters();

        for (String key : parameters.keySet()) {
            StoreProcParam p = (StoreProcParam) parameters.get(key);
            if (p.getParamIoType() == ProcParaTypeEnum.FUNCRESULT) {
                strCaller.append("?=");
            } else {
                if (strParams.length() > 0) {
                    strParams.append(",");
                }
                strParams.append("?");
            }
        }

        strCaller.append("call ")
                .append(cmd.getCommandText())
                .append("(")
                .append(strParams)
                .append(")}");
        return strCaller.toString();
    }

    private void setProcParams(CallableStatement cs, List<StoreProcParam> params) throws SQLException {
        for (int i = 0, size = params.size(); i < size; i++) {
            StoreProcParam param = params.get(i);
            if (param.getParamIoType() != ProcParaTypeEnum.INPUT) {
                cs.registerOutParameter(i + 1, getSqlType(param));
            }
            if (param.getParamIoType() == ProcParaTypeEnum.INPUT
                    || param.getParamIoType() == ProcParaTypeEnum.INOUT) {
                Object v = param.getValue();
                if (v != null) {
                    cs.setObject(i + 1, v);
                } else {
                    int sqlType = getSqlType(param);
                    cs.setNull(i + 1, sqlType);
                }
            }
        }
    }

    protected int getSqlType(StoreProcParam p) {
        switch (p.getParamDataType()) {
            case NUMBER:
                return Types.NUMERIC;
            case DATETIME:
                return Types.DATE;
            case STRING:
                return Types.VARCHAR;
            case CLOB:
                return Types.CLOB;
            case TIMESTAMP:
                return Types.NUMERIC;
            case CURSOR:
                return Types.REF_CURSOR;
            case BLOB:
                return Types.BLOB;
            default:
                return Types.VARCHAR;
        }
    }

    protected RowSet executeQuery(PreparedStatement statement, Object[] params) throws SQLException {
        try {
            DbUtils.fillStatement(statement, params);
            ResultSet rs = statement.executeQuery();
            CachedRowSetImpl rowSet = new CachedRowSetImpl();
            rowSet.populate(rs);
            return rowSet;
        } finally {
            DbUtils.closeStatement(statement);
        }
    }

    protected Integer getTotalRows(SqlCommand cmd) throws SQLException {
        String execSql = "select count(*) as total from(" + cmd.getJdbcSql()
                + ") row_ ";
        PreparedStatement statement = getConnection().prepareStatement(execSql);
        Integer result = 0;
        RowSet rs = executeQuery(statement, cmd.getJdbcParameters());
        if (rs != null && rs.next()) {
            result = rs.getInt(1);
        }

        DbUtils.closeResultSet(rs);
        return result;
    }
}
