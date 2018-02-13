package dust.service.db.tenant;

/**
 * 租户常量，包含执行SQL
 * @author huangshengtao
 */
public class TenantConsts {

    public static final String SQL_DB = "" +
            "SELECT id, status, gmt_create, gmt_modified, remark," +
            "cluster, name, host, access_level, permission, user, password " +
            "FROM dust_db_access ";

    public static final String SQL_APP_DB = "" +
            "SELECT id, status, gmt_create, gmt_modified, remark," +
            " tenant_id, db_access_id, app_id, app_alias, db_name " +
            "FROM dust_app_config ";

    public static final String SQL_INSERT_DB = "" +
            "INSERT INTO dust_db_access(id, status, remark," +
            "cluster, name, host, access_level, permission, user, password) " +
            "VALUES(:id, :status, :remark, " +
            ":cluster, :name, :host, :accessLevel, :permission, :user, :password) ";

    public static final String SQL_INSERT_APP_DB = "" +
            "INSERT INTO dust_app_config(id, status, remark, " +
            "tenant_id, db_access_id, app_id, app_alias, db_name) " +
            "VALUES(:id, :status, :remark, " +
            ":tenantId, :dbAccessId, :appId, :appAlias, :dbName)";

    public static final String SQL_UPDATE_DB = "" +
            "UPDATE dust_db_access SET status=:status " +
            "WHERE id=:id ";

    public static final String SQL_UPDATE_APP_DB = "" +
            "UPDATE dust_app_config SET status=:status " +
            "WHERE id=:id ";

    public static final String SQL_FIND_DB = "" +
            "SELECT id, status, gmt_create, gmt_modified, remark," +
            "cluster, name, host, access_level, permission, user, password " +
            "FROM dust_db_access " +
            "WHERE id=:id";

    public static final String SQL_FIND_APP_DB = "" +
            "SELECT id, status, gmt_create, gmt_modified, remark," +
            " tenant_id, db_access_id, app_id, app_alias, db_name " +
            "FROM dust_app_config " +
            "WHERE tenant_id=:tenantId AND app_id=:appId";

    public static final String SQL_FIND_ADMIN_DB = "" +
            "SELECT id, status, gmt_create, gmt_modified, remark," +
            "cluster, name, host, access_level, permission, user, password " +
            "FROM dust_db_access " +
            "WHERE access_level = 'Admin' AND cluster=:cluster";

    public static final String SQL_DELETE_TENANT = "DELETE FROM dust_tenant WHERE id=:id";

    public static final String SQL_DELETE_DB = "DELETE FROM dust_db WHERE id=:id";

    public static final String SQL_DELETE_APP_DB = "DELETE FROM dust_app_db WHERE tenant_id=:id";


    public static final String SQL_CREATE_Schema = "Create DataBase if not exists %s";

    public static final String SQL_GRANT_TENANT = "GRANT SELECT,INSERT,UPDATE,DELETE ON %s.* TO %s@'%%' IDENTIFIED BY '%s'";

    public static final String SQL_GRANT_SUPER = "GRANT ALL ON %s .* TO %s@'%' IDENTIFIED BY '%s'";

    public static final String DB_LEVEL_ADMIN = "Admin";

    public static final String DB_LEVEL_READ_WRITE = "ReadWrite";

    public static final String DB_LEVEL_DEV = "Dev";

    public static final String PASSWORD_PREFIX = "!T";

}
