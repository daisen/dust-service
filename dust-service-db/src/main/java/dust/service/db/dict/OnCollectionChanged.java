package dust.service.db.dict;

/**
 * @author huangshengtao on 2017-12-18.
 */
public interface OnCollectionChanged {
    void collectionChanged(DataObj dataObj, ActionType actionType);

    public enum ActionType {
        ADD, REPLACE, RESET, REMOVE
    }
}
