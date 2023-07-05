package com.domaado.mobileapp.network;

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

import com.domaado.mobileapp.App;
import com.domaado.mobileapp.Common;
import com.domaado.mobileapp.Constant;
import com.domaado.mobileapp.widget.myLog;

import java.util.HashMap;

public class SecureNetworkUtil {
	
	private final static String TAG = "SecureNetworkUtil";

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

	public static String getEncStringBase64(String value) throws Exception {

		if(TextUtils.isEmpty(new String(App.getIvByte())) || TextUtils.isEmpty(new String(App.getKeyByte()))) {
			myLog.e(TAG, "*** getEncStringBase64 - iv or key is null!");
		}

		String encrypted = null;
		MCrypt mcrypt = new MCrypt(App.getIvByte(), App.getKeyByte());

		if(!TextUtils.isEmpty(value))
			encrypted = Common.getBase64encode(mcrypt.encrypt(value));

		return encrypted;

	}

	public static String getDecStringBase64(String encString) {

		MCrypt mcrypt = new MCrypt(App.getIvByte(), App.getKeyByte());
		String decrypted = null;

		try {

//			myLog.e(TAG, "*** getDecStringBase64 - iv: "+new String(App.getIvByte()));
//			myLog.e(TAG, "*** getDecStringBase64 - key: "+new String(App.getKeyByte()));

//			encString = new String(Common.getBase64decode(encString));
//			decrypted = new String(mcrypt.decrypt(encString));

//			encString = new String(Common.getBase64decode(encString));
			decrypted = new String(mcrypt.decrypt(Common.getBase64decode(encString)));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return decrypted;
	}
}
