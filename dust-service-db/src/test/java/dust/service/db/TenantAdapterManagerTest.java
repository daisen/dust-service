package dust.service.db;

import dust.service.db.sql.DataTable;
import dust.service.db.sql.ISqlAdapter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.SQLException;

/**
 * 租户适配管理类
 * @author huangshengtao
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:application.properties")
public class TenantAdapterManagerTest {
    @Autowired
    TenantAdapterManager tenantAdapterManager;

    @Test
    public void testTenantGetNow() throws SQLException {
        ISqlAdapter adapter = tenantAdapterManager.getAdapter("12345678", "001");
        DataTable dt = adapter.query("SELECT NOW() ", null);
        Assert.assertNotNull(dt);
    }
}
