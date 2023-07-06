package com.domaado.mobileapp;

import android.widget.TextView;

import com.domaado.mobileapp.widget.myLog;

/**
 * Created by JamesHong on 2023/04/19
 * Project domaadoNew
 */
public class SplashScreenTest extends SplashScreen {

    @Override
    public void setTestMode() {
        App.setIsTEST(true);
    }

    @Override
    public boolean checkDebug() {
        String version = Common.getAppVersion(this);
        myLog.debugMode = true;

        ((TextView)findViewById(R.id.intro_version)).setText(version+"T");

        return myLog.debugMode;
    }
}
