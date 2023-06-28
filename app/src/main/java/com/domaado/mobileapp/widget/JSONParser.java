package com.domaado.mobileapp.widget;

import android.content.Context;

import com.domaado.mobileapp.Constant;

import com.domaado.mobileapp.R;
import com.domaado.mobileapp.network.SecureNetworkUtil;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

/**
 * 
 * @author idman99
 *
 */
public class JSONParser {

	final static String TAG = "JSONParser";
	
	Context mContext;
	
	/**
	 * 캐싱이 되어도 상관없는 요청일때!
	 */
	public final static boolean CACHE_ENABLE 	= true;
	/**
	 * 캐싱되면 절대 안되는 교환데이터들.. 예를들어 인증코드 회원정보 저장 괕은 요청들..
	 */
	public final static boolean CACHE_DISABLE 	= false;
	
    static InputStream is = null;
    static JSONObject jObj = null;
//    static String json = "";
    boolean isArray = false;
    
    boolean isCache = true;
    
    // constructor
    public JSONParser(Context ctx, boolean _isCache) {
    	this.mContext = ctx;
    	this.isCache = _isCache;
    }

    public JSONObject getJSONFromUrl(String url) {

    	StringBuffer cStringBuffer = new StringBuffer("");
    	
        try {
            // defaultHttpClient
            DefaultHttpClient httpClient = new DefaultHttpClient();
            httpClient.getParams().setParameter("http.connection.timeout", Constant.CONNECT_TIMEOUT * 1000);
            httpClient.getParams().setParameter("http.socket.timeout", Constant.READ_TIMEOUT * 1000);
            
            HttpGet httpGet = new HttpGet(url);

            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();
            
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
            //StringBuilder sb = new StringBuilder();
            cStringBuffer = new StringBuffer("");
            String line = null;
            while ((line = reader.readLine()) != null) {
            	cStringBuffer.append(new String(line.getBytes("UTF-8")) + "\n");
            }
            is.close();

        } catch (Exception e) {
            myLog.e(TAG, "InputStreamReader: " + e.toString());
        } finally {
			try { if(reader != null) { reader.close(); reader = null; } } catch(Throwable t) {}
			try { if(is != null) { is.close(); is = null; } } catch(Throwable t) {}
		}
		

        // try parse the string to a JSON object
        try {
            jObj = new JSONObject(cStringBuffer.toString());
            
        } catch (JSONException e) {
            myLog.e(TAG, "JSON Parser: Error parsing data: " + e.toString());
            myLog.d(TAG, "url="+url);
            myLog.d(TAG, "result="+cStringBuffer.toString());
        }

        return jObj;

    }

	/**
	 *
	 * @param mContext
	 * @param filename
	 * @param TAG_CONTACTS
	 * @param ITEMS
	 * @param encString
	 * @param param
	 * @param _isCache
	 * @return
	 */
    public static String[][] getJSONData(
//    		Activity act, 
    		Context mContext,
    		final String filename,
    		String TAG_CONTACTS,
    		final String[] ITEMS,
    		final String encString,
    		final String param,
    		final boolean _isCache) {
		
		String[][] result = null;
		int secSeq = 0;
		
		if(ITEMS != null) secSeq = ITEMS.length;
		
		try {

			String url = SecureNetworkUtil.getURLwithParams(mContext, mContext.getResources().getString(R.string.url_api_site)+filename, secSeq, encString, param);

			result = parserLoadJSONData(mContext, TAG_CONTACTS, url, ITEMS, _isCache);
						
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	public static String[][] parserLoadJSONData(Context ctx, final String TAG_CONTACTS, String url, final String[] ITEMS, boolean _isCache) {
		String[][] result = null;
		boolean isArray = false;
		
		JSONParser jParser = new JSONParser(ctx, _isCache);
		JSONObject json = jParser.getJSONFromUrl(url);
		
        Object jsonobj;
		try {
			// json이 ARRAY일 수도 있고, 그냥 OBJECT일 수도 있다. 그래서!
			jsonobj = json.get(TAG_CONTACTS);
			if(jsonobj instanceof JSONArray) isArray = true;

		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
		    // Getting Array of Contacts
			if(isArray) {
				JSONArray contacts = json.getJSONArray(TAG_CONTACTS);
				result = new String[contacts.length()][ITEMS.length];
			    
			    // looping through All Contacts
			    for(int i=0; i < contacts.length(); i++){
			        JSONObject c = contacts.getJSONObject(i);
	
			        for(int j=0; j < ITEMS.length; j++) {
			        	result[i][j] = c.getString(ITEMS[j]);
			        }
			    }
			} else {
				JSONObject obj = json.getJSONObject(TAG_CONTACTS);
				result = new String[1][ITEMS.length];
				
				for(int j=0; j < ITEMS.length; j++) {
					result[0][j] = obj.getString(ITEMS[j]);
				}
			}
		} catch (JSONException e) {
			myLog.d(TAG, "json="+json.toString());
		    e.printStackTrace();
		}
		
		return result;
	}
	
	/**
	 * JSON String 에서 index번째 데이터를 반환한다.
	 * 
	 * @param value
	 * @param index
	 * @param items
	 * @return
	 */
	public HashMap<String, Object> getJSONResult(String arrName, String value, int index, String[] items) {
		HashMap<String, Object> result = new HashMap<String, Object>();
		//final String[] resultItems = { "ret", "mesg" };	// 서버측과 필드명이 일치해야 함!
		
		//myLog.d(TAG, "getJSONResult: "+value);
		
		try {
			JSONObject json = new JSONObject(value);
			JSONArray jArr = json.getJSONArray(arrName);
			String[][] parseredData = new String[jArr.length()][items.length];
			for (int i = 0; i < jArr.length(); i++) {
				json = jArr.getJSONObject(i);
				
				for(int j = 0; j < items.length; j++) {
					parseredData[i][j] = json.getString(items[j]);
					if(index == i) {
						result.put(items[j], parseredData[i][j]);
					}
				}
				if(index == i) break;
				
			}
		} catch (JSONException e) {
			e.printStackTrace();
			
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		
		return result;
	}
}