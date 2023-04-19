package com.domaado.mobileapp.data;

import android.text.TextUtils;

import java.io.Serializable;

/**
 * Created by jameshong on 2018. 6. 20..
 */

public class QueryParams implements Serializable {
    String title;
    String message;
    String responseId;
    String action;

    double lat;
    double lon;
    String url;

    public QueryParams() {
    }

    public QueryParams(String title, String message, String responseId, String action, String lat, String lon, String url) {
        this.title = title;
        this.message = message;
        this.responseId = responseId;
        this.action = action;

        setLat(lat);
        setLon(lon);
        setUrl(url);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getResponseId() {
        return responseId;
    }

    public void setResponseId(String responseId) {
        this.responseId = responseId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = !TextUtils.isEmpty(lat) ? Double.parseDouble(lat) : 0;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = !TextUtils.isEmpty(lon) ? Double.parseDouble(lon) : 0;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "QueryParams{" +
                "title='" + title + '\'' +
                ", message='" + message + '\'' +
                ", responseId='" + responseId + '\'' +
                ", action='" + action + '\'' +
                ", lat=" + lat +
                ", lon=" + lon +
                ", url=" + url +
                '}';
    }
}
