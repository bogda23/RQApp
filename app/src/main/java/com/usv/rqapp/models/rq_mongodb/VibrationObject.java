package com.usv.rqapp.models.rq_mongodb;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

public class VibrationObject implements Serializable {

    public static final String COUNTRY_NAME = "country_name";
    public static final String COUNTY_NAME = "county_name";
    public static final String ISO_CODE = "iso_code";

    @SerializedName("_id")
    private VibrationIDLocation vibrationID;

    @SerializedName("vibration_value")
    private Double vibrationValue;

    @SerializedName("last_update")
    private Timestamp lastUpdate;

    @SerializedName("country_name")
    private String countryName;

    @SerializedName("iso_code")
    private String isoCode;

    @SerializedName("county_name")
    private  String countyName;

    public VibrationObject() {
    }

    public VibrationObject(VibrationIDLocation vibrationID, Double vibrationValue, String countryName, String isoCode, String countyName) {
        this.vibrationID = vibrationID;
        this.vibrationValue = vibrationValue;
        this.countryName = countryName;
        this.isoCode = isoCode;
        this.countyName = countyName;
    }

    public VibrationIDLocation getVibrationID() {
        return vibrationID;
    }

    public void setVibrationID(VibrationIDLocation vibrationID) {
        this.vibrationID = vibrationID;
    }

    public Double getVibrationValue() {
        return vibrationValue;
    }

    public void setVibrationValue(Double vibrationValue) {
        this.vibrationValue = vibrationValue;
    }

    public Timestamp getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Timestamp lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getIsoCode() {
        return isoCode;
    }

    public void setIsoCode(String isoCode) {
        this.isoCode = isoCode;
    }

    public String getCountyName() {
        return countyName;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VibrationObject)) return false;
        VibrationObject that = (VibrationObject) o;
        return Objects.equals(getVibrationID(), that.getVibrationID()) &&
                Objects.equals(getVibrationValue(), that.getVibrationValue()) &&
                Objects.equals(getLastUpdate(), that.getLastUpdate()) &&
                Objects.equals(getCountryName(), that.getCountryName()) &&
                Objects.equals(getIsoCode(), that.getIsoCode()) &&
                Objects.equals(getCountyName(), that.getCountyName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getVibrationID(), getVibrationValue(), getLastUpdate(), getCountryName(), getIsoCode(), getCountyName());
    }

    @Override
    public String toString() {
        return "VibrationObject{" +
                "vibrationID=" + vibrationID +
                ", vibrationValue=" + vibrationValue +
                ", lastUpdate=" + lastUpdate +
                ", countryName='" + countryName + '\'' +
                ", isoCode='" + isoCode + '\'' +
                ", countyName='" + countyName + '\'' +
                '}';
    }
}
