package dust.service.micro.security.jwt;

import dust.service.core.util.CustomBase64;
import dust.service.micro.config.DustMsProperties;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 转码工具类，用于避免一些敏感信息的明文传输
 * 主要是请求参数的转码，现阶段暂不处理header的内容
 *
 * @author huangshengtao
 */
@Component
public class EncodeProvider {
    private final Logger logger = LoggerFactory.getLogger(TokenProvider.class);
    public static final String ENCODE_TYPE = "CUSTOM_BASE64";
    public static final int LEVEL_TOKEN = 0;
    public static final int LEVEL_PARAMETER = 1;
    public static final int LEVEL_ALL = 2;


    @Autowired
    private DustMsProperties dustMsProperties;

    @Autowired(required = false)
    private IEncodeFactory encodeFactory;

    /**
     * 解密方法，用于解密相应的内容
     *
     * @param encodeString
     * @return
     */
    public String decode(String encodeString) {
        String encodeType = getEncodeType();
        if (StringUtils.isEmpty(encodeType) || StringUtils.equalsIgnoreCase(encodeType, ENCODE_TYPE)) {
            return CustomBase64.decode(encodeString);
        }

        if (encodeFactory != null) {
            return encodeFactory.decode(encodeType, encodeString);
        }

        return encodeString;
    }

    public boolean isEnable() {
        return dustMsProperties.getSecurity().getAuthentication().getEncode().isEnable();
    }

    public String getEncodeType() {
        return dustMsProperties.getSecurity().getAuthentication().getEncode().getEncodeType();
    }

    public int getEncodeLevel() {
        return dustMsProperties.getSecurity().getAuthentication().getEncode().getEncodeLevel();
    }
}
