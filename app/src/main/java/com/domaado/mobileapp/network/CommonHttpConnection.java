package com.domaado.mobileapp.network;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.domaado.mobileapp.Constant;

import org.apache.http.client.HttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class CommonHttpConnection implements Runnable {

	/*
	 * HTTP EVENT
	 */
	public static final int DID_START 							= 0;
	public static final int DID_ERROR 							= 1;
	public static final int DID_SUCCEED 						= 2;

	/*
	 * HTTP Method
	 */
	private static final int GET 								= 0;
	private static final int POST 								= 1;
	private static final int PUT 								= 2;
	private static final int DELETE 							= 3;
	private static final int BITMAP 							= 4;

	private static final int DEFAULT_TIMEOUT 					= 5000;

	private String url										= null;
	private int			method									= GET;
	private Handler handler									= null;
	private String data 									= null;
	private int			timeout									= DEFAULT_TIMEOUT;
	private HttpClient httpClient								= null;
	private boolean		stopFlag								= false;
	
	private static Context mContext;
	
	public static final boolean CACHE_ENABLE					= true;
	public static final boolean CACHE_DISABLE					= false;
	
	private boolean isCache = true;
	
	public CommonHttpConnection() {
		this(mContext, new Handler(), CACHE_ENABLE);
	}

	@Override
	protected void finalize() throws Throwable {
		url 		= null;
		handler 	= null;
		data 		= null;
		httpClient 	= null;
		
		super.finalize();
	}

	public CommonHttpConnection(Context ctx, Handler _handler, boolean _isCache) {
		CommonHttpConnection.mContext = ctx;
		handler = _handler;
		
		isCache = _isCache;
	}

	public void create(int method, String url, String data) {
		this.method = method;
		this.url = url;
		this.data = data;
		ConnectionManager.getInstance().push(this);
	}

	public void get(String url) {
		create(GET, url, null);
	}

	public void post(String url, String data) {
		create(POST, url, data);
	}

	public void put(String url, String data) {
		create(PUT, url, data);
	}

	public void delete(String url) {
		create(DELETE, url, null);
	}

	public void bitmap(String url) {
		create(BITMAP, url, null);
	}

	public void cancel() {
		handler = null;
		stopFlag = true;
	}
	
	public void run() {

		if(handler != null) {
			handler.sendMessage(Message.obtain(handler,
					CommonHttpConnection.DID_START));
		}
		
		StringBuffer sb = new StringBuffer("");
		
		URL urlcon;
		try {
			if(TextUtils.isEmpty(sb)) {
				
				urlcon = new URL(url);
				HttpURLConnection conn = (HttpURLConnection) urlcon.openConnection();
				if (conn != null) {
					conn.setConnectTimeout(this.getTimeout());
					conn.setUseCaches(false);
	
					try {
						validate(conn.getResponseCode()); // HTTP_OK?
						
						// 서버에서 읽어오기위한 스트림 객체
						InputStreamReader isr = new InputStreamReader(
								conn.getInputStream(), Constant.ENCODING);
						// 줄단위로 읽기 위해 BufferReader로 감싼다.
						BufferedReader br = new BufferedReader(isr);
	
						// Loop 돌면서 읽어오기 
						sb = new StringBuffer("");
						while (true) {
							String line = br.readLine();
							if (line == null) {
								break;
							}
							sb.append(line);
						}
	
						br.close();
						conn.disconnect();
						
						if(handler != null) {
							Message message = Message.obtain(handler, DID_SUCCEED, sb.toString());

							handler.sendMessage(message);
						}
					}catch(HTTPNotOkException e) {
						if(handler != null) {
						handler.sendMessage(Message.obtain(handler,
								CommonHttpConnection.DID_ERROR, e));
						}
					}
	
				}
			} else {
				// cache data 전달!
				if(handler != null) {
					Message message = Message.obtain(handler, DID_SUCCEED, sb.toString());
					handler.sendMessage(message);
				}
			}
		} catch (MalformedURLException e) {
			
			if(handler != null) {
				handler.sendMessage(Message.obtain(handler,
						CommonHttpConnection.DID_ERROR, e));
			}
		} catch (IOException e) {
			
			if(handler != null) {
				handler.sendMessage(Message.obtain(handler,
						CommonHttpConnection.DID_ERROR, e));
			}
		} catch (Exception e) {
			if(handler != null) {
				handler.sendMessage(Message.obtain(handler,
						CommonHttpConnection.DID_ERROR, e));
			}
		}
		

		ConnectionManager.getInstance().didComplete(this);
	}
	
	private void validate (int code) throws HTTPNotOkException {
		
		if(code != HttpURLConnection.HTTP_OK) {
			throw new HTTPNotOkException("HTTP Not OK");
		}
		
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

}
