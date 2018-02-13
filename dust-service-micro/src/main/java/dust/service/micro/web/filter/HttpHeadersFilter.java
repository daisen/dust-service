package dust.service.micro.web.filter;

import dust.service.core.util.BeanUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.*;
import java.io.IOException;

/**
 * 请求头的修改过滤器
 * 暂时不启用，后续根据需求在加入
 * 系统参数的处理已经转移到security模块
 * @author huangshengtao
 */
public class HttpHeadersFilter extends GenericFilterBean {


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (BeanUtils.context != null) {
            Object filter = BeanUtils.getBean("dustCustomFilter");
            if (filter != null && filter instanceof GenericFilterBean) {
                ((GenericFilterBean) filter).doFilter(servletRequest, servletResponse, filterChain);
                return;
            }

        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }
}
