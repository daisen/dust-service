package dust.service.db.sql;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @see SqlAdapterImpl
 * 重载连接操作，关闭事务
 * @author huangshengtao
 */
@Component("readSqlAdapterImpl")
@Scope("prototype")
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
