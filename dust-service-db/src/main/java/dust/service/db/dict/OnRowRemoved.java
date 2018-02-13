package dust.service.db.dict;

/**
 * @author huangshengtao on 2017-12-18.
 */
public interface OnRowRemoved {
    void rowRemoved(DataObj dataObj, DataObjRow row);
}
