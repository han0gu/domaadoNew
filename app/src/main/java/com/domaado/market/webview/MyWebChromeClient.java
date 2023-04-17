package com.domaado.market.webview;

import android.net.Uri;
import android.os.Message;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

/**
 * Created by HongEuiChan on 2017. 11. 30..
 *
 * Webview용 크롬 클라이언트
 */

public class MyWebChromeClient extends WebChromeClient {

    public String TAG = MyWebInterface.class.getSimpleName();

    public MyWebInterface myWebInterface;

    public void setMyWebInterface(MyWebInterface webInterface) {
        this.myWebInterface = webInterface;
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        super.onProgressChanged(view, newProgress);
    }

    @Override
    public void onReceivedTitle(WebView view, String titleString) {
        super.onReceivedTitle(view, titleString);

        if(this.myWebInterface!=null) {
            this.myWebInterface.onTitleChanged(titleString);
        }
    }

    @Override
    public void onShowCustomView(View view, CustomViewCallback callback) {
        super.onShowCustomView(view, callback);
    }

    @Override
    public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
        return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg);
    }

    @Override
    public void onCloseWindow(WebView window) {
        super.onCloseWindow(window);
    }

    @Override
    public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
        // 필요할 경우 커스텀 알럿으로 처리!
        return super.onJsAlert(view, url, message, result);
    }

    @Override
    public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
        return super.onJsConfirm(view, url, message, result);
    }

    @Override
    public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
        return super.onJsPrompt(view, url, message, defaultValue, result);
    }

    @Override
    public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) {
        return super.onJsBeforeUnload(view, url, message, result);
    }

    @Override
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        return super.onConsoleMessage(consoleMessage);
    }

//    @Override
//    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
//
//        if(this.myWebInterface!=null) {
//            myWebInterface.openFileChooser(webView, filePathCallback, fileChooserParams);
//        }
//
//        return true;
//        //return super.onShowFileChooser(webView, filePathCallback, fileChooserParams);
//    }

    // For Android < 3.0
    public void openFileChooser(ValueCallback<Uri> uploadMsg) {
        openFileChooser(uploadMsg, "");
    }

    // For Android 3.0+
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
        if(this.myWebInterface!=null) {
            myWebInterface.openFileChooser(uploadMsg, acceptType);
        }
    }

    // For Android 4.1+
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
        openFileChooser(uploadMsg, acceptType);
    }


    // For Android 5.0+
    @Override
    public boolean onShowFileChooser(
            WebView webView, ValueCallback<Uri[]> filePathCallback,
            WebChromeClient.FileChooserParams fileChooserParams) {

        if(this.myWebInterface!=null) {
            myWebInterface.openFileChooser(webView, filePathCallback, fileChooserParams);
        }

        return true;

    }

}
