package dust.service.micro.common;

/**
 * 微服务异常基类
 * @author huangshengtao
 */
public class DustMsException extends Exception {
    private static final long serialVersionUID = 1L;

    public DustMsException() {
    }

    public DustMsException(String message, Throwable cause) {
        super(message, cause);
    }

    public DustMsException(String message) {
        super(message);
    }

    public DustMsException(Throwable cause) {
        super(cause);
    }
}
