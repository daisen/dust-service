package dust.service.core.exception;

/**
 * 程序错误
 * @author huangshengtao
 */
public class DustException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public DustException(){
        super();
    }

    public DustException(String message, Throwable cause){
        super(message, cause);
    }

    public DustException(String message){
        super(message);
    }

    public DustException(Throwable cause){
        super(cause);
    }
}
