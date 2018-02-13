package dust.service.micro.security.jwt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filters incoming requests and installs a Spring Security principal if a header corresponding to a valid user is
 * found.
 */
public class JWTFilter extends GenericFilterBean {
    private final static Logger logger = LoggerFactory.getLogger(JWTFilter.class);

    private TokenProvider tokenProvider;

    private SignProvider signProvider;

    public JWTFilter(TokenProvider tokenProvider, SignProvider signProvider) {
        this.tokenProvider = tokenProvider;
        this.signProvider = signProvider;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;

        //验证数据安全性
        if (!this.signProvider.validateRequest(httpServletRequest)) {
            debuggerLog("数据加密认证失败，返回400错误");
            ((HttpServletResponse) servletResponse).setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        //验证JWT,用户信息
        if (!this.tokenProvider.validateRequest(httpServletRequest)) {
            debuggerLog("授权认证失败，返回401错误");
            ((HttpServletResponse) servletResponse).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    private void debuggerLog(String msg) {
        if (logger.isDebugEnabled()) {
            logger.debug(msg);
        }
    }
}
