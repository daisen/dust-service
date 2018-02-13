package dust.service.db;

import dust.service.db.sql.ISqlAdapter;
import dust.service.db.tenant.DbManager;
import dust.service.db.tenant.pojo.AppConfig;
import dust.service.db.tenant.pojo.DbAccess;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.SQLException;

/**
 * DbManager测试类
 * @author huangshengtao
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class DbManagerTest {
    @Autowired
    DbManager dbManager;

    @Autowired
    TenantAdapterManager tenantAdapterManager;

    @Value("${dust.db.test.commit:false}")
    public boolean commit;

    @Test
    public void testInsertApp() throws SQLException {
        AppConfig appConfig = new AppConfig();
        appConfig.setAppId("001");
        appConfig.setAppAlias("myTest");
        appConfig.setStatus("1");
        appConfig.setDbAccessId("1");
        appConfig.setTenantId("12345678");
        ISqlAdapter adapter = tenantAdapterManager.getTenantConfigAdapter();
        adapter.update("use dustdb", null);
        dbManager.insertApp(adapter, appConfig);
        if (commit) {
            adapter.commit();
        }
        adapter.close();
    }

    @Test
    public void testInsertDbAccess() throws SQLException {
        DbAccess dbAccess = new DbAccess();
        dbAccess.setName("myTest");
        dbAccess.setHost("10.16.10.96:4006");
        dbAccess.setStatus("1");
        dbAccess.setUser("root");
        dbAccess.setPassword("Root@123");
        dbAccess.setCluster("ReadWriteSplit");
        ISqlAdapter adapter = tenantAdapterManager.getTenantConfigAdapter();
        adapter.update("use dustdb", null);
        dbManager.insertDbAccess(adapter, dbAccess);
        if (commit) {
            adapter.commit();
        }
        adapter.close();
    }

    @Test
    public void testCreateDb() {
        Assert.assertTrue(dbManager.createDb("12345678", "001"));
    }

}
