package dust.service.db.sql;

/**
 * @see SqlCommand
 * 用于区分SqlCommand的类型
 * dustdb将数据库的操作分为三种情况文本，表（视图），存储过程（函数）
 */
public enum CommandTypeEnum {
    Text, TableDirect, StoredProcedure
}
