package dust.service.db.sql;

/**
 * 数据列
 * @author huangshengtao on 2017-8-25.
 */
public class DataColumn {
    private DataTable dataTable;
    private String name;

    public DataColumn() {

    }

    public DataColumn(DataTable dt) {
        this.dataTable = dt;
    }

    public DataTable getDataTable() {
        return dataTable;
    }

    protected void setDataTable(DataTable dataTable) {
        this.dataTable = dataTable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
