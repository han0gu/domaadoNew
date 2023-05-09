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
}
