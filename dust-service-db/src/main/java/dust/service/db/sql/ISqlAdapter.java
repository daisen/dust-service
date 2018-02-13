package dust.service.db.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * 处理与Sql有关的适配器，用于面向不同的数据库的适配
 * 开放相应的方法用于获取数据和修改数据
 *
 * @author : huangshengtao
 */
public interface ISqlAdapter {

    Connection getConnection() throws SQLException;

    void commit() throws SQLException;

    void rollback() throws SQLException;

    void close() throws SQLException;

    void rollbackQuiet();

    void closeQuiet();

    void commitAndCloseQuiet();

    void init(String datasourceName) throws SQLException;

    DataTable query(SqlCommand sqlcommand) throws SQLException;

    DataTable query(String sql, Map<String, Object> params) throws SQLException;

    int[] update(String sql, Map<String, Object> params) throws SQLException;

    int[] update(SqlCommand sqlcommand) throws SQLException;

    void useDbName(String dbName) throws SQLException;

    String getDbType();
}
