package com.domaado.mobileapp.data;

import android.os.Build;

import java.io.Serializable;
import java.util.HashMap;

import com.domaado.mobileapp.Common;

/**
 * Created by jameshong on 2018. 5. 30..
 */
public class DeviceInfo extends EntryBase implements Serializable {

    public String[] fields = { "device_model", "device_platform", "device_version", "app_version" };

    String deviceModel;
    String devicePlatform;
    String deviceVersion;
    String appVersion;

    public DeviceInfo() {
        setDeviceModel(Build.MODEL);
        setDeviceVersion(String.valueOf(Build.VERSION.RELEASE));
        setDevicePlatform("Android");
    }

    public void set(String key, Object value) {
        if(value == null) return;

        if(fields[0].equalsIgnoreCase(key)) setDeviceModel(Common.valueOf(value));
        else if(fields[1].equalsIgnoreCase(key)) setDevicePlatform(Common.valueOf(value));
        else if(fields[2].equalsIgnoreCase(key)) setDeviceVersion(Common.valueOf(value));
        else if(fields[3].equalsIgnoreCase(key)) setAppVersion(Common.valueOf(value));
    }

    public Object get(String key ) {

        if(fields[0].equalsIgnoreCase(key)) return getDeviceModel();
        else if(fields[1].equalsIgnoreCase(key)) return getDevicePlatform();
        else if(fields[2].equalsIgnoreCase(key)) return getDeviceVersion();
        else if(fields[3].equalsIgnoreCase(key)) return getAppVersion();
        else return null;
    }

    public HashMap<String, Object> getRequestParameterMap() {
        HashMap<String, Object> map = new HashMap<>();

        map.put(fields[0], getDeviceModel());
        map.put(fields[1], getDevicePlatform());
        map.put(fields[2], getDeviceVersion());
        map.put(fields[3], getAppVersion());

        return map;
    }

    public String getDeviceModel() {
        return getNotNullString(deviceModel);
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getDevicePlatform() {
        return getNotNullString(devicePlatform);
    }

    public void setDevicePlatform(String devicePlatform) {
        this.devicePlatform = devicePlatform;
    }

    public String getDeviceVersion() {
        return getNotNullString(deviceVersion);
    }

    public void setDeviceVersion(String deviceVersion) {
        this.deviceVersion = deviceVersion;
    }

    public String getAppVersion() {
        return getNotNullString(appVersion);
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
