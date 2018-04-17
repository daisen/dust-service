package dust.service.db.sql;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import dust.service.core.util.Converter;
import dust.service.db.DustDbRuntimeException;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 数据库Sql命令，dustdb不推荐使用拼接Sql的方式进行数据操作。<br/>
 * 使用SqlCommand的sql加上参数的方式来访问数据库，一是提高效率的同时，也可以防止Sql注入<br/>
 * 推荐使用参数化的sql书写方式，各个数据库对参数化的SQL语句都有优化，同时也可以避免书写错误或者SQL注入的问题<br/>
 *
 * {@link #getJdbcSql()} 返回相应的执行Sql语句，{@link #getJdbcParameters()}返回执行语句的参数<br/>
 *  1.0.2018040301版本之后支持如果没有设置parameter，默认表示执行sql不需要对参数进行处理，即:key, #{key}, @{key}都不会替换为?<br/>
 *
 * @author huangshengtao
 */
public class SqlCommand {

    private static final Integer MAX_PARAMETER_SIZE = 100;
    private static Pattern patterns = Pattern.compile(":([_A-Za-z][_A-Za-z0-9]+)|#\\{([_A-Za-z0-9]+)}|\\$\\{([_A-Za-z0-9]+)}");
    private static final String OR_OPERATION = " OR ";
    private static final String AND_OPERATION = " AND ";
    private static final String INDEX_KEY = "INDEX";

    private final StringBuilder commandText = new StringBuilder();
    private CommandTypeEnum commandType;
    private Map<String, Object> parameters;
    private final ArrayList<String> keys = Lists.newArrayList();
    private final ArrayList<Map<String, Object>> parametersList = new ArrayList<>();
    private final StringBuffer where = new StringBuffer();
    private final ArrayList<String> orderList = new ArrayList<>();
    private Integer pageSize = -1;
    private Integer pageIndex = -1;
    private Integer totalRows = -1;
    private boolean useOrWhere = false;
    private int index = 0;
    private Object tag;

    public SqlCommand(String sql) {
        this.commandText.append(sql);
        init();
    }

    public SqlCommand() {
        init();
    }

    private void init() {
        commandType = CommandTypeEnum.Text;
        next();
    }

    /**
     * 添加Sql参数，Sql语句中以:key的形式来存放参数
     *
     * @param key
     * @param value
     * @return
     */
    public SqlCommand setParameter(String key, Object value) throws SQLException {
        if (commandType == CommandTypeEnum.StoredProcedure && !(value instanceof StoreProcParam)) {
            throw new SQLException("SqlCommand类型设置为存储过程，Parameter只允许StoreProcParam类型");
        }

        if (INDEX_KEY.equals(key)) {
            throw new IllegalArgumentException("parameter key not allow to be " + key);
        }

        if (StringUtils.isNumeric(key)) {
            Integer intKey = Converter.toInteger(key);
            if (intKey != null && intKey >= 0 && intKey < MAX_PARAMETER_SIZE) {
                throw new IllegalArgumentException("parameter key not allow to be zero or positive integer which less than 100");
            }
        }


        parameters.put(key, value);
        return this;
    }

    public SqlCommand appendParameter(Object value) {
        parameters.put("" + parameters.size(), value);
        return this;
    }

    public String getParameterKey(String key) {
        for (int i = 0; ; i++) {
            if (i > 0) {
                key = key + i;
            }

            if (!parameters.containsKey(key)) {
                return key;
            }
        }
    }

    /**
     * 获取相应的Sql参数值
     * 默认指向参数集合列表的最后一个集合，如果需要修改其他的结合，请通过getParametersList来修改
     *
     * @param key
     * @return
     */
    public Object getParameter(String key) {
        return parameters.get(key);
    }

    /**
     * CommandType分为Sql文本语句，存储过程，表名
     *
     * @return
     */
    public CommandTypeEnum getCommandType() {
        return commandType;
    }

    /**
     * CommandType分为Sql文本语句，存储过程，表名
     * 默认值为文本语句
     *
     * @param commandType
     */
    public SqlCommand setCommandType(CommandTypeEnum commandType) {
        this.commandType = commandType;
        return this;
    }

    /**
     * 跳址，创建新的参数集合，并加入到参数集合列表
     * 用于SqlCommand需要批量执行语句时
     *
     * @return
     */
    public Map<String, Object> next() {
        if (parameters != null && parameters.size() == 0) {
            return parameters;
        }
        parameters = Maps.newHashMap();
        this.index = parametersList.size();
        parametersList.add(parameters);
        return parameters;
    }

    public Map<String, Object> iterator() {
        this.index++;
        if (this.index < 0 || this.index >= this.parametersList.size()) {
            this.index--;
            return null;
        }


        return this.parametersList.get(this.index);
    }

    public Map<String, Object> jump(int index) {
        if (index < 0 || index >= this.parametersList.size()) {
            throw new IndexOutOfBoundsException("index must be between 0 and parameters size");
        }

        this.index = 0;
        return this.parametersList.get(index);
    }

    /**
     * 获取jdbc参数列表，jdbc使用？作为参数占位符
     * 需配合{@link SqlCommand#getJdbcSql()} 使用
     *
     * @return
     */
    public Object[] getJdbcParameters() {
        return getJdbcParameters(parameters);
    }

    /**
     * 获取按照Sql语句执行所需的jdbc参数
     * {@link #getJdbcSql()}
     *
     * @param item
     * @return
     */
    private Object[] getJdbcParameters(Map<String, Object> item) {
        List<Object> params = new ArrayList<>();
        if (parametersList.size() > 0 && parameters.size() > 0) {
            if (keys.size() > 0) {
                for (String key : keys) {
                    params.add(item.get(key));
                }
            } else {
                String str = combineSql();
                Matcher m = patterns.matcher(str);
                int psIndex = 0;
                while (m.find()) {
                    String key = getRegexKey(m);
                    Object temp;
                    if (INDEX_KEY.equals(key)) {
                        temp = item.get("" + psIndex);
                    } else if (item.containsKey(key)) {
                        temp = item.get(key);
                    } else {
                        throw new DustDbRuntimeException(m.group(1) + " 缺失参数值");
                    }
                    params.add(temp);
                    psIndex++;
                }
            }
        }
        return params.toArray();
    }

    private String getRegexKey(Matcher m) {
        char firstCh = m.group().charAt(0);
        String key = null;
        if (firstCh == ':') {
            key = m.group(1);
        } else if (firstCh == '#') {
            key = m.group(2);
        } else if (firstCh == '$') {
            key = m.group(3);
        }

        return key;
    }

    /**
     * 获取多组Sql语句执行所需的jdbc参数
     *
     * @return
     */
    public List<Object[]> getJdbcParameterList() {
        return parametersList.stream().map(this::getJdbcParameters).collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 获取Where条件，CommandType为TableDirect时，通常使用该操作
     *
     * @return
     */
    public String getWhere() {
        return where.toString();
    }

    public boolean hasWhere() {
        return where.length() > 0;
    }

    /**
     * 排序字符串，不含Order By等关键字
     *
     * @return
     */
    public String getOrder() {
        StringBuilder sb = new StringBuilder();
        for (String anOrderList : orderList) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(anOrderList);
        }

        return sb.toString();
    }

    /**
     * 增加Order，可以加上排序方式
     *
     * @param order
     * @return
     */
    public SqlCommand addOrder(String order) {
        orderList.add(order);
        return this;
    }

    /**
     * 生成执行的Sql语句
     *
     * @return
     */
    private String combineSql() {

        if (commandType == CommandTypeEnum.StoredProcedure) {
            return commandText.toString();
        }

        StringBuilder desSb = new StringBuilder();
        if (commandType == CommandTypeEnum.TableDirect) {
            desSb.append("SELECT\r\n *\r\nFROM ");
            desSb.append(commandText);
        } else if (commandType == CommandTypeEnum.Text) {
            desSb.append(commandText);
        }

        String wh = getWhere();
        if (!StringUtils.isEmpty(wh)) {
            desSb.append("\r\nWHERE ");
            desSb.append(wh);
        }

        String orderStr = getOrder();
        if (!StringUtils.isEmpty(orderStr)) {
            desSb.append("\r\nORDER BY ");
            desSb.append(orderStr);
        }
        return desSb.toString();

    }

    /**
     * jdbcSql指的是标准的jdbc语句，如
     * <code>
     * select c1, c2 from t1 where c1 = ? and c2 = ?
     * insert into t1 (c1, c2) values ( ?, ?)
     * </code>
     * <p>
     * SqlCommand支持的参数语句为:key的方式， 如
     * <code>
     * select c1, c2 from t1 where c1 = :c1 and c2 = :c2
     * insert into t1 (c1, c2) values ( :c1, :c1)
     * </code>
     * 上述语句会经过参数解析，解析成jdbcSql
     *
     * @return
     */
    public String getJdbcSql() {
//        String str = combineSql();
//        Matcher m = patterns.matcher(str);
//        while (m.find()) {
//            str = str.replaceFirst(getRegExGroup(m.group()), "?");
//        }
//
//        return str;

        return getJdbcExecuteSql(val -> {
            return "?";
        });
    }

    private String getRegExGroup(String srcGroup) {
        return Pattern.quote(srcGroup);
        /*
        if (srcGroup.length() > 0 && srcGroup.charAt(0) == '$') {
            return "\\" + srcGroup;
        }

        return srcGroup;*/
    }

    /**
     * 获取执行sql
     * {@link #getJdbcParameters()}
     *
     * @param sqlFunc
     * @return
     */
    public String getJdbcExecuteSql(Function<Object, String> sqlFunc) {
        keys.clear();
        String str = combineSql();
        if (parametersList.size() == 0 && parameters.size() == 0) {
            return str;
        }

        Matcher m = patterns.matcher(str);
        int psIndex = 0;
        while (m.find()) {
            String key = getRegexKey(m);
            Object temp;
            if (INDEX_KEY.equals(key)) {
                temp = parameters.get("" + psIndex);
                keys.add("" + psIndex);
            } else if (parameters.containsKey(key)) {
                temp = parameters.get(key);
                keys.add(key);
            } else {
                throw new DustDbRuntimeException(m.group(1) + " 缺失参数值");
            }

            String dbValue = "";
            if (sqlFunc == null) {
                if (temp == null) {
                    dbValue = "NULL";
                } else {
                    dbValue = "'" + temp + "'";
                }
            } else {
                dbValue = sqlFunc.apply(temp);
            }
            str = str.replaceFirst(getRegExGroup(m.group()), dbValue);
            psIndex++;

        }
        return str;
    }

    /**
     * 拼接Command，用于commandText以外的拼接
     * 2018.3.5 移除commandText的拼接操作
     *
     * @param whereCmd
     * @param useOrOperator
     * @return
     */
    public SqlCommand append(SqlCommand whereCmd, Boolean useOrOperator) {
        if (whereCmd == null) return this;

        this.appendWhere(whereCmd.getWhere(), useOrOperator);
        this.appendParameters(whereCmd.parameters);
        this.orderList.addAll(whereCmd.orderList);
        return this;
    }

    public SqlCommand append(SqlCommand whereCmd) {
        return append(whereCmd, false);
    }

    /**
     * 增加排序操作
     * 当sql中不存在Where时，可以使用该方法
     * strOrder可以是多个排序字段，也可以携带desc和asc
     *
     * @param strOrder
     * @return
     */
    public SqlCommand appendOrderString(String strOrder) {
        String[] strOrders = strOrder.split(",");
        for (String strOrder1 : strOrders) {
            this.orderList.add(strOrder1);
        }
        return this;
    }

    public SqlCommand resetWhere() {
        this.where.setLength(0);
        return this;
    }

    public SqlCommand appendWhere(String sql) {
        return appendWhere(sql, false);
    }

    public SqlCommand appendParameters(Map<String, Object> ps) {
        if (ps == null) {
            throw new NullPointerException("method appendParameters not allow ps to be null");
        }

        int len = parameters.size();
        int srcLen = ps.size();

        for (Map.Entry<String, Object> entry : ps.entrySet()) {
            if (StringUtils.isNumeric(entry.getKey())) {
                Integer intKey = Converter.toInteger(entry.getKey());
                if (intKey >= srcLen) {
                    throw new IndexOutOfBoundsException("method parameter ps have numeric key and key greater than size");
                }

                parameters.put((len + intKey) + "", entry.getValue());
            } else {
                parameters.put(entry.getKey(), entry.getValue());
            }
        }

        return this;
    }

    /**
     * 语句的Where条件
     * <ul>
     * <li>当sql中不存在Where时，可以使用该方法</li>
     * <li>支持OR拼接</li>
     * </ul>
     * OR操作如果当前没有其他Where条件，则会累积到下一个条件
     * <code>
     * // cmd.where = ""
     * cmd.appendWhere("a=1", true);
     * // cmd.where = "a=1"
     * cmd.appWhere("b=2", false);
     * //cmd.where = "a=1 OR b=2"
     * </code>
     *
     * @param sql
     * @param useOrOperator
     * @return
     */
    public SqlCommand appendWhere(String sql, Boolean useOrOperator) {
        if (StringUtils.isEmpty(sql)) {
            return this;
        }

        if (useOrOperator) {
            if (where.length() > 0) {
                where.append(OR_OPERATION);
            } else {
                this.useOrWhere = true;
            }

        } else {
            if (this.useOrWhere) {
                where.append(OR_OPERATION);
            }

            if (where.length() > 0) {
                where.append(AND_OPERATION);
            }
        }

        where.append(sql);
        return this;
    }

    /**
     * Property Getters and Setters
     *
     *
     */

    /**
     * 获取数据源中执行的Sql语句、表名或者存储过程
     *
     * @return
     */
    public String getCommandText() {
        return commandText.toString();
    }

    public boolean isNewLine() {
        if (commandText.length() < 2) {
            return false;
        }

        return commandText.charAt(commandText.length() - 1) == '\n' && commandText.charAt(commandText.length() - 2) == '\r';
    }

    public boolean hasSql() {
        return commandText.length() > 0;
    }

    /**
     * 设置数据源中执行的Sql语句、表名或者存储过程
     *
     * @param commandText
     * @return
     */
    public SqlCommand setCommandText(String commandText) {
        this.commandText.setLength(0);
        this.commandText.append(commandText);
        return this;
    }

    public SqlCommand resetCommandText() {
        this.commandText.setLength(0);
        return this;
    }

    public SqlCommand appendSql(Object sql) {
        this.commandText.append(sql);
        return this;
    }

    public Integer getBeginIndex() {
        return this.pageIndex * this.pageSize;
    }

    public Integer getEndIndex() {
        return (this.pageIndex + 1) * this.pageSize - 1;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public SqlCommand setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public Integer getPageIndex() {
        return pageIndex;
    }

    public SqlCommand setPageIndex(Integer pageIndex) {
        this.pageIndex = pageIndex;
        return this;
    }

    public Integer getTotalRows() {
        return totalRows;
    }

    public SqlCommand setTotalRows(Integer totalRows) {
        this.totalRows = totalRows;
        return this;
    }

    public Map<String, Object> getParameters() {
        return this.parameters;
    }

    public Object getTag() {
        return tag;
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }
}
