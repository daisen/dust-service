/**
 * everything will be ok
 *
 * @author : huangxianhui
 * @Version :
 * @Date : 2016年2月3日 下午1:12:56
 */
package dust.service.micro.util;

import dust.commons.util.CustomBase64;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

/**
 * 继承PropertyPlaceholderConfigurer 支持密文属性的属性配置器
 */
public class EnProperty extends PropertyPlaceholderConfigurer {

    @Override
    protected String convertProperty(String propertyName, String propertyValue) {
        if (isEncryptProp(propertyName)) {
            //String decryptValue = DESUtils.getDecryptString(propertyValue);
            String decryptValue = CustomBase64.deconf(propertyValue);
            // System.out.println(propertyValue+" de:--> "+ decryptValue);
            return decryptValue;
        } else {
            return propertyValue;
        }
    }

    /**
     * 判断是否是加密的属性
     *
     * @param propertyName
     * @return
     */
    private boolean isEncryptProp(String propertyName) {
        if (propertyName.endsWith("_e"))
            return true;
        else
            return false;
    }
}
