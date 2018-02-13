package dust.service.db.dict.condition;

import com.alibaba.fastjson.JSONObject;

/**
 * @author huangshengtao on 2017-12-29.
 */
public class ValueNode extends BaseNode<ValueNode> {
    public ValueNode(String value) {
        this();
        this.value = value;
    }

    public ValueNode() {
        this.type = NodeType.VALUE;
    }

    @Override
    public ValueNode parse(JSONObject jsonObject) {
        if (jsonObject == null) {
            throw new NullPointerException("parse jsonObject");
        }

        if (jsonObject.containsKey("type")) {
            String strType = jsonObject.getString("type");
            if (NodeType.valueOf(strType) != NodeType.VALUE) {
                throw new IllegalArgumentException("Node type of json not compatible ");
            }
        }

        this.value = jsonObject.getString("value");
        return this;
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("value", value);
        json.put("type", this.type);
        return json;
    }
}
