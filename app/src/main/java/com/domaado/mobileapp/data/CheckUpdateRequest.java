package com.domaado.mobileapp.data;

import android.content.Context;

import com.google.firebase.iid.FirebaseInstanceId;
import com.domaado.mobileapp.Common;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by jameshong on 2018. 6. 1..
 */

public class CheckUpdateRequest extends RequestBase  implements Serializable {
    String appVersion;
    String fcmToken;

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
        this.setMobileNo(Common.getPhoneNumber(ctx));
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

    public HashMap<String, Object> getRequestParameterMap() {
        HashMap<String, Object> map = getBaseParameter();

        map.put("fcm_token", this.getFcmToken());
        map.put("mobile_no", this.getMobileNo());
        map.put("lat", this.getLat());
        map.put("lon", this.getLon());
        map.put("hashkey", this.getHashKey());

        return map;
    }

    @Override
    public String toString() {
        return "CheckUpdateRequest{" +
                "appVersion='" + appVersion + '\'' +
                ", fcmToken='" + fcmToken + '\'' +
                ", mobileNo='" + mobileNo + '\'' +
                ", requestId='" + requestId + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", requestType='" + requestType + '\'' +
                ", requestLocale='" + requestLocale + '\'' +
                ", deviceInfo=" + deviceInfo +
                '}';
    }
}
