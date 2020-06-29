package com.usv.rqapp.controllers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.google.firebase.firestore.GeoPoint;

import java.text.MessageFormat;

public class DialogController {
    public static void alertView(Context context, String message, int icon) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle("Informații")
                .setIcon(icon)
                .setMessage(message)
//     .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//      public void onClick(DialogInterface dialoginterface, int i) {
//          dialoginterface.cancel();
//          }})
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                    }
                }).show();
    }

    private String formatLocation(GeoPoint location, String format) {
        return MessageFormat.format(format,
                location.getLatitude(), location.getLongitude());
    }

}
