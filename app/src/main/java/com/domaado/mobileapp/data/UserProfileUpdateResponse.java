package com.domaado.mobileapp.data;

import java.io.Serializable;

/**
 * Created by jameshong on 2018. 6. 1..
 */
public class UserProfileUpdateResponse extends ResponseBase implements Serializable {

    public String[] OBJECTS_KEY = { "data" };
    public String[] fields = { "result_code" };

    ResultCode resultCode;

    public UserProfileUpdateResponse() {
    }

    public ResultCode getResultCode() {
        return resultCode;
    }

    public void setResultCode(ResultCode resultCode) {
        this.resultCode = resultCode;
    }

    public void set(String key, Object value) {
        if(fields[0].equalsIgnoreCase(key)) setResultCode(ResultCode.valueOf(String.valueOf(value)));

    }

    @Override
    public String toString() {
        return "UserProfileUpdateResponse{" +
                "resultCode=" + (resultCode!=null ? resultCode.getValue() : "null") +
                ", seq=" + seq +
                ", requestId='" + requestId + '\'' +
                ", responseYn='" + responseYn + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
