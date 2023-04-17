package com.domaado.market.data;

import android.content.Context;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by jameshong on 2018. 5. 30..
 */

public class CarPhotoRequest extends RequestBase  implements Serializable {

    public static final String ORDER_TYPE_ARRIVED   = "1";
    public static final String ORDER_TYPE_PARKED    = "2";
    public static final String ORDER_TYPE_DELIVERY  = "3";

    String responseId;
    String orderType;

    public CarPhotoRequest(Context ctx) {
        init(ctx);
    }

    public String getResponseId() {
        return responseId;
    }

    public void setResponseId(String responseId) {
        this.responseId = responseId;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public HashMap<String, Object> getRequestParameterMap() {
        HashMap<String, Object> map = getBaseParameter();

        map.put("response_id", this.getResponseId());
        map.put("order_type", this.getOrderType());

        return map;
    }

    @Override
    public String toString() {
        return "PhotoRequest{" +
                "responseId='" + responseId + '\'' +
                ", orderType='" + orderType + '\'' +
                ", requestId='" + requestId + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", requestType='" + requestType + '\'' +
                ", requestLocale='" + requestLocale + '\'' +
                ", deviceInfo=" + deviceInfo.toString() +
                '}';
    }
}
