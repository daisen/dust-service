package dust.service.db.dict.support;

import com.google.common.collect.Lists;
import dust.service.db.dict.condition.BaseNode;
import dust.service.db.dict.condition.NodeType;
import dust.service.db.sql.DataTable;
import dust.service.db.sql.ISqlAdapter;
import dust.service.db.sql.SqlCommand;
import dust.service.core.util.Converter;
import dust.service.db.dict.*;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static dust.service.db.dict.DictConstant.DEFAULT_VALUE_NOW;
import static dust.service.db.dict.DictConstant.DEFAULT_VALUE_UPDATE;

/**
 * @author huangshengtao on 2018-1-10.
 */
public class MySqlBuilder extends SqlBuilder {

    @Override
    public void save(DataObj destObj, ISqlAdapter adapter, boolean autoCommit) throws SQLException {

        if (adapter == null) {
            adapter = DictGlobalConfig.getSqlAdapter();
        }

        if (adapter == null) {
            throw new IllegalThreadStateException("current thread can not find dict sql adapter");
        }

        List<SqlCommand> deleteCommands = Lists.newArrayList();
        List<SqlCommand> insertCommands = Lists.newArrayList();
        List<SqlCommand> delayInsertCommands = Lists.newArrayList();
        List<SqlCommand> updateCommands = Lists.newArrayList();

        //构建command
        deleteCommands.add(getDeleteSqlCore(destObj, destObj.getTableName()));
        insertCommands.add(getInsertSqlCore(destObj, destObj.getTableName()));
        updateCommands.add(getUpdateSqlCore(destObj, destObj.getTableName()));

        List<Table> tables = destObj.getTables();
        for (Table tb : tables) {
            if (tb.getFollowDelete()) {
                deleteCommands.add(getDeleteSqlCore(destObj, tb.getTableName()));
            }

            if (tb.getFollowInsert()) {
                if (checkIdRelationColumn(destObj, tb.getTableName())) {
                    delayInsertCommands.add(getInsertSqlCore(destObj, tb.getTableName()));
                } else {
                    insertCommands.add(getInsertSqlCore(destObj, tb.getTableName()));
                }
            }

            if (tb.getFollowUpdate()) {
                insertCommands.add(getUpdateSqlCore(destObj, tb.getTableName()));
            }
        }

        //找出自增列
        Integer autoIncrementColumnIndex = -1;
        for (int i = 0; i < destObj.getColumnSize(); i++) {
            DataObjColumn col = destObj.getColumn(i);
            if (col.isAutoIncrement() && StringUtils.equals(col.getTableName(), destObj.getTableName())) {
                autoIncrementColumnIndex = i;
            }
        }

        //生成参数
        destObj.getChanges(row -> {
            if (row.getRowState() == RowState.DELETED) {
                for (int i = 0; i < deleteCommands.size(); i++) {
                    filterCommandParameter(row, deleteCommands.get(i));
                }
            }

            if (row.getRowState() == RowState.MODIFIED) {
                for (int i = 0; i < updateCommands.size(); i++) {
                    filterCommandParameter(row, updateCommands.get(i));
                }
            }

            if (row.getRowState() == RowState.ADDED) {
                for (int i = 0; i < insertCommands.size(); i++) {
                    filterCommandParameter(row, insertCommands.get(i));
                }
            }
            return true;
        });

        //执行
        for (SqlCommand cmd : deleteCommands) {
            executeCommand(cmd, adapter);
        }

        for (SqlCommand cmd : updateCommands) {
            executeCommand(cmd, adapter);
        }

        for (int i = 0; i < insertCommands.size(); i++) {
            executeCommand(insertCommands.get(i), adapter);
            if (i == 0) {
                //获取LastId
                destObj.setLastId(getLastId(adapter));
            }
        }

        //自增字段赋值
        int id = destObj.getLastId();
        for (DataObjRow row : destObj.getRows()) {
            if (row.getRowState() == RowState.ADDED) {
                row.setValue(autoIncrementColumnIndex, id++);

                //延迟插入,生成参数
                for (int i = 0; i < delayInsertCommands.size(); i++) {
                    filterCommandParameter(row, delayInsertCommands.get(i));
                }
            }
        }

        if (delayInsertCommands.size() > 0) {
            for (SqlCommand cmd : delayInsertCommands) {
                executeCommand(cmd, adapter);
            }

        }

        if (autoCommit) {
            adapter.commit();
        }

    }

    @Override
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

    private boolean checkIdRelationColumn(DataObj destObj, String tbName) {
        for (int i = 0; i < destObj.getColumnSize(); i++) {
            DataObjColumn col = destObj.getColumn(i);
            if (StringUtils.isNotEmpty(col.getIdColumnLabel()) && StringUtils.equals(col.getTableName(), tbName)) {
                return true;
            }
        }
        return false;
    }

    private void filterCommandParameter(DataObjRow row, SqlCommand cmd) {
        cmd.next();
        List<Integer> indexes = (List<Integer>) cmd.getTag();
        for (int i = 0; i < indexes.size(); i++) {
            cmd.appendParameter(row.getValue(indexes.get(i)));
        }
    }

    private int getLastId(ISqlAdapter adapter) throws SQLException {
        SqlCommand cmd = new SqlCommand("SELECT @@IDENTITY as ID");
        DataTable dt = adapter.query(cmd);
        if (dt.getRows().size() == 1) {
            return Converter.toInteger(dt.getRows().get(0).get("ID"));
        }
        return 0;
    }

    private void executeCommand(SqlCommand cmd, ISqlAdapter adapter) throws SQLException {
        if (cmd.getParameters().size() > 0) {
            adapter.update(cmd);
        }
    }

    private SqlCommand getDeleteSqlCore(DataObj destObj, String tbName) throws SQLException {
        SqlCommand cmd = new SqlCommand();
        cmd.appendSql("DELETE FROM ");
        cmd.appendSql(tbName);

        StringBuilder primaryWhere = new StringBuilder();
        List<Integer> primaryIndexes = Lists.newArrayList();
        for (int i = 0; i < destObj.getColumnSize(); i++) {
            DataObjColumn col = destObj.getColumn(i);
            if (!col.isIgnore() && col.isPrimaryKey() && StringUtils.equals(col.getTableName(), tbName)) {
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
            throw new SQLException("not found primary key in delete table(" + tbName + ")");
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

    private SqlCommand getUpdateSqlCore(DataObj destObj, String tbName) throws SQLException {
        SqlCommand cmd = new SqlCommand();
        cmd.appendSql("UPDATE ");
        cmd.appendSql(tbName);
        cmd.appendSql(" SET ");

        StringBuilder sbSet = new StringBuilder();
        StringBuilder pkWhere = new StringBuilder();
        List<Integer> pkIndexes = Lists.newArrayList();
        List<Integer> setIndexes = Lists.newArrayList();
        for (int i = 0; i < destObj.getColumnSize(); i++) {
            DataObjColumn col = destObj.getColumn(i);

            if (col.isIgnore()
                    || StringUtils.equals(col.getDefaultValue(), DictConstant.DEFAULT_VALUE_NOW)
                    || StringUtils.equals(col.getDefaultValue(), DictConstant.DEFAULT_VALUE_UPDATE)) {
                continue;
            }

            if (StringUtils.equals(col.getTableName(), tbName)) {
                if (!col.isAutoIncrement()) {
                    if (sbSet.length() > 0) {
                        sbSet.append(",");
                    }
                    sbSet.append(col.getColumnName());
                    sbSet.append("=");
                    sbSet.append("${INDEX}");
                    setIndexes.add(i);
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
            throw new SQLException("not found modified column in table(" + tbName + ")");
        }
        cmd.appendSql(sbSet);

        //where
        if (pkWhere.length() == 0) {
            throw new SQLException("not found primary key column in table(" + tbName + ")");
        }
        cmd.appendSql(" WHERE ");
        cmd.appendSql(pkWhere);

        setIndexes.addAll(pkIndexes);
//        destObj.getUpdateRows(input -> {
//            cmd.next();
//            for (int i = 0; i < setIndexes.size(); i++) {
//                cmd.appendParameter(input.getValue(setIndexes.get(i)));
//            }
//
//            return true;
//        });

        cmd.setTag(setIndexes);

        return cmd;

    }

    private SqlCommand getInsertSqlCore(DataObj destObj, String tbName) throws SQLException {
        SqlCommand cmd = new SqlCommand();
        cmd.appendSql("INSERT INTO ");
        cmd.appendSql(tbName);
        cmd.appendSql("(");

        StringBuilder sbCols = new StringBuilder();
        StringBuilder sbValues = new StringBuilder();
        List<Integer> colIndexes = Lists.newArrayList();
        for (int i = 0; i < destObj.getColumnSize(); i++) {
            DataObjColumn col = destObj.getColumn(i);
            if (col.isAutoIncrement()
                    || col.isIgnore()
                    || StringUtils.equals(col.getDefaultValue(), DictConstant.DEFAULT_VALUE_NOW)
                    || StringUtils.equals(col.getDefaultValue(), DictConstant.DEFAULT_VALUE_UPDATE)) {
                continue;
            }

            if (StringUtils.equals(col.getTableName(), tbName)) {
                if (sbCols.length() > 0) {
                    sbCols.append(",");
                    sbValues.append(",");
                }
                sbCols.append("`" + col.getColumnName() + "`");
                sbValues.append("${INDEX}");

                if (StringUtils.isEmpty(col.getIdColumnLabel())) {
                    colIndexes.add(i);
                } else {
                    colIndexes.add(destObj.findIdxByLabel(col.getIdColumnLabel()));
                }

            }
        }

        if (sbCols.length() == 0) {
            throw new SQLException("not found insert column in table(" + tbName + ")");
        }

        cmd.appendSql(sbCols);
        cmd.appendSql(")");
        cmd.appendSql(" VALUES (");
        cmd.appendSql(sbValues);
        cmd.appendSql(")");

        cmd.setTag(colIndexes);

//        destObj.getInsertRows(input -> {
//            cmd.next();
//            for (int i = 0; i < colIndexes.size(); i++) {
//                cmd.appendParameter(input.getValue(colIndexes.get(i)));
//            }
//
//            return true;
//        });

        return cmd;
    }

    @Override
    public String getTableScript(DataObj destObj) {
        checkNotNull(destObj);

        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE `");
        sb.append(destObj.getTableName());
        sb.append("` (");
        sb.append("\r\n");

        StringBuilder primaryKeys = new StringBuilder();
        for (int i = 0; i < destObj.getColumnSize(); i++) {
            DataObjColumn col = destObj.getColumn(i);
            if (!StringUtils.equals(col.getTableName(), destObj.getTableName())) {
                continue;
            }

            if (sb.charAt(sb.length() - 1) != '\n') {
                sb.append(",");
                sb.append("\r\n");
            }

            sb.append(getDbColumnType(col));

            if (col.isPrimaryKey()) {
                if (primaryKeys.length() > 0) {
                    primaryKeys.append(",");
                }

                primaryKeys.append("`" + col.getColumnName() + "`");
            }
        }

        sb.append(",\r\n");
        sb.append("  PRIMARY KEY (");
        sb.append(primaryKeys);
        sb.append(")");

        sb.append("\r\n");
        sb.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");

        return sb.toString();
    }

    private String getDbColumnType(DataObjColumn col) {
        StringBuilder sbCol = new StringBuilder();
        sbCol.append("  `");
        sbCol.append(col.getColumnName());
        sbCol.append("` ");
        switch (col.getDataType()) {
            case BINARY:
                sbCol.append("blob");
                break;
            case TEXT:
                if (col.getWidth() < 5000 && col.getWidth() > 0) {
                    sbCol.append("varchar(");
                    sbCol.append(col.getWidth());
                    sbCol.append(")");
                } else {
                    sbCol.append("text");
                }
                break;
            case STRING:
                if (col.getWidth() > 255) {
                    sbCol.append("varchar(");
                } else {
                    sbCol.append("char(");
                }

                sbCol.append(col.getWidth());
                sbCol.append(")");
                break;
            case INT:
                sbCol.append("int");
                break;
            case UBIGINT:
                sbCol.append("bigint unsigned");
                break;
            case BOOLEAN:
                sbCol.append("tinyint unsigned");
                break;
            case CURRENCY:
            case NUMBER:
                sbCol.append("numeric(");
                sbCol.append(col.getWidth());
                sbCol.append(",");
                sbCol.append(col.getDecimalDigits());
                sbCol.append(")");
                break;
            case DATE:
                sbCol.append("datetime");
                break;
        }

        if (col.isRequired()) {
            sbCol.append(" NOT NULL");
        } else if (StringUtils.isEmpty(col.getDefaultValue())) {
            sbCol.append(" DEFAULT NULL");
        }

        if (!StringUtils.isEmpty(col.getDefaultValue())) {
            if (col.getDataType() == DataType.DATE && StringUtils.equals(col.getDefaultValue(), DEFAULT_VALUE_NOW)) {
                sbCol.append(" DEFAULT CURRENT_TIMESTAMP");
            } else if (col.getDataType() == DataType.DATE && StringUtils.equals(col.getDefaultValue(), DEFAULT_VALUE_UPDATE)) {
                sbCol.append(" DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP");
            } else {
                sbCol.append(" DEFAULT ");
                sbCol.append("'");
                sbCol.append(col.getDefaultValue());
                sbCol.append("'");
            }

        }

        if (col.getDataType() == DataType.UBIGINT && col.isAutoIncrement()) {
            sbCol.append(" AUTO_INCREMENT");
        }

        return sbCol.toString();
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
            selectCmd.appendSql("`" + col.getColumnLabel() + "`");
        }

        selectCmd.appendSql("\r\nFROM ");
        selectCmd.appendSql(obj.getTableName());
        selectCmd.appendSql(" ");
        selectCmd.appendSql(obj.getTableName());

        List<Table> tables = obj.getTables();
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
                selectCmd.appendSql(tb.getTableName());

                SqlCommand onCmd = new SqlCommand();

                //condtion条件
                List<Condition> conditions = tb.getAllCondition();
                if (conditions.size() > 0) {
                    onCmd.append(getConditionsSql(conditions));
                }

                //关联字段条件
                if (!StringUtils.isEmpty(tb.getColumnName()) && !StringUtils.isEmpty(tb.getRelationColumn())) {
                    StringBuilder onWhere = new StringBuilder();
                    onWhere.append(tb.getTableName());
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

    private SqlCommand getConditionsSql(List<Condition> conditions) {
        SqlCommand subWhereCmd = new SqlCommand();
        for (int i = 0; i < conditions.size(); i++) {
            Condition item = conditions.get(i);
            SqlCommand cmd = getConditionSql(item);
            subWhereCmd.append(cmd);
        }

        return subWhereCmd;
    }

    private SqlCommand getConditionSql(Condition condition) {
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


    private SqlCommand getSubConditionsCommand(List<Condition> subConditions) {
        SqlCommand cmd = new SqlCommand();
        if (subConditions.size() == 0) {
            return cmd;
        }

        for (int i = 0; i < subConditions.size(); i++) {
            Condition condition = subConditions.get(i);
            SqlCommand subCmd = getConditionSql(condition);
            cmd.append(subCmd);
        }

        return cmd;
    }

    private void appendSubTableNodeSql(BaseNode right, SqlCommand cmd) {
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

    private void appendColumnOrValueNodeSql(BaseNode node, SqlCommand cmd) {
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
    private void appendDimValueNodeSql(BaseNode node, SqlCommand cmd, int dim) {
        if (node.getType() == NodeType.VALUE) {
            if ((dim & 0x1) == 1) {
                cmd.appendSql("%");
            }

            cmd.appendSql("${INDEX}");
            if ((dim & 0x2) == 2) {
                cmd.appendSql("%");
            }
            cmd.appendParameter(node.getTypeValue());
        }
    }
    private void appendValueListNodeSql(BaseNode node, SqlCommand cmd) {
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

    @Override
    public String getSelectSql(DataObj obj) {
        SqlCommand selectCmd = getSelectSqlCore(obj);
        return selectCmd.getJdbcExecuteSql(val -> {
            return "";
        });
    }

    @Override
    public String getInsertSql(DataObj destObj, String tbName) throws SQLException {
        SqlCommand insertCmd = getInsertSqlCore(destObj, tbName);
        return insertCmd.getJdbcExecuteSql(this::getDbValue);
    }

    @Override
    public String getUpdateSql(DataObj destObj, String tbName) throws SQLException {
        SqlCommand updateCmd = getUpdateSqlCore(destObj, tbName);
        return updateCmd.getJdbcExecuteSql(this::getDbValue);
    }

    private String getDbValue(Object o) {
        if (o == null) {
            return "NULL";
        }

        switch (o.getClass().getSimpleName()) {
            case "Integer":
            case "int":
            case "Long":
            case "long":
            case "Short":
            case "short":
            case "Double":
            case "double":
            case "BigDecimal":
            case "Float":
            case "float":
                return Converter.toString(o);
            case "String":
                return "'" + o + "'";
            case "Boolean":
            case "boolean":
                return Converter.toBoolean(o) == true ? "1" : "0";
            case "Date":
                return "str_to_date('" + Converter.toString(o, "yyyy-MM-dd HH:mm:ss") + ",'%Y-%m-%d %Y-%m-%d %r')";
        }

        return Converter.toString(o);
    }

    @Override
    public StringBuilder batchInsertSql(DataObj destObj, String tbName) throws SQLException {
        SqlCommand insertCmd = getInsertSqlCore(destObj, tbName);
        StringBuilder batchSql = new StringBuilder();
        insertCmd.jump(0);
        while (insertCmd.iterator() != null) {
            batchSql.append(insertCmd.getJdbcExecuteSql(this::getDbValue));
            batchSql.append("\n");
        }
        return batchSql;
    }

    @Override
    public StringBuilder batchUpdateSql(DataObj destObj, String tbName) throws SQLException {
        SqlCommand updateCmd = getUpdateSqlCore(destObj, tbName);
        StringBuilder batchSql = new StringBuilder();
        updateCmd.jump(0);
        while (updateCmd.iterator() != null) {
            batchSql.append(updateCmd.getJdbcExecuteSql(this::getDbValue));
            batchSql.append("\n");
        }
        return batchSql;
    }
}
