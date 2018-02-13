package dust.service.micro.web;

/**
 * 系统参数信息的容器
 */
@Deprecated
public class SysContextHolder {
    private static final ThreadLocal threadLocal = new ThreadLocal();

    public static void clearContext() {
        threadLocal.remove();
    }

    public static Object getContext() {
        return threadLocal.get();
    }

    public static void setContext(Object var1) {
        threadLocal.set(var1);
    }

    public static Object createEmptyContext() {
        //TODO
        return null;
    }
}
