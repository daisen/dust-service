package dust.service.db;

import com.alibaba.fastjson.JSONObject;
import dust.service.TestApplication;
import dust.service.db.sql.DataTable;
import dust.service.core.util.SnowFlakeIdWorker;
import dust.service.db.dict.condition.ColumnNode;
import dust.service.db.dict.condition.OperationType;
import dust.service.db.dict.condition.ValueNode;
import dust.service.db.sql.ISqlAdapter;
import dust.service.db.dict.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;

/**
 * @author huangshengtao on 2018-1-23.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TestApplication.class)
@TestPropertySource(locations = "classpath:application.properties")
public class DictTest {

    @Autowired
    DbAdapterManager dbAdapterManager;

    @Test
    public void createDataObjFromMySql() {
        DataObj obj = DataObjBuilder.create("*", "*", "1");
        Assert.assertTrue(obj.getName() != null);
    }


    public DataObj createDataObjFromManual() {
        DataObj obj = new DataObj();
        obj.setName("dataobj");
        obj.setTableName("dataobj");
        obj.getPageInfo().setStart(0);
        obj.getPageInfo().setPageSize(50);
        obj.where("1=1");
        obj.setFixCondition(new Condition());
        obj.getFixCondition().setLeft(new ColumnNode("dataobj", "app"));
        obj.getFixCondition().setOperation(OperationType.EQUAL);
        obj.getFixCondition().setRight(new ValueNode("*"));

        DataObjColumn col = new DataObjColumn(DataType.UBIGINT);
        col.setAutoIncrement(true);
        col.setColumnName("id");
        col.setTableName("dataobj");
        col.setPrimaryKey(true);
        col.setRequired(true);
        col.setName("id字段");
        obj.addColumn(col);

        col = new DataObjColumn(DataType.DATE);
        col.setAutoIncrement(true);
        col.setColumnName("gmt_create");
        col.setTableName("dataobj");
        col.setPrimaryKey(false);
        col.setRequired(true);
        col.setName("创建时间");
        col.setDefaultValue("@NOW");
        obj.addColumn(col);

        col = new DataObjColumn(DataType.DATE);
        col.setAutoIncrement(true);
        col.setColumnName("gmt_modified");
        col.setTableName("dataobj");
        col.setPrimaryKey(false);
        col.setRequired(true);
        col.setName("更新时间");
        col.setDefaultValue("@UPDATE");
        obj.addColumn(col);

        col = new DataObjColumn(DataType.STRING);
        col.setAutoIncrement(true);
        col.setColumnName("name");
        col.setTableName("dataobj");
        col.setPrimaryKey(false);
        col.setRequired(false);
        col.setName("名称");
        obj.addColumn(col);

        return obj;
    }

    @Test
    public void getSchemaJson() {
        DataObj obj = createDataObjFromManual();
        JSONObject json = obj.toSchemaJson();
        Assert.assertEquals("", "{\"tables\":[],\"whereSql\":\"1=1\",\"columns\":[{\"dataType\":\"UBIGINT\",\"tableName\":\"dataobj\",\"name\":\"id字段\",\"objId\":0,\"ignore\":false,\"id\":0,\"conditions\":[],\"columnName\":\"id\",\"primaryKey\":true},{\"defaultValue\":\"@NOW\",\"dataType\":\"DATE\",\"tableName\":\"dataobj\",\"name\":\"创建时间\",\"objId\":0,\"ignore\":false,\"id\":0,\"conditions\":[],\"columnName\":\"gmt_create\",\"primaryKey\":false},{\"defaultValue\":\"@UPDATE\",\"dataType\":\"DATE\",\"tableName\":\"dataobj\",\"name\":\"更新时间\",\"objId\":0,\"ignore\":false,\"id\":0,\"conditions\":[],\"columnName\":\"gmt_modified\",\"primaryKey\":false},{\"dataType\":\"STRING\",\"tableName\":\"dataobj\",\"name\":\"名称\",\"objId\":0,\"ignore\":false,\"id\":0,\"conditions\":[],\"columnName\":\"name\",\"primaryKey\":false}],\"name\":\"dataobj\",\"pageInfo\":{\"pageSize\":50,\"start\":0},\"orders\":[],\"fixCondition\":{\"left\":{\"type\":\"COLUMN\",\"tableName\":\"dataobj\",\"columnName\":\"app\"},\"require\":true,\"right\":{\"type\":\"VALUE\",\"value\":\"0\"},\"operation\":\"EQUAL\"},\"conditions\":[],\"tableName\":\"dataobj\"}"
                , json.toJSONString());

    }

    @Test
    public void createTableScript() {
        DataObj obj = createDataObjFromManual();
        String sql = obj.toTableScript("mysql");
        System.out.println(sql);
    }

    @Test
    public void searchData() throws SQLException {
        DataObj obj = createDataObjFromManual();
        DictGlobalConfig.setSqlAdapter(dbAdapterManager.getAdapter("dustdb"));
        obj.search();
        Assert.assertTrue(obj.getRows().size() > 0);
    }

    @Test
    public void searchDataWithCondition() throws SQLException, IOException {
        DataObj obj = createDataObjFromManual();
        DictGlobalConfig.setSqlAdapter(dbAdapterManager.getAdapter("dustdb"));
        Condition condition = new Condition();
        condition.setLeft(new ColumnNode("dataobj", "id"));
        condition.setRight(new ValueNode("1"));
        condition.setOperation(OperationType.EQUAL);
        obj.addCondition(condition);
        obj.search();
        Assert.assertTrue(obj.getRows().size() == 1);
    }

    @Test
    public void bigSearchDataDirect() throws SQLException {
        ISqlAdapter adapter = dbAdapterManager.getAdapter("test");
        for (int i = 0; i < 10; i++) {
            long start = System.currentTimeMillis();
            DataTable dt = adapter.query("Select `id`, `gmt_create`, `gmt_modified`, `alias`, `app`, `module`, `name`, `table_name`, `conditions`, `fix_condition`, `orders`, `start`, `page_size`, `fix_where_sql`, `order_by_sql`, `where_sql` from dataobj limit 0,"
                    + (i + 1) * 1000, null);
            long end = System.currentTimeMillis();
            System.out.println(((i + 1) * 1000) + "," + (end - start));
        }
    }

    @Test
    public void bigSearchData() throws SQLException, IOException {

        DataObj obj = DataObjBuilder.create("test", "*", "dataobj");
        DictGlobalConfig.setSqlAdapter(dbAdapterManager.getAdapter("test"));
        for (int i = 0; i < 10; i++) {
            long start = new Date().getTime();
            obj.getPageInfo().setStart(0);
            obj.getPageInfo().setPageSize((i + 1) * 1000);
            obj.search();
            long end = new Date().getTime();
            System.out.println(((i + 1) * 1000) + "," + (end - start));
        }


        Assert.assertTrue(obj.getRows().size() > 0);
        System.out.println(obj.getRows().size());
    }


    @Test
    public void importData() throws SQLException {
        DataObj obj = DataObjBuilder.create("test", "*", "dataobj");
        DictGlobalConfig.setSqlAdapter(dbAdapterManager.getAdapter("test"));
        int batch = 10000;
        int per = 100;

        long start = System.currentTimeMillis();
        for (int j = 0; j < batch; j++) {
            obj.clear();
            for (int i = 0; i < per; i++) {
                DataObjRow r = obj.newRow();
                obj.addRow(r);
                r.setValue("app", "test");
                r.setValue("module", "test");
                r.setValue("alias", "table" + SnowFlakeIdWorker.getInstance0().nextId());
                r.setValue("name", "测试数据");
                r.setValue("tableName", "table");
                System.out.println(i + "-" + j);
            }
            obj.save();
        }

        long end = System.currentTimeMillis();
        System.out.println(end - start);
        obj.search();
        Assert.assertTrue(obj.getRows().size() == (batch * per));

    }
}
