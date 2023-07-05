package com.domaado.mobileapp.data;

import android.content.Context;

import com.onesignal.OSDeviceState;
import com.onesignal.OneSignal;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jameshong on 2018. 6. 1..
 */

public class CheckUpdateRequest extends RequestBase  implements Serializable {

    public String[] OBJECTS_KEY = { "data" };
    public String[] fields = { "fcm_token", "lat", "lon", "push_id", "hash_key" };

    String fcmToken;
    String lat;
    String lon;

    String oneSignalPushId;

    String hashKey;

    public CheckUpdateRequest() {
    }

    public CheckUpdateRequest(Context ctx) {
        init(ctx);

        OSDeviceState osDeviceState = OneSignal.getDeviceState();
        assert osDeviceState != null;
        setOneSignalPushId(osDeviceState.getPushToken());
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public String getHashKey() {
        return hashKey;
    }

    public void setHashKey(String hashKey) {
        this.hashKey = hashKey;
    }

    public String getOneSignalPushId() {
        return this.oneSignalPushId;
    }

    public void setOneSignalPushId(String oneSignalPushId) {
        this.oneSignalPushId = oneSignalPushId;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public void setLat(double lat) {
        this.lat = String.valueOf(lat);
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public void setLon(double lon) {
        this.lon = String.valueOf(lon);
    }


    public HashMap<String, Object> getRequestParameterMap() {
        HashMap<String, Object> map = getBaseParameter();

        HashMap<String, Object> data = new HashMap<>();
        data.put(fields[0], getFcmToken());
        data.put(fields[1], getLat());
        data.put(fields[2], getLon());
        data.put(fields[3], getOneSignalPushId());
        data.put(fields[4], getHashKey());

        map.put(OBJECTS_KEY[0], data);


        return map;
    }

    public HashMap<String, String> getRequestParameterMapString() {
        HashMap<String, String> map = new HashMap<>();

        for (Map.Entry<String, Object> entry : getRequestParameterMap().entrySet()) {
            map.put(entry.getKey(), entry.getKey());
        }

        return map;
    }

    @Override
    public String toString() {
        return "CheckUpdateRequest{" +
                "fcmToken='" + fcmToken + '\'' +
                ", lat='" + lat + '\'' +
                ", lon='" + lon + '\'' +
                ", oneSignalPushId='" + oneSignalPushId + '\'' +
                ", hashKey='" + hashKey + '\'' +
                ", requestId='" + requestId + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", requestType='" + requestType + '\'' +
                ", requestLocale='" + requestLocale + '\'' +
                ", deviceInfo=" + deviceInfo.toString() +
                '}';
    }
}
