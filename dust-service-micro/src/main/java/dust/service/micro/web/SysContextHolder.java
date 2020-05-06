package dust.service.micro.web;


import dust.commons.thread.LocalHolder;

/**
 * 系统参数信息的容器
 */
@Deprecated
public class SysContextHolder {
    private static final String LOCAL = "SysContextHolder.local";

    public static Object getContext() {
        return LocalHolder.get(LOCAL).get();
    }

    public static void setContext(Object var1) {
        LocalHolder.get(LOCAL).set(var1);
    }

    public static Object createEmptyContext() {
        //TODO
        return null;
    }
}
