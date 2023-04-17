package com.domaado.market.data;

import android.content.Context;


import com.domaado.market.Common;
import com.domaado.market.Constant;

import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by jameshong on 2018. 5. 29..
 */

public class RequestBase implements Serializable {
    String requestId;
    String deviceId;
    String requestType;
    String requestLocale;

    DeviceInfo deviceInfo;

    public void init(Context ctx) {
        this.requestId = String.valueOf(Common.getUniqueID());
        this.deviceId = Common.getDefaultUUID(ctx, UUID.randomUUID().toString());
        this.requestType = Constant.REQUEST_TYPE;
        this.requestLocale = Common.getConfig(ctx, Constant.CONFIG_USERLANGUAGE); //Common.getConfigForServer(ctx, Common.CONFIG_USERLANGUAGE));

        this.deviceInfo = new DeviceInfo();
        this.deviceInfo.setAppVersion(Common.getAppVersion(ctx));

    }

    public HashMap<String, Object> getBaseParameter() {
        HashMap<String, Object> map = new HashMap<>();

        map.put("request_id", this.getRequestId());
        map.put("device_id", this.getDeviceId());
        map.put("request_type", this.getRequestType());
        map.put("request_locale", this.getRequestLocale());

        map.put("device_model", this.getDeviceInfo().getDeviceModel());
        map.put("device_platform", this.getDeviceInfo().getDevicePlatform());
        map.put("device_version", this.getDeviceInfo().getDeviceVersion());
        map.put("app_version", this.getDeviceInfo().getAppVersion());

        return map;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getRequestLocale() {
        return requestLocale;
    }

    public void setRequestLocale(String requestLocale) {
        this.requestLocale = requestLocale;
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    @Override
    public String toString() {
        return "RequestBase{" +
                "requestId='" + requestId + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", requestType='" + requestType + '\'' +
                ", requestLocale='" + requestLocale + '\'' +
                ", deviceInfo=" + deviceInfo.toString() +
                '}';
    }
}
