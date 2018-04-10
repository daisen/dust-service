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
import org.springframework.core.annotation.Order;

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

    private boolean checkIdRelationColumn(DataObj destObj, String tbName) {
        for (int i = 0; i < destObj.getColumnSize(); i++) {
            DataObjColumn col = destObj.getColumn(i);
            if (StringUtils.isNotEmpty(col.getIdColumnLabel()) && StringUtils.equals(col.getTableName(), tbName)) {
                return true;
            }
        }
        return false;
    }


    private int getLastId(ISqlAdapter adapter) throws SQLException {
        SqlCommand cmd = new SqlCommand("SELECT @@IDENTITY as ID");
        DataTable dt = adapter.query(cmd);
        if (dt.getRows().size() == 1) {
            return Converter.toInteger(dt.getRows().get(0).get("ID"));
        }
        return 0;
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

    @Override
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
                return "str_to_date('" + Converter.toString(o, "yyyy-MM-dd HH:mm:ss") + ",'%Y-%m-%d %Y-%m-%d %r')";
        }

        return Converter.toString(o);
    }
}
