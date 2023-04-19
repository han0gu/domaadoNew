package com.domaado.mobileapp;

/**
 * Created by JamesHong on 2023/04/19
 * Project domaadoNew
 */
public class SplashScreenTest extends SplashScreen {

    @Override
    public void setTestMode() {
        App.setIsTEST(true);
    }
}
