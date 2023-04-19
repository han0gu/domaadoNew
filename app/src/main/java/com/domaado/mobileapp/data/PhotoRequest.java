package com.domaado.mobileapp.data;

import android.content.Context;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by jameshong on 2018. 5. 30..
 */

public class PhotoRequest extends RequestBase  implements Serializable {
    String responseId;

    public PhotoRequest() {
    }

    public PhotoRequest(Context ctx) {
        init(ctx);
    }

    public String getResponseId() {
        return responseId;
    }

    public void setResponseId(String responseId) {
        this.responseId = responseId;
    }

    public HashMap<String, Object> getRequestParameterMap() {
        HashMap<String, Object> map = getBaseParameter();

        return map;
    }

    @Override
    public String toString() {
        return "PhotoRequest{" +
                "requestId='" + requestId + '\'' +
                ", responseId='" + responseId + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", requestType='" + requestType + '\'' +
                '}';
    }
}
