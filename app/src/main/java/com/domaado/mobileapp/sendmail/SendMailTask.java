package com.domaado.mobileapp.sendmail;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.SpannableString;
import android.text.TextUtils;


import com.domaado.mobileapp.R;
import com.domaado.mobileapp.network.HttpRequestor;
import com.domaado.mobileapp.widget.myLog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeoutException;

/**
 * Gmail을 통해 메일을 전송한다.
 * 
 * @author hongeuichan
 *
 */
public class SendMailTask extends AsyncTask<String, Void, String> {
	
	private final static String TAG = "SendMailTask";
	static Context mContext;
	Handler mHandler = null;
	
	public static final int SENDMAIL_START 							= 0;
	public static final int SENDMAIL_ERROR 							= 1;
	public static final int SENDMAIL_SUCCEED 						= 2;
	
	/**
	 * 
	 * @param ctx
	 */
	public SendMailTask(Context ctx) {
		mContext = ctx;
	}
	
	/**
	 * 
	 * @param context
	 * @param handler
	 */
	public SendMailTask(Context context, Handler handler){
		mContext = context;
		if(handler != null) {
			mHandler = handler;
		}
	}

	@Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

	/**
	 * 수신자주소, 발신자주소, 제목, 메일내용, 발신자이름
	 */
	@Override
	protected String doInBackground(String... arg) {
		// 오른쪽, 왼쪽 각각 얼마씩 적립할지 서버에서 정보를 갖고 온다.
		// 적립금액을 갖고 올때 RenewADtree flag 값도 같이 수신한다.
		
		mHandler.sendMessage(Message.obtain(mHandler, SENDMAIL_START));
		
		String ret = null;
		
		if(arg.length < 4) {
			myLog.e(TAG, "receiver address, sender address, subject, contents.. minium 4 required parameters!");
			return ret;
		}
		
		String mailaddr = arg[0];
		String from = arg[1];
		String subj = arg[2];
		String strMsg = arg[3];
		String sender = arg.length > 4 ? arg[4] : "";
		String bodytextflag = arg.length > 5 ? arg[5] : "";
		
		boolean isBodyText = TextUtils.isEmpty(bodytextflag) ? false : true;
		
		try {

            ret = httpRequestAuthMail(mailaddr, from, sender, subj, strMsg, isBodyText);

		} catch (Exception e) {
            myLog.e(TAG, e.getMessage());
            ret = e.getMessage();
            mHandler.sendMessage(Message.obtain(mHandler, SENDMAIL_ERROR, ret));
        }
		mHandler.sendMessage(Message.obtain(mHandler, SENDMAIL_SUCCEED, ret));
		
		return ret;
	}
	
	@Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
         
        if(result != null){
        	myLog.d(TAG, "result = " + result);
        }
         
    }
     
    @Override
    protected void onCancelled() {
        super.onCancelled();
    }
	
	public static String httpRequestAuthMail(String recp, String from, String sender, String subj, String body, boolean isBodyText) {
		
		StringBuffer cStringBuffer = new StringBuffer("");
		
		try {
			String URLstr = mContext.getString(R.string.url_site);
			String targetUrl = URLstr+"/auth_gmail.html";
			URL mURL = new URL(targetUrl);
			HttpRequestor cHttpRequestor = new HttpRequestor(mURL);
			cHttpRequestor.addParameter("recp", recp);
			cHttpRequestor.addParameter("from", from);
			cHttpRequestor.addParameter("sender", sender);
			cHttpRequestor.addParameter("subj", subj);
			if(isBodyText) {
				SpannableString htmlText = SpannableString.valueOf(body);
				cHttpRequestor.addParameter("body", Html.toHtml(htmlText));
			} else {
				cHttpRequestor.addParameter("body", body);
			}
			
			InputStream cInputStream = null;
			cInputStream = cHttpRequestor.sendPost("", "");
//			if(cInputStream.available() < 1) {
//				myLog.e(TAG, "Connection fail! URL("+ targetUrl+")");
//				return null;
//			}
			BufferedReader cBufferedReader = null;
			
			cBufferedReader = new BufferedReader(new InputStreamReader(cInputStream));
			
	    	String strLine = "";
	    	while((strLine = cBufferedReader.readLine()) != null)
			{
	    		cStringBuffer.append(strLine).append("\n");
			}
	    	cBufferedReader.close();
			cInputStream.close();
			cHttpRequestor = null;
			mURL = null;

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		}

		return cStringBuffer.toString();
	}
	
}
