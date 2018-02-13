package dust.service.core.util;

/**
 * Created by huangshengtao on 2016-9-2.
 */
public class CamelNameUtils {

    /**
     * convert camel name to underscore name
     *
     * @return
     */
    public static String camel2underscore(String camelName) {
        //先把第一个字母大写
        camelName = capitalize(camelName);

        String regex = "([A-Z][a-z]+)";
        String replacement = "$1_";

        String underscoreName = camelName.replaceAll(regex, replacement);
        //output: Pur_Order_Id_ 接下来把最后一个_去掉，然后全部改小写

        underscoreName = underscoreName.toLowerCase().substring(0, underscoreName.length() - 1);

        return underscoreName;
    }

    /**
     * convert camel name to upper underscore name
     *
     * @return
     */
    public static String camel2underscoreU(String camelName) {
        //先把第一个字母大写
        camelName = capitalize(camelName);

        String regex = "([A-Z][a-z]+)";
        String replacement = "$1_";

        String underscoreName = camelName.replaceAll(regex, replacement);

        //output: Pur_Order_Id_ 接下来把最后一个_去掉，然后全部改大写
        underscoreName = underscoreName.toUpperCase().substring(0, underscoreName.length() - 1);

        return underscoreName;
    }

    /**
     * convert underscore name to camel name
     *
     * @param underscoreName
     * @return
     */
    public static String underscore2camel(String underscoreName) {
        String[] sections = underscoreName.split("_");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sections.length; i++) {
            String s = sections[i];
            if (i == 0) {
                sb.append(s);
            } else {
                sb.append(capitalize(s));
            }
        }
        return sb.toString();
    }

    /**
     * capitalize the first character
     *
     * @param str
     * @return
     */
    public static String capitalize(String str) {
        if (str == null || str.length() == 0) {
            return str;
        }
        if (str.length() == 1) {
            return str.toLowerCase();
        }
        return String.valueOf(Character.toTitleCase(str.charAt(0))) +
                str.substring(1);
    }


    //首字母转小写
    public static String toLowerCaseFirstOne(String s){
        if(Character.isLowerCase(s.charAt(0)))
            return s;
        else
            return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();
    }
    //首字母转大写
    public static String toUpperCaseFirstOne(String s){
        if(Character.isUpperCase(s.charAt(0)))
            return s;
        else
            return (new StringBuilder()).append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).toString();
    }


    //首字母转大写
    public static String toOtherCaseFirstOne(String s){
        if(Character.isUpperCase(s.charAt(0)))
            return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();
        else
            return (new StringBuilder()).append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).toString();
    }
}