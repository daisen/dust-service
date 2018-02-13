package dust.service.db.sql;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * 一种简易的表映射类，方便存取数据，取代原有List Map的方式
 * @author huangshengtao
 */
public class DataTable {
    private List<DataRow> rows = Lists.newArrayList();
    private List<DataColumn> columns = Lists.newArrayList();

    public DataTable() {

    }

    private void init() {
    }

    public List<DataColumn> getColumns() {
        return columns;
    }

    private void addColumn(DataColumn dc) {
        this.columns.add(dc);
    }

    public DataRow addRow(DataRow row) {
        row.setDataTable(this);
        rows.add(row);
        return row;
    }

    public DataRow removeRow(DataRow row) {
        rows.remove(row);
        return row;
    }

    public String get(Integer index, String column) {
        return rows.get(index).get(column);
    }

    public DataRow row(int index) {
        return this.rows.get(index);
    }

    public List<DataRow> getRows() {
        return rows;
    }

    public Integer size() {
        return this.rows.size();
    }

    public void destroy() {
        this.columns.clear();
        for (int i = 0; i < this.rows.size(); i++) {
            this.rows.get(i).destroy();
        }
        this.rows.clear();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("{");
        sb.append("\n\trowCount:");
        sb.append(rows.size());

        sb.append("\n\t}");
        return sb.toString();
    }

}
