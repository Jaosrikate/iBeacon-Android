//package com.example.srikate.ibeacondemo.utils;
//
///**
// * Created by srikate on 10/8/2017 AD.
// */
//
//import java.util.Calendar;
//import java.util.Date;
//import java.util.Locale;
//
//public class TimeFormatUtils {
//
//    public static void removeTime(Calendar c) {
//        c.set(Calendar.HOUR_OF_DAY, 0);
//        c.set(Calendar.MINUTE, 0);
//        c.set(Calendar.SECOND, 0);
//        c.set(Calendar.MILLISECOND, 0);
//    }
//
//    public static void setMaxTime(Calendar c) {
//        c.set(Calendar.HOUR_OF_DAY, 23);
//        c.set(Calendar.MINUTE, 59);
//        c.set(Calendar.SECOND, 59);
//    }
//
//    public static String showDate(String dateString) {
//
//        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
//        try {
//            DateTime jodaTime = formatter.parseDateTime(dateString);
//            return jodaTime.toString("yyyy-MM-dd");
//        } catch (Exception e) {
//            Timber.e(e.getMessage());
//            return null;
//        }
//    }
//
//    public static String getFullDate(String dateString) {
//
//        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy");
//        try {
//            DateTime jodaTime = formatter.parseDateTime(dateString);
//            return jodaTime.toString("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
//        } catch (Exception e) {
//            return null;
//        }
//    }
//
//    public static String showDate(Date date) {
//        return new DateTime(date).toString("dd/MM/yyyy");
//    }
//
//    public static String showDateFullMonth(Date date) {
//        return new DateTime(date).toString("d MMM yyyy");
//    }
//
//    public static String showMonth(Date date) {
//        return new DateTime(date).toString("MMM yyyy");
//    }
//
//    public static String showDateFullMonth(String dateString) {
//        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
//        try {
//            DateTime jodaTime = formatter.parseDateTime(dateString);
//            return jodaTime.toString("d MMM yyyy");
//        } catch (Exception e) {
//            Timber.e(e.getMessage());
//            return null;
//        }
//    }
//
//    public static String showTime(Date date) {
//        return new DateTime(date).toString("hh:mm a", Locale.US);
//    }
//
//    public static String timeFormatApiSlot(Date date) {
//        return new DateTime(date).toString("yyyy-MM-dd");
//    }
//
//    public static Date getZeroTimeDate(Date dateParam) {
//        Date res = dateParam;
//        Calendar calendar = Calendar.getInstance();
//
//        calendar.setTime(dateParam);
//        calendar.set(Calendar.HOUR_OF_DAY, 0);
//        calendar.set(Calendar.MINUTE, 0);
//        calendar.set(Calendar.SECOND, 0);
//        calendar.set(Calendar.MILLISECOND, 0);
//
//        res = calendar.getTime();
//
//        return res;
//    }
//}
