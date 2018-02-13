package dust.service.db.util;

import org.springframework.core.env.Environment;

import java.util.Properties;

/**
 * Created by huangshengtao on 2017-5-5.
 */
public class DruidPropertiesUtils {
    public static Properties getProperties(Environment env) {
        Properties properties =  new Properties();
        if (env == null) {
            return new Properties();
        }

        {
            String[] keys = {
                    "druid.testWhileIdle",
                    "druid.testOnBorrow",
                    "druid.validationQuery",
                    "druid.useGlobalDataSourceStat",
                    "druid.useGloalDataSourceStat",
                    "druid.filters",
                    "druid.stat.sql.MaxSize",
                    "druid.timeBetweenLogStatsMillis",
                    "druid.clearFiltersEnable",
                    "druid.resetStatEnable",
                    "druid.notFullTimeoutRetryCount",
                    "druid.maxWaitThreadCount",
                    "druid.failFast",
                    "druid.phyTimeoutMillis",
                    "druid.minEvictableIdleTimeMillis",
                    "druid.maxEvictableIdleTimeMillis"
            };

            for (String key : keys) {
                if (env.containsProperty(key)) {
                    properties.setProperty(key, env.getProperty(key));
                }
            }

        }

        return properties;
    }
}
