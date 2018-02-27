package dust.service.db.dict;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import dust.service.db.sql.DataRow;
import dust.service.core.util.SnowFlakeIdWorker;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author huangshengtao on 2017-12-18.
 */
public class DataObjRow {
    private long id;
    private long objId;
    private long index = -1;
    private long originIndex = -1;
    private RowState rowState = RowState.DETACHED;
    private final List<DataObjField> fieldList = Lists.newArrayList();

    @JsonIgnore
    private DataObj dataObj;

    public DataObjRow(long objId) {
        this.objId = objId;
        init();
    }

    public DataObjRow(DataObj obj) {
        this.dataObj = obj;
        init();
    }

    private void init() {
        if (DictGlobalConfig.isEnableObjId()) {
            id = SnowFlakeIdWorker.getInstance0().nextId();
            if (dataObj == null && objId != 0) {
                dataObj = ObjContainer.find(objId);
            }
        }

        if (dataObj == null) {
            throw new NullPointerException("dataObj not allow null");
        }

        for (int i = 0; i < dataObj.getColumnSize(); i++) {
            DataObjColumn col = dataObj.getColumn(i);
            fieldList.add(new DataObjField(this, col));
        }
    }

    public long getId() {
        return id;
    }

    public String getString(String columnLabel) {
        DataObjField field = getField(columnLabel);
        return field.getString();
    }

    public long getIndex() {
        return index;
    }

    protected void setIndex(long index) {
        this.index = index;
    }

    public DataObjField getField(int index) {
        if (index >= this.fieldList.size()) {
            throw new IndexOutOfBoundsException("index out of bound");
        }
        return this.fieldList.get(index);
    }

    public DataObjField getField(String columnLabel) {
        checkNotNull(columnLabel);
        Integer idx = this.dataObj.findIdxByLabel(columnLabel);
        return getField(idx);
    }

    public DataObjField getField(DataObjColumn dc) {
        checkNotNull(dc);

        String columnLabel = dc.getColumnLabel();
        return getField(columnLabel);
    }

    public void remove() {
        this.dataObj.removeRow(this);
    }

    public void delete() {
        dataObj.deleteRow(this);
    }

    public Object toJSON() {
        JSONObject o = new JSONObject();
        for(DataObjField field : fieldList) {
            o.put(field.getColumn().getColumnLabel(), field.getValue());
        }
        return o;
    }

    public RowState getRowState() {
        return rowState;
    }

    protected void setRowState(RowState rowState) {
        this.rowState = rowState;
    }

    public void rejectChanges() {
        if (rowState != RowState.MODIFIED && rowState != RowState.UNCHANGED) {
            throw new IllegalArgumentException("rejectChanges only can be invoked when row state is modified or unchanged");
        }

        for(DataObjField field : fieldList) {
            field.rejectChanges();
        }
        this.rowState = RowState.UNCHANGED;
    }

    public void acceptChanges() {
        if (rowState != RowState.MODIFIED && rowState != RowState.ADDED) {
            throw new IllegalArgumentException("acceptChanges only can be invoked when row state is modified or added");
        }

        for(DataObjField field : fieldList) {
            field.acceptChanges();
        }

        this.rowState = RowState.UNCHANGED;
    }

    public List<DataObjField> getChanges() {
        if (rowState != RowState.MODIFIED && rowState != RowState.UNCHANGED) {
            throw new IllegalArgumentException("getChanges only can be invoked when row state is modified or unchanged");
        }

        List<DataObjField> arr = Lists.newArrayList();
        for (DataObjField field : fieldList) {
            if (field.isModified()) {
                arr.add(field);
            }
        }

        return arr;
    }

    public DataObj getDataObj() {
        return dataObj;
    }

    protected void setDataObj(DataObj dataObj) {
        this.dataObj = dataObj;
    }


    public Object getValue(String columnName) {
        return getField(columnName).getValue();
    }

    public Object getValue(int index) {
        return getField(index).getValue();
    }

    public Object getValue(DataObjColumn dc) {
        return getField(dc).getValue();
    }

    public DataObjRow setValue(int index, Object val) {
        getField(index).setValue(val);
        return this;
    }

    public DataObjRow setValue(String columnLabel, Object val) {
        getField(columnLabel).setValue(val);
        return this;
    }

    public DataObjRow setValue(DataObjColumn dc, Object val) {
        getField(dc).setValue(val);
        return this;
    }

    public Object getOriginValue(int index) {
        return getField(index).getOriginValue();
    }

    public Object getOriginValue(String columnLabel) {
        return getField(columnLabel).getOriginValue();
    }

    public Object getOriginValue(DataObjColumn dc) {
        return getField(dc).getOriginValue();
    }

    public void destroy() {
        this.dataObj = null;
        this.fieldList.clear();
    }

    public void loadData(DataRow row) {
        int len = fieldList.size();
        for(int i = 0; i < len; i++) {
            DataObjField field = fieldList.get(i);
            if (field.getColumn().isIgnore()) {
                if (StringUtils.isNotEmpty(field.getColumn().getMirrorColumnLabel())) {
                    field.loadValue(row.get(field.getColumn().getMirrorColumnLabel()));
                }
            } else {
                field.loadValue(row.get(field.getColumn().getColumnLabel()));
            }
        }
    }
}
