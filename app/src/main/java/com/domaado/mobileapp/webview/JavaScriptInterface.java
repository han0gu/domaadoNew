package com.domaado.mobileapp.webview;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;


import com.domaado.mobileapp.R;
import com.domaado.mobileapp.widget.CustomAlertDialog;
import com.domaado.mobileapp.widget.myLog;

/**
 * Created by HongEuiChan on 2017. 9. 11..
 *
 * Webview용 Javascript interface
 */

public class JavaScriptInterface {

    String TAG = "JavaScriptInterface";

    public Activity activity;
    public WebView webView;
    public HandleUiListener handleUiListener;

    public JavaScriptInterface(Activity activity, WebView webView, HandleUiListener handleUiListener) {
        this.activity = activity;
        this.webView = webView;
        this.handleUiListener = handleUiListener;
    }

    @JavascriptInterface
    public void closeButton() {
        if(activity!=null) {
            activity.finish();
        }
    }

    @JavascriptInterface
    public void showBackButton() {
        if(handleUiListener!=null) handleUiListener.setBackButton(true);
    }

    @JavascriptInterface
    public void hideBackButton() {
        if(handleUiListener!=null) handleUiListener.setBackButton(false);
    }

    @JavascriptInterface
    public void setTitleText(String title) {
        if(handleUiListener!=null) handleUiListener.setTitleText(title);
    }

    @JavascriptInterface
    public void showMessage(final String message) {

        alertMessage(activity.getResources().getString(R.string.app_name), message, activity.getString(R.string.btn_neutral), "", false, null);

    }

    @JavascriptInterface
    public void showMessage(final String message, String okString, String noString) {

        alertMessage(activity.getResources().getString(R.string.app_name), message, okString, noString,true, null);

    }

    @JavascriptInterface
    public void openCamera(String idx, String type) {
        if(handleUiListener!=null) handleUiListener.openCamera(idx, type);
    }

    @JavascriptInterface
    public void onceCamera(String idx, String type, String seq) {
        if(handleUiListener!=null) handleUiListener.onceCamera(idx, type, seq);
    }

    @JavascriptInterface
    public void sendShare(int target, String title, String message, String imageUrl, String link) {
        if(handleUiListener!=null) handleUiListener.sendShare(target, title, message, imageUrl, link);
    }

    private void alertMessage(final String title, final String message, final String okString, final String noString, final boolean isCancel, final Handler handler) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    final CustomAlertDialog mesgBox = new CustomAlertDialog(activity);
                    myLog.d(TAG, "Create alterDialog box!");

                    mesgBox.setMTitle(title); // 팝업 타이틀
                    mesgBox.setMessage(message); // 팝업 내용
                    mesgBox.setCancelable(false);

                    if (isCancel) {
                        mesgBox.setYesButton((isCancel ? activity.getString(R.string.btn_yes) : activity.getString(R.string.btn_neutral)), new View.OnClickListener() {
                            public void onClick(View v) {

                                if (mesgBox != null) {

                                    mesgBox.dismiss();

                                    if (handler != null) {
                                        handler.sendEmptyMessage(0);
                                    }
                                }
                                return;
                            }

                        });

                        mesgBox.setNoButton(activity.getString(R.string.btn_no), new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                if (mesgBox != null) {

                                    mesgBox.dismiss();

                                    if (handler != null) {
                                        handler.sendEmptyMessage(1);
                                    }
                                }
                            }
                        });
                    } else {
                        mesgBox.setCloseButton(okString, new View.OnClickListener() {
                            public void onClick(View v) {

                                if (mesgBox != null) {

                                    mesgBox.dismiss();

                                    if (handler != null) {
                                        handler.sendEmptyMessage(0);
                                    }
                                }
                                return;
                            }

                        });
                    }

                    mesgBox.setOnKeyListener( // 백버튼을 눌렀을 때
                            new AlertDialog.OnKeyListener() {
                                @Override
                                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                                        dialog.dismiss();
                                        dialog = null;
                                    }
                                    return false;
                                }
                            });

                    mesgBox.setCancelable(false);

                    if (mesgBox != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            mesgBox.create();
                        }

                        mesgBox.show();
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }
}
