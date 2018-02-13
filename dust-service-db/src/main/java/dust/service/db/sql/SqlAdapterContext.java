package dust.service.db.sql;

import java.util.ArrayList;
import java.util.List;

/**
 * @author huangshengtao on 2017-7-17.
 */
public class SqlAdapterContext {
    private List<ISqlAdapter> adapterList = new ArrayList<>();

    public List<ISqlAdapter> getAdapterList() {
        return adapterList;
    }
}
