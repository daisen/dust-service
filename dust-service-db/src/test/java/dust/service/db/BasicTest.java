package dust.service.db;

import com.google.common.collect.Lists;

import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.util.Random;

/**
 * 基础测试类，不依赖于Spring
 * @author huangshengtao
 */
public class BasicTest {
    volatile static boolean needSet =true;

    public static void main(String[] args) throws InterruptedException, IOException {
        LocalTest test = new LocalTest();
        for(int i = 0; i < 100; i++) {
            Thread th = new Thread(new Runnable() {
                @Override
                public void run() {
                    test.set();
                    try {
                        int sl = new Random().nextInt(1000);
                        Thread.sleep(sl);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    test.print();

                }
            });
            th.start();
        }

        System.in.read();

    }

    public static class LocalTest {
        static final ThreadLocal<IntTest> localString = new ThreadLocal<>();

        public void set() {
            if (localString.get() != null) {
                System.out.println(this.hashCode() + "异常");
            }
            IntTest cls = new IntTest();
            cls.setN(new Random().nextInt(100));
            localString.set(cls);
            System.out.println(Thread.currentThread().getId() + " 写入" + localString.get().getN());
        }

        public void print() {
            System.out.println(Thread.currentThread().getId() + " 读取" + localString.get().getN());
        }
    }


    public static class IntTest {
        int n;

        public int getN() {
            return n;
        }

        public void setN(int n) {
            this.n = n;
        }
    }

    public static final String url = "jdbc:mysql://10.16.8.96:4006/b";
    public static final String name = "com.mysql.jdbc.Driver";
    public static final String user = "root";
    public static final String password = "Root@123";

    public Connection conn = null;
    public PreparedStatement pst = null;
    public List<Connection> conns = Lists.newArrayList();

    public void getConn() throws InterruptedException {
        for (int i = 0; i < 150; i ++) {
            try {
                Class.forName(name);//指定连接类型
                conn = DriverManager.getConnection(url, user, password);//获取连接
                conn.setAutoCommit(false);
                conns.add(conn);
                pst = conn.prepareStatement("Select 1");//准备执行语句
                pst.execute();
                System.out.println("连接" + i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Thread.sleep(10000);
    }

    public void close() {
        try {
            this.conn.close();
            this.pst.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
