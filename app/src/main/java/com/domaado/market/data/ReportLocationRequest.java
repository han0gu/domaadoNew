package com.domaado.market.data;

import android.content.Context;

import java.io.Serializable;
import java.util.HashMap;

public class ReportLocationRequest extends RequestBase implements Serializable {
    double driverLat;
    double driverLon;

    public ReportLocationRequest(Context ctx) {
        init(ctx);
    }

    public HashMap<String, Object> getRequestParameterMap() {
        HashMap<String, Object> map = getBaseParameter();

        map.put("user_lat", getDriverLat());
        map.put("user_lon", getDriverLon());

        return map;
    }

    public double getDriverLat() {
        return driverLat;
    }

    public void setDriverLat(double driverLat) {
        this.driverLat = driverLat;
    }

    public double getDriverLon() {
        return driverLon;
    }

    public void setDriverLon(double driverLon) {
        this.driverLon = driverLon;
    }

    @Override
    public String toString() {
        return "ReportLocationRequest{" +
                "driver_lat=" + driverLat +
                ", driver_lon=" + driverLon +
                ", requestId='" + requestId + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", requestType='" + requestType + '\'' +
                ", requestLocale='" + requestLocale + '\'' +
                ", deviceInfo=" + deviceInfo +
                '}';
    }
}
