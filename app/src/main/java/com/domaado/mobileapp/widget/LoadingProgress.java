package com.domaado.mobileapp.widget;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import com.domaado.mobileapp.Common;
import com.domaado.mobileapp.Constant;
import com.domaado.mobileapp.R;


public class LoadingProgress {

    public static Activity activity;
    private static LoadingProgress loadingProgress;

    private ProgressDialog progressDialog = null;
    private Handler timeoutHandler = new Handler();
    private CustomAlertDialog mesgbox;

    public static Handler resultHandler;

    private Runnable timeoutCheckRunnable = new Runnable() {
        @Override
        public void run() {
            mesgbox = Common.alertMessage(activity,
                    activity.getResources().getString(R.string.app_name),
                    activity.getResources().getString(R.string.can_not_connect_server),
                    activity.getResources().getString(R.string.btn_ok),
                    new Handler() {
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
                        Message msg2 = new Message();
                        msg2.what = Constant.RESPONSE_TIMEOUT;

                        resultHandler.sendMessage(msg2);
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

    public LoadingProgress() {
    }

    public static synchronized LoadingProgress getInstance(Activity act) {
        activity = act;

        if(loadingProgress==null) {
            loadingProgress = new LoadingProgress();
        }

        return loadingProgress;
    }

    public static synchronized LoadingProgress getInstance(Activity act, Handler handler) {
        activity = act;
        resultHandler = handler;

        if(loadingProgress==null) {
            loadingProgress = new LoadingProgress();
        }

        return loadingProgress;
    }

    public void show() {
        if(progressDialog != null && progressDialog.isShowing()) {
            return;
        }

        progressDialog = new ProgressDialog(activity);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        progressDialog.show();
        progressDialog.setContentView(R.layout.custom_progressdialog_process);
        TextView mesg = (TextView) progressDialog.findViewById(R.id.cpp_progress_message);

        mesg.setText(activity.getResources().getString(R.string.server_loading_message));

        progressDialog.setCancelable(false);

        timeoutHandler.postDelayed(timeoutCheckRunnable, (Constant.CONNECT_TIMEOUT));
    }

    public void dismiss() {
        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            if (timeoutHandler != null && timeoutCheckRunnable != null) {
                timeoutHandler.removeCallbacks(timeoutCheckRunnable);
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public boolean isShow() {
        if(progressDialog != null && progressDialog.isShowing()) return true;
        else return false;
    }
}
