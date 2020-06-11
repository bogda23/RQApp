package com.usv.rqapp.reCaptcha;

import com.usv.rqapp.data.captcha.CaptchaResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface IreCaptcha {
    @FormUrlEncoded
    @POST("google_reCaptcha.php")
    Call<CaptchaResponse> captchaValidate(@Field("recaptcha-response") String response );
}
