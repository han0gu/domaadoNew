package com.domaado.market;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.domaado.market.locale.LocaleUtils;

/**
 * Created by HongEuiChan on 2017. 10. 9..
 */

public class CheckInternetConnection extends AppCompatActivity {

    private Context mContext;
    private String TAG = "CheckInternetConnection";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.7f;
        getWindow().setAttributes(lpWindow);

        setContentView(R.layout.connection_issue);

        mContext = getBaseContext();

        Common.setTaskBarColored(this, 0);

        LocaleUtils.initialize(this);

        setUI();
    }

    private void setUI() {

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonAction(view.getId());
            }
        };

        (findViewById(R.id.con_issue_btn_finish)).setOnClickListener(clickListener);
        (findViewById(R.id.con_issue_btn_retry)).setOnClickListener(clickListener);

    }

    private void buttonAction(int viewid) {
        switch (viewid) {
            case R.id.con_issue_btn_finish:
                finish();
                break;
            case R.id.con_issue_btn_retry:
                connectionCheckAction();
                break;
        }
    }

    private void connectionCheckAction() {
        if(Common.checkNetworkConnect(this)) {
            Intent i = new Intent(this, SplashScreen.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

            startActivity(i);

            finish();
        } else {
            Common.alertMessage(this,
                    getResources().getString(R.string.app_name),
                    getResources().getString(R.string.conn_issue_desc2),
                    getResources().getString(R.string.btn_ok),
                    new Handler() {

                        @Override
                        public void handleMessage(Message msg) {
                            super.handleMessage(msg);
                        }
                    });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

}
