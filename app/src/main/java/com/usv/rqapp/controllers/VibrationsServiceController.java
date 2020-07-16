package com.usv.rqapp.controllers;

import android.util.Log;

import com.usv.rqapp.models.rq_mongodb.BaseVibrations;
import com.usv.rqapp.models.rq_mongodb.VibrationObject;
import com.usv.rqapp.services.vibrationsCall.RetrofitVibrationClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VibrationsServiceController {
    private static final String TAG = "VibrationsServiceController";
    private RetrofitVibrationClient retrofitVibrationClient;
    private volatile BaseVibrations baseVibrations;


    public VibrationsServiceController() {
        retrofitVibrationClient = RetrofitVibrationClient.getInstance();
    }


    //region Gson manipulation

    /**
     * Convert VibrationObject to FeatureCollection for MapBox
     *
     * @return
     */
    public String getFeatureCollectionGson() {
        Integer size = getBaseVibrations().getVibrationObjects().size();
        String itemToAdd = "";
        String point = "";
        String points="";
        String featureGson ="";


        for (int i = 0; i < size; i++) {
            itemToAdd = getBaseVibrations().getVibrationObjects().get(i).getVibrationID().getLongitude() + "," + getBaseVibrations().getVibrationObjects().get(i).getVibrationID().getLatitude();
            point = "\n           {\n" +
                    "                      \"type\": \"Feature\",\n" +
                    "                      \"properties\": {},\n" +
                    "                      \"geometry\": {\n" +
                    "                        \"type\": \"Point\",\n" +
                    "                        \"coordinates\": [" +itemToAdd +"]\n" +
                    "                       }\n" +
                    "            } ,";
            if (i == size - 1) {
                points += point.substring(0, point.length() - 1);
            } else {
                points += point;
            }
        }
        featureGson= "{\n" +
                "  \"type\": \"FeatureCollection\",\n" +
                "  \"features\": [\n" +
                points +
                "  ]\n" +
                "}";

        return featureGson;
    }
    //endregion


    //region Load data from vibration server

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
    //endregion


    //region Put data to vibration server

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
    //endregion


    //region Getters & Setters

    public BaseVibrations getBaseVibrations() {
        return baseVibrations;
    }


    public void setBaseVibrations(BaseVibrations baseVibrations) {
        this.baseVibrations = baseVibrations;
    }

    //endregion
}
