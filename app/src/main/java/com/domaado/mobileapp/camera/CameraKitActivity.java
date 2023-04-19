package com.domaado.mobileapp.camera;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.camerakit.CameraKitView;
import com.domaado.mobileapp.R;
import com.domaado.mobileapp.widget.myLog;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;

public class CameraKitActivity extends AppCompatActivity implements View.OnClickListener {

    private String TAG = CameraKitActivity.class.getSimpleName();

    private CameraKitView cameraKitView;

    private LinearLayout dotbox;
    private Context mContext;
    //private AlarmManager mAlarmMgr;

    private HashMap<String, String> fnames;
    private int fnameSeq;
    private int MAX_SHOT = 6;

    private File mFile;
    private String mFilename;

    private boolean areWeFocused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.camerakit_get_quick);

        mContext = getBaseContext();

        //mAlarmMgr = (AlarmManager) getSystemService(ALARM_SERVICE);

        fnames = new HashMap<>();
        fnameSeq = 0;

        setUI();
    }

    private void setUI() {

        findViewById(R.id.camera_get_cancel_btn).setOnClickListener(this);
        findViewById(R.id.camera_get_base_btn).setOnClickListener(this);
        findViewById(R.id.camera_get_shot_btn).setOnClickListener(this);
        findViewById(R.id.camera_submit_btn).setOnClickListener(this);

        cameraKitView = findViewById(R.id.camera);

        cameraKitView.setPreviewListener(new CameraKitView.PreviewListener() {
            @Override
            public void onStart() {
                areWeFocused = true;
                toggleShootButton();
            }

            @Override
            public void onStop() {
                areWeFocused = false;
                toggleShootButton();
            }
        });

        initFilename();

        updateButtonCount();
    }

    private void shotCamera() {
        cameraKitView.captureImage(new CameraKitView.ImageCallback() {
            @Override
            public void onImage(CameraKitView cameraKitView, final byte[] capturedImage) {
                //File savedPhoto = new File(Environment.getExternalStorageDirectory(), "valet_imsi.jpg");
                try {
                    FileOutputStream outputStream = new FileOutputStream(mFile.getPath());
                    outputStream.write(capturedImage);
                    outputStream.close();

                    resultPhoto(mFile.getPath());

                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        cameraKitView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraKitView.onResume();
    }

    @Override
    protected void onPause() {
        cameraKitView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        cameraKitView.onStop();
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        cameraKitView.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onBackPressed() {
        (findViewById(R.id.camera_get_cancel_btn)).performClick();
    }

    @Override
    public void onClick(View v) {

        switch(v.getId()) {
            case R.id.camera_get_cancel_btn:
                myFinish();
                break;
            case R.id.camera_get_base_btn:
                runBaseCamera();
                break;
            case R.id.camera_get_shot_btn:
                shotCamera();
                break;
            case R.id.camera_submit_btn:
                resultPhoto();
                break;
        }
    }

    private void initFilename() {
        mFilename = Environment.getExternalStorageDirectory() + "/imsi_" + System.currentTimeMillis() + ".jpg";

        mFile = new File(mFilename); //getExternalFilesDir(null), "cardcompare_imsi.jpg");

        removeTempFile();
    }

    public void removeTempFile() {
        try {
            if (mFile != null) {
                mFile.delete();
            }

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void myFinish() {
        finish();
    }

//    private static final int MILLISINFUTURE = 11*1000;
//    private static final int COUNT_DOWN_INTERVAL = 1000;
//    private CountDownTimer countDownTimer;

//    public void countDownTimer(){
//        cancelAlarm();
//
//        countDownTimer = new CountDownTimer(MILLISINFUTURE, COUNT_DOWN_INTERVAL) {
//            public void onTick(long millisUntilFinished) {
//                //
//            }
//            public void onFinish() {
//                // alert!
//                timerAlert();
//            }
//        };
//    }
//
//    private void setAlarm() {
//        countDownTimer();
//        countDownTimer.start();
//    }
//
//    private void cancelAlarm() {
//        try {
//            countDownTimer.cancel();
//        } catch (Exception e) {}
//
//        countDownTimer=null;
//    }

    private void runBaseCamera() {
        Intent intent = new Intent();
        intent.putExtra("action", CameraUtil.RETRY_BASE_CAMERA);
        intent.putExtra("boxinfo", getDotBoxInfo());
        intent.putExtra("result",  CameraUtil.GET_PHOTO);
        setResult(RESULT_OK, intent);

        finish();
    }

//    private void timerAlert() {
//        Common.alertMessage(this,
//                getResources().getString(R.string.app_name),
//                getResources().getString(R.string.retry_base_camera),
//                getResources().getString(R.string.btn_ok),
//                new Handler(Looper.getMainLooper()) {
//
//                    @Override
//                    public void handleMessage(Message msg) {
//                        // TODO Auto-generated method stub
//                        super.handleMessage(msg);
//
//                        if(msg.what == 0) {
//                            runBaseCamera();
//                        }
//                    }
//
//                });
//    }

    private void toggleShootButton() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Button camera_get_shot_btn = (Button)findViewById(R.id.camera_get_shot_btn);

                if(areWeFocused) {
                    camera_get_shot_btn.setBackgroundResource(R.drawable.rounded_red_bg_ov);
                    camera_get_shot_btn.setEnabled(true);
                    //cancelAlarm();
                } else {
                    camera_get_shot_btn.setBackgroundResource(R.drawable.rounded_gray_bg);
                    camera_get_shot_btn.setEnabled(false);
                    //setAlarm();
                }
            }
        });
    }

    private String getDotBoxInfo() {

        //cancelAlarm();

        //FrameLayout cameraFrame = (FrameLayout)findViewById(R.id.container);

        dotbox = (LinearLayout)findViewById(R.id.camera_get_area);
        FrameLayout container = findViewById(R.id.container);

        myLog.d(TAG, "getDotBoxInfo: view w:"+ container.getMeasuredWidth()+", h:"+container.getMeasuredHeight());
        myLog.d(TAG, "getDotBoxInfo: box w:"+ dotbox.getMeasuredWidth()+", h:"+dotbox.getMeasuredHeight());

        //int[] locations = new int[2];
        //dotbox.getLocationInWindow(locations);

        int x = container.getMeasuredWidth(); //(textureView.getMeasuredWidth() - dotbox.getMeasuredWidth()) / 2;
        int y = container.getMeasuredHeight(); //(textureView.getMeasuredHeight() - dotbox.getMeasuredHeight()) / 2;
        int ex = dotbox.getMeasuredWidth(); //x + dotbox.getMeasuredWidth();
        int ey = dotbox.getMeasuredHeight(); //y + dotbox.getMeasuredHeight();
        float xx = dotbox.getX();
        float yy = dotbox.getY();

        int bottomMargin = 1; //(int)Common.convertDpToPixel(mContext, 80);

        String result = x+","+y+","+ex+","+ey+","+bottomMargin+","+xx+","+yy; //+","+mPreviewSize.getWidth()+","+mPreviewSize.getHeight();

        myLog.d(TAG, "getDotBoxInfo: "+ result);

        return result;
    }

    private void resultPhoto() {

        if(fnameSeq>0) {

            Intent intent = new Intent();
            intent.putExtra("action", CameraUtil.GET_PHOTO_ACTION);
            for(int i=0; i<fnames.size(); i++) {
                String key = String.format("photo%d", i+1);
                String value = fnames.get(String.valueOf(i));
                intent.putExtra(key, value);
            }
            intent.putExtra("max", fnames.size());

//			intent.putExtra("photo1", fnames.get("0"));
//			intent.putExtra("photo2", fnames.get("1"));
            intent.putExtra("boxinfo", getDotBoxInfo());
            intent.putExtra("result", CameraUtil.GET_PHOTO);
            setResult(RESULT_OK, intent);

            finish();
        }
    }

    private void resultPhoto(String fname) {

        addPhoto(fnameSeq, fname);

        Toast.makeText(this, fname, Toast.LENGTH_SHORT).show();

        if(fnameSeq>5) {

            resultPhoto();
        } else {
            if(fnameSeq>0) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.camera_submit_btn).setVisibility(View.VISIBLE);
                    }
                });
            }

            fnameSeq++;

            if(fnameSeq >= MAX_SHOT) {
//				runOnUiThread(new Runnable() {
//					@Override
//					public void run() {
//						findViewById(R.id.camera_submit_btn).performClick();
//					}
//				});

                resultPhoto();

                return;
            }

            initFilename();

            updateButtonCount();
        }
    }

    private void addPhoto(int fseq, String fname) {
        if(fnames==null) fnames = new HashMap<>();

        fnames.put(String.valueOf(fseq), fname);
    }

    private void updateButtonCount() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Button camera_get_shot_btn = findViewById(R.id.camera_get_shot_btn);
                if(fnames==null) fnames = new HashMap<>();
                int count = fnames.size();
                camera_get_shot_btn.setText(getResources().getString(R.string.btn_shot_count, String.valueOf(count+1), String.valueOf(MAX_SHOT)));
            }
        });
    }
}
