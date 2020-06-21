package com.usv.rqapp.controllers;

import android.util.Log;

import com.usv.rqapp.models.rqdb.BaseVibrations;
import com.usv.rqapp.services.vibrations.RetrofitVibrationClient;
import com.usv.rqapp.services.vibrations.VibrationService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VibrationsServiceController {
    private static final String TAG = "VibrationsServiceController";
    private VibrationService iServiceVibrations;
    private RetrofitVibrationClient retrofitVibrationClient;
    private BaseVibrations baseVibrations;

    public VibrationsServiceController() {
        retrofitVibrationClient = RetrofitVibrationClient.getInstance();
    }

    public void loadVibrations() {
        Call<BaseVibrations> call = retrofitVibrationClient.getService().getPointsByIsoCode("RO");

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

    public BaseVibrations getBaseVibrations() {
        return baseVibrations;
    }

    public void setBaseVibrations(BaseVibrations baseVibrations) {
        this.baseVibrations = baseVibrations;
    }
}
