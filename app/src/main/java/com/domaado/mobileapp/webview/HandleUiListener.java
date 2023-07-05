package com.domaado.mobileapp.webview;

/**
 * Created by kbins(James Hong) on 2019,May,10
 */
public interface HandleUiListener {
    void setBackButton(boolean show);
    void setTitleText(String title);
    void sendShare(int target, String title, String message, String imageUrl, String linkUrl);
    void openCamera(String idx, String type);
    void onceCamera(String idx, String type, String seq);

    default void openKakaoLogin(String callback) {}
    default void callCallbackResponse(String callback) {}

    //
    default void savelogin(String loginid, String passwd, String callback) {}
    default void offlogin(String callback) {}
    default void autologin(String callback) {}
    default void onMedia(int type, String uploadUrl, String dataVal, String callback) {}
    default void selectCapture(String uploadUrl, String dataVal, String callback) {}

    default void getpushid(String callback) {}
}
