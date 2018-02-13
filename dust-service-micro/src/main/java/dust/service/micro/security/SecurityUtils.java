package dust.service.micro.security;

import com.alibaba.fastjson.JSONObject;
import dust.service.micro.security.jwt.JWTFilter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;
import java.util.Collection;

/**
 * 安全信息公共类，用于业务获取特定的信息
 * @author huangshengtao
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * 获取当前请求的用户
     *
     * @return 默认是userId, 可通过{@link JWTFilter}来控制内容
     */
    public static String getCurrentUser() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        String userName = null;
        if (authentication != null) {
            if (authentication.getPrincipal() instanceof UserDetails) {
                UserDetails springSecurityUser = (UserDetails) authentication.getPrincipal();
                userName = springSecurityUser.getUsername();
            } else if (authentication.getPrincipal() instanceof String) {
                userName = (String) authentication.getPrincipal();
            } else if (authentication.getPrincipal() instanceof Principal) {
                userName = ((Principal) authentication.getPrincipal()).getName();
            }
        }
        return userName;
    }

    /**
     * 检查是否认证
     *
     * @return true 已认证； false 未认证
     */
    public static boolean isAuthenticated() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        if (authentication != null) {
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            if (authorities != null) {
                for (GrantedAuthority authority : authorities) {
                    if (authority.getAuthority().equals(AuthoritiesConstants.ANONYMOUS)) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * 当前用户是否具有authority角色
     *
     *
     * @param authority the authority to check
     * @return true 具有相应的角色，否则 false
     */
    public static boolean isCurrentUserInRole(String authority) {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        if (authentication != null) {
            if (authentication.getPrincipal() instanceof UserDetails) {
                UserDetails springSecurityUser = (UserDetails) authentication.getPrincipal();
                return springSecurityUser.getAuthorities().contains(new SimpleGrantedAuthority(authority));
            }

            if (authentication instanceof DustAuthentication) {
                DustAuthentication author = (DustAuthentication) authentication;
                return author.getOrgs().contains(authority);

            }
        }
        return false;
    }

    /**
     * JWT以及业务接口组成的登录用户信息
     * NOTE： 只有使用{@link DustAuthentication}时，才会返回信息
     * @return json格式的内容
     */
    public static JSONObject getUserInfo() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        if (authentication != null && authentication instanceof DustAuthentication) {
            return ((DustAuthentication) authentication).getUserInfo();
        }
        return null;
    }
}
