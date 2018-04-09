package dust.service.core;

import dust.service.core.util.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

/**
 * @author huangshengtao on 2018-4-9.
 */
@Configuration
public class CoreAutoConfiguration implements InitializingBean {
    @Autowired
    ApplicationContext applicationContext;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.CONTEXT = applicationContext;
    }


}
