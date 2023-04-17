package com.domaado.market.data;

import android.text.TextUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Locale;

/**
 * Created by jameshong on 2018. 5. 29..
 */

public class ResponseBase implements Serializable {
    public String[] fields = { "request_id", "response_yn", "message" };

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
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setBase(String key, String value) {
        if(fields[0].equals(key)) setRequestId(value);
        else if(fields[1].equals(key)) setResponseYn(value);
        else if(fields[2].equals(key)) setMessage(value);
    }

    @Override
    public String toString() {
        return "ResponseBase{" +
                "fields=" + Arrays.toString(fields) +
                ", seq=" + seq +
                ", requestId='" + requestId + '\'' +
                ", responseYn='" + responseYn + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
