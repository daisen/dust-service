package dust.service.db;

/**
 * 数据库异常，不包括有关Sql的异常
 * @author huangshengtao
 */
public class DBException extends Exception {
    private static final long serialVersionUID = 1L;

    public DBException(){
        super();
    }

    public DBException(String message, Throwable cause){
        super(message, cause);
    }

    public DBException(String message){
        super(message);
    }

    public DBException(Throwable cause){
        super(cause);
    }
}
