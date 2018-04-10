package dust.service.db.dict.support;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import dust.service.core.util.Converter;
import dust.service.db.dict.*;
import dust.service.db.dict.condition.BaseNode;
import dust.service.db.dict.condition.NodeType;
import dust.service.db.sql.DataRow;
import dust.service.db.sql.DataTable;
import dust.service.db.sql.ISqlAdapter;
import dust.service.db.sql.SqlCommand;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static dust.service.db.dict.DictConstant.DEFAULT_VALUE_NOW;
import static dust.service.db.dict.DictConstant.DEFAULT_VALUE_UPDATE;

/**
 * Sql Builder for Oracle
 *
 * @author huangshengtao on 2018-3-22.
 */
public class OracleBuilder extends SqlBuilder {

    @Override
    public void save(DataObj destObj, ISqlAdapter adapter, boolean autoCommit) throws SQLException {

        if (adapter == null) {
            adapter = DictGlobalConfig.getSqlAdapter();
        }

        if (adapter == null) {
            throw new IllegalThreadStateException("current thread can not find dict sql adapter");
        }

        List<DataObjRow> listInsertRow = Lists.newArrayList();
        List<DataObjRow> listUpdateRow = Lists.newArrayList();
        List<DataObjRow> listDeleteRow = Lists.newArrayList();

        destObj.getChanges(row -> {
            if (row.getRowState() == RowState.DELETED) {
                listDeleteRow.add(row);
            }

            if (row.getRowState() == RowState.MODIFIED) {
                listUpdateRow.add(row);
            }

            if (row.getRowState() == RowState.ADDED) {
                listInsertRow.add(row);
            }
            return true;
        });

        updateAutoIncrementColumn(destObj, adapter, listInsertRow);

        List<SqlCommand> deleteCommands = Lists.newArrayList();
        List<SqlCommand> insertCommands = Lists.newArrayList();
        List<SqlCommand> updateCommands = Lists.newArrayList();

        //构建command
        if (listDeleteRow.size() > 0) {
            deleteCommands.add(getDeleteSqlCore(destObj, destObj.getTableName()));
        }

        if (listInsertRow.size() > 0) {
            insertCommands.add(getInsertSqlCore(destObj, destObj.getTableName()));
        }

        if (listUpdateRow.size() > 0) {
            updateCommands.add(getUpdateSqlCore(destObj, destObj.getTableName()));
        }

        //构建关联表
        List<Table> tables = destObj.getTables();
        for (Table tb : tables) {
            if (tb.getFollowDelete() && listDeleteRow.size() > 0) {
                deleteCommands.add(getDeleteSqlCore(destObj, tb.getTableName()));
            }

            if (tb.getFollowInsert() && listInsertRow.size() > 0) {
                insertCommands.add(getInsertSqlCore(destObj, tb.getTableName()));
            }

            if (tb.getFollowUpdate() && listUpdateRow.size() > 0) {
                insertCommands.add(getUpdateSqlCore(destObj, tb.getTableName()));
            }
        }

        //构建参数
        for (DataObjRow row : listDeleteRow) {
            for (int i = 0; i < deleteCommands.size(); i++) {
                filterCommandParameter(row, deleteCommands.get(i));
            }
        }

        for (DataObjRow row : listUpdateRow) {
            for (int i = 0; i < updateCommands.size(); i++) {
                filterCommandParameter(row, updateCommands.get(i));
            }
        }

        for (DataObjRow row : listInsertRow) {
            for (int i = 0; i < insertCommands.size(); i++) {
                filterCommandParameter(row, insertCommands.get(i));
            }
        }

        //执行
        for (SqlCommand cmd : deleteCommands) {
            executeCommand(cmd, adapter);
        }

        for (SqlCommand cmd : updateCommands) {
            executeCommand(cmd, adapter);
        }

        for (int i = 0; i < insertCommands.size(); i++) {
            executeCommand(insertCommands.get(i), adapter);
        }

        if (autoCommit) {
            adapter.commit();
        }

    }

    private void updateAutoIncrementColumn(DataObj destObj, ISqlAdapter adapter, List<DataObjRow> listInsertRow) throws SQLException {
        if (listInsertRow.size() <= 0) {
            return;
        }

        Map<DataObjColumn, List<Integer>> mapCol = Maps.newHashMap();
        for (int i = 0; i < destObj.getColumnSize(); i++) {
            DataObjColumn col = destObj.getColumn(i);
            if (!StringUtils.equalsIgnoreCase(col.getTableName(), destObj.getTableName())) {
                Table t = destObj.getTable(col.getTableName());
                if (!t.getFollowInsert()) {
                    continue;
                }
            }
            if (col.isAutoIncrement()) {
                mapCol.put(col, getAutoIncrementIds(adapter, col.getTableName(), listInsertRow.size()));
            }
        }

        for (int i = 0; i < listInsertRow.size(); i++) {
            for (DataObjColumn col : mapCol.keySet()) {
                listInsertRow.get(i).setValue(col, mapCol.get(col).get(i));
            }
        }
    }

    private List<Integer> getAutoIncrementIds(ISqlAdapter adapter, String tbName, int len) throws SQLException {
        if (len <= 0) {
            throw new IllegalArgumentException("len");
        }

        checkNotNull(tbName);

        SqlCommand cmd = new SqlCommand();
        cmd.setTotalRows(len);
        cmd.appendSql("SELECT");
        cmd.appendSql(" SEQ_" + tbName);
        cmd.appendSql(".nextval AS ID");
        cmd.appendSql(" FROM dual CONNECT BY rownum <=");
        cmd.appendSql(len);

        DataTable dt = adapter.query(cmd);
        if (dt.getRows().size() <= 0) {
            throw new SQLException("auto increment column can not create id");
        }

        List<Integer> list = Lists.newArrayList();
        for (DataRow dr : dt.getRows()) {
            list.add(Converter.toInteger(dr.get("ID")));
        }
        return list;
    }

    @Override
    protected boolean handleUpdateSqlCoreColumn(DataObjColumn col, StringBuilder sbSet, List<Integer> setIndexes) {
        if (StringUtils.equals(col.getDefaultValue(), DictConstant.DEFAULT_VALUE_UPDATE)) {
            if (sbSet.length() > 0) {
                sbSet.append(",");
            }
            sbSet.append(col.getColumnName());
            sbSet.append("=");
            sbSet.append("SYSDATE");

            return true;
        }

        return false;
    }

    @Override
    protected boolean isInsertColumn(DataObjColumn col) {
        return !col.isIgnore()
                && !StringUtils.equals(col.getDefaultValue(), DictConstant.DEFAULT_VALUE_NOW)
                && !StringUtils.equals(col.getDefaultValue(), DictConstant.DEFAULT_VALUE_UPDATE);
    }

    @Override
    public String getTableScript(DataObj destObj) {
        checkNotNull(destObj);

        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ");
        sb.append(destObj.getTableName());
        sb.append(" (");
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

                primaryKeys.append(col.getColumnName());
            }
        }

        sb.append("\r\n");
        sb.append(");");

        //主键
        sb.append("ALTER TABLE");
        sb.append(destObj.getTableName());
        sb.append("\r\n");
        sb.append("  ADD CONSTRAINT");
        sb.append(" PK_" + destObj.getTableName());
        sb.append(" PRIMARY KEY (");
        sb.append(primaryKeys);
        sb.append(");\r\n");

        //序列
        sb.append("\r\n");
        sb.append("CREATE SEQUENCE ");
        sb.append("SEQ_" + destObj.getTableName());
        sb.append(" minvalue 1 maxvalue 999999999999999999999999999 start with 1 increment by 1 cache 20 order;");
        return sb.toString();
    }

    private String getDbColumnType(DataObjColumn col) {
        StringBuilder sbCol = new StringBuilder();
        sbCol.append(" ");
        sbCol.append(col.getColumnName());
        sbCol.append(" ");
        switch (col.getDataType()) {
            case BINARY:
                sbCol.append("blob");
                break;
            case TEXT:
                sbCol.append("clob");
                break;
            case STRING:
                if (col.getWidth() < 2000) {
                    sbCol.append("varchar2(");
                    sbCol.append(col.getWidth());
                    sbCol.append(")");
                } else {
                    sbCol.append("clob");
                }
                break;
            case INT:
                sbCol.append("number(19,0)");
                break;
            case UBIGINT:
                sbCol.append("number(20,0)");
                break;
            case BOOLEAN:
                sbCol.append("number(1,0)");
                break;
            case CURRENCY:
            case NUMBER:
                sbCol.append("number(");
                sbCol.append(col.getWidth());
                sbCol.append(",");
                sbCol.append(col.getDecimalDigits());
                sbCol.append(")");
                break;
            case DATE:
                sbCol.append("date");
                break;
        }


        if (!StringUtils.isEmpty(col.getDefaultValue())) {
            if (col.getDataType() == DataType.DATE && StringUtils.equals(col.getDefaultValue(), DEFAULT_VALUE_NOW)) {
                sbCol.append(" DEFAULT sysdate");
            } else if (col.getDataType() == DataType.DATE && StringUtils.equals(col.getDefaultValue(), DEFAULT_VALUE_UPDATE)) {
                sbCol.append(" DEFAULT sysdate");
            } else {
                sbCol.append(" DEFAULT ");
                sbCol.append("'");
                sbCol.append(col.getDefaultValue());
                sbCol.append("'");
            }

        }

        if (col.isRequired()) {
            sbCol.append(" NOT NULL");
        }

        return sbCol.toString();
    }


    protected String getDbValue(Object o) {
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
                return "to_date('" + Converter.toString(o, "yyyy-MM-dd HH:mm:ss") + ",'yyyy-mm-dd hh24:mi:ss')";
        }

        return Converter.toString(o);
    }
}
