package com.domaado.mobileapp.data;

import android.content.Context;
import android.text.TextUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;

import com.domaado.mobileapp.Common;
import com.domaado.mobileapp.Constant;
import com.domaado.mobileapp.network.SecureNetworkUtil;

/**
 * Created by jameshong on 2018. 5. 29..
 */
public class RequestBase implements Serializable {
    public String[] baseFields = { "request_id", "device_id", "request_type", "request_locale", "deviceinfo" };

    String requestId;
    String deviceId;
    String requestType;
    String requestLocale;

    DeviceInfo deviceInfo = new DeviceInfo();

    public void init(Context ctx) {
        this.requestId = String.valueOf(Common.getUniqueID());
        this.deviceId = Common.getDefaultUUID(ctx, UUID.randomUUID().toString());
        this.requestType = Constant.REQUEST_TYPE;
        this.requestLocale = Common.getConfig(ctx, Constant.CONFIG_USERLANGUAGE); //Common.getConfigForServer(ctx, Common.CONFIG_USERLANGUAGE));

        this.deviceInfo.setAppVersion(Common.getAppVersion(ctx));
    }

    public HashMap<String, Object> getBaseParameter() {
        HashMap<String, Object> map = new HashMap<>();

        map.put(baseFields[0], this.getRequestId());
        map.put(baseFields[1], this.getDeviceId());
        map.put(baseFields[2], this.getRequestType());
        map.put(baseFields[3], this.getRequestLocale());

        map.put(baseFields[4], getDeviceInfo().getRequestParameterMap());
//        map.putAll(deviceInfo.getRequestParameterMap());

        return map;
    }

    public String getRequestId() {
        return getNotNullString(requestId);
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getDeviceId() {
        return getNotNullString(deviceId);
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getRequestType() {
        return getNotNullString(requestType);
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getRequestLocale() {
        return getNotNullString(requestLocale);
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

    public String getDecString(String encString) {

        try {
            String decString = SecureNetworkUtil.getDecStringBase64(encString);
            return decString;
        } catch(Exception e) {
            e.printStackTrace();

            return encString;
        }
    }

    public String getEncString(String string) {
        try {
            string = SecureNetworkUtil.getEncStringBase64(string);
            return string;
        } catch(Exception e) {
            e.printStackTrace();
            return string;
        }
    }

    public String getNotNullString(String value) {
        if("null".equalsIgnoreCase(value) || TextUtils.isEmpty(value)) return "";
        else return value;
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
