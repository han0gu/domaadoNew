package com.domaado.mobileapp.data;

import android.content.Context;

import com.google.firebase.iid.FirebaseInstanceId;
import com.domaado.mobileapp.Common;
import com.onesignal.OSDeviceState;
import com.onesignal.OneSignal;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jameshong on 2018. 6. 1..
 */

public class CheckUpdateRequest extends RequestBase  implements Serializable {
    String appVersion;
    String fcmToken;
    String oneSignalPushId;

    String mobileNo;

    double lat;
    double lon;

    String hashKey;

    public CheckUpdateRequest() {
    }

    public CheckUpdateRequest(Context ctx) {
        init(ctx);

        //this.setFcmToken(Common.getSharedPreferencesString("fcm_token", ctx));
        this.setFcmToken(FirebaseInstanceId.getInstance().getToken());
        this.setMobileNo(""); //Common.getPhoneNumber(ctx));

        OSDeviceState osDeviceState = OneSignal.getDeviceState();
        assert osDeviceState != null;
        setOneSignalPushId(osDeviceState.getPushToken());
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
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

    public HashMap<String, Object> getRequestParameterMap() {
        HashMap<String, Object> map = getBaseParameter();

        map.put("fcm_token", this.getFcmToken());
//        map.put("mobile_no", this.getMobileNo());
        map.put("lat", this.getLat());
        map.put("lon", this.getLon());
        map.put("hashkey", this.getHashKey());
        map.put("push_id", this.getOneSignalPushId());

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
                "appVersion='" + appVersion + '\'' +
                ", fcmToken='" + fcmToken + '\'' +
                ", oneSignalPushId='" + oneSignalPushId + '\'' +
                ", requestId='" + requestId + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", requestType='" + requestType + '\'' +
                ", requestLocale='" + requestLocale + '\'' +
                ", deviceInfo=" + deviceInfo +
                '}';
    }
}
