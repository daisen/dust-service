package dust.service.db;

import com.google.common.collect.Lists;
import dust.service.TestApplication;
import dust.service.db.druid.DruidTemplate;
import dust.service.db.sql.DataTable;
import dust.service.db.sql.SqlCommand;
import dust.service.db.support.oracle.OracleDataBase;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 测试Druid连接池性能
 *
 * @author huangshengtao
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TestApplication.class)
@TestPropertySource(locations = "classpath:application.properties")
public class DruidTemplateTest {
    @Autowired
    DruidTemplate druidTemplate;

    @Test
    public void testConnect() throws SQLException, InterruptedException {

        List<Connection> lists = Lists.newArrayList();
        for (int i = 0; i < 150; i++) {
            Connection connection = druidTemplate.getConnection("myTest");
            connection.setAutoCommit(true);
            PreparedStatement ps = connection.prepareStatement("SELECT id from sys_role");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println(rs.getString("id"));
                while (rs.next()) {
                    System.out.println(rs.getString("id"));
                }
            } else {
                System.out.println("null");
            }
            rs.close();

            lists.add(connection);
            Thread.sleep(1000);
            System.out.println("连接: " + i);
        }

    }

    @Test
    public void testOracleJdbc() throws SQLException {
        //jdbc:oracle:thin:@//111.198.136.108:1521/orcl
        Connection connection = druidTemplate.getConnection("oracle");
        connection.setAutoCommit(true);
        PreparedStatement ps = connection.prepareStatement("SELECT sysdate from dual");
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            System.out.println(rs.getString("sysdate"));
            while (rs.next()) {
                System.out.println(rs.getString("sysdate"));
            }
        } else {
            System.out.println("null");
        }
        rs.close();
    }

    @Test
    public void testNewSqlCommand() throws SQLException {
        SqlCommand cmd = new SqlCommand();
        cmd.appendSql("SELECT * FROM dataobj");
        cmd.appendWhere("id=#{INDEX}");
        cmd.appendParameter(1);
        cmd.appendWhere("alias=:1alias");
        cmd.setParameter("1alias", "dataobj");
        Connection connection = druidTemplate.getConnection("oracle");
        connection.setAutoCommit(true);

        OracleDataBase dataBase = new OracleDataBase();
        dataBase.setConnection(connection);
        DataTable dt = dataBase.query(cmd);
        Assert.assertTrue(dt.size() == 1);
    }

}
