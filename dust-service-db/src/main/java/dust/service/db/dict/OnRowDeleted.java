package dust.service.db.dict;

/**
 * @author huangshengtao on 2017-12-18.
 */
public interface OnRowDeleted {
    void rowDeleted(DataObj dataObj, DataObjRow row);
}
