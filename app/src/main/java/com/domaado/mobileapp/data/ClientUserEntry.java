package com.domaado.mobileapp.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by jameshong on 2018. 5. 30..
 */
public class ClientUserEntry extends EntryBase implements Serializable {

    public String[] fields = { "user_seq", "usr_nick_name", "email", "mobile_no", "user_yn", "user_profile_img", "user_type_nm", "country_code", "topic" };

    String userSeq;
    String usrNickName;
    String email;
    String mobileNo;
    String userYn;
    String userProfileImg;
    String userTypeNm;
    String countryCode;
    String topic;

    Bitmap userProfileBitmap;
    Uri userProfilePhotoUri;

    public ClientUserEntry() {
        this.userProfileBitmap = null;
    }

    public void set(String key, Object value) {
        if(value == null) return;

        if(fields[0].equalsIgnoreCase(key)) setUserSeq(String.valueOf(value));
        else if(fields[1].equalsIgnoreCase(key)) setUsrNickName(String.valueOf(value));
        else if(fields[2].equalsIgnoreCase(key)) setEmail(getDecString(String.valueOf(value)));
        else if(fields[3].equalsIgnoreCase(key)) setMobileNo(getDecString(String.valueOf(value)));
        else if(fields[4].equalsIgnoreCase(key)) setUserYn(String.valueOf(value));
        else if(fields[5].equalsIgnoreCase(key)) setUserProfileImg(String.valueOf(value));
        else if(fields[6].equalsIgnoreCase(key)) setUserTypeNm(String.valueOf(value));
        else if(fields[7].equalsIgnoreCase(key)) setCountryCode(String.valueOf(value));
        else if(fields[8].equalsIgnoreCase(key)) setTopic(String.valueOf(value));
    }

    public Object get(String key ) {

        if(fields[0].equalsIgnoreCase(key)) return getUserSeq();
        else if(fields[1].equalsIgnoreCase(key)) return getUsrNickName();
        else if(fields[2].equalsIgnoreCase(key)) return getEmail();
        else if(fields[3].equalsIgnoreCase(key)) return getMobileNo();
        else if(fields[4].equalsIgnoreCase(key)) return getUserYn();
        else if(fields[5].equalsIgnoreCase(key)) return getUserProfileImg();
        else if(fields[6].equalsIgnoreCase(key)) return getUserTypeNm();
        else if(fields[7].equalsIgnoreCase(key)) return getCountryCode();
        else if(fields[8].equalsIgnoreCase(key)) return getTopic();
        else return null;
    }

    public HashMap<String, Object> getRequestParameterMap() {
        HashMap<String, Object> map = new HashMap<>();

        map.put(fields[0], getUserSeq());
        map.put(fields[1], getUsrNickName());
        map.put(fields[2], getEncString(getEmail()));
        map.put(fields[3], getEncString(getMobileNo()));
        map.put(fields[4], getUserYn());
        map.put(fields[5], getUserProfileImg());
        map.put(fields[6], getUserTypeNm());
        map.put(fields[7], getCountryCode());
        map.put(fields[8], getTopic());

        return map;
    }

    public String getUserSeq() {
        return userSeq;
    }

    public void setUserSeq(String userSeq) {
        this.userSeq = userSeq;
    }

    public String getUsrNickName() {
        return getNotNullString(usrNickName);
    }

    public void setUsrNickName(String usrNickName) {
        this.usrNickName = usrNickName;
    }

    public String getEmail() {
        return getNotNullString(email);
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getUserYn() {
        return userYn;
    }

    public void setUserYn(String userYn) {
        this.userYn = userYn;
    }

    public String getUserProfileImg() {
        return userProfileImg;
    }

    public void setUserProfileImg(String userProfileImg) {
        this.userProfileImg = userProfileImg;
    }

    public String getUserTypeNm() {
        return userTypeNm;
    }

    public void setUserTypeNm(String userTypeNm) {
        this.userTypeNm = userTypeNm;
    }

    public Bitmap getUserProfileBitmap() {
        return userProfileBitmap;
    }

    public void setUserProfileBitmap(Bitmap userProfileBitmap) {
        this.userProfileBitmap = userProfileBitmap;
    }

    public void setUserProfileBitmap(InputStream inputStream) {
        try {
            this.userProfileBitmap = BitmapFactory.decodeStream(inputStream);
        } catch(Exception e) {}
    }

    public Uri getUserProfilePhotoUri() {
        return userProfilePhotoUri;
    }

    public void setUserProfilePhotoUri(Uri userProfilePhotoUri) {
        this.userProfilePhotoUri = userProfilePhotoUri;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    @Override
    public String toString() {
        return "ClientUserEntry{" +
                "userSeq='" + userSeq + '\'' +
                ", usrNickName='" + usrNickName + '\'' +
                ", email='" + email + '\'' +
                ", mobileNo='" + mobileNo + '\'' +
                ", userYn='" + userYn + '\'' +
                ", userProfileImg='" + userProfileImg + '\'' +
                ", userTypeNm='" + userTypeNm + '\'' +
                ", countryCode='" + countryCode + '\'' +
                ", topic='" + topic + '\'' +
                '}';
    }
}
