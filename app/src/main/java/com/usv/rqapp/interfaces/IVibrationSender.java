package com.usv.rqapp.interfaces;

public interface IVibrationSender {

    String TAG = "IVibrationSender";

    void onVibrationSignalListener(Boolean start);
}
