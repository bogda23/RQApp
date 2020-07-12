package com.usv.rqapp.models.rq_mongodb;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Objects;

public class VibrationIDLocation implements Serializable {

    @SerializedName("lat")
    private Double latitude;

    @SerializedName("lng")
    private Double longitude;


    public VibrationIDLocation(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VibrationIDLocation)) return false;
        VibrationIDLocation that = (VibrationIDLocation) o;
        return Objects.equals(getLatitude(), that.getLatitude()) &&
                Objects.equals(getLongitude(), that.getLongitude());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLatitude(), getLongitude());
    }

    @Override
    public String toString() {
        return "VibrationIDLocation{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
