package dust.service.db.dict.condition;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;

import java.util.ArrayList;

/**
 * @author huangshengtao on 2017-12-29.
 */
public class ValueListNode extends BaseNode<ValueListNode> {
    public ValueListNode(String ...values) {
        this();
        if (values != null && values.length > 0) {
            ArrayList<String> tempList = Lists.newArrayList(values);
            this.values.addAll(tempList);
        }
    }

    public ValueListNode() {
        this.type = NodeType.VALUE_lIST;
    }

    @Override
    public ValueListNode parse(JSONObject jsonObject) {
        if (jsonObject == null) {
            throw new NullPointerException("parse jsonObject");
        }

        if (jsonObject.containsKey("type")) {
            String strType = jsonObject.getString("type");
            if (NodeType.valueOf(strType) != NodeType.VALUE_lIST) {
                throw new IllegalArgumentException("Node type of json not compatible ");
            }
        }

        if (jsonObject.containsKey("values")) {
            JSONArray jsonArray = jsonObject.getJSONArray("values");
            for (int i = 0; i < jsonArray.size(); i++) {
                String val = jsonArray.getString(i);
                values.add(val);
            }
        }

        return this;
    }

    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        JSONArray arr = new JSONArray();
        for(String val : values) {
            arr.add(val);
        }

        jsonObject.put("type", this.type);
        jsonObject.put("values", arr);
        return jsonObject;
    }
}
