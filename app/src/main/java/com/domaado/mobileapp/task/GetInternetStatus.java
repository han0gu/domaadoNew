package com.domaado.mobileapp.task;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.widget.TextView;


import com.domaado.mobileapp.R;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by jameshong on 2018. 1. 17..
 *
 * API 서버 접속 테스트
 *
 */

public class GetInternetStatus extends AsyncTask<Void, Void, Boolean> {

    Activity mActivity;
    private ProgressDialog progressDialog = null;

    public GetInternetStatus(Activity act) {
        this.mActivity = act;
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

        mesg.setText(mActivity.getResources().getString(R.string.check_api_server));

        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @Override
    protected Boolean doInBackground(Void... params) {

        return hasInternetAccess();
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);

        if (progressDialog != null) {
            try {
                progressDialog.dismiss();
            } catch(Exception e){}
        }
    }

    protected boolean hasInternetAccess() {

        try {
            URL url = new URL(mActivity.getResources().getString(R.string.url_api_site));

            HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
            urlc.setRequestProperty("User-Agent", "Android Application:1");
            urlc.setRequestProperty("Connection", "close");
            urlc.setConnectTimeout(1000 * 30);
            urlc.connect();

            if (urlc.getResponseCode() == 200 || urlc.getResponseCode() > 400) {
                // Requested site is available
                return true;
            }
        } catch (Exception ex) {
            // Error while trying to connect
            ex.printStackTrace();
            return false;
        }

        return false;
    }
}
