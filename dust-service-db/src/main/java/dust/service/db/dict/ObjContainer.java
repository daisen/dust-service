package dust.service.db.dict;

import com.google.common.collect.Maps;

import java.util.HashMap;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author huangshengtao on 2017-12-28.
 */
public class ObjContainer {
    private static ThreadLocal<ObjContainer> local = new ThreadLocal<>();

    HashMap<Long, Object> mapObj = Maps.newHashMap();



    public static <T> T find(long id) {
        return (T)local.get().mapObj.get(id);
    }

    public static <T> void put(long id, T obj) {
        checkNotNull(obj);

        ObjContainer localContainer = local.get();
        if (localContainer == null) {
            localContainer = new ObjContainer();
            local.set(new ObjContainer());
        }

        localContainer.mapObj.put(id, obj);
    }

    public static void destroy() {
        local.remove();
    }
}
