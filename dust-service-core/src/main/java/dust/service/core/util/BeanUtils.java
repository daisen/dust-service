package dust.service.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;


/**
 * 用于获取当前SpringContext里面的Bean对象
 *
 * @author : huangshengtao
 */
@Component
public class BeanUtils implements ApplicationContextAware {

    public static final Logger logger = LoggerFactory.getLogger(BeanUtils.class);

    public static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext context) {
        if (BeanUtils.context == null) {
            BeanUtils.context = context;
        }
    }

    public static Object getBean(String name) {
        name = context.containsBean(name) ? name : CamelNameUtils.toOtherCaseFirstOne(name);
        if (context.containsBean(name)) {
            return context.getBean(name);
        } else {
            logger.info("没有找到名为{}的SpringBean", name);
            return null;
        }
    }

    public static Object getClassByName(String clazz) {
        Object beanObj = null;
        try {
            beanObj = Class.forName(clazz).newInstance(); // 反射获取对象
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            logger.info("Class.forName发生异常", e);
        }
        return beanObj;
    }

}
