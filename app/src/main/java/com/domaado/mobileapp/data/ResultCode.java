package com.domaado.mobileapp.data;

import com.domaado.mobileapp.R;

/**
 * S01	성공
 * E01  오류
 */
public enum ResultCode {
    BLANK(""),
    S01("S01"),
    E01("E01");

    final private String resultCode;

    ResultCode(String s) {
        resultCode = s;
    }

    public String getValue() {
        return resultCode;
    }

    public int getMessage() {
        switch(this) {
            case S01:
                return R.string.result_code_S01;
            case E01:
            default:
                return R.string.result_code_E01;
        }
    }
}
