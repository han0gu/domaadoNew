package com.domaado.mobileapp.webview;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;


import com.domaado.mobileapp.Common;
import com.domaado.mobileapp.R;
import com.domaado.mobileapp.data.CheckUpdateRequest;
import com.domaado.mobileapp.widget.CustomAlertDialog;
import com.domaado.mobileapp.widget.myLog;

import java.io.File;

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

    @JavascriptInterface
    public void openKakaoLogin(String callback) {
        if(handleUiListener!=null) handleUiListener.openKakaoLogin(callback);
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

    /**
     *
     * @param domain
     * @param channel
     */
    @JavascriptInterface
    public void openYoutube(String domain, String channel) {
        // https://www.youtube.com/@domaado

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String url = domain + File.separator + channel;

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setPackage("com.google.android.youtube");
                intent.setData(Uri.parse(url));

                try {
                    activity.startActivity(intent);
                } catch(ActivityNotFoundException e) {
                    e.printStackTrace();
                    openBrowser(url);
                }
            }
        });
    }

    /**
     *
     * @param domain
     * @param channel
     */
    @JavascriptInterface
    public void openInstagram(String domain, String channel) {
        // https://www.instagram.com/domado_official/

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String url = domain + File.separator + "_u" + channel;

                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setComponent(new ComponentName("com.instagram.android",
                        "com.instagram.android.activity.UrlHandlerActivity"));

                try {
                    activity.startActivity(intent);
                } catch(ActivityNotFoundException e) {
                    e.printStackTrace();
                    openBrowser(domain + File.separator + channel);
                }
            }
        });
    }

    @JavascriptInterface
    public void openBrowser(String url) {
        myLog.d(TAG, "*** openBroswer - open browser url: "+url);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Common.openBrowser(activity, url);
            }
        });
    }

    @JavascriptInterface
    public void shareImage(String mimetype, final String base64String, String title) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {

//                    myLog.e(TAG, "*** shareImage mimetype: "+mimetype);
//                    myLog.e(TAG, "*** shareImage base64String: "+base64String);
//                    myLog.e(TAG, "*** shareImage title: "+title);

                    String base64 = base64String;
                    String checkedMimeType = mimetype;

                    if(!TextUtils.isEmpty(base64String) && base64String.startsWith("data:")) {
                        String[] base64splits = base64String.split(",");
                        try {
                            checkedMimeType = base64splits[0].split(":")[1].split(";")[0];
                            myLog.e(TAG, "*** shareImage checkedMimeType: "+checkedMimeType);
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                        base64 = base64splits[1];
                    }

                    Bitmap bitmap = Common.getBase64decodeImage(base64);

                    myLog.e(TAG, "*** shareImage bitmap: "+(bitmap!=null ? bitmap.getByteCount() : "null"));

                    Uri uri = Common.getBitmapToUri(activity, bitmap, title);

                    myLog.e(TAG, "*** shareImage uri: "+(uri!=null ? uri.toString() : "null"));

                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    shareIntent.setType(checkedMimeType); // "image/jpeg"
                    activity.startActivity(Intent.createChooser(shareIntent, activity.getResources().getText(R.string.send_to_title)));
                } catch(Exception e) {
                    e.printStackTrace();
                    Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     *
     * @param callback JSON DATA
     */
    @JavascriptInterface
    public void getAppInfo(String callback) {
        if(handleUiListener!=null) handleUiListener.callCallbackResponse(callback);
    }
}
