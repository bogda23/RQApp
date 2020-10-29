package com.usv.rqapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.GeoPoint;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.usv.rqapp.databinding.ActivityMainBinding;
import com.usv.rqapp.fragments.WelcomeFragment;
import com.usv.rqapp.interfaces.DataInterface;
import com.usv.rqapp.interfaces.IVibrationSender;
import com.usv.rqapp.interfaces.NewsFeedFragmentListener;
import com.usv.rqapp.network.MyReceiver;
import com.usv.rqapp.services.vibrationBackgroundService.VibrationIntentService;

public class MainActivity extends AppCompatActivity implements DataInterface, NewsFeedFragmentListener, IVibrationSender, SensorEventListener {
    private static final String TAG = "MainActivity";
    private static final Integer AMPLITUDE_READ_TIMES = 100;
    private final int REQUEST_CODE_START_ACTIVITY = 51;

    private BroadcastReceiver MyReceiver = null;
    private String networkStatus;
    private ActivityMainBinding binding;

    private TextView tvError;
    private ConstraintLayout clRoot;

    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private Boolean isAccelerometerAvailable;
    private Boolean isNotFirstVibration = false;
    private float currentX, currentY, currentZ;
    private int readTimes = 0;
    private float amplitudeValue;


    //region Location variables
    private Location lastLocation;
    private volatile LatLng coordsToSend;
    private LatLng destinationLatLng;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    //endregion


    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setLifecycleOwner(this);

        initLocation();

        broadcastIntent();
        initVibrationSystem();
        //  getWindow().setStatusBarColor(getColor(R.color.colorGreyLight));
        manageFragments();
    }


    //region Location ------------------------------------------------------------------------------
    private void initLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void getDeviceLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @SuppressLint("MissingPermission")
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    lastLocation = task.getResult();
                    if (lastLocation != null) {

                    } else {
                        LocationRequest locationRequest = LocationRequest.create();
                        locationRequest.setInterval(10000);
                        locationRequest.setFastestInterval(5000);
                        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                        locationCallback = new LocationCallback() {
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                super.onLocationResult(locationResult);
                                if (locationResult == null) {
                                    return;
                                }
                                lastLocation = locationResult.getLastLocation();
                                mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
                            }
                        };

                        mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "unable to get last location", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void createDeviceGpsLocationRequest() {
        //check if gps is enabled or not and then request user to enable it
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //Location Request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());


        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @SuppressLint("MissingPermission")
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                getDeviceLocation();                /** GPS Enabled*/

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {   /** Location is enabled by user or not*/
                if (e instanceof ResolvableApiException) {
                    ResolvableApiException resolvable = (ResolvableApiException) e;

                    try {
                        resolvable.startResolutionForResult(MainActivity.this, REQUEST_CODE_START_ACTIVITY);

                    } catch (IntentSender.SendIntentException e1) {
                        e1.printStackTrace();
                    } catch (Exception e2) {
                        e2.getMessage();
                    }
                }
            }
        });
    }
    //endregion ------------------------------------------------------------------------------------


    @Override
    protected void onPause() {
        super.onPause();
        //unregisterReceiver(MyReceiver);
    }


    private void initVibrationSystem() {
        sensorManager = (SensorManager) getSystemService(this.SENSOR_SERVICE);

        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            isAccelerometerAvailable = true;
        } else {
            isAccelerometerAvailable = false;
            Toast.makeText(this, "Nu ați permis colectarea vibrațiilor", Toast.LENGTH_SHORT).show();
        }

       /* if (isVibrationSystemActive) {
            // if (vehicleSpeed > MINIMUM_VEHICLE_SPEED) {
            //  collectVibration();
            //   }
        }*/
    }

    public void broadcastIntent() {
        MyReceiver = new MyReceiver();
        registerReceiver(MyReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private void manageFragments() {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_frame, new WelcomeFragment()).commit();
    }

    /**
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_START_ACTIVITY) {
            if (resultCode == RESULT_OK) {
                getDeviceLocation();
            }
        }
    }

    @Override
    public void networkStatusEvent(String status) {
        networkStatus = status;
        if (status == CONSTANTS.NO_INTERNET) {
            CustomAnimation.applyAnimationTo(binding.tvInternetStatus, R.anim.top_to_middle);
            binding.tvInternetStatus.setVisibility(View.VISIBLE);


        } else {
            CustomAnimation.applyAnimationTo(binding.tvInternetStatus, R.anim.middle_to_top);
            binding.tvInternetStatus.setVisibility(View.GONE);
        }

    }


    @Override
    public void onLocationPressed(String titleEvent, GeoPoint geoPoint) {
        if (titleEvent != null && geoPoint != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_frame, NavigatorFragment.newInstanceWithGeoPoint(titleEvent, geoPoint)).commit();
        }
    }

    @Override
    public void onVibrationSignalListener(Boolean start) {
        if (start) {
            if (isAccelerometerAvailable) {
                sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
            }

        } else {
            if (isAccelerometerAvailable && sensorManager != null) {
                sensorManager.unregisterListener(this);
            }
        }
    }

    public void startVibrationService(LatLng latLng, float currentX, float currentY, float currentZ, float amplitudeValue) {
        Intent serviceIntent = new Intent(this, VibrationIntentService.class);
        if (latLng != null) {
            serviceIntent.putExtra(VibrationIntentService.CURRENT_X_MOVEMENT, currentX);
            serviceIntent.putExtra(VibrationIntentService.CURRENT_Y_MOVEMENT, currentY);
            serviceIntent.putExtra(VibrationIntentService.CURRENT_Z_MOVEMENT, currentZ);
            serviceIntent.putExtra(VibrationIntentService.AMPLITUDE_VALUE, amplitudeValue);
            serviceIntent.putExtra(VibrationIntentService.LOCATION_VALUE, new double[]{latLng.getLatitude(), latLng.getLongitude()});

            ContextCompat.startForegroundService(this, serviceIntent);
        }

    }

    //region Overrided methods for accelerometer service
    @Override
    public void onSensorChanged(SensorEvent event) {
        createDeviceGpsLocationRequest();
        currentX = event.values[0];
        currentY = event.values[1];
        currentZ = event.values[2];


        if (currentX > amplitudeValue) {
            amplitudeValue = currentX;
        } else if (currentY > amplitudeValue) {
            amplitudeValue = currentY;
        } else if (currentZ > amplitudeValue) {
            amplitudeValue = currentZ;
        }


        if (lastLocation != null) {
            coordsToSend = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());

            if (readTimes > AMPLITUDE_READ_TIMES) {
                startVibrationService(coordsToSend, currentX, currentY, currentZ, amplitudeValue);
                readTimes = 0;
                amplitudeValue = 0;
            }
            readTimes++;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    //endregion
}

