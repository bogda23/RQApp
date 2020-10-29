package com.usv.rqapp.models.rq_mongodb;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class BaseVibrations implements Serializable {

    @SerializedName("list")
    private List<VibrationObject> vibrationObjects;

    @SerializedName("error")
    private String errorMessage;


    public List<VibrationObject> getVibrationObjects() {
        return vibrationObjects;
    }

    public void setVibrationObjects(List<VibrationObject> vibrationObjects) {
        this.vibrationObjects = vibrationObjects;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseVibrations)) return false;
        BaseVibrations that = (BaseVibrations) o;
        return Objects.equals(getVibrationObjects(), that.getVibrationObjects()) &&
                Objects.equals(getErrorMessage(), that.getErrorMessage());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getVibrationObjects(), getErrorMessage());
    }

    @Override
    public String toString() {
        return "BaseVibrations{" +
                "vibrationObjects=" + vibrationObjects +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
