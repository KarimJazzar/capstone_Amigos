package com.amigos.myapplication.helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.amigos.myapplication.R;
import com.amigos.myapplication.activities.CreateRideActivity;

public class AlertDialogHelper {
    public static void show(Context context, String title, String msg) {
        new AlertDialog.Builder(context)
        .setTitle(title)
        .setMessage(msg)
        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Continue with delete operation
            }
        })
        .setIcon(R.drawable.warning)
        .show();
    }
}
