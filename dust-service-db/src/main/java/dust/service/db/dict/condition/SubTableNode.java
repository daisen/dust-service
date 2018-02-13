package dust.service.db.dict.condition;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import dust.service.db.dict.Condition;

/**
 * @author huangshengtao on 2017-12-29.
 */
public class SubTableNode extends BaseNode<SubTableNode> {
    public SubTableNode(String tableName, String columnName, Condition... conditions) {
        this();
        this.tableName = tableName;
        this.setColumnName(columnName);

        if (conditions != null && conditions.length > 0) {
            this.subConditions.addAll(Lists.newArrayList(conditions));
        }
    }

    public SubTableNode() {
        this.type = NodeType.SUB_TABLE;
    }

    @Override
    public SubTableNode parse(JSONObject jsonObject) {
        if (jsonObject == null) {
            throw new NullPointerException("parse jsonObject");
        }

        if (jsonObject.containsKey("type")) {
            String strType = jsonObject.getString("type");
            this.type = NodeType.valueOf(strType);
            if (type != NodeType.SUB_TABLE) {
                throw new IllegalArgumentException("Node type of json not compatible ");
            }
        }

        this.tableName = jsonObject.getString("tableName");
        this.setColumnName(jsonObject.getString("columnName"));
        JSONArray jsonArray = jsonObject.getJSONArray("subConditions");
        for (int i = 0; i < jsonArray.size(); i++) {
            this.subConditions.add(new Condition().parse(jsonArray.getJSONObject(i)));
        }

        return this;
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("type", this.type);
        json.put("tableName", this.tableName);
        json.put("columnName", this.columnName);
        JSONArray subConditionArray = new JSONArray();
        for(Condition cdt : subConditions) {
            subConditionArray.add(cdt.toJson());
        }

        json.put("subConditions", subConditionArray);
        return json;
    }
}
