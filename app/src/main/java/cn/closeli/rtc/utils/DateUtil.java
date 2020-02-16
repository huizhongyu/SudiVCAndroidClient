package cn.closeli.rtc.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 *
 */

public class DateUtil {

    /**
     * 获取时间戳
     *
     * @return
     */
    public static String getTimestamp() {
        Random random = new Random();
        int num = random.nextInt(100);
        return System.currentTimeMillis() + "" + num;
    }

    /**
     * 输入：2018-05-14T07:40:02Z
     * 输出：2018-05-14 15:40:02
     *
     * @param utcString
     * @return
     */
    public static Date formatUtc2Sdf(String utcString) {
        try {
            utcString = utcString.replace("Z", " UTC");
            SimpleDateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss Z");
            SimpleDateFormat defaultFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = utcFormat.parse(utcString);
            return date;
            //return defaultFormat.format(date);
        } catch (ParseException pe) {
            pe.printStackTrace();
            return null;
        }
    }

    /**
     * 输入：String date = “2016-08-15T16:00:00.000Z”
     * 输出：2016-08-16 00:00:00
     *
     * @param UTCString
     * @return
     */
    private static String UTCStringtODefaultString(String UTCString) {
        try {
            UTCString = UTCString.replace("Z", " UTC");
            SimpleDateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS Z");
            SimpleDateFormat defaultFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = utcFormat.parse(UTCString);
            return defaultFormat.format(date);
        } catch (ParseException pe) {
            pe.printStackTrace();
            return null;
        }
    }

    public static boolean before(Date date1, Date date2) {
        return date1.before(date2);
    }

    public static boolean before(String time1, String time2) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date a = sdf.parse(time1);
        Date b = sdf.parse(time2);
        return a.before(b);
    }

    public static String dateToStrLong(Date dateDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        String dateString = formatter.format(dateDate);
        return dateString;
    }

    public static Date StringToSDate(String dateString) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        Date date1 = null;
        try {
            date1 = formatter.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date1;
    }

    /**
     * int -- > string 19930323 -- > 1993-03-23
     *
     * @return
     */
    public static String stringFormat(int dateInt) {
        String s = String.valueOf(dateInt);
        StringBuffer stringBuffer = new StringBuffer(dateInt);
        if (s.length() != 8) {
            return "";
        }
        stringBuffer.append(s.substring(0, 4)).append("-").append(s.substring(4, 6)).append("-").append(s.substring(6, 8));
        return stringBuffer.toString();
    }

    /**
     * int -- > string 1993-03-23 -- > 19930323
     *
     * @return
     */
    public static String intFormat(String dateInt) {
        String[] s = dateInt.split("-");
        StringBuffer stringBuffer = new StringBuffer();
        for (String add : s) {
            stringBuffer.append(add);
        }
        return stringBuffer.toString();
    }

    public static String getNow() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        return df.format(new Date());// new Date()为获取当前系统时间
    }

    /**
     * 获取日期
     * @return
     */
    public static String getWeekStr(int dayOfWeek) {
        String week ="星期一";
        switch (dayOfWeek) {
            case 1:
                week = "星期日";
                break;

            case 2:
                week = "星期一";
                break;

            case 3:
                week = "星期二";
                break;

            case 4:
                week = "星期三";
                break;

            case 5:
                week = "星期四";
                break;

            case 6:
                week = "星期五";
                break;

            case 7:
                week = "星期六";
                break;
        }
        return week;
    }
}
