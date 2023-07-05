package com.domaado.mobileapp.data;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.UUID;

import com.domaado.mobileapp.Common;
import com.domaado.mobileapp.Constant;
import com.domaado.mobileapp.widget.JsonUtils;
import com.domaado.mobileapp.widget.myLog;

/**
 * Created by jameshong on 2018. 6. 20..
 */

public class QueryParams implements Serializable {

    private String TAG = QueryParams.class.getSimpleName();

    public String[] fields = { "title", "message", "data", "action", "lat", "lon", "body", "serial_number", "url" };

    String serialNumber;
    String title;
    String message;
    String data;
    String action;
    String body;

    double lat;
    double lon;

    String url;

    public QueryParams() {
        this.serialNumber = UUID.randomUUID().toString();
    }

    public QueryParams(String title, String action, String data) {
        this.serialNumber = UUID.randomUUID().toString();
        this.title = title;
        this.message = "";
        this.data = data;
        this.action = action;
    }

    public QueryParams(String title, String message, String data, String action, String lat, String lon, String url) {
        this.serialNumber = UUID.randomUUID().toString();
        this.title = title;
        this.message = message;
        this.data = data;
        this.action = action;
        this.url = url;

        setLat(lat);
        setLon(lon);
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
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

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
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

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDataWithKey(String key) {
        String result = "";

        if(!TextUtils.isEmpty(getData())) {
            try {
                String data = new String(Common.getBase64decode(getData()), Constant.ENCODING);

                JSONObject obj = new JSONObject(data);
                if(obj.has(key)) {
                    result = obj.getString(key);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                myLog.e(TAG, "*** queryParams - JSONException: "+e.getLocalizedMessage());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                myLog.e(TAG, "*** queryParams - UnsupportedEncodingException: "+e.getLocalizedMessage());
            }
        }

        return result;
    }

    public String getJSONString() {

        JSONObject json = JsonUtils.mapToJson(getRequestParameterMap());

        return json.toString();
    }

    public QueryParams parseJSON(String json) {
        QueryParams q = new QueryParams();

        try {
            JSONObject obj = new JSONObject(json);

            for(String key : q.fields) {
                if(obj.has(key)) q.set(key, obj.get(key));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return q;
    }

    public String getDataToJSONEncoding() {
        return Common.getBase64encode(getJSONString());
    }

    public void set(String key, Object value) {
        if(value == null) return;

        if(fields[0].equalsIgnoreCase(key)) setTitle(String.valueOf(value));
        else if(fields[1].equalsIgnoreCase(key)) setMessage(String.valueOf(value));
        else if(fields[2].equalsIgnoreCase(key)) setData(String.valueOf(value));
        else if(fields[3].equalsIgnoreCase(key)) setAction(String.valueOf(value));
        else if(fields[4].equalsIgnoreCase(key)) setLat(String.valueOf(value));
        else if(fields[5].equalsIgnoreCase(key)) setLon(String.valueOf(value));
        else if(fields[6].equalsIgnoreCase(key)) setBody(String.valueOf(value));
        else if(fields[7].equalsIgnoreCase(key)) setSerialNumber(String.valueOf(value));
        else if(fields[8].equalsIgnoreCase(key)) setUrl(String.valueOf(value));
    }

    public Object get(String key ) {

        if(fields[0].equalsIgnoreCase(key)) return getTitle();
        else if(fields[1].equalsIgnoreCase(key)) return getMessage();
        else if(fields[2].equalsIgnoreCase(key)) return getData();
        else if(fields[3].equalsIgnoreCase(key)) return getAction();
        else if(fields[4].equalsIgnoreCase(key)) return getLat();
        else if(fields[5].equalsIgnoreCase(key)) return getLon();
        else if(fields[6].equalsIgnoreCase(key)) return getBody();
        else if(fields[7].equalsIgnoreCase(key)) return getSerialNumber();
        else if(fields[8].equalsIgnoreCase(key)) return getUrl();
        else return null;
    }

    public HashMap<String, Object> getRequestParameterMap() {
        HashMap<String, Object> map = new HashMap<>();

        map.put(fields[0], getTitle());
        map.put(fields[1], getMessage());
        map.put(fields[2], getData());
        map.put(fields[3], getAction());
        map.put(fields[4], getLat());
        map.put(fields[5], getLon());
        map.put(fields[6], getBody());
        map.put(fields[7], getSerialNumber());
        map.put(fields[8], getUrl());

        return map;
    }

    @Override
    public String toString() {
        return "QueryParams{" +
                "serialNumber=" + serialNumber +
                ", title='" + title + '\'' +
                ", message='" + message + '\'' +
                ", data='" + data + '\'' +
                ", action='" + action + '\'' +
                ", body='" + body + '\'' +
                ", lat=" + lat +
                ", lon=" + lon +
                ", url=" + url +
                '}';
    }
}
