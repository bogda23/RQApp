package com.usv.rqapp.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.usv.rqapp.CONSTANTS;
import com.usv.rqapp.interfaces.DataInterface;

public class MyReceiver extends BroadcastReceiver {

    public static String status;
    DataInterface onNetworkEventListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        onNetworkEventListener = (DataInterface) context;
        status = NetworkManager.getConnectivityStatusString(context);
        if (status.isEmpty()) {
            status = CONSTANTS.NO_INTERNET;
            onNetworkEventListener.networkStatusEvent(status);
        } else {
            onNetworkEventListener.networkStatusEvent(status);
        }

    }


}