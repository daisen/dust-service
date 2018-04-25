package dust.service.db.support.mssql;

import dust.service.db.sql.CommandTypeEnum;
import dust.service.db.sql.DataBaseImpl;
import dust.service.db.sql.SqlCommand;
import dust.service.db.support.DataBaseFactory;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.RowSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 适配SqlServer数据库操作
 * @author huangshengtao on 2018-4-25.
 */
public class SqlServerDataBase extends DataBaseImpl {

    static Logger logger = LoggerFactory.getLogger(SqlServerDataBase.class);

    @Override
    public RowSet queryRowSet(SqlCommand cmd) throws SQLException {
        if (cmd.getCommandType() == CommandTypeEnum.StoredProcedure) {
            return super.queryRowSet(cmd);
        }

        String execSql = "";
        boolean tmpIgnoreOrder = cmd.isIgnoreOrder();
        try {
            Object[] params = cmd.getJdbcParameters();
            if (cmd.getPageSize() > 0) {
                if (cmd.getTotalRows() <= 0) {
                    cmd.setTotalRows(getTotalRows(cmd));
                }

                cmd.setIgnoreOrder(true);
                execSql = "SELECT * FROM (SELECT row_number () OVER (ORDER BY " + cmd.getOrder() + ") AS rownum_ ,* " +
                        "FROM (" + cmd.getJdbcSql() + ") datarow_ ) row_ WHERE rownum_ >= ? AND rownum_ <= ?";

                params = ArrayUtils.addAll(params, new Object[]{cmd.getBeginIndex() + 1, cmd.getEndIndex() + 1});
            }
            PreparedStatement statement = getConnection().prepareStatement(execSql);
            RowSet rs = executeQuery(statement, params);
            if (cmd.getTotalRows() <= 0) {
                cmd.setTotalRows(rs.getFetchSize());
            }
            return rs;
        } catch (SQLException se) {
            logger.error(execSql);
            throw se;
        } finally {
            cmd.setIgnoreOrder(tmpIgnoreOrder);
        }
    }

    @Override
    public String getDbType() {
        return DataBaseFactory.JdbcConstants.SQL_SERVER;
    }
}
