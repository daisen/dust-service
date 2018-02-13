package dust.service.db;

import dust.service.db.sql.DataTable;
import dust.service.db.sql.ISqlAdapter;
import dust.service.db.sql.SqlCommand;
import dust.service.db.tenant.TenantConsts;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import java.sql.SQLException;

/**
 * DbAdapterManager的测试类
 * @author huangshengtao
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class DbAdapterManagerTest {
    @Autowired
    DbAdapterManager dbAdapterManager;
    @Test
    public void testConnect() {
        ISqlAdapter adapter = dbAdapterManager.getAdapter("myTest");
        Assert.assertNotNull(adapter);
        dbAdapterManager.destroy();
    }

    @Test
    public void testSwitchSchema() throws SQLException {
        ISqlAdapter adapter = dbAdapterManager.getAdapter("myTest");
        adapter.update("use dustdb", null);
        DataTable dt = adapter.query(TenantConsts.SQL_DB, null);
        Assert.assertNotNull(dt);
        dbAdapterManager.destroy();
    }

    @Test
    public void testInsert() throws SQLException {
        ISqlAdapter adapter = dbAdapterManager.getAdapter("myTest");
        adapter.update("use dustdb", null);
        SqlCommand cmd = new SqlCommand(TenantConsts.SQL_INSERT_DB);
        cmd.setParameter("id", null);
        cmd.setParameter("status", "1");
        cmd.setParameter("remark", null);
        cmd.setParameter("cluster", "96");
        cmd.setParameter("name", "myTest");
        cmd.setParameter("host", "10.16.10.96:4006");
        cmd.setParameter("accessLevel", "Admin");
        cmd.setParameter("permission", null);
        cmd.setParameter("user", "root");
        cmd.setParameter("password", "Root@123");
        int[] arr = adapter.update(cmd);
        Assert.assertNotNull(arr);
        dbAdapterManager.destroy();
    }

    @Test
    public void testUpdate() throws SQLException {
        ISqlAdapter adapter = dbAdapterManager.getAdapter("myTest");
        adapter.update("use dustdb", null);
        SqlCommand cmd = new SqlCommand(TenantConsts.SQL_UPDATE_DB);
        cmd.setParameter("status", "1");
        cmd.setParameter("id", "1");
        int[] arr = adapter.update(cmd);
        Assert.assertNotNull(arr);
        dbAdapterManager.destroy();
    }

    @Test
    public void testGetNow() throws SQLException {
        ISqlAdapter adapter = dbAdapterManager.getAdapter("myTest");
        adapter.update("use dustdb", null);
        DataTable dt = adapter.query("SELECT NOW() FROM dust_app_config", null);
        Assert.assertNotNull(dt);
        dbAdapterManager.destroy();
    }

    @Test
    public void testReadAdapter() throws SQLException {
        ISqlAdapter adapter = dbAdapterManager.getReadAdapter("myTest");
        adapter.update("use dustdb", null);
        SqlCommand cmd = new SqlCommand(TenantConsts.SQL_UPDATE_DB);
        cmd.setParameter("status", "1");
        cmd.setParameter("id", "1");
        int[] arr = adapter.update(cmd);
        Assert.assertNotNull(arr);
        dbAdapterManager.destroy();
    }

    @Test
    public void testBatchSelect() throws SQLException {
        for (int i = 0; i < 100; i++) {
            ISqlAdapter adapter = dbAdapterManager.getAdapter("myTest");
            DataTable dt = adapter.query(TenantConsts.SQL_DB, null);
            System.out.println("查询：" + i);
            Assert.assertNotNull(dt);
        }
    }
}
