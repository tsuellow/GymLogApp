package com.example.android.gymlogapp.data;

import android.arch.persistence.room.TypeConverter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateConverter {

    @TypeConverter
    public static String getDateString(Date date){
        String dateString=null;
        if(date!=null){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            dateString= sdf.format(date);
        }
        return dateString;
    }

    @TypeConverter
    public static Date String2Date(String datestr){
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date date = null;
        if(datestr!=null) {
            try {
                date = sdf.parse(datestr);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return date;
    }
}
