package dust.service.db.tenant;

/**
 * Created by huangshengtao on 2017-3-3.
 */
public class TenantException extends Exception {
    private static final long serialVersionUID = 1L;

    public TenantException(){
        super();
    }

    public TenantException(String message, Throwable cause){
        super(message, cause);
    }

    public TenantException(String message){
        super(message);
    }

    public TenantException(Throwable cause){
        super(cause);
    }
}
