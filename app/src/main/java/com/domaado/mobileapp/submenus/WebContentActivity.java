package com.domaado.mobileapp.submenus;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.domaado.mobileapp.App;
import com.domaado.mobileapp.Common;
import com.domaado.mobileapp.Constant;
import com.domaado.mobileapp.R;
import com.domaado.mobileapp.Utility;
import com.domaado.mobileapp.data.QueryParams;
import com.domaado.mobileapp.locale.LocaleUtils;
import com.domaado.mobileapp.sensors.GPSTracker;
import com.domaado.mobileapp.sensors.GPSTrackerListener;
import com.domaado.mobileapp.share.KakaoTalklink;
import com.domaado.mobileapp.webview.HandleUiListener;
import com.domaado.mobileapp.webview.JavaScriptInterface;
import com.domaado.mobileapp.webview.MyWebChromeClient;
import com.domaado.mobileapp.webview.MyWebInterface;
import com.domaado.mobileapp.webview.MyWebView;
import com.domaado.mobileapp.webview.MyWebViewClient;
import com.domaado.mobileapp.widget.KeyboardHeightProvider;
import com.domaado.mobileapp.widget.SoftKeyboardStateWatcher;
import com.domaado.mobileapp.widget.myLog;

import org.apache.http.util.EncodingUtils;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimerTask;

public class WebContentActivity extends AppCompatActivity implements View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private Context mContext;
    private String TAG = WebContentActivity.class.getSimpleName();

    private MyWebView mWebView;
    public ValueCallback mFileCallback;
    public static final int FILECHOOSE_CALLBACK         = 100;
    public static final int FILECHOOSER_NORMAL_REQ_CODE = 200;
    public static final int FILECHOOSER_LOLLIPOP_REQ_CODE = 300;

    private ValueCallback<Uri> filePathCallbackNormal;
    private ValueCallback<Uri[]> filePathCallbackLollipop;
    private Uri mCapturedImageURI;

    public TextView title;

    public GPSTracker gpsTracker;

    private SoftKeyboardStateWatcher softKeyboardStateWatcher;

    public final static String ACTION_FILTER = "com.domaado.mobileapp.firebase.action";
    public final static String ACTION_UPDATE_LOCATION = "com.domaado.mobileapp.update.location";

//    private Timer mTimer;

    LocalBroadcastManager mLocalBroadcastManager;
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            myLog.d(TAG, "*** BroadcastReceiver - " + intent.getAction());

            if(intent.getAction().equals(Constant.OPENURL_FILTER)) {
                checkPageAndRefresh(intent);
            } else if(intent.getAction().equals(Constant.REFRESH_FILTER)) {
                refreshPage();
            } else

                // local filters
                if (intent.getAction().equals(ACTION_FILTER)) {

                    Serializable serializable = intent.getSerializableExtra("queryParams");
                    QueryParams queryParams = (QueryParams)serializable;
                    if(queryParams!=null) App.setQueryParam(queryParams);

                    if(App.getQueryParams()!=null) {
                        processAction(App.getQueryParams());
                        App.resetQueryParam();
                    } else {
                        Intent flowIntent = new Intent(WebContentActivity.this, WebContentActivity.class);
                        flowIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(flowIntent);
                    }
                } else if(intent.getAction().equals((ACTION_UPDATE_LOCATION))) {
                    updateLocation(App.getCurrentLocation());
                }
        }
    };

    /**
     * FOR BANKPAY
     */
    final String ISP_LINK =  "market://details?id=kvp.jjy.MispAndroid320";      //ISP 설치 링크
    final String KFTC_LINK = "market://details?id=com.kftc.bankpay.android";    //금융결제원 설치 링크

    final String MERCHANT_URL = "https://web.nicepay.co.kr/smart/mainPay.jsp";	//가맹점의 결제 요청 페이지 URL

    private String NICE_BANK_URL = "";	// 계좌이체  인증후 거래 요청 URL

    private String BANK_TID = "";		// 계좌이체 거래시 인증ID

    public interface WebViewInterface {
        void installKFTC();
        void installISP();
        String makeBankPayData(String str);

        void loadUrl(String url);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_100_50);

        mContext = getBaseContext();

        Common.setTaskBarColored(this, 0);

        LocaleUtils.initialize(this);

        setUI();

        softKeyboardResizeUI();

//        mTimer = new Timer();

        checkQueryParams();
    }


//    public ActivityResultLauncher<Intent> MorningMissionCompleteResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
//        if (result.getResultCode() == Activity.RESULT_OK) {
//
//        }
//    });

    public int REQUEST_CODE = 1000;

    private void checkForUpdates() {
        GoogleApiAvailability availability = GoogleApiAvailability.getInstance();
        int resultCode = availability.isGooglePlayServicesAvailable(this);
        if (resultCode == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED) {
            availability.getErrorDialog(this, resultCode, REQUEST_CODE).show();
        }
    }

    /**
     * SOFTKEYBOARD HEIGHT WITH UI RESIZE
     */
    private void softKeyboardResizeUI() {
        RelativeLayout setup_push_root = findViewById(R.id.rootLayout);

        new KeyboardHeightProvider(this, getWindowManager(), setup_push_root, new KeyboardHeightProvider.KeyboardHeightListener() {
            @Override
            public void onKeyboardHeightChanged(int keyboardHeight, boolean keyboardOpen, boolean isLandscape) {
                myLog.d(TAG, "*** keyboardHeight: " + keyboardHeight + " keyboardOpen: " + keyboardOpen + " isLandscape: " + isLandscape);

                // UI 하단에 메뉴를 고정하기 때문에 키보드 높이에서 메뉴높이만큼 빼야한다. (dp)
//                LinearLayout sub_menu_box = findViewById(R.id.sub_menu_box);
//                final int keyboardHeightResize = keyboardHeight - sub_menu_box.getMeasuredHeight();

                final int keyboardHeightResize = keyboardHeight;

                if(keyboardOpen) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LinearLayout guide_bottom_layout = findViewById(R.id.guide_bottom_layout);

                            ViewGroup.LayoutParams params = guide_bottom_layout.getLayoutParams();
                            params.height = keyboardHeightResize;
                            guide_bottom_layout.setLayoutParams(params);

                            guide_bottom_layout.invalidate();
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LinearLayout guide_bottom_layout = findViewById(R.id.guide_bottom_layout);

                            ViewGroup.LayoutParams params = guide_bottom_layout.getLayoutParams();
                            params.height = 0;
                            guide_bottom_layout.setLayoutParams(params);

                            guide_bottom_layout.invalidate();
                        }
                    });
                }
            }
        });
    }

    /**
     * TODO. WEBVIEW가 초기화되고, 초기 페이지가 열린상태로 앞서 전달된 ACTION과 URL데이터가 전달되었는지 확인하여 처리한다.
     */
    private void checkQueryParams() {
        QueryParams queryParams = App.getQueryParams();

        if(queryParams!=null && !TextUtils.isEmpty(queryParams.getAction())) {
            if(queryParams.getAction().equalsIgnoreCase(Constant.PUSH_ACTION_OPEN_URL) && !TextUtils.isEmpty(queryParams.getUrl()) && mWebView!=null) {
                mWebView.loadUrl(queryParams.getUrl());
            }
        }
    }

    /**
     * TODO 열려있는 페이지가 있다면, 전달받은 페이지와 같은지 보고, 같으면 새로고침, 다르면 팝업 후 페이지 이동!
     *
     * @param intent
     */
    private void checkPageAndRefresh(Intent intent) {

        Serializable serializable = intent != null ? intent.getSerializableExtra("queryParams") : null;
        final QueryParams queryParams = serializable!=null ? (QueryParams)serializable : new QueryParams();

        if(mWebView!=null && !TextUtils.isEmpty(queryParams.getUrl())) {
            String currentUrl = mWebView.getUrl();
            if(!TextUtils.isEmpty(currentUrl) && (queryParams.getUrl().contains(currentUrl) || currentUrl.contains(queryParams.getUrl()))) {
                // 해당URL에 있는경우 새로고침
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        excuteJavascript("reload_contents", new String[]{queryParams.getUrl()});
//                        mWebView.reload();
                    }
                });
            } else {
                Common.alertMessage(this,
                        queryParams.getTitle(),
                        queryParams.getMessage(),
                        getResources().getString(R.string.btn_view),
                        getResources().getString(R.string.btn_close),
                        new Handler() {

                            @Override
                            public void handleMessage(@NonNull Message msg) {
                                super.handleMessage(msg);

                                switch(msg.what) {
                                    case Constant.ALERTDIALOG_RESULT_YES:
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mWebView.loadUrl(queryParams.getUrl());
                                            }
                                        });
                                        break;
                                    case Constant.ALERTDIALOG_RESULT_NO:
                                    case Constant.ALERTDIALOG_RESULT_NUTRUAL:
                                        break;
                                }
                            }
                        });
            }
        }
    }

    /**
     * TODO 현재 페이지를 새로고침한다.
     */
    private void refreshPage() {
        if(mWebView!=null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mWebView.reload();
                }
            });
        }
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    protected void onResume() {
        super.onResume();

        myLog.d(TAG, "*** onResume");

        if(mLocalBroadcastManager==null) {
            mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
            IntentFilter mIntentFilter = new IntentFilter();
            mIntentFilter.addAction(ACTION_FILTER);
            mIntentFilter.addAction(ACTION_UPDATE_LOCATION);
            mIntentFilter.addAction(Constant.OPENURL_FILTER);
            mIntentFilter.addAction(Constant.REFRESH_FILTER);
            mLocalBroadcastManager.registerReceiver(mBroadcastReceiver, mIntentFilter);
        }

        if (!checkPlayServices()) {
            myLog.d(TAG, "*** You need to install Google Play Services to use the App properly");
        }

        initLocationService();
    }

    private Location currentLocation;
    private GoogleApiClient googleApiClient;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private LocationRequest locationRequest;
    private static final long UPDATE_INTERVAL = 1000, FASTEST_INTERVAL = 1000; // = 5 seconds

    private void initLocationService() {

        if(googleApiClient == null) {
            // we build google api client
            googleApiClient = new GoogleApiClient.Builder(this).
                    addApi(LocationServices.API).
                    addConnectionCallbacks(this).
                    addOnConnectionFailedListener(this).build();
        }

        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    private void startLocationUpdates() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                &&  ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "You need to enable permissions to display location !", Toast.LENGTH_SHORT).show();
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST);
            } else {
                //finish();
                Common.alertMessage(this,
                        getResources().getString(R.string.app_name),
                        getResources().getString(R.string.require_google_service_api),
                        getResources().getString(R.string.btn_ok),
                        new Handler() {

                            @Override
                            public void handleMessage(Message msg) {
                                super.handleMessage(msg);
                            }
                        });
            }

            return false;
        }

        return true;
    }

    @Override
    public void onLocationChanged(Location location) {

        currentLocation = location;

        myLog.d(TAG, "*** location: "+location.toString());

        if(location != null) {
            App.setCurrentLocation(location);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        myLog.e(TAG, "onConnectionFailed: "+connectionResult.getErrorMessage());

        initPosition();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                &&  ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Permissions ok, we get last location
        currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        if (currentLocation != null) {
            myLog.d(TAG, "*** Latitude : " + currentLocation.getLatitude() + "\nLongitude : " + currentLocation.getLongitude());

//            try {
//                if(mTimer!=null) {
//                    mTimer.cancel();
//                    mTimer = new Timer();
//                }
//
//                mTimer.schedule(new updateLocationData(), (long)(Constant.REFRESH_UPDATE_LOCATION_SECONDS*1000), (long)(Constant.REFRESH_UPDATE_LOCATION_SECONDS*1000));
//
//            } catch(Exception e) {
//                e.printStackTrace();
//            }
        }

        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        myLog.d(TAG, "onConnectionSuspended");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks whether a hardware keyboard is available
        if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) {
            Toast.makeText(this, "keyboard visible", Toast.LENGTH_SHORT).show();
        } else if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
            Toast.makeText(this, "keyboard hidden", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

//        try {
//            if(mTimer!=null) {
//                mTimer.cancel();
//            }
//        } catch(Exception e) {
//            e.printStackTrace();
//        }

        if(mLocalBroadcastManager!=null) {
            mLocalBroadcastManager.unregisterReceiver(mBroadcastReceiver);
            mLocalBroadcastManager = null;
        }

        stopGps();

        try {
            if(googleApiClient!=null) LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private void stopGps() {
        if(gpsTracker!=null) {
            gpsTracker.stopUsingGPS();
            gpsTracker = null;
        }
    }

    private void processAction(QueryParams queryParams) {

        myLog.d(TAG, "*** processAction ");

        final String action = queryParams.getAction();
        final String title = !TextUtils.isEmpty(queryParams.getTitle()) ? queryParams.getTitle() : getResources().getString(R.string.push_title);
        final String message = !TextUtils.isEmpty(queryParams.getMessage()) ? queryParams.getMessage() : getResources().getString(R.string.push_message);
        final String responseId = queryParams.getResponseId();

        if(Constant.PUSH_ACTION_ALERT.equalsIgnoreCase(action)) {
            Common.alertMessage(
                    this,
                    title,
                    message,
                    getResources().getString(R.string.btn_ok),
                    new Handler() {

                        @Override
                        public void handleMessage(Message msg) {
                            super.handleMessage(msg);

                            Intent flowIntent = new Intent(WebContentActivity.this, WebContentActivity.class);
                            flowIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            startActivity(flowIntent);
                        }
                    }
            );
        } else {
            Toast.makeText(this, "Unknown action("+action+")", Toast.LENGTH_SHORT).show();
        }
    }

    private void initPosition() {

        myLog.d(TAG, "*** initPosition ");

        gpsTracker = new GPSTracker(this, new GPSTrackerListener() {
            @Override
            public void onUpdateLocation(Location location) {
                myLog.d(TAG, "*** initPosition GPSTracker Location: "+location.toString());
                App.setCurrentLocation(location);

                updateLocation(location);
            }
        });

        gpsTracker.requestUpdateGPS();
        updateLocation(gpsTracker.getFastLocation());
    }

    private void updateLocation(Location location) {
        if(location!=null) {
            double lat = location.getLatitude();
            double lon = location.getLongitude();

//            excuteJavascript("updateLocation", new String[]{String.valueOf(lat), String.valueOf(lon)});
        }
    }

    /**
     * TODO: 웹페이지의 자바스크립트를 호출한다.
     *
     * @param method
     * @param params
     */
    private void excuteJavascript(String method, String ... params) {
        StringBuffer paramValues = new StringBuffer("");

        final String script;

        if(params!=null) {
            for(String param: params) {
                if(paramValues.length()>0) paramValues.append(",");
                paramValues.append("'"+param+"'");

            }
            script = String.format("if(typeof %s === 'function') %s(%s);", method, method, paramValues.toString());
        } else {
            script = String.format("if(typeof %s === 'function') %s();", method, method);
        }


        myLog.d(TAG, "*** excuteJavascript: "+script);

        if(mWebView!=null) {
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl("javascript:"+script);
                }
            });
        }
    }

//    @Override
//    public void onTitleChanged(String titleText) {
//        TextView sub_top_title = findViewById(R.id.sub_top_title);
//        sub_top_title.setText(titleText);
//    }

    private LinearLayout sub_top_back_btn_box;

    private TextView sub_top_common(String rightMenuText) {
        // top include resources
        sub_top_back_btn_box = (LinearLayout) findViewById(R.id.sub_top_back_btn_box);
        TextView sub_top_title = (TextView)findViewById(R.id.sub_top_title);
        TextView sub_top_right_cmd = (TextView)findViewById(R.id.sub_top_right_cmd);
        sub_top_back_btn_box.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WebContentActivity.this.finish();
            }
        });
        sub_top_back_btn_box.setVisibility(View.GONE);

        sub_top_title.setText("");  // menu title

        sub_top_right_cmd.setText(rightMenuText); // right menu
        sub_top_right_cmd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 메뉴동작!
            }
        });

        try {
            Common.updateActionBarHeight(this, (LinearLayout) findViewById(R.id.guide_top_box));
        } catch(Exception e) {
            e.printStackTrace();
        }

        return sub_top_title;
    }

    private void setUI() {
        setContentView(R.layout.guide);

        try {
            Common.updateActionBarHeight(this, (LinearLayout) findViewById(R.id.guide_top_box));
        } catch(Exception e) {
            e.printStackTrace();
        }

        title = sub_top_common("");

        mWebView = findViewById(R.id.guide_webview);

        String lang = Common.getConfig(this, Constant.CONFIG_USERLANGUAGE);
        lang = !TextUtils.isEmpty(lang) ? lang.toLowerCase(Locale.getDefault()) : Locale.KOREA.getCountry().toLowerCase(Locale.getDefault());

        String path = "";
        if(getIntent()!=null) {
            path = getIntent().getStringExtra("url");
        }

        String cacheParam = String.format(Locale.getDefault(), "?v=%s", Common.getAppVersion(this));

        if(TextUtils.isEmpty(path)) {
            path = App.isIsTEST() ? getResources().getString(R.string.url_site_test)+cacheParam : getResources().getString(R.string.url_site)+cacheParam; // + getResources().getString(R.string.url_webcontent_main, Common.getDefaultUUID(this, UUID.randomUUID().toString()));
        }

        myLog.d(TAG, "*** web path:"+path);

        initWebView(path);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                initPosition();
            }
        }, 1000);

        softKeyboardStateWatcher = new SoftKeyboardStateWatcher(findViewById(R.id.rootLayout));
        softKeyboardStateWatcher.addSoftKeyboardStateListener(new SoftKeyboardStateWatcher.SoftKeyboardStateListener() {
            @Override
            public void onSoftKeyboardOpened(int keyboardHeightInPx) {
                myLog.d(TAG, "*** onSoftKeyboardOpened: "+keyboardHeightInPx);
            }

            @Override
            public void onSoftKeyboardClosed() {
                myLog.d(TAG, "*** onSoftKeyboardClosed");

                //keybaordOutJsCall();
            }
        });
    }

    /**
     * 웹뷰 초기화
     *
     * @param url
     */
    private void initWebView(String url) {
        if(mWebView == null) return;

        MyWebChromeClient myWebChromeClient = new MyWebChromeClient();
        myWebChromeClient.setMyWebInterface(new MyWebInterface() {
            @Override
            public void onTitleChanged(String titleText) {
                myLog.d(TAG, "*** onTitleChanged: "+ titleText);

                TextView sub_top_title = findViewById(R.id.sub_top_title);
                sub_top_title.setText(titleText);
            }

            @Override
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
                filePathCallbackNormal = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_NORMAL_REQ_CODE);
            }

            @Override
            public void openFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {

//                if (filePathCallbackLollipop != null) {
////                    filePathCallbackLollipop.onReceiveValue(null);
//                    filePathCallbackLollipop = null;
//                }

                filePathCallbackLollipop = filePathCallback;


                // Create AndroidExampleFolder at sdcard
                File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "AndroidExampleFolder");
                if (!imageStorageDir.exists()) {
                    // Create AndroidExampleFolder at sdcard
                    imageStorageDir.mkdirs();
                }

                // Create camera captured image file path and name
                File file = new File(imageStorageDir + File.separator + "IMG_" + String.valueOf(System.currentTimeMillis()) + ".jpg");
                mCapturedImageURI = Uri.fromFile(file);

                Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);

                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");

                // Create file chooser intent
                Intent chooserIntent = Intent.createChooser(i, getResources().getString(R.string.file_choose_title));
                // Set camera intent to file chooser
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[]{captureIntent});

                // On select image call onActivityResult method of activity
                startActivityForResult(chooserIntent, FILECHOOSER_LOLLIPOP_REQ_CODE);

            }

        });

        mWebView.setWebChromeClient(myWebChromeClient);

        mWebView.setWebViewClient(new MyWebViewClient(this, new WebViewInterface() {
            @Override
            public void installKFTC() {
                installKFTC();
            }

            @Override
            public void installISP() {
                installISP();
            }

            @Override
            public String makeBankPayData(String str) {
                return makeBankPayData(str);
            }

            @Override
            public void loadUrl(final String url) {
                mWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        mWebView.loadUrl(url);
                    }
                });
            }
        }));

        /**
         * MyWebview에서 처리함.
         */
//        setWebViewOptions();

        mWebView.loadUrl(url);

        HandleUiListener handleUiListener = new HandleUiListener() {
            @Override
            public void setBackButton(final boolean show) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(show) {
                            sub_top_back_btn_box.setVisibility(View.VISIBLE);
                        } else {
                            sub_top_back_btn_box.setVisibility(View.GONE);
                        }
                    }
                });
            }

            @Override
            public void setTitleText(final String title) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView sub_top_title = findViewById(R.id.sub_top_title);
                        sub_top_title.setText(title);
                    }
                });
            }

            @Override
            public void sendShare(final int target, final String title, final String message, final String imageUrl, final String linkUrl) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        KakaoTalklink kakaoTalklink = new KakaoTalklink(WebContentActivity.this);

                        String _title = !TextUtils.isEmpty(title) ? title : getResources().getString(R.string.share_title, getResources().getString(R.string.app_name));
                        String _message = !TextUtils.isEmpty(message) ? message : getResources().getString(R.string.share_message);
                        String _imageUrl = !TextUtils.isEmpty(imageUrl) ? imageUrl : getResources().getString(R.string.url_site)+getResources().getString(R.string.share_image_url);
                        String _linkUrl = !TextUtils.isEmpty(linkUrl) ? linkUrl : "https://play.google.com/store/apps/details?id="+ getPackageName();

                        kakaoTalklink.postMessage(_title, _message, _imageUrl, _linkUrl);
                    }
                });
            }

            @Override
            public void openCamera(String idx, String type) {
//                if(cameraModule!=null) cameraModule.openCameraShot(idx, type, "");
            }

            @Override
            public void onceCamera(String idx, String type, String seq) {
//                if(cameraModule!=null) cameraModule.openCameraShot(idx, type, seq);
            }

            @Override
            public void openKakaoLogin(String callback) {
                KakaoTalklink.getInstance(WebContentActivity.this).loginKakao(new Handler(msg -> {
                    switch(msg.what) {
                        case KakaoTalklink.KAKAO_SUCCESS:
                            callJavascriptCallBack(Common.buildCallbackWithValue(callback, new String[]{"0"}));
                            break;
                        case KakaoTalklink.KAKAO_FAILURE:
                            callJavascriptCallBack(Common.buildCallbackWithValue(callback, new String[]{"1"}));
                            break;
                        default:
                            break;
                    }
                    return true;
                }));
            }
        };

        //handle downloading
        mWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimeType,
                                        long contentLength) {

                myLog.e(TAG, "*** onDownloadStart: "+url);

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
                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                dm.enqueue(request);

                Toast.makeText(getApplicationContext(), getResources().getString(R.string.file_downloading_message), Toast.LENGTH_LONG).show();

            }});

        mWebView.addJavascriptInterface(new JavaScriptInterface(this, mWebView, handleUiListener), Constant.WEBVIEW_BRIDGE_PREFIX);
    }

//    @Override
//    public void finish() {
//
//        Intent intent = new Intent();
//        setResult(RESULT_OK, intent);
//
//        super.finish();
//        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
//    }

    private void webViewCallback(String callback, String ...values) {
        callJavascriptCallBack(Common.buildCallbackWithValue(callback, values));
    }

    /**
     *  var message = {
     *      'action': 'bind',
     *      'name': 'message'
     *  };
     *  webkit.messageHandlers.[javascriptBridge].postMessage(message);
     *
     *
     * @param callbackScriptFunction
     */

    public void callJavascriptCallBack(String callbackScriptFunction) {
        if(mWebView!=null) {
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl("javascript:" + callbackScriptFunction);
                }
            });
        } else {
            myLog.e(TAG, "*** WEBVIEW IS NULL!");
        }
    }

    /**
     * 웹뷰 옵션 설정
     */
    private void setWebViewOptions() {

        WebSettings s = mWebView.getSettings();
        mWebView.setScrollBarStyle(ScrollView.SCROLLBARS_OUTSIDE_OVERLAY);

        s.setUserAgentString(Constant.WEBVIEW_USER_AGENT);
        s.setJavaScriptCanOpenWindowsAutomatically(true);
        s.setLoadsImagesAutomatically(true);
        s.setDomStorageEnabled(true);
        if (Build.VERSION.SDK_INT >= 8 && Build.VERSION.SDK_INT <= 18) {
            s.setPluginState(WebSettings.PluginState.ON);
        }
        s.setRenderPriority(WebSettings.RenderPriority.HIGH);
        s.setCacheMode(WebSettings.LOAD_NO_CACHE);
        s.setDatabaseEnabled(true);
        s.setAppCacheEnabled(true);
        s.setSupportMultipleWindows(true);
        s.setDefaultTextEncodingName("UTF-8");
        s.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);

        s.setBuiltInZoomControls(true);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);
        s.setSavePassword(true);
        s.setSaveFormData(true);
        s.setJavaScriptEnabled(true);
        s.setSupportZoom(false);
        s.setTextZoom(100);

        // enable navigator.geolocation
        s.setGeolocationEnabled(true);
        s.setGeolocationDatabasePath("/data/data/" + mContext.getString(R.string.path));

        // enable Web Storage: localStorage, sessionStorage
        s.setDomStorageEnabled(true);

        // caching.
        s.setAppCacheEnabled(true);
        s.setAppCachePath(mContext.getCacheDir().getPath());
        s.setCacheMode(WebSettings.LOAD_DEFAULT);

        s.setAllowFileAccess(true);
        s.setAllowFileAccessFromFileURLs(true);

        // CHROME DEBUG
        if(myLog.debugMode) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                mWebView.setWebContentsDebuggingEnabled(true);
        }

        // Webview 설정 - 쿠키 등 결제를 위한 설정
        /**************************************************************
         * 안드로이드 5.0 이상으로 tagetSDK를 설정하여 빌드한경우 아래 구문을 추가하여 주십시요
         **************************************************************/
        if( android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
            mWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            cookieManager.setAcceptThirdPartyCookies(mWebView, true);
        }

    }

    @Override
    public void finish() {

        super.finish();

        //overridePendingTransition(R.anim.fade_out_100_50, R.anim.slide_out_right);

    }

    private boolean isBackPressed = false;
    private Handler exitHandler;

    /**
     * 백버튼 핸들 Twice처리
     */
    @Override
    public void onBackPressed() {

        if(!isBackPressed) {

            if(Constant.EXIT_WITH_PRE_WEBVIEW_HISTORY) {
                /**
                 * Webview history가 없을 때 종료하는 경우
                 */
                if (mWebView != null && mWebView.canGoBack()) {
                    mWebView.goBack();
                } else {
                    isBackPressed = true;

                    if (exitHandler == null) exitHandler = new Handler();

                    exitHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isBackPressed = false;
                            Toast.makeText(WebContentActivity.this, getResources().getString(R.string.twicback_to_exit_message), Toast.LENGTH_SHORT).show();
                        }
                    }, 300);
                }
            } else {
                /**
                 * Webview history와 관계없이 종료시키는 경우!
                 */
                isBackPressed = true;

                if (exitHandler == null) exitHandler = new Handler();

                exitHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isBackPressed = false;

                        if(mWebView!=null && mWebView.canGoBack()) {
                            mWebView.goBack();
                        } else {
                            Toast.makeText(WebContentActivity.this, getResources().getString(R.string.twicback_to_exit_message), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, 300);
            }
        } else if(isBackPressed) {

            if(exitHandler!=null) exitHandler.removeCallbacksAndMessages(null);

            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        myLog.d(TAG, "*** onActivityResult requestCode (" + requestCode + "), resultCode (" + resultCode + ")");

        switch (requestCode) {
            case FILECHOOSER_NORMAL_REQ_CODE: {
                if (filePathCallbackNormal == null) return;
                Uri result = (data == null || resultCode != RESULT_OK) ? null : data.getData();
                filePathCallbackNormal.onReceiveValue(result);
                filePathCallbackNormal = null;
                break;
            }

            case FILECHOOSER_LOLLIPOP_REQ_CODE: {
                if (filePathCallbackLollipop == null) return;
                Uri[] result = new Uri[0];
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    if(resultCode == RESULT_OK){
                        result = (data == null) ? new Uri[]{mCapturedImageURI} : WebChromeClient.FileChooserParams.parseResult(resultCode, data);
                    }
                    filePathCallbackLollipop.onReceiveValue(result);
                }

                break;
            }

            case FILECHOOSE_CALLBACK:
                if(resultCode == RESULT_OK) {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mFileCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
                    } else {
                        mFileCallback.onReceiveValue(new Uri[]{data.getData()});
                    }
                    mFileCallback = null;
                } else {
                    mFileCallback.onReceiveValue(null);
                }

                break;
        }

        if(data!=null && data.getExtras()!=null) {
            String resVal = data.getExtras().getString("bankpay_value");
            String resCode = data.getExtras().getString("bankpay_code");
            myLog.i("NICE","resCode : "+ resCode);
            myLog.i("NICE","resVal : "+ resVal);

            if("091".equals(resCode)){//계좌이체 결제를 취소한 경우
                Utility.AlertDialog("인증 오류", "계좌이체 결제를 취소하였습니다.",  this);
                mWebView.postUrl(MERCHANT_URL, null);
            } else if("060".equals(resCode)){
                Utility.AlertDialog("인증 오류", "타임아웃",  this);
                mWebView.postUrl(MERCHANT_URL, null);
            } else if("050".equals(resCode)){
                Utility.AlertDialog("인증 오류", "전자서명 실패",  this);
                mWebView.postUrl(MERCHANT_URL, null);
            } else if("040".equals(resCode)){
                Utility.AlertDialog("인증 오류", "OTP/보안카드 처리 실패",  this);
                mWebView.postUrl(MERCHANT_URL, null);
            } else if("030".equals(resCode)){
                Utility.AlertDialog("인증 오류", "인증모듈 초기화 오류",  this);
                mWebView.postUrl(MERCHANT_URL, null);
            } else if("000".equals(resCode)){	// 성공일 경우
                String postData = "callbackparam2="+BANK_TID+"&bankpay_code="+resCode+"&bankpay_value="+resVal;
                mWebView.postUrl(NICE_BANK_URL, EncodingUtils.getBytes(postData,"euc-kr"));
            }
        }
    }

    class updateLocationData extends TimerTask {

        @Override
        public void run() {

            myLog.d(TAG, "*** updateLocationData!");

            if(App.getCurrentLocation()!=null) {
                myLog.d(TAG, "*** updateLocationData! - location:"+App.getCurrentLocation());
                LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(WebContentActivity.this);
                localBroadcastManager.sendBroadcast(new Intent(WebContentActivity.ACTION_UPDATE_LOCATION));
            }
        }
    }

    /**
     *
     * 계좌이체 데이터를 파싱한다.
     *
     * @param str
     * @return
     */
    private String makeBankPayData(String str) {
        String[] arr = str.split("&");
        String[] parse_temp;
        HashMap<String, String> tempMap = new HashMap<String,String>();

        for( int i=0;i<arr.length;i++ ) {
            try {
                parse_temp = arr[i].split("=");
                tempMap.put(parse_temp[0], parse_temp[1]);
            } catch(Exception e){

            }
        }

        BANK_TID = tempMap.get("user_key");
        NICE_BANK_URL = tempMap.get("callbackparam1");
        return str;
    }

    /**
     * 	ISP가 설치되지 않았을때 처리를 진행한다.
     *
     *
     */
    private void installISP() {
        AlertDialog.Builder d = new AlertDialog.Builder(this);
        d.setMessage("ISP결제를 하기 위해서는 ISP앱이 필요합니다.\n설치 페이지로  진행하시겠습니까?");
        d.setTitle( "ISP 설치" );
        d.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(ISP_LINK));
                startActivity(intent);
            }
        });
        d.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                //결제 초기 화면을 요청합니다.
                mWebView.postUrl(MERCHANT_URL, null);

            }
        });
        d.show();
    }
    /**
     * 	계좌이체 BANKPAY 설치 진행 안내
     *
     *
     */
    private void installKFTC() {
        AlertDialog.Builder d = new AlertDialog.Builder(this);
        d.setMessage("계좌이체 결제를 하기 위해서는 BANKPAY 앱이 필요합니다.\n설치 페이지로  진행하시겠습니까?");
        d.setTitle( "계좌이체 BANKPAY 설치" );
        d.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(KFTC_LINK));
                startActivity(intent);
            }
        });
        d.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                mWebView.postUrl(MERCHANT_URL, null);
            }
        });
        d.show();
    }

}
