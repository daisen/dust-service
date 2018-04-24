package dust.service.db.support.mysql;

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
 * 支持MySql的DataBase实现
 * @author huangshengtao
 */
public class MySqlDataBase extends DataBaseImpl {
    static Logger logger = LoggerFactory.getLogger(MySqlDataBase.class);

    @Override
    public RowSet queryRowSet(SqlCommand cmd) throws SQLException {
        if (cmd.getCommandType() == CommandTypeEnum.StoredProcedure) {
            return super.queryRowSet(cmd);
        }

        String execSql = cmd.getJdbcSql();
        try {
            Object[] params = cmd.getJdbcParameters();
            if (cmd.getPageSize() > 0) {
                if (cmd.getPageSize() > 0 && cmd.getTotalRows() <= 0) {
                    cmd.setTotalRows(getTotalRows(cmd));
                }
                execSql = execSql + " limit ?,?";

                params = ArrayUtils.addAll(params, new Object[]{cmd.getBeginIndex(), cmd.getPageSize()});
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
        }
    }

    @Override
    public String getDbType() {
        return DataBaseFactory.JdbcConstants.MYSQL;
    }
}
