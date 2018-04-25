package dust.service.db;

import dust.service.TestApplication;
import dust.service.db.sql.SqlCommand;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author huangshengtao on 2018-4-25.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TestApplication.class)
@TestPropertySource(locations = "classpath:application.properties")
public class SqlCommandTest {

    @Autowired
    DbAdapterManager dbAdapterManager;

    public void testMsSqlSelect() {
        SqlCommand cmd = new SqlCommand("Select ");

    }
}
