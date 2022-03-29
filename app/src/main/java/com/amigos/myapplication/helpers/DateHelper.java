package com.amigos.myapplication.helpers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateHelper {

    public static String getCurrentDateAsString() {
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd - MMM - yyyy", Locale.getDefault());
        String result = df.format(c);
        return result;
    }

    public static Date getCurrentDate() {
        return Calendar.getInstance().getTime();
    }

    public static String dateToString(Date date){
        SimpleDateFormat formatter = new SimpleDateFormat("dd - MMM - yyyy", Locale.getDefault());
        String result = formatter.format(date);
        return result;
    }

    public static Date stringToDate(String date) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd - MMM - yyyy");
        Date result = formatter.parse(date);
        return result;
    }
}
