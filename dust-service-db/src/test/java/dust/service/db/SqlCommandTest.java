package dust.service.db;

import dust.service.TestApplication;
import dust.service.db.sql.DataTable;
import dust.service.db.sql.ISqlAdapter;
import dust.service.db.sql.SqlCommand;
import dust.service.db.support.mssql.SqlServerDataBase;
import org.hibernate.validator.constraints.br.TituloEleitoral;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.RowSet;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author huangshengtao on 2018-4-25.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TestApplication.class)
@TestPropertySource(locations = "classpath:application.properties")
public class SqlCommandTest {

    @Autowired
    DbAdapterManager dbAdapterManager;

    @Test
    public void testMsSqlSelect() throws SQLException {
        SqlCommand cmd = new SqlCommand("Select * from test");
        cmd.setPageIndex(0);
        cmd.setPageSize(5);
        cmd.appendOrderString("id");
        ISqlAdapter adapter = dbAdapterManager.getAdapter("mssql");
        DataTable dt = adapter.query(cmd);
        Assert.assertTrue(dt.size() > 0);
    }

    @Test
    public void testMsSqlSelect2() throws SQLException {
        SqlCommand cmd = new SqlCommand("Select * from test");
        cmd.setPageIndex(0);
        cmd.setPageSize(5);
        cmd.appendOrderString("id");
        ISqlAdapter adapter = dbAdapterManager.getAdapter("mssql");
        DataTable dt = adapter.query(cmd);
        Assert.assertTrue(dt.size() > 0);

        Connection connection = adapter.getConnection();
        SqlServerDataBase dataBase = new SqlServerDataBase();
        dataBase.setConnection(connection);
        RowSet rs = dataBase.queryRowSet(cmd);
        Assert.assertTrue(rs.next());
        rs.close();
    }

    @Test
    public void testMsSqlInsert() throws SQLException {
        SqlCommand cmd = new SqlCommand("insert into test(a,b,c) values(:a, :b, :c)");
        for (int i = 0; i < 10; i++) {
            cmd.next();
            cmd.setParameter("a", "a" + i);
            cmd.setParameter("b", "b" + i);
            cmd.setParameter("c", "c" + i);
        }

        ISqlAdapter adapter = dbAdapterManager.getAdapter("mssql");
        adapter.update(cmd);
        adapter.commit();
        adapter.close();
    }
}
