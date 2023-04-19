package com.domaado.mobileapp.webview;

import android.net.Uri;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

/**
 * Created by HongEuiChan on 2017. 11. 30..
 *
 * custom webview interface
 */

public interface MyWebInterface {
    void onTitleChanged(String titleText);
    void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType);
    void openFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams);
}
