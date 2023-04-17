package com.domaado.market.network;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.domaado.market.Common;
import com.domaado.market.Constant;
import com.domaado.market.widget.myLog;

import java.util.HashMap;

public class SecureNetworkUtil {
	
	private final static String TAG = "SecureNetworkUtil";

	@SuppressLint("InlinedApi")
	@SuppressWarnings("deprecation")
	public static boolean isAirplaneMode(Context context) {

		int result;
		try {
			if (Build.VERSION.SDK_INT < 17) {
				result = Settings.System.getInt(context.getContentResolver(),
						Settings.System.AIRPLANE_MODE_ON);
			} else {
				result = Settings.System.getInt(context.getContentResolver(),
						Settings.Global.AIRPLANE_MODE_ON);
			}

			return result != 0;
		} catch (SettingNotFoundException e) {
			return false;
		}
	}

	public static boolean isWifiMode(Context context) {

		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo ni = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		boolean isWifiConn = ni.isConnected();

		return isWifiConn;
	}

	public static boolean isMobileMode(Context context) {

		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo ni = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		boolean isMobileConn = ni.isConnected();

		return isMobileConn;
	}

	public static boolean isRoaming(Context context) {

		TelephonyManager telephony = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
        return telephony.isNetworkRoaming() == true;

    }

	public static String getWifiMACAddress(Context context) {

		WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		String macAddress = wifiInfo.getMacAddress();

		return macAddress;
	}

	public static String getMobileNetworkMACAddress() {

		String result = null;

		return result;
	}

	public static boolean isDualUsim() {
		boolean result = false;

		return result;
	}

	/**
	 * 
	 * @param ctx
	 * @param url
	 * @param no
	 * @param username
	 * @return
	 * @throws Exception
	 */
	public static String getURLwithParams(Context ctx, String url, int no, String username, String param) throws Exception {
		String LocaleStr = java.util.Locale.getDefault().toString();
		String temp = null;

		temp = url + "?enc=" + getEncString(no, username); // encrypted;
		
		temp = temp + "&no=" + String.valueOf(no);
//		temp = temp + "&uid=" + username;
		temp = temp + "&country=" + LocaleStr;
		if(!TextUtils.isEmpty(param)) temp = temp + "&" + param;

		myLog.d(TAG, "getURLwithParams="+temp);
		
		return temp;

	}
	
	public static String getURLwithParams(Context ctx, String url, int no, HashMap<String, String> addrParam, String[] paramName, String param) throws Exception {
		String LocaleStr = java.util.Locale.getDefault().toString();
		String temp = null;

		temp = url + "?enc=" + getEncString(no, Common.getEncryptStringFromLinkParams(addrParam, paramName)); // encrypted;
		
		temp = temp + "&no=" + String.valueOf(no);

		temp = temp + "&country=" + LocaleStr;
		if(!TextUtils.isEmpty(param)) temp = temp + "&" + param;

		myLog.d(TAG, "getURLwithParams="+temp);
		
		return temp;

	}
	
	public static String getURLwithParams(Context ctx, String url, int no, String username) throws Exception {
		
		return getURLwithParams(ctx, url, no, username, null);

	}

	/**
	 *
	 * @param no
	 * @param username
	 * @return
	 * @throws Exception
	 */
	public static String getEncString(int no, String username) throws Exception {
		
		String encrypted = null;
		String iv = (String.valueOf(no)+ Constant.MYCRYPT_IV(Constant.cryptIvs)).substring(0,16);
		MCrypt mcrypt = new MCrypt(iv);
		
		encrypted = MCrypt.bytesToHex(mcrypt.encrypt(username));

		return encrypted;
		
	}
	
	public static String getDecString(int no, String encString) {
		
		String iv = (String.valueOf(no)+Constant.MYCRYPT_IV(Constant.cryptIvs)).substring(0,16);
		MCrypt mcrypt = new MCrypt(iv);
		String decrypted = null;
		
		try {
			decrypted = new String(mcrypt.decrypt(encString));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return decrypted;
	}
	
	public static String getAsciiFilter(String str) {
	    StringBuilder filtered = new StringBuilder(str.length());
	    for (int i = 0; i < str.length(); i++) {
	        char current = str.charAt(i);
	        if (current >= 0x20 && current <= 0x7e) {
	            filtered.append(current);
	        }
	    }

	    return filtered.toString();
	}
	
	
}
