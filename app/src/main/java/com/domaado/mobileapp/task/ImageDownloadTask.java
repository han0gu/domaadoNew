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

import com.domaado.mobileapp.Common;
import com.domaado.mobileapp.Constant;

import com.domaado.mobileapp.R;
import com.domaado.mobileapp.data.PhotoEntry;
import com.domaado.mobileapp.data.PhotoRequest;
import com.domaado.mobileapp.data.PhotoResponse;
import com.domaado.mobileapp.network.HttpRequestor;
import com.domaado.mobileapp.widget.CustomAlertDialog;
import com.domaado.mobileapp.widget.JsonUtils;
import com.domaado.mobileapp.widget.myLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

/**
 * Created by HongEuiChan on 2017. 8. 18..
 *
 */

public class ImageDownloadTask extends AsyncTask<String, String, String> {

    private String TAG = "ImageDownloadTask";

    private Activity mActivity;
    private Handler resultHandler;

    private PhotoRequest photoRequest;
    private PhotoResponse photoResponse;

    private JSONObject requestBody;

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
                        if (photoResponse != null) {
                            msg.what = 0;
                        } else {
                            msg.what = 1;
                        }

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

    public ImageDownloadTask(Activity act, String url, Handler handler) {
        this.mActivity = act;
        this.resultHandler = handler;

        photoRequest = new PhotoRequest();

        photoResponse = new PhotoResponse();

        HashMap<String, Object> requestBodyMap =  photoRequest.getRequestParameterMap();
        requestBody = JsonUtils.mapToJson(requestBodyMap);

        myLog.d(TAG, "*** DrawingDownloadTask: "+requestBody);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        // show progress
        progressDialog = new ProgressDialog(mActivity);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        progressDialog.show();
        progressDialog.setContentView(R.layout.custom_progressdialog_process);
        TextView mesg = (TextView)progressDialog.findViewById(R.id.cpp_progress_message);

        mesg.setText(mActivity.getResources().getString(R.string.server_image_download));

        progressDialog.setCancelable(true);

        timeoutHandler.postDelayed(timeoutCheckRunnable, (Constant.CONNECT_TIMEOUT));
    }

    @Override
    protected String doInBackground(String... param) {
        String result = null;

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
                String ret = getResponse(value.toString());

                if(photoResponse != null) {
                    decodeData(value.toString());
                }
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
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        if(timeoutHandler != null && timeoutCheckRunnable != null) {
            timeoutHandler.removeCallbacks(timeoutCheckRunnable);
        }

        if(resultHandler != null) {

            Message msg = new Message();
            msg.obj = photoResponse;

            if(photoResponse != null) {
                msg.what = 0;
            } else {
                msg.what = 1;
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
     "response": {
         "response_id": "85439",
         "message": "OK",
         "developer_message": "",
         "response_datetime": "1508425264174",
         "more_info": ""
     }

     * @param data
     * @return
     */

    public String getResponse(String data) {
        String result = "";

        try {
            JSONObject obj = new JSONObject(data).getJSONObject("response");

            result = obj.getString("response_yn");

            String requestId = obj.getString("request_id");
            String message = obj.getString("message");

            photoResponse.setRequestId(requestId);
            photoResponse.setResponseYn(result);
            photoResponse.setMessage(message);

            myLog.d(TAG, "**************************************************");
            myLog.d(TAG, photoResponse.toString());
            myLog.d(TAG, "**************************************************");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    private void decodeData(String data) {

        myLog.d(TAG, "*** decodeData: "+data);

        try {
            JSONObject json = new JSONObject(data);

            JSONArray arr = json.getJSONArray("list");
            ArrayList<PhotoEntry> photos = new ArrayList<>();

            for(int i=0; i<arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);

                PhotoEntry photoRow = new PhotoEntry();
                photoRow.setPhotoName(obj.getString("photo_name"));
                photoRow.setPhotoData(obj.getString("photo_data"));

                photos.add(photoRow);
            }

            photoResponse.setPhotos(photos);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
