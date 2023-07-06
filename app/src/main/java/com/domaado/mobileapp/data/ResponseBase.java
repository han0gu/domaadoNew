package com.domaado.mobileapp.data;

import android.text.TextUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;

import com.domaado.mobileapp.network.SecureNetworkUtil;

/**
 * Created by jameshong on 2018. 5. 29..
 */

public class ResponseBase implements Serializable {
    public String[] baseFields = { "request_id", "response_yn", "message" };

    int seq;
    String requestId;
    String responseYn;
    String message;

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getResponseYn() {
        if(!TextUtils.isEmpty(this.responseYn)) return this.responseYn.toUpperCase(Locale.getDefault());
        else return responseYn;
    }

    public void setResponseYn(String responseYn) {
        this.responseYn = responseYn;
    }

    public String getMessage() {
        return getNotNullString(message);
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setBase(String key, String value) {
        if(baseFields[0].equals(key)) setRequestId(value);
        else if(baseFields[1].equals(key)) setResponseYn(value);
        else if(baseFields[2].equals(key)) setMessage(value);
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

    //"request_id", "response_yn", "message"
    public HashMap<String, Object> getBaseParameter() {
        HashMap<String, Object> map = new HashMap<>();

        map.put(baseFields[0], this.getRequestId());
        map.put(baseFields[1], this.getResponseYn());
        map.put(baseFields[2], this.getMessage());

        return map;
    }

    @Override
    public String toString() {
        return "ResponseBase{" +
                ", seq=" + seq +
                ", requestId='" + requestId + '\'' +
                ", responseYn='" + responseYn + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
