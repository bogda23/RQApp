package com.usv.rqapp.controllers;

import android.util.Log;

import com.usv.rqapp.models.rqdb.BaseVibrations;
import com.usv.rqapp.models.rqdb.VibrationIDLocation;
import com.usv.rqapp.models.rqdb.VibrationObject;
import com.usv.rqapp.services.vibrations.RetrofitVibrationClient;
import com.usv.rqapp.services.vibrations.VibrationService;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VibrationsServiceController {
    private static final String TAG = "VibrationsServiceController";
    private RetrofitVibrationClient retrofitVibrationClient;
    private BaseVibrations baseVibrations;


    public VibrationsServiceController() {
        retrofitVibrationClient = RetrofitVibrationClient.getInstance();
    }

    /**
     * @param isoCode
     */
    public void loadVibrationsByIsoCode(String isoCode) {
        Call<BaseVibrations> call = retrofitVibrationClient.getService().getPointsByIsoCode(isoCode);

        call.enqueue(new Callback<BaseVibrations>() {
            @Override
            public void onResponse(Call<BaseVibrations> call, Response<BaseVibrations> response) {
                setBaseVibrations(response.body());
                Log.e(TAG, baseVibrations.toString());
            }

            @Override
            public void onFailure(Call<BaseVibrations> call, Throwable t) {
                Log.e(TAG, t.getMessage());
            }
        });
    }

    /**
     * @param countryName
     */
    public void loadVibrationsByCountryName(String countryName) {
        Call<BaseVibrations> call = retrofitVibrationClient.getService().getPointsByCountryName(countryName);

        call.enqueue(new Callback<BaseVibrations>() {
            @Override
            public void onResponse(Call<BaseVibrations> call, Response<BaseVibrations> response) {
                setBaseVibrations(response.body());
                Log.e(TAG, baseVibrations.toString());
            }

            @Override
            public void onFailure(Call<BaseVibrations> call, Throwable t) {
                Log.e(TAG, t.getMessage());
            }
        });
    }


    /**
     * @param countyName
     */
    public void loadVibrationsByCountyName(String countyName) {
        Call<BaseVibrations> call = retrofitVibrationClient.getService().getPointsByCountyName(countyName);

        call.enqueue(new Callback<BaseVibrations>() {
            @Override
            public void onResponse(Call<BaseVibrations> call, Response<BaseVibrations> response) {
                setBaseVibrations(response.body());
                Log.e(TAG, baseVibrations.toString());
            }

            @Override
            public void onFailure(Call<BaseVibrations> call, Throwable t) {
                Log.e(TAG, t.getMessage());
            }
        });
    }


    /**
     * @param vibrationObject
     */
    public void putVibrationOnLocation(VibrationObject vibrationObject) {
        Call<BaseVibrations> call = retrofitVibrationClient.getService().putVibrationOnPoint(vibrationObject.getVibrationID().getLatitude(), vibrationObject.getVibrationID().getLongitude(),
                vibrationObject.getVibrationValue(), vibrationObject.getCountryName(), vibrationObject.getIsoCode(), vibrationObject.getCountyName());

        call.enqueue(new Callback<BaseVibrations>() {
            @Override
            public void onResponse(Call<BaseVibrations> call, Response<BaseVibrations> response) {
                setBaseVibrations(response.body());
                Log.e(TAG, baseVibrations.toString());
            }

            @Override
            public void onFailure(Call<BaseVibrations> call, Throwable t) {
                Log.e(TAG, t.getMessage());
            }
        });

    }

    /**
     * @return
     */
    public BaseVibrations getBaseVibrations() {
        return baseVibrations;
    }

    /**
     * @param baseVibrations
     */
    public void setBaseVibrations(BaseVibrations baseVibrations) {
        this.baseVibrations = baseVibrations;
    }
}
