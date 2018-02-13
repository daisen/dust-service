package dust.service.db.dict;

import dust.service.db.sql.ISqlAdapter;

import java.sql.SQLException;

/**
 * @author huangshengtao on 2018-1-10.
 */
public abstract class SqlBuilder {

    public abstract void save(DataObj destObj, ISqlAdapter adapter) throws SQLException;

    public abstract void search(DataObj destObj, ISqlAdapter adapter) throws SQLException;

    public abstract void save(DataObj destObj) throws SQLException;

    public abstract void search(DataObj destObj) throws SQLException;

    public abstract String getTableScript(DataObj destObj);

    public abstract String getSelectSql(DataObj destObj);

    public abstract String getInsertSql(DataObj destObj, String tbName) throws SQLException;

    public abstract String getUpdateSql(DataObj destObj, String tbName) throws SQLException;

    public abstract StringBuilder batchInsertSql(DataObj destObj, String tbName) throws SQLException;

    public abstract StringBuilder batchUpdateSql(DataObj destObj, String tbName) throws SQLException;
}
