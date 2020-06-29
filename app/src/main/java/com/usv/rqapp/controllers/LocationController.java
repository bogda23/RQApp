package com.usv.rqapp.controllers;

import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.view.View;

import java.text.MessageFormat;
import java.util.Locale;

public class LocationController {

    /**
     * @param lastLocation
     * @return
     */
    public Uri getUriFromLocation(Location lastLocation) {
        Uri uri = null;
        if (!validLocation(lastLocation)) {
            uri = Uri.parse(formatLocation(lastLocation, "geo:{0},{1}?q={0},{1}"));
            return uri;
        }
        return uri;
    }


    /**
     * @param location
     * @return
     */
    private String getLongitude(Location location) {
        return String.format(Locale.getDefault(), "%3.5f", location.getLongitude());
    }


    /**
     * @param location
     * @return
     */
    private String getLatitude(Location location) {
        return String.format(Locale.getDefault(), "%2.5f", location.getLatitude());
    }


    /**
     * @param location
     * @param format
     * @return
     */
    private String formatLocation(Location location, String format) {
        return MessageFormat.format(format,
                getLatitude(location), getLongitude(location));
    }


    /**
     * @param location
     * @return
     */
    public static boolean validLocation(Location location) {
        if (location == null) {
            return false;
        }

        // Location must be from less than 30 seconds ago to be considered valid
        if (Build.VERSION.SDK_INT < 17) {
            return System.currentTimeMillis() - location.getTime() < 30e3;
        } else {
            return SystemClock.elapsedRealtimeNanos() - location.getElapsedRealtimeNanos() < 30e9;
        }
    }

}
