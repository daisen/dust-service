package dust.service.db.dict.condition;

import com.alibaba.fastjson.JSONObject;

/**
 * @author huangshengtao on 2017-12-29.
 */
public class ColumnNode extends BaseNode<ColumnNode> {
    public ColumnNode(String tableName, String columnName) {
        this();
        this.tableName = tableName;
        this.setColumnName(columnName);
    }

    public ColumnNode() {
        this.type = NodeType.COLUMN;
    }

    @Override
    public ColumnNode parse(JSONObject jsonObject) {
        if (jsonObject == null) {
            throw new NullPointerException("parse jsonObject");
        }

        if (jsonObject.containsKey("type")) {
            String strType = jsonObject.getString("type");
            this.type = NodeType.valueOf(strType);
            if (type != NodeType.COLUMN) {
                throw new IllegalArgumentException("Node type of json not compatible ");
            }
        }

        this.tableName = jsonObject.getString("tableName");
        this.setColumnName(jsonObject.getString("columnName"));
        return this;
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("tableName", this.tableName);
        json.put("columnName", this.columnName);
        json.put("type", this.type);
        return json;
    }
}
