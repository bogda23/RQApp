package com.usv.rqapp.services.vibrationsCall;

import com.usv.rqapp.models.rq_mongodb.BaseVibrations;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface VibrationService {
    @GET(VibrationConstants.INFO)
    Call<BaseVibrations> getPointsByIsoCode(@Query("iso_code") String isoCode);

    @GET(VibrationConstants.INFO)
    Call<BaseVibrations> getPointsByCountryName(@Query("country_name") String countryName);

    @GET(VibrationConstants.INFO)
    Call<BaseVibrations> getPointsByCountyName(@Query("county_name") String countyName);

    @POST(VibrationConstants.VIBRATIONS)
    @FormUrlEncoded
    Call<BaseVibrations>  putVibrationOnPoint(@Field("lat") Double latitude,
                                            @Field("lng") Double longitude,
                                           @Field("vibration_value") Double vibrationValue,
                                           @Field("country_name") String countryName,
                                           @Field("iso_code") String isoCode,
                                           @Field("county_name") String countyName);

}
