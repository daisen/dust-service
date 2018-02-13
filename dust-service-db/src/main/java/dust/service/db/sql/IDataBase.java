package dust.service.db.sql;

import javax.sql.RowSet;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 定义了基本的数据库操作，用于数据库适配器使用{@link ISqlAdapter}
 * 通常每个ISqlAdapter拥有自己的IDatabase，连接在ISqlAdapter管理
 * 根据数据库的不同实现各自的接口方法
 */
public interface IDataBase {

    IDataBase setConnection(Connection connection);

    DataTable query(SqlCommand cmd) throws SQLException;

    RowSet queryRowSet(SqlCommand cmd) throws SQLException;

    int[] update(SqlCommand cmd) throws SQLException;

    String getDbType();
}