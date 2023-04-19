package com.domaado.mobileapp.widget;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.text.TextUtils;
import android.util.Log;

import com.domaado.mobileapp.Common;

public class myLog {
	//public static boolean debugMode = true;
	public static boolean debugMode = true;
	
	/**
	 * 버전의 소숫점 이하가 홀수인경우 디버그모드로 동작한다.
	 * 
	 * @param ctx
	 */
	public static boolean setDebugMode(Context ctx) {
		PackageInfo info = Common.getVersion(ctx);
		
		if(info != null) {
			String version = info.versionName;
			if(!TextUtils.isEmpty(version)) {
				String[] vv = version.split("\\.");
				if(vv.length > 1) {
					int verfloat = Integer.parseInt(vv[1]);
					if((verfloat % 2) == 1) {
						debugMode = true;
					} else {
						debugMode = false;
					}
//					
//					d("myLog", "vv="+Arrays.toString(vv)+" verfloat="+verfloat+", "+(verfloat%2));
				}
				
			}
			
		}
		
		i("myLog", "## debug mode is "+debugMode);
		
		return debugMode;
	}

	public static void e(String TAG, String msg) {
		if (debugMode == true) {
			Log.e(TAG, msg);
		}
	}

	public static void i(String TAG, String msg) {
		Log.i(TAG, msg);
	}

	public static void w(String TAG, String msg) {
		if (debugMode == true) {
			Log.w(TAG, msg);
		}
	}

	public static void v(String TAG, String msg) {
		if (debugMode == true) {
			Log.v(TAG, msg);
		}
	}
	
	public static void d(String TAG, String msg) {
		int maxLength = 500;

		if(debugMode) {
			Log.d(TAG, msg);
//			while( msg.length() > 0 )
//			{
//				if( msg.length() > maxLength )
//				{
//					Log.d( TAG, msg.substring( 0, maxLength )+"##\n");
//					msg = msg.substring( maxLength, maxLength );
//				}
//				else
//				{
//					Log.d( TAG, msg );
//					break;
//				}
//			}
		}
	}

}
