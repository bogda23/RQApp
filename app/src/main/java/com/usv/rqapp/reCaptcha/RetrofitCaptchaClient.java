package com.usv.rqapp.reCaptcha;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitCaptchaClient {
    private static Retrofit retrofit;

    public  static  Retrofit getClient(String baseUrl){
        if(retrofit == null){
            retrofit = new Retrofit.Builder().baseUrl(baseUrl).addConverterFactory(GsonConverterFactory.create()).build();
        }
        return  retrofit;
    }
}
