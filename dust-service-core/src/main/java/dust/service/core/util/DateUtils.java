package dust.service.core.util;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public final class DateUtils {

    public static String DateFormat = "yyyy-MM-dd";
    public static String DateTimeFormat = "yyyy-MM-dd HH:mm:ss";

    private DateUtils() {
    }

    public static String Date() {
        Date dt = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(DateFormat);
        return sdf.format(dt);
    }

    public static String DateTime() {
        Date dt = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(DateTimeFormat);
        return sdf.format(dt);
    }

    public static String formatDecimal(float orgin, int scale) {
        String formatStr = ".";
        while (formatStr.length() <= scale) {
            formatStr += "0";
        }
        DecimalFormat decimalFormat = new DecimalFormat(formatStr);// 构造方法的字符格式这里如果小数不足2位,会以0补足.
        return decimalFormat.format(orgin);// format 返回的是字符串
    }

    public static String formatDecimal(double orgin, int scale) {
        String formatStr = "";
        for (int i = 0; i < scale; i++) {
            formatStr += "0";
        }
        formatStr = scale > 0 ? "0." + formatStr : "0";
        DecimalFormat decimalFormat = new DecimalFormat(formatStr);// 构造方法的字符格式这里如果小数不足2位,会以0补足.
        return decimalFormat.format(orgin);// format 返回的是字符串
    }

    /**
     * 将字符串转换成日�? 日期格式：yyyy-MM-dd HH:mm:ss
     *
     * @param dateValue 日期字符�?
     * @return
     */
    public static Date parseDate(String dateValue) {
        if (dateValue == null) {
            throw new NullPointerException();
        }
        return parseDate(dateValue, "yyyy-MM-dd");
    }

    /**
     * 将字符串按指定格式转换成日期
     *
     * @param dateValue 日期字符�?
     * @param format    日期格式
     * @return
     */
    public static Date parseDate(String dateValue, String format) {
        if (dateValue == null || format == null) {
            throw new NullPointerException();
        }

        DateFormat dateFormat = new SimpleDateFormat(format);
        Date date = null;
        try {
            // 你要得到的Date日期
            date = dateFormat.parse(dateValue);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * 将字符串转换成日�? 日期格式：yyyy-MM-dd HH:mm:ss
     *
     * @param dateValue 日期字符�?
     * @return
     */
    public static Date parseDateTime(String dateValue) {
        if (dateValue == null) {
            throw new NullPointerException();
        }
        return parseDateTime(dateValue, DateTimeFormat);
    }

    /**
     * 将字符串按指定格式转换成日期
     *
     * @param dateValue 日期字符�?
     * @param format    日期格式
     * @return
     */
    public static Date parseDateTime(String dateValue, String format) {
        if (dateValue == null || format == null) {
            throw new NullPointerException("parseDateTime dateValue format");
        }
        if (dateValue.length() < format.length())
            format = format.substring(0, dateValue.length());

        DateFormat dateFormat = new SimpleDateFormat(format);
        Date date = null;
        try {
            // 你要得到的Date日期
            date = dateFormat.parse(dateValue);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * 将日期转化为字符串，格式：yyyy-MM-dd HH:mm:ss
     *
     * @param date 日期
     * @return
     */
    public static String dateToString(Date date) {
        if (date == null) {
            throw new NullPointerException();
        }

        return dateToString(date, DateTimeFormat);
    }

    /**
     * 将日期按格式转化为字符串
     *
     * @param date   日期
     * @param format 格式
     * @return
     */
    public static String dateToString(Date date, String format) {
        if (date == null) {
            return "";
        }
        if (format == null) {
            throw new NullPointerException();
        }

        DateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(date);
    }

    /**
     * 规格化日期，使得时间能插入数据库 eg: 20120101 ---> 2012-01-01
     *
     * @return
     */
    public static String formatStringDate(String date) {

        if (date != null && !"".equals(date)) {
            return date.substring(0, 4) + "-" + date.substring(4, 6) + "-"
                    + date.substring(6, 8);
        } else {
            return "1900-01-01";
        }
    }

    /**
     * 规格化时间，使得时间能插入数据库 eg: 080706 ----> 08:07:06 即：8�?�?�?
     *
     * @param time
     * @return
     */
    public static String fromatStringTime(String time) {

        if (time != null && !"".equals(time)) {
            return time.substring(0, 2) + ":" + time.substring(2, 4) + ":"
                    + time.substring(4, 6);
        } else {
            return "00:00:00";
        }
    }

    /**
     * 校验日期时间的合法�?，格式为 YYYY-MM-DD HH:MM:SS
     *
     * @param dateTime 日期时间，格式为YYYY-MM-DD HH:MM:SS
     * @return true：合法；false：非�?
     */
    public static boolean isValidDateTime(String dateTime) {
        if (dateTime == null) {
            return false;
        }

        if (dateTime.length() == 19) {
            String[] array = dateTime.split(" ");
            return array.length == 2 && isValidDate(array[0]) && isValidTime(array[1]);
        }
        return false;

    }

    /**
     * 校验日期的合法�?，格式为 YYYY-MM-DD
     *
     * @param date 日期，格式为 YYYY-MM-DD
     * @return true：合法；false：非�?
     */
    public static boolean isValidDate(String date) {
        if (date == null) {
            return false;
        }

        String[] array = date.split("-");
        if (array.length != 3) {
            return false;
        }
        if (array[0].length() != 4 || array[1].length() != 2
                || array[2].length() != 2) {
            return false;
        }
        int year = Integer.valueOf(array[0]);
        int month = Integer.valueOf(array[1]);
        int day = Integer.valueOf(array[2]);
        if (year < 1900 || year > 2099) {
            return false;
        }
        if (month < 1 && month > 12) {
            return false;
        }
        if (month == 4 || month == 6 || month == 9 || month == 11) {
            if (day < 1 || day > 30) {// "日期�? - 30之间" ;
                return false;
            }
        } else {
            if (month != 2) {
                if (day < 1 || day > 31) {// "日期�? - 31之间" ;
                    return false;
                }
            } else {
                // month == 2
                if ((year % 100) != 0 && (year % 4 == 0) || (year % 100) == 0
                        && (year % 400) == 0) {
                    if (day < 1 || day > 29) {
                        // "日期�? - 29之间" ;
                        return false;
                    }
                } else {
                    if (day < 1 || day > 28) {
                        // "日期�? - 28之间" ;
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * 校验时间的合法�?，格式为 HH:MM:SS
     *
     * @param time 时间，格式为HH:MM:SS
     * @return true：合法；false：非�?
     */
    public static boolean isValidTime(String time) {
        if (time == null) {
            return false;
        }

        String[] array = time.split(":");
        if (array.length != 3) {
            return false;
        }
        if (array[0].length() != 2 || array[1].length() != 2
                || array[2].length() != 2) {
            return false;
        }
        int hour = Integer.valueOf(array[0]);
        int minute = Integer.valueOf(array[1]);
        int second = Integer.valueOf(array[2]);
        // 验证小时
        if (hour < 0 || hour > 24) {
            return false;
        }
        // 验证分钟
        if (minute < 0 || minute > 60) {
            return false;
        }
        // 验证�?
        if (second < 0 || second > 60) {
            return false;
        }
        return true;
    }

    /**
     * 获取当前日期的上�?���?��天的日期
     *
     * @return
     */
    public static String getLastMonthLastDayDate() {
        Date date = new Date();
        return getLastMonthLastDayDateByDate(dateToString(date, DateFormat));
        // return getLastMonthLastDayDateByDate(beginDate);
    }

    public static String getReportLastMonthEndDay(String beginDate) {
        String result = "";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DateFormat);
            Date date = sdf.parse(beginDate);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.roll(Calendar.DAY_OF_MONTH, -1);
            result = sdf.format(cal.getTime());
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 获取指定日期的上�?��月的�?���?��的日�?
     *
     * @param dateValue 指定日期，格式yyyy-MM-dd
     * @return
     */
    public static String getLastMonthLastDayDateByDate(String dateValue) {
        if (!isValidDate(dateValue)) {
            throw new IllegalArgumentException();
        }

        // 创建日历
        Calendar cal = Calendar.getInstance();
        Date date = parseDate(dateValue, "yyyy-MM-dd");
        cal.setTime(date);
        int year,month,day;

        // 获取上一月的第一�?
        month = cal.get(Calendar.MONTH); // 上个月月�?
        if (month == 0) {
            year = cal.get(Calendar.YEAR) - 1;
            month = 12;
        } else {
            year = cal.get(Calendar.YEAR);
        }
        String tempDate = year + "-";
        if (month <= 9) {
            tempDate += "0" + month;
        } else {
            tempDate += month;
        }
        tempDate += "-01";

        date = parseDate(tempDate, "yyyy-MM-dd");
        cal.setTime(date);

        // 通过上一月第�?��获取上一月最后一�?
        day = cal.getActualMaximum(Calendar.DAY_OF_MONTH);// 结束的天�?
        tempDate = year + "-";
        if (month <= 9) {
            tempDate += "0" + month;
        } else {
            tempDate += month;
        }
        tempDate += "-" + day;

        return tempDate;
    }

    /**
     * 比较两个日期的大小，日期格式为yyyy-MM-dd
     *
     * @param d1 日期1
     * @param d2 日期2
     * @return 0：d1=d2；负数：d1<d2；正数：d1>d2
     */
    public static int compare(String d1, String d2) {
        if (d1 == null || d2 == null) {
            throw new NullPointerException();
        }

        if (!isValidDate(d1) || !isValidDate(d2)) {
            throw new IllegalArgumentException();
        }

        return d1.compareTo(d2);
    }

    /**
     * 同步服务器时�?
     *
     * @param time 格式:2012-09-19 11:22:56
     * @return 0 失败 1 成功
     * @author yxc
     */
    public static int setSystemTimeForLinux(String time) {

        try {
            String cmd = "hwclock --set --date='" + time + "'";
            String[] comands = new String[]{"/bin/bash", "-c", cmd};
            Runtime.getRuntime().exec(comands).waitFor();

            String syncmd = "hwclock --hctosys";//
            String[] syncomands = new String[]{"/bin/bash", "-c", syncmd};
            Runtime.getRuntime().exec(syncomands).waitFor();

        } catch (Exception e) {
            e.printStackTrace();
            return 0;// 异常，同步失�?
        }

        return 1;// 同步服务器时间成�?
    }

    public static int setSystemTimeForWindows(String time) {

        String date = time.substring(0, 10);
        String times = time.substring(11, 19);

        try {
            Runtime.getRuntime().exec("cmd /c date " + date).waitFor();
            Runtime.getRuntime().exec("cmd /c time " + times).waitFor();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return 0;// 异常，同步失�?
        }

        return 1;// 同步服务器时间成�?
    }
}
