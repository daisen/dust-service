package dust.service.db.sql;

import dust.service.core.util.Converter;

/**
 * 数据库存储过的参数类，存放存储过程参数的基本信息，被{@link SqlCommand}使用
 * @author huangshengtao
 */
public class StoreProcParam {

    private String paramName;
    private DataTypeEnum paramDataType;
    private int paramSize;
    private ProcParaTypeEnum paramIoType;
    private Object value;

    public StoreProcParam(String name, DataTypeEnum dataType, int size, ProcParaTypeEnum ioType, Object vaule) {
        setParamName(name);
        setParamDataType(dataType);
        setParamSize(size);
        setParamIoType(ioType);
        setValue(vaule);
    }

    public StoreProcParam(String name , int dataType,  int size, int ioType, Object value)
    {
        setParamName(name);
        setParamDataType(Converter.toEnumByValue(DataTypeEnum.class, Converter.toString(dataType)));
        setParamSize(size);
        setParamIoType(Converter.toEnumByValue(ProcParaTypeEnum.class, Converter.toString(ioType)));
        setValue(value);
    }
    public String getParamName() {
        return paramName;
    }

    public StoreProcParam setParamName(String paramName) {
        this.paramName = paramName;
        return this;
    }

    public DataTypeEnum getParamDataType() {
        return paramDataType;
    }

    public StoreProcParam setParamDataType(DataTypeEnum paramDataType) {
        this.paramDataType = paramDataType;
        return this;
    }

    public int getParamSize() {
        return paramSize;
    }

    public StoreProcParam setParamSize(int paramSize) {
        this.paramSize = paramSize;
        return this;
    }

    public ProcParaTypeEnum getParamIoType() {
        return paramIoType;
    }

    public StoreProcParam setParamIoType(ProcParaTypeEnum paramIoType) {
        this.paramIoType = paramIoType;
        return this;
    }

    public Object getValue() {
        return value;
    }

    public StoreProcParam setValue(Object value) {
        this.value = value;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\n\tparamName:\"");
        sb.append(this.getParamName());
        sb.append("\"");

        sb.append("\n\tparamDataType:\"");
        sb.append(this.getParamDataType());
        sb.append("\"");

        sb.append("\n\tparamSize:");
        sb.append(this.getParamSize());
        sb.append("");

        sb.append("\n\tvalue:\"");
        sb.append(this.getValue());
        sb.append("\"");

        sb.append("\n\tparamIoType:\"");
        sb.append(this.getParamIoType());
        sb.append("\"");

        sb.append("\n\t}");
        return sb.toString();
    }

}
