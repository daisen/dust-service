package dust.service.micro.common;

/**
 * 业务异常基类
 * @author huangshengtao
 */
public class BuzException extends Exception {
    private static final long serialVersionUID = 1L;

    public BuzException() {
    }

    public BuzException(String message, Throwable cause) {
        super(message, cause);
    }

    public BuzException(String message) {
        super(message);
    }

    public BuzException(Throwable cause) {
        super(cause);
    }
}
