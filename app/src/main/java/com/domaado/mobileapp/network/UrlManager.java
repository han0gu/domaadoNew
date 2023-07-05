package com.domaado.mobileapp.network;

import android.content.Context;

import androidx.annotation.NonNull;

import com.domaado.mobileapp.App;
import com.domaado.mobileapp.Constant;
import com.domaado.mobileapp.R;


/**
 * Created by jameshong on 2018. 5. 30..
 */

public class UrlManager {

    /**
     * 기본 서버 URL
     *
     * @param ctx
     * @return
     */
    public static String getServerUrl(Context ctx) {
        return App.isIsTEST() ? ctx.getResources().getString(R.string.url_api_site_test) : ctx.getResources().getString(R.string.url_api_site);
    }

    public static String getCheckUpdateAPI(Context ctx) {
        return ctx.getResources().getString(R.string.url_check_update);
    }

    public static String getPhotoProfileUpdateAPI(@NonNull Context ctx) {
        return ctx.getResources().getString(R.string.url_my_photo_update);
    }

    /**
     * 광고이미지 확인 PATH
     *
     * @param ctx
     * @return
     */
    // https://goodriver.co.kr/COM1026/_admin/_API/gs/intro.asp
//    public static String getIntroAdPath(Context ctx) {
//        return ctx.getResources().getString(R.string.url_intro_ad);
//    }
//
//    public static String getMemberJoin(Context ctx) {
//        return ctx.getResources().getString(R.string.url_member_join);
//    }

}
