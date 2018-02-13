package dust.service.micro.config;

import dust.service.micro.web.filter.HttpHeadersFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;


/**
 * 配置Web应用所需的API信息
 * @author huangshengtao
 */
@Configuration
//@EnableWebMvc
public class WebConfiguration extends WebMvcConfigurerAdapter
        implements ServletContextInitializer, EmbeddedServletContainerCustomizer {

    private final Logger log = LoggerFactory.getLogger(WebConfiguration.class);

    @Autowired
    DustMsProperties dustMsProperties;

    @Override
    public void customize(ConfigurableEmbeddedServletContainer configurableEmbeddedServletContainer) {

    }

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {

    }

    @Bean
    public GenericFilterBean sysParamFilter() {
        return new HttpHeadersFilter();
    }

    @Bean
    @ConditionalOnProperty(name = "dust.ms.cors.allowed-origins")
    public CorsFilter corsFilter() {
        log.debug("Registering CORS filter");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = dustMsProperties.getCors();
        source.registerCorsConfiguration("/api/**", config);
        source.registerCorsConfiguration("/v2/api-docs", config);
        source.registerCorsConfiguration("/oauth/**", config);
        String[] matchers = dustMsProperties.getCorsMatchers();
        if (matchers != null) {
            for (int i = 0; i < matchers.length; i ++) {
                source.registerCorsConfiguration(matchers[i], config);
            }
        }
        return new CorsFilter(source);
    }
}
