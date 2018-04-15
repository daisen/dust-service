package dust.service.db.dict;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author huangshengtao on 2017-12-18.
 */
public class Table {
    private long id;
    private String tableName;
    private String alias;
    private RelationType relationType = RelationType.LEFT;
    private String columnName;
    private String relationColumn;
    private String relationWhere;
    private Boolean followDelete = false;
    private Boolean followInsert = false;
    private Boolean followUpdate = true;
    private final List<Condition> conditions = Lists.newArrayList();

    public Table() {
    }

    public Table(String tableName) {
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getAlias() {
        if (StringUtils.isEmpty(alias)) {
            return tableName;
        }
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public RelationType getRelationType() {
        return relationType;
    }

    public void setRelationType(RelationType relationType) {
        this.relationType = relationType;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getRelationColumn() {
        return relationColumn;
    }

    public void setRelationColumn(String relationColumn) {
        this.relationColumn = relationColumn;
    }

    public String getRelationWhere() {
        return relationWhere;
    }

    public void setRelationWhere(String relationWhere) {
        this.relationWhere = relationWhere;
    }

    public Boolean getFollowDelete() {
        return followDelete;
    }

    public void setFollowDelete(Boolean followDelete) {
        this.followDelete = followDelete;
    }

    public Boolean getFollowInsert() {
        return followInsert;
    }

    public void setFollowInsert(Boolean followInsert) {
        this.followInsert = followInsert;
    }

    public Boolean getFollowUpdate() {
        return followUpdate;
    }

    public void setFollowUpdate(Boolean followUpdate) {
        this.followUpdate = followUpdate;
    }

    public Table addCondition(Condition condition) {
        checkNotNull(condition);
        this.conditions.add(condition);
        return this;
    }

    public void removeCondition(Condition condition) {
        if (!this.conditions.contains(condition)) {
            throw new IllegalArgumentException("relation not contain this condition");
        }
        this.conditions.remove(condition);
    }

    public List<Condition> getAllCondition() {
        List<Condition> tmp = Lists.newArrayList();
        tmp.addAll(this.conditions);
        return tmp;
    }

    public static Table create(JSONObject schema) {
        Table tb = new Table();
        tb.tableName = schema.getString("tableName");
        tb.alias = schema.getString("alias");
        tb.columnName = schema.getString("columnName");
        tb.relationColumn = schema.getString("relationColumn");
        tb.relationWhere = schema.getString("relationWhere");
        tb.followDelete = schema.getBoolean("followDelete");
        tb.followInsert = schema.getBoolean("followInsert");
        tb.followUpdate = schema.getBoolean("followUpdate");

        JSONArray arrCdts = schema.getJSONArray("conditions");
        if (arrCdts != null) {
            for (int i = 0; i < arrCdts.size(); i++) {
                tb.conditions.add(Condition.create(arrCdts.getJSONObject(i)));
            }
        }

        tb.relationType = RelationType.valueOf(schema.getString("relationType"));

        return tb;
    }

    public JSONObject toJson() {
        JSONObject jsonTable = new JSONObject();
        jsonTable.put("tableName", this.tableName);
        jsonTable.put("alias", this.alias);
        jsonTable.put("columnName", this.columnName);
        jsonTable.put("relationColumn", this.relationColumn);
        jsonTable.put("relationType", this.relationType);
        jsonTable.put("relationWhere", this.relationWhere);
        jsonTable.put("followDelete", this.followDelete);
        jsonTable.put("followInsert", this.followInsert);
        jsonTable.put("followUpdate", this.followUpdate);
        JSONArray jsonCondition = new JSONArray();
        for(Condition condition : this.conditions) {
            jsonCondition.add(condition.toJson());
        }
        jsonTable.put("conditions", jsonCondition.toJSONString());
        return jsonTable;
    }
}
