package com.domaado.mobileapp.data;

import android.text.TextUtils;

import com.domaado.mobileapp.Common;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Locale;

/**
 * Created by jameshong on 2018. 6. 1..
 */

public class CheckUpdateResponse extends ResponseBase implements Serializable {

    public String[] OBJECTS_KEY = { "data" };
    public String[] fields = { "continue_user_yn", "key", "iv", "access_token", "member" };

    String continueUserYn;
    String key;
    String iv;
    String accessToken;
    MemberEntry memberEntry;

    public CheckUpdateResponse() {
        memberEntry = new MemberEntry();
    }

    public String getKey() {
        return key;
    }

    public byte[] getKeyBytes() {
        return Common.hexToByteArray(getKey());
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getIv() {
        return iv;
    }

    public byte[] getIvBytes() {
        return Common.hexToByteArray(getIv());
    }

    public void setIv(String iv) {
        this.iv = iv;
    }

    public String getContinueUserYn() {
        return continueUserYn;
    }

    public void setContinueUserYn(String continueUserYn) {
        this.continueUserYn = continueUserYn;
    }

    public MemberEntry getMemberEntry() {
        return memberEntry;
    }

    public void setMemberEntry(MemberEntry memberEntry) {
        this.memberEntry = memberEntry;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void set(String key, Object value) {
        if(fields[0].equalsIgnoreCase(key)) setContinueUserYn(Common.valueOf(value));
        else if(fields[1].equalsIgnoreCase(key)) setKey(Common.valueOf(value));
        else if(fields[2].equalsIgnoreCase(key)) setIv(Common.valueOf(value));
        else if(fields[3].equalsIgnoreCase(key)) setAccessToken(Common.valueOf(value));
        else if(fields[4].equalsIgnoreCase(key) && !TextUtils.isEmpty(Common.valueOf(value))) {
            try {
                JSONObject obj = new JSONObject(Common.valueOf(value));
                MemberEntry memberEntry = new MemberEntry();
                for(String field : memberEntry.fields) {
                    if(obj.has(field)) memberEntry.set(field, obj.get(field));
                }

                setMemberEntry(memberEntry);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String toString() {
        return "CheckUpdateResponse{" +
                "continueUserYn='" + continueUserYn + '\'' +
                ", key='" + key + '\'' +
                ", iv='" + iv + '\'' +
                ", accessToken='" + accessToken + '\'' +
                ", memberEntry=" + memberEntry.toString() +
                ", seq=" + seq +
                ", requestId='" + requestId + '\'' +
                ", responseYn='" + responseYn + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
