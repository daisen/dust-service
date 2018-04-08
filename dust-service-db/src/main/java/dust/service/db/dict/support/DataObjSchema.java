package dust.service.db.dict.support;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import dust.service.core.thread.LocalHolder;
import dust.service.core.util.CamelNameUtils;
import dust.service.db.sql.DataRow;
import dust.service.db.sql.DataTable;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 *   convert class for dict schema
 * @author huangshengtao on 2018-4-4.
 */
public class DataObjSchema {
    private final static String LOCAL_SCHEMA = "DataObjSchema.LOCAL_SCHEMA";
    private final static String LOCAL_SCHEMA_ALIAS_INDEX = "DataObjSchema.LOCAL_SCHEMA_ALIAS_INDEX";
    private final static DataObjSchema instance = new DataObjSchema();

    public static DataObjSchema getInstance() {
        return instance;
    }

    public JSONObject getSchemaJSON(String app, String module, String alias) {
        ThreadLocal<Map<String, Long>> localSchemaJson = LocalHolder.get(LOCAL_SCHEMA_ALIAS_INDEX);
        Map<String, Long> cache = localSchemaJson.get();
        if (cache == null) {
            localSchemaJson.set(Maps.newHashMap());
            return null;
        }

        String cacheKey = app + "_" + module + "_" + alias;
        Long id = cache.get(cacheKey);
        return getSchemaJSON(id);
    }

    public JSONObject getSchemaJSON(Long id) {
        ThreadLocal<Map<Long, JSONObject>> localSchemaJson = LocalHolder.get(LOCAL_SCHEMA);
        Map<Long, JSONObject> cache = localSchemaJson.get();
        if (cache == null) {
            localSchemaJson.set(Maps.newHashMap());
            return null;
        }

        return cache.get(id);
    }

    public JSONObject toJSONSchema(DataRow objRow, DataTable columnData, DataTable tableData) {
        checkNotNull(objRow);
        checkNotNull(columnData);

        JSONObject jsonOfObj = row2CamelJson(objRow);

        JSONObject p = new JSONObject();
        if (objRow.containsColumn("start")) {
            p.put("start", objRow.get("start"));
        } else if (objRow.containsColumn("start_index")) {
            p.put("start", objRow.get("startIndex"));
        }

        p.put("pageSize", objRow.get("page_size"));
        jsonOfObj.put("pageInfo", p);

        if (StringUtils.isNotEmpty(objRow.get("conditions"))) {
            jsonOfObj.put("conditions", JSON.parseArray(objRow.get("conditions")));
        }

        if (StringUtils.isNotEmpty(objRow.get("orders"))) {
            jsonOfObj.put("orders", JSON.parseArray(objRow.get("orders")));
        }

        if (columnData.size() == 0) {
            jsonOfObj.put("columns", new JSONArray());
        } else {
            JSONArray arr = new JSONArray();
            for (int i = 0; i < columnData.size(); i++) {
                DataRow r = columnData.getRows().get(i);
                arr.add(row2CamelJson(r));
            }

            jsonOfObj.put("columns", arr);
        }



        if (tableData.size() == 0) {
            jsonOfObj.put("tables", new JSONArray());
        } else {
            JSONArray arr = new JSONArray();
            for (int i = 0; i < tableData.size(); i++) {
                DataRow r = tableData.getRows().get(i);
                arr.add(row2CamelJson(r));

            }
            jsonOfObj.put("tables", arr);
        }

        Long id = jsonOfObj.getLong("id");
        String alias = jsonOfObj.getString("alias");
        String app = jsonOfObj.getString("app");
        String module = jsonOfObj.getString("module");

        saveScheme(id, jsonOfObj);
        updateSchemaAliasIndex(app, module, alias, id);
        return jsonOfObj;

    }

    private JSONObject row2CamelJson(DataRow r) {
        JSONObject json = new JSONObject();
        r.iterator(colKey -> {
            json.put(CamelNameUtils.underscore2camel(colKey), r.get(colKey));
            return true;
        });

        return json;
    }

    private void saveScheme(Long id, JSONObject json) {
        ThreadLocal<Map<Long, JSONObject>> localSchema = LocalHolder.get(LOCAL_SCHEMA);
        Map<Long, JSONObject> cache = localSchema.get();
        if (cache == null) {
            localSchema.set(Maps.newHashMap());
        }

        localSchema.get().put(id, json);
    }

    private void updateSchemaAliasIndex(String app, String module, String key, Long id) {
        String cacheKey = app + "_" + module + "_" + key;
        ThreadLocal<Map<String, Long>> localSchemaAliasIndex = LocalHolder.get(LOCAL_SCHEMA_ALIAS_INDEX);
        Map<String, Long> cache = localSchemaAliasIndex.get();
        if (cache == null) {
            localSchemaAliasIndex.set(Maps.newHashMap());
        }

        localSchemaAliasIndex.get().put(cacheKey, id);

    }
}
