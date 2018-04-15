package dust.service.db.dict;

import com.google.common.collect.Lists;
import dust.service.db.dict.condition.BaseNode;
import dust.service.db.dict.condition.NodeType;
import dust.service.db.sql.DataTable;
import dust.service.db.sql.ISqlAdapter;
import dust.service.db.sql.SqlCommand;
import javafx.scene.control.Tab;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author huangshengtao on 2018-1-10.
 */
public abstract class SqlBuilder {

    public abstract void save(DataObj destObj, ISqlAdapter adapter, boolean autoCommit) throws SQLException;

    public abstract String getTableScript(DataObj destObj);

    protected abstract String getDbValue(Object o);

    public void search(DataObj destObj, ISqlAdapter adapter) throws SQLException {
        checkNotNull(destObj);
        destObj.clear();
        if (adapter == null) {
            adapter = DictGlobalConfig.getSqlAdapter();
        }

        if (adapter == null) {
            throw new IllegalThreadStateException("current thread can not find dict sql adapter");
        }


        SqlCommand selectCmd = getSelectSqlCore(destObj);

        DataTable dataTable = adapter.query(selectCmd);
        if (dataTable.size() > 0) {
            destObj.loadData(dataTable);
            destObj.getPageInfo().setTotalRows(selectCmd.getTotalRows());
        }
    }

    public String getSelectSql(DataObj obj) {
        SqlCommand selectCmd = getSelectSqlCore(obj);
        return selectCmd.getJdbcExecuteSql(this::getDbValue);
    }

    public String getInsertSql(DataObj destObj, String tbName) throws SQLException {
        Table tb = destObj.getTable(tbName);
        SqlCommand insertCmd = getInsertSqlCore(destObj, tb);
        return insertCmd.getJdbcExecuteSql(this::getDbValue);
    }

    public String getUpdateSql(DataObj destObj, String tbName) throws SQLException {
        Table tb = destObj.getTable(tbName);
        SqlCommand updateCmd = getUpdateSqlCore(destObj, tb);
        return updateCmd.getJdbcExecuteSql(this::getDbValue);
    }

    protected boolean isInsertColumn(DataObjColumn col) {
        return !col.isAutoIncrement()
                && !col.isIgnore()
                && !StringUtils.equals(col.getDefaultValue(), DictConstant.DEFAULT_VALUE_NOW)
                && !StringUtils.equals(col.getDefaultValue(), DictConstant.DEFAULT_VALUE_UPDATE);
    }

    protected boolean isUpdateColumn(DataObjColumn col) {
        return !col.isIgnore()
                && !StringUtils.equals(col.getDefaultValue(), DictConstant.DEFAULT_VALUE_NOW)
                && !StringUtils.equals(col.getDefaultValue(), DictConstant.DEFAULT_VALUE_UPDATE);
    }

    /**
     * 处理插入列
     *
     * @param col
     * @param sbCols
     * @param sbValues
     * @param colIndexes
     * @return 是否已经处理，true-表示已经处理，不需要默认操作；false-继续走默认操作
     */
    protected boolean handleInsertSqlCoreColumn(DataObjColumn col, StringBuilder sbCols, StringBuilder sbValues, List<Integer> colIndexes) {
        return false;
    }

    /**
     * 处理更新列
     *
     * @param col
     * @param sbSet
     * @param setIndexes
     * @return 是否已经处理，true-表示已经处理，不需要默认操作；false-继续走默认操作
     */
    protected boolean handleUpdateSqlCoreColumn(DataObjColumn col, StringBuilder sbSet, List<Integer> setIndexes) {
        return false;
    }

    private SqlCommand getSelectSqlCore(DataObj obj) {
        SqlCommand selectCmd = new SqlCommand();
        selectCmd.appendSql("SELECT\r\n");
        for (int i = 0; i < obj.getColumnSize(); i++) {
            DataObjColumn col = obj.getColumn(i);
            if (col.isIgnore()) {
                continue;
            }

            if (!selectCmd.isNewLine()) {
                selectCmd.appendSql(",\r\n  ");
            } else {
                selectCmd.appendSql("  ");
            }

            selectCmd.appendSql(col.getTableName());
            selectCmd.appendSql(".");
            selectCmd.appendSql(col.getColumnName());
            selectCmd.appendSql(" AS ");
            selectCmd.appendSql(col.getColumnLabel());
        }

        selectCmd.appendSql("\r\nFROM ");
        selectCmd.appendSql(obj.getTableName());
        selectCmd.appendSql(" ");
        selectCmd.appendSql(obj.getTableName());

        List<Table> tables = obj.getTables();
        joinTable(obj, selectCmd, tables);

        SqlCommand whereCmd = new SqlCommand();
        List<Condition> conditions = obj.getAllCondition();
        if (conditions.size() > 0) {
            whereCmd.append(getConditionsSql(conditions));
        }

        Condition fixCondition = obj.getFixCondition();
        if (fixCondition != null) {
            whereCmd.append(getConditionSql(fixCondition));
        }

        if (!StringUtils.isEmpty(obj.where())) {
            whereCmd.appendWhere(obj.where());
        }

        if (!StringUtils.isEmpty(obj.fixWhere())) {
            whereCmd.appendWhere(obj.fixWhere());
        }

        if (whereCmd.hasWhere()) {
            selectCmd.append(whereCmd);
        }

        List<OrderBy> orders = obj.getAllOrderBy();
        if (orders.size() > 0) {
            StringBuilder orderSql = new StringBuilder();
            for (int i = 0; i < orders.size(); i++) {
                if (orderSql.length() > 0) {
                    orderSql.append(",");
                }

                orderSql.append(orders.get(i).getColumnLabel());
                orderSql.append(" ");
                if (orders.get(i).getOrderByType() == OrderByType.DESC) {
                    orderSql.append("DESC");
                } else {
                    orderSql.append("ASC");
                }

            }

            if (orderSql.length() > 0) {
//                selectCmd.appendSql("\r\nORDER BY ");
//                selectCmd.appendSql(orderSql);
                selectCmd.appendOrderString(orderSql.toString());
            }
        }

        if (obj.getPageInfo() != null) {
            selectCmd.setPageIndex(obj.getPageInfo().getStart());
            selectCmd.setPageSize(obj.getPageInfo().getPageSize());
            selectCmd.setTotalRows(obj.getPageInfo().getTotalRows());
        }

        return selectCmd;
    }

    protected SqlCommand getInsertSqlCore(DataObj destObj, Table tb) throws SQLException {
        SqlCommand cmd = new SqlCommand();
        cmd.appendSql("INSERT INTO ");
        cmd.appendSql(tb.getTableName());
        cmd.appendSql("(");

        StringBuilder sbCols = new StringBuilder();
        StringBuilder sbValues = new StringBuilder();
        List<Integer> colIndexes = Lists.newArrayList();
        for (int i = 0; i < destObj.getColumnSize(); i++) {
            DataObjColumn col = destObj.getColumn(i);
            if (!isInsertColumn(col)) {
                continue;
            }

            if (StringUtils.equals(col.getTableName(), tb.getAlias())) {

                if (!handleInsertSqlCoreColumn(col, sbCols, sbValues, colIndexes)) {
                    if (sbCols.length() > 0) {
                        sbCols.append(",");
                        sbValues.append(",");
                    }
                    sbCols.append(col.getColumnName());
                    sbValues.append("${INDEX}");

                    if (StringUtils.isEmpty(col.getIdColumnLabel())) {
                        colIndexes.add(i);
                    } else {
                        colIndexes.add(destObj.findIdxByLabel(col.getIdColumnLabel()));
                    }
                }
            }
        }

        if (sbCols.length() == 0) {
            throw new SQLException("not found insert column in table(" + tb.getTableName() + ")");
        }

        cmd.appendSql(sbCols);
        cmd.appendSql(")");
        cmd.appendSql(" VALUES (");
        cmd.appendSql(sbValues);
        cmd.appendSql(")");

        cmd.setTag(colIndexes);
        return cmd;
    }


    protected SqlCommand getUpdateSqlCore(DataObj destObj, Table tb) throws SQLException {
        SqlCommand cmd = new SqlCommand();
        cmd.appendSql("UPDATE ");
        cmd.appendSql(tb.getTableName());
        cmd.appendSql(" SET ");

        StringBuilder sbSet = new StringBuilder();
        StringBuilder pkWhere = new StringBuilder();
        List<Integer> pkIndexes = Lists.newArrayList();
        List<Integer> setIndexes = Lists.newArrayList();
        for (int i = 0; i < destObj.getColumnSize(); i++) {
            DataObjColumn col = destObj.getColumn(i);

            if (!isUpdateColumn(col)) {
                continue;
            }

            if (StringUtils.equals(col.getTableName(), tb.getAlias())) {
                if (!handleUpdateSqlCoreColumn(col, sbSet, setIndexes)) {
                    if (!col.isAutoIncrement()) {
                        if (sbSet.length() > 0) {
                            sbSet.append(",");
                        }
                        sbSet.append(col.getColumnName());
                        sbSet.append("=");
                        sbSet.append("${INDEX}");
                        setIndexes.add(i);
                    }
                }

                if (col.isPrimaryKey()) {
                    if (pkWhere.length() > 0) {
                        pkWhere.append(",");
                    }

                    pkIndexes.add(i);
                    pkWhere.append(col.getColumnName());
                    pkWhere.append("=");
                    pkWhere.append("${INDEX}");
                }
            }
        }

        //set
        if (sbSet.length() == 0) {
            throw new SQLException("not found modified column in table(" + tb.getTableName() + ")");
        }
        cmd.appendSql(sbSet);

        //where
        if (pkWhere.length() == 0) {
            throw new SQLException("not found primary key column in table(" + tb.getTableName() + ")");
        }
        cmd.appendSql(" WHERE ");
        cmd.appendSql(pkWhere);

        setIndexes.addAll(pkIndexes);
        cmd.setTag(setIndexes);
        return cmd;
    }


    protected SqlCommand getDeleteSqlCore(DataObj destObj, Table tb) throws SQLException {
        SqlCommand cmd = new SqlCommand();
        cmd.appendSql("DELETE FROM ");
        cmd.appendSql(tb.getTableName());

        StringBuilder primaryWhere = new StringBuilder();
        List<Integer> primaryIndexes = Lists.newArrayList();
        for (int i = 0; i < destObj.getColumnSize(); i++) {
            DataObjColumn col = destObj.getColumn(i);
            if (!col.isIgnore() && col.isPrimaryKey() && StringUtils.equals(col.getTableName(), tb.getAlias())) {
                if (primaryWhere.length() > 0) {
                    primaryWhere.append(" AND ");
                }

                primaryWhere.append(col.getColumnName());
                primaryWhere.append("=");
                primaryWhere.append("${INDEX}");

                primaryIndexes.add(i);
            }
        }

        if (primaryWhere.length() == 0) {
            throw new SQLException("not found primary key in delete table(" + tb.getTableName() + ")");
        }

        cmd.appendSql(" WHERE ");
        cmd.appendSql(primaryWhere);
        cmd.setTag(primaryIndexes);
//        List<DataObjRow> delRows = destObj.getDeleteRows();
//        for (int i = 0; i < delRows.size(); i++) {
//            if (i > 0) {
//                cmd.next();
//            }
//            DataObjRow dor = delRows.get(i);
//            for (int j = 0; j < primaryIndexes.size(); j++) {
//                cmd.appendParameter(dor.getValue(j));
//            }
//        }
        return cmd;
    }


    private SqlCommand getSubConditionsCommand(List<Condition> subConditions) {
        SqlCommand cmd = new SqlCommand();
        if (subConditions.size() == 0) {
            return cmd;
        }

        for (int i = 0; i < subConditions.size(); i++) {
            Condition condition = subConditions.get(i);
            SqlCommand subCmd = getConditionSql(condition);
            cmd.append(subCmd, !condition.isRequire());
        }

        return cmd;
    }


    public StringBuilder batchInsertSql(DataObj destObj, Table tb) throws SQLException {
        SqlCommand insertCmd = getInsertSqlCore(destObj, tb);
        StringBuilder batchSql = new StringBuilder();
        insertCmd.jump(0);
        while (insertCmd.iterator() != null) {
            batchSql.append(insertCmd.getJdbcExecuteSql(this::getDbValue));
            batchSql.append("\n");
        }
        return batchSql;
    }

    public StringBuilder batchUpdateSql(DataObj destObj, String tbName) throws SQLException {
        Table tb = destObj.getTable(tbName);
        SqlCommand updateCmd = getUpdateSqlCore(destObj, tb);
        StringBuilder batchSql = new StringBuilder();
        updateCmd.jump(0);
        while (updateCmd.iterator() != null) {
            batchSql.append(updateCmd.getJdbcExecuteSql(this::getDbValue));
            batchSql.append("\n");
        }
        return batchSql;
    }



    private SqlCommand getConditionsSql(List<Condition> conditions) {
        SqlCommand subWhereCmd = new SqlCommand();
        for (int i = 0; i < conditions.size(); i++) {
            Condition item = conditions.get(i);
            SqlCommand cmd = getConditionSql(item);
            subWhereCmd.append(cmd, !item.isRequire());
        }

        return subWhereCmd;
    }


    protected void joinTable(DataObj obj, SqlCommand selectCmd, List<Table> tables) {
        if (tables.size() > 0) {
            for (Table tb : tables) {
                switch (tb.getRelationType()) {
                    case LEFT:
                        selectCmd.appendSql("\r\nLEFT JOIN ");
                        break;
                    case RIGHT:
                        selectCmd.appendSql("\r\nRIGHT JOIN ");
                        break;
                    case INNER:
                        selectCmd.appendSql("\r\nINNER JOIN ");
                        break;
                }

                selectCmd.appendSql(tb.getTableName());
                selectCmd.appendSql(" ");
                selectCmd.appendSql(tb.getAlias());

                SqlCommand onCmd = new SqlCommand();

                //condtion条件
                List<Condition> conditions = tb.getAllCondition();
                if (conditions.size() > 0) {
                    onCmd.append(getConditionsSql(conditions));
                }

                //关联字段条件
                if (!StringUtils.isEmpty(tb.getColumnName()) && !StringUtils.isEmpty(tb.getRelationColumn())) {
                    StringBuilder onWhere = new StringBuilder();
                    onWhere.append(tb.getAlias());
                    onWhere.append(".");
                    onWhere.append(tb.getColumnName());
                    onWhere.append("=");
                    onWhere.append(obj.getTableName());
                    onWhere.append(".");
                    onWhere.append(tb.getRelationColumn());
                    onCmd.appendWhere(onWhere.toString());
                }

                //自写关联条件
                if (!StringUtils.isEmpty(tb.getRelationWhere())) {
                    //注入风险
                    onCmd.appendWhere(tb.getRelationWhere());
                }

                if (onCmd.hasWhere()) {
                    selectCmd.appendSql(" ON ");
                    selectCmd.appendSql(onCmd.getWhere());
                    selectCmd.appendParameters(onCmd.getParameters());
                }
            }
        }
    }

    protected SqlCommand getConditionSql(Condition condition) {
        SqlCommand cmd = new SqlCommand();
        switch (condition.getOperation()) {
            case EQUAL:
                appendColumnOrValueNodeSql(condition.getLeft(), cmd);
                cmd.appendSql("=");
                appendColumnOrValueNodeSql(condition.getRight(), cmd);
                break;
            case NOT_EQUAL:
                appendColumnOrValueNodeSql(condition.getLeft(), cmd);
                cmd.appendSql("<>");
                appendColumnOrValueNodeSql(condition.getRight(), cmd);
                break;
            case GREATER:
                appendColumnOrValueNodeSql(condition.getLeft(), cmd);
                cmd.appendSql(">");
                appendColumnOrValueNodeSql(condition.getRight(), cmd);
                break;
            case EQUAL_GREATER:
                appendColumnOrValueNodeSql(condition.getLeft(), cmd);
                cmd.appendSql(">=");
                appendColumnOrValueNodeSql(condition.getRight(), cmd);
                break;
            case LESS:
                appendColumnOrValueNodeSql(condition.getLeft(), cmd);
                cmd.appendSql("<");
                appendColumnOrValueNodeSql(condition.getRight(), cmd);
                break;
            case EQUAL_LESS:
                appendColumnOrValueNodeSql(condition.getLeft(), cmd);
                cmd.appendSql("<=");
                appendColumnOrValueNodeSql(condition.getRight(), cmd);
                break;
            case NULL:
                appendColumnOrValueNodeSql(condition.getLeft(), cmd);
                cmd.appendSql(" IS NULL");
                break;
            case NOT_NULL:
                appendColumnOrValueNodeSql(condition.getLeft(), cmd);
                cmd.appendSql(" IS NOT NULL");
                break;
            case CONTAIN:
                appendColumnOrValueNodeSql(condition.getLeft(), cmd);
                cmd.appendSql(" LIKE ");
                appendDimValueNodeSql(condition.getRight(), cmd, 3);
                break;
            case START_WITH:
                appendColumnOrValueNodeSql(condition.getLeft(), cmd);
                cmd.appendSql(" LIKE ");
                appendDimValueNodeSql(condition.getRight(), cmd, 2);
                break;
            case END_WITH:
                appendColumnOrValueNodeSql(condition.getLeft(), cmd);
                cmd.appendSql(" LIKE ");
                appendDimValueNodeSql(condition.getRight(), cmd, 1);
                break;
            case IN_LIST:
                appendColumnOrValueNodeSql(condition.getLeft(), cmd);
                cmd.appendSql(" IN ");
                appendValueListNodeSql(condition.getRight(), cmd);
                break;
            case IN_TABLE:
                if (condition.getRight().getType() != NodeType.SUB_TABLE) {
                    throw new IllegalArgumentException("IN_TABLE operation need sub table");
                }

                appendColumnOrValueNodeSql(condition.getLeft(), cmd);
                cmd.appendSql(" IN (");
                appendSubTableNodeSql(condition.getRight(), cmd);
                cmd.appendSql(")");
            case EXIST:
                if (condition.getRight().getType() != NodeType.SUB_TABLE) {
                    throw new IllegalArgumentException("EXIST operation need sub table");
                }
                cmd.appendSql("EXISTS(");
                appendSubTableNodeSql(condition.getRight(), cmd);
                cmd.appendSql(")");
                break;
            case NOT_EXIST:
                if (condition.getRight().getType() != NodeType.SUB_TABLE) {
                    throw new IllegalArgumentException("NOT_EXIST operation need sub table");
                }
                cmd.appendSql("NOT EXISTS(");
                appendSubTableNodeSql(condition.getRight(), cmd);
                cmd.appendSql(")");
                break;
        }

        if (condition.getSubConditions().size() > 0) {
            SqlCommand otherCmd = getSubConditionsCommand(condition.getSubConditions());
            Condition firstCondition = condition.getSubConditions().get(0);
            cmd.appendSql(firstCondition.isRequire() ? " AND " : " OR ");
            cmd.appendSql(otherCmd.getWhere());
            cmd.appendParameters(otherCmd.getParameters());
        }

        //转化commandText为where
        cmd.resetWhere();
        cmd.appendWhere("(" + cmd.getCommandText() + ")");
        cmd.setCommandText("");
        return cmd;
    }

    protected void appendSubTableNodeSql(BaseNode right, SqlCommand cmd) {
        if (right.getType() == NodeType.SUB_TABLE) {
            cmd.appendSql("SELECT ");

            if (!StringUtils.isEmpty(right.getColumnName())) {
                cmd.appendSql(right.getColumnName());
            } else {
                cmd.appendSql("1");
            }

            cmd.appendSql(" FROM ");
            cmd.appendSql(right.getTableName());
            cmd.appendSql(" ");
            cmd.appendSql("t");
            List<Condition> subConditions = right.getSubConditions();
            if (subConditions.size() > 0) {
                cmd.appendSql(" WHERE ");
                SqlCommand subCmd = getSubConditionsCommand(subConditions);
                cmd.appendSql(subCmd.getWhere());
                cmd.appendParameters(subCmd.getParameters());
            }
        }
    }

    protected void appendColumnOrValueNodeSql(BaseNode node, SqlCommand cmd) {
        switch (node.getType()) {
            case COLUMN:
                cmd.appendSql(node.getTableName());
                cmd.appendSql(".");
                cmd.appendSql(node.getColumnName());
                break;
            case VALUE:
                cmd.appendSql("${INDEX}");
                cmd.appendParameter(node.getTypeValue());
                break;
        }
    }

    /**
     * 模糊匹配，dim=2 首匹配 dim=1 末尾匹配 dim=3 包含匹配
     *
     * @param node
     * @param cmd
     * @param dim
     */
    protected void appendDimValueNodeSql(BaseNode node, SqlCommand cmd, int dim) {
        StringBuilder sbValue = new StringBuilder();
        if (node.getType() == NodeType.VALUE) {
            if ((dim & 0x1) == 1) {
                sbValue.append("%");
            }

            sbValue.append(node.getValue());

            cmd.appendSql("${INDEX}");
            if ((dim & 0x2) == 2) {
                sbValue.append("%");
            }
            cmd.appendParameter(sbValue.toString());
        }
    }


    protected void appendValueListNodeSql(BaseNode node, SqlCommand cmd) {
        cmd.appendSql("(");
        if (node.getType() == NodeType.VALUE_lIST) {
            int len = node.getValues().size();
            for (int i = 0; i < len; i++) {
                if (i > 0) {
                    cmd.appendSql(",");
                }
                cmd.appendSql("${INDEX}");
                cmd.appendParameter(node.toTypeValue(node.getValues().get(i)));
            }
        }

        cmd.appendSql(")");
    }

    /**
     * 根据SqlCommand的tag将DataObjRow转化为参数
     * @param row
     * @param cmd
     */
    protected void filterCommandParameter(DataObjRow row, SqlCommand cmd) {
        cmd.next();
        List<Integer> indexes = (List<Integer>) cmd.getTag();
        for (int i = 0; i < indexes.size(); i++) {
            cmd.appendParameter(row.getValue(indexes.get(i)));
        }
    }

    /**
     * 执行SqlCommand，如果SqlCommand没有参数，则不需要执行
     * 用于insert和update
     * @param cmd
     * @param adapter
     * @throws SQLException
     */
    protected void executeCommand(SqlCommand cmd, ISqlAdapter adapter) throws SQLException {
        if (cmd.getParameters().size() > 0) {
            adapter.update(cmd);
        }
    }
}
