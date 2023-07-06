package com.domaado.mobileapp.data;

import android.text.TextUtils;

import com.domaado.mobileapp.Common;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by jameshong on 2018. 5. 30..
 */

public class PhotoResponse extends ResponseBase  implements Serializable {

    public String[] OBJECTS_KEY = { "data" };
    public String[] fields = { "photos" };

    ArrayList<PhotoEntry> photos;

    public PhotoResponse() {
        photos = new ArrayList<>();
    }

    public PhotoResponse(String _requestId, String _responseYn, ArrayList<PhotoEntry> _photos) {
        this.requestId = _requestId;
        this.responseYn = _responseYn;

        this.photos.clear();
        this.photos.addAll(_photos);
    }

    public ArrayList<PhotoEntry> getPhotos() {
        return photos;
    }

    public void setPhotos(ArrayList<PhotoEntry> photos) {
        this.photos.clear();
        this.photos.addAll(photos);
    }

    public void addPhotos(PhotoEntry photoEntry) {
        if(this.photos==null) this.photos = new ArrayList<>();
        this.photos.add(photoEntry);
    }

    public void set(String key, Object value) {
        if(fields[0].equalsIgnoreCase(key) && !TextUtils.isEmpty(Common.valueOf(value))) {
            try {
                JSONArray jsonArray = new JSONArray(Common.valueOf(value));
                for(int i=0; i<jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    PhotoEntry photoEntry = new PhotoEntry();
                    for(String field : photoEntry.fields) {
                        if(obj.has(field)) photoEntry.set(field, obj.get(field));
                    }
                    addPhotos(photoEntry);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String toString() {
        return "PhotoResponse{" +
                "photos=" + (photos!=null ? Arrays.toString(photos.toArray()) : "null") +
                ", seq=" + seq +
                ", requestId='" + requestId + '\'' +
                ", responseYn='" + responseYn + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
