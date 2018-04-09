package com.example.demo;

import com.example.DemoApplication;
import dust.service.db.DbAdapterManager;
import dust.service.db.sql.ISqlAdapter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.SQLException;

/**
 * @author huangshengtao on 2018-4-9.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {DemoApplication.class})
@TestPropertySource(locations = "classpath:application.properties")
public class TestDb {
    @Autowired
    DbAdapterManager dbAdapterManager;

    @Test
    public void testAdapter() throws SQLException {
        ISqlAdapter sqlAdapter = dbAdapterManager.getAdapter("myTest");
        sqlAdapter.query("select 1", null);
    }
}
