package com.domaado.market.network;

import android.content.Context;

import com.domaado.market.R;


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
        return ctx.getResources().getString(R.string.url_site);
    }

    public static String getCheckUpdate(Context ctx) {
        return ctx.getResources().getString(R.string.url_check_update);
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
