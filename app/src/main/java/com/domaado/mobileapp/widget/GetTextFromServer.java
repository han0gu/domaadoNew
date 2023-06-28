package com.domaado.mobileapp.widget;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.widget.TextView;


import com.domaado.mobileapp.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * 
 * @author idman99
 *
 */
public class GetTextFromServer extends AsyncTask<String, String, String[]> {

	public final static int ISTEXT_CONTENTS = 0;
	public final static int ISHTML_CONTENTS = 1;

	Activity act;
	Context ctx;
	private ProgressDialog mProgressDialog;
	Handler mesgHandler;
	int[] tvs;
	int isContents;
	
	public GetTextFromServer(Activity _act, int _iscontents, int... _tvs) {
		act = _act;
		ctx = (Context)act;
		mesgHandler = processHandler(act);
		isContents = _iscontents;
		tvs = _tvs;
		
		mProgressDialog = new ProgressDialog(ctx);
	}
	
	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub

		super.onPreExecute();

	}
	
	@Override
	protected String[] doInBackground(String... params) {
		// TODO Auto-generated method stub
		String[] ret = new String[params.length];
		
		for(int i=0; i<params.length; i++) {
			ret[i] = getTextInfo(ctx, params[i]);
		}
		
		return ret;
	}
	
	@Override
	protected void onPostExecute(String[] result) {
		// TODO Auto-generated method stub

		super.onPostExecute(result);
		
//		mProgressDialog.dismiss();
		
		if(result.length > 0) {
			if(mesgHandler != null) {
				Bundle data = new Bundle();
				data.putStringArray("result", result);
				data.putIntArray("tvs", tvs);
				Message msg = new Message();
				msg.what = 0;
				msg.setData(data);
				
				mesgHandler.sendMessage(msg);
			}
		}
		
	}
	
	private String getTextInfo(Context ctx, String fileName) {
		String strLine = "";
		StringBuilder mStreamString = new StringBuilder();

		HttpURLConnection conn = null;
		InputStream is = null;
		
		try {
			String strUrl = ctx.getString(R.string.url_api_site);
			URL mURL = new URL(strUrl + "/" + fileName);
			conn = (HttpURLConnection) mURL.openConnection();
			is = conn.getInputStream();

			BufferedReader rd;
			rd = new BufferedReader(new InputStreamReader(is));

			while ((strLine = rd.readLine()) != null) {

				mStreamString.append(strLine);
				mStreamString.append("\n");

			}

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try { is.close(); is = null; } catch(Throwable t) {}
			try { conn.disconnect(); conn = null; } catch(Throwable t) {}
		}

		return mStreamString.toString();
	}
	
	public Handler processHandler(final Activity act) {
		return new Handler() {
			@Override
			public void handleMessage(Message msg) {
				final String[] result = msg.getData().getStringArray("result");
				final int[] tvs = msg.getData().getIntArray("tvs");
				
				act.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if(!act.isFinishing()) {
							for(int i=0; i<tvs.length; i++) {
								try{
									TextView v = (TextView) act.findViewById(tvs[i]);
									switch(isContents) {
									case ISHTML_CONTENTS:
										v.setText(Html.fromHtml(result[i]));
										break;
									case ISTEXT_CONTENTS:
									default:
										v.setText(result[i]);
										break;
									}
									
								}catch(Exception e) {
									e.printStackTrace();
								}
							}
						}
					}
				});
			}
		};
	}
}
