package dust.service.db;

import dust.service.TestApplication;
import dust.service.db.dict.DataObj;
import dust.service.db.dict.DataObjBuilder;
import dust.service.db.dict.DictGlobalConfig;
import dust.service.db.sql.DataTable;
import dust.service.db.sql.ISqlAdapter;
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
 * @author huangshengtao on 2018-2-9.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TestApplication.class)
@TestPropertySource(locations = "classpath:application.properties")
public class DictPerformanceTest {
    @Autowired
    DbAdapterManager dbAdapterManager;

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


}
