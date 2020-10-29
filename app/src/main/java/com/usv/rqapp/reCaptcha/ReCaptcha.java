package com.usv.rqapp.reCaptcha;

public class ReCaptcha {

    //localhost = 10.0.2.2 (on emulator) and 10.0.2.1 (router)  or 127.0.0.1  or 192.168.0.105:80
    private static final String BASE_CAPTCHA_URL = "http://192.168.0.105:80/rqapp_reCaptcha_validation_server/";

    public static IreCaptcha getCaptchaApi(){
        return RetrofitCaptchaClient.getClient(BASE_CAPTCHA_URL).create(IreCaptcha.class);
    }

}
