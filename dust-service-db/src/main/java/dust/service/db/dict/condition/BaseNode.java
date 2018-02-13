package dust.service.db.dict.condition;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import dust.service.db.dict.Condition;
import dust.service.db.dict.DataType;
import dust.service.core.util.CamelNameUtils;
import dust.service.core.util.Converter;
import dust.service.db.dict.DictGlobalConfig;

import java.util.List;

/**
 * @author huangshengtao on 2017-12-29.
 */
public abstract class BaseNode<T> {
    protected NodeType type;
    protected String value;
    protected DataType dataType = DataType.STRING;
    protected String tableName;
    protected String columnName;
    protected final List<String> values = Lists.newArrayList();
    protected final List<Condition> subConditions = Lists.newArrayList();

    public BaseNode() {}

    public NodeType getType() {
        return type;
    }

    public void setType(NodeType type) {
        if (!this.getClass().getSimpleName().contains("BaseNode")) {
            throw new IllegalArgumentException("specific node's type not allow modify");
        }

        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public DataType getDataType() {
        return dataType;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<String> getValues() {
        return values;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        if (DictGlobalConfig.isAllowColumnNameOutOfUnderscore()) {
            this.columnName = columnName;
        } else {
            this.columnName = CamelNameUtils.camel2underscore(columnName);
        }
    }

    public List<Condition> getSubConditions() {
        return subConditions;
    }

    public abstract T parse(JSONObject jsonObject);

    public abstract JSONObject toJson();

    public Object getTypeValue() {
        return toTypeValue(value);
    }

    public Object toTypeValue(Object o) {
        switch (dataType) {
            case BOOLEAN:
                return Converter.toBoolean(o);
            case BINARY:
                return Converter.toBytes(o);
            case INT:
            case UBIGINT:
                return Converter.toInteger(o);
            case NUMBER:
                return Converter.toDouble(o);
            case TEXT:
            case STRING:
                return Converter.toString(o);
            case DATE:
                return Converter.toDate(o);
            case CURRENCY:
                return Converter.toBigDecimal(o);
        }

        throw new IllegalStateException("getTypeValue not support dataType");
    }
}
