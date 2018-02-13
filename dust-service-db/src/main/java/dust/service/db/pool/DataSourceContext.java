package dust.service.db.pool;

/**
 * 数据源相关的上下文
 * 后续可能会用于读写分离，目前只提供用于存放当前
 */
public class DataSourceContext {

    private String name;
    private String url;
    private String username;
    private String password;
    private String dataBase;
    private String validationQuery;
    private boolean enable = true;

    public DataSourceContext() {
        this.name = "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDataBase() {
        return dataBase;
    }

    public void setDataBase(String dataBase) {
        this.dataBase = dataBase;
    }

    public String getValidationQuery() {
        return validationQuery;
    }

    public void setValidationQuery(String validationQuery) {
        this.validationQuery = validationQuery;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\n\tUrl:\"");
        sb.append(this.getUrl());
        sb.append("\"");

        sb.append(",\n\tUsername:\"");
        sb.append(this.getUsername());
        sb.append("\"");

        sb.append(",\n\tName:\"");
        sb.append(this.getName());
        sb.append("\"");

        sb.append("\n\t}");
        return sb.toString();
    }
}
