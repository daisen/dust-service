package dust.service.micro.util;

import dust.service.db.DbAdapterManager;
import dust.service.db.sql.ISqlAdapter;
import dust.service.db.sql.SqlCommand;
import dust.service.micro.config.DustMsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


import java.sql.SQLException;

/**
 * 用于记录日志使用
 */
public class DbLogUtil {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final String SQL_LOG_INSERT= "INSERT INTO dust_log_microservice(content) VALUES(:content)";

    @Autowired
    private DbAdapterManager dbAdapterManager;

    @Autowired
    private DustMsProperties dustMsProperties;

    public void write(String message, Throwable e) {
        if (!dustMsProperties.getLogging().getLogdb().isEnabled()) return;
        ISqlAdapter adapter = dbAdapterManager.getAdapter(dustMsProperties.getLogging().getLogdb().getDataSource());
        if (adapter != null) {
            SqlCommand cmd = new SqlCommand(SQL_LOG_INSERT);
            try {
                String str = e != null ? String.format("%s:%s", message, e.getStackTrace()) : message;
                cmd.setParameter("content", str);
                adapter.update(cmd);
                adapter.commit();
                adapter.closeQuiet();
            } catch (SQLException ex) {
                adapter.closeQuiet();
                log.error("记录日志发生异常，发生详情： {}.{} with cause = {}", DbLogUtil.class, "write", ex.getCause());
            }

        }
    }

    public void write(Throwable e) {
        this.write(e.getMessage(), e);
    }

    public void write(String message) {
        this.write(message, null);
    }
}
