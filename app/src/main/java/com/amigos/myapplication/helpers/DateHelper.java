package com.amigos.myapplication.helpers;

import android.app.DatePickerDialog;
import android.content.Context;
import android.widget.DatePicker;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateHelper {

    public static void selectDate(TextView texview, Context context){

        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);


        DatePickerDialog datePickerDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year,
                                  int monthOfYear, int dayOfMonth) {

                SimpleDateFormat month_date = new SimpleDateFormat("MMM");
                c.set(Calendar.MONTH,monthOfYear);

                texview.setText(dayOfMonth + " - " + (month_date.format(c.getTime())) + " - " + year);

            }
        }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }
}
