package dust.service.db.sql;

/**
 * 存储过程参数枚举
 * <ul>
 *     <li>无</li>
 *     <li>输入</li>
 *     <li>输出</li>
 *     <li>输入输出</li>
 *     <li>返回值</li>
 *     <li>错误信息</li>
 *     <li>错误码</li>
 *     <li>日志详情</li>
 *     <li>函数返回值</li>
 * </ul>
 * @author huangshengtao
 */
public enum ProcParaTypeEnum {
    NONE("0"),
    INPUT("1"),
    OUTPUT("2"),
    INOUT("3"),
    RETURN("4"),
    ERRMSG("5"),
    ERRCODE("6"),
    LOGERRMSG("7"),
    FUNCRESULT("8");

    private String value;

    ProcParaTypeEnum(String value) {
        this.value = value;
    }

    @Override
    public String toString() {

        return this.value;
    }

}
