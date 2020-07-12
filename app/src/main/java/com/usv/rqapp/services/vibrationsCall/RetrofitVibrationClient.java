package com.usv.rqapp.services.vibrationsCall;



import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitVibrationClient {
    private static RetrofitVibrationClient instance = null;
    private Retrofit retrofit;
    private OkHttpClient client;

    private VibrationService vibrationService;

    private RetrofitVibrationClient() {

        OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder();

        client = okHttpBuilder.build();

        retrofit = new Retrofit.Builder().baseUrl(VibrationConstants.BASE_URL).addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        vibrationService = retrofit.create(VibrationService.class);
    }

    public static RetrofitVibrationClient getInstance() {
        if (instance == null) {
            instance = new RetrofitVibrationClient();
        }

        return instance;
    }

    public VibrationService getService() {
        return vibrationService;
    }
}
