package dust.service.db.dict;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import dust.service.core.util.Converter;
import dust.service.db.dict.condition.*;

import java.util.List;

/**
 * @author huangshengtao on 2017-12-18.
 */
public class Condition {
    private BaseNode left;
    private OperationType operation = OperationType.EQUAL;
    private BaseNode right;
    private final List<Condition> subConditions = Lists.newArrayList();
    private boolean require = true;

    public Condition() {

    }

    public Condition(BaseNode left, OperationType operation, BaseNode right, boolean require) {
        this.left = left;
        this.operation = operation;
        this.right = right;
        this.require = require;
    }

    public Condition(String tableName, String columnName, String value) {
        this.left = new ColumnNode(tableName, columnName);
        this.operation = OperationType.EQUAL;
        this.right = new ValueNode(value);
    }

    public BaseNode getLeft() {
        return left;
    }

    public void setLeft(BaseNode left) {
        this.left = left;
    }

    public OperationType getOperation() {
        return operation;
    }

    public void setOperation(OperationType operation) {
        this.operation = operation;
    }

    public BaseNode getRight() {
        return right;
    }

    public void setRight(BaseNode right) {
        this.right = right;
    }

    public List<Condition> getSubConditions() {
        return subConditions;
    }

    public boolean isRequire() {
        return require;
    }

    public void setRequire(boolean require) {
        this.require = require;
    }

    public Condition addSubCondition(Condition subCondition) {
        this.subConditions.add(subCondition);
        return this;
    }

    public boolean removeSubCondition(Condition subCondition) {
        return this.subConditions.remove(subCondition);
    }

    public Condition parse(JSONObject jsonObject) {
        String strOperation = jsonObject.getString("operation");
        if (strOperation == null) {
            throw new IllegalArgumentException("parsing JSONObject have no compatible operation");
        }

        this.operation = OperationType.valueOf(strOperation);
        JSONObject rightJson = jsonObject.getJSONObject("right");
        JSONObject leftJson = jsonObject.getJSONObject("left");

        if (jsonObject.containsKey("require")) {
            this.require = Converter.toBoolean(jsonObject.get("require"));
        } else if (jsonObject.containsKey("isRequire")) {
            this.require = Converter.toBoolean(jsonObject.get("isRequire"));
        }

        switch (operation) {
            case EXIST:
            case NOT_EXIST:
                this.right = new SubTableNode().parse(rightJson);
                break;
            case LESS:
            case EQUAL:
            case GREATER:
            case NOT_EQUAL:
            case EQUAL_LESS:
            case EQUAL_GREATER:
                String strType = leftJson.getString("type");
                this.left = (BaseNode) getBaseNode(strType).parse(leftJson);
                this.right = (BaseNode) getBaseNode(strType).parse(rightJson);
                break;
            case CONTAIN:
            case START_WITH:
            case END_WITH:
                this.left = new ColumnNode().parse(leftJson);
                this.right = new ValueNode().parse(rightJson);
                break;
            case IN_LIST:
                this.left = new ColumnNode().parse(leftJson);
                this.right = new ValueListNode().parse(rightJson);
                break;
            case IN_TABLE:
                this.left = new ColumnNode().parse(leftJson);
                this.right = new SubTableNode().parse(rightJson);
                break;
            case NULL:
            case NOT_NULL:
                this.left = new ColumnNode().parse(leftJson);
                break;
        }

        return this;
    }

    private BaseNode getBaseNode(String type) {
        NodeType nodeType = NodeType.valueOf(type);
        switch (nodeType) {
            case COLUMN:
                return new ColumnNode();
            case EXPR:
                return new ExprNode();
            case VALUE:
                return new ValueNode();
            case SUB_TABLE:
                return new SubTableNode();
            case VALUE_lIST:
                return new ValueListNode();
        }

        throw new IllegalArgumentException("node type not compatible");
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("require", this.require);
        json.put("operation", this.operation);
        json.put("left", this.left.toJson());
        json.put("right", this.right.toJson());
        return json;
    }

    public static Condition create(JSONObject jsonObject) {
        Condition cdt = new Condition();
        cdt.parse(jsonObject);
        return cdt;
    }

}
