package dust.service.micro.security.jwt;

import com.alibaba.fastjson.JSONObject;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;

/**
 * 第三方认证操作接口
 *
 * @author huangshengtao
 */
public interface IAuthentication {

    boolean validateRequest(HttpServletRequest request);

    String createToken(Authentication authentication, Boolean rememberMe);
}
