package com.domaado.mobileapp.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.TextView;

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

import com.domaado.mobileapp.Constant;
import com.domaado.mobileapp.R;
import com.domaado.mobileapp.data.CheckUpdateRequest;
import com.domaado.mobileapp.data.CheckUpdateResponse;
import com.domaado.mobileapp.network.HttpRequestor;
import com.domaado.mobileapp.widget.JsonUtils;
import com.domaado.mobileapp.widget.myLog;

/**
 * Created by jameshong on 2018. 1. 17..
 *
 * 새 앱이 존재하는지 확인!
 *
 */
public class CheckUpdateTask extends AsyncTask<String, String, CheckUpdateResponse> {

    private String TAG = CheckUpdateTask.class.getSimpleName();

    private Context context;
    private Handler resultHandler;

    private JSONObject requestBody;

    private boolean isShowProgress = true;
    private ProgressDialog progressDialog = null;
    private Handler timeoutHandler = new Handler();

    private Runnable timeoutCheckRunnable = new Runnable() {
        @Override
        public void run() {
            if(timeoutHandler != null) {
                timeoutHandler.removeCallbacks(null);
            }

            if(resultHandler!=null) {
                Message msg = new Message();
                msg.what = Constant.RESPONSE_TIMEOUT;

                resultHandler.sendMessage(msg);
            }
        }
    };

    public CheckUpdateTask(Context context, CheckUpdateRequest request, boolean showProgress, Handler handler) {
        this.context = context;
        this.isShowProgress = showProgress;
        this.resultHandler = handler;

        HashMap<String, Object> requestBodyMap =  request.getRequestParameterMap();
        requestBody = JsonUtils.mapToJson(requestBodyMap);

//        myLog.d(TAG, "*** body: "+requestBody);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        // show progress
        if(isShowProgress) {
            progressDialog = new ProgressDialog(context);
            progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            progressDialog.show();
            progressDialog.setContentView(R.layout.custom_progressdialog_process);
            TextView mesg = (TextView) progressDialog.findViewById(R.id.cpp_progress_message);

            mesg.setText(context.getResources().getString(R.string.server_loading_message));

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
            HttpRequestor httpRequestor = new HttpRequestor(mURL, Constant.REQUEST_TIMEOUT);

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
     * @param jsonString
     * @return
     */
    private CheckUpdateResponse getResponseData(String jsonString) {

        CheckUpdateResponse response = new CheckUpdateResponse();

        //myLog.d(TAG, "*** decodeData: "+data);

        try {
            JSONObject json = new JSONObject(jsonString);
            for(String field : response.baseFields) {
                if(json.has(field)) response.setBase(field, !TextUtils.isEmpty(json.getString(field)) ? json.getString(field) : "");
            }

            if(!TextUtils.isEmpty(response.getResponseYn()) && "Y".equals(response.getResponseYn().toUpperCase())) {
                // 응답이 성공이면.
                if(json.has(response.OBJECTS_KEY[0]) && response.fields.length > 0 && !json.isNull(response.OBJECTS_KEY[0])) {
                    JSONObject data = json.getJSONObject(response.OBJECTS_KEY[0]);
                    for (String field : response.fields) {
                        if (data.has(field)) response.set(field, !TextUtils.isEmpty(data.getString(field)) ? data.getString(field) : "");
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return response;
    }
}
