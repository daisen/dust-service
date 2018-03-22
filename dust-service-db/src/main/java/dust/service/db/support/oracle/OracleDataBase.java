package dust.service.db.support.oracle;

import dust.service.db.DustDbRuntimeException;
import dust.service.db.sql.*;
import dust.service.db.support.DataBaseFactory;
import oracle.jdbc.OracleTypes;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.sql.RowSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 支持Oracle的DataBase实现
 * @author huangshengtao
 */
@Component
@Scope("prototype")
public class OracleDataBase extends DataBaseImpl {

    @Override
    public RowSet queryRowSet(SqlCommand cmd) throws SQLException {
        if (cmd.getCommandType() == CommandTypeEnum.StoredProcedure) {
            return super.queryRowSet(cmd);
        }

        if (cmd.getTotalRows() <= 0) {
            cmd.setTotalRows(getTotalRows(cmd));
        }

        String execSql = cmd.getJdbcSql();
        Object[] params = cmd.getJdbcParameters();
        if (cmd.getPageSize() > 0) {
            execSql = "select * from(select row_.*,rownum rownum_ from(" + execSql
                    + ")row_ )  where rownum_ >= ? and  rownum_ <= ?";

            params = ArrayUtils.addAll(params, new Object[]{cmd.getBeginIndex() + 1, cmd.getEndIndex() + 1});
        }
        PreparedStatement statement = getConnection().prepareStatement(execSql);
        RowSet rs = executeQuery(statement, params);
        if (cmd.getTotalRows() <= 0) {
            cmd.setTotalRows(rs.getFetchSize());
        }
        return rs;
    }



    @Override
    protected List<StoreProcParam> orderParams(SqlCommand cmd) throws SQLException {
        String storedProcName = cmd.getCommandText();
        String packName = storedProcName.lastIndexOf('.') > 0 ? storedProcName.substring(0,
                storedProcName.lastIndexOf('.')).toUpperCase() : null;
        String procName = storedProcName.substring(storedProcName.indexOf('.') + 1, storedProcName.length())
                .toUpperCase();
        Map<String, Object> paramMap = cmd.getParameters();
        Map<String, StoreProcParam> paramMapUpper = new HashMap<>();
        boolean nullable = false;
        for (String key : paramMap.keySet()) {
            StoreProcParam p = (StoreProcParam) paramMap.get(key);
            if(p.getParamIoType() == ProcParaTypeEnum.FUNCRESULT) {
                nullable = true;
                continue;
            }
            paramMapUpper.put(key.toUpperCase(), p);
        }
        String sql;
        List<StoreProcParam> params = new ArrayList<>();
        if (StringUtils.isEmpty(packName)) {
            sql = "select nvl(a.argument_name,a.object_name) ARGUMENT_NAME, OVERLOAD, POSITION from user_arguments  a "
                    + " where a.OBJECT_NAME='" + procName + "' order by OVERLOAD, position";
        } else {
            sql = "select nvl(a.argument_name,a.object_name) ARGUMENT_NAME, OVERLOAD, POSITION from user_arguments  a "
                    + " where a.PACKAGE_NAME='" + packName + "' and  a.OBJECT_NAME='" + procName
                    + "' order by OVERLOAD, position";
        }

        RowSet rs = queryRowSet(new SqlCommand(sql));
        // Boolean hit = true;
        String oldOverload = "", newOverload = "", position, columnName;
        while (true) {
            while (StringUtils.equals(oldOverload, newOverload)) {
                if (rs.isAfterLast() || !rs.next()) {
                    throw new DustDbRuntimeException("系统找不到符合要求的" + procName);
                }
                newOverload = rs.getString("OVERLOAD");
            }
            oldOverload = newOverload;
            // 判断是否符合类型要求，存储过程OR函数
            position = rs.getString("POSITION");
            columnName = rs.getString("ARGUMENT_NAME");
            if ((position.equals("0") && !nullable) || (!position.equals("0") && nullable)) {
                continue;
            }
            // 某个overload内检查参数是否一致
            while (StringUtils.equals(oldOverload, newOverload)) {

                if (!position.equals("0") && paramMapUpper.containsKey(columnName)) {
                    params.add(paramMapUpper.get(columnName));
                }
                if (!rs.next()) {
                    break;
                }
                newOverload = rs.getString("OVERLOAD");
                position = rs.getString("POSITION");
                columnName = rs.getString("ARGUMENT_NAME");
            }

            if (!nullable && paramMapUpper.size() == params.size()) {
                return params;
            }
            if (nullable && (paramMapUpper.size() - params.size()) == 1) {
                return params;
            }
            params.clear();
        }
    }

    @Override
    protected int getSqlType(StoreProcParam p) {
        switch (p.getParamDataType()) {
            case NUMBER:
                return OracleTypes.NUMBER;
            case DATETIME:
                return OracleTypes.DATE;
            case STRING:
                return OracleTypes.VARCHAR;
            case CLOB:
                return OracleTypes.CLOB;
            case TIMESTAMP:
                return OracleTypes.NUMBER;
            case CURSOR:
                return OracleTypes.CURSOR;
            case BLOB:
                return OracleTypes.BLOB;
            default:
                return OracleTypes.VARCHAR;
        }
    }

    @Override
    public String getDbType() {
        return DataBaseFactory.JdbcConstants.ORACLE;
    }
}
