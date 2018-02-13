package dust.service.db.sql;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 写操作数据适配器
 * @author huangshengtao
 */
@Component("writeSqlAdapterImpl")
@Scope("prototype")
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
