package dust.service.micro.repository;

import dust.service.micro.common.DustMsException;

/**
 * 数据库访问异常
 * @author huangshengtao
 */
public class RepositoryException extends DustMsException {
    private static final long serialVersionUID = 1L;

    public RepositoryException() {
    }

    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public RepositoryException(String message) {
        super(message);
    }

    public RepositoryException(Throwable cause) {
        super(cause);
    }
}

