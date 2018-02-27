package dust.service.db.dict;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dust.service.core.util.Converter;
import dust.service.core.util.SnowFlakeIdWorker;
import org.apache.commons.lang3.StringUtils;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 数据单元，一行一列确定一个数据集的位置，位置对应一个数据单元
 *
 * @author huangshengtao on 2017-12-18.
 */
public class DataObjField {

    private long id;
    private Object originValue;
    private Object value;
    private boolean modified = false;
    private long rowId;
    private long columnId;

    @JsonIgnore
    private DataObjRow row;
    @JsonIgnore
    private DataObjColumn column;

    public DataObjField(DataObjRow row, DataObjColumn col) {
        checkNotNull(row);
        checkNotNull(col);

        this.row = row;
        this.column = col;
        this.rowId = col.getId();
        this.columnId = row.getId();

        init();
    }

    public DataObjField(long row, long col) {
        checkNotNull(row);
        checkNotNull(col);

        this.rowId = row;
        this.columnId = col;

        init();
    }


    private void init() {
        if (DictGlobalConfig.isEnableObjId()) {
            if (row == null || this.column == null) {
                this.row = ObjContainer.find(rowId);
                this.column = ObjContainer.find(columnId);
                checkNotNull(this.row);
                checkNotNull(this.column);
            }

            this.id = SnowFlakeIdWorker.getInstance0().nextId();
        }

        GenerateDefaultValue();
    }

    private void GenerateDefaultValue() {
        String defaultValue = this.column.getDefaultValue();
        if (!StringUtils.isEmpty(defaultValue)) {
            setValue(defaultValue);
        }
    }

    public Object getOriginValue() {
        return originValue;
    }

    public Object getValue() {
        return value;
    }

    public String getString() {
        return Converter.toString(this.value);
    }

    public void setValue(Object value) {
        Object colVal = this.column.toValue(value);

        // 1. 都为null，未改变
        // 2. 有一个为null
        if (colVal == null) {
            if (originValue != null) {
                changeState();
            }
        } else if (!value.equals(originValue)) {
            changeState();
        }

        this.value = value;
    }

    private void changeState() {
        modified = true;
        if (this.row.getRowState() == RowState.UNCHANGED) {
            this.row.setRowState(RowState.MODIFIED);
        }
    }

    protected void loadValue(Object value) {
        Object colVal = this.column.toValue(value);
        this.value = colVal;
        this.originValue = colVal;
    }

    public long getRowId() {
        return rowId;
    }

    public long getColumnId() {
        return columnId;
    }

    public DataObjRow getRow() {
        return row;
    }

    public DataObjColumn getColumn() {
        return column;
    }

    public DataObjField copyTo(DataObjField newField) {
        newField.value = this.value;
        newField.originValue = this.value;
        return newField;
    }

    public boolean isModified() {
        return modified;
    }

    public long getId() {
        return id;
    }

    public void rejectChanges() {
        this.modified = false;
        this.value = this.originValue;
    }

    public void acceptChanges() {
        this.modified = false;
        this.originValue = this.value;
    }

    @Override
    public String toString() {
        return "value:" + value + " orgin:" + originValue + " columnId:" + columnId;
    }
}
