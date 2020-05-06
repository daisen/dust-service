package dust.service.micro.aop.web;

import com.alibaba.fastjson.JSONObject;
import dust.db.dict.*;
import dust.db.sql.*;
import dust.service.micro.common.BuzException;
import dust.service.micro.common.DustMsException;
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

import java.sql.SQLException;
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
        Object data = null;
        String status = "500";
        String error = null;
        String message = null;
        String exception = null;

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
                            data = dataObj.toPageDataJSON();
                        } else {
                            data = dataObj.toDataJSON();
                        }
                        break;
                    case "DataObjRow":
                        data = ((DataObjRow) result).toJSON();
                        break;
                    case "DataTable":
                        data = ((DataTable) result).toDataJSON();
                        break;
                    case "DataRow":
                        data = ((DataRow) result).toJSON();
                        break;
                    default:
                        data = result;

                }
            }

            status = "200";
        } catch (BuzException ex) {
            status = "6001";
            error = "业务错误";
            message = ex.getMessage();
            exception = ex.toString();

        } catch (SQLException ex) {
            status = "6100";
            error = "数据库错误";
            message = ex.getMessage();
            exception = ex.toString();
        } catch (DustMsException ex) {
            status = "6200";
            error = "Dust组件错误";
            message = ex.getMessage();
            exception = ex.toString();
        } catch (IllegalArgumentException e) {
            String str = String.format("无效参数: %s in %s.%s()", Arrays.toString(joinPoint.getArgs()),
                    joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
            if (!env.acceptsProfiles(Constants.SPRING_PROFILE_DEVELOPMENT)) {
                dbLogUtil.write(str, e);
            }
            logger.error(str);

            status = "5001";
            error = "参数不符合要求";
            message = e.getMessage();
            exception = e.toString();
        } catch (Exception ex) {
            String str = String.format("服务器内部错误: %s in %s.%s()", Arrays.toString(joinPoint.getArgs()),
                    joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
            if (!env.acceptsProfiles(Constants.SPRING_PROFILE_DEVELOPMENT)) {
                dbLogUtil.write(str, ex);
            }

            logger.error(str, ex);

            status = "5000";
            error = "服务器内部错误";
            message = ex.getMessage();
            exception = ex.toString();

        }

        jsonResult.put("data", data);
        jsonResult.put("status", status);
        jsonResult.put("error", error);
        jsonResult.put("message", message);
        jsonResult.put("exception", exception);
        return jsonResult;
    }


}
