package dust.service.db.sql;

import com.alibaba.druid.util.StringUtils;
import com.google.common.collect.Maps;
import dust.service.core.util.Converter;

import java.util.Map;
import java.util.function.Function;

/**
 * 一种简易的数据行映射，取代原来的Map方式存放数据
 */
public class DataRow {
    private Map<String, String> data = Maps.newHashMap();
    private Map<String, String> origin = Maps.newHashMap();

    private DataTable dataTable;


    public DataRow() {

    }

    public DataRow(DataTable dt) {
        this.dataTable = dt;
    }

    /**
     * 获取指定列的值
     * 列名不区分大小写，空值无效
     * @param column
     * @return
     */
    public String get(String column) {
        if (StringUtils.isEmpty(column)) {
            return null;
        }

        String name = column.toLowerCase();
        return data.get(name);
    }


    /**
     * 列值
     * @param column
     * @return
     */
    public String col(String column) {
        return get(column);
    }

    /**
     * 设置某一列的值，Object会自动转化为字符串存放
     * 列名为空值无效
     * @param column
     * @param value
     * @return 返回DataRow对象，可完成连续操作
     */
    public DataRow set(String column, Object value) {
        if (!StringUtils.isEmpty(column)) {
            String strValue = Converter.toString(value);
            data.put(column.toLowerCase(), strValue);
        }
        return this;
    }

    public DataTable getDataTable() {
        return dataTable;
    }

    protected void setDataTable(DataTable dataTable) {
        this.dataTable = dataTable;
    }

    public void iterator(Function<String, Boolean> func) {
        for(String str : this.data.keySet()) {
            if (!func.apply(str)) {
                return;
            }
        }
    }


    public Map toMap() {
        return Maps.newHashMap(this.data);
    }

    public void destroy() {
        this.data.clear();
        this.origin.clear();

    }
}
