package com.domaado.market;

import android.content.Context;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.domaado.market.locale.LocaleUtils;
import com.domaado.market.widget.GetTextFromServer;

/**
 * Created by HongEuiChan on 2017. 10. 9..
 */

public class GuideView extends AppCompatActivity implements View.OnClickListener {

    private Context mContext;
    private String TAG = "GuideView";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.7f;
        getWindow().setAttributes(lpWindow);

        setContentView(R.layout.guide_view);

        mContext = getBaseContext();

        Common.setTaskBarColored(this, 0);

        LocaleUtils.initialize(this);

        setUI();
    }

    private void setUI() {
        ImageButton guideview_close_btn = (ImageButton)findViewById(R.id.guide_close_btn);

        guideview_close_btn.setOnClickListener(this);

        // contents
        if(getIntent()!=null) {
            String titleText = getIntent().getStringExtra("title");
            int contentUrl = getIntent().getIntExtra("contentUrl", 0);

            TextView title = (TextView)findViewById(R.id.guide_title);
            TextView content = (TextView) findViewById(R.id.guide_content);

            if(contentUrl>0) {
                loadContent(R.id.guide_content, getResources().getString(contentUrl)); //getResources().getString(contentId);
            }

            title.setText(titleText);
            content.setMovementMethod(new ScrollingMovementMethod());

            //content.setFocusable(false);
        }

    }

    private void loadContent(int res, String url) {
        int[] tvs = { res };

        String[] urls = { url };

        GetTextFromServer stfs = new GetTextFromServer(this, GetTextFromServer.ISTEXT_CONTENTS, tvs);
        stfs.execute(urls);
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

    @Override
    public void onClick(View v) {

        switch(v.getId()) {
            case R.id.guide_close_btn:
                finish();
                break;


        }
    }
}
