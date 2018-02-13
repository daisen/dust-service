package dust.service.micro.security.jwt;

/**
 * JWT过程发生的异常
 * @author huangshengtao
 */
public class JWTException extends Exception {

    public JWTException(String message, Throwable cause) {
        super(message, cause);
    }

    public JWTException(String message) {
        super(message);
    }

    public static void throwNew(String message, Throwable cause) throws JWTException {
        throw new JWTException(message, cause);
    }
}
