package com.domaado.mobileapp.task;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.TextView;

import com.domaado.mobileapp.App;
import com.domaado.mobileapp.Common;
import com.domaado.mobileapp.Constant;

import com.domaado.mobileapp.R;
import com.domaado.mobileapp.data.CheckUpdateRequest;
import com.domaado.mobileapp.data.CheckUpdateResponse;
import com.domaado.mobileapp.data.ResponseBase;
import com.domaado.mobileapp.network.HttpRequestor;
import com.domaado.mobileapp.widget.CustomAlertDialog;
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

/**
 * Created by jameshong on 2018. 1. 17..
 *
 * 새 앱이 존재하는지 확인!
 *
 */

public class CheckUpdateTask extends AsyncTask<String, String, CheckUpdateResponse> {

    private String TAG = "CheckUpdateTask";

    private Activity mActivity;
    private Handler resultHandler;

    private CheckUpdateRequest checkUpdateRequest;

    private JSONObject requestBody;

    private boolean isShowProgress = true;
    private ProgressDialog progressDialog = null;
    private Handler timeoutHandler = new Handler();
    CustomAlertDialog mesgbox;

    private Runnable timeoutCheckRunnable = new Runnable() {
        @Override
        public void run() {
            mesgbox = Common.alertMessage(mActivity, mActivity.getResources().getString(R.string.app_name), mActivity.getResources().getString(R.string.server_response_error), new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);

                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }

                    if(timeoutHandler != null) {
                        timeoutHandler.removeCallbacks(null);
                    }
                    mesgbox = null;

                    if(resultHandler!=null) {
                        msg.what = Constant.RESPONSE_TIMEOUT;

                        resultHandler.sendMessage(msg);
                    }
                }
            });

            if (mesgbox != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mesgbox.create();
                }

                mesgbox.show();
            }
        }
    };

    public CheckUpdateTask(Activity act, boolean showProgress, Handler handler) {
        this.mActivity = act;
        this.isShowProgress = showProgress;
        this.resultHandler = handler;

        checkUpdateRequest = new CheckUpdateRequest(mActivity);
        checkUpdateRequest.setAppVersion(Common.getVersionBuildCode(mActivity));

        if(App.getCurrentLocation()!=null) {
            checkUpdateRequest.setLat(App.getCurrentLocation().getLatitude());
            checkUpdateRequest.setLon(App.getCurrentLocation().getLongitude());
        }

        checkUpdateRequest.setHashKey(Common.getKeyHash(act));

        HashMap<String, Object> requestBodyMap =  checkUpdateRequest.getRequestParameterMap();
        requestBody = JsonUtils.mapToJson(requestBodyMap);

        //myLog.d(TAG, "*** body: "+requestBody);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        // show progress
        if(isShowProgress) {
            progressDialog = new ProgressDialog(mActivity);
            progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            progressDialog.show();
            progressDialog.setContentView(R.layout.custom_progressdialog_process);
            TextView mesg = (TextView) progressDialog.findViewById(R.id.cpp_progress_message);

            mesg.setText(mActivity.getResources().getString(R.string.server_loading_message));

            progressDialog.setCancelable(true);
        }

        timeoutHandler.postDelayed(timeoutCheckRunnable, (Constant.CONNECT_TIMEOUT));
    }

    @Override
    protected CheckUpdateResponse doInBackground(String... param) {
        CheckUpdateResponse result = null;

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
    protected void onPostExecute(CheckUpdateResponse result) {
        super.onPostExecute(result);

        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        if(timeoutHandler != null && timeoutCheckRunnable != null) {
            timeoutHandler.removeCallbacks(timeoutCheckRunnable);
        }

        if(resultHandler != null) {

            Message msg = new Message();
            msg.obj = result;

            if(result != null && !TextUtils.isEmpty(result.getResponseYn()) && "Y".equals(result.getResponseYn().toUpperCase())) {
                msg.what = Constant.RESPONSE_SUCCESS;
            } else {
                msg.what = Constant.RESPONSE_FAILURE;
            }

            resultHandler.sendMessage(msg);
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();

        if (progressDialog != null) {
            progressDialog.dismiss();
        }

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

            for(String field : response.fields) {
                if(json.has(field)) response.setBase(field, json.getString(field));
            }

            myLog.d(TAG, "**************************************************");
            myLog.d(TAG, response.toString());
            myLog.d(TAG, "**************************************************");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return response;
    }

    /**
     *
     * @param data
     * @return
     */
    private CheckUpdateResponse getResponseData(String data) {

        CheckUpdateResponse response = new CheckUpdateResponse();

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
        }

        return response;
    }
}
