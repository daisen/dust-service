package dust.service.micro.security.jwt;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import dust.commons.util.Converter;
import dust.service.micro.config.DustMsProperties;
import dust.service.micro.security.DustAuthentication;
import dust.service.micro.security.SysParam;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author huangshengtao on 2017-9-14.
 */
public class JWTAuthenticationImpl implements IAuthentication {
    private final Logger logger = LoggerFactory.getLogger(JWTAuthenticationImpl.class);

    public static final String AUTHORITIES_KEY = "auth";
    public static final String AUTHORIZATION_HEADER = "X-TOKEN";
    public static final String AUTHORIZATION_PARAMETER = "token";

    protected DustMsProperties dustMsProperties;

    public JWTAuthenticationImpl(DustMsProperties dustMsProperties) {
        this.dustMsProperties = dustMsProperties;
    }

    @Override
    public boolean validateRequest(HttpServletRequest request) {
        if (!isEnable()) {
            return true;
        }

        return this.validateRequestByDust(request);
    }

    public boolean validateRequestByDust(HttpServletRequest request) {
        try {
            DustAuthentication auth = null;
            String token = resolveToken(request);
            if (StringUtils.isEmpty(token)) {
                return false;
            }

            //BearerToken，标准JWT模式
            if (token.startsWith("Bearer ")) {
                auth = bearer2Authentication(token);
            }

            //JSON明文传输
            if (token.startsWith("{") && token.endsWith("}")) {
                auth =  Json2Authentication(token);
            }

            if (auth == null) {
                debuggerLog("无效的token，无法转化为DustAuthentication");
                return false;
            }

            SecurityContextHolder.getContext().setAuthentication(auth);
            SysParam sysParam = auth.getSysParam();
            if (sysParam == null) {
                auth.setSysParam(this.resolveSysParam(request));
            }
            return true;


        } catch (SignatureException e) {
            debuggerLog("无效的token, 无法parse为可识别的信息");
            return false;
        } catch (JWTException je) {
            debuggerLog("jsonwebtoken第三方认证失败");
            return false;
        }
    }


    @Override
    public String createToken(Authentication authentication, Boolean rememberMe) {
        long now = (new Date()).getTime();
        Date validity;
        if (rememberMe) {
            validity = new Date(now + this.getTokenValidityInSecondsForRememberMe());
        } else {
            validity = new Date(now + this.getTokenValidityInSeconds());
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", authentication.getName());
        if (authentication.getDetails() instanceof Map) {
            claims = (Map<String, Object>) authentication.getDetails();
        }

        return Jwts.builder()
                .setSubject(authentication.getName())
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS512, getSecretKey())
                .setExpiration(validity)
                .compact();
    }

    /**
     * JSON转化为DustAuthentication
     * @param token
     * @return
     */
    protected DustAuthentication Json2Authentication(String token) throws JWTException {
        try {
            JSONObject userInfo = JSON.parseObject(token);
            return new DustAuthentication(userInfo.getString(getUserKey()), userInfo);
        } catch (JSONException ex) {
            throw new JWTException("token不满足JSON格式", ex);
        }
    }

    protected String getUserKey() {
        return "userId";
    }


    /**
     * token转化为Authorization对象
     * @param token
     */
    protected DustAuthentication bearer2Authentication(String token) throws JWTException {
        String authToken = token.substring(7, token.length());
        Claims claims = Jwts.parser()
                .setSigningKey(getSecretKey())
                .parseClaimsJws(authToken)
                .getBody();
        JSONObject userInfo = new JSONObject(claims);

        if (logger.isDebugEnabled()) {
            logger.debug("JWT.Claims:" + userInfo);
        }

        if (userInfo != null) {
            return new DustAuthentication(userInfo.getString(getUserKey()), userInfo);
        } else {
            throw new JWTException("没有找到可解析的token信息");
        }
    }

    protected String resolveToken(HttpServletRequest request) throws JWTException {
        String token = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.isEmpty(token)) {
            token = request.getParameter(AUTHORIZATION_PARAMETER);
        }
        return token;
    }

    protected SysParam resolveSysParam(HttpServletRequest req) {
        SysParam sysParam = new SysParam();
        Map<String, String[]> maps = req.getParameterMap();
        maps.forEach((s, strings) -> {
            if (StringUtils.equalsIgnoreCase(s, SysParam.APP_ID)) {
                sysParam.setAppId(strings != null && strings.length > 0 ? strings[0] : "");
            }

            if (StringUtils.equalsIgnoreCase(s, SysParam.TENANT_ID)) {
                sysParam.setTenantId(strings != null && strings.length > 0 ? strings[0] : "");
            }

            if (StringUtils.equalsIgnoreCase(s, SysParam.TIMESTAMP)) {
                sysParam.setTimestamp(Converter.toLong(strings != null && strings.length > 0 ? strings[0] : ""));
            }
        });
        return sysParam;
    }

    public boolean isEnable() {
        return dustMsProperties.getSecurity().getAuthentication().getJwt().isEnable();
    }

    public String getSecretKey() {
        return dustMsProperties.getSecurity().getAuthentication().getJwt().getSecret();
    }

    public long getTokenValidityInSeconds() {
        return 1000 * dustMsProperties.getSecurity().getAuthentication().getJwt().getTokenValidityInSeconds();
    }

    public long getTokenValidityInSecondsForRememberMe() {
        return 1000 * dustMsProperties.getSecurity().getAuthentication().getJwt().getTokenValidityInSecondsForRememberMe();
    }

    private void debuggerLog(String msg) {
        if (logger.isDebugEnabled()) {
            logger.debug(msg);
        }
    }
}
