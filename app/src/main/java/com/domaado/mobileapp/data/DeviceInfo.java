package com.domaado.mobileapp.data;

import android.os.Build;

import java.io.Serializable;

/**
 * Created by jameshong on 2018. 5. 30..
 */

public class DeviceInfo  implements Serializable {

    String deviceModel;
    String devicePlatform;
    String deviceVersion;
    String appVersion;

    public DeviceInfo() {
        setDeviceModel(Build.MODEL);
        setDeviceVersion(String.valueOf(Build.VERSION.RELEASE));
        setDevicePlatform("Android");
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getDevicePlatform() {
        return devicePlatform;
    }

    public void setDevicePlatform(String devicePlatform) {
        this.devicePlatform = devicePlatform;
    }

    public String getDeviceVersion() {
        return deviceVersion;
    }

    public void setDeviceVersion(String deviceVersion) {
        this.deviceVersion = deviceVersion;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    @Override
    public String toString() {
        return "DeviceInfo{" +
                "deviceModel='" + deviceModel + '\'' +
                ", devicePlatform='" + devicePlatform + '\'' +
                ", deviceVersion='" + deviceVersion + '\'' +
                ", appVersion='" + appVersion + '\'' +
                '}';
    }
}
