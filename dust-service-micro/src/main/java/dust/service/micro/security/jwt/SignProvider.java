package dust.service.micro.security.jwt;

import com.google.common.collect.Lists;
import dust.service.micro.config.DustMsProperties;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * 数据验证类
 * 处理规则
 * <ul>
 * <li>将所有参数（sign除外）按照参数名的字母顺序排序，并用&连接:app_id=123&tenant_id=789&timestamp=12345</li>
 * <li>加上服务的相对地址，组成最后的待加密内容：urlPath + ? + 排序后才参数，如sys/user? app_id=123& tenant_id=789&timestamp=12345</li>
 * <li>将待加密内容进行一次md5，如514a018a1cbb0ff13e0753e5e9d74a71</li>
 * <li>最后的请求内容http://xxxx/ sys/user? app_id=123& tenant_id=789&timestamp=12345&sign=514a018a1cbb0ff13e0753e5e9d74a71</li>
 * </ul>
 *
 * @author huangshengtao
 */
@Component
public class SignProvider {
    private final static Logger logger = LoggerFactory.getLogger(SignProvider.class);

    private static final String SIGN_KEY = "sign";

    @Autowired
    private DustMsProperties dustMsProperties;

    public boolean validateRequest(HttpServletRequest req) {
        if (!isEnable()) {
            if (logger.isDebugEnabled()) {
                logger.debug("未开启数据签名认证");
            }
            return true;
        }

        String sign = req.getParameter(SIGN_KEY);
        if (StringUtils.isEmpty(sign)) return false;

        StringBuilder sb = new StringBuilder();
        sb.append(req.getRequestURI());
        sb.append("?");
        sb.append(orderRequestParams(req));
        Md5PasswordEncoder encoder = new Md5PasswordEncoder();
        String reqSign = encoder.encodePassword(sb.toString(), getSecretKey());
        return StringUtils.equalsIgnoreCase(reqSign, sign);
    }

    private String orderRequestParams(HttpServletRequest req) {
        List<String> stringList = Lists.newArrayList();
        Map<String, String[]> maps = req.getParameterMap();
        maps.forEach((s, strings) -> {
            if (StringUtils.equalsIgnoreCase(s, SIGN_KEY)) return;
            String value = String.format("%s=%s", s, strings != null && strings.length > 0 ? strings[0] : "");
            stringList.add(value);
        });

        stringList.sort((o1, o2) -> StringUtils.compare(o1, o2));
        return StringUtils.join(stringList, "&");
    }

    public String getSecretKey() {
        return dustMsProperties.getSecurity().getAuthentication().getSign().getSecret();
    }

    public boolean isEnable() {
        return dustMsProperties.getSecurity().getAuthentication().getSign().isEnable();
    }
}
