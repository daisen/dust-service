package dust.service.db.util;

import dust.service.db.sql.DataRow;
import dust.service.db.sql.DataTable;
import dust.service.core.util.Converter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

/**
 * 数据库操纵工具类，如处理sql参数以及关闭相关连接等
 *
 * @author huangshengtao
 */
public class DbUtils {

    public static Logger logger = LoggerFactory.getLogger(DbUtils.class);

    /**
     * 处理Sql语句的参数
     * 根据参数的类型决定生成的方，目前支持三种方式
     * 1. {@link java.util.Date} 会转化为 {@link Timestamp}
     * 2. {@link byte[]} 会转化为 {@link Clob}
     * 3. 其他类型按照Object处理，交由jdbc自行处理
     *
     * @param stmt
     * @param params
     * @throws SQLException
     */
    public static void fillStatement(PreparedStatement stmt, Object[] params) throws SQLException {
        if (params == null || stmt == null) {
            return;
        }

        for (int len = params.length, i = 0; i < len; i++) {
            Object p = params[i];
            int index = i + 1;
            if (p != null) {
                //时间日期
                if (p.getClass().isAssignableFrom(java.util.Date.class)) {
                    stmt.setTimestamp(index, new Timestamp(((java.util.Date) p).getTime()));
                    continue;
                }

                //字节流
                if (p.getClass().isAssignableFrom(byte[].class)) {
                    Clob clob = stmt.getConnection().createClob();
                    clob.setString(1, p.toString());
                    continue;
                }

                //其他
                stmt.setObject(index, p);
            } else {
                stmt.setNull(index, Types.VARCHAR);
            }
        }
    }

    /**
     * @param stmt
     * @param params
     * @throws SQLException
     * @see #fillStatement(PreparedStatement, Object[]) 的List参数方式
     */
    public static void fillStatement(PreparedStatement stmt, List<Object> params) throws SQLException {
        fillStatement(stmt, params.toArray());
    }

    /**
     * 数据集转为DataTable
     *
     * @param rs
     * @return
     */
    public static DataTable resultSet2Map(ResultSet rs) {
        DataTable result = new  DataTable();
        if (rs == null) {
            return result;
        }

        try {
            while (rs.next()) {
                DataRow dr = new DataRow();
                ResultSetMetaData rsmd = rs.getMetaData();
                int columnCount = rsmd.getColumnCount();
                for (int i = 1; i < columnCount + 1; i++) {// count i
                    String fieldName = rsmd.getColumnLabel(i).toLowerCase();
                    if (StringUtils.isEmpty(fieldName)) {
                        fieldName = rsmd.getColumnName(i).toLowerCase();
                    }
                    String fieldClassName = rsmd.getColumnClassName(i);
                    dr.set(fieldName, DbUtils.dbValueToString(rs, fieldClassName, i));
                }
                result.addRow(dr);
            }
        } catch (SQLException ex) {
            logger.error("数据集转化List失败", ex);
        }
        closeResultSet(rs);
        return result;
    }

    /**
     * 根据数据集的数据转化为字符串，无法转化则返回null
     *
     * @param rs
     * @param fieldClassName
     * @param fieldName
     * @return
     */
//    public static String dbValueToString(ResultSet rs, String fieldClassName, String fieldName) {
//        try {
//            if (null == rs.getObject(fieldName)) {
//                return null;
//            }
//            if (fieldClassName.equals("java.sql.Timestamp")
//                    || fieldClassName.equals("java.sql.Time")
//                    || fieldClassName.equals("java.sql.Date")
//                    || fieldClassName.equals("java.util.Date")) {
//                java.util.Date orginDate = rs.getTimestamp(fieldName);
//                return Converter.toString(orginDate);
//            }
//
//            if (fieldClassName.contains("CLOB")) {// "java.sql.Clob"
//
//                Clob clob = rs.getClob(fieldName);
//                if (clob != null) {
//                    return clob.getSubString(1, (int) clob.length());
//                } else {
//                    return null;
//                }
//            }
//
//            if (fieldClassName.contains("BLOB")) {// java.sql.Blob
//                Blob blob = rs.getBlob(fieldName);
//                if (blob != null) {
//                    byte[] bytes = blob.getBytes(1, (int) blob.length());
//                    return new String(bytes);
//                } else {
//                    return null;
//                }
//            }
//
//            if (fieldClassName.equals("[B") || fieldClassName.equals("byte[]")) {
//                byte[] s = rs.getBytes(fieldName);
//                return new String(s);
//            }
//            return rs.getObject(fieldName).toString();
//        } catch (Exception ex) {
//            logger.error("数据库数据转化为java字符串失败", ex);
//            return null;
//        }
//    }

    /**
     * 根据数据集的数据转化为字符串，无法转化则返回null
     *
     * @param rs
     * @param fieldClassName
     * @param index
     * @return
     */
    public static String dbValueToString(ResultSet rs, String fieldClassName, int index) {
        try {
            if (null == rs.getObject(index)) {
                return null;
            }
            if (fieldClassName.equals("java.sql.Timestamp")
                    || fieldClassName.equals("java.sql.Time")
                    || fieldClassName.equals("java.sql.Date")
                    || fieldClassName.equals("java.util.Date")) {
                java.util.Date orginDate = rs.getTimestamp(index);
                return Converter.toString(orginDate);
            }

            if (fieldClassName.equals("java.sql.Clob")) {// "java.sql.Clob"

                Clob clob = rs.getClob(index);
                if (clob != null) {
                    return clob.getSubString(1, (int) clob.length());
                } else {
                    return null;
                }
            }

            if (fieldClassName.equals("java.sql.Blob")) {// java.sql.Blob
                Blob blob = rs.getBlob(index);
                if (blob != null) {
                    byte[] bytes = blob.getBytes(1, (int) blob.length());
                    return new String(bytes);
                } else {
                    return null;
                }
            }

            if (fieldClassName.equals("[B") || fieldClassName.equals("byte[]")) {
                byte[] s = rs.getBytes(index);
                return new String(s);
            }
            return rs.getObject(index).toString();
        } catch (Exception ex) {
            logger.error("数据库数据转化为java字符串失败", ex);
            return null;
        }
    }


    /**
     * 关闭数据集
     *
     * @param rs
     */
    public static void closeResultSet(ResultSet rs) {
        try {
            if (null != rs) {
                rs.close();
            }
        } catch (SQLException ex) {
            logger.error("关闭数据集失败", ex);
        }
    }

    /**
     * 关闭语句，如果发生错误会记录到日志
     *
     * @param stmt
     * @throws Exception
     */
    public static void closeStatement(Statement stmt) {
        try {
            if (null != stmt) {
                stmt.close();
            }
        } catch (SQLException ex) {
            logger.error("语句关闭失败", ex);
        }
    }

    /**
     * 关闭连接
     *
     * @param conn
     * @throws SQLException
     */
    public static void close(Connection conn) throws SQLException {
        if (conn != null) {
            conn.close();
        }
    }

    /**
     * @param conn
     * @see #close(Connection) 静默方法，不会抛出异常，但是会记录日志
     */
    public static void closeQuietly(Connection conn) {
        try {
            close(conn);
        } catch (SQLException e) {
            logger.error("Connection关闭失败", e);
        }
    }

    /**
     * 提交事务
     * @param conn
     * @throws SQLException
     */
    public static void commit(Connection conn) throws SQLException {
        if (conn != null) {
            conn.commit();
        }
    }

    /**
     * 提交事务并关闭连接
     * <Strong>注意:</Strong>该方法有可能事务提交成功，关闭连接失败，如果发生异常注意根据异常进行区分
     * <Strong>注意:</Strong>该方法事务提交异常，默认先回滚事务，再进行关闭连接
     * @param conn
     * @throws SQLException
     */
    public static void commitAndClose(Connection conn) throws SQLException {
        if (conn != null) {
            try {
                commit(conn);
            } catch (SQLException e) {
                rollbackAndCloseQuietly(conn);
                throw new SQLException("事务提交失败", e);
            }

            try {
                close(conn);
            } catch (SQLException ex) {
                throw new SQLException("事务成功，连接关闭失败", ex);
            }
        }
    }

    /**
     * @see #commitAndClose(Connection) 逻辑一致，不会抛出异常，仅记录日志
     * @param conn
     */
    public static void commitAndCloseQuietly(Connection conn) {
        try {
            commitAndClose(conn);
        } catch (SQLException ex) {
            logger.error("提交事务并关闭连接失败", ex);
        }
    }

    /**
     * 回滚事务
     * @param conn
     * @throws SQLException
     */
    public static void rollback(Connection conn) throws SQLException {
        if (conn != null) {
            conn.rollback();
        }
    }

    /**
     * 静默回滚事务
     * @see #rollback(Connection)
     * @param conn
     */
    public static void rollbackQuietly(Connection conn) {
        if (conn != null) {
            try {
                rollback(conn);
            } catch (Exception e) {
            }
        }
    }

    /**
     * 回滚事务并关闭连接
     * <strong>注意：</strong>方法可能回滚成功，连接关闭失败，可根据异常进行判断
     * @param conn
     * @throws SQLException
     */
    public static void rollbackAndClose(Connection conn) throws SQLException {
        if (conn != null) {
            try {
                rollback(conn);
            } catch (SQLException e) {
                closeQuietly(conn);
                throw new SQLException("事务回滚失败", e);
            }

            try {
                close(conn);
            } catch (SQLException ex) {
                throw new SQLException("事务回滚成功，连接关闭失败", ex);
            }
        }
    }

    /**
     * @see #rollbackAndClose(Connection)
     * 不会排除异常，但记录日志
     * @param conn
     */
    public static void rollbackAndCloseQuietly(Connection conn) {
        try {
            rollbackAndClose(conn);
        } catch (SQLException e) {
            logger.error("事务回滚并关闭连接失败", e);
        }
    }
}
