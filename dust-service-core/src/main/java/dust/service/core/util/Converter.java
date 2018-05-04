package dust.service.core.util;

import com.alibaba.druid.util.StringUtils;

import java.io.*;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet;

/**
 * Created by huangshengtao on 2016-2-3.
 */
public class Converter {

    public static final String DATATIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DATA_FORMAT = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "HH:mm:ss";

    public static Boolean toBoolean(Object value) {
        if (value instanceof String) {
            String strValue = (String) value;
            return "1".equals(strValue) || "true".equalsIgnoreCase(strValue);
        }

        if (value instanceof Boolean) {
            return (Boolean) value;
        }

        return false;
    }


    public static Date toDate(Object value, String format) {
        if (value == null) {
            return null;
        }
        if (value instanceof Date) {
            return (Date) value;
        }

        if (value instanceof Long) {
            return new Date((Long) value);
        }

        if (value instanceof String && StringUtils.isNumber((String)value)) {
            return new Date(toLong(value));
        }

        // 字符串格式的时间日期
        String strDate = toString(value);
        format = null == format ? "yyyy-MM-dd HH:mm:ss" : format;
        if (format.length() < strDate.length() && format.equals(DATA_FORMAT)) {
            format = DATATIME_FORMAT;
        }

        DateFormat dateFormat = new SimpleDateFormat(format);
        Date date = null;
        try {
            date = dateFormat.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static Date toDateTime(String value) {
        return toDate(value, DATATIME_FORMAT);
    }

    public static java.sql.Date toSqlDataTime(String value) {
        Date date = toDateTime(value);
        if (null != date) {
            return new java.sql.Date(date.getTime());
        }
        return null;
    }

    public static Date toDate(Object value) {
        return toDate(value, DATA_FORMAT);
    }

    public static Double toDouble(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Double) {
            return (Double) value;
        }

        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).doubleValue();
        }

        Double db = null;
        try {
            db = Double.parseDouble(value.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return db;
    }

    public static BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }

        BigDecimal bd = null;
        try {
            bd = new BigDecimal(value.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bd;
    }

    public static Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Integer) {
            return (Integer) value;
        }

        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).intValue();
        }

        Integer i = null;
        try {
            i = Integer.parseInt(value.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return i;
    }

    public static Long toLong(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).longValue();
        }

        if (value instanceof Long) {
            return (Long) value;
        }

        Long i = null;
        try {
            i = Long.parseLong(value.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return i;
    }

    public static String toString(Object o, String... formats) {
        if (o == null) {
            return null;
        }

        if (o instanceof Date) {
            String format = formats == null || formats.length == 0 ? null : formats[0];
            return parseDateTime((Date) o, format);
        }

        //TODO 支持不同类型的转化

        return o.toString();
    }

    public static String parseDate(Date d, String format) {
        if (d == null) {
            return null;
        }

        if (StringUtils.isEmpty(format)) {
            format = DATA_FORMAT;
        }

        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(d);
    }

    public static String parseDate(Date d) {
        return parseDate(d, null);
    }

    public static String parseDateTime(Date d, String format) {
        if (d == null) {
            return null;
        }

        if (StringUtils.isEmpty(format)) {
            format = DATATIME_FORMAT;
        }

        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(d);
    }

    public static String parseDateTime(Date d) {
        return parseDateTime(d, null);
    }

    public static Object toStringBuffer(Object value) {
        if (value == null) {
            return null;
        }

        return new StringBuffer(toString(value));
    }

    public static byte[] toBytes(Object value) {
        if (value == null) {
            return null;
        }

        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(value);
            oos.flush();
            byte[] bytes = bos.toByteArray();
            oos.close();
            bos.close();
            return bytes;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static Object toObject(byte[] bs) {
        if (bs == null) return null;

        Object o = null;
        try {
            ByteArrayInputStream bos = new ByteArrayInputStream(bs);
            ObjectInputStream oos = new ObjectInputStream(bos);
            o = oos.readObject();
            oos.close();
            bos.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return o;
    }

    public static String toStringEmpty(Object o) {
        if (o == null) {
            return "";
        }
        return o.toString();
    }


    /*
     * clazz: 枚举类
	 * index：枚举类的原始序列
	 */
    public static <T> T toEnumByIndex(Class<T> clazz, int index) {
        T[] c = clazz.getEnumConstants();
        return c[index];
    }

    /*
     * clazz: 枚举类
     * Name：枚举类的名称
     */
    public static <T extends Enum<T>> T toEnumByName(Class<T> clazz, Object name) {
        if (null != name) {
            return Enum.valueOf(clazz, name.toString());
        } else {
            return null;
        }
    }

    /*
     * clazz: 枚举类
     * value: 枚举的toString方法的返回值
     */
    public static <T extends Enum<T>> T toEnumByValue(Class<T> clazz, Object value) {
        if (null != value) {
            EnumSet<T> currEnumSet = EnumSet.allOf(clazz);
            for (T dataType : currEnumSet) {
                if (dataType.toString().equals(value.toString())) {
                    return dataType;
                }
            }
        }
        return null;
    }

    /*
     * clazz: 枚举类
     * value: 枚举的toString方法的返回值
     * 针对此种枚举类型 a("01")，其他类型还需要扩展
     */
    public static <T extends Enum<T>> void addEnums(Class<T> clazz, Object value) {
        Object[] objs = new Object[]{value};
        Class<?>[] classs = new Class<?>[]{String.class};
        DynamicEnum.addEnum(clazz, clazz.getSimpleName() + value.toString(), classs, objs);
    }
}
