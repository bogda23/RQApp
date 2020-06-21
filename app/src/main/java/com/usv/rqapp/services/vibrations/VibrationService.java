package com.usv.rqapp.services.vibrations;

import com.usv.rqapp.models.rqdb.BaseVibrations;
import com.usv.rqapp.models.rqdb.VibrationIDLocation;

import io.reactivex.rxjava3.core.Observable;
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
    Observable<String> putVibrationOnPoint(@Field("_id") VibrationIDLocation vibrationIDLocation,
                                           @Field("vibration_value") Double vibrationValue,
                                           @Field("country_name") String countryName,
                                           @Field("iso_code") String isoCode,
                                           @Field("county_name") String countyName);

}
