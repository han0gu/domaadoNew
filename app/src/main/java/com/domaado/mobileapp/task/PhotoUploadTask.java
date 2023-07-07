package com.domaado.mobileapp.task;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.TextView;

import com.domaado.mobileapp.Common;
import com.domaado.mobileapp.Constant;
import com.domaado.mobileapp.R;
import com.domaado.mobileapp.data.PhotoEntry;
import com.domaado.mobileapp.data.DomaadoImageResponse;
import com.domaado.mobileapp.network.HttpRequestor;
import com.domaado.mobileapp.widget.CustomAlertDialog;
import com.domaado.mobileapp.widget.FileUtils;
import com.domaado.mobileapp.widget.myLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * 파일 멀티파트 업로드!
 */
public class PhotoUploadTask extends AsyncTask<String, String, DomaadoImageResponse> {

    private String TAG = PhotoUploadTask.class.getSimpleName();

    private Activity mActivity;
    private Handler resultHandler;

    private boolean isShowProgress = true;
    private ProgressDialog progressDialog = null;
    private Handler timeoutHandler = new Handler();
    private CustomAlertDialog mesgbox;

    private PhotoEntry photoEntry;

    private Runnable timeoutCheckRunnable = new Runnable() {
        @Override
        public void run() {
            mesgbox = Common.alertMessage(mActivity, mActivity.getResources().getString(R.string.app_name), mActivity.getResources().getString(R.string.server_response_error), new Handler(msg -> {

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
                return true;
            }));

            if (mesgbox != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mesgbox.create();
                }

                mesgbox.show();
            }
        }
    };

    public PhotoUploadTask(Activity act, PhotoEntry photoEntry, boolean showProgress, Handler handler) {
        this.mActivity = act;
        this.isShowProgress = showProgress;
        this.resultHandler = handler;
        this.photoEntry = photoEntry;

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
    protected DomaadoImageResponse doInBackground(String... param) {
        DomaadoImageResponse result = null;

        StringBuffer value = new StringBuffer("");
        String host = param.length > 0 ? param[0] : "";
        String path = param.length > 1 ? param[1] : "";

//        String filePath = Common.uri2path(mActivity, photoEntry.getPhotoUri());
        File filePath = null;
        try {
            filePath = FileUtils.getFileFromUri(mActivity, photoEntry.getPhotoUri());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        myLog.d(TAG, "*** URL: " + host+path);
        myLog.d(TAG, "*** filePath: " + filePath);
        myLog.d(TAG, "*** photoEntry: " + photoEntry.toString());

        if(TextUtils.isEmpty(host) || filePath==null) {
            myLog.e(TAG, "*** REQUIRED Parameters (host, filepath)!!");
            return null;
        }

        try {
            String URLstr = host + path;
            URL mURL = new URL(URLstr);
            HttpRequestor httpRequestor = new HttpRequestor(mURL, Constant.REUEST_TIMEOUT);

            for (Map.Entry<String, Object> entry : photoEntry.getRequestParameterMapParams().entrySet()) {
                if(entry.getValue() instanceof String) {
                    myLog.d(TAG, "*** PARAMS: "+entry.getKey()+" = "+entry.getValue());
                    httpRequestor.addParameter(entry.getKey(), Common.valueOf(entry.getValue()));
                } else if(entry.getValue() instanceof Integer) {
                    myLog.d(TAG, "*** PARAMS: "+entry.getKey()+" = "+entry.getValue());
                    httpRequestor.addParameter(entry.getKey(), Common.valueOf(entry.getValue()));
                }
            }

            if(!TextUtils.isEmpty(photoEntry.getPhotoParam())) {
                Map<String, String> customParamsMap = Common.splitQuery(photoEntry.getPhotoParam());
                for (Map.Entry<String, String> entry : customParamsMap.entrySet()) {
                    myLog.d(TAG, "*** PARAMS: "+entry.getKey()+" = "+entry.getValue());
                    httpRequestor.addParameter(entry.getKey(), Common.valueOf(entry.getValue()));
                }
            }

            String photoName = !TextUtils.isEmpty(photoEntry.getPhotoName()) ? photoEntry.getPhotoName() : "imageFile";
            //String filepath = Common.getPathFromUri(mActivity.getContentResolver(), photoEntry.getPhotoUri());
            httpRequestor.addFile(photoName, filePath);

            InputStream inputStream = null;
            inputStream = httpRequestor.sendMultipartPost();

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
        }

        return result;
    }

    @Override
    protected void onPostExecute(DomaadoImageResponse result) {
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
    private DomaadoImageResponse getResponseData(String jsonString) {

        DomaadoImageResponse response = new DomaadoImageResponse();

        try {
            JSONObject json = new JSONObject(jsonString);
            for(String field : response.baseFields) {
                if(json.has(field)) response.setBase(field, json.getString(field));
            }

            for (String field : response.fields) {
                if (json.has(field)) response.set(field, json.getString(field));
            }

//            if(!TextUtils.isEmpty(response.getResponseYn()) && "Y".equals(response.getResponseYn().toUpperCase())) {
//                // 응답이 성공이면.
//                if(json.has(response.OBJECTS_KEY[0]) && response.fields.length > 0 && !json.isNull(response.OBJECTS_KEY[0])) {
//                    JSONObject data = json.getJSONObject(response.OBJECTS_KEY[0]);
//                    for (String field : response.fields) {
//                        if (data.has(field)) response.set(field, data.getString(field));
//                    }
//                }
//            }

        } catch (JSONException e) {
            e.printStackTrace();
            response.setMessage(mActivity.getResources().getString(R.string.server_json_data_error) + "\n" + e.getMessage());
        }

        return response;
    }
}
