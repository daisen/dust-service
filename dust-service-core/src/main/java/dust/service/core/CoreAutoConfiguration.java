package dust.service.core;

import dust.commons.util.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

/**
 * Created by hst on 2018/7/2.
 */
@Configuration
public class CoreAutoConfiguration {
    public CoreAutoConfiguration(ApplicationContext context) {
        BeanUtils.FACTORY = new BeanUtils.IBeanFactory() {
            @Override
            public boolean containsBean(String name) {
                return context.containsBean(name);
            }

            @Override
            public Object getBean(String name) {
                return context.getBean(name);
            }
        };
    }
}
