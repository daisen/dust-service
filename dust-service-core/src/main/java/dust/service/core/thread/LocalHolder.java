package dust.service.core.thread;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author huangshengtao on 2018-2-28.
 */
public class LocalHolder {
    private final static Map<String, ThreadLocal> threadLocalMap = Maps.newHashMap();

    public static <T> ThreadLocal<T> get(String key) {
        if (key == null) {
            throw new NullPointerException("key");
        }
        if (!threadLocalMap.containsKey(key)) {
            threadLocalMap.put(key, new ThreadLocal());
        }

        return threadLocalMap.get(key);
    }

    public static void remove() {
        for(String key : threadLocalMap.keySet()) {
            threadLocalMap.get(key).remove();
        }
    }
}
