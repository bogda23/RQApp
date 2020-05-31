package com.usv.rqapp.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.usv.rqapp.CONSTANTS;

public class NetworkManager {

    public static String getConnectivityStatusString(Context context) {
        String status = null;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                status = CONSTANTS.WIFI_ACTIVATED;
                return status;
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                status =CONSTANTS.DATA_ACTIVATED;
                return status;
            }
        } else {
            status = CONSTANTS.NO_INTERNET;
            return status;
        }
        return status;
    }
}
