package dust.service.db.sql;

/**
 * 数据库支持的类型
 * <ul>
 *     <li>数字</li>
 *     <li>字符串</li>
 *     <li>时间日期</li>
 *     <li>大文本</li>
 *     <li>时间戳</li>
 *     <li>游标</li>
 *     <li>字节流</li>
 * </ul>
 */
public enum DataTypeEnum {
    NUMBER("0"),
    STRING("1"),
    DATETIME("2"),
    CLOB("3"),
    TIMESTAMP("4"),
    CURSOR("5"),
    BLOB("6");

    private String value;

    DataTypeEnum(String value) {
        this.value = value;
    }

    @Override
    public String toString() {

        return this.value;
    }

}
