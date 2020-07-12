package com.usv.rqapp.controllers;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.usv.rqapp.models.rq_mongodb.VibrationIDLocation;
import com.usv.rqapp.models.rq_mongodb.VibrationObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.usv.rqapp.CONSTANTS.MAPBOX_ACCESS_TOKEN;

public class VibrationSender {
    private VibrationObject vibrationObject;
    private MapboxGeocoding reverseGeocode;

    private static final String TAG = "GeocodeController";

    public VibrationSender() {
        vibrationObject = new VibrationObject();
    }

    //region Functionalities
    private void sendVibrationObject(LatLng latLng, float amplitudeValue, VibrationsServiceController vibrationsServiceController) {
        requestGeocodeFromLocation(latLng);

        reverseGeocode.enqueueCall(new Callback<GeocodingResponse>() {
            @Override
            public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
                if (response.body() != null) {
                    List<CarmenFeature> results = response.body().features();
                    if (results.size() > 0) {
                        CarmenFeature feature = results.get(0);

                        vibrationObject.setCountyName(feature.context().get(0).text());
                        vibrationObject.setIsoCode(feature.context().get(1).shortCode().toUpperCase());
                        vibrationObject.setCountryName(feature.context().get(1).text());

                        VibrationObject vibrationObjectToSend = new VibrationObject(new VibrationIDLocation(latLng.getLatitude(), latLng.getLongitude())
                                , Double.valueOf(amplitudeValue), vibrationObject.getCountryName(), vibrationObject.getIsoCode(), vibrationObject.getCountyName());

                        sendDataToVibrationServer(vibrationsServiceController, vibrationObjectToSend);

                    } else {
                        Log.e(TAG, "onResponse: ");
                    }
                }
            }

            @Override
            public void onFailure(Call<GeocodingResponse> call, Throwable t) {
                Timber.e("Geocoding Failure: " + t.getMessage());
            }
        });


    }

    public void sendDataToServerThread(LatLng latLng, float amplitudeValue, VibrationsServiceController vibrationsServiceController) {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            public void run() {
                sendVibrationObject(latLng, amplitudeValue, vibrationsServiceController);
            }
        }, 0);
    }

    private void requestGeocodeFromLocation(LatLng latLng) {
        reverseGeocode = MapboxGeocoding.builder()
                .accessToken(MAPBOX_ACCESS_TOKEN)
                .query(Point.fromLngLat(latLng.getLongitude(), latLng.getLatitude()))
                .geocodingTypes(GeocodingCriteria.TYPE_PLACE)
                .mode(GeocodingCriteria.MODE_PLACES)
                .build();
    }

    //region Vibration Service initiation
    private void sendDataToVibrationServer(VibrationsServiceController vibrationsServiceController, VibrationObject vibrationObject) {

        Log.e(TAG, "onResponse: Country:  " + vibrationObject.getCountryName());
        Log.e(TAG, "onResponse: County:   " + vibrationObject.getCountyName());
        Log.e(TAG, "onResponse: IsoCode:  " + vibrationObject.getIsoCode());
        Log.e(TAG, "onResponse: Amplitude:  " + vibrationObject.getVibrationValue());
        Log.e(TAG, "onResponse: Location:  " + vibrationObject.getVibrationID().toString());

        vibrationsServiceController.putVibrationOnLocation(vibrationObject);
        if (vibrationsServiceController.getBaseVibrations() != null) {
            Log.e(TAG, vibrationsServiceController.getBaseVibrations().toString());
        }

    }
    //endregion


    //region Getters && Setters

    public VibrationObject getVibrationObject() {
        return vibrationObject;
    }

    public void setVibrationObject(VibrationObject vibrationObject) {
        this.vibrationObject = vibrationObject;
    }


    //endregion


}
