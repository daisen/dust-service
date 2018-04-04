package dust.service.db.dict;

/**
 * @author huangshengtao on 2018-1-23.
 */
public interface IDataObjContainer {

    DataObj create(String app, String module, String alias);

    DataObj create(Long id);
}
