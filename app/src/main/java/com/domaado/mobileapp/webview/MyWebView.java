package com.domaado.mobileapp.webview;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ScrollView;

import androidx.annotation.NonNull;

import com.domaado.mobileapp.Constant;
import com.domaado.mobileapp.R;
import com.domaado.mobileapp.widget.myLog;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by kbins(James Hong) on 2018,November,26
 */
public class MyWebView extends WebView {


    private OnScrollChangeListener onScrollChangeListener;

    public MyWebView(final Context context) {
        super(context);
    }

    public MyWebView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public MyWebView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (onScrollChangeListener != null) {
            onScrollChangeListener.onScrollChange(this, l, t, oldl, oldt);
        }
    }

    public void setOnScrollChangeListener(OnScrollChangeListener onScrollChangeListener) {
        this.onScrollChangeListener = onScrollChangeListener;
    }

    public OnScrollChangeListener getOnScrollChangeListener() {
        return onScrollChangeListener;
    }

    public interface OnScrollChangeListener {
        /**
         * Called when the scroll position of a view changes.
         *
         * @param v          The view whose scroll position has changed.
         * @param scrollX    Current horizontal scroll origin.
         * @param scrollY    Current vertical scroll origin.
         * @param oldScrollX Previous horizontal scroll origin.
         * @param oldScrollY Previous vertical scroll origin.
         */
        void onScrollChange(WebView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY);
    }

    @Override
    public void loadUrl(@NonNull String url) {

//        setWebViewOptions();

        super.loadUrl(url);

//        if(!TextUtils.isEmpty(App.getBearerToken())){
//            Map<String, String> extraHeaders = new HashMap<>();
//            extraHeaders.put(Constant.CUSTOM_ACCESS_TOKEN_NAME, App.getBearerToken());
//            extraHeaders.put(Constant.CUSTOM_WEBVIEW_COUNT, String.valueOf(App.getWebCallCount()));
//
//            super.loadUrl(url, extraHeaders);
//        }else{
//            super.loadUrl(url);
//        }
    }

//    private void setWebViewOptions() {
//
//        WebSettings s = getSettings();
//        setScrollBarStyle(ScrollView.SCROLLBARS_OUTSIDE_OVERLAY);
//
//        if(!TextUtils.isEmpty(s.getUserAgentString()) && !s.getUserAgentString().contains(Constant.WEBVIEW_USER_AGENT)) {
//            s.setUserAgentString(s.getUserAgentString()+" "+Constant.WEBVIEW_USER_AGENT);
//        }
//
//        s.setJavaScriptCanOpenWindowsAutomatically(true);
//        s.setLoadsImagesAutomatically(true);
//        s.setDomStorageEnabled(true);
//        if (Build.VERSION.SDK_INT >= 8 && Build.VERSION.SDK_INT <= 18) {
//            s.setPluginState(WebSettings.PluginState.ON);
//        }
//        s.setRenderPriority(WebSettings.RenderPriority.HIGH);
//        s.setCacheMode(WebSettings.LOAD_NO_CACHE);
//        s.setDatabaseEnabled(true);
//        s.setAppCacheEnabled(true);
//        s.setSupportMultipleWindows(true);
//        s.setDefaultTextEncodingName("UTF-8");
//        s.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
//
//        s.setBuiltInZoomControls(true);
//        s.setUseWideViewPort(true);
//        s.setLoadWithOverviewMode(true);
//        s.setSavePassword(true);
//        s.setSaveFormData(true);
//        s.setJavaScriptEnabled(true);
//        s.setSupportZoom(false);
//        s.setTextZoom(100);
//
//        // enable navigator.geolocation
//        s.setGeolocationEnabled(true);
//        s.setGeolocationDatabasePath("/data/data/" + getContext().getString(R.string.path));
//
//        // enable Web Storage: localStorage, sessionStorage
//        s.setDomStorageEnabled(true);
//
//        // caching.
//        s.setAppCacheEnabled(true);
//        s.setAppCachePath(getContext().getCacheDir().getPath());
//        s.setCacheMode(WebSettings.LOAD_DEFAULT);
//
//        s.setDomStorageEnabled(true);
//        s.setAppCacheEnabled(true);
//        s.setAppCachePath(getApplicationContext().getFilesDir().getAbsolutePath() + "/cache");
//        s.setDatabaseEnabled(true);
//        s.setDatabasePath(getApplicationContext().getFilesDir().getAbsolutePath() + "/databases");
//
//        s.setAllowFileAccess(true);
//        s.setAllowFileAccessFromFileURLs(true);
//
//        // CHROME DEBUG
//        if(myLog.debugMode) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
//                setWebContentsDebuggingEnabled(true);
//        }
//
//        // Webview 설정 - 쿠키 등 결제를 위한 설정
//        /**************************************************************
//         * 안드로이드 5.0 이상으로 tagetSDK를 설정하여 빌드한경우 아래 구문을 추가하여 주십시요
//         **************************************************************/
//        if( android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
//            getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
//            CookieManager cookieManager = CookieManager.getInstance();
//            cookieManager.setAcceptCookie(true);
//            cookieManager.setAcceptThirdPartyCookies(this, true);
//        }
//
//    }
}
