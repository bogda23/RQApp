package com.usv.rqapp.controllers;

import android.content.Context;
import android.content.Intent;

public class Forwarder {

    //TODO: SHARE INFORMATION TO MESSENGER, WHATSAPP ETC
    public static void shareInformation(Context context) {

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("plain/text");
        intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{});
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
        intent.putExtra(android.content.Intent.EXTRA_TEXT, "");

        /* Send it off to the Activity-Chooser */
        context.startActivity(Intent.createChooser(intent, "Send"));
    }
}
