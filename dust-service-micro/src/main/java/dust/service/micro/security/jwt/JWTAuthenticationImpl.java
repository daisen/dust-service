package dust.service.micro.security.jwt;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import dust.service.core.util.Converter;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author huangshengtao on 2017-9-14.
 */
public class JWTAuthenticationImpl implements IAuthentication {
    private final Logger logger = LoggerFactory.getLogger(JWTAuthenticationImpl.class);

    private static final String AUTHORITIES_KEY = "auth";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String AUTHORIZATION_PARAMETER = "token";

    private EncodeProvider encodePrivider;
    private DustMsProperties dustMsProperties;

    public JWTAuthenticationImpl(DustMsProperties dustMsProperties, EncodeProvider encodeProvider) {
        this.dustMsProperties = dustMsProperties;
        this.encodePrivider = encodeProvider;
    }

    @Override
    public boolean validateRequest(HttpServletRequest request) {
        if (!isEnable()) {
            return true;
        }

        try {
            Authentication authentication = resolveToken(request);
            if (authentication != null) {
                if (authentication instanceof DustAuthentication) {
                    SysParam sysParam = ((DustAuthentication) authentication).getSysParam();
                    if (sysParam == null) {
                        ((DustAuthentication) authentication).setSysParam(this.resolveSysParam(request));
                    }
                }
                SecurityContextHolder.getContext().setAuthentication(authentication);
                return true;
            }
            return false;
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
        String authorities = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        Date validity;
        if (rememberMe) {
            validity = new Date(now + this.getTokenValidityInSecondsForRememberMe());
        } else {
            validity = new Date(now + this.getTokenValidityInSeconds());
        }

        return Jwts.builder()
                .setSubject(authentication.getName())
                .claim(AUTHORITIES_KEY, authorities)
                .signWith(SignatureAlgorithm.HS512, getSecretKey())
                .setExpiration(validity)
                .compact();
    }

    /**
     * JSON转化为DustAuthentication
     * @param token
     * @return
     */
    public DustAuthentication Json2Authentication(String token) throws JWTException {
        try {
            JSONObject userInfo = JSON.parseObject(token);
            return new DustAuthentication(userInfo);
        } catch (JSONException ex) {
            throw new JWTException("token不满足JSON格式", ex);
        }
    }

    /**
     * 普通token转化为UserPasswordAuthentication
     * @param token
     * @return
     */
    private Authentication getUserPasswordAuthentication(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(getSecretKey())
                .parseClaimsJws(token)
                .getBody();
        Collection<? extends GrantedAuthority> authorities =
                Arrays.asList(claims.get(AUTHORITIES_KEY).toString().split(",")).stream()
                        .map(authority -> new SimpleGrantedAuthority(authority))
                        .collect(Collectors.toList());

        User principal = new User(claims.getSubject(), "",
                authorities);

        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    /**
     * token转化为Authorization对象
     * @param token
     */
    public DustAuthentication bearer2Authentication(String token) throws JWTException {
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
            return new DustAuthentication(userInfo);
        } else {
            throw new JWTException("没有找到可解析的token信息");
        }
    }

    private Authentication resolveToken(HttpServletRequest request) throws JWTException {
        String token = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.isEmpty(token)) {
            token = request.getParameter(AUTHORIZATION_PARAMETER);
        }

        if (StringUtils.isNotEmpty(token)) {
            //BearToken，标准JWT模式
            if (token.startsWith("Bearer ")) {
                return bearer2Authentication(token);

            }

            //JSON明文传输
            if (token.startsWith("{") && token.endsWith("}")) {
                return Json2Authentication(token);
            }

            return getUserPasswordAuthentication(token);
        }

        return null;
    }

    private SysParam resolveSysParam(HttpServletRequest req) {
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
