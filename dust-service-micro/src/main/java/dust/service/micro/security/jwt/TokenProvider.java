package dust.service.micro.security.jwt;

import dust.service.micro.config.DustMsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

/**
 * Token处理类
 * 使用jjwt包来进行token相关的转码操作
 *
 * @author huangshengtao
 */
@Component
public class TokenProvider {

    private final Logger logger = LoggerFactory.getLogger(TokenProvider.class);

    @Autowired
    private DustMsProperties dustMsProperties;

    @Autowired(required = false)
    private IAuthentication authenticationImpl;

    @Autowired
    EncodeProvider encodeProvider;

    private IAuthentication jwtAuthenticationImpl;

    @PostConstruct
    public void init() {
        jwtAuthenticationImpl = new JWTAuthenticationImpl(dustMsProperties, encodeProvider);
    }

    public String createJwtToken(Authentication authentication, Boolean rememberMe) {
        if (authenticationImpl != null) {
            return authenticationImpl.createToken(authentication, rememberMe);
        } else {
            return jwtAuthenticationImpl.createToken(authentication, rememberMe);
        }
    }

    public boolean validateRequest(HttpServletRequest request) {
        if (authenticationImpl != null) {
            return authenticationImpl.validateRequest(request);
        } else {
            return jwtAuthenticationImpl.validateRequest(request);
        }
    }

}
