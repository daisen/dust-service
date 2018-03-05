package dust.service.db.dict;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import dust.service.core.util.CamelNameUtils;
import dust.service.core.util.Converter;
import dust.service.core.util.SnowFlakeIdWorker;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author huangshengtao on 2017-12-18.
 */
public class DataObjColumn {
    private long objId;

    private long id;
    private String name;
    private String columnLabel;
    private String columnName;
    private String tableName;
    private boolean primaryKey;
    private boolean ignore;
    private String mirrorColumnLabel;
    private boolean required;
    private int width;
    private int decimalDigits;
    private DataType dataType;
    private String defaultValue;
    private boolean autoIncrement;

    //暂不启用
    private String relationTableName;
    private String relationColumnName;
    private String idColumnLabel;

    private List<Condition> conditions = Lists.newArrayList();
    private StringBuilder whereSql = new StringBuilder();

    @JsonIgnore
    private DataObj dataObj;
    @JsonIgnore
    private Table table;

    public DataObjColumn(DataType dataType) {
        this(null, null, null, null, dataType);
    }

    public DataObjColumn(String columnName, String tableName, DataType dataType) {
        this(null, columnName, null, tableName, dataType);
    }

    public DataObjColumn(String name, String columnName, String columnLabel, String tableName, DataType dateType) {
        this.name = name;
        this.columnName = columnName;
        this.columnLabel = columnLabel;
        this.tableName = tableName;
        this.dataType = dateType;

        if (DictGlobalConfig.isEnableObjId()) {
            id = SnowFlakeIdWorker.getInstance0().nextId();
        }
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public int getDecimalDigits() {
        return decimalDigits;
    }

    public void setDecimalDigits(int decimalDigits) {
        this.decimalDigits = decimalDigits;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public void setAutoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
    }

    public DataObjColumn setName(String name) {
        this.name = name;
        return this;
    }

    public String getColumnLabel() {
        if (StringUtils.isEmpty(columnLabel)) {
            this.columnLabel = CamelNameUtils.underscore2camel(this.columnName);
        }
        return columnLabel;
    }

    public DataObjColumn setColumnLabel(String columnLabel) {
        if (this.dataObj != null) {
            this.dataObj.updateColumnIndex(this.columnLabel, columnLabel);
        }
        this.columnLabel = columnLabel;
        return this;
    }

    public String getColumnName() {
        return columnName;
    }

    public DataObjColumn setColumnName(String columnName) {
        this.columnName = columnName;
        return this;
    }

    public String getTableName() {
        return tableName;
    }

    public DataObjColumn setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public DataObjColumn setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
        return this;
    }

    public boolean isIgnore() {
        return ignore;
    }

    public DataObjColumn setIgnore(boolean ignore) {
        this.ignore = ignore;
        return this;
    }

    public String getMirrorColumnLabel() {
        return mirrorColumnLabel;
    }

    public DataObjColumn setMirrorColumnLabel(String mirrorColumnLabel) {
        this.mirrorColumnLabel = mirrorColumnLabel;
        return this;
    }

    public long getObjId() {
        return objId;
    }

    public DataObjColumn setObjId(long objId) {
        this.objId = objId;
        return this;
    }

    public DataType getDataType() {
        return dataType;
    }

    public DataObj getDataObj() {
        return dataObj;
    }

    protected void setDataObj(DataObj dataObj) {
        this.dataObj = dataObj;
    }

    public String getRelationTableName() {
        return relationTableName;
    }

    public void setRelationTableName(String relationTableName) {
        this.relationTableName = relationTableName;
    }

    public String getRelationColumnName() {
        return relationColumnName;
    }

    public void setRelationColumnName(String relationColumnName) {
        this.relationColumnName = relationColumnName;
    }

    public String getIdColumnLabel() {
        return idColumnLabel;
    }

    public void setIdColumnLabel(String idColumnLabel) {
        this.idColumnLabel = idColumnLabel;
    }

    public DataObjColumn parseCondition(String json) {
        JSONArray jsonArray = JSON.parseArray(json);
        if (jsonArray.size() > 0) {
            for(int i = 0; i < jsonArray.size(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);
                Condition newCondition = new Condition();
                newCondition.parse(item);
                conditions.add(newCondition);
            }
        }

        return this;
    }

    public JSONObject toSchemaJson() {
        JSONObject schemaJson = new JSONObject();
        schemaJson.put("id", this.id);
        schemaJson.put("name", this.name);
        schemaJson.put("columnLabel", this.columnLabel);
        schemaJson.put("columnName", this.columnName);
        schemaJson.put("tableName", this.tableName);
        schemaJson.put("primaryKey", this.primaryKey);
        schemaJson.put("ignore", this.ignore);
        schemaJson.put("required", this.required);
        schemaJson.put("mirrorColumnLabel", this.mirrorColumnLabel);
        schemaJson.put("objId", this.objId);
        schemaJson.put("dataType", this.dataType);
        schemaJson.put("relationColumnName", this.relationColumnName);
        schemaJson.put("relationTableName", this.relationTableName);
        schemaJson.put("idColumnLabel", this.idColumnLabel);
        schemaJson.put("defaultValue", this.defaultValue);
        schemaJson.put("width", this.width);
        schemaJson.put("decimalDigits", this.decimalDigits);

        JSONArray arr = new JSONArray();
        for (Condition cdt: conditions) {
            arr.add(cdt.toJson());
        }
        schemaJson.put("conditions", arr);
        return schemaJson;
    }

    public DataObjColumn clone() {
        DataObjColumn dataColumn = new DataObjColumn(this.dataType);
        dataColumn.name = this.name;
        dataColumn.columnName = this.columnName;
        dataColumn.columnLabel = this.columnLabel;
        dataColumn.tableName = this.tableName;
        dataColumn.ignore = this.ignore;
        dataColumn.primaryKey = this.primaryKey;
        dataColumn.required = this.required;
        dataColumn.mirrorColumnLabel = this.mirrorColumnLabel;
        dataColumn.idColumnLabel = this.idColumnLabel;
        dataColumn.relationColumnName = this.relationColumnName;
        dataColumn.relationTableName = this.relationTableName;
        dataColumn.defaultValue = this.defaultValue;
        dataColumn.width = this.width;
        dataColumn.decimalDigits = this.decimalDigits;
        dataColumn.whereSql.append(this.whereSql);

        for(Condition cdt : this.conditions) {
            dataColumn.conditions.add(cdt);
        }
        return dataColumn;
    }

    public Object toValue(Object value) {
        switch (dataType) {
            case NUMBER:
                return Converter.toDouble(value);
            case CURRENCY:
                return Converter.toBigDecimal(value);
            case INT:
            case UBIGINT:
                return Converter.toInteger(value);
            case STRING:
            case TEXT:
                return Converter.toString(value);
            case BINARY:
                return Converter.toBytes(value);
            case DATE:
                if ("@NOW".equals(value) || "@UPDATE".equals(value)) {
                    return new Date();
                }
                return Converter.toDate(value);
            case BOOLEAN:
                return Converter.toBoolean(value);
        }

        throw new IllegalArgumentException("column data type not support convert");
    }

    public void addCondition(Condition cdt) {
        if (!this.conditions.contains(cdt)) {
            this.conditions.add(cdt);
        }
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public DataObjColumn where(String sql) {
        if (whereSql.length() > 0) {
            whereSql.append(" AND ");
            whereSql.append(sql);
        } else {
            whereSql.append(sql);
        }

        return this;
    }


    public DataObjColumn whereOr(String sql) {
        if (whereSql.length() > 0) {
            whereSql.append(" OR ");
            whereSql.append(sql);
        } else {
            whereSql.append(sql);
        }

        return this;
    }

    public static DataObjColumn create(Map schema) {

        String strDataType = Converter.toString(schema.get("dataType"));
        if (strDataType == null) {
            throw new IllegalArgumentException("dataType is not compatible");
        }

        DataType dataType;
        try {
            dataType = DataType.valueOf(strDataType);
        } catch (Exception e) {
            throw new IllegalArgumentException("dataType is not compatible");
        }

        DataObjColumn dataColumn = new DataObjColumn(dataType);
        if (schema.containsKey("name")) {
            dataColumn.name = Converter.toString(schema.get("name"));
        }

        if (schema.containsKey("columnName")) {
            dataColumn.columnName = Converter.toString(schema.get("columnName"));
        }

        if (schema.containsKey("columnLabel")) {
            dataColumn.columnLabel = Converter.toString(schema.get("columnLabel"));
        }

        if (schema.containsKey("tableName")) {
            dataColumn.tableName = Converter.toString(schema.get("tableName"));
        }

        if (schema.containsKey("ignore")) {
            dataColumn.ignore = Converter.toBoolean(schema.get("ignore"));
        } else if (schema.containsKey("isIgnore")) {
            dataColumn.ignore = Converter.toBoolean(schema.get("isIgnore"));
        }

        if (schema.containsKey("primaryKey")) {
            dataColumn.primaryKey = Converter.toBoolean(schema.get("primaryKey"));
        } else if (schema.containsKey("isPrimaryKey")) {
            dataColumn.primaryKey = Converter.toBoolean(schema.get("isPrimaryKey"));
        }


        if (schema.containsKey("autoIncrement")) {
            dataColumn.autoIncrement = Converter.toBoolean(schema.get("autoIncrement"));
        } else if (schema.containsKey("isAutoIncrement")) {
            dataColumn.autoIncrement = Converter.toBoolean(schema.get("isAutoIncrement"));
        }


        if (schema.containsKey("required")) {
            dataColumn.required = Converter.toBoolean(schema.get("required"));
        } else if (schema.containsKey("isRequired")) {
            dataColumn.required = Converter.toBoolean(schema.get("isRequired"));
        }


        if (schema.containsKey("mirrorColumnLabel")) {
            dataColumn.mirrorColumnLabel = Converter.toString(schema.get("mirrorColumnLabel"));
        }

        if (schema.containsKey("defaultValue")) {
            dataColumn.defaultValue = Converter.toString(schema.get("defaultValue"));
        }

        if (schema.containsKey("width")) {
            dataColumn.width = Converter.toInteger(schema.get("width"));
        }

        if (schema.containsKey("decimalDigits")) {
            dataColumn.decimalDigits = Converter.toInteger(schema.get("decimalDigits"));
        }

        if (schema.containsKey("relationTableName")) {
            dataColumn.relationTableName = Converter.toString(schema.get("relationTableName"));
        }

        if (schema.containsKey("relationColumnName")) {
            dataColumn.relationColumnName = Converter.toString(schema.get("relationColumnName"));
        }

        if (schema.containsKey("idColumnLabel")) {
            dataColumn.idColumnLabel = Converter.toString(schema.get("idColumnLabel"));
        }


        return dataColumn;
    }

}
