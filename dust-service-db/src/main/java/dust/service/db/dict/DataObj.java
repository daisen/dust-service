package dust.service.db.dict;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import dust.service.db.sql.DataTable;
import dust.service.core.util.SnowFlakeIdWorker;
import dust.service.db.dict.condition.BaseNode;
import dust.service.db.dict.condition.ColumnNode;
import dust.service.db.dict.express.Expression;
import dust.service.db.sql.DataRow;
import dust.service.db.sql.ISqlAdapter;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author huangshengtao on 2017-12-18.
 */
public class DataObj {
    private Long id;
    private String alias;
    private String name;
    private String tableName;
    private Integer currentIndex = -1;
    private Integer lastId;
    private final List<Table> tables = Lists.newArrayList();

    private OnValidate onValidate;
    private OnCurrentChanged onCurrentChanged;
    private OnCollectionChanged onCollectChanged;
    private OnRowDeleted onRowDeleted;
    private OnRowRemoved onRowRemoved;

    private final List<DataObjRow> rows = Lists.newArrayList();
    private final List<DataObjColumn> columns = Lists.newArrayList();
    private final Map<String, Integer> columnLabels = Maps.newHashMap();
    private final List<DataObjRow> deleteRows = Lists.newArrayList();

    private String orderBySql;
    private final StringBuffer whereSql = new StringBuffer();
    private String fixWhereSql;
    private final PageInfo pageInfo = new PageInfo();
    private final List<Condition> conditions = Lists.newArrayList();
    private Condition fixCondition;
    private final List<OrderBy> orders = Lists.newArrayList();

    public Long getId() {
        return id;
    }

    public Integer getLastId() {
        return lastId;
    }

    public void setLastId(int lastId) {
        this.lastId = lastId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public DataObj() {
        if (DictGlobalConfig.isEnableObjId()) {
            id = SnowFlakeIdWorker.getInstance0().nextId();
        }
    }

    //校验

    /**
     * 校验
     *
     * @return
     * @
     */
    public boolean validate() {
        for(DataObjRow row : this.rows) {
            for(DataObjColumn col : this.columns) {
                if (col.isRequired() || col.isPrimaryKey()) {
                    Object val = row.getField(col).getValue();
                    if (val == null || val.equals("")) {
                        return false;
                    }
                }
            }
        }

        return onValidate();
    }

    public void addOnValidate(OnValidate onValidate) {
        checkNotNull(onValidate);
        this.onValidate = onValidate;
    }

    private boolean onValidate() {
        if (this.onValidate != null && !onValidate.validate(this)) {
            return false;
        }

        return true;
    }

    //校验结束

    //数据行为

    public DataObjRow moveTo(int idx) {
        if (this.rows.size() > idx) {
            int oldIndex = this.currentIndex;
            this.currentIndex = idx;
            this.currentChanged(oldIndex, idx);
            return this.rows.get(idx);
        }

        return null;
    }

    public void setCurrentRow(DataObjRow dataObjRow) {
        int idx = this.rows.indexOf(dataObjRow);
        if (idx > -1) {
            this.moveTo(idx);
        }
    }

    public void addOnCurrentChanged(OnCurrentChanged onCurrentChanged) {
        this.onCurrentChanged = onCurrentChanged;
    }

    public void removeOnCurrentChanged() {
        this.onCurrentChanged = null;
    }

    private void currentChanged(int oldIndex, int newIndex) {
        if (this.onCurrentChanged != null) {
            this.onCurrentChanged.currentChanged(this, oldIndex, newIndex);
        }
    }

    public void addOnCollectChanged(OnCollectionChanged onCollectionChanged) {
        this.onCollectChanged = onCollectionChanged;
    }

    public void removeOnCollectChanged() {
        this.onCollectChanged = null;
    }

    private void collectChanged(OnCollectionChanged.ActionType actionType) {
        if (this.onCollectChanged != null) {
            this.onCollectChanged.collectionChanged(this, actionType);
        }
    }

    public void addOnRowDeleted(OnRowDeleted onRowDeleted) {
        this.onRowDeleted = onRowDeleted;
    }

    public void removeOnRowDeleted() {
        this.onRowDeleted = null;
    }

    private void rowDeleted(DataObjRow dataObjRow) {
        if (onRowDeleted != null) {
            this.onRowDeleted.rowDeleted(this, dataObjRow);
        }
    }

    public void addOnRowRemoved(OnRowRemoved onRowRemoved) {
        this.onRowRemoved = onRowRemoved;
    }

    public void removeOnRowRemoved() {
        this.onRowRemoved = null;
    }

    private void rowRemoved(DataObjRow dataObjRow) {
        if (onRowRemoved != null) {
            this.onRowRemoved.rowRemoved(this, dataObjRow);
        }
    }

    public Integer getCurrentIndex() {
        return currentIndex;
    }

    public DataObjRow getCurrentRow() {
        return rows.get(currentIndex);
    }

    //数据行为结束

    //二维结构

    public int getColumnSize() {
        return columns.size();
    }

    public DataObjColumn getColumn(String tableName, String columnName) {
        for (DataObjColumn dc : columns) {
            if (StringUtils.equals(dc.getTableName(), tableName)
                    && StringUtils.equals(dc.getColumnName(), columnName)) {
                return dc;
            }
        }

        throw new IllegalArgumentException("Column not found(" + tableName + "," + columnName + ")");
    }

    public DataObjColumn getColumn(int index) {
        if (index >= this.columns.size()) {
            throw new IllegalArgumentException("index");
        }

        return this.columns.get(index);
    }

    public DataObjColumn getColumn(String columnLabel) {
        for (DataObjColumn dc : columns) {
            if (!StringUtils.isEmpty(dc.getColumnLabel())) {
                if (dc.getColumnLabel().equals(columnLabel)) {
                    return dc;
                }
            } else {
                if (StringUtils.equals(dc.getColumnName(), columnLabel)) {
                    return dc;
                }
            }
        }

        throw new IllegalArgumentException("Column not found(" + columnLabel + ")");
    }

    public Integer findIdxByLabel(String columnLabel) {
        Integer ret = this.columnLabels.get(columnLabel);
        return ret == null ? -1 : ret;
    }

    public void updateColumnIndex(String oldColumnLabel, String newColumnLabel) {
        Integer idx = this.columnLabels.get(oldColumnLabel);
        if (idx == null) {
            return;
        }

        this.columnLabels.remove(oldColumnLabel);
        this.columnLabels.put(newColumnLabel, idx);
    }

    public DataObjRow getRow(int index) {
        if (index >= this.rows.size()) {
            throw new IndexOutOfBoundsException("row index out of bound");
        }
        return this.rows.get(index);
    }

    public DataObjField getField(int colIndex, int rowIndex) {
        if (colIndex >= this.columns.size()
                || rowIndex >= this.rows.size()) {
            throw new IndexOutOfBoundsException("colIndex rowIndex out of bound");
        }

        return this.rows.get(rowIndex).getField(colIndex);
    }

    public DataObjField getField(String colLabel, int rowIndex) {
        if (rowIndex >= this.rows.size()) {
            throw new IndexOutOfBoundsException("rowIndex out of bound");
        }
        return this.rows.get(rowIndex).getField(colLabel);
    }

    public DataObjField getField(DataObjColumn col, DataObjRow row) {
        checkNotNull(row);
        return row.getField(col);
    }

    public DataObjField getField(String colName, DataObjRow row) {
        checkNotNull(row);
        return row.getField(colName);
    }

    public List<DataObjRow> getRows() {
        return this.rows;
    }

    public JSONArray toDataJSON() {
        JSONArray jsonArray = new JSONArray();
        for (DataObjRow r : rows) {
            jsonArray.add(r.toJSON());
        }
        return jsonArray;
    }

    public JSONObject toPageDataJSON() {
        JSONObject pageJson = new JSONObject();
        pageJson.put("size", this.getPageInfo().getTotalRows());
        pageJson.put("page", this.toDataJSON());
        return pageJson;
    }

    //二维结构结束

    //多表级联

    public Table getTable(String tbName) {
        checkNotNull(tbName);
        for (Table t : tables) {
            if (StringUtils.equalsIgnoreCase(tbName, t.getTableName())) {
                return t;
            }
        }
        return null;
    }

    public List<Table> getTables() {
        return this.tables;
    }

    public String getTableNames() {
        StringBuffer sb = new StringBuffer();
        for (Table t : tables) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(t.getTableName());
        }
        return sb.toString();
    }

    //多表级联结束

    //数据操作

    public void loadData(DataTable dataTable) {
        for(DataRow row : dataTable.getRows()) {
            DataObjRow r = new DataObjRow(this);
            r.loadData(row);
            r.setRowState(RowState.UNCHANGED);
            this.rows.add(r);
        }
        this.collectChanged(OnCollectionChanged.ActionType.RESET);
    }

    public DataObjRow newRow() {
        return new DataObjRow(this);
    }

    public DataObjRow addRow(DataObjRow dataObjRow) {
        checkNotNull(dataObjRow);

        if (dataObjRow.getRowState() != RowState.DETACHED) {
            throw new IllegalArgumentException("dataObjRow row state not compatible’ ");
        }

        this.rows.add(dataObjRow);
        dataObjRow.setRowState(RowState.ADDED);
        this.collectChanged(OnCollectionChanged.ActionType.ADD);
        return dataObjRow;
    }

    /**
     * 业务修改，强修改，会把已存在的行强制置为修改状态；如果不存在则以修改行加入dataObj
     *
     * @param row
     */
    public void modifyRow(DataObjRow row) {
        if (this.rows.contains(row)) {
            row.setRowState(RowState.MODIFIED);
            this.collectChanged(OnCollectionChanged.ActionType.REPLACE);
        } else {
            if (row.getRowState() == RowState.DETACHED && row.getDataObj() == this) {
                row.setRowState(RowState.MODIFIED);
                this.rows.add(row);
                this.collectChanged(OnCollectionChanged.ActionType.ADD);
            }
        }
    }

    public void clear() {
        this.rows.clear();
        this.deleteRows.clear();
        this.collectChanged(OnCollectionChanged.ActionType.RESET);
    }

    /**
     * 业务方法，如果dataObjRow已在DataObj则移除行的同时做删除操作，如果dataObjRow不再DataObj则做业务标记操作，做删除操作
     *
     * @param dataObjRow
     */
    public void deleteRow(DataObjRow dataObjRow) {
        checkNotNull(dataObjRow);
        if (!this.rows.contains(dataObjRow)) {
            if (dataObjRow.getRowState() == RowState.DETACHED && dataObjRow.getDataObj() == this) {
                dataObjRow.setRowState(RowState.DELETED);
                this.deleteRows.add(dataObjRow);
                return;
            } else {
                throw new IllegalArgumentException("dataObjRow has been deleted or removed");
            }
        }

        this.rows.remove(dataObjRow);
        dataObjRow.rejectChanges();
        dataObjRow.setRowState(RowState.DELETED);
        this.rowDeleted(dataObjRow);
        deleteRows.add(dataObjRow);
        this.rowRemoved(dataObjRow);
        this.collectChanged(OnCollectionChanged.ActionType.REMOVE);
    }

    /**
     * 还原修改，注意，行的顺序不会还原
     * 增加行移除，修改行还原，删除行从末尾补上
     */
    public void rejectChanges() {
        int len = this.rows.size();
        for (int i = len; i > 0; i--) {
            DataObjRow row = this.rows.get(i - 1);
            if (row.getRowState() == RowState.ADDED) {
                this.rows.remove(row);
            }

            if (row.getRowState() == RowState.MODIFIED) {
                row.rejectChanges();
            }
        }

        for (DataObjRow row : this.deleteRows) {
            row.setRowState(RowState.UNCHANGED);
            this.rows.add(row);
        }

        this.deleteRows.clear();

        this.collectChanged(OnCollectionChanged.ActionType.RESET);
    }

    /**
     * 接受修改，通常用于save操作
     */
    public void acceptChanges() {
        for(DataObjRow row : this.rows) {
            row.acceptChanges();
        }

        this.deleteRows.clear();
        this.collectChanged(OnCollectionChanged.ActionType.RESET);
    }

    public void removeRow(DataObjRow dataObjRow) {
        checkNotNull(dataObjRow);
        if (!this.rows.contains(dataObjRow)) {
            throw new IllegalArgumentException("dataObjRow has been deleted or removed");
        }

        this.rows.remove(dataObjRow);
        this.rowRemoved(dataObjRow);
        this.collectChanged(OnCollectionChanged.ActionType.REMOVE);
    }

    public void deleteRows(Function<DataObjRow, Boolean> func) {
        for (DataObjRow row : this.rows) {
            Boolean ret = func.apply(row);
            if (ret != null && ret) {
                this.rows.remove(row);
                row.rejectChanges();
                row.setRowState(RowState.DELETED);
                deleteRows.add(row);
            }
        }

        this.collectChanged(OnCollectionChanged.ActionType.REMOVE);
    }

    public DataObj setValue(DataObjRow row, DataObjColumn col, Object val) {
        getField(col, row).setValue(val);
        return this;
    }

    public DataObj setValue(int rowIdx, String colName, Object val) {
        getField(colName, rowIdx).setValue(val);
        return this;
    }

    public DataObj setValue(DataObjRow row, String colName, Object val) {
        getField(colName, row).setValue(val);
        return this;
    }

    public Object getValue(DataObjRow row, DataObjColumn col) {
        getField(col, row).getValue();
        return this;
    }

    public Object getValue(int rowIdx, String colName) {
        getField(colName, rowIdx).getValue();
        return this;
    }

    public Object getValue(DataObjRow row, String colName) {
        getField(colName, row).getValue();
        return this;
    }


    //数据操作结束


    //数据交互
    public String orderBy(String sql) {
        this.orderBySql = sql;
        return this.orderBySql;
    }

    public String orderBy() {
        return this.orderBySql;
    }

    public String fixWhere(String sql) {
        this.fixWhereSql = sql;
        return this.fixWhereSql;
    }

    public String fixWhere() {
        return this.fixWhereSql;
    }

    public String where() {
        return this.whereSql.toString();
    }

    public DataObj where(String sql) {
        if (this.whereSql.length() > 0) {
            this.whereSql.append(" AND ");
        }
        this.whereSql.append(sql);
        return this;
    }

    public DataObj whereOr(String sql) {
        if (this.whereSql.length() > 0) {
            this.whereSql.append(" OR ");
        }
        this.whereSql.append(sql);
        return this;
    }

    public DataObj resetWhere() {
        this.whereSql.setLength(0);
        return this;
    }

    public List<DataObjRow> getChanges() {
        List<DataObjRow> tmpRows = Lists.newArrayList();

        for (DataObjRow r : rows) {
            if (r.getRowState() != RowState.UNCHANGED) {
                tmpRows.add(r);
            }
        }

        for (DataObjRow r : deleteRows) {
            tmpRows.add(r);
        }

        return tmpRows;
    }

    public List<DataObjRow> getDeleteRows() {
        return this.deleteRows;
    }

    public void getUpdateRows(Function<DataObjRow, Boolean> iterator) {
        for (DataObjRow r : rows) {
            if (r.getRowState() == RowState.MODIFIED) {
                if (iterator.apply(r) != true) {
                    return;
                }
            }
        }
    }

    public void getInsertRows(Function<DataObjRow, Boolean> iterator) {
        for (DataObjRow r : rows) {
            if (r.getRowState() == RowState.ADDED) {
                if (iterator.apply(r) != true) {
                    return;
                }
            }
        }
    }


    public void getChanges(Function<DataObjRow, Boolean> iterator) {
        for (DataObjRow r : rows) {
            if (r.getRowState() != RowState.UNCHANGED) {
                if (iterator.apply(r) != true) {
                    return;
                }
            }
        }

        for (DataObjRow r : deleteRows) {
            if (iterator.apply(r) != true) {
                return;
            }
        }

    }


    public List<DataObjRow> filter(String expr) {
        Expression expression = new Expression(this, expr);
        return expression.execute();
    }

    public void filter(String expr, Function<DataObjRow, Boolean> iterator) {
        checkNotNull(iterator);
        Expression expression = new Expression(this, expr);
        expression.execute(iterator);

    }

    public void save() throws SQLException {
        ISqlAdapter globalAdapter = DictGlobalConfig.getSqlAdapter();
        save(globalAdapter, true);
    }

    public void save(ISqlAdapter adapter) throws SQLException {
        save(adapter, false);
    }

    private void save(ISqlAdapter adapter, boolean autoCommit) throws SQLException {
        SqlBuilderFactory.build(adapter).save(this, adapter, autoCommit);
        this.acceptChanges();
    }

    public PageInfo getPageInfo() {
        return pageInfo;
    }

    public void removeCondition(Condition condition) {
        if (!this.conditions.contains(condition)) {
            throw new IllegalArgumentException("dataObj can not find this condition");
        }

        this.conditions.remove(condition);
    }

    public void removeCondition() {
        this.conditions.clear();
    }

    public void removeCondition(String columnName) {
        for (int len = conditions.size(); len >= 0; len--) {
            BaseNode left = conditions.get(len - 1).getLeft();
            BaseNode right = conditions.get(len - 1).getRight();
            if (left != null && left instanceof ColumnNode
                    && StringUtils.equalsIgnoreCase(left.getColumnName(), columnName)) {
                conditions.remove(len - 1);
            }

            if (right != null && right instanceof ColumnNode
                    && StringUtils.equalsIgnoreCase(right.getColumnName(), columnName)) {
                conditions.remove(len - 1);
            }
        }
    }

    public List<Condition> getAllCondition() {
        List<Condition> tmpCondition = Lists.newArrayList();
        tmpCondition.addAll(this.conditions);
        return tmpCondition;
    }

    public List<OrderBy> getAllOrderBy() {
        List<OrderBy> tmpOrderBy = Lists.newArrayList();
        tmpOrderBy.addAll(this.orders);
        return tmpOrderBy;
    }

    public DataObj addCondition(Condition condition) {
        checkNotNull(condition);
        if (condition.getLeft() == null && condition.getRight() == null) {
            throw new IllegalArgumentException("condition left or right not compatible");
        }

        this.conditions.add(condition);
        return this;
    }

    public DataObj addOrderBy(OrderBy orderBy) {
        if (StringUtils.isEmpty(orderBy.getColumnLabel())) {
            throw new NullPointerException("not allow order by to input column label empty or null ");
        }
        this.orders.add(orderBy);
        return this;
    }

    public Condition getFixCondition() {
        return fixCondition;
    }

    public void setFixCondition(Condition fixCondition) {
        this.fixCondition = fixCondition;
    }

    public void search() throws SQLException {
        this.search(DictGlobalConfig.getSqlAdapter());

    }

    public void search(ISqlAdapter adapter) throws SQLException {
        SqlBuilderFactory.build(adapter).search(this, adapter);
    }

    //数据交互结束

    //数据对象结构
    public void parseJson(JSONObject jsonObject) {
        this.name = jsonObject.getString("name");
        this.alias = jsonObject.getString("alias");

        this.tableName = jsonObject.getString("tableName");
        if (StringUtils.isEmpty(this.tableName)) {
            throw new IllegalArgumentException("empty tableName not compatible");
        }

        //cols
        this.columns.clear();
        JSONArray arrCol = jsonObject.getJSONArray("columns");
        for (int i = 0; i < arrCol.size(); i++) {
            JSONObject jsonCol = arrCol.getJSONObject(i);
            this.addColumn(DataObjColumn.create(jsonCol));
        }

        //sub tables
        this.tables.clear();
        JSONArray arrTable = jsonObject.getJSONArray("tables");
        if (arrTable != null) {
            for (int i = 0; i < arrTable.size(); i++) {
                JSONObject jsonTable = arrTable.getJSONObject(i);
                Table tb = Table.create(jsonTable);
                this.tables.add(tb);
            }
        }

        this.whereSql.setLength(0);
        String strWhere = jsonObject.getString("whereSql");
        if (StringUtils.isNotEmpty(strWhere)) {
            this.whereSql.append(strWhere);
        }

        this.orderBySql = jsonObject.getString("orderBySql");
        this.fixWhereSql = jsonObject.getString("fixWhereSql");

        JSONObject jsonPage = jsonObject.getJSONObject("pageInfo");
        this.pageInfo.setStart(jsonPage.getInteger("start"));
        this.pageInfo.setPageSize(jsonPage.getInteger("pageSize"));

        //conditions
        JSONArray arrCdts = jsonObject.getJSONArray("conditions");
        if (arrCdts != null) {
            for (int i = 0; i < arrCdts.size(); i++) {
                conditions.add(Condition.create(arrCdts.getJSONObject(i)));
            }
        }

        //orders
        JSONArray arrOrders = jsonObject.getJSONArray("orders");
        if (arrOrders != null) {
            for (int i = 0; i < arrOrders.size(); i++) {
                JSONObject jsOder = arrOrders.getJSONObject(i);
                OrderBy orderBy = new OrderBy();
                orderBy.setColumnLabel(jsOder.getString("columnLabel"));
                orderBy.setOrderByType(OrderByType.valueOf(jsOder.getString("orderByType")));
                this.orders.add(orderBy);
            }
        }

        //fixCondition
        JSONObject jsonFixCondition = jsonObject.getJSONObject("fixCondition");
        if (jsonFixCondition != null) {
            this.fixCondition = Condition.create(jsonFixCondition);
        }

    }

    public JSONObject toSchemaJson() {
        JSONObject schemaJson = new JSONObject();
        schemaJson.put("name", this.name);
        schemaJson.put("alias", this.alias);
        schemaJson.put("tableName", this.tableName);

        //cols
        JSONArray arrCol = new JSONArray();
        for (int i = 0; i < columns.size(); i++) {
            arrCol.add(columns.get(i).toSchemaJson());
        }
        schemaJson.put("columns", arrCol);

        //sub tables
        JSONArray arrTable = new JSONArray();
        for (int i = 0; i < tables.size(); i++) {
            Table table = tables.get(i);
            arrTable.add(table.toJson());
        }

        schemaJson.put("tables", arrTable);

        schemaJson.put("whereSql", this.whereSql.toString());
        schemaJson.put("orderBySql", this.orderBySql);
        schemaJson.put("fixWhereSql", this.fixWhereSql);
        schemaJson.put("pageInfo", this.pageInfo);

        //conditions
        JSONArray arrCdts = new JSONArray();
        for (int i = 0; i < conditions.size(); i++) {
            arrCdts.add(conditions.get(i).toJson());
        }

        schemaJson.put("conditions", arrCdts);

        //orders
        JSONArray arrOrders = new JSONArray();
        for (int i = 0; i < orders.size(); i++) {
            JSONObject jsOder = new JSONObject();
            jsOder.put("columnLabel", orders.get(i).getColumnLabel());
            jsOder.put("orderByType", orders.get(i).getOrderByType());
            arrOrders.add(jsOder);

        }
        schemaJson.put("orders", arrOrders);

        //fixCondition
        if (this.fixCondition != null) {
            schemaJson.put("fixCondition", this.fixCondition.toJson());
        }

        return schemaJson;
    }

    public void parseSql(String sql, String dbType) {
        //TODO 暂不支持，后续开发 依赖于SqlBuilder
    }

    public String toTableScript(String dbType) {
        return SqlBuilderFactory.build(dbType).getTableScript(this);
    }

    public String toSelectSql(String dbType) {
        return SqlBuilderFactory.build(dbType).getSelectSql(this);
    }

    public String toInsertSql(String dbType, String tbName) throws SQLException {
        return SqlBuilderFactory.build(dbType).getInsertSql(this, tbName);
    }

    public String toUpdateSql(String dbType, String tbName) throws SQLException {
        return SqlBuilderFactory.build(dbType).getUpdateSql(this, tbName);
    }

    public DataObj addColumn(DataObjColumn dc) {
        checkNotNull(dc);
        dc.setDataObj(this);
        this.columnLabels.put(dc.getColumnLabel(), this.columns.size());
        this.columns.add(dc);
        return this;
    }

    /**
     * 只允许无数据时，修改结构
     * @param dc
     * @return
     */
    public DataObj removeColumn(DataObjColumn dc) {
        checkNotNull(dc);
        if (this.rows.size() > 0) {
            throw new IllegalStateException("dataobj has rows");
        }

        if (this.deleteRows.size() > 0) {
            throw new IllegalStateException("dataobj has delete rows");
        }

        if (!this.columns.contains(dc)) {
            this.columns.remove(dc);
            this.columnLabels.remove(dc.getColumnLabel());
            dc.setDataObj(null);
        }
        return this;
    }

    //数据对象结构结束

    //析构，销毁
    public void destroy() {
        this.rows.clear();
        this.columns.clear();
        this.columnLabels.clear();
        this.tables.clear();
        this.onValidate = null;
        this.onRowRemoved = null;
        this.onRowDeleted = null;
        this.onCollectChanged = null;
        this.onCurrentChanged = null;

    }
    //销毁结束
}
