package dust.service.core.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016-6-13.
 */
public class ClassBuildUtils {

    static Logger logger = LoggerFactory.getLogger(ClassBuildUtils.class);

    public static Object copyClass(Object src) {
        Class cls = src.getClass();
        Object obj = null;
        try {
            obj = cls.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return obj;
        }
        Field[] fields = cls.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            field.setAccessible(true);
            try {
                field.set(obj, field.get(src));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return obj;
    }

    public static <T> T mapToObject(Map<String, Object> params, Class<T> type) {
        T obj = null;
        try {
            obj = type.newInstance();
        } catch (InstantiationException e) {
            logger.error("无法创建"+ type.toString(), e);
            return null;
        } catch (IllegalAccessException e) {
            logger.error("无法创建"+ type.toString(), e);
            return null;
        }
        return mapToObject(params, obj, type);
    }

    /**
     * 按照类T开放的set方法，进行赋值
     * 将map的key不区分大小写，不区分下划线，且支持子对象的赋值，暂未支持子属性是List的情况
     *
     * @param params
     * @param obj
     * @param type
     * @return
     */
    public static <T> T mapToObject(Map<String, Object> params, T obj, Class<T> type) {

        //获取所有的set方法
        Method[] m = type.getMethods();
        Map<String, Method> setMethods = new HashMap<>();
        for (Method temp : m) {
            if (temp.getName().startsWith("set")) {
                String fieldName = temp.getName().substring(3);
                setMethods.put(CamelNameUtils.camel2underscore(fieldName), temp);
                if (fieldName.indexOf("_") < 0) {
                    setMethods.put(CamelNameUtils.toLowerCaseFirstOne(fieldName), temp);
                }
            }
        }

        for (String key : params.keySet()) {
            int doIndex = key.indexOf(".");
            if (doIndex < 0 && setMethods.containsKey(key)) {
                try {
                    setMethods.get(key).invoke(obj, typeConvert(params.get(key), setMethods.get(key).getParameterTypes()[0]));
                } catch (IllegalAccessException e) {

                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            } else if (doIndex >= 0 && setMethods.containsKey(key.substring(0, doIndex))) {
                setSubValue(obj, key, params.get(key));
            }
        }

        return obj;
    }

    private static Object setSubValue(Object obj, String path, Object value) {
        try {
            int index = path.indexOf(".");
            if (index < 0) {
                Method setMethod = findMethod(obj.getClass(), "set" + path, true);
                if (setMethod != null) {
                    setMethod.invoke(obj, value);
                }
            } else {
                String fieldName = path.substring(0, index);
                Method getMethod = findMethod(obj.getClass(), "get" + fieldName);
                Object o = null;
                if (getMethod != null) {
                    o = getMethod.invoke(obj);
                }

                if (o == null) {
                    Method setMethod = findMethod(obj.getClass(), "set" + fieldName, true);
                    if (setMethod != null) {
                        Object newObj = typeConvert(value, setMethod.getParameterTypes()[0]);
                        setSubValue(newObj, path.substring(index + 1), value);
                        setMethod.invoke(obj, newObj);
                    }
                } else {
                    Object newObj = o;
                    setSubValue(newObj, path.substring(index + 1), value);
                }

            }
        } catch (IllegalAccessException e) {
            logger.error(path + "属性赋值失败", e);
        } catch (InvocationTargetException e) {
            logger.error(path + "属性赋值失败", e);
        }
        return obj;
    }


    private static Method findMethod(Class clz, String lowerName, boolean ignoreObjectMethod) {
        for (Method m : clz.getMethods()) {
            if (StringUtils.equalsIgnoreCase(lowerName, m.getName())) {
                if (ignoreObjectMethod) {
                    Class<?>[] pts = m.getParameterTypes();
                    for (Class c : pts) {
                        if (!c.getSimpleName().equals("Object")) {
                            return m;
                        }
                    }
                } else {
                    return m;
                }
            }
        }

        return null;
    }

    private static Method findMethod(Class clz, String lowerName) {
        return findMethod(clz, lowerName, false);
    }



    public static Object typeConvert(Object obj, Class clazz) {

        switch (clazz.getSimpleName()) {
            case "Integer":
            case "int":
                return Converter.toInteger(obj);
            case "String":
                return Converter.toString(obj);

            case "Boolean":
            case "boolean":
                return Converter.toBoolean(obj);

            case "Long":
            case "long":
                return Converter.toLong(obj);

            case "Short":
            case "short":
                return Converter.toInteger(obj).shortValue();

            case "Double":
            case "double":
                return Converter.toDouble(obj);

            case "BigDecimal":
                return Converter.toBigDecimal(obj);

            case "Float":
            case "float":
                return Converter.toDouble(obj).floatValue();
            case "Date":
                return Converter.toDate(obj);
            case "Object":
                return null;

        }

        try {
            return clazz.newInstance();
        } catch (Exception ex) {
            return null;
        }

    }


    public static Boolean toBoolean(String source) {
        if (source.equals("true") || source.equals("1")) {
            return true;
        }
        if (source.equals("false") || source.equals("0")) {
            return false;
        } else {
            return false;
        }
    }


}


