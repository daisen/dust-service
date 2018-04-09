package dust.service.db.sql;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @see SqlAdapterImpl
 * 重载连接操作，关闭事务
 * @author huangshengtao
 */
public class ReadSqlAdapterImpl extends SqlAdapterImpl {
    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = super.getConnection();
        if (connection != null) {
            connection.setAutoCommit(true);
            connection.setTransactionIsolation(Connection.TRANSACTION_NONE);
        }
        return connection;
    }
}
