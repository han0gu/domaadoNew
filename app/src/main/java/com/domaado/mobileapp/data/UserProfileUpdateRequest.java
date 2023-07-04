package com.domaado.mobileapp.data;

import android.content.Context;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by jameshong on 2018. 6. 1..
 */
public class UserProfileUpdateRequest extends RequestBase  implements Serializable {

    public String[] OBJECTS_KEY = { "data" };
    public String[] fields = { "clientuser", "photo" };

    ClientUserEntry clientUserEntry;
    PhotoEntry photoEntry;

    public UserProfileUpdateRequest() {
        clientUserEntry = new ClientUserEntry();
        photoEntry = new PhotoEntry();
    }

    public UserProfileUpdateRequest(Context ctx) {
        init(ctx);
    }

    public HashMap<String, Object> getRequestParameterMap() {
        HashMap<String, Object> map = getBaseParameter();

        HashMap<String, Object> data = new HashMap<>();
        data.put(fields[0], getClientUserEntry().getRequestParameterMap());
        data.put(fields[1], getPhotoEntry().getRequestParameterMap());

        map.put(OBJECTS_KEY[0], data);

        return map;
    }

    public ClientUserEntry getClientUserEntry() {
        return clientUserEntry;
    }

    public void setClientUserEntry(ClientUserEntry clientUserEntry) {
        this.clientUserEntry = clientUserEntry;
    }

    public PhotoEntry getPhotoEntry() {
        return photoEntry;
    }

    public void setPhotoEntry(PhotoEntry photoEntry) {
        this.photoEntry = photoEntry;
    }

    @Override
    public String toString() {
        return "UserProfileUpdateRequest{" +
                "clientUserEntry=" + clientUserEntry.toString() +
                ", photoEntry=" + photoEntry.toString() +
                ", requestId='" + requestId + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", requestType='" + requestType + '\'' +
                ", requestLocale='" + requestLocale + '\'' +
                ", deviceInfo=" + deviceInfo.toString() +
                '}';
    }
}
