package com.domaado.mobileapp.data;

import android.content.Context;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by jameshong on 2018. 6. 1..
 */
public class UserProfileRequest extends RequestBase  implements Serializable {

    public String[] OBJECTS_KEY = { "data" };
    public String[] fields = { "clientuser" };

    MemberEntry memberEntry;

    public UserProfileRequest() {
        memberEntry = new MemberEntry();
    }

    public UserProfileRequest(Context ctx) {
        init(ctx);
    }

    public HashMap<String, Object> getRequestParameterMap() {
        HashMap<String, Object> map = getBaseParameter();

        HashMap<String, Object> data = new HashMap<>();
        data.put(fields[0], getClientUserEntry().getRequestParameterMap());

        map.put(OBJECTS_KEY[0], data);

        return map;
    }

    public MemberEntry getClientUserEntry() {
        return memberEntry;
    }

    public void setClientUserEntry(MemberEntry memberEntry) {
        this.memberEntry = memberEntry;
    }

    @Override
    public String toString() {
        return "UserProfileRequest{" +
                "clientUserEntry=" + memberEntry.toString() +
                ", requestId='" + requestId + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", requestType='" + requestType + '\'' +
                ", requestLocale='" + requestLocale + '\'' +
                ", deviceInfo=" + deviceInfo.toString() +
                '}';
    }
}
