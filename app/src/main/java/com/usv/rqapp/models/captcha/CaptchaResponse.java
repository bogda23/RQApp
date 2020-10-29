package com.usv.rqapp.models.captcha;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CaptchaResponse {
    private boolean success;
    private String challenge_ts;
    private String apk_package_name;
    @SerializedName("error-codes")
    private List<String> errorCodes;

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setChallenge_ts(String challenge_ts) {
        this.challenge_ts = challenge_ts;
    }

    public void setApk_package_name(String apk_package_name) {
        this.apk_package_name = apk_package_name;
    }

    public void setErrorCodes(List<String> errorCodes) {
        this.errorCodes = errorCodes;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getChallenge_ts() {
        return challenge_ts;
    }

    public String getApk_package_name() {
        return apk_package_name;
    }

    public List<String> getErrorCodes() {
        return errorCodes;
    }

    @Override
    public String toString() {
        return "CaptchaResponse{" +
                "success=" + success +
                ", challenge_ts='" + challenge_ts + '\'' +
                ", apk_package_name='" + apk_package_name + '\'' +
                ", errorCodes=" + errorCodes +
                '}';
    }
}
