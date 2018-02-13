package dust.service.db.dict;

/**
 * @author huangshengtao on 2017-12-18.
 */
public interface OnCurrentChanged {
    void currentChanged(DataObj dataObj, int oldIndex, int newIndex);
}
