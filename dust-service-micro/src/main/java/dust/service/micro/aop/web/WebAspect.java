package dust.service.micro.aop.web;

import com.alibaba.fastjson.JSONObject;
import dust.service.db.dict.DataObj;
import dust.service.db.dict.DataObjRow;
import dust.service.micro.config.Constants;
import dust.service.micro.util.DbLogUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

import java.util.Arrays;

/**
 * 切入WebController层，进行返回值处理
 * 切面为DustMapping注解
 * 遇到异常则记录日志
 * <ul>
 * <li>如果不是开发配置，启用了数据库日志，则记录到数据库</li>
 * <li>同时记录到{@link Logger}</li>
 * </ul>
 */
@Aspect
@Order(2)
public class WebAspect {

    final static Logger logger = LoggerFactory.getLogger(WebAspect.class);
    @Autowired
    private Environment env;


    @Autowired
    private DbLogUtil dbLogUtil;

    @Pointcut("@annotation(dust.service.micro.annotation.DustMapping)")

    public void loggingPointcut() {
    }

    @Around("loggingPointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        if (logger.isDebugEnabled()) {
            logger.debug("Enter: {}.{}() with argument[s] = {}", joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(), Arrays.toString(joinPoint.getArgs()));
        }
        JSONObject jsonResult = new JSONObject();
        try {
            Object result = joinPoint.proceed();

            if (logger.isDebugEnabled()) {
                logger.debug("Exit: {}.{}() with result = {}", joinPoint.getSignature().getDeclaringTypeName(),
                        joinPoint.getSignature().getName(), result);
            }

            if (result != null) {
                String clzName = result.getClass().getSimpleName();
                switch (clzName) {
                    case "JSONObject":
                        jsonResult.putAll((JSONObject) result);
                        break;
                    case "DataObj":
                        DataObj dataObj = (DataObj) result;
                        if (dataObj.getPageInfo().getPageSize() > 0) {
                            jsonResult.put("data", dataObj.toPageDataJson());
                        } else {
                            jsonResult.put("data", dataObj.toDataJson());
                        }
                        break;
                    case "DataObjRow":
                        jsonResult.put("data", ((DataObjRow) result).toJSON());
                        break;
                    default:
                        jsonResult.put("data", result);

                }
            } else {
                jsonResult.put("data", null);
            }

            jsonResult.put("status", "200");
            return jsonResult;
        } catch (IllegalArgumentException e) {
            String str = String.format("无效参数: %s in %s.%s()", Arrays.toString(joinPoint.getArgs()),
                    joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
            if (!env.acceptsProfiles(Constants.SPRING_PROFILE_DEVELOPMENT)) {
                dbLogUtil.write(str, e);
            }
            logger.error(str);

            jsonResult.put("status", "510");
            jsonResult.put("error", "参数不符合要求");
            jsonResult.put("message", e.getMessage());
            jsonResult.put("exception", e.toString());
            return jsonResult;
        } catch (Exception ex) {
            String str = String.format("服务器内部错误: %s in %s.%s()", Arrays.toString(joinPoint.getArgs()),
                    joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
            if (!env.acceptsProfiles(Constants.SPRING_PROFILE_DEVELOPMENT)) {
                dbLogUtil.write(str, ex);
            }

            logger.error(str, ex);

            jsonResult.put("status", "511");
            jsonResult.put("error", "服务器内部错误");
            jsonResult.put("message", ex.getMessage());
            jsonResult.put("exception", ex.toString());
            return jsonResult;
        }
    }


}
