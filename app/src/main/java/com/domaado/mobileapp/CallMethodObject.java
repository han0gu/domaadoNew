package com.domaado.mobileapp;

import java.util.concurrent.Callable;

/**
 * 메소드 버스 클래스
 *
 * Created by kbins(James Hong) on 2018,October,22
 */
public class CallMethodObject implements Callable<Object> {
    public Object value;
    public boolean success;

    public CallMethodObject() {
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return this.value;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public Object call() throws Exception {
        return this.value;
    }
}
