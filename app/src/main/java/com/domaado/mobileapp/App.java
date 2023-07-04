package com.domaado.mobileapp;

import android.app.Activity;
import android.app.Application;
import android.location.Location;

import com.domaado.mobileapp.data.QueryParams;
import com.domaado.mobileapp.widget.myLog;
import com.onesignal.OSDeviceState;
import com.onesignal.OneSignal;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;

@ReportsCrashes(formKey="",formUri = "http://creport.hongeuichan.com/index.php",
	mode = ReportingInteractionMode.DIALOG,
	forceCloseDialogAfterToast = false, 
	reportType = HttpSender.Type.JSON,
	httpMethod = org.acra.sender.HttpSender.Method.POST,
	resDialogTitle = R.string.crash_dialog_title,
	resDialogText = R.string.crash_dialog_text
)

public class App extends Application {

	private String TAG = App.class.getSimpleName();
	
	private static App myApp;
	private static Activity activity;
	
	public App() {
		myApp = this;
	}
	public static App Instance(){return myApp;};

	public static QueryParams queryParams;

	public static String callCenterTel;

	public static String centerIdx;
	public static String driverIdx;
	public static Location currentLocation;

	public static boolean isTEST = false;

	public static byte[] keyByte;
	public static byte[] ivByte;
	
	@Override
	public void onCreate() {
		
		// ACRA  
		ACRA.init(this);
		
		CrashReportSender sender = new CrashReportSender("http://creport.hongeuichan.com/index.php", null);
		ACRA.getErrorReporter().setReportSender(sender);
		// ACRA

		queryParams = new QueryParams();

		// Logging set to help debug issues, remove before releasing your app.
		OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);

		// OneSignal Initialization
		OneSignal.initWithContext(this);
		OneSignal.setAppId(Constant.ONESIGNAL_APP_ID);

		// promptForPushNotifications will show the native Android notification permission prompt.
		// We recommend removing the following code and instead using an In-App Message to prompt for notification permission (See step 7)
		OneSignal.promptForPushNotifications();

		//OSDeviceState osDeviceState = OneSignal.getDeviceState();

		super.onCreate();
	}

	@Override
	public void onTerminate() {
		super.onTerminate();

		myLog.d(TAG, "*** onTerminate");
	}
	
	public synchronized static void setActivity(Activity act) {
		activity = act;
	}
	
	public synchronized static Activity getActivity() {
		if(activity == null) {
			activity = getActivity();
		}
		return activity;
	}

	public static boolean isIsTEST() {
		return isTEST;
	}

	public static void setIsTEST(boolean isTEST) {
		App.isTEST = isTEST;
	}

	public synchronized static void setQueryParam(QueryParams _queryParams) {
		queryParams = _queryParams;
	}

	public synchronized static void resetQueryParam() {
		if(queryParams!=null) {
			queryParams.setTitle(null);
			queryParams.setMessage(null);
			queryParams.setResponseId(null);
			queryParams.setAction(null);
			queryParams.setLat(0);
			queryParams.setLon(0);
			queryParams.setUrl("");
		}
	}

	public synchronized static QueryParams getQueryParams() {
		return queryParams;
	}

	public synchronized static String getCallCenterTel() {
		return callCenterTel;
	}

	public synchronized static void setCallCenterTel(String callCenterTel) {
		App.callCenterTel = callCenterTel;
	}

	public synchronized static String getCenterIdx() {
		return centerIdx;
	}

	public synchronized static void setCenterIdx(String centerIdx) {
		App.centerIdx = centerIdx;
	}

	public synchronized static String getDriverIdx() {
		return driverIdx;
	}

	public synchronized static void setDriverIdx(String driverIdx) {
		App.driverIdx = driverIdx;
	}

	public synchronized static Location getCurrentLocation() {
		if(currentLocation==null) currentLocation = new Location("gps");
		return currentLocation;
	}

	public synchronized static void setCurrentLocation(Location currentLocation) {
		App.currentLocation = currentLocation;
	}

	public static byte[] getKeyByte() {
		return keyByte;
	}

	public static void setKeyByte(byte[] keyByte) {
		App.keyByte = keyByte;
	}

	public static byte[] getIvByte() {
		return ivByte;
	}

	public static void setIvByte(byte[] ivByte) {
		App.ivByte = ivByte;
	}
}
