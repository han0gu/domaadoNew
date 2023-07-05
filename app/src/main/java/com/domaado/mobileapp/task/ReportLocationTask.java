package com.domaado.mobileapp.task;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

import com.domaado.mobileapp.Constant;

import com.domaado.mobileapp.R;
import com.domaado.mobileapp.data.ReportLocationRequest;
import com.domaado.mobileapp.data.ReportLocationResponse;
import com.domaado.mobileapp.data.ResponseBase;
import com.domaado.mobileapp.network.HttpRequestor;
import com.domaado.mobileapp.widget.JsonUtils;
import com.domaado.mobileapp.widget.myLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

import static com.domaado.mobileapp.Constant.RESPONSE_FAILURE;
import static com.domaado.mobileapp.Constant.RESPONSE_SUCCESS;
import static com.domaado.mobileapp.Constant.RESPONSE_TIMEOUT;


/**
 * Created by jameshong on 2018. 6. 12..
 *
 * 기사위치를 주기적으로 서버에 보고한다.
 *
 */

public class ReportLocationTask extends AsyncTask<String, String, ReportLocationResponse> {

    private String TAG = ReportLocationTask.class.getSimpleName();

    private Context mContext;
    private Handler resultHandler;

    private JSONObject requestBody;

    private Handler timeoutHandler = new Handler(Looper.getMainLooper());

    private Runnable timeoutCheckRunnable = new Runnable() {
        @Override
        public void run() {

            Toast.makeText(mContext, mContext.getResources().getString(R.string.server_response_error), Toast.LENGTH_SHORT).show();

            if(timeoutHandler != null) {
                timeoutHandler.removeCallbacks(null);
            }

            if(resultHandler!=null) {
                Message msg = new Message();
                msg.what = RESPONSE_TIMEOUT;
                resultHandler.sendMessage(msg);
            }

        }
    };

    public ReportLocationTask(Context ctx, ReportLocationRequest reportLocationRequest, Handler handler) {
        this.mContext = ctx;
        this.resultHandler = handler;

        HashMap<String, Object> requestBodyMap =  reportLocationRequest.getRequestParameterMap();
        requestBody = JsonUtils.mapToJson(requestBodyMap);

        //myLog.d(TAG, "*** body: "+requestBody);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        timeoutHandler.postDelayed(timeoutCheckRunnable, (Constant.CONNECT_TIMEOUT));
    }

    @Override
    protected ReportLocationResponse doInBackground(String... param) {
        ReportLocationResponse result = null;

        StringBuffer value = new StringBuffer("");
        String host = param.length > 0 ? param[0] : "";
        String path = param.length > 1 ? param[1] : "";
        String body = requestBody != null ? requestBody.toString() : "";

        myLog.d(TAG, "*** URL:"+host+path);
        myLog.d(TAG, "*** body(\n"+body+")");

        if(TextUtils.isEmpty(host) || TextUtils.isEmpty(path)) return null;

        try {
            String URLstr = host + path;
            URL mURL = new URL(URLstr);
            HttpRequestor httpRequestor = new HttpRequestor(mURL, Constant.REUEST_TIMEOUT);

            InputStream inputStream = null;
            inputStream = httpRequestor.sendPost("application/json", body);

            BufferedReader bufferedReader = null;
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String strline = "";
            while((strline = bufferedReader.readLine()) != null) {
                value.append(strline).append("\n");
            }

            bufferedReader.close();
            inputStream.close();
            httpRequestor = null;
            mURL = null;

            myLog.d(TAG, "*** value="+value.toString());

            if(!TextUtils.isEmpty(value.toString())) {
                result = getResponseData(value.toString());
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    protected void onPostExecute(ReportLocationResponse result) {
        super.onPostExecute(result);

        if(timeoutHandler != null && timeoutCheckRunnable != null) {
            timeoutHandler.removeCallbacks(timeoutCheckRunnable);
        }

        if(resultHandler != null) {

            Message msg = new Message();
            msg.obj = result;

            if(result != null && !TextUtils.isEmpty(result.getResponseYn()) && "Y".equals(result.getResponseYn().toUpperCase())) {
                msg.what = RESPONSE_SUCCESS;
            } else {
                msg.what = RESPONSE_FAILURE;
            }

            resultHandler.sendMessage(msg);
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();

        if(timeoutHandler != null && timeoutCheckRunnable != null) {
            timeoutHandler.removeCallbacks(timeoutCheckRunnable);
        }
    }

    /**
     *
     * @param data
     * @return
     */
    public ResponseBase getResponse(String data) {
        ResponseBase response = new ResponseBase();

        try {
            JSONObject json = new JSONObject(data); //.getJSONObject("response");

            for(String field : response.baseFields) {
                if(json.has(field)) response.setBase(field, json.getString(field));
            }

            myLog.d(TAG, "**************************************************");
            myLog.d(TAG, response.toString());
            myLog.d(TAG, "**************************************************");

        } catch (JSONException e) {
            e.printStackTrace();
            response.setMessage(mContext.getResources().getString(R.string.server_json_data_error) + "\n" + e.getMessage());
        }

        return response;
    }

    /**
     *
     * @param data
     * @return
     */
    private ReportLocationResponse getResponseData(String data) {

        ReportLocationResponse response = new ReportLocationResponse();

        //myLog.d(TAG, "*** decodeData: "+data);

        try {
            ResponseBase responseBase = getResponse(data);

            response.setRequestId(responseBase.getRequestId());
            response.setResponseYn(responseBase.getResponseYn());
            response.setMessage(responseBase.getMessage());

            if(!TextUtils.isEmpty(responseBase.getResponseYn()) && "Y".equals(responseBase.getResponseYn().toUpperCase())) {
                // 응답이 성공이면.

                JSONObject json = new JSONObject(data);
                for(String field : response.fields) {
                    if(json.has(field)) response.set(field, json.getString(field));
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
            response.setMessage(mContext.getResources().getString(R.string.server_json_data_error) + "\n" + e.getMessage());
        }

        return response;
    }
}
