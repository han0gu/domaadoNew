package com.domaado.mobileapp.widget;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * BUTTON COUNTDOWN THREAD
 */
public class ButtonCountdown extends Thread {

    private int countDownSeconds;
    private boolean countDownStop = false;

    private TextView view;
    private String text;
    private ProgressBar progressBar;
    private Handler handler;

    private Handler timeoutHandle;

    public ButtonCountdown(TextView _view, String _text, int _seconds, ProgressBar _progressBar, Handler _handler) {

        this.view = _view;
        this.text = _text;
        this.countDownSeconds = _seconds;
        this.progressBar = _progressBar;
        this.handler = _handler;

        if(this.view != null) {
            this.view.setVisibility(View.VISIBLE);
        }

        if(this.progressBar != null) {
            this.progressBar.setVisibility(View.VISIBLE);
        }

        this.timeoutHandle = new Handler() {
            public void handleMessage( Message msg) {

                view.setText(text + countDownSeconds);
                countDownSeconds--;

                if(countDownSeconds<0) {
                    if(progressBar!=null) {
                        progressBar.setVisibility(View.GONE);
                    }

                    handler.sendEmptyMessage(0);
                }

            }
        };
    }

    @Override
    public void run() {
        super.run();

        while(countDownSeconds >= 0) {
            if(countDownStop) break;

            try {
                timeoutHandle.sendMessage( timeoutHandle.obtainMessage());
                Thread.sleep(1000);
            } catch( Throwable t) {

            }
        }
    }

    @Override
    public void interrupt() {

        countDownStop = true;

        super.interrupt();
    }

}
