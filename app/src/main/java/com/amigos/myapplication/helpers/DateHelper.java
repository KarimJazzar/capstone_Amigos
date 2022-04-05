package com.amigos.myapplication.helpers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateHelper {
    public static String getCurrentDateAsString() {
        Date c = new Date();
        SimpleDateFormat df = new SimpleDateFormat("dd - MMM - yyyy");
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

    public static boolean didDatePassed(String date) {
        try {
            Date current = new SimpleDateFormat("dd - MMM - yyyy").parse(getCurrentDateAsString());
            return  (new SimpleDateFormat("dd - MMM - yyyy").parse(date).before(current));
        } catch (ParseException e) {
            e.printStackTrace();
            return true;
        }
    }
}
