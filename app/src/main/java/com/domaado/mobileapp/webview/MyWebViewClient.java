package com.domaado.mobileapp.webview;

import static android.content.Context.DOWNLOAD_SERVICE;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.webkit.URLUtil;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.domaado.mobileapp.App;
import com.domaado.mobileapp.Common;
import com.domaado.mobileapp.Constant;
import com.domaado.mobileapp.R;
import com.domaado.mobileapp.Utility;
import com.domaado.mobileapp.submenus.WebContentActivity;
import com.domaado.mobileapp.widget.myLog;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;

/**
 * Created by HongEuiChan on 2017. 11. 30..
 *
 * custom WebviewClient
 */

public class MyWebViewClient extends WebViewClient implements DownloadListener {

    private String TAG = MyWebViewClient.class.getSimpleName();
    private Activity mActivity;

    private WebContentActivity.WebViewInterface webViewInterface;
    // ISP앱에서 결제후 리턴받을 스키마 이름을 설정합니다.
    // AndroidManaifest.xml에 명시된 값과 동일한 값을 설정하십시요.
    // 스키마 뒤에 ://를 붙여주십시요.
    private String WAP_URL = "nicepaysample"+"://";

    public MyWebViewClient(Activity activity, WebContentActivity.WebViewInterface webViewInterface) {
        this.mActivity = activity;
        this.webViewInterface = webViewInterface;
    }

    private void handleTelLink(String url){
        // Initialize an intent to open dialer app with specified phone number
        // It open the dialer app and allow user to call the number manually
        Intent intent = new Intent(Intent.ACTION_DIAL);

        // Send phone number to intent as data
        intent.setData(Uri.parse(url));

        // Start the dialer app activity with number
        mActivity.startActivity(intent);
    }

    private void sendSmsIntent(String number){
        try{
            Uri smsUri = Uri.parse("sms:"+number);
            Intent sendIntent = new Intent(Intent.ACTION_SENDTO, smsUri);
            sendIntent.putExtra("sms_body", "");
            mActivity.startActivity(sendIntent);

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void sendEmail(String email){
        try{
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
            emailIntent.putExtra(Intent.EXTRA_CC, new String[]{email});
            emailIntent.putExtra(Intent.EXTRA_BCC, new String[]{email});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Email Test");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "");
            emailIntent.setType("message/rfc822");
            mActivity.startActivity(Intent.createChooser(emailIntent, "Email Choose"));
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void openAppMarket(String appPackageName) {
        try {
            if(!TextUtils.isEmpty(appPackageName) && appPackageName.startsWith("details?id="))
                mActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://" + appPackageName)));
            else
                mActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            mActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    public void openBrowser(String url) {
        try {

            if(App.isIsTEST()) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mActivity, "OPEN BROWSER", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            if (!url.startsWith("http://") && !url.startsWith("https://"))
                url = "http://" + url;

            Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            mActivity.startActivity(myIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(mActivity, "No application can handle this request."
                    + " Please install a webbrowser",  Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

//    @Override
//    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
//        return super.shouldOverrideUrlLoading(view, request);
//    }

    private boolean override = false;

    private boolean procShouldOverrideUrlLoading(WebView view, String url, WebResourceRequest request) {

        myLog.i(TAG,"*** procShouldOverrideUrlLoading : "+url);

        view.setDownloadListener(this);

        Intent intent = null;
        // 웹뷰에서 ispmobile  실행한 경우...
        if( url.startsWith("ispmobile") ) {
            if( Utility.isPackageInstalled(mActivity.getApplicationContext(), "kvp.jjy.MispAndroid320") ) {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                mActivity.startActivity(intent);
                return true;
            } else {
                if(webViewInterface!=null) webViewInterface.installISP();
                return true;
            }

            // 웹뷰에서 계좌이체를 실행한 경우...
        } else  if(url.startsWith("kftc-bankpay") ) {
            if (Utility.isPackageInstalled(mActivity.getApplicationContext(), "com.kftc.bankpay.android")) {
                String sub_str_param = "kftc-bankpay://eftpay?";
                String reqParam = url.substring(sub_str_param.length());
                try {
                    reqParam = URLDecoder.decode(reqParam, "utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                reqParam = webViewInterface.makeBankPayData(reqParam);

                intent = new Intent(Intent.ACTION_MAIN);
                intent.setComponent(new ComponentName("com.kftc.bankpay.android", "com.kftc.bankpay.android.activity.MainActivity"));
                intent.putExtra("requestInfo", reqParam);
                mActivity.startActivityForResult(intent, 1);

                return true;
            } else {
                if (webViewInterface != null) webViewInterface.installKFTC();
                return true;
            }
        } else if((
//                url.startsWith("intent://") ||
                url.contains("market://")
                || url.contains("vguard")
                || url.contains("droidxantivirus")
                || url.contains("v3mobile")
                || url.contains(".apk")
                || url.contains("mvaccine")
                || url.contains("smartwall://")
                || url.contains("http://m.ahnlab.com/kr/site/download")) ) {
            try {
                intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
            } catch (URISyntaxException ex) {
                myLog.e(TAG,"[error] Bad request uri format : [" + url + "] =" + ex.getMessage());
                return false;
            }

            if( mActivity.getPackageManager().resolveActivity(intent,0) == null ) {
                String pkgName = intent.getPackage();
                if( pkgName != null ) {
                    Uri uri = Uri.parse("market://search?q=pname:" + pkgName);
                    intent = new Intent(Intent.ACTION_VIEW, uri);
                    mActivity.startActivity(intent);
                }
            } else {
                Uri uri = Uri.parse(intent.getDataString());
                intent = new Intent(Intent.ACTION_VIEW, uri);
                mActivity.startActivity(intent);
            }

            return true;
            // 웹뷰에서 안심클릭을 실행한 경우...
        } else if (url != null	&& (url.contains("vguard")
                || url.contains("droidxantivirus")
                || url.contains("lottesmartpay")
                || url.contains("smshinhancardusim://")
                || url.contains("shinhan-sr-ansimclick")
                || url.contains("v3mobile")
                || url.endsWith(".apk")
                || url.contains("smartwall://")
                || url.contains("appfree://")
                || url.contains("market://")
                || url.contains("ansimclick://")
                || url.contains("ansimclickscard")
                || url.contains("ansim://")
                || url.contains("mpocket")
                || url.contains("mvaccine")
                || url.contains("market.android.com")
                || url.contains("samsungpay")
                || url.contains("droidx3web://")
                || url.contains("kakaopay")
                || url.contains("callonlinepay")	//2018-01-15 LG페이추가
                || url.contains("http://m.ahnlab.com/kr/site/download"))) {

            try{
                try {
                    intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                    myLog.i("NICE","intent getDataString +++===>"+intent.getDataString());

                } catch (URISyntaxException ex) {
                    myLog.e("Browser","Bad URI " + url + ":" + ex.getMessage());
                    return false;
                }

                if( url.startsWith("intent") ) { //chrome 버젼 방식
                    if( mActivity.getPackageManager().resolveActivity(intent,0)==null ) {
                        String packagename=intent.getPackage();
                        if( packagename !=null ) {
                            Uri uri = Uri.parse("market://search?q=pname:"+packagename);
                            intent = new Intent(Intent.ACTION_VIEW,uri);
                            mActivity.startActivity(intent);
                            return true;
                        }
                    }

                    Uri uri = Uri.parse(intent.getDataString());
                    intent = new Intent(Intent.ACTION_VIEW, uri);
                    mActivity.startActivity(intent);

                    return true;
                } else { //구 방식
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    mActivity.startActivity(intent);
                    //return true;
                }
            } catch( Exception e ) {
                myLog.i("NICE", e.getMessage());
                return false;
            }

            return true;
        }
        // ispmobile에서 결제 완료후 스마트주문 앱을 호출하여 결제결과를 전달하는 경우
        else if (url.startsWith(WAP_URL)) {
            String thisurl = url.substring(WAP_URL.length());
            view.loadUrl(thisurl);
            return  true;
        } else if(url.startsWith("http")){

            // 지정된 서버의 컨텐츠를 부르고 있는지 판단.
            if(url.startsWith(mActivity.getResources().getString(R.string.url_site))||url.startsWith(mActivity.getResources().getString(R.string.url_site_test))) return false;
            else {
                openBrowser(url);
                return true;
            }
        } else if(url.startsWith("tel:")) {
            handleTelLink(url);
            return true;
        } else if(url.startsWith("sms:")) {
            String[] contents = url.split("sms://");
            if(contents!=null && contents.length>1) {
                sendSmsIntent(contents[1]);
                return true;
            }
        } else if(url.startsWith("mailto:")) {
            String[] contents = url.split("mailto://");
            if(contents!=null && contents.length>1) {
                sendEmail(contents[1]);
                return true;
            }
        } else if(url.startsWith("email:")) {
            String[] contents = url.split("email://");
            if(contents!=null && contents.length>1) {
                sendEmail(contents[1]);
                return true;
            }
        } else if(url.startsWith("market:")) {
            String[] contents = url.split("market://");
            if(contents!=null && contents.length>1) {
                openAppMarket(contents[1]);
                return true;
            }
        } else if(url.startsWith("intent:") ) {
            //intent:#Intent;action=com.kakao.talk.intent.action.CAPRI_LOGGED_IN_ACTIVITY;launchFlags=0x08880000;S.com.kakao.sdk.talk.appKey=f22c02417d427f354b0f89cbc6cd3fb4;S.com.kakao.sdk.talk.redirectUri=https://domaado.me/intro;S.com.kakao.sdk.talk.kaHeader=sdk/2.1.0%20os/javascript%20sdk_type/javascript%20lang/ko%20device/Linux_aarch64%20origin/https%3A%2F%2Fdomaado.me;S.com.kakao.sdk.talk.extraparams=%7B%22client_id%22%3A%22f22c02417d427f354b0f89cbc6cd3fb4%22%2C%22state%22%3A%22jr5k40XO8x0!ZMeJ%22%2C%22redirect_uri%22%3A%22https%3A%2F%2Fdomaado.me%2Fintro%22%2C%22response_type%22%3A%22code%22%2C%22auth_tran_id%22%3A%22J3zVDnguPRsZajF-0s95Al5mqnPOTgceguYL~JEEQVdrGYqH0RNz0KGIMXbb%22%2C%22is_popup%22%3Atrue%7D;S.com.kakao.sdk.talk.state=jr5k40XO8x0!ZMeJ;S.browser_fallback_url=https%3A%2F%2Fkauth.kakao.com%2Foauth%2Fauthorize%3Fclient_id%3Df22c02417d427f354b0f89cbc6cd3fb4%26state%3Djr5k40XO8x0!ZMeJ%26redirect_uri%3Dhttps%253A%252F%252Fdomaado.me%252Fintro%26response_type%3Dcode%26auth_tran_id%3DJ3zVDnguPRsZajF-0s95Al5mqnPOTgceguYL~JEEQVdrGYqH0RNz0KGIMXbb%26ka%3Dsdk%252F2.1.0%2520os%252Fjavascript%2520sdk_type%252Fjavascript%2520lang%252Fko%2520device%252FLinux_aarch64%2520origin%252Fhttps%25253A%25252F%25252Fdomaado.me%26is_popup%3Dfalse;end;

            /**
             * FOR KAKAO LOGIN
             */
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (request != null && request.getUrl().getScheme().equals("intent")) {

                        Intent i = Intent.parseUri(request.getUrl().toString(), Intent.URI_INTENT_SCHEME);

                        if(i.resolveActivity(mActivity.getPackageManager()) != null) {
                            mActivity.startActivity(i);
                            myLog.d(TAG, "*** activity start!.");
                            return true;
                        }

                        String fallbackUrl = i.getStringExtra("browser_fallback_url");
                        if(!TextUtils.isEmpty(fallbackUrl)) {
                            webViewInterface.loadUrl(fallbackUrl);
                            myLog.d(TAG, "*** Fallback: "+fallbackUrl);
                            return true;
                        }
                    }
                }
            } catch(Exception e) {
                e.printStackTrace();
                myLog.e(TAG, "*** Exception: "+e.getMessage());
            }

//
//            try {
//                intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
//            } catch (URISyntaxException ex) {
//                myLog.e(TAG,"[error] Bad request uri format : [" + url + "] =" + ex.getMessage());
//                return false;
//            }
//
//            if( mActivity.getPackageManager().resolveActivity(intent,0) == null ) {
//                String pkgName = intent.getPackage();
//                if( pkgName != null ) {
//                    Uri uri = Uri.parse("market://search?q=pname:" + pkgName);
//                    intent = new Intent(Intent.ACTION_VIEW, uri);
//                    mActivity.startActivity(intent);
//                }
//            } else {
//                Uri uri = Uri.parse(intent.getDataString());
//                intent = new Intent(Intent.ACTION_VIEW, uri);
//                mActivity.startActivity(intent);
//            }

//            return true;

        } else if (url.startsWith("geo:") || url.startsWith(WebView.SCHEME_MAILTO)) {
            try {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                mActivity.startActivity(i);
                override = false;
            } catch (android.content.ActivityNotFoundException e) {
                myLog.e(TAG, "Error with " + url + ": " + e.toString());
            }
        }

        // yellowin url start 예외처리구문
        else if (url != null // && !url.equals("ansimclick.hyundiacard.com") && !url.contains("naver") &&
                && (url.contains("http://market.android.com")
                || url.contains("market://")
                || url.contains("intent")
                || url.contains("com.kftc.bankpay.android://")
                || url.contains("com.lotte.lottesmartpay://")
                || url.contains("com.ahnlab.v3mobileplus://")
                || url.contains("hanaansim://")
                || url.contains("vguard://")
                || url.contains("droidxantivirus://")
                || url.contains("smshinhancardusim://")
                || url.contains("smartwall://")
                || url.contains("appfree://")

                || url.contains("kb-acp://")
                || url.contains("lguthepay://")
                || url.contains("v3mobile://")
                || url.contains("kakaopay://")

                || url.contains("kftc-bankpay://")
                || url.contains("ispmobile://")
                || url.contains("hdcardappcardansimclick://")
                || url.contains("smhyundaiansimclick://")
                || url.contains("shinhan-sr-ansimclick://")
                || url.contains("smshinhanansimclick://")
                || url.contains("nonghyupcardansimclick://")
                || url.contains("kb-acp://")
                || url.contains("mpocket.online.ansimclick://")
                || url.contains("ansimclickscard://")
                || url.contains("ansimclickipcollect://")
                || url.contains("vguardstart://")
                || url.contains("samsungpay://")
                || url.contains("scardcertiapp://")
                || url.contains("lottesmartpay://")
                || url.contains("lotteappcard://")
                || url.contains("cloudpay://")
                || url.contains("nhappvardansimclick://")
                || url.contains("nhappcardansimclick://")
                || url.contains("nhallonepayansimclick://")
                || url.contains("citispay://")
                || url.contains("citimobileapp://")
                || url.contains("citicardappkr://")
                || url.contains("payco://")
                || url.contains("paypin://")
                || url.contains("paycoapplogin://")
                || url.contains("tswansimclick://")
                || url.contains("bankwallet://")
                || url.contains("uppay://")
                || url.contains("kpay://")
                || url.contains("smilepayapp://")
                || url.contains("maps://")
                || url.contains("citymapper://")
                || url.contains("comgooglemaps://")
                || url.contains("navigon://")
                || url.contains("transit://")
                || url.contains("waze://")
                || url.contains("yandexnavi://")
                || url.contains("uber://")
                || url.contains("tomtomhome://")
                || url.contains("com.sygic.aura://")
                || url.contains("here-route://")
                || url.contains("moovit://")
                || url.contains("lyft://")
                || url.contains("mapsme://")
                || url.contains("cabify://")
                || url.contains("baidumap://")
                || url.contains("lguthepay://")
                || url.contains("smartxpay-transfer://")
                || url.contains("smilepay://")
                || url.contains("supertoss://")
                || url.contains("hanawalletmembers://")
                || url.contains("wooripay://")
                || url.contains("shinsegaeeasypayment://")
                || url.contains("lpayapp://")
                || url.contains("lmslpay://")
                || url.contains("chai://")
                || url.contains("intmoney://")
                || url.contains("hanaskcardmobileportal://")
                || url.contains("ukbanksmartbanknonloginpay://")
                || url.contains("kdb-bankpay://")
                || url.contains("ibk-bankpay://")
                || url.contains("kb-bankpay://")
                || url.contains("keb-bankpay://")
                || url.contains("sh-bankpay://")
                || url.contains("nhb-bankpay://")
                || url.contains("nh-bankpay://")
                || url.contains("wr-bankpay://")
                || url.contains("sc-bankpay://")
                || url.contains("s-bankpay://")
                || url.contains("ct-bankpay://")
                || url.contains("dg-bankpay://")
                || url.contains("bnk-bankpay://")
                || url.contains("kj-bankpay://")
                || url.contains("jj-bankpay://")
                || url.contains("kn-bankpay://")
                || url.contains("kp-bankpay://")
                || url.contains("cu-bankpay://")
                || url.contains("mg-bankpay://")
                || url.contains("kbn-bankpa://y")
                || url.contains("kkb-bankpay://")

                // || url.contains("ansimclick")
                // || (url.contains("ansimclick") && !url.equals("ansimclick.hyundiacard.com"))
                || url.contains("market://details?id=com.shcard.smartpay")
                || url.contains("http://m.ahnlab.com/kr/site/download")
                || url.endsWith(".apk")

                // 본인인증
                || url.contains("tauthlink://")
                || url.contains("ktauthexternalcall://")
                || url.contains("upluscorporation://")

                || url.contains("line://")// 라인
                || url.contains("bandapp://")// 네이버 밴드
                || url.contains("kakaotalk://")// 카카오톡
                || url.contains("storylink://")
                || url.contains("kakaoplus://")
                || url.contains("kakaokompassauth://")
                || url.contains("storykompassauth://")
                || url.contains("kakaolink://")
                || url.contains("kakaotalk-5.9.7://")
                || url.contains("kakaostory-2.9.0://")
                || url.contains("naversearchapp")// 네이버앱
                || url.contains("naversearchthirdlogin")
                || (url.contains("nidlogin") && !url.contains("naver.com"))
                || url.contains("daumapps://open")// 다음
                || url.contains("googlechromes://")// 크롬
                || url.contains("fb://")// 페이스북
                || url.contains("fbapi://")
                || url.contains("fb-messenger-api://")
                || url.contains("fbauth2://")
                || url.contains("fbshareextension://"))) {
            myLog.d(TAG, "yellowin url scheme 11111 " + url);
            try {
                // Intent intent = new Intent();
                try {
                    intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                    myLog.d(TAG, "yellowin url scheme aaaaaaa " + url);
                } catch (java.net.URISyntaxException ex) {
                    ex.printStackTrace();
                    myLog.d(TAG, "yellowin url scheme eeeeeee " + ex.getMessage());
                    override = false;
                }

                try {
                    mActivity.startActivity(intent);
                    override = true;
                } catch (android.content.ActivityNotFoundException e) {
                    // chrome Version type
                    myLog.d(TAG, "yellowin url scheme bbbbbb " + e.getMessage());

                    if (mActivity.getPackageManager().resolveActivity(intent, 0) == null) {
                        myLog.d(TAG, "yellowin url scheme 33333 " + url);
                        String packagename = intent.getPackage();
                        if (packagename != null) {
                            Uri uri = Uri.parse("market://search?q=pname:" + packagename);
                            intent = new Intent(Intent.ACTION_VIEW, uri);
                            mActivity.startActivity(intent);
                            override = true;
                        }
                    } else {
                        myLog.d(TAG, "yellowin url scheme 44444 " + url);
                        intent.addCategory(Intent.CATEGORY_BROWSABLE);
                        intent.setComponent(null);
                        try {
                            if (mActivity.startActivityIfNeeded(intent, -1)) {
                                override = true;
                            }
                        } catch (android.content.ActivityNotFoundException ex) {
                            override = false;
                        }
                    }
                    myLog.d(TAG, "yellowin url scheme 44444 " + url);
                    override = true;
                }
            } catch (android.content.ActivityNotFoundException ee) {
                // Log.e("error ===>", e.getMessage());
                myLog.e("yellowin url", "yellowin url scheme ssssssss ");
                ee.printStackTrace();
                override = false;
            }
        } else if (url.contains("domaado.me")

                // tags.bluekai.com
                || url.contains("tags.bluekai.com")
                || url.contains("astg.widerplanet.com")

                // https://pf.kakao.com
                // || url.contains("pf.kakao.com") // 카카오 채널...
                || url.contains("nid.naver.com")
                || url.contains("accounts.kakao.com")
                || url.contains("kauth.kakao.com")
                || url.contains("api.twitter.com")
                || (url.contains("facebook.com") && url.contains("oauth"))
                || url.contains("accounts.google.com")
                || url.contains("account/login")

                || url.contains("drmobile.inicis.com")
                || url.contains("mobilians.co.kr")
                || url.contains("alipay.com")
                || url.contains("allpayx.com")
                || url.contains("fcmobile.inicis.com")
                || url.contains("http://aio.best")
                || url.contains("smpay.kcp.co.kr")
                || url.contains("kcp.co.kr")
                || url.contains("ansimclick.hyundiacard.com")
                || url.contains("allatpay.com")
                || url.contains("xpay.lgdacom.net")
                || url.contains("pg-web.kakao.com") // 카카오
                // || url.contains("kakao.com")
                || url.contains("shinhancard.com")
                || url.contains("hyundaicard.com")
                || url.contains("ssgpay.com")
                || url.contains("payco.com")
                || url.contains("lpay.com")
                || url.contains("inicis.com")
                || url.contains("vpay.co.kr")
                || url.contains("bootpay.co.kr")
                || url.contains("allatpay.com")
                || url.contains("smilepay.co.kr")
                || url.contains("nicepay.co.kr")
                || url.contains("teledit.com")

                || url.contains("eximbay.com")
                || url.contains("paypal.com")
                || url.contains("paypalobjects.com")
                || url.contains("tpay.co.kr")
                || url.contains("uplus.co.kr")
                || url.contains("bankpay.or.kr")
                || url.contains("ksmobile.inicis.com")
                // || url.contains("kftc-bankpay")
                || url.contains("allthegate.com")
                || url.contains("iamport.kr")
                || url.contains("pay.toss")
                || url.contains("samsungcard.co.kr")
                || url.contains("samsungcard.com")
                || url.contains("samsungcard-ad.com")
                || url.contains("nonghyup.com")
                || url.contains("pay.tosspayments.com/card")
                || url.contains("acs.hanacard.co.kr")
                || url.contains("tosspayments.com")
                || url.contains("pay.naver.com")
                || url.contains("pstatic.net")
                || url.contains("paymentwall.com")
                || url.contains("pay.kakao.com")
                || url.contains("welcomepayments.co.kr")
                || url.contains("payletter.com")
                || url.contains("easypay.co.kr")
                || url.contains("citibank.co.kr")
                || url.contains("godo.co.kr")
                || url.contains("godomall.com")) {
            myLog.d(TAG, "yellowin url yes : " + url);

            override = false;
        }
        // If sms:5551212?body=This is the message
        else if (url.startsWith("sms:")) {
            try {
                intent = new Intent(Intent.ACTION_VIEW);
                // Get address
                String address = null;
                int parmIndex = url.indexOf('?');
                if (parmIndex == -1) {
                    address = url.substring(4);
                } else {
                    address = url.substring(4, parmIndex);

                    // If body, then set sms body
                    Uri uri = Uri.parse(url);
                    String query = uri.getQuery();
                    if (query != null) {
                        if (query.startsWith("body=")) {
                            intent.putExtra("sms_body", query.substring(5));
                        }
                    }
                }
                intent.setData(Uri.parse("sms:" + address));
                intent.putExtra("address", address);
                intent.setType("vnd.android-dir/mms-sms");
                mActivity.startActivity(intent);
                override = true;
            } catch (android.content.ActivityNotFoundException e) {
                myLog.e(TAG, "Error sending sms " + url + ":" + e.toString());
            }
        } else if (url != null) {
            myLog.d(TAG, "yellowin url no : " + url);
            try {
                try {
                    intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                    mActivity.startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Uri uri = Uri.parse(url);
                intent = new Intent(Intent.ACTION_VIEW, uri);
                mActivity.startActivity(intent);

                override = true;

            } catch (android.content.ActivityNotFoundException e) {
                e.printStackTrace();
                override = false;
            }
            myLog.d(TAG, "yellowin url 00000 no : " + url);
        }

        return override;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url){
        return procShouldOverrideUrlLoading(view, url, null);
    }

    // From api level 24
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request){

        // Get the tel: url
        String url = request.getUrl().toString();

        return procShouldOverrideUrlLoading(view, url, request);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        if(webViewInterface!=null) {
            webViewInterface.loadDefaultValue();
        }
        super.onPageFinished(view, url);
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        super.onLoadResource(view, url);
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        return super.shouldInterceptRequest(view, request);
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        // 필요할 경우 커스텀 알럿으로 오류알림!
        super.onReceivedError(view, request, error);
    }

    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        super.onReceivedHttpError(view, request, errorResponse);
    }

    @Override
    public void onReceivedLoginRequest(WebView view, String realm, String account, String args) {
        super.onReceivedLoginRequest(view, realm, account, args);
    }

    @Override
    public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
        //handler.proceed(); // Ignore SSL certificate errors

        String message = error!=null ? error.toString() : mActivity.getResources().getString(R.string.webview_ssl_error);

        Common.alertMessage(mActivity,
                mActivity.getResources().getString(R.string.app_name),
                mActivity.getResources().getString(R.string.webview_ssl_error),
                mActivity.getResources().getString(R.string.btn_continue),
                mActivity.getResources().getString(R.string.btn_cancel),
                new Handler() {

                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);

                        switch(msg.what) {
                            case Constant.ALERTDIALOG_RESULT_YES:
                                handler.proceed();
                                break;
                            case Constant.ALERTDIALOG_RESULT_NO:
                            case Constant.ALERTDIALOG_RESULT_NUTRUAL:
                                handler.cancel();
                                break;
                        }
                    }
                });

    }

    @Override
    public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setMimeType(mimeType);

//                String cookies = CookieManager.getInstance().getCookie(url);
//                request.addRequestHeader("cookie", cookies);

        request.addRequestHeader("User-Agent", userAgent);
        request.setDescription("Downloading File...");
        request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType));
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(
                        url, contentDisposition, mimeType));
        DownloadManager dm = (DownloadManager) mActivity.getSystemService(DOWNLOAD_SERVICE);
        dm.enqueue(request);

        Toast.makeText(mActivity, mActivity.getResources().getString(R.string.file_downloading_message), Toast.LENGTH_LONG).show();
    }
}