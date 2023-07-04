package com.domaado.mobileapp.data;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.domaado.mobileapp.Common;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by jameshong on 2018. 5. 30..
 */

public class PhotoEntry extends EntryBase implements Serializable {
    public String[] fields = { "photo_idx", "photo_type", "photo_name", "photo_url", "photo_data", "user_idx" };

    String photoIdx;
    String photoType;
    String photoName;
    String photoUrl;
    String photoData;
    String userIdx;

    public String getPhotoIdx() {
        return photoIdx;
    }

    public void setPhotoIdx(String photoIdx) {
        this.photoIdx = photoIdx;
    }

    public String getPhotoType() {
        return photoType;
    }

    public void setPhotoType(String photoType) {
        this.photoType = photoType;
    }

    public String getPhotoName() {
        return photoName;
    }

    public void setPhotoName(String photoName) {
        this.photoName = photoName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getPhotoData() {
        return photoData;
    }

    public String getPhotoDataLog() {
        if(!TextUtils.isEmpty(photoData) && photoData.length()>80) {
            return photoData.substring(80);
        } else {
            return photoData;
        }
    }

    public void setPhotoData(String photoData) {
        this.photoData = photoData;
    }

    public void setPhotoData(Bitmap image) {
        try {
            this.photoData = Common.getBase64encodeImage(image);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public Bitmap getPhotoBitmap() {
        Bitmap bitmap = Common.getBase64decodeImage(photoData);

        return bitmap;
    }

    public String getUserIdx() {
        return userIdx;
    }

    public void setUserIdx(String userIdx) {
        this.userIdx = userIdx;
    }

    public HashMap<String, Object> getRequestParameterMap() {
        HashMap<String, Object> map = new HashMap<>();

        map.put(fields[0], getPhotoIdx());
        map.put(fields[1], getPhotoType());
        map.put(fields[2], getPhotoName());
        map.put(fields[3], getPhotoUrl());
        map.put(fields[4], getPhotoData());
        map.put(fields[5], getUserIdx());

        return map;
    }

    public void set(String key, Object value) {
        if(fields[0].equalsIgnoreCase(key)) setPhotoIdx(String.valueOf(value));
        else if(fields[1].equalsIgnoreCase(key)) setPhotoType(String.valueOf(value));
        else if(fields[2].equalsIgnoreCase(key)) setPhotoName(String.valueOf(value));
        else if(fields[3].equalsIgnoreCase(key)) setPhotoUrl(String.valueOf(value));
        else if(fields[4].equalsIgnoreCase(key)) setPhotoData(String.valueOf(value));
        else if(fields[5].equalsIgnoreCase(key)) setUserIdx(String.valueOf(value));
    }

    public Object get(String key) {
        if(fields[0].equalsIgnoreCase(key)) return getPhotoIdx();
        else if(fields[1].equalsIgnoreCase(key)) return getPhotoType();
        else if(fields[2].equalsIgnoreCase(key)) return getPhotoName();
        else if(fields[3].equalsIgnoreCase(key)) return getPhotoUrl();
        else if(fields[4].equalsIgnoreCase(key)) return getPhotoData();
        else if(fields[5].equalsIgnoreCase(key)) return getUserIdx();
        else return null;
    }

    @Override
    public String toString() {
        return "PhotoEntry{" +
                "photoIdx='" + photoIdx + '\'' +
                ", photoType='" + photoType + '\'' +
                ", photoName='" + photoName + '\'' +
                ", photoUrl='" + photoUrl + '\'' +
                ", userIdx='" + userIdx + '\'' +
                ", photoData='" + getPhotoDataLog() + '\'' +
                '}';
    }
}
