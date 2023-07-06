package com.domaado.mobileapp;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.domaado.mobileapp.data.PhotoEntry;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.skt.Tmap.TMapView;
import com.domaado.mobileapp.locale.LocaleUtils;
import com.domaado.mobileapp.sensors.BluetoothTetheringHelper;
import com.domaado.mobileapp.task.GetInternetStatus;
import com.domaado.mobileapp.type.LatLng;
import com.domaado.mobileapp.widget.CustomAlertDialog;
import com.domaado.mobileapp.widget.CustomAlertDialogInput;
import com.domaado.mobileapp.widget.CustomToast;
import com.domaado.mobileapp.widget.DateUtil;
import com.domaado.mobileapp.widget.myLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.kakao.util.helper.Utility.getPackageInfo;
import static com.domaado.mobileapp.Constant.CONFIG_USERLANGUAGE;
import static com.domaado.mobileapp.Constant.CONFIG_USERLANGUAGE_SERVER_VALUE;
import static com.domaado.mobileapp.Constant.CONNECT_TIMEOUT;

public class Common {

	public static String TAG = "Common";

	public static String PreferenceTAG = "chauffeur";

	public static String getConfig(Context ctx, String key) {
		//ctx = App.getContext();

		String value = getSharedPreferencesString(key, ctx);

		if (TextUtils.isEmpty(value) && CONFIG_USERLANGUAGE.equals(key))
			value = Locale.getDefault().getLanguage();

		return value;
	}

	public static void setConfig(Context ctx, String key, String value) {

		myLog.d(TAG, "*** setConfig: key:" + key + ", value:" + value);

		saveSharedPreferencesString(key, value, ctx);
	}

	public static boolean isSetConfig(Context ctx, String key) {
		String value = getSharedPreferencesString(key, ctx);

		return !TextUtils.isEmpty(value);
	}

	public static Locale getConfigLanguageLocale(Context context) {

		String language = getSharedPreferencesString(CONFIG_USERLANGUAGE, context);

		if (TextUtils.isEmpty(language)) {
			language = Locale.getDefault().getLanguage();
		}

		Locale locale;

		if (LocaleUtils.ENGLISH.equals(language)) locale = Locale.US;
		else if (LocaleUtils.FRENCH.equals(language)) locale = Locale.FRANCE;
		else if (LocaleUtils.CHINESE.equals(language)) locale = Locale.SIMPLIFIED_CHINESE;
		else if (LocaleUtils.KOREAN.equals(language)) locale = Locale.KOREAN;
		else if (LocaleUtils.JAPANESE.equals(language)) locale = Locale.JAPANESE;
		else locale = Locale.ENGLISH;

		return locale;
	}

	public static String getConfigForServer(Context ctx, String key) {
		String language = getSharedPreferencesString(key, ctx);
		int seq = 0;

		if(LocaleUtils.ENGLISH.equals(language)) seq = 1;
		else if(LocaleUtils.CHINESE.equals(language)) seq = 2;
		else if(LocaleUtils.JAPANESE.equals(language)) seq = 3;
		else if(LocaleUtils.FRENCH.equals(language)) seq = 4;

		return CONFIG_USERLANGUAGE_SERVER_VALUE[seq];
	}

	public static int getConfigLanguageForTMAP(Context ctx) {
		String language = getSharedPreferencesString(CONFIG_USERLANGUAGE, ctx);
		int seq = TMapView.LANGUAGE_KOREAN;

		if(LocaleUtils.ENGLISH.equals(language)) seq = TMapView.LANGUAGE_ENGLISH;
		else if(LocaleUtils.CHINESE.equals(language)) seq = TMapView.LANGUAGE_CHINESE;
		else if(LocaleUtils.JAPANESE.equals(language)) seq = TMapView.LANGUAGE_JAPANESE;
		else if(LocaleUtils.FRENCH.equals(language)) seq = TMapView.LANGUAGE_ENGLISH; // 프랑스어 지원 없음.

		return seq;
	}

	public static void setTaskBarColored(Activity act, int keyboardHeight) {
		try {

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				Window w = act.getWindow();

				w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
						WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

				// status bar height
				int statusBarHeight = Common.getStatusBarHeight(act);

				View view = new View(act);
				view.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT));

				// 키보드가 올라와 있으면 그만큼 제외하고 높이를 설정한다.
				view.getLayoutParams().height = statusBarHeight - keyboardHeight;

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
					view.setBackgroundColor(act.getResources().getColor(R.color.colorPrimary, null));
				else
					view.setBackgroundColor(act.getResources().getColor(R.color.colorPrimary));

				//ContextCompat
				((ViewGroup) w.getDecorView()).addView(view);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static int getStatusBarHeight(Context ctx) {
		int result = 0;
		int resourceId = ctx.getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = ctx.getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}

	public static boolean saveSharedPreferencesString(String item, String text, Context ctx) {
		boolean ret = false;

		try {
			SharedPreferences pref = ctx.getSharedPreferences(PreferenceTAG, Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
			SharedPreferences.Editor edit = pref.edit();

			if (!TextUtils.isEmpty(text)) {
				edit.putString(item, text);
			}

			ret = edit.commit();
			//ret = true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ret;
	}

	public static boolean saveSharedPreferences(String item, boolean value, String text, Context ctx) {
		boolean ret = false;

		try {
			SharedPreferences pref = ctx.getSharedPreferences(Common.PreferenceTAG, Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
			SharedPreferences.Editor edit = pref.edit();

			if (!TextUtils.isEmpty(text)) {
				//ret = pref.edit().putString(item, text).commit();
				edit.putString(item, text);
			} else {
				//ret = pref.edit().putBoolean(item, value).commit();
				edit.putBoolean(item, value);
			}

			ret = edit.commit();
			//ret = true;
		} catch (Exception e) {
			e.printStackTrace();
			//myLog.e(TAG, e.toString());
		}

		//myLog.d(TAG, "-- saveSharedPreferences: Common.PreferenceTAG:"+Common.PreferenceTAG+", ret:"+ret+", item:"+item+", value:"+value+", text:"+text+", ctx:"+ctx);

		return ret;
	}

	public static boolean saveSharedPreferences(String item, int value, Context ctx) {
		boolean ret = false;

		try {
			SharedPreferences pref = ctx.getSharedPreferences(Common.PreferenceTAG, Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
			SharedPreferences.Editor edit = pref.edit();

			edit.putInt(item, value);

			ret = edit.commit();

		} catch (Exception e) {
			e.printStackTrace();
		}

		//myLog.d(TAG, "-- saveSharedPreferences: Common.PreferenceTAG:"+Common.PreferenceTAG+", ret:"+ret+", item:"+item+", value:"+value+", ctx:"+ctx);

		return ret;
	}

	public static boolean getSharedPreferencesBoolean(String item, Context ctx) {
		Boolean ret = false;

		SharedPreferences pref = ctx.getSharedPreferences(Common.PreferenceTAG, Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);

		try {
			ret = pref.getBoolean(item, false);
		} catch (ClassCastException e) {
			removeSharedPreferences(item, ctx);
		}

		return ret;
	}

	public static int getSharedPreferencesInt(String item, Context ctx, int defaultValue) {
		int ret = defaultValue;

		SharedPreferences pref = ctx.getSharedPreferences(Common.PreferenceTAG, Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);

		try {
			ret = pref.getInt(item, defaultValue);
		} catch (ClassCastException e) {
			removeSharedPreferences(item, ctx);
		}

		return ret;
	}

	public static String getSharedPreferencesString(String item, Context ctx) {
		String ret = "";

		if (ctx == null) return ret;

		try {
			SharedPreferences pref = ctx.getSharedPreferences(Common.PreferenceTAG, Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);

			ret = pref.getString(item, null);
		} catch (Exception e) {
			e.printStackTrace();
//			removeSharedPreferences(item, ctx);
			ret = "";
		}

		//myLog.d(TAG, "-- getSharedPreferencesString: Common.PreferenceTAG:"+Common.PreferenceTAG+", ret:"+ret+", item:"+item+", ctx:"+ctx);

		return ret;
	}

	public static String getSharedPreferencesString(SharedPreferences pref, String item, Context ctx) {
		String ret = null;

		try {
			ret = pref.getString(item, null);
		} catch (ClassCastException e) {
			e.printStackTrace();
//			removeSharedPreferences(item, ctx);
			ret = "";
		}

		//myLog.d(TAG, "-- getSharedPreferencesString: Common.PreferenceTAG:"+Common.PreferenceTAG+", ret:"+ret+", item:"+item+", ctx:"+ctx);

		return ret;
	}

	public static boolean removeSharedPreferences(String item, Context ctx) {
		boolean ret = false;

		try {
			SharedPreferences pref = ctx.getSharedPreferences(Common.PreferenceTAG, Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);

//			ret = pref.edit().remove(item).commit();

			SharedPreferences.Editor edit = pref.edit();
			edit.remove(item);
			ret = edit.commit();
//		    ret = true;
		} catch (Exception e) {
			myLog.e(TAG, e.toString());
		}

		myLog.d(TAG, "-- removeSharedPreferences: Common.PreferenceTAG:" + Common.PreferenceTAG + ", ret:" + ret + ", item:" + item + ", ctx:" + ctx);

		return ret;
	}

	public static float convertDpToPixel(Context context, float dp) {
//	    Resources resources = context.getResources();
//	    DisplayMetrics metrics = resources.getDisplayMetrics();
//	    float px = dp * (metrics.densityDpi / 160f);
//	    return px;

		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
	}

	/**
	 * This method converts device specific pixels to density independent pixels.
	 *
	 * @param px A value in px (pixels) unit. Which we need to convert into db
	 * @param context Context to get resources and device specific display metrics
	 * @return A float value to represent dp equivalent to px value
	 */
	public static float convertPixelsToDp(Context context, float px) {
//	    Resources resources = context.getResources();
//	    DisplayMetrics metrics = resources.getDisplayMetrics();
//	    float dp = px / (metrics.densityDpi / 160f);
//	    return dp;
		return px / context.getResources().getDisplayMetrics().density;

		//return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, context.getResources().getDisplayMetrics());
	}

	/**
	 * 숫자변환이 가능한지 검사한다.!
	 *
	 * @param str
	 * @return
	 */
	public static boolean isNumeric(String str) {
		try {
			Double.parseDouble(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	public static String getVersionInfo(Context ctx) {
		PackageInfo pinfo = null;
		try {
			pinfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int versionNumber = pinfo != null ? pinfo.versionCode : 0;
		String versionName = pinfo != null ? pinfo.versionName : "";

		return versionName + " (" + versionNumber + ")";
	}

	public static String getAppVersion(Context ctx) {
		PackageInfo pinfo = null;
		try {
			pinfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int versionNumber = pinfo != null ? pinfo.versionCode : 0;
		String versionName = pinfo != null ? pinfo.versionName : "";

		return versionName + "." + versionNumber;
	}

	public static String getVersionBuildCode(Context ctx) {
		PackageInfo pinfo = null;
		try {
			pinfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int versionNumber = pinfo != null ? pinfo.versionCode : 0;

		return String.valueOf(versionNumber);
	}

	public static String getDayAndWeek(Context ctx) {
		String result = "";

		Calendar c = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("MM.dd", Locale.KOREA);

		String nowDateString = sdf.format(c.getTime());

		String Week = getDayOfWeek(ctx);

		result = nowDateString + " " + Week;

		return result;
	}

	@SuppressLint("SimpleDateFormat")
	public static String getDate(long timeStamp) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date netDate = (new Date(timeStamp));
			return sdf.format(netDate);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public static int getYear() {
		Calendar c = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy");

		int year = Integer.parseInt(sdf.format(c.getTime()));

		return year;
	}

	@SuppressLint("SimpleDateFormat")
	public static String getDateYMD(long timeStamp) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date netDate = (new Date(timeStamp));
			return sdf.format(netDate);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public static String getDate() {

		Calendar c = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		String nowDateString = sdf.format(c.getTime());

		return nowDateString;
	}

	/**
	 *
	 * @param fmt
	 * @return
	 */
	public static String getDate(String fmt) {

		Calendar c = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(fmt);

		String nowDateString = sdf.format(c.getTime());

		return nowDateString;
	}

	public static String getDateOfTimestamp(Object tsobj, boolean enableTime) {
		String LocaleStr = java.util.Locale.getDefault().toString();
		SimpleDateFormat sdf;

		String tf = enableTime == true ? " HH:mm:ss" : "";
		long ts = 0;
		if (tsobj instanceof String) {
			String tsstr = (String) tsobj;
			if (!TextUtils.isEmpty(tsstr)) {
				ts = Long.parseLong(tsstr);
			}
		}

		if (LocaleStr.startsWith("ko") || LocaleStr.startsWith("kr")) {
			sdf = new SimpleDateFormat("yyyy-MM-dd" + tf, Locale.KOREA);
		} else {
			sdf = new SimpleDateFormat("MM/dd/yyyy" + tf, Locale.ENGLISH);
		}
		Date date = new Date(ts);

		String nowDateString = sdf.format(date);

		return nowDateString;
	}

	public static String getDayOfWeek(Context ctx) {

		String result = null;
		Date newDate = new Date();

		String nowDateString = DateFormat.format(DateUtil.orgDateFormat,
				newDate).toString();

		try {
			int dayOfWeek = DateUtil.getWeek(nowDateString);

			switch (dayOfWeek) {
				case 1: // sun
					result = "  Sun";
					break;
				case 2: // mon
					result = "  Mon";
					break;
				case 3:
					result = "  Tue";
					break;
				case 4:
					result = "  Wed";
					break;
				case 5:
					result = "  Thu";
					break;
				case 6:
					result = "  Fri";
					break;
				case 7: // sat
					result = "  Sat";
					break;
			}

		} catch (ParseException e) {
			e.printStackTrace();
		}

		return result;
	}

	public static int getUniqueID() {
		String str = String.valueOf(System.currentTimeMillis());
		String idstr = str.substring(str.length() - 6, str.length());

		return Integer.parseInt(idstr);
	}

	public static String getFileName() {

		Calendar c = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");

		String nowDateString = sdf.format(c.getTime());

		return nowDateString;
	}

	public static double eval(final String str) {
		return new Object() {
			int pos = -1, ch;

			void nextChar() {
				ch = (++pos < str.length()) ? str.charAt(pos) : -1;
			}

			boolean eat(int charToEat) {
				while (ch == ' ') nextChar();
				if (ch == charToEat) {
					nextChar();
					return true;
				}
				return false;
			}

			double parse() {
				nextChar();
				double x = parseExpression();
				if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char) ch);
				return x;
			}

			// Grammar:
			// expression = term | expression `+` term | expression `-` term
			// term = factor | term `*` factor | term `/` factor
			// factor = `+` factor | `-` factor | `(` expression `)`
			//        | number | functionName factor | factor `^` factor

			double parseExpression() {
				double x = parseTerm();
				for (; ; ) {
					if (eat('+')) x += parseTerm(); // addition
					else if (eat('-')) x -= parseTerm(); // subtraction
					else return x;
				}
			}

			double parseTerm() {
				double x = parseFactor();
				for (; ; ) {
					if (eat('*')) x *= parseFactor(); // multiplication
					else if (eat('/')) x /= parseFactor(); // division
					else return x;
				}
			}

			double parseFactor() {
				if (eat('+')) return parseFactor(); // unary plus
				if (eat('-')) return -parseFactor(); // unary minus

				double x = 0d;
				int startPos = this.pos;
				if (eat('(')) { // parentheses
					x = parseExpression();
					eat(')');
				} else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
					while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
					x = Double.parseDouble(str.substring(startPos, this.pos));
				} else if (ch >= 'a' && ch <= 'z') { // functions
					while (ch >= 'a' && ch <= 'z') nextChar();
					String func = str.substring(startPos, this.pos);
					x = parseFactor();
					if (func.equals("sqrt")) x = Math.sqrt(x);
					else if (func.equals("sin")) x = Math.sin(Math.toRadians(x));
					else if (func.equals("cos")) x = Math.cos(Math.toRadians(x));
					else if (func.equals("tan")) x = Math.tan(Math.toRadians(x));
					else {
						myLog.e(TAG, "**** eval error: unknown function: " + func);
						//throw new RuntimeException("Unknown function: " + func);
					}
				} else {
					myLog.e(TAG, "**** eval error: Unexpected: " + (char) ch);
					//throw new RuntimeException("Unexpected: " + (char)ch);
				}

				if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation

				return x;
			}
		}.parse();
	}

	public static double toFixed(double value, int digit) {
		double ret = 0d;

		try {
			String tmp = String.format(Locale.US, "%." + digit + "f", value);
			ret = Double.parseDouble(tmp);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ret;
	}

	/**
	 * 동작중인 앱의 언어를 변경합니다.
	 *
	 * @param context
	 * @param language
	 */
	public static void setDefaultLanguage(Context context, String language) {
		try {

			String[] langs = context.getResources().getStringArray(R.array.support_language_code);

			Locale locale = new Locale(language);

			for (String lang : langs) {
				if (language.equals(lang)) {
					String[] langArr = lang.split("_");
					if (langArr != null && langArr.length > 1) {
						locale = new Locale(langArr[0], langArr[1]);
						break;
					} else if (language.equals("zh-TW")) {
						locale = Locale.TAIWAN;
						break;
					}
				}

			}

			Locale.setDefault(locale);

			myLog.d(TAG, "** setDefaultLanguage: " + language);

			Resources resources = context.getResources();

			Configuration configuration = resources.getConfiguration();
			configuration.locale = locale;

			resources.updateConfiguration(configuration, resources.getDisplayMetrics());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String arrayToJSONstringify(ArrayList<int[]> list) {

		JSONObject jResult = new JSONObject();
		JSONArray jArray = new JSONArray();

		for (int i = 0; i < list.size(); i++) {
			JSONObject jGroup = new JSONObject();

			int[] row = list.get(i);
			for (int j = 0; j < row.length; j++) {
				try {
					jGroup.put(String.valueOf(j), row[j]);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			jArray.put(jGroup);
		}

		try {
			jResult.put("data", jArray);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return jResult.toString();
	}

	public static boolean getUSIMState(Context ctx) {
		boolean bUsimCheck = false;

		TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);

		bUsimCheck = tm.getSimState() != TelephonyManager.SIM_STATE_ABSENT;

		return bUsimCheck;
	}

	/**
	 * Wifi 감도 return
	 *
	 * @param ctx
	 * @return
	 */
	public static int getWifiRssi(Context ctx) {
		int ret = 0;
		String service = Context.WIFI_SERVICE;
		final WifiManager wifiManager = (WifiManager) ctx
				.getSystemService(service);

		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		ret = wifiInfo.getRssi();

		return ret;
	}

	public static boolean checkNetworkStatus(Context ctx) {
		boolean ret = false;

		ConnectivityManager connect = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (connect.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
			int rssiLevel = getWifiRssi(ctx);
			myLog.d(TAG, "WIFI-RSSI LEVEL:" + rssiLevel);

			if (rssiLevel >= -90) ret = true;

		} else if (getUSIMState(ctx) == true) {
			if (connect.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED) {
				myLog.d(TAG, "3G/4G Data connected!");
				ret = true;
			}
		}

		if (!ret) {
			// bluetooth tethering check!
			BluetoothTetheringHelper bluetoothTetheringHelper = new BluetoothTetheringHelper(ctx);
			if (bluetoothTetheringHelper.IsBluetoothTetherEnabled()) {
				myLog.d(TAG, "Bluetooth Tethering is enabled!");
				ret = true;
			}

			bluetoothTetheringHelper = null;
		}

		return ret;

	}

	public static boolean _checkNetworkStatus(Context context) {
		boolean flag = false;
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo[] netInfo = cm.getAllNetworkInfo();

		//it provide all type of connectivity ifo
		for (NetworkInfo ni : netInfo) {
			if (ni.getTypeName().equalsIgnoreCase("Connecxtion Type"))
				if (ni.isConnected())
					flag = true;
		}
		return flag;
	}

	public static boolean checkNetworkConnect(Activity act) {
		Boolean result = false;

		try {

			//if (checkNetworkStatus(act)) {
				//get the result after executing AsyncTask
				result = new GetInternetStatus(act).execute().get();
			//}

		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}

		return result;
	}

	/**
	 * check bluetooth on
	 *
	 * @param ctx
	 * @return
	 */
//	public static boolean checkBluetoothOn(Context ctx) {
//		boolean ret = false;
//
//		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//		if (bluetoothAdapter != null) {
//			if (bluetoothAdapter.isEnabled()) ret = true;
//		}
//
//		return ret;
//	}

	/**
	 * check gps on
	 *
	 * @param ctx
	 * @return
	 */
	public static boolean checkGpsOn(Context ctx) {
		boolean ret = false;

//		LocationManager locationManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
//		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) ret = true;

		LocationManager lm = (LocationManager)ctx.getSystemService(Context.LOCATION_SERVICE);
		boolean gps_enabled = false;
		boolean network_enabled = false;

		try {
			gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
		} catch(Exception ex) {}

		try {
			network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		} catch(Exception ex) {}

		return gps_enabled && network_enabled;
	}

	public static boolean isLocationEnabled(Context context) {
		int locationMode = 0;
		String locationProviders;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
			try {
				locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

			} catch (Settings.SettingNotFoundException e) {
				e.printStackTrace();
				return false;
			}

			return locationMode != Settings.Secure.LOCATION_MODE_OFF;

		}else{
			locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
			return !TextUtils.isEmpty(locationProviders);
		}

	}

	public static void callGPSSetting(Context ctx) {
		LocationManager lm = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
		if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			ctx.startActivity(intent);
		}
	}

	public static String replaceForSQL(String str) {
		//str = str.replaceAll("'", ""); // 프랑스어때문에 필터링하면 안됨!!!
		str = str.replaceAll("\"", "");
		str = str.replaceAll("\\*", "");
		str = str.replaceAll(" ", "%");

		return str;
	}

	public static String getPhoneNumber(Context ctx) {
		String mPhoneNumber = null;

		TelephonyManager tMgr = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
		if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.READ_PHONE_NUMBERS) == PackageManager.PERMISSION_GRANTED) {
			mPhoneNumber = tMgr.getLine1Number();

			if(!TextUtils.isEmpty(mPhoneNumber) && mPhoneNumber.startsWith("+82")) {
				mPhoneNumber = mPhoneNumber.replace("-","").replace("+82", "0");
			}
		}

		if(TextUtils.isEmpty(mPhoneNumber)) mPhoneNumber = "01012345678";

		return mPhoneNumber;
	}

	public static PackageInfo getVersion(Context ctx) {
		PackageInfo pinfo = null;
		try {
			pinfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return pinfo;
	}

	public static boolean appInstalled(Context ctx, String uri) {
		PackageManager pm = ctx.getPackageManager();

		try {
			pm.getPackageInfo(uri, 0); //PackageManager.GET_ACTIVITIES);
			return true;
		} catch (PackageManager.NameNotFoundException e) {
			return false;
		}
	}

	public static void appDownload(Context ctx, String pkgName) {
		Uri uri = Uri.parse("market://details?id=" + pkgName);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		ctx.startActivity(intent);
	}

	public static AlertDialog.Builder selectCameraDialog;
	public static AlertDialog selectCameraAlertDialog;

	public static void openDialog(final Activity context, final List<Intent> intents,
								   List<ResolveInfo> activitiesInfo) {

		selectCameraAlertDialog = null;

		String defPkgName = getSharedPreferencesString(Constant.DEFAULT_CAMERA_APP_KEY, context);

		if(!TextUtils.isEmpty(defPkgName)) {

			try {
				Intent intent = context.getPackageManager().getLaunchIntentForPackage(defPkgName);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setAction(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_LAUNCHER);

				context.startActivity(intent);
				return;
			} catch (Exception e) {
				e.printStackTrace();

				Toast.makeText(context, context.getResources().getString(R.string.notfound_camera_app), Toast.LENGTH_SHORT).show();
			}
		}

		if(intents!=null && intents.size()==1) {
			Intent intent = intents.get(0);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setAction(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);

			context.startActivity(intent);
		} else {

			if(selectCameraDialog==null) selectCameraDialog = new AlertDialog.Builder(context);
			selectCameraDialog.setTitle(context.getResources().getString(R.string.choose_camera_app));
			selectCameraDialog.setAdapter(buildAdapter(context, activitiesInfo),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							/**
							 * 이부분이 동작안됨.
							 */
							Intent intent = intents.get(id);
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							intent.setAction(Intent.ACTION_MAIN);
							intent.addCategory(Intent.CATEGORY_LAUNCHER);

							context.startActivity(intent);

						}
					});

			selectCameraDialog.setNeutralButton(context.getResources().getString(R.string.btn_cancel),
					new android.content.DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							removeSharedPreferences(Constant.DEFAULT_CAMERA_APP_KEY, context);
							dialog.dismiss();
						}
					});

			selectCameraAlertDialog = selectCameraDialog.show();

		}
	}

	public static CompoundButton lastCheckedBox;

	public static ArrayAdapter<ResolveInfo> buildAdapter(final Context context, final List<ResolveInfo> activitiesInfo) {
		return new ArrayAdapter<ResolveInfo>(context, R.layout.intent_listview_row, R.id.listview_row_title, activitiesInfo){
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = super.getView(position, convertView, parent);
				final ResolveInfo res=activitiesInfo.get(position);

				ImageView image=(ImageView) view.findViewById(R.id.listview_row_icon);
				image.setImageDrawable(res.loadIcon(context.getPackageManager()));

				TextView textview=(TextView)view.findViewById(R.id.listview_row_title);
				textview.setText(res.loadLabel(context.getPackageManager()).toString());

				CheckBox listview_row_always_check_btn = (CheckBox)view.findViewById(R.id.listview_row_always_check_btn);
				listview_row_always_check_btn.setChecked(false);
				listview_row_always_check_btn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						if(lastCheckedBox!=null) {
							lastCheckedBox.setChecked(false);
						}

						if(isChecked) {
							saveSharedPreferencesString(Constant.DEFAULT_CAMERA_APP_KEY, res.activityInfo.packageName, context);
							buttonView.setText("");
							lastCheckedBox = buttonView;
						} else {
							removeSharedPreferences(Constant.DEFAULT_CAMERA_APP_KEY, context);
							buttonView.setText(context.getResources().getString(R.string.btn_always));
							lastCheckedBox = null;
						}

					}
				});

				LinearLayout listview_row_box = view.findViewById(R.id.listview_row_box);
				listview_row_box.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						try {
							if (selectCameraAlertDialog != null) {
								selectCameraAlertDialog.dismiss();
							}
						} catch(Exception e) {
							e.printStackTrace();
						}

						Intent intent = context.getPackageManager().getLaunchIntentForPackage(res.activityInfo.packageName);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						intent.setAction(Intent.ACTION_MAIN);
						intent.addCategory(Intent.CATEGORY_LAUNCHER);

						context.startActivity(intent);

					}
				});

				return view;
			}
		};
	}

	public static CustomAlertDialog mesgBox;
	public static CustomAlertDialogInput inputMesgBox;

	public static CustomAlertDialog alertMessage(Activity mActivity, String title, String message, final Handler handler) {
		return alertMessage(mActivity, title, message, mActivity.getString(R.string.btn_neutral), null, handler);
	}

	public static CustomAlertDialog alertMessage(Activity mActivity, String title, String message, String okbtnText, final Handler handler) {
		return alertMessage(mActivity, title, message, okbtnText, null, handler);
	}

	public static CustomAlertDialog alertMessage(Activity mActivity, String title, String message, String okBtnText, String cancelBtnText, final Handler handler) {

		if(mesgBox!=null) {
			try {
				mesgBox.dismiss();
				mesgBox = null;
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

		try {

			mesgBox = new CustomAlertDialog(mActivity);

			mesgBox.setMTitle(title); // 팝업 타이틀
			mesgBox.setMessage(message); // 팝업 내용
			mesgBox.setCancelable(false);

			if (!TextUtils.isEmpty(cancelBtnText)) {
				mesgBox.setYesButton(okBtnText, new View.OnClickListener() {
					public void onClick(View v) {

						if (mesgBox != null) {

							mesgBox.dismiss();

							if (handler != null) {
								handler.sendEmptyMessage(Constant.ALERTDIALOG_RESULT_YES);
							}
						}

						return;
					}

				});

				mesgBox.setNoButton(cancelBtnText, new View.OnClickListener() {

					@Override
					public void onClick(View view) {
						if (mesgBox != null) {

							mesgBox.dismiss();

							if (handler != null) {
								handler.sendEmptyMessage(Constant.ALERTDIALOG_RESULT_NO);
							}
						}
					}
				});
			} else {
				mesgBox.setPositiveButton(okBtnText, new View.OnClickListener() {
					public void onClick(View v) {
						if (mesgBox != null)
							mesgBox.dismiss();

						if (handler != null) handler.sendEmptyMessage(Constant.ALERTDIALOG_RESULT_NUTRUAL);

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

								if (handler != null) handler.sendEmptyMessage(Constant.ALERTDIALOG_RESULT_NO);
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

			return mesgBox;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static CustomAlertDialog alertMessage(Activity mActivity, String title, String message, String okBtnText, String cancelBtnText, String centerBtnText, final Handler handler) {

		if(mesgBox!=null) {
			try {
				mesgBox.dismiss();
				mesgBox = null;
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

		try {

			mesgBox = new CustomAlertDialog(mActivity);

			mesgBox.setMTitle(title); // 팝업 타이틀
			mesgBox.setMessage(message); // 팝업 내용
			mesgBox.setCancelable(false);

			if (!TextUtils.isEmpty(cancelBtnText) && !TextUtils.isEmpty(centerBtnText)) {
				mesgBox.setYesButton(okBtnText, new View.OnClickListener() {
					public void onClick(View v) {

						if (mesgBox != null) {

							mesgBox.dismiss();

							if (handler != null) {
								handler.sendEmptyMessage(Constant.ALERTDIALOG_RESULT_YES);
							}
						}

						return;
					}

				});

				mesgBox.setNoButton(cancelBtnText, new View.OnClickListener() {

					@Override
					public void onClick(View view) {
						if (mesgBox != null) {

							mesgBox.dismiss();

							if (handler != null) {
								handler.sendEmptyMessage(Constant.ALERTDIALOG_RESULT_NO);
							}
						}
					}
				});

				mesgBox.setPositiveButton(centerBtnText, new View.OnClickListener() {
					public void onClick(View v) {
						if (mesgBox != null)
							mesgBox.dismiss();

						if (handler != null) handler.sendEmptyMessage(Constant.ALERTDIALOG_RESULT_NUTRUAL);

						return;
					}

				});
			} else if(!TextUtils.isEmpty(centerBtnText)) {
				mesgBox.setYesButton(okBtnText, new View.OnClickListener() {
					public void onClick(View v) {

						if (mesgBox != null) {

							mesgBox.dismiss();

							if (handler != null) {
								handler.sendEmptyMessage(Constant.ALERTDIALOG_RESULT_YES);
							}
						}

						return;
					}

				});

				mesgBox.setNoButton(cancelBtnText, new View.OnClickListener() {

					@Override
					public void onClick(View view) {
						if (mesgBox != null) {

							mesgBox.dismiss();

							if (handler != null) {
								handler.sendEmptyMessage(Constant.ALERTDIALOG_RESULT_NO);
							}
						}
					}
				});
			} else {
				mesgBox.setPositiveButton(okBtnText, new View.OnClickListener() {
					public void onClick(View v) {
						if (mesgBox != null)
							mesgBox.dismiss();

						if (handler != null) handler.sendEmptyMessage(Constant.ALERTDIALOG_RESULT_NUTRUAL);

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

								if (handler != null) handler.sendEmptyMessage(Constant.ALERTDIALOG_RESULT_NO);
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

			return mesgBox;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static CustomAlertDialogInput alertInputMessage(Activity mActivity, String title, String message, String okBtnText, String cancelBtnText, boolean isDecimal, final Handler handler) {

		if(inputMesgBox!=null) {
			try {
				inputMesgBox.dismiss();
				inputMesgBox = null;
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

		try {

			inputMesgBox = new CustomAlertDialogInput(mActivity);

			inputMesgBox.setTitle(title); // 팝업 타이틀
			inputMesgBox.setMessage(message); // 팝업 내용
			inputMesgBox.setCancelable(false);

			if (!TextUtils.isEmpty(cancelBtnText)) {
				inputMesgBox.setYesButton(okBtnText, new View.OnClickListener() {
					public void onClick(View v) {

						if (inputMesgBox != null) {

							inputMesgBox.dismiss();

							if (handler != null) {
								String value = inputMesgBox.getValue();

								Message msg = new Message();
								msg.obj = value;
								msg.what = Constant.ALERTDIALOG_RESULT_YES;
								handler.sendMessage(msg);

							}
						}

						return;
					}

				});

				inputMesgBox.setNoButton(cancelBtnText, new View.OnClickListener() {

					@Override
					public void onClick(View view) {
						if (inputMesgBox != null) {

							inputMesgBox.dismiss();

							if (handler != null) {
								handler.sendEmptyMessage(Constant.ALERTDIALOG_RESULT_NO);
							}
						}
					}
				});
			} else {
				inputMesgBox.setPositiveButton(okBtnText, new View.OnClickListener() {
					public void onClick(View v) {
						if (inputMesgBox != null)
							inputMesgBox.dismiss();

						if (handler != null) handler.sendEmptyMessage(Constant.ALERTDIALOG_RESULT_NUTRUAL);

						return;
					}

				});
			}

			inputMesgBox.setOnKeyListener( // 백버튼을 눌렀을 때
					new AlertDialog.OnKeyListener() {
						@Override
						public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
							if (keyCode == KeyEvent.KEYCODE_BACK) {
								dialog.dismiss();

								if (handler != null) handler.sendEmptyMessage(Constant.ALERTDIALOG_RESULT_NO);
							}
							return false;
						}
					});

			inputMesgBox.setCancelable(false);

			if (inputMesgBox != null) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					inputMesgBox.create();
				}

				inputMesgBox.show();
			}

			if(isDecimal) inputMesgBox.setInputTypeDecimal();

			return inputMesgBox;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static boolean isExistFile(Context ctx, String filename) {
		boolean ret = false;

		File path = getStoragePath(ctx); //Environment.getExternalStoragePublicDirectory(Environment.getExternalStorageDirectory() + File.separator + ctx.getResources().getString(R.string.path));
		if (!path.exists()) path.mkdirs();

		if (TextUtils.isEmpty(filename)) return ret;

		File file = new File(path, filename);

		if (file.isFile() && file.exists()) ret = true;

		return ret;
	}

	public static Bitmap getFromImageFile(Context ctx, String filename) {
		Bitmap bitmap = null;

		File path = getStoragePath(ctx);

		if (!path.exists()) path.mkdirs();

		File file = new File(path, filename);

		if (file.isFile() && file.exists()) {
			try {
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inPreferredConfig = Bitmap.Config.ARGB_8888;

				bitmap = BitmapFactory.decodeStream(new FileInputStream(file), null, options);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		return bitmap;
	}

	@Nullable
	public static Bitmap getBitmapFromPath(Context ctx, String filename) {
		Bitmap bitmap = null;

		File file = new File(filename);

		if (file.isFile() && file.exists()) {
			try {
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inPreferredConfig = Bitmap.Config.ARGB_8888;

				bitmap = BitmapFactory.decodeStream(new FileInputStream(file), null, options);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		return bitmap;
	}

	public static Uri getBitmapToUri(Context context, Bitmap bitmap, String title) {
		if(TextUtils.isEmpty(title)) title = context.getResources().getString(R.string.app_name);

		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
		String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, title, null);

		return Uri.parse(path);
	}

	public static boolean removeFile(Context ctx, String filename) {
		boolean ret = false;

		if(isExistFile(ctx, filename)) {
			ret = new File(filename).delete();
		}

		return ret;
	}

	public static String SaveToFile(Context ctx, Bitmap image, String filename) {
		final File path = getStoragePath(ctx);

		return SaveToFile(image, path, File.separator + filename, true);
	}

	public static String SaveToFile(Bitmap image, File path, String filename) {
		return SaveToFile(image, path, File.separator + filename, true);
	}

	public static String SaveToFile(Bitmap image, File path, String filename, boolean overwrite) {
		OutputStream outStream = null;
		String resultPath = "";

		if (!path.exists()) path.mkdirs();

		if (!path.isDirectory()) {
			myLog.e(TAG, path + " is not directory!");
			return resultPath;
		}

		File file = new File(path, filename);
		try {
			if (file.exists() && !overwrite) {
				if (!overwrite) myLog.e(TAG, file + " is exist!");
				else myLog.e(TAG, "Can't not create " + file);
				return resultPath;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (image == null) {
			myLog.e(TAG, "is not set Bitmap image!..");
			return resultPath;
		}

		try {
			// bitmap객체를 파일로 저장
			outStream = new FileOutputStream(file);
			if (image.compress(Bitmap.CompressFormat.PNG, 100, outStream)) {
				resultPath = path + filename;
			}
			outStream.flush();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				outStream.close();
				outStream = null;
			} catch (Throwable t) {
			}
		}

		return resultPath;
	}

	public static String SaveBitmapToFile(Bitmap image, String filename) {
		OutputStream outStream = null;
		String resultPath = "";

		if (image == null) {
			myLog.e(TAG, "*** SaveBitmapToFile - is not set Bitmap image!..");
			return resultPath;
		}

		try {
			myLog.e(TAG, "*** SaveBitmapToFile path: "+filename);
			// bitmap객체를 파일로 저장
			outStream = new FileOutputStream(new File(filename));
			if (image.compress(Bitmap.CompressFormat.JPEG, 100, outStream)) {
				resultPath = filename;
			}
			outStream.flush();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				outStream.close();
				outStream = null;
			} catch (Throwable t) {
			}
		}

		return resultPath;
	}

	public static InputStream getAssetFile(Context ctx, String filename) {
		return getAssetFile(ctx, "maps", filename);
	}

	public static InputStream getAssetFile(Context ctx, String path, String filename) {
		AssetManager assetManager = ctx.getAssets();
		InputStream input = null;
		try {
			input = assetManager.open(path + File.separator + filename);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return input;
	}

	public static boolean existFile(Context ctx, String filename) {
		boolean ret = false;

		InputStream input = getAssetFile(ctx, filename);

		try {
			if (input != null) ret = true;
			if (input.available() > 0) ret = true;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public static double getDistanceMeter(double lat1, double lon1, double lat2, double lon2) {
		double meter = 0;
		try {
			Location locationA = new Location("point A");
			locationA.setLatitude(lat1);
			locationA.setLongitude(lon1);
			Location locationB = new Location("point B");
			locationB.setLatitude(lat2);
			locationB.setLongitude(lon2);

			meter = locationA.distanceTo(locationB);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return meter;
	}

	public static double getDistanceMeter(LatLng src, LatLng tar) {
		double meter = 0;
		try {
			Location locationA = new Location("point A");
			locationA.setLatitude(src.latitude);
			locationA.setLongitude(src.longitude);
			Location locationB = new Location("point B");
			locationB.setLatitude(tar.latitude);
			locationB.setLongitude(tar.longitude);

			meter = locationA.distanceTo(locationB);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return meter;
	}

	public static double getDistance(double lat1, double lon1, double lat2, double lon2) {
		double theta = lon1 - lon2;
		double dist = Math.sin(deg2rad(lat1))
				* Math.sin(deg2rad(lat2))
				+ Math.cos(deg2rad(lat1))
				* Math.cos(deg2rad(lat2))
				* Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		return (dist);
	}

	public static double getDistance(LatLng src, LatLng tar) {
		double theta = src.longitude - tar.longitude;
		double dist = Math.sin(deg2rad(src.latitude))
				* Math.sin(deg2rad(tar.latitude))
				+ Math.cos(deg2rad(src.latitude))
				* Math.cos(deg2rad(tar.latitude))
				* Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		//킬로미터는 dist에 1.609344를 곱해야함
		return (dist);
	}

	public static double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}

	public static double rad2deg(double rad) {
		return (rad * 180.0 / Math.PI);
	}

	public static void writeUUID(File file, String uuid) {
		FileWriter fw = null;
		try {
			// open file.
			fw = new FileWriter(file);

			// write file.
			fw.write(uuid);

		} catch (Exception e1) {
			e1.printStackTrace();
		}

		// close file.
		if (fw != null) {
			// catch Exception here or throw.
			try {
				fw.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}

	public static String readUUID(File file) {
		StringBuffer result = new StringBuffer("");

		FileReader fr;
		char ch;
		int data;

		try {

			if (!file.exists()) file.createNewFile();

			// open file.
			fr = new FileReader(file);

			while ((data = fr.read()) != -1) {
				ch = (char) data;
				result.append(ch);
			}

			fr.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result.toString();
	}

	public static File getStoragePath(Context ctx) {
		File path;

		try {
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
				path = ctx.getExternalFilesDir(Environment.getRootDirectory() + File.separator + ctx.getResources().getString(R.string.path));
			} else {
				path = Environment.getExternalStoragePublicDirectory(Environment.getExternalStorageDirectory() + File.separator + ctx.getResources().getString(R.string.path));
			}
		} catch (Exception e) {
			e.printStackTrace();

			path = new File(ctx.getResources().getString(R.string.path));
		}

		return path;
	}

	public static File getStoragePath(Context ctx, String filename) {
		// path exist check
		String path = "";

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			path = ctx.getExternalFilesDir(Environment.getRootDirectory() + File.separator + ctx.getResources().getString(R.string.path)).getAbsolutePath();
		} else {
			path = Environment.getExternalStorageDirectory() + File.separator + ctx.getResources().getString(R.string.path);
		}

		if(!new File(path).exists()) {
			new File(path).mkdir();
		} else if(!new File(path).isDirectory()) {
			new File(path).delete();
			new File(path).mkdir();
		}

		return new File(path, filename);
	}

	public static String getStoragePathString(Context ctx, String prefixPath) {
		// path exist check
		String path = "";

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			path = ctx.getExternalFilesDir(Environment.getRootDirectory() + File.separator + prefixPath).getAbsolutePath();
		} else {
			path = Environment.getExternalStorageDirectory() + File.separator + prefixPath;
		}

		if(!new File(path).exists()) {
			new File(path).mkdir();
		} else if(!new File(path).isDirectory()) {
			new File(path).delete();
			new File(path).mkdir();
		}

		return path;
	}

	public static String getStoragePathString(Context ctx) {
		// path exist check
		String path = "";

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			path = ctx.getExternalFilesDir(Environment.getRootDirectory().toString()).getAbsolutePath();
		} else {
			path = Environment.getExternalStorageDirectory().toString();
		}

		return path;
	}

	public static String getDefaultUUID(Context ctx, String uuid) {

		final File path = getStoragePath(ctx); //Environment.getExternalStoragePublicDirectory(Environment.getExternalStorageDirectory() + File.separator + ctx.getResources().getString(R.string.path));

		if (!path.exists()) path.mkdirs();

		final File file = new File(path, "uuid.txt");

		String result = "";
		String oldUuid = readUUID(file);

		if (!TextUtils.isEmpty(oldUuid)) result = oldUuid;
		else {
			writeUUID(file, uuid);
			result = uuid;
		}

		myLog.d(TAG, "*** UUID: " + result);

		return result;
	}

	public static String getBase64encode(String content) {
		return Base64.encodeToString(content.getBytes(), Base64.NO_WRAP);
	}

	public static String getBase64encode(byte[] content) {
		return Base64.encodeToString(content, Base64.NO_WRAP);
	}

	public static String getBase64encodeImage(Bitmap bitmap) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

		return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
	}

	public static byte[] getBase64decode(String base64String) {
		byte[] decodedString = null;
		try {
			decodedString = Base64.decode(base64String, Base64.DEFAULT);
		} catch(IllegalArgumentException e) {
			e.printStackTrace();
		}
		return decodedString;
	}

	public static Bitmap getBase64decodeImage(String base64String) {
		byte[] decodedbyte = getBase64decode(base64String);
		Bitmap decodedByte = null;
		try {
			decodedByte = BitmapFactory.decodeByteArray(decodedbyte, 0, decodedbyte.length);
		} catch(Exception e) {
			e.printStackTrace();
		}

		return decodedByte;
	}

	public static String getEncryptStringFromLinkParams(HashMap<String, String> map, String[] seqStr) {
		String ret = "";

		for (int i = 0; i < seqStr.length; i++) {
			ret = TextUtils.isEmpty(ret) ? ret + map.get(seqStr[i]) : ret + "#" + map.get(seqStr[i]);
		}

		// 서버에 생성된 링크를 계속 보내 리워드를 획득하는 버그를 해결하기 위하여 제한시간을 둠.
		// 여기에서 서버 통신유효시간을 같이 던진다. 서버측에서는 통신유효시간 이내면 정상처리하고 나머지는 오류를 보내도록 한다.
		ret = ret + "#" + System.currentTimeMillis() + (CONNECT_TIMEOUT);

		if (ret.length() > 0) {
			ret = Common.getBase64encode(ret);
		}

		return ret;
	}

	public static String getIMEI(Context ctx) {
		String strIMEI = "";

		try {
			TelephonyManager mTelephony = (TelephonyManager) ctx
					.getSystemService(Context.TELEPHONY_SERVICE);
			if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
				strIMEI = mTelephony.getDeviceId();
			}

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			strIMEI = "";
		}

		return strIMEI;
	}

	/**
	 * 계정에 등록된 이메일 주소를 반환한다.
	 *
	 * @param ctx
	 * @return
	 */
	public static String getAccountEmailAddress(Context ctx, String type) {
		String ret = "";

		try {
			Account[] accounts =  AccountManager.get(ctx).getAccounts();
			Account account = null;
			for(int i=0;i<accounts.length;i++) {
				account = accounts[i];
				myLog.d(TAG, "Account - name: " + account.name + ", type :" + account.type);
				if(account.name.contains("@")) {
					if(account.type.equals(type)) {     //이러면 구글 계정 구분 가능
						ret = account.name;
						break;
					}
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}

		if(TextUtils.isEmpty(ret)) {
			ret = getImsiAccount(ctx);
		}

		return ret;
	}

	/**
	 * 계정에 등록된 이메일 주소를 반환한다.
	 *
	 * @param ctx
	 * @return
	 */
	public static String getAccountEmailAddress(Context ctx) {
		String ret = "";

		ret = getAccountEmailAddress(ctx, "com.google");

		if(TextUtils.isEmpty(ret)) {
			try {
				Account[] accounts =  AccountManager.get(ctx).getAccounts();
				Account account = null;
				for(int i=0;i<accounts.length;i++) {
					account = accounts[i];
					//              myLog.d(TAG, "Account - name: " + account.name + ", type :" + account.type);
					if(account.name.contains("@")) {
						ret = account.name;
						break;
						//            	  if(account.type.equals("com.google")) {     //이러면 구글 계정 구분 가능
						//            	  }
					}
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

		if(TextUtils.isEmpty(ret)) {
			ret = getImsiAccount(ctx);
		}

		return ret;
	}

	public static String getImsiAccount(Context ctx) {
		String ret = "";
		String devid = getDefaultUUID(ctx, UUID.randomUUID().toString());

		if(TextUtils.isEmpty(devid)) devid = String.valueOf(getUniqueID());

		if(!TextUtils.isEmpty(devid)) {
			try {
				String domain = ctx.getResources().getString(R.string.url_site);
				if(!TextUtils.isEmpty(domain)) {
					String[] domains = domain.split("//"); // domain.replaceAll("www.", "");
					if(domains.length>1) domain = domains[1];
				}
				ret = devid+"@"+domain;
			} catch(Exception e) {
				e.printStackTrace();
				ret = "unknown";
			} finally {
				// 그래도 비어있다면.... 서버로부터 생성된 regkey값을 가져와서 처리함... 하지만 아직 구현되지 않음.
			}
		} else {
			ret = "unknown";
		}

		return ret;
	}

	public static String makeHumanTime(Context ctx, double srcmin) {
		String result = "-";

		try {
			double hour = srcmin / 60;
			double min = srcmin % 60;

			if ((int) hour > 0)
				result =  ctx.getResources().getString(R.string.hour_string, (int) hour, (int) min);
			else
				result =  ctx.getResources().getString(R.string.minute_string, (int) min);
		} catch(Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public static String makeHumanSeconds(Context ctx, double srcsec) {
		String result = "-";

		try {
			double srcmin = srcsec / 60;
			double hour = srcmin / 60;
			double min = srcmin % 60;

			if ((int) hour > 0)
				result = ctx.getResources().getString(R.string.hour_string, (int) hour, (int) min);
			else
				result = ctx.getResources().getString(R.string.minute_string, (int) min);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public static String makeHumanForMeter(Context ctx, double srcmeter) {
		String result = String.format(ctx.getResources().getString(R.string.formeter_string), (int)srcmeter);

		return result;
	}

	public static String makeHumanMeter(Context ctx, double srcmeter) {
		String result = "-";

		try {
			//double km = srcmeter / 1000;
			if(srcmeter < 1000)
				result = String.format(ctx.getResources().getString(R.string.formeter_string), (int)srcmeter);
			else {
				double km = srcmeter / 1000;
				result = String.format(ctx.getResources().getString(R.string.meter_string), km);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public static long stringToTimestamp(String mytime) {
		return stringToTimestamp(mytime, "yyyy-MM-dd HH:mm:ss");
	}

	public static long stringToTimestamp(String mytime, String format) {

		SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		Date myDate = null;
		long ret = 0;

		try {
			myDate = dateFormat.parse(mytime);
			ret = myDate.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public static View makeEmptyView(Context ctx, String message) {

		TextView view = new TextView(ctx);
		LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
		param.gravity = Gravity.CENTER_VERTICAL| Gravity.CENTER_HORIZONTAL;
		view.setLayoutParams(param);
		view.setText(message);
		view.setTextSize(Common.convertDpToPixel(ctx, 16));
		view.setTextColor(ctx.getResources().getColor(R.color.theme_color));

		return view;
	}

	public static String toUpperString(String value) {
		if(!TextUtils.isEmpty(value)) return value.toUpperCase(Locale.getDefault());
		else return value;
	}

	public static boolean isRunning(Context ctx) {
		ActivityManager activityManager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

		for (ActivityManager.RunningTaskInfo task : tasks) {
			if (ctx.getPackageName().equalsIgnoreCase(task.baseActivity.getPackageName()))
				return true;
		}

		return false;
	}

	public static void phoneCallAction(Activity activity, String phoneNumber, int RESULT_CODE) {

		myLog.d(TAG, "*** phoneCallAction: "+phoneNumber);

		if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
			Intent intent = new Intent(Intent.ACTION_DIAL);
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

			if(!TextUtils.isEmpty(phoneNumber) && phoneNumber.startsWith("tel:"))
				intent.setData(Uri.parse(phoneNumber));
			else
				intent.setData(Uri.parse("tel:"+phoneNumber));
			try {
				activity.startActivity(intent);
			} catch (Exception e) {
				myLog.d(TAG, "*** phoneCallAction Exception: "+e.getLocalizedMessage());

				e.printStackTrace();
			}
		} else {

			Common.alertMessage(activity,
					activity.getResources().getString(R.string.app_name),
					activity.getResources().getString(R.string.permission_phone_message),
					new Handler(){

						@Override
						public void handleMessage(Message msg) {
							super.handleMessage(msg);
						}
					});
		}

	}

	public static String getDateHourMin(long timeStamp) {

		try {
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
			Date netDate = (new Date(timeStamp));
			return sdf.format(netDate);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static long getDateStringtoTimestamp(String dateString) {
		if(!TextUtils.isEmpty(dateString)) {
			Date date = null;
			try {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				date = (Date) formatter.parse(dateString);

				return date.getTime();
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return 0;
	}

	public static String getElapsedTime(Context ctx, long timeStamp) {
		long elapsedTime = (System.currentTimeMillis() / 1000) - timeStamp;

		long hour = elapsedTime / 3600;
		long min = (elapsedTime % 3600) / 60;
		long sec = (elapsedTime % 3600) % 60;

		if(hour>0)
			return String.format(ctx.getResources().getString(R.string.call_row_elapsedtime_hour), String.format("%d", (int)hour), String.format("%d", (int)min));
		else if(min>0)
			return String.format(ctx.getResources().getString(R.string.call_row_elapsedtime_min), String.format("%d", (int)min));
		else
			return String.format(ctx.getResources().getString(R.string.call_row_elapsedtime));
	}

    public static String getKeyHash(final Context context) {
        PackageInfo packageInfo = getPackageInfo(context, PackageManager.GET_SIGNATURES);
        if (packageInfo == null)
            return null;

        for (Signature signature : packageInfo.signatures) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                return Base64.encodeToString(md.digest(), Base64.NO_WRAP);
            } catch (NoSuchAlgorithmException e) {
                myLog.w(TAG, "*** KeyHash: Unable to get MessageDigest. signature=" + signature+"\n"+e);
            }
        }
        return null;
    }

    /**
     * TODO: FCM Push message sender
     *
     * @param activity
     * @param dataValue
     * @param instanceIdToken
     */
    public static void sendPushToSingleInstance(final Context activity,
                                                final HashMap dataValue /*your data from the activity*/,
                                                final String instanceIdToken /*firebase instance token you will find in documentation that how to get this*/ ) {

        final CustomToast toast = new CustomToast(activity);

        final String url = "https://fcm.googleapis.com/fcm/send";   // "https://fcm.googleapis.com/fcm/notification"; //

        StringRequest myReq = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Toast.makeText(activity, "Bingo Success", Toast.LENGTH_SHORT).show();
                        myLog.e(TAG, "*** sendPushToSingleInstance - onResponse: "+ response);

                        toast.showToast("Bingo Success", Toast.LENGTH_SHORT);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Toast.makeText(activity, "Oops error", Toast.LENGTH_SHORT).show();
                        myLog.e(TAG, "*** sendPushToSingleInstance - onErrorResponse: "+ error.getLocalizedMessage());

                        error.printStackTrace();

                        toast.showToast(error.getLocalizedMessage(), Toast.LENGTH_SHORT);
                    }
                }) {

            @Override
            public byte[] getBody() throws AuthFailureError {
                JSONObject main = new JSONObject();
                try {
                    /**
                     * "notification" 항목을 넣으면 폰에서 노티피케이션을 하지 않아도 알림이 올라온다.
                     */
                    main.put("to", instanceIdToken);
                    main.put("priority", "high");
                    main.put("data", new JSONObject(dataValue));
                    main.put("content_available", true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                myLog.e(TAG, "*** sendPushToSingleInstance: "+ main.toString());

                return main.toString().getBytes();

            }

            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "key=" + Constant.FCM_SERVER_KEY);
                return headers;
            }

        };

        Volley.newRequestQueue(activity).add(myReq);
    }

    public static void getKeyHash(Context context, String hashStretagy) {
        PackageInfo info;
        try {
            info = context.getPackageManager().getPackageInfo(BuildConfig.APPLICATION_ID, PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md;
                md = MessageDigest.getInstance(hashStretagy);
                md.update(signature.toByteArray());
                String something = new String(Base64.encode(md.digest(), 0));
                myLog.e("*** KeyHash  -->>>>>>>>>>>>" , something);

                // Notification.registerGCM(this);
            }
        } catch (PackageManager.NameNotFoundException e1) {
            myLog.e("*** KeyHash name not found" , e1.toString());
        } catch (NoSuchAlgorithmException e) {
            myLog.e("*** KeyHash no such an algorithm" , e.toString());
        } catch (Exception e) {
            myLog.e("*** KeyHash exception" , e.toString());
        }
    }

	public static boolean isValidUrl(String url) {
		Pattern p = Patterns.WEB_URL;
		Matcher m = p.matcher(url.toLowerCase());
		return m.matches();
	}

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("NewApi")
    public static String determineTermuxArchName() {
        // Note that we cannot use System.getProperty("os.arch") since that may give e.g. "aarch64"
        // while a 64-bit runtime may not be installed (like on the Samsung Galaxy S5 Neo).
        // Instead we search through the supported abi:s on the device, see:
        // http://developer.android.com/ndk/guides/abis.html
        // Note that we search for abi:s in preferred order (the ordering of the
        // Build.SUPPORTED_ABIS list) to avoid e.g. installing arm on an x86 system where arm
        // emulation is available.

        for (String androidArch : Build.SUPPORTED_ABIS) {
            switch (androidArch) {
                case Constant.CPU_ARCH_ARM64_V8A: return androidArch; //"aarch64";
                case Constant.CPU_ARCH_ARMEABI: return androidArch; // "armeabi";
                case Constant.CPU_ARCH_ARMEABI_V7A: return androidArch; // "arm";
                case Constant.CPU_ARCH_X86_64: return androidArch; // "x86_64";
                case Constant.CPU_ARCH_X86: return androidArch; //"i686";
            }
        }
        throw new RuntimeException("Unable to determine arch from Build.SUPPORTED_ABIS =  " + Arrays.toString(Build.SUPPORTED_ABIS));

    }

    public static void updateActionBarHeight(Activity activity, final LinearLayout main_top_box) {
    	final int paddingTop = getActionBarHeight(activity);

		main_top_box.post(new Runnable() {
			@Override
			public void run() {
				myLog.d(TAG, "*** updateActionBarHeight: "+paddingTop+" px");
				main_top_box.setPadding(0, paddingTop, 0, 0);
			}
		});

	}

    public static int getActionBarHeight(Activity activity) {
    	int actionBarHeight = 24; //(int)convertDpToPixel(activity, 20);

		try {
			if (android.os.Build.MODEL.startsWith("SM-G97")) {    // Galaxy S10 계열 모델들
				actionBarHeight = 38;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}

//		TypedValue tv = new TypedValue();
//		if(activity.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
//			actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, activity.getResources().getDisplayMetrics());
//			actionBarHeight = (int)convertPixelsToDp(activity, actionBarHeight);
//		}

		return (int)convertDpToPixel(activity, actionBarHeight);
	}

	public static String getMimeType(String url) {
		String type = null;
		String extension = MimeTypeMap.getFileExtensionFromUrl(url);
		if (extension != null) {
			type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
		}
		return type;
	}

	public static void openBrowser(@NonNull Activity activity, String url) {

		try {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			activity.startActivity(browserIntent);
		} catch(ActivityNotFoundException e) {
			e.printStackTrace();

			alertMessage(activity,
					activity.getResources().getString(R.string.app_name),
					activity.getResources().getString(R.string.not_found_browser),
					activity.getResources().getString(R.string.btn_ok),
					new Handler() {

						@Override
						public void handleMessage(Message msg) {
							super.handleMessage(msg);
						}
					});
		}
	}

	public static int countChar(String str, char c) {
		int count = 0;

		for(int i=0; i < str.length(); i++) {
			if(str.charAt(i) == c)
				count++;
		}

		return count;
	}

	public static String buildCallbackWithValue(String callback, HashMap<String, String> objMaps) {
		String buildedScript = callback;

		Iterator<String> keys = objMaps.keySet().iterator();
		while(keys.hasNext()) {
			String key = keys.next();
			String value = objMaps.get(key);

			value = !TextUtils.isEmpty(value) ? value.replaceAll("\"", "\\\\\"") : "";

			buildedScript = buildedScript.replace(key, "\""+value+"\"");

		}

		return buildedScript;
	}

	public static String buildCallbackWithValue(String callback, String[] objs) {

		return buildCallbackWithValue(callback, objs, null);

	}

	/**
	 * 콜백 자바스크립트 펑션에 지정된 값을 넣고, 콜백이 존재하지 않으면 지정된 메시지를 띄운다.
	 *
	 * @param callback
	 * @param objs
	 * @param notfoundMessage
	 * @return
	 */
	public static String buildCallbackWithValue(String callback, String[] objs, String notfoundMessage) {
		String buildedScript = callback;

		if(!TextUtils.isEmpty(callback)) {
			for (int i = 0; i < countChar(callback, '$'); i++) {
				String key = String.format(Locale.getDefault(), "$%d", i + 1);
				String value = objs.length > i ? objs[i] : "";

				value = !TextUtils.isEmpty(value) ? value.replaceAll("\"", "\\\\\"") : "";

				buildedScript = buildedScript.replace(key, "\"" + value + "\"");
			}

			if (!TextUtils.isEmpty(notfoundMessage)) {
				try {
					String function = buildedScript.split("\\(")[0];
					if (!TextUtils.isEmpty(function) && !TextUtils.isEmpty(buildedScript))
						buildedScript = String.format(Locale.getDefault(), "if(typeof %s !== 'undefined') %s; else alert('%s');", function, buildedScript, notfoundMessage);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				try {
					String function = buildedScript.split("\\(")[0];
					if (!TextUtils.isEmpty(function) && !TextUtils.isEmpty(buildedScript))
						buildedScript = String.format(Locale.getDefault(), "if(typeof %s !== 'undefined') %s; else alert('ERROR: %s UNDEFINED');", function, buildedScript, function);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		myLog.d(TAG, "*** buildCallbackWithValue: "+buildedScript);

		return buildedScript;
	}

	public static JSONObject toJSON(HashMap<String, Object> map){
		JSONObject jsonObject = new JSONObject(map);
		return jsonObject;
	}

	public static byte[] bitmapToByteArray(Bitmap bitmap) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
		byte[] byteArray = stream.toByteArray();
		return byteArray;
	}

	public static Bitmap byteArrayToBitmap(byte[] byteArray) {
		Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
		return bitmap;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	public static int getBitmapSizeOf(Bitmap data) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
			return data.getRowBytes() * data.getHeight();
		} else {
			return data.getByteCount();
		}
	}

	public static void setImageBitmap(Context context, ImageView view, Bitmap bitmap) {
		try {
			int sizeBytes = getBitmapSizeOf(bitmap);

			myLog.e(TAG, "*** setImageBitmap size is " + String.format("%,d bytes", sizeBytes));
			//if(myLog.debugMode) Toast.makeText(context, "*** Debug: setImageBitmap size is " + String.format("%,d MB", (sizeBytes/1048576)), Toast.LENGTH_SHORT).show();

			if (sizeBytes > (50 * 1048576)) {
				// 50MB Over!
				Bitmap mCacheBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / 2, bitmap.getHeight() / 2, false);
				myLog.e(TAG, "*** setImageBitmap resize to " + String.format("%,d bytes", getBitmapSizeOf(mCacheBitmap)));

				bitmap = mCacheBitmap;
			}

			view.setImageBitmap(bitmap);

		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			myLog.e(TAG, "*** OutOfMemory: "+e.getMessage());
			if(myLog.debugMode) {
				Toast.makeText(context, context.getResources().getString(R.string.outofmemory_bitmap_size), Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			e.printStackTrace();
			myLog.e(TAG, "*** Exception: "+e.getMessage());
			if(myLog.debugMode) {
				Toast.makeText(context, context.getResources().getString(R.string.unknownerror_bitmap_size), Toast.LENGTH_SHORT).show();
			}
		}
	}

	public static Uri convertLocalFile(Context context, Uri sourceUri) {
		Uri targetUri = null;

		try {
			byte[] bytes = getBytes(context, sourceUri);

			String filename = sourceUri.getLastPathSegment();
			File path = getStoragePath(context, filename);

			targetUri = saveByteToFile(bytes, path);

		} catch (IOException e) {
			e.printStackTrace();
		}

		return targetUri;
	}

	@Nullable
	public static Uri saveByteToFile(byte[] bytes, File file) {
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(bytes);
			fos.close();

			return Uri.fromFile(file);

		} catch (Exception e) {
			myLog.e(TAG, e.getMessage());
		}

		return null;
	}

	public static byte[] getBytes(@NonNull InputStream inputStream) throws IOException {

		byte[] bytesResult = null;
		ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
		int bufferSize = 1024;
		byte[] buffer = new byte[bufferSize];
		try {
			int len;
			while ((len = inputStream.read(buffer)) != -1) {
				byteBuffer.write(buffer, 0, len);
			}
			bytesResult = byteBuffer.toByteArray();
		} finally {
			// close the stream
			try{ byteBuffer.close(); } catch (IOException ignored){ /* do nothing */ }
		}
		return bytesResult;
	}

	public static byte[] getBytes(@NonNull Context context, Uri uri) throws IOException {
		InputStream iStream = context.getContentResolver().openInputStream(uri);
		try {
			return getBytes(iStream);
		} finally {
			// close the stream
			try {
				iStream.close();
			} catch (IOException ignored) { /* do nothing */ }
		}
	}

	public static void makeNoMediaFolder(String path) {
		File file = new File(path + File.separator + ".nomedia");

		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getCacheFileName(Context context, String url) {
		String fname = url;
		try {
			String[] fnames = url.split("/");
			if (fnames != null && fnames.length > 0) {
				fname = fnames[fnames.length - 1];
				fname = !TextUtils.isEmpty(fname) ? fname.replaceAll("[^a-zA-Z0-9_-]", "") : fname;
			}
		} catch (Exception e) {
		}

		// 캐시파일명 생성
		String path = "";

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			path = context.getExternalFilesDir(Environment.getRootDirectory() + File.separator + "image_cache").getAbsolutePath();
		} else {
			path = Environment.getExternalStorageDirectory() + File.separator + "image_cache";
		}

		if (!new File(path).exists()) {
			new File(path).mkdir();
		} else if (!new File(path).isDirectory()) {
			new File(path).delete();
			new File(path).mkdir();
		}

		makeNoMediaFolder(path);

		String filepath = path + File.separator + fname;

		return filepath;
	}

	public static void clearImageCache(Context context, String url) {
		try {
			String filepath = getCacheFileName(context, url);

			if ((new File(filepath)).exists()) {
				File file = new File(filepath);
				file.delete();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getLastFilename(String url) {
		try {
			File file = new File(url);
			return Uri.fromFile(file).getLastPathSegment();
		} catch(Exception e) {
			e.printStackTrace();
		}

		return url;
	}

	public static void saveImageBitmap(Bitmap bitmap, File saveFile) {
		try {
			FileOutputStream fos = new FileOutputStream(saveFile);
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
			fos.flush();
			fos.close();
		} catch (IOException e) {
			myLog.e(TAG, "*** saveImageBitmap: " + e.getLocalizedMessage());
			e.printStackTrace();
		}
	}

//	public static String getPathFromUri(Context context, Uri uri){
//		try {
//			Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
//			cursor.moveToNext();
//			@SuppressLint("Range") String path = cursor.getString(cursor.getColumnIndex("_data"));
//			cursor.close();
//			return path;
//		} catch(Exception e) {
//			e.printStackTrace();
//			myLog.e(TAG, "*** getPathFromUri: "+e.getMessage());
//
//			return uri.toString();
//		}
//	}

	public static String getPathFromUri(ContentResolver resolver, Uri uri) {
		if(uri!=null) {
			Cursor returnCursor =
					resolver.query(uri, null, null, null, null);
			assert returnCursor != null;
			int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
			returnCursor.moveToFirst();
			String name = returnCursor.getString(nameIndex);
			returnCursor.close();
			return name;
		} else {
			return "unknown";
		}
	}

	public static Bitmap getUriImage(Context context, Uri uri) throws FileNotFoundException, IOException{
		InputStream input = context.getContentResolver().openInputStream(uri);

		BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
		onlyBoundsOptions.inJustDecodeBounds = true;
		onlyBoundsOptions.inDither=true;//optional
		onlyBoundsOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;//optional
		Bitmap bitmap = BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
		input.close();

		return bitmap;
	}

	public static void loadImageCache(Context context, String url, final ImageView imageView, final int width, final Handler handler) {

		try {

			final Message message = new Message();
			PhotoEntry photoEntry = new PhotoEntry();

			if (TextUtils.isEmpty(url)) {
				imageView.setImageResource(R.drawable.noimage);
				return;
			}

			final String filepath = getCacheFileName(context, url);
			Date lastModDate = new Date();
			long fileSize = 0;
			String fname = "";
			if ((new File(filepath)).exists()) {
				File file = new File(filepath);
				fname = Uri.fromFile(file).getLastPathSegment();
				lastModDate = new Date(file.lastModified());
				fileSize = file.length();

				photoEntry.setPhotoName(getLastFilename(url));
				photoEntry.setPhotoType(getMimeType(url));

			}

			boolean doCache = false;
			String cacheName = "image_cache_" + fname;
			if (!TextUtils.isEmpty(fname)) {
				String ts = getSharedPreferencesString(cacheName, context);

				String ca = String.format(Locale.getDefault(), "%s%s", String.valueOf(lastModDate.getTime()), String.valueOf(fileSize));

				if (!TextUtils.isEmpty(ca) && ca.equals(ts)) {
					doCache = true;
				}
			}

//			long lastSeconds = (System.currentTimeMillis() - (60 * 60 * 24 * 1000));

//			myLog.d(TAG, "*** loadImageCache: filepath: "+filepath);
//			myLog.d(TAG, "*** loadImageCache: save: "+lastModDate.getTime());
//			myLog.d(TAG, "*** loadImageCache: cache: "+doCache);

			// 캐시처리가 아니라면!
			if (!doCache) {
//			if (!(new File(filepath)).exists() || lastModDate.getTime() < lastSeconds) {

				if (url.contains("http") && url.contains("?"))
					url = String.format("%s&ts=%s", url, String.valueOf(System.currentTimeMillis()));
				else if (url.contains("http"))
					url = String.format("%s?ts=%s", url, String.valueOf(System.currentTimeMillis()));

//				myLog.d(TAG, "*** loadImageCache: url: "+url);

				DisplayImageOptions options;

				ImageLoader imageLoader = ImageLoader.getInstance();

				if (!imageLoader.isInited())
					imageLoader.init(ImageLoaderConfiguration.createDefault(context));

				options = new DisplayImageOptions.Builder()
						.showImageForEmptyUri(R.drawable.ic_launcher)
						.showImageOnFail(R.drawable.ic_launcher)
						.resetViewBeforeLoading()
						.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
						.bitmapConfig(Bitmap.Config.RGB_565)
						.displayer(new FadeInBitmapDisplayer(300))
						.cacheOnDisk(false)
						.build();

				//String fullurl = Constant.SITE_URL[Constant.ISTEST]+File.separator+url;

//				String encodedUrl = URLEncoder.encode(url, "UTF-8");

//				myLog.d(TAG, "*** loadImageCache: url: "+url);
				String newUrl = url;
				try {
					newUrl = URLDecoder.decode(url, "UTF-8");
				} catch (Exception e) {
				}
//				myLog.d(TAG, "*** loadImageCache: newUrl: "+newUrl);

				imageLoader.loadImage(newUrl, options, new ImageLoadingListener() {
					@Override
					public void onLoadingStarted(String s, View view) {

					}

					@Override
					public void onLoadingFailed(String s, View view, FailReason failReason) {
						String errMessage = null;
						switch (failReason.getType()) {
							case IO_ERROR:
								errMessage = "Input/Output error";
								break;
							case OUT_OF_MEMORY:
								errMessage = "Out Of Memory error";
								break;
							case NETWORK_DENIED:
								errMessage = "Downloads are denied";
								break;
							case DECODING_ERROR:
								errMessage = "Unsupported URI scheme";
								break;
							case UNKNOWN:
								errMessage = "Unknown error";
								break;
						}

						if (handler != null) {
							message.what = 1;
							message.obj = errMessage;

							handler.sendMessage(message);
						}
					}

					@Override
					public void onLoadingComplete(String s, View view, Bitmap bitmap) {

						new File(filepath).delete();
						saveImageBitmap(bitmap, new File(filepath));

						try {
							File cacheFile = new File(filepath);
							Date lastModDate = new Date(cacheFile.lastModified());
							String ca = String.format(Locale.getDefault(), "%s%s", String.valueOf(lastModDate.getTime()), String.valueOf(cacheFile.length()));
							if (!TextUtils.isEmpty(ca) && cacheFile.exists() && cacheFile.length() > 0)
								saveSharedPreferencesString(cacheName, ca, context);
						} catch (Exception e) {
						}

						ViewGroup.LayoutParams params = imageView.getLayoutParams();
						if (width == -1) {
							params.height = (int) convertDpToPixel(context, 160); //view.getHeight();
						} else if (width != 0) {
							params.width = width;
						} else {
							params.width = bitmap.getWidth();
							params.height = bitmap.getHeight();
						}
						imageView.setImageBitmap(bitmap);
						imageView.setLayoutParams(params);

						if (imageView instanceof ImageView) {
							((ImageView) imageView).setAdjustViewBounds(true);
							if (width == -1)
								((ImageView) imageView).setScaleType(ImageView.ScaleType.CENTER_CROP);
							else
								((ImageView) imageView).setScaleType(ImageView.ScaleType.FIT_CENTER);
						}

						imageView.invalidate();

						photoEntry.setPhotoData(bitmap);

						if (handler != null) {
							message.what = 0;
							message.obj = photoEntry; //bitmap;

							handler.sendMessage(message);
						}
					}

					@Override
					public void onLoadingCancelled(String s, View view) {
						if (handler != null) {
							message.what = 2;
							message.obj = s;

							handler.sendMessage(message);
						}
					}
				});
			} else {
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inPreferredConfig = Bitmap.Config.RGB_565;
				Bitmap bitmap = BitmapFactory.decodeFile(filepath, options);

				ViewGroup.LayoutParams params = imageView.getLayoutParams();
				if (width == -1) {
					params.height = (int) convertDpToPixel(context, 160); //imageView.getHeight();
				} else if (width != 0) {
					params.width = width;
				} else {
					params.width = bitmap.getWidth();
					params.height = bitmap.getHeight();
				}
				imageView.setImageBitmap(bitmap);
				imageView.setLayoutParams(params);

				if (imageView instanceof ImageView) {
					((ImageView) imageView).setAdjustViewBounds(true);
					if (width == -1)
						((ImageView) imageView).setScaleType(ImageView.ScaleType.CENTER_CROP);
					else
						((ImageView) imageView).setScaleType(ImageView.ScaleType.FIT_CENTER);
				}

				imageView.invalidate();

				photoEntry.setPhotoData(bitmap);

				if (handler != null) {
					message.what = 0;
					message.obj = photoEntry; // bitmap;

					handler.sendMessage(message);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();

			if (handler != null) {
				Message message = new Message();
				message.what = 1;
				message.obj = e.getLocalizedMessage();

				handler.sendMessage(message);
			}
		}

	}

	public static void loadImageCache(Context context, String url, final Handler handler) {

		try {

			final Message message = new Message();
			PhotoEntry photoEntry = new PhotoEntry();

			if (TextUtils.isEmpty(url)) {
				return;
			}

			final String filepath = getCacheFileName(context, url);
			Date lastModDate = new Date();
			long fileSize = 0;
			String fname = "";
			if ((new File(filepath)).exists()) {
				File file = new File(filepath);
				fname = Uri.fromFile(file).getLastPathSegment();
				lastModDate = new Date(file.lastModified());
				fileSize = file.length();

				photoEntry.setPhotoName(getLastFilename(url));
				photoEntry.setPhotoType(getMimeType(url));

			}

			boolean doCache = false;
			String cacheName = "image_cache_" + fname;
			if (!TextUtils.isEmpty(fname)) {
				String ts = getSharedPreferencesString(cacheName, context);

				String ca = String.format(Locale.getDefault(), "%s%s", String.valueOf(lastModDate.getTime()), String.valueOf(fileSize));

				if (!TextUtils.isEmpty(ca) && ca.equals(ts)) {
					doCache = true;
				}
			}

			// 캐시처리가 아니라면!
			if (!doCache) {
//			if (!(new File(filepath)).exists() || lastModDate.getTime() < lastSeconds) {

				if (url.contains("http") && url.contains("?"))
					url = String.format("%s&ts=%s", url, String.valueOf(System.currentTimeMillis()));
				else if (url.contains("http"))
					url = String.format("%s?ts=%s", url, String.valueOf(System.currentTimeMillis()));

//				myLog.d(TAG, "*** loadImageCache: url: "+url);

				DisplayImageOptions options;

				ImageLoader imageLoader = ImageLoader.getInstance();

				if (!imageLoader.isInited())
					imageLoader.init(ImageLoaderConfiguration.createDefault(context));

				options = new DisplayImageOptions.Builder()
						.showImageForEmptyUri(R.drawable.ic_launcher)
						.showImageOnFail(R.drawable.ic_launcher)
						.resetViewBeforeLoading()
						.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
						.bitmapConfig(Bitmap.Config.RGB_565)
						.displayer(new FadeInBitmapDisplayer(300))
						.cacheOnDisk(false)
						.build();

				//String fullurl = Constant.SITE_URL[Constant.ISTEST]+File.separator+url;

//				String encodedUrl = URLEncoder.encode(url, "UTF-8");

//				myLog.d(TAG, "*** loadImageCache: url: "+url);
				String newUrl = url;
				try {
					newUrl = URLDecoder.decode(url, "UTF-8");
				} catch (Exception e) {
				}
//				myLog.d(TAG, "*** loadImageCache: newUrl: "+newUrl);

				imageLoader.loadImage(newUrl, options, new ImageLoadingListener() {
					@Override
					public void onLoadingStarted(String s, View view) {

					}

					@Override
					public void onLoadingFailed(String s, View view, FailReason failReason) {
						String errMessage = null;
						switch (failReason.getType()) {
							case IO_ERROR:
								errMessage = "Input/Output error";
								break;
							case OUT_OF_MEMORY:
								errMessage = "Out Of Memory error";
								break;
							case NETWORK_DENIED:
								errMessage = "Downloads are denied";
								break;
							case DECODING_ERROR:
								errMessage = "Unsupported URI scheme";
								break;
							case UNKNOWN:
								errMessage = "Unknown error";
								break;
						}

						if (handler != null) {
							message.what = 1;
							message.obj = errMessage;

							handler.sendMessage(message);
						}
					}

					@Override
					public void onLoadingComplete(String s, View view, Bitmap bitmap) {

						photoEntry.setPhotoData(bitmap);

						if (handler != null) {
							message.what = 0;
							message.obj = photoEntry; //bitmap;

							handler.sendMessage(message);
						}
					}

					@Override
					public void onLoadingCancelled(String s, View view) {
						if (handler != null) {
							message.what = 2;
							message.obj = s;

							handler.sendMessage(message);
						}
					}
				});
			} else {
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inPreferredConfig = Bitmap.Config.RGB_565;
				Bitmap bitmap = BitmapFactory.decodeFile(filepath, options);

				photoEntry.setPhotoData(bitmap);

				if (handler != null) {
					message.what = 0;
					message.obj = photoEntry; // bitmap;

					handler.sendMessage(message);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();

			if (handler != null) {
				Message message = new Message();
				message.what = 1;
				message.obj = e.getLocalizedMessage();

				handler.sendMessage(message);
			}
		}

	}

	@NonNull
	public static byte[] hexToByteArray(String hex) {
		if(!TextUtils.isEmpty(hex)) {
			hex = hex.length() % 2 != 0 ? "0" + hex : hex;

			byte[] b = new byte[hex.length() / 2];

			for (int i = 0; i < b.length; i++) {
				int index = i * 2;
				int v = Integer.parseInt(hex.substring(index, index + 2), 16);
				b[i] = (byte) v;
			}
			return b;
		}

		return new byte[]{};

	}

	public static boolean isEmpty(Object obj) {
		try {
			return "null".equalsIgnoreCase(String.valueOf(obj)) || TextUtils.isEmpty(String.valueOf(obj));
		} catch(Exception e) {}
		return false;
	}

	@NonNull
	public static String valueOf(Object obj) {
		if(!isEmpty(obj)) {
			return String.valueOf(obj);
		} else {
			return "";
		}
	}

	public static String setNewDefaultUUID(Context ctx, String uuid) {
		final File path = getStoragePath(ctx);

		if (!path.exists()) {
			path.mkdirs();
		}

		final File file = new File(path, Constant.DEVICE_ID_UUID_FILENAME);

		try {
			if (file.exists()) file.delete();
			writeUUID(file, uuid);
		} catch (Exception e) {
			e.printStackTrace();
			myLog.e(TAG, "*** setNewDefaultUUID Exception: " + e.getMessage());
			return "ERROR";
		} finally {
			myLog.d(TAG, "*** PATH: " + file.toString());
		}

		return uuid;
	}

}
