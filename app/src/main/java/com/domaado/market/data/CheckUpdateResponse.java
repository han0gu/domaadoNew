package com.domaado.market.data;

import android.text.TextUtils;

import java.io.Serializable;
import java.util.Locale;

/**
 * Created by jameshong on 2018. 6. 1..
 */

public class CheckUpdateResponse extends ResponseBase implements Serializable {

    public String[] fields = { "recommender_yn", "member_yn", "update_yn", "driver_grade", "call_status", "response_id", "customer_lat", "customer_lon", "callcenter_tel" };

    String recommenderYn;
    String memberYn;
    String updateYn;

    String driverGrade;

    String callStatus;
    String responseId;

    String customerLat;
    String customerLon;

    String callcenterTel;

    public CheckUpdateResponse() {

    }

    public CheckUpdateResponse(String _requestId, String _responseYn, String _message, String _recommenderYn, String _memberYn, String _updateYn, String _driverGrade) {
        this.requestId = _requestId;
        this.responseYn = _responseYn;
        this.message = _message;
        this.recommenderYn = _recommenderYn;
        this.memberYn = _memberYn;
        this.updateYn = _updateYn;
        this.driverGrade = _driverGrade;
    }

    public String getRecommenderYn() {
        if(!TextUtils.isEmpty(this.recommenderYn)) return this.recommenderYn.toUpperCase(Locale.getDefault());
        else return recommenderYn;
    }

    public void setRecommenderYn(String recommenderYn) {
        this.recommenderYn = recommenderYn;
    }

    public String getMemberYn() {
        if(!TextUtils.isEmpty(this.memberYn)) return this.memberYn.toUpperCase(Locale.getDefault());
        else return memberYn;
    }

    public void setMemberYn(String memberYn) {
        this.memberYn = memberYn;
    }

    public String getUpdateYn() {
        return updateYn;
    }

    public void setUpdateYn(String updateYn) {
        this.updateYn = updateYn;
    }

    public String getCallStatus() {
        return callStatus;
    }

    public void setCallStatus(String callStatus) {
        this.callStatus = callStatus;
    }

    public String getResponseId() {
        return responseId;
    }

    public void setResponseId(String responseId) {
        this.responseId = responseId;
    }

    public String getDriverGrade() {
        return driverGrade;
    }

    public void setDriverGrade(String driverGrade) {
        this.driverGrade = driverGrade;
    }

    public String getCustomerLat() {
        return customerLat;
    }

    public void setCustomerLat(String customerLat) {
        this.customerLat = customerLat;
    }

    public String getCustomerLon() {
        return customerLon;
    }

    public void setCustomerLon(String customerLon) {
        this.customerLon = customerLon;
    }

    public String getCallcenterTel() {
        return callcenterTel;
    }

    public void setCallcenterTel(String callcenterTel) {
        this.callcenterTel = callcenterTel;
    }

    public void set(String key, String value) {
        if(fields[0].equalsIgnoreCase(key)) setRecommenderYn(value);
        else if(fields[1].equalsIgnoreCase(key)) setMemberYn(value);
        else if(fields[2].equalsIgnoreCase(key)) setUpdateYn(value);
        else if(fields[3].equalsIgnoreCase(key)) setDriverGrade(value);
        else if(fields[4].equalsIgnoreCase(key)) setCallStatus(value);
        else if(fields[5].equalsIgnoreCase(key)) setResponseId(value);
        else if(fields[6].equalsIgnoreCase(key)) setCustomerLat(value);
        else if(fields[7].equalsIgnoreCase(key)) setCustomerLon(value);
        else if(fields[8].equalsIgnoreCase(key)) setCallcenterTel(value);
    }

    @Override
    public String toString() {
        return "CheckUpdateResponse{" +
                "recommenderYn='" + recommenderYn + '\'' +
                ", memberYn='" + memberYn + '\'' +
                ", updateYn='" + updateYn + '\'' +
                ", driverGrade='" + driverGrade + '\'' +
                ", callStatus='" + callStatus + '\'' +
                ", responseId='" + responseId + '\'' +
                ", customerLat='" + customerLat + '\'' +
                ", customerLon='" + customerLon + '\'' +
                ", callCenterTel='" + callcenterTel + '\'' +
                ", seq=" + seq +
                ", requestId='" + requestId + '\'' +
                ", responseYn='" + responseYn + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
