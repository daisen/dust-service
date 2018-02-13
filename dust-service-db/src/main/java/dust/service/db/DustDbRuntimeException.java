package dust.service.db;

/**
 * DustDB运行异常，通常帮助开发者开发阶段调试用，原则上开发环境不允许存在{@link RuntimeException}
 *
 * @author huangshengtao
 */
public class DustDbRuntimeException extends RuntimeException{
    private static final long serialVersionUID = 1L;

    public DustDbRuntimeException(){
        super();
    }

    public DustDbRuntimeException(String message, Throwable cause){
        super(message, cause);
    }

    public DustDbRuntimeException(String message){
        super(message);
    }

    public DustDbRuntimeException(Throwable cause){
        super(cause);
    }
}
