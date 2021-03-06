package com.im.test.utils;

import android.util.Log;

import com.im.test.BuildConfig;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author a
 * @date 2019/1/28
 */

public class TimeUtils {
    public static final String FORMAT_TYPE_1 = "yyyy-MM-dd HH:mm:ss";
    public static final String FORMAT_TYPE_2 = "yyyy年MM月dd日 HH时mm分ss秒";
    public static final String FORMAT_TYPE_3 = "yy-MM-dd";
    public static final String FORMAT_TYPE_4 = "yyyyMMddHHmmss";

    /**
     * formatType格式为yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日 HH时mm分ss秒
     *
     * @param data data Date类型的时间
     * @param formatType
     * @return
     */
    public static String dateToString(Date data, String formatType) {
        return new SimpleDateFormat(formatType).format(data);
    }

    /**
     * formatType要转换的string类型的时间格式
     * @param currentTime currentTime要转换的long类型的时间
     * @param formatType
     * @return
     * @throws ParseException
     */
    public static String longToString(long currentTime, String formatType)
            throws ParseException {
        // long类型转成Date类型
        Date date = longToDate(currentTime, formatType);
        // date类型转成String
        String strTime = dateToString(date, formatType);
        return strTime;
    }

    /**
     * strTime的时间格式必须要与formatType的时间格式相同
     *
     * @param strTime strTime要转换的string类型的时间，formatType要转换的格式yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日
     * @param formatType HH时mm分ss秒，
     * @return
     * @throws ParseException
     */
    public static Date stringToDate(String strTime, String formatType)
            throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(formatType);
        Date date = null;
        date = formatter.parse(strTime);
        return date;
    }

    /**
     *
     * @param currentTime currentTime要转换的long类型的时间
     * @param formatType formatType要转换的时间格式yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日 HH时mm分ss秒
     * @return
     * @throws ParseException
     */
    public static Date longToDate(long currentTime, String formatType)
            throws ParseException {
        // 根据long类型的毫秒数生命一个date类型的时间
        Date dateOld = new Date(currentTime);
        // 把date类型的时间转换为string
        String sDateTime = dateToString(dateOld, formatType);
        // 把String类型转换为Date类型
        Date date = stringToDate(sDateTime, formatType);
        return date;
    }


    public static String timeDifference(long time1, long time2) {
        String str = "";

        long diff = time1 - time2;

        long day = diff / (24 * 60 * 60 * 1000);
        long hour = (diff / (60 * 60 * 1000) - day * 24);
        long min = ((diff / (60 * 1000)) - day * 24 * 60 - hour * 60);
        long s = (diff / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);

        day = day < 0 ? 0 : day;
        hour = hour < 0 ? 0 : hour;
        min = min < 0 ? 0 : min;
        s = s < 0 ? 0 : s;

        if (day > 0) {

            str = day + "天前";
        } else if (day <= 0) {
            if (hour > 0) {
                str = hour + "小时前";
            } else {
                if (min > 0) {
                    str = min + "分钟前";
                } else {
                    str = s + "秒前";
                }
            }
        }
        if (BuildConfig.DEBUG){
            Log.d("TimeUtils", day + "天" + hour + "小时" + min + "分" + s + "秒");
        }
        return str;
    }

    public static long diff(long time1, long time2) {

        long diff = time1 - time2;

        long day = diff / (24 * 60 * 60 * 1000);
        long hour = (diff / (60 * 60 * 1000) - day * 24);
        long min = ((diff / (60 * 1000)) - day * 24 * 60 - hour * 60);
        long s = (diff / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);

        if (BuildConfig.DEBUG){
            Log.d("TimeUtils", day + "天" + hour + "小时" + min + "分" + s + "秒");
        }
        return min;
    }
}
