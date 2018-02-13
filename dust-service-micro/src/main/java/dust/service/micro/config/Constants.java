package dust.service.micro.config;

/**
 * 微服务常量类
 * @author huangshengtao
 */
public final class Constants {

    // Spring开发模式的配置文件
    public static final String SPRING_PROFILE_DEVELOPMENT = "dev";
    // Spring生产模式的配置文件
    public static final String SPRING_PROFILE_PRODUCTION = "prod";
    // Spring profile used when deploying with Spring Cloud (used when deploying to CloudFoundry)
    public static final String SPRING_PROFILE_CLOUD = "cloud";

    // SpringSwagger生产模式的配置文件
    public static final String SPRING_PROFILE_SWAGGER = "swagger";

    public static final String SYSTEM_ACCOUNT = "system";

    private Constants() {
    }
}
