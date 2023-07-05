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

    MemberEntry memberEntry;

    public UserProfileResponse() {
        memberEntry = new MemberEntry();
    }

    public MemberEntry getClientUserEntry() {
        return memberEntry;
    }

    public void setClientUserEntry(MemberEntry memberEntry) {
        this.memberEntry = memberEntry;
    }

    public void set(String key, Object value) {
        if(fields[0].equalsIgnoreCase(key)) {
            try {
                JSONObject obj = new JSONObject(String.valueOf(value));
                MemberEntry memberEntry = new MemberEntry();
                for(String field : memberEntry.fields) {
                    if(obj.has(field)) memberEntry.set(field, obj.get(field));
                }

                setClientUserEntry(memberEntry);

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
                ", clientUserEntry=" + memberEntry.toString() +
                '}';
    }
}
