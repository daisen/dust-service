package dust.service.db.dict;

import dust.db.DustDbProperties;
import dust.db.dict.DictGlobalConfig;
import dust.service.db.DustDbAutoConfiguration;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Configuration;

/**
 * Created by hst on 2018/7/2.
 */
@Configuration
@AutoConfigureAfter(DustDbAutoConfiguration.class)
public class DictAutoConfiguration implements InitializingBean {

    @Autowired
    DustDbProperties dustDbProperties;

    @Override
    public void afterPropertiesSet() throws Exception {
        DictGlobalConfig.setEnableObjId(dustDbProperties.getDict().isEnableObjId());
        DictGlobalConfig.setAutoInitAdapter(dustDbProperties.getDict().isAutoInitAdapter());
        DictGlobalConfig.setDataSourceName(dustDbProperties.getDict().getDataSourceName());
        DictGlobalConfig.setAllowColumnNameOutOfUnderscore(dustDbProperties.getDict().isAllowColumnNameOutOfUnderscore());
        DictGlobalConfig.setContainerClass(dustDbProperties.getDict().getContainerClass());
    }
}
