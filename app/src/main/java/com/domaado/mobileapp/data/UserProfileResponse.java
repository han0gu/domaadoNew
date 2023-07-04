package com.domaado.mobileapp.data;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by jameshong on 2018. 6. 1..
 */
public class UserProfileResponse extends ResponseBase implements Serializable {

    public String[] OBJECTS_KEY = { "data" };
    public String[] fields = { "clientuser" };

    ClientUserEntry clientUserEntry;

    public UserProfileResponse() {
        clientUserEntry = new ClientUserEntry();
    }

    public ClientUserEntry getClientUserEntry() {
        return clientUserEntry;
    }

    public void setClientUserEntry(ClientUserEntry clientUserEntry) {
        this.clientUserEntry = clientUserEntry;
    }

    public void set(String key, Object value) {
        if(fields[0].equalsIgnoreCase(key)) {
            try {
                JSONObject obj = new JSONObject(String.valueOf(value));
                ClientUserEntry clientUserEntry = new ClientUserEntry();
                for(String field : clientUserEntry.fields) {
                    if(obj.has(field)) clientUserEntry.set(field, obj.get(field));
                }

                setClientUserEntry(clientUserEntry);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String toString() {
        return "UserProfileResponse{" +
                "seq=" + seq +
                ", requestId='" + requestId + '\'' +
                ", responseYn='" + responseYn + '\'' +
                ", message='" + message + '\'' +
                ", clientUserEntry=" + clientUserEntry.toString() +
                '}';
    }
}
