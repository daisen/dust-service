package dust.service.db.sql;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 写操作数据适配器
 * @author huangshengtao
 */
public class WriteSqlAdapterImpl extends SqlAdapterImpl {
    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = super.getConnection();
        if (connection != null) {
            connection.setAutoCommit(true);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        }
        return connection;
    }
}
