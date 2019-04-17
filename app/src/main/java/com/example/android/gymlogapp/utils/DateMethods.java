package com.example.android.gymlogapp.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import static java.util.Calendar.*;


public class DateMethods {

    public static Date getRoundDate(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    public static Date getCurrentClassCutoff(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.MINUTE, -35);
        return calendar.getTime();
    }

    public static Date getRoundedHour(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }


    public static int getDiffYears(Date first, Date last) {
        Calendar a = getCalendar(first);
        Calendar b = getCalendar(last);
        int diff = b.get(YEAR) - a.get(YEAR);
        if (a.get(MONTH) > b.get(MONTH) ||
                (a.get(MONTH) == b.get(MONTH) && a.get(DATE) > b.get(DATE))) {
            diff--;
        }
        return diff;
    }

    public static Calendar getCalendar(Date date) {
        Calendar cal = Calendar.getInstance(Locale.US);
        cal.setTime(date);
        return cal;
    }

    public static String getDateString(Calendar cal){
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        month = 1 + month;
        return day + "/" + month + "/" + year;
    }

    public static String getTimeString(int hour, int min){
        String timePicked = "";
        String am_pm;
        String sHour;
        if(hour >12){
            hour = hour-12;
            am_pm="pm";
            sHour=String.valueOf(hour);
        } else {
            am_pm="am";
            sHour = String.valueOf(hour);
        }
        String sMinute = "00";
        if(min < 10){
            sMinute = "0"+min;
        } else {
            sMinute = String.valueOf(min);
        }
        timePicked=""+sHour+":"+sMinute+" "+am_pm;
        return timePicked;
    }

}
