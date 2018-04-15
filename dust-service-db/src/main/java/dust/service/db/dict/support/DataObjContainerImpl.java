package dust.service.db.dict.support;

import com.alibaba.fastjson.JSONObject;
import dust.service.core.thread.LocalHolder;
import dust.service.core.util.Converter;
import dust.service.db.DustDbRuntimeException;
import dust.service.db.dict.DataObj;
import dust.service.db.dict.DataObjBuilder;
import dust.service.db.dict.IDataObjContainer;
import dust.service.db.sql.DataTable;
import dust.service.db.sql.ISqlAdapter;
import dust.service.db.sql.SqlCommand;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * @author huangshengtao on 2018-4-4.
 */
public class DataObjContainerImpl implements IDataObjContainer {

    private Logger logger = LoggerFactory.getLogger(DataObjContainerImpl.class);
    private final static String LOCAL_SQL_ADAPTER = "DataObjContainerImpl.LOCAL_SQL_ADAPTER";
    private final static String DEFAULT_DB = "mysql";
    private ISqlAdapter sqlAdapter;

    public void setSqlAdapter(ISqlAdapter sqlAdapter) {
        this.sqlAdapter = sqlAdapter;
    }

    public ISqlAdapter getSqlAdapter() {
        if (sqlAdapter != null) {
            return sqlAdapter;
        }

        ThreadLocal<ISqlAdapter> localSqlAdapter = LocalHolder.get(LOCAL_SQL_ADAPTER);

        ISqlAdapter adapter = localSqlAdapter.get();
        try {
            if (adapter == null || adapter.getConnection() == null) {
                localSqlAdapter.remove();

                adapter = DataObjBuilder.getSqlAdapter();
                LocalHolder.get(LOCAL_SQL_ADAPTER).set(adapter);
                if (!isSupport(localSqlAdapter.get().getDbType())) {
                    throw new DustDbRuntimeException("dict Container dbType not compatible for " + adapter.getDbType());
                }
            }
        } catch (SQLException e) {
            logger.error("data obj container open connection error", e);
            throw new DustDbRuntimeException("data obj container open connection error", e);
        }

        return adapter;
    }

    public boolean isSupport(String dbType) {
        return StringUtils.equals(dbType, DEFAULT_DB);
    }

    public SqlCommand findObjIdCommand(String app, String module, String alias) throws SQLException {
        SqlCommand cmd = new SqlCommand("" +
                "SELECT\n" +
                "  id \n" +
                "FROM\n" +
                "  dataobj\n" +
                "WHERE\n" +
                "  app = :app\n" +
                "AND module = :module\n" +
                "AND alias = :alias\n");
        cmd.setParameter("app", app);
        cmd.setParameter("module", module);
        cmd.setParameter("alias", alias);
        return cmd;
    }

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
                "WHERE\n" +
                " id=:id");
        masterCmd.setParameter("id", id);
        return masterCmd;
    }

    public SqlCommand getObjColumnCommand(Long masterId) throws SQLException {
        SqlCommand columnCmd = new SqlCommand("" +
                "SELECT\n" +
                "  id,\n" +
                "  gmt_create,\n" +
                "  gmt_modified,\n" +
                "  master_id,\n" +
                "  name,\n" +
                "  column_name,\n" +
                "  column_label,\n" +
                "  relation_table_name,\n" +
                "  relation_column_name,\n" +
                "  id_column_label,\n" +
                "  default_value,\n" +
                "  data_type,\n" +
                "  mirror_column_label,\n" +
                "  table_name,\n" +
                "  is_primary_key,\n" +
                "  is_ignore,\n" +
                "  width,\n" +
                "  decimal_digits,\n" +
                "  is_required,\n" +
                "  is_auto_increment\n" +
                "FROM\n" +
                "  dataobj_column\n" +
                "WHERE\n" +
                "  master_id = :masterId");
        columnCmd.setParameter("masterId", masterId);
        return columnCmd;
    }

    public SqlCommand getObjTableCommand(Long masterId) throws SQLException {
        SqlCommand tableCmd = new SqlCommand("" +
                "SELECT\n" +
                "  id,\n" +
                "  gmt_create,\n" +
                "  gmt_modified,\n" +
                "  master_id,\n" +
                "  table_name,\n" +
                "  alias,\n" +
                "  relation_type,\n" +
                "  column_name,\n" +
                "  relation_column,\n" +
                "  relation_where,\n" +
                "  follow_delete,\n" +
                "  follow_insert,\n" +
                "  follow_update\n" +
                "FROM\n" +
                "  dataobj_table\n" +
                "WHERE\n" +
                "  master_id = :masterId");
        tableCmd.setParameter("masterId", masterId);
        return tableCmd;
    }

    @Override
    public DataObj create(String app, String module, String alias) {
        ISqlAdapter dictSqlAdapter = getSqlAdapter();
        DataObj obj = new DataObj();
        try {
            JSONObject jsonOfObj = DataObjSchema.getInstance().getSchemaJSON(app, module, alias);
            if (jsonOfObj == null) {
                SqlCommand idCommand = findObjIdCommand(app, module, alias);
                DataTable dt = dictSqlAdapter.query(idCommand);
                if (dt.size() == 0) {
                    throw DustDbRuntimeException.create("dataObj(app:{0}, module:{1}, alias:{2}) not found" ,
                            app, module, alias);
                }

                Long id = Converter.toLong(dt.get(0, "id"));
                jsonOfObj = getJsonObjectFromDb(id, dictSqlAdapter);
            }

            obj.parseJson(jsonOfObj);
        } catch (SQLException sqle) {
            throw new DustDbRuntimeException("DataObjContainer.create exception", sqle);
        }

        return obj;
    }

    @Override
    public DataObj create(Long id) {
        ISqlAdapter dictSqlAdapter = getSqlAdapter();
        DataObj obj = new DataObj();
        try {
            JSONObject jsonOfObj = DataObjSchema.getInstance().getSchemaJSON(id);
            if (jsonOfObj == null) {
                jsonOfObj = getJsonObjectFromDb(id, dictSqlAdapter);
            }

            obj.parseJson(jsonOfObj);
        } catch (SQLException sqle) {
            throw new DustDbRuntimeException("DataObjContainer.create exception", sqle);
        }

        return obj;
    }

    public JSONObject getJsonObjectFromDb(Long id, ISqlAdapter dictSqlAdapter) throws SQLException {
        JSONObject jsonOfObj;DataTable objData = dictSqlAdapter.query(getObjCommand(id));
        DataTable columnData = dictSqlAdapter.query(getObjColumnCommand(id));
        DataTable tableData = dictSqlAdapter.query(getObjTableCommand(id));
        jsonOfObj = DataObjSchema.getInstance().toJSONSchema(objData.row(0), columnData, tableData);
        return jsonOfObj;
    }
}
