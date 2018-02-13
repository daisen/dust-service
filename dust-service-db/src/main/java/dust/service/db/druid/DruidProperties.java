package dust.service.db.druid;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;

/**
 * druid配置信息，通过SpringBoot属性配置操作
 * @author huangshengtao
 */
@ConfigurationProperties("druid")
public class DruidProperties extends HashMap<String, Object> {
}
