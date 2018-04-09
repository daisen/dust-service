package dust.service.db.dict.support;

import dust.service.db.sql.SqlCommand;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;

/**
 * @author huangshengtao on 2018-3-22.
 */
public class DataObjContainer4Oracle extends DataObjContainerImpl {

    @Override
    public SqlCommand getObjCommand(Long id) throws SQLException {

        SqlCommand masterCmd = new SqlCommand("" +
                "SELECT\n" +
                "  id,\n" +
                "  gmt_create,\n" +
                "  gmt_modified,\n" +
                "  alias,\n" +
                "  app,\n" +
                "  module,\n" +
                "  name,\n" +
                "  table_name,\n" +
                "  conditions,\n" +
                "  fix_condition,\n" +
                "  orders,\n" +
                "  start_index,\n" +
                "  page_size,\n" +
                "  fix_where_sql,\n" +
                "  order_by_sql,\n" +
                "  where_sql\n" +
                "FROM\n" +
                "  dataobj\n" +
                "WHERE id = :id");
        masterCmd.setParameter("id", id);
        return masterCmd;
    }

    @Override
    public boolean isSupport(String dbType) {
        return StringUtils.equals(dbType, "oracle");
    }
}
