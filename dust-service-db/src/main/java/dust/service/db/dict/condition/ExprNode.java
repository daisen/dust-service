package dust.service.db.dict.condition;

import com.alibaba.fastjson.JSONObject;

/**
 * @author huangshengtao on 2017-12-29.
 */
public class ExprNode extends BaseNode<ExprNode> {
    public ExprNode(String expr) {
        this();
        this.value = expr;
    }

    public ExprNode() {
        this.type = NodeType.EXPR;
    }

    @Override
    public ExprNode parse(JSONObject jsonObject) {
        if (jsonObject == null) {
            throw new NullPointerException("parse jsonObject");
        }

        if (jsonObject.containsKey("type")) {
            String strType = jsonObject.getString("type");
            this.type = NodeType.valueOf(strType);
            if (type != NodeType.EXPR) {
                throw new IllegalArgumentException("Node type of json not compatible ");
            }
        }

        this.value = jsonObject.getString("value");
        return this;

    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("value", this.value);
        json.put("type", this.type);
        return json;
    }
}
