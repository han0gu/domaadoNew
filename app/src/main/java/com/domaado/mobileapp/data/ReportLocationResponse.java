package com.domaado.mobileapp.data;

import java.io.Serializable;

/**
 * Created by jameshong on 2018. 5. 30..
 */

public class ReportLocationResponse extends ResponseBase  implements Serializable {

    public String[] fields = {};

    public ReportLocationResponse() {

    }

    public ReportLocationResponse(String _requestId, String _responseYn, String _message) {
        this.requestId = _requestId;
        this.responseYn = _responseYn;
        this.message = _message;
    }

    public ReportLocationResponse(int _seq, String _requestId, String _responseYn, String _message) {
        this.seq = _seq;
        this.requestId = _requestId;
        this.responseYn = _responseYn;
        this.message = _message;
    }

    public void set(String key, String value) {

    }

    @Override
    public String toString() {
        return "ReportLocationResponse{" +
                "seq=" + seq +
                ", requestId='" + requestId + '\'' +
                ", responseYn='" + responseYn + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
