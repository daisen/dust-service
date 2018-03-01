package dust.service.db.dict;

import com.google.common.collect.Maps;
import dust.service.core.thread.LocalHolder;

import java.util.HashMap;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author huangshengtao on 2017-12-28.
 */
public class ObjContainer {
    //    private static ThreadLocal<ObjContainer> local = new ThreadLocal<>();
    public final static String LOCAL = "ObjContainer.local";

    HashMap<Long, Object> mapObj = Maps.newHashMap();



    public static <T> T find(long id) {
        ThreadLocal<ObjContainer> local = LocalHolder.get(LOCAL);
        return (T) local.get().mapObj.get(id);
    }

    public static <T> void put(long id, T obj) {
        checkNotNull(obj);
        ThreadLocal<ObjContainer> local = LocalHolder.get(LOCAL);
        ObjContainer localContainer = local.get();
        if (localContainer == null) {
            localContainer = new ObjContainer();
            local.set(new ObjContainer());
        }

        localContainer.mapObj.put(id, obj);
    }
}
