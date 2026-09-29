package com.semple.zhixiaoduo.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @filename DateUtils
 * @description 日期工具类
 * @autor aofaming
 * @date 2023/5/30 9:01
 */
@Slf4j
public class DateUtils {

    private static final ZoneId ZONE = ZoneId.systemDefault();

    public static final String DATE_FORMAT = "yyyy-MM-dd";

    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final String DATETIME_FORMAT_SHORT = "yyyyMMddHHmmss";

    public static final String DATE_DEFAULT_LONG_FORMAT = "yyyyMMddHHmmssSSS";

    public static final String DATE_DEFAULT_SHORT_FORMAT = "yyyyMMdd";

    public static final String DATE_FORMAT_YM = "yyyyMM";


    public static final String DATETIME_FORMAT_CHINESE = "yyyy年MM月dd日HH时mm分";


    public static final String DATETIME_FORMAT_YANDT = "yyyyMMdd-HHmmss";


    public static final String DATETIME_FORMAT_ZEROS = "yyyy-MM-dd HH:mm:00";

    public static final String DATETIME_FORMAT_NOTSECONDS = "yyyy-MM-dd HH:mm";

    public static final String DATETIME_FORMAT_NOBLANK = "yyyy-MM-ddHH:mm:ss";


    public static final String DATETIME_FORMAT_CHINESE_BLANK = "yyyy-MM-ddHH: mm: ss";


    public static final String DATETIME_SHORT_FORMAT = "yyyy-MM-dd HH:mm";

    /**
     * 获取当前日期
     *
     * @return java.time.LocalDate
     * @author aofaming
     * @date 2023/5/30 9:32
     */
    public static LocalDate getCurrentDate() {
        return LocalDate.now();
    }

    /**
     * 获取当前日期时间
     *
     * @return java.time.LocalDateTime
     * @author aofaming
     * @date 2023/5/30 9:36
     */
    public static LocalDateTime getCurrentDateTime() {
        return LocalDateTime.now();
    }

    /**
     * 日期字符串转化为日期类型
     *
     * @param dateStr
     * @return java.time.LocalDate
     * @author aofaming
     * @date 2023/5/30 9:13
     */
    public static LocalDate strToLocalDate(String dateStr) {
        if (null == dateStr || dateStr.length() == 0) {
            return null;
        }
        LocalDate date = LocalDate.parse(dateStr);
        return date;
    }

    /**
     * 将style格式的日期字符串转化为日期类型
     *
     * @param dateStr
     * @param style
     * @return java.time.LocalDate
     * @author aofaming
     * @date 2023/5/30 9:15
     */
    public static LocalDate strToLocalDate(String dateStr, String style) {
        if (null == dateStr || dateStr.length() == 0) {
            return null;
        }
        LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(style));
        return date;
    }

    /**
     * 日期字符串转化为日期时间类型
     *
     * @param dateStr
     * @return java.time.LocalDateTime
     * @author aofaming
     * @date 2023/5/30 9:13
     */
    public static LocalDateTime strToLocalDateTime(String dateStr) {
        if (null == dateStr || dateStr.length() == 0) {
            return null;
        }
        LocalDateTime date = LocalDateTime.parse(dateStr);
        return date;
    }

    /**
     * 将style格式的日期字符串转化为日期类型
     *
     * @param dateStr
     * @param style
     * @return java.time.LocalDateTime
     * @author aofaming
     * @date 2023/5/30 9:15
     */
    public static LocalDateTime strToLocalDateTime(String dateStr, String style) {
        if (null == dateStr || dateStr.length() == 0) {
            return null;
        }
        LocalDateTime date = LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern(style));
        return date;
    }

    /**
     * 将localDate转化为yyyy-MM-dd格式日期字符串
     *
     * @param date
     * @return java.lang.String
     * @author aofaming
     * @date 2023/5/30 9:14
     */
    public static String localDateToStr(LocalDate date) {
        if (null == date) {
            return null;
        }
        String strDate = date.format(DateTimeFormatter.ofPattern(DATE_FORMAT));
        return strDate;
    }

    /**
     * 将localDate转化为style格式的日期字符串
     *
     * @param date
     * @param style
     * @return java.lang.String
     * @author aofaming
     * @date 2023/5/30 9:14
     */
    public static String localDateToStr(LocalDate date, String style) {
        if (null == date) {
            return null;
        }
        String strDate = date.format(DateTimeFormatter.ofPattern(style));
        return strDate;
    }

    /**
     * 将localDateTime转化为yyyy-MM-dd HH:mm:ss格式日期字符串
     *
     * @param dateTime
     * @return java.lang.String
     * @author aofaming
     * @date 2023/5/30 9:14
     */
    public static String localDateTimeToStr(LocalDateTime dateTime) {
        if (null == dateTime) {
            return null;
        }
        String strDate = dateTime.format(DateTimeFormatter.ofPattern(DATETIME_FORMAT));
        return strDate;
    }

    /**
     * 将localDateTime转化为style格式的日期字符串
     *
     * @param dateTime
     * @param style
     * @return java.lang.String
     * @author aofaming
     * @date 2023/5/30 9:14
     */
    public static String localDateTimeToStr(LocalDateTime dateTime, String style) {
        if (null == dateTime) {
            return null;
        }
        String strDate = dateTime.format(DateTimeFormatter.ofPattern(style));
        return strDate;
    }

    /**
     * 日期字符串转化为日期类型
     *
     * @param dateStr
     * @return java.util.Date
     * @author aofaming
     * @date 2023/5/30 9:13
     */
    public static Date strToDate(String dateStr) {
        if (null == dateStr || dateStr.length() == 0) {
            return null;
        }
        LocalDate date = LocalDate.parse(dateStr);
        return localDateToDate(date);
    }

    /**
     * 将style格式的日期字符串转化为日期类型
     *
     * @param dateStr
     * @param style
     * @return java.util.Date
     * @author aofaming
     * @date 2023/5/30 9:15
     */
    public static Date strToDate(String dateStr, String style) {
        if (null == dateStr || dateStr.length() == 0) {
            return null;
        }
        LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(style));
        return localDateToDate(date);
    }

    /**
     * 日期字符串转化为日期时间类型
     *
     * @param dateStr
     * @return java.util.Date
     * @author aofaming
     * @date 2023/5/30 9:13
     */
    public static Date strToDateTime(String dateStr) {
        if (null == dateStr || dateStr.length() == 0) {
            return null;
        }
        LocalDateTime date = LocalDateTime.parse(dateStr);
        return localDateTimeToDate(date);
    }

    /**
     * 将style格式的日期字符串转化为日期类型
     *
     * @param dateStr
     * @param style
     * @return java.util.Date
     * @author aofaming
     * @date 2023/5/30 9:15
     */
    public static Date strToDateTime(String dateStr, String style) {
        if (null == dateStr || dateStr.length() == 0) {
            return null;
        }
        LocalDateTime date = LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern(style));
        return localDateTimeToDate(date);
    }

    /**
     * 将localDate转化为yyyy-MM-dd格式日期字符串
     *
     * @param date
     * @return java.lang.String
     * @author aofaming
     * @date 2023/5/30 9:14
     */
    public static String dateToStr(Date date) {
        if (null == date) {
            return null;
        }
        LocalDate localDate = dateToLocalDate(date);
        String strDate = localDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT));
        return strDate;
    }


    public static String transForDate5(Integer ms){
        String str = "";
        if(ms!=null){
            long msl=(long)ms*1000;
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            if(ms!=null){
                try {
                    str=sdf.format(msl);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return str;
    }



    /**
     * 将Date转化为style格式的日期字符串
     *
     * @param date
     * @param style
     * @return java.lang.String
     * @author aofaming
     * @date 2023/5/30 9:14
     */
    public static String dateToStr(Date date, String style) {
        if (null == date) {
            return null;
        }
        LocalDate localDate = dateToLocalDate(date);
        String strDate = localDate.format(DateTimeFormatter.ofPattern(style));
        return strDate;
    }


    public static void main(String[] args) {
//		System.out.printf(DateUtils.dateTimeToStr(new Date()));

        try {



           String d = DateUtils.strDateFormat("2024-09-07 12:23:34",DateUtils.DATETIME_FORMAT,"yyyy-MM-dd HH:mm");
            System.out.println(d);
//			test.setStartTime(date);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 将dateTime转化为日期时间yyyy-MM-dd HH:mm:ss字符串
     *
     * @param dateTime
     * @return java.lang.String
     * @author aofaming
     * @date 2023/5/30 9:14
     */
    public static String dateTimeToStr(Date dateTime) {
        if (null == dateTime) {
            return null;
        }
        LocalDateTime localDateTime = dateToLocalDateTime(dateTime);
        String strDate = localDateTime.format(DateTimeFormatter.ofPattern(DATETIME_FORMAT));
        return strDate;
    }

    /**
     * 将dateTime转化为 style格式的 字符串
     *
     * @param dateTime
     * @return java.lang.String
     * @author aofaming
     * @date 2023/5/30 9:14
     * @param style style 参数。
     */
    public static String dateTimeToStr(Date dateTime,String style) {
        if (null == dateTime) {
            return null;
        }
        LocalDateTime localDateTime = dateToLocalDateTime(dateTime);
        String strDate = localDateTime.format(DateTimeFormatter.ofPattern(style));
        return strDate;
    }

    /**
     * 将dateTime转化为style格式的日期字符串
     *
     * @param dateTime
     * @param style
     * @return java.lang.String
     * @author aofaming
     * @date 2023/5/30 9:14
     */
    public static String localDateTimeToStr(Date dateTime, String style) {
        if (null == dateTime) {
            return null;
        }
        LocalDateTime localDateTime = dateToLocalDateTime(dateTime);
        String strDate = localDateTime.format(DateTimeFormatter.ofPattern(style));
        return strDate;
    }


    /***
     * 获取30分钟之前的时间
     *
     * @author aofaming
     * @date 2024/2/26 16:37
     * @param style
     * @return String
     */
    public static String currentbeforeThirtyMin(String style){
        Calendar calendar = Calendar.getInstance();
        Date currentTime =calendar.getTime();
        calendar.setTime(currentTime);
        calendar.add(Calendar.MINUTE,-30);
        Date newtime =calendar.getTime();
        LocalDateTime localDateTime = dateToLocalDateTime(newtime);
        String strDate = localDateTime.format(DateTimeFormatter.ofPattern(style));
        return strDate;
    }


    /***
     * 获取3个月之前的日期
     *
     * @author aofaming
     * @date 2024/2/26 16:39
     * @param style
     * @return String
     */
    public static String currentbeforeThreeMoth(String style){
        Calendar calendar = Calendar.getInstance();
        Date currentTime =calendar.getTime();
        calendar.setTime(currentTime);
        calendar.add(Calendar.MONTH,-3);
        Date newtime =calendar.getTime();
        LocalDateTime localDateTime = dateToLocalDateTime(newtime);
        String strDate = localDateTime.format(DateTimeFormatter.ofPattern(style));
        return strDate;
    }




    /***
     * 获取昨天的日期
     *
     * @author aofaming
     * @date 2024/3/14 10:32
     * @param style
     * @return String
     */
    public static String getYesterDay(String style){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY,-1);
        Date newtime =calendar.getTime();
        LocalDateTime localDateTime = dateToLocalDateTime(newtime);
        String strDate = localDateTime.format(DateTimeFormatter.ofPattern(style));
        return strDate;
    }





    /**
     * 将Date转换成LocalDate
     *
     * @param date
     * @return java.time.LocalDate
     * @author aofaming
     * @date 2023/5/30 9:20
     */
    public static LocalDate dateToLocalDate(Date date) {
        return date.toInstant().atZone(ZONE).toLocalDate();
    }

    /**
     * 将Date转换成LocalDateTime
     *
     * @param date
     * @return java.time.LocalDate
     * @author aofaming
     * @date 2023/5/30 9:20
     */
    public static LocalDateTime dateToLocalDateTime(Date date) {
        return date.toInstant().atZone(ZONE).toLocalDateTime();
    }

    /**
     * 将LocalDate转换成Date
     *
     * @param localDate
     * @return java.util.Date
     * @author aofaming
     * @date 2023/5/30 9:28
     */
    public static Date localDateToDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 将LocalDateTime转换成Date
     *
     * @param localDateTime
     * @return java.util.Date
     * @author aofaming
     * @date 2023/5/30 9:28
     */
    public static Date localDateTimeToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZONE).toInstant());
    }

    /**
     * 获取两个日期相差的天数
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return int
     * @author justin
     * @date 2023/8/1 15:49
     */
    public static int daysBetween(Date startDate, Date endDate) {
        Calendar calst = Calendar.getInstance();
        Calendar caled = Calendar.getInstance();
        calst.setTime(startDate);
        caled.setTime(endDate);
        calst.set(11, 0);
        calst.set(12, 0);
        calst.set(13, 0);
        caled.set(11, 0);
        caled.set(12, 0);
        caled.set(13, 0);
        long days = (caled.getTime().getTime() / 1000L - calst.getTime().getTime() / 1000L) / 3600L / 24L;
        return (int) days;
    }

    public static String strDateFormat(String dateStr, String oldPattern, String newPattern) {
        SimpleDateFormat df = new SimpleDateFormat(oldPattern);

        try {
            Date date = df.parse(dateStr);
            df = new SimpleDateFormat(newPattern);
            String str = df.format(date);
            return str;
        } catch (ParseException var6) {
            var6.printStackTrace();
            return null;
        }
    }

    /**
     * 将时间戳转换为日期
     *
     * @param timestamp
     * @return java.util.Date
     * @author aofaming
     * @date 2023/7/26 13:43
     */
    public static Date timestampToDate(Timestamp timestamp) {
        LocalDateTime localDateTime = timestamp.toLocalDateTime();
        return localDateTimeToDate(localDateTime);
    }


    /****
     * 获取时间段 周一至周日
     *
     * @author aofaming
     * @date 2023/12/22 9:40
     * @param startDate
     * @param endDate
     * @return List<StringBuilder>
     */
    public static List<String> dateSplit(String startDate, String endDate){

        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            List<Date> dateList = new ArrayList();
//        开始日期
            Date start = simpleDateFormat.parse(startDate);
//        结束日期
            Date end = simpleDateFormat.parse(endDate);

            Calendar c = Calendar.getInstance();
            c.setTime(start);
            if (c.get(Calendar.DAY_OF_WEEK) == 1) {
                c.add(Calendar.DAY_OF_MONTH, -1);
            }
            c.add(Calendar.DATE, c.getFirstDayOfWeek() - c.get(Calendar.DAY_OF_WEEK) + 1);
            Date time = c.getTime();
            Long spi = end.getTime() - start.getTime();
            Long step = spi / (24 * 60 * 60 * 1000);// 相隔天数

            dateList.add(start);
            for (int i = 1; i <= step; i++) {
                // 比上一天加1
                dateList.add(new Date(dateList.get(i - 1).getTime() + (24 * 60 * 60 * 1000)));
            }

            ArrayList<String> dateLists = new ArrayList();
            String startFormat = simpleDateFormat.format(start);
            StringBuilder stringBuilder = null;
            if (!LocalDate.parse(startFormat).getDayOfWeek().toString().equals("MONDAY")) {
                stringBuilder = new StringBuilder();
                stringBuilder.append(startFormat).append("~");
            }
            for (Date date : dateList) {
                String day = simpleDateFormat.format(date);
                String s = LocalDate.parse(day).getDayOfWeek().toString();

                if ("MONDAY".equals(s)) {
                    stringBuilder = new StringBuilder();
                    stringBuilder.append(day).append("~");
                }

                if ("SUNDAY".equals(s)) {
                    stringBuilder.append(day);
                    dateLists.add(stringBuilder.toString());
                }
            }

            String endFormat = simpleDateFormat.format(end);
            if (!LocalDate.parse(endFormat).getDayOfWeek().toString().equals("SUNDAY")) {
                stringBuilder.append(endFormat);
                dateLists.add(stringBuilder.toString());
            }
            return dateLists;
        }catch (Exception e){
            log.error("时间转换异常,异常信息：{}", ExceptionUtils.getThrowables(e));
        }
        return null;
    }


    /***
     * 获取所在月第一天的日期
     *
     * @author aofaming
     * @date 2024/3/15 16:19
     * @param date
     * @param style
     * @return String
     */
    public static String getFirstDayDateOfMonth(final Date date,String style) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        final int last = cal.getActualMinimum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DAY_OF_MONTH, last);
        Date time = cal.getTime();
        LocalDateTime localDateTime = dateToLocalDateTime(time);
        return localDateTime.format(DateTimeFormatter.ofPattern(style));
    }


    /***
     * @author aofaming
     * @date 2024/3/15 16:19
     * @param dateStr
     * @return String
     */
    public static String getFirstDayOfWeek(String dateStr) {
        LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(DATE_FORMAT));
        Date time = localDateToDate(date);
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_DEFAULT_SHORT_FORMAT); // 设置时间格式
        Calendar cal = Calendar.getInstance();
        cal.setTime(time);
        // 判断要计算的日期是否是周日，如果是则减一天计算周六的，否则会出问题，计算到下一周去了
        int dayWeek = cal.get(Calendar.DAY_OF_WEEK);// 获得当前日期是一个星期的第几天
        if (1 == dayWeek) {
            cal.add(Calendar.DAY_OF_MONTH, -1);
        }
        cal.setFirstDayOfWeek(Calendar.MONDAY);// 设置一个星期的第一天，按中国的习惯一个星期的第一天是星期一
        int day = cal.get(Calendar.DAY_OF_WEEK);// 获得当前日期是一个星期的第几天
        cal.add(Calendar.DATE, cal.getFirstDayOfWeek() - day);// 根据日历的规则，给当前日期减去星期几与一个星期第一天的差值

        return sdf.format(cal.getTime());
    }


}
