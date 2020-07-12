package com.usv.rqapp.services.vibrationBackgroundService;

import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.usv.rqapp.R;
import com.usv.rqapp.controllers.VibrationSender;
import com.usv.rqapp.controllers.VibrationsServiceController;

import static com.usv.rqapp.MyApp.CHANNEL_ID;

public class VibrationIntentService extends IntentService {
    private static final String TAG = "VibrationIntentService";
    private static final float DEFAULT = 0;

    public static final String CURRENT_X_MOVEMENT = "currentX";
    public static final String CURRENT_Y_MOVEMENT = "currentY";
    public static final String CURRENT_Z_MOVEMENT = "currentZ";
    public static final String AMPLITUDE_VALUE = "amplitudeValue";
    public static final String LOCATION_VALUE = "locationValue";

    public static final int SERVICE_DELAY_MILLIS = 50;

    private PowerManager.WakeLock wakeLock;
    private VibrationsServiceController vibrationsServiceController;
    private VibrationSender vibrationSender;

    public VibrationIntentService() {
        super(TAG);
        setIntentRedelivery(true);
    }

    private void initGeocodeController() {
        vibrationSender = new VibrationSender();
        vibrationsServiceController = new VibrationsServiceController();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initGeocodeController();
        Log.d(TAG, "onCreate");

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "ExampleApp:Wakelock");
        //wakeLock.acquire(200000);
        wakeLock.acquire();


        Log.d(TAG, "Wakelock acquired");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Vibration Service")
                    .setContentText("Running...")
                    .setSmallIcon(R.drawable.ic_logo_foreground)
                    .build();
            startForeground(1, notification);
        }
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        Log.d(TAG, "onHandleIntent");
        float currentX, currentY, currentZ, amplitudeValue;
        double[] location;
        assert intent != null;
        currentX = intent.getFloatExtra(CURRENT_X_MOVEMENT, DEFAULT);
        currentY = intent.getFloatExtra(CURRENT_Y_MOVEMENT, DEFAULT);
        currentZ = intent.getFloatExtra(CURRENT_Z_MOVEMENT, DEFAULT);
        amplitudeValue = intent.getFloatExtra(AMPLITUDE_VALUE, DEFAULT);
        location = intent.getDoubleArrayExtra(LOCATION_VALUE);

        vibrationSender.sendDataToServerThread(new LatLng(location[0], location[1]), amplitudeValue, vibrationsServiceController);
        SystemClock.sleep(SERVICE_DELAY_MILLIS);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        wakeLock.release();
        Log.d(TAG, "Wakelock released");
        vibrationSender = null;
        vibrationsServiceController = null;
    }


}