package dust.service.db.dict.builder;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import dust.service.core.util.CamelNameUtils;
import dust.service.db.dict.DataObj;
import dust.service.db.dict.DataObjBuilder;
import dust.service.db.sql.DataTable;
import dust.service.db.sql.SqlCommand;
import dust.service.core.util.Converter;
import dust.service.db.DustDbRuntimeException;
import dust.service.db.dict.IDataObjContainer;
import dust.service.db.sql.DataRow;
import dust.service.db.sql.ISqlAdapter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * @author huangshengtao on 2018-1-23.
 */
public class DataObjContainer4Mysql implements IDataObjContainer {
    private Logger logger = LoggerFactory.getLogger(DataObjContainer4Mysql.class);

    private final static ThreadLocal<ISqlAdapter> localSqlAdapter = new ThreadLocal<>();
    private ISqlAdapter sqlAdapter;

    public void setSqlAdapter(ISqlAdapter sqlAdapter) {
        this.sqlAdapter = sqlAdapter;
    }

    public ISqlAdapter getSqlAdapter() {
        if (sqlAdapter != null) {
            return sqlAdapter;
        }

        ISqlAdapter adapter = localSqlAdapter.get();
        try {
            if (adapter == null || adapter.getConnection() == null) {
                localSqlAdapter.remove();

                adapter = DataObjBuilder.getSqlAdapter();
                localSqlAdapter.set(adapter);
                if (!StringUtils.equals(localSqlAdapter.get().getDbType(), "mysql")) {
                    throw new DustDbRuntimeException("dict Container dbType not compatible");
                }
            }
        } catch (SQLException e) {
            throw new DustDbRuntimeException("data obj container open connection error", e);
        }

        return  adapter;
    }

    @Override
    public DataObj create(String app, String module, String key) {
        ISqlAdapter dictSqlAdapter = getSqlAdapter();
        DataObj obj = new DataObj();
        try {
            SqlCommand masterCmd = new SqlCommand("" +
                    "SELECT\n" +
                    "  `id`,\n" +
                    "  `gmt_create`,\n" +
                    "  `gmt_modified`,\n" +
                    "  `alias`,\n" +
                    "  `app`,\n" +
                    "  `module`,\n" +
                    "  `name`,\n" +
                    "  `table_name`,\n" +
                    "  `conditions`,\n" +
                    "  `fix_condition`,\n" +
                    "  `orders`,\n" +
                    "  `start`,\n" +
                    "  `page_size`,\n" +
                    "  `fix_where_sql`,\n" +
                    "  `order_by_sql`,\n" +
                    "  `where_sql`\n" +
                    "FROM\n" +
                    "  dataobj\n" +
                    "WHERE\n" +
                    "  app = :app\n" +
                    "AND module = :module\n" +
                    "AND (id = :id OR alias=:alias)");
            masterCmd.setParameter("app", app);
            masterCmd.setParameter("module", module);
            masterCmd.setParameter("id", key);
            masterCmd.setParameter("alias", key);

            DataTable dt = dictSqlAdapter.query(masterCmd);
            if (dt.size() == 0) {
                throw new DustDbRuntimeException("dataObj not found for " + key);
            }

            DataRow dr = dt.getRows().get(0);
            JSONObject jsonOfObj = row2CamelJson(dr);
//            jsonOfObj.put("name", dr.get("name"));
//            jsonOfObj.put("alias", dr.get("alias"));
//            jsonOfObj.put("tableName", dr.get("table_name"));
//            jsonOfObj.put("whereSql", dr.get("where_sql"));
//            jsonOfObj.put("orderBySql", dr.get("order_by_sql"));
//            jsonOfObj.put("fixWhereSql", dr.get("fix_where_sql"));
//            jsonOfObj.put("fixCondition", dr.get("fix_condition"));

            JSONObject p = new JSONObject();
            p.put("start", dr.get("start"));
            p.put("pageSize", dr.get("page_size"));
            jsonOfObj.put("pageInfo", p);

            if (StringUtils.isNotEmpty(dr.get("conditions"))) {
                jsonOfObj.put("conditions", JSON.parseArray(dr.get("conditions")));
            }

            if (StringUtils.isNotEmpty(dr.get("orders"))) {
                jsonOfObj.put("conditions", JSON.parseArray(dr.get("orders")));
            }

            String masterId = dr.get("id");
            SqlCommand columnCmd = new SqlCommand("" +
                    "SELECT\n" +
                    "  `id`,\n" +
                    "  `gmt_create`,\n" +
                    "  `gmt_modified`,\n" +
                    "  `master_id`,\n" +
                    "  `name`,\n" +
                    "  `column_name`,\n" +
                    "  `column_label`,\n" +
                    "  `relation_table_name`,\n" +
                    "  `relation_column_name`,\n" +
                    "  `id_column_name`,\n" +
                    "  `default_value`,\n" +
                    "  `data_type`,\n" +
                    "  `mirror_column_label`,\n" +
                    "  `table_name`,\n" +
                    "  `is_primary_key`,\n" +
                    "  `is_ignore`,\n" +
                    "  `width`,\n" +
                    "  `decimal_digits`,\n" +
                    "  `is_required`,\n" +
                    "  `is_auto_increment`\n" +
                    "FROM\n" +
                    "  dataobj_column\n" +
                    "WHERE\n" +
                    "  master_id = :masterId");
            columnCmd.setParameter("masterId", masterId);

            DataTable dtCol = dictSqlAdapter.query(columnCmd);
            if (dtCol.size() == 0) {
                jsonOfObj.put("columns", new JSONArray());
            } else {
                JSONArray arr = new JSONArray();
                for (int i = 0; i < dtCol.size(); i++) {
                    DataRow r = dtCol.getRows().get(i);
//
//                    jsonOfCol.put("name", r.get("name"));
//                    jsonOfCol.put("columnName", r.get("column_name"));
//                    jsonOfCol.put("columnLabel", r.get("column_label"));
//                    jsonOfCol.put("dataType", r.get("data_type"));
//                    jsonOfCol.put("mirrorColumnLabel", r.get("mirror_column_label"));
//                    jsonOfCol.put("width", r.get("width"));
//                    jsonOfCol.put("decimalDigits", r.get("decimal_digits"));
//                    jsonOfCol.put("tableName", r.get("table_name"));
//                    jsonOfCol.put("ignore", Converter.toBoolean(r.get("is_ignore")));
//                    jsonOfCol.put("primaryKey", Converter.toBoolean(r.get("is_primary_key")));
//                    jsonOfCol.put("required", Converter.toBoolean(r.get("is_required")));
//                    jsonOfCol.put("autoIncrement", Converter.toBoolean(r.get("is_auto_increment")));
//                    jsonOfCol.put("defaultValue", r.get("default_value"));
//                    jsonOfCol.put("relationTableName", r.get("relation_table_name"));
//                    jsonOfCol.put("relationColumnName", r.get("relation_column_name"));
//                    jsonOfCol.put("idColumnName", r.get("id_column_name"));
                    arr.add(row2CamelJson(r));
                }

                jsonOfObj.put("columns", arr);
            }

            
            SqlCommand tableCmd = new SqlCommand("" +
                    "SELECT\n" +
                    "  `id`,\n" +
                    "  `gmt_create`,\n" +
                    "  `gmt_modified`,\n" +
                    "  `master_id`,\n" +
                    "  `tableName`,\n" +
                    "  `relation_type`,\n" +
                    "  `columnName`,\n" +
                    "  `relationColumn`,\n" +
                    "  `relationWhere`,\n" +
                    "  `follow_delete`,\n" +
                    "  `follow_insert`,\n" +
                    "  `follow_update`\n" +
                    "FROM\n" +
                    "  dataobj_table\n" +
                    "WHERE\n" +
                    "  master_id = :masterId");
            tableCmd.setParameter("masterId", masterId);

            DataTable dtTable = dictSqlAdapter.query(tableCmd);
            if (dtTable.size() == 0) {
                jsonOfObj.put("tables", new JSONArray());
            } else {
                JSONArray arr = new JSONArray();
                for (int i = 0; i < dtTable.size(); i++) {
                    DataRow r = dtTable.getRows().get(i);

//                    jsonOfTable.put("tableName", r.get("table_name"));
//                    jsonOfTable.put("columnName", r.get("column_name"));
//                    jsonOfTable.put("relationColumn", r.get("relation_column"));
//                    jsonOfTable.put("followDelete", Converter.toBoolean(r.get("follow_delete")));
//                    jsonOfTable.put("followInsert", Converter.toBoolean(r.get("follow_insert")));
//                    jsonOfTable.put("followUpdate", Converter.toBoolean(r.get("follow_update")));
//                    jsonOfTable.put("relationType", r.get("relationType"));
//                    jsonOfTable.put("conditions", JSON.parseArray(r.get("conditions")));
                    arr.add(row2CamelJson(r));

                }
                jsonOfObj.put("tables", arr);
            }

            obj.parseJson(jsonOfObj);
        } catch (SQLException sqle) {
            throw new DustDbRuntimeException("DataObjContainer4Mysql throw sql exception", sqle);
        }

        return obj;
    }

    private JSONObject row2CamelJson(DataRow r) {
        JSONObject json = new JSONObject();
        r.iterator(colKey-> {
            json.put(CamelNameUtils.underscore2camel(colKey), r.get(colKey));
            return true;
        });

        return json;
    }

}
