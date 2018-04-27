package dust.service.db.support;

import dust.service.db.sql.IDataBase;
import dust.service.db.support.mssql.SqlServerDataBase;
import dust.service.db.support.mysql.MySqlDataBase;
import dust.service.db.support.oracle.OracleDataBase;

import static dust.service.db.support.DataBaseFactory.JdbcConstants.*;

/**
 * 数据源工厂类
 *
 * @author huangshengtao
 */
public class DataBaseFactory {
    public static IDataBase create(String url) {
        IDataBase dataBase;
        String dbType = getDbType(url);
        if (dbType == null) {
            return new MySqlDataBase();
        }

        switch (dbType.toLowerCase()) {
            case JdbcConstants.ORACLE:
                dataBase = new OracleDataBase();
                break;
            case JdbcConstants.MYSQL:
                dataBase = new MySqlDataBase();
                break;
            case JdbcConstants.SQL_SERVER:
                dataBase = new SqlServerDataBase();
                break;
            default:
                dataBase = new MySqlDataBase();
        }
        return dataBase;
    }

    public static String getDbType(String rawUrl) {

        if (rawUrl == null) {
            return null;
        }

        if (rawUrl.startsWith("jdbc:derby:") || rawUrl.startsWith("jdbc:log4jdbc:derby:")) {
            return DERBY;
        } else if (rawUrl.startsWith("jdbc:mysql:") || rawUrl.startsWith("jdbc:cobar:")
                || rawUrl.startsWith("jdbc:log4jdbc:mysql:")) {
            return MYSQL;
        } else if (rawUrl.startsWith("jdbc:mariadb:")) {
            return MARIADB;
        } else if (rawUrl.startsWith("jdbc:oracle:") || rawUrl.startsWith("jdbc:log4jdbc:oracle:")) {
            return ORACLE;
        } else if (rawUrl.startsWith("jdbc:alibaba:oracle:")) {
            return ALI_ORACLE;
        } else if (rawUrl.startsWith("jdbc:microsoft:") || rawUrl.startsWith("jdbc:log4jdbc:microsoft:")) {
            return SQL_SERVER;
        } else if (rawUrl.startsWith("jdbc:sqlserver:") || rawUrl.startsWith("jdbc:log4jdbc:sqlserver:")) {
            return SQL_SERVER;
        } else if (rawUrl.startsWith("jdbc:sybase:Tds:") || rawUrl.startsWith("jdbc:log4jdbc:sybase:")) {
            return SYBASE;
        } else if (rawUrl.startsWith("jdbc:jtds:") || rawUrl.startsWith("jdbc:log4jdbc:jtds:")) {
            return JTDS;
        } else if (rawUrl.startsWith("jdbc:fake:") || rawUrl.startsWith("jdbc:mock:")) {
            return MOCK;
        } else if (rawUrl.startsWith("jdbc:postgresql:") || rawUrl.startsWith("jdbc:log4jdbc:postgresql:")) {
            return POSTGRESQL;
        } else if (rawUrl.startsWith("jdbc:edb:")) {
            return ENTERPRISEDB;
        } else if (rawUrl.startsWith("jdbc:hsqldb:") || rawUrl.startsWith("jdbc:log4jdbc:hsqldb:")) {
            return HSQL;
        } else if (rawUrl.startsWith("jdbc:odps:")) {
            return ODPS;
        } else if (rawUrl.startsWith("jdbc:db2:")) {
            return DB2;
        } else if (rawUrl.startsWith("jdbc:sqlite:")) {
            return "sqlite";
        } else if (rawUrl.startsWith("jdbc:ingres:")) {
            return "ingres";
        } else if (rawUrl.startsWith("jdbc:h2:") || rawUrl.startsWith("jdbc:log4jdbc:h2:")) {
            return H2;
        } else if (rawUrl.startsWith("jdbc:mckoi:")) {
            return "mckoi";
        } else if (rawUrl.startsWith("jdbc:cloudscape:")) {
            return "cloudscape";
        } else if (rawUrl.startsWith("jdbc:informix-sqli:") || rawUrl.startsWith("jdbc:log4jdbc:informix-sqli:")) {
            return "informix";
        } else if (rawUrl.startsWith("jdbc:timesten:")) {
            return "timesten";
        } else if (rawUrl.startsWith("jdbc:as400:")) {
            return "as400";
        } else if (rawUrl.startsWith("jdbc:sapdb:")) {
            return "sapdb";
        } else if (rawUrl.startsWith("jdbc:JSQLConnect:")) {
            return "JSQLConnect";
        } else if (rawUrl.startsWith("jdbc:JTurbo:")) {
            return "JTurbo";
        } else if (rawUrl.startsWith("jdbc:firebirdsql:")) {
            return "firebirdsql";
        } else if (rawUrl.startsWith("jdbc:interbase:")) {
            return "interbase";
        } else if (rawUrl.startsWith("jdbc:pointbase:")) {
            return "pointbase";
        } else if (rawUrl.startsWith("jdbc:edbc:")) {
            return "edbc";
        } else if (rawUrl.startsWith("jdbc:mimer:multi1:")) {
            return "mimer";
        } else if (rawUrl.startsWith("jdbc:dm:")) {
            return JdbcConstants.DM;
        } else if (rawUrl.startsWith("jdbc:kingbase:")) {
            return JdbcConstants.KINGBASE;
        } else if (rawUrl.startsWith("jdbc:log4jdbc:")) {
            return LOG4JDBC;
        } else if (rawUrl.startsWith("jdbc:hive:")) {
            return HIVE;
        } else if (rawUrl.startsWith("jdbc:hive2:")) {
            return HIVE;
        } else if (rawUrl.startsWith("jdbc:phoenix:")) {
            return PHOENIX;
        } else {
            return null;
        }

    }

    public interface JdbcConstants {

        public static final String JTDS              = "jtds";

        public static final String MOCK              = "mock";

        public static final String HSQL              = "hsql";

        public static final String DB2               = "db2";

        public static final String DB2_DRIVER        = "COM.ibm.db2.jdbc.app.DB2Driver";

        public static final String POSTGRESQL        = "postgresql";
        public static final String POSTGRESQL_DRIVER = "org.postgresql.Driver";

        public static final String SYBASE            = "sybase";

        public static final String SQL_SERVER        = "sqlserver";
        public static final String SQL_SERVER_DRIVER = "com.microsoft.jdbc.sqlserver.SQLServerDriver";
        public static final String SQL_SERVER_DRIVER_SQLJDBC4 = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
        public static final String SQL_SERVER_DRIVER_JTDS = "net.sourceforge.jtds.jdbc.Driver";

        public static final String ORACLE            = "oracle";
        public static final String ORACLE_DRIVER     = "oracle.jdbc.OracleDriver";

        public static final String ALI_ORACLE        = "AliOracle";
        public static final String ALI_ORACLE_DRIVER = "com.alibaba.jdbc.AlibabaDriver";

        public static final String MYSQL             = "mysql";
        public static final String MYSQL_DRIVER      = "com.mysql.jdbc.Driver";
        public static final String MYSQL_DRIVER_6    = "com.mysql.cj.jdbc.Driver";

        public static final String MARIADB           = "mariadb";
        public static final String MARIADB_DRIVER    = "org.mariadb.jdbc.Driver";

        public static final String DERBY             = "derby";

        public static final String HBASE             = "hbase";

        public static final String HIVE              = "hive";
        public static final String HIVE_DRIVER       = "org.apache.hive.jdbc.HiveDriver";

        public static final String H2                = "h2";
        public static final String H2_DRIVER         = "org.h2.Driver";

        public static final String DM                = "dm";
        public static final String DM_DRIVER         = "dm.jdbc.driver.DmDriver";

        public static final String KINGBASE          = "kingbase";
        public static final String KINGBASE_DRIVER   = "com.kingbase.Driver";

        public static final String OCEANBASE         = "oceanbase";
        public static final String OCEANBASE_DRIVER  = "com.mysql.jdbc.Driver";

        public static final String INFORMIX          = "informix";

        /**
         * 阿里云odps
         */
        public static final String ODPS              = "odps";
        public static final String ODPS_DRIVER       = "com.aliyun.odps.jdbc.OdpsDriver";

        public static final String TERADATA          = "teradata";
        public static final String TERADATA_DRIVER   = "com.teradata.jdbc.TeraDriver";

        /**
         * Log4JDBC
         */
        public static final String LOG4JDBC          = "log4jdbc";
        public static final String LOG4JDBC_DRIVER   = "net.sf.log4jdbc.DriverSpy";

        public static final String PHOENIX           = "phoenix";
        public static final String PHOENIX_DRIVER    = "org.apache.phoenix.jdbc.PhoenixDriver";

        public static final String ENTERPRISEDB        = "edb";
        public static final String ENTERPRISEDB_DRIVER = "com.edb.Driver";
    }
}
