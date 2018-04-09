package dust.service.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;


/**
 * 用于获取当前SpringContext里面的Bean对象
 *
 * @author : huangshengtao
 */
public class BeanUtils {

    public static final Logger logger = LoggerFactory.getLogger(BeanUtils.class);

    public static ApplicationContext CONTEXT;

    public static Object getBean(String name) {
        if (CONTEXT == null) {
            return null;
        }

        name = CONTEXT.containsBean(name) ? name : CamelNameUtils.toOtherCaseFirstOne(name);
        if (CONTEXT.containsBean(name)) {
            return CONTEXT.getBean(name);
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
