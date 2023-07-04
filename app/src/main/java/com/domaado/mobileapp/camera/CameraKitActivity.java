package com.domaado.mobileapp.camera;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.jameshong.camerakit.CameraKit;
import com.jameshong.camerakit.CameraKitError;
import com.jameshong.camerakit.CameraKitEvent;
import com.jameshong.camerakit.CameraKitEventListener;
import com.jameshong.camerakit.CameraKitImage;
import com.jameshong.camerakit.CameraKitImageTimer;
import com.jameshong.camerakit.CameraKitVideo;
import com.jameshong.camerakit.CameraView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

import com.domaado.mobileapp.Common;
import com.domaado.mobileapp.R;
import com.domaado.mobileapp.widget.myLog;

/**
 * cameraKit을 활용한 사진촬영
 */
public class CameraKitActivity extends AppCompatActivity implements View.OnClickListener {

    private String TAG = CameraKitActivity.class.getSimpleName();

    public static final String CAMERAKIT_KEY_IDX    = "idx";
    public static final String CAMERAKIT_KEY_TYPE   = "type";
    public static final String CAMERAKIT_KEY_SEQ    = "seq";

    public static final int CAMERA_TYPE_FACE        = 1;
    public static final int CAMERA_TYPE_BODY        = 2;

    private CameraView cameraKitView;
    private CameraKitImage cameraKitImage;
    private CameraKitImageTimer cameraKitImageTimer;

    private SeekBar camera_step_seekbar;

    private LinearLayout dotbox;
    private Context mContext;

    private HashMap<String, String> fnames;
    private int fnameSeq;
    public static final int MAX_SHOT = 3;

    private int previusProgress = 0;

    private File mFile;
    private String mFilename;

    private String CAMERA_IDX;
    private int CAMERA_TYPE     = 0;
    private int CAMERA_SEQ      = 0;

    private CameraUtil cameraUtil;

    private SoundPool shutter, countdown;

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

        cameraUtil = new CameraUtil(this);

        shutter = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);

        if(getIntent()!=null) {
            CAMERA_IDX = getIntent().getStringExtra(CAMERAKIT_KEY_IDX);
            try {
                CAMERA_TYPE = !TextUtils.isEmpty(getIntent().getStringExtra(CAMERAKIT_KEY_TYPE)) ? Integer.parseInt(getIntent().getStringExtra(CAMERAKIT_KEY_TYPE)) : 1;
            } catch (Exception e){}

            try {
                CAMERA_SEQ = !TextUtils.isEmpty(getIntent().getStringExtra(CAMERAKIT_KEY_SEQ)) ? Integer.parseInt(getIntent().getStringExtra(CAMERAKIT_KEY_SEQ)) : 0;
            } catch (Exception e){}
        }

        setUI();
    }

    private void setUI() {

//        findViewById(R.id.camera_get_cancel_btn).setOnClickListener(this);
        findViewById(R.id.camera_get_base_btn).setOnClickListener(this);
//        findViewById(R.id.camera_get_shot_btn).setOnClickListener(this);
        findViewById(R.id.camera_submit_btn).setOnClickListener(this);

        findViewById(R.id.camera_close_btn).setOnClickListener(this);

        findViewById(R.id.camera_retry_shot_btn).setOnClickListener(this);

        findViewById(R.id.camera_bottom_complete_box).setVisibility(View.GONE);
//        findViewById(R.id.camera_get_cancel_btn).setVisibility(View.GONE);
        findViewById(R.id.camera_get_base_btn).setVisibility(View.INVISIBLE);
//        findViewById(R.id.camera_get_shot_btn).setVisibility(View.GONE);

        findViewById(R.id.camera_countdown).setVisibility(View.GONE);

        cameraKitView = findViewById(R.id.camera);

        cameraKitView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent event) {
                if(event!=null && !TextUtils.isEmpty(event.getMessage())) {

                    myLog.e(TAG, "*** cameraKit onEvent: "+event.toString());

                    alertMessage(event.getMessage(), null);
                }
            }

            @Override
            public void onError(CameraKitError error) {
                if(error!=null && !TextUtils.isEmpty(error.getMessage())) {

                    myLog.e(TAG, "*** cameraKit onError: "+error.toString());

                    alertMessage(error.getMessage(), null);
                }
            }

            @Override
            public void onImage(CameraKitImage image) {
                try {
                    shutterSound();

                    cameraKitImage = image;

                    FileOutputStream outputStream = new FileOutputStream(mFile.getPath());
                    outputStream.write(image.getJpeg());
                    outputStream.close();

                    rotateImage(image.getJpeg(), mFile.getPath(), new Handler() {

                        @Override
                        public void handleMessage(Message msg) {
                            super.handleMessage(msg);

                            fnameSeq = camera_step_seekbar.getProgress();
                            resultPhoto(mFile.getPath());
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onVideo(CameraKitVideo video) {
                alertMessage("비디오는 지원하지 않습니다.", null);
            }

            @Override
            public void onImageTimer(CameraKitImageTimer image) {
                // timer shutter ui는 별도로 구현해야한다.

                int countdown = image.getCountDown();

                if(countdown==0) {
//                    alertMessage("타이머촬영", null);
                    try {
                        TextView camera_countdown = findViewById(R.id.camera_countdown);
                        camera_countdown.setVisibility(View.GONE);

                        shutterSound();

                        cameraKitImageTimer = image;

                        FileOutputStream outputStream = new FileOutputStream(mFile.getPath());
                        outputStream.write(image.getJpeg());
                        outputStream.close();

                        rotateImage(image.getJpeg(), mFile.getPath(), new Handler() {

                            @Override
                            public void handleMessage(Message msg) {
                                super.handleMessage(msg);

                                fnameSeq = camera_step_seekbar.getProgress();
                                resultPhoto(mFile.getPath());
                            }
                        });

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if(countdown == -1) {
                    // 카운트다운 시작!
                    shutterTimerSound();
                } else {
//                    Toast.makeText(CameraKitActivity.this, "COUNTDOWN:"+countdown, Toast.LENGTH_SHORT).show();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView camera_countdown = findViewById(R.id.camera_countdown);
                            camera_countdown.setVisibility(View.VISIBLE);
                            camera_countdown.setText(String.format("%d", countdown));
                        }
                    });
                    countdownSound();
                }

            }

        });

//        try {
//            Size cameraSize = cameraKitView.getPreviewSize();
//            String mesg = String.format(Locale.getDefault(), "%,d x %,d", cameraSize.getWidth(), cameraSize.getHeight());
//
//            myLog.e(TAG, "*** camera preview: "+mesg);
//
//            Toast.makeText(this, mesg, Toast.LENGTH_SHORT).show();
//
//        } catch(Exception e) {
//            e.printStackTrace();
//        }

        initFilename();

        camera_step_seekbar = findViewById(R.id.camera_step_seekbar);
        camera_step_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Button camera_submit_btn = findViewById(R.id.camera_submit_btn);
//                Button camera_get_shot_btn = findViewById(R.id.camera_get_shot_btn);

                setSeekBarTextStyle(progress);

                switch(progress) {
                    case 0:
                    case 1:
                    case 2:
                        camera_submit_btn.setVisibility(View.VISIBLE);
//                        camera_get_shot_btn.setVisibility(View.VISIBLE);
//                        camera_submit_btn.setText(getResources().getString(R.string.btn_next));
//                        camera_get_shot_btn.setText(getResources().getString(R.string.btn_shot));
                        findViewById(R.id.camera_bottom_complete_box).setVisibility(View.GONE);
                        findViewById(R.id.camera_control_box).setVisibility(View.VISIBLE);
                        break;
                    case 3:
                        camera_submit_btn.setVisibility(View.VISIBLE);
//                        camera_get_shot_btn.setVisibility(View.GONE);
//                        camera_submit_btn.setText(getResources().getString(R.string.btn_confirm));
                        findViewById(R.id.camera_bottom_complete_box).setVisibility(View.VISIBLE);
                        findViewById(R.id.camera_control_box).setVisibility(View.GONE);
                        break;
                }

                if(fromUser) {
                    if (fnames != null && TextUtils.isEmpty(fnames.get(String.valueOf(progress))) && previusProgress != progress) {
                        // 촬영되지 않은 스탭!
                        Toast.makeText(CameraKitActivity.this, progress + " 촬영된 사진이 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                }

                previusProgress = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        camera_step_seekbar.setProgress(0);

        if(CAMERA_TYPE==CAMERA_TYPE_BODY) {
            ((ImageView)findViewById(R.id.camera_guide_line)).setImageResource(R.drawable.camera_body_guide_fit);
            ((TextView)findViewById(R.id.camera_shot_guide_text)).setText(getResources().getString(R.string.camera_body_guide));
            cameraKitView.setFacing(CameraKit.Constants.FACING_BACK);
        } else if(CAMERA_TYPE==CAMERA_TYPE_FACE) {
            ((ImageView)findViewById(R.id.camera_guide_line)).setImageResource(R.drawable.camera_face_guide_fit);
            ((TextView)findViewById(R.id.camera_shot_guide_text)).setText(getResources().getString(R.string.camera_face_guide));
            cameraKitView.setFacing(CameraKit.Constants.FACING_FRONT);
        }

        updateButtonCount();
    }

    private void resetAllCamera() {

        if(camera_step_seekbar!=null) {
            camera_step_seekbar.setProgress(0);
            if(fnames!=null) fnames.clear();
        }

        showCameraControl();
    }

    private void rotateImage(byte[] data, String path, Handler handler) {
        Bitmap bitmap = Common.byteArrayToBitmap(data);
        Matrix matrix = new Matrix();
        matrix.postRotate(cameraUtil.getImageRotate(path));
        Bitmap cropped = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        data = Common.bitmapToByteArray(cropped);

        SaveBitmapToFile(Common.byteArrayToBitmap(data), path, handler);
    }

    private void SaveBitmapToFile(Bitmap bitmap, String strFilePath, Handler handler) {

        File fileCacheItem = new File(strFilePath);
        OutputStream out = null;

        try {
            fileCacheItem.createNewFile();
            out = new FileOutputStream(fileCacheItem);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(handler!=null) handler.sendEmptyMessage(0);
        }
    }

    public void shutterSound() {
        int shutterSound = shutter.load(this, R.raw.camera_shutter_click_03, 1);
        shutter.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                shutter.play(shutterSound, 1f, 1f, 1, 0, 1f);
            }
        });

    }

    public void countdownSound() {
        int countdownSound = shutter.load(this, R.raw.truck_horn, 1);
        shutter.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                shutter.play(countdownSound, 1f, 1f, 1, 0, 1.7f);
            }
        });

    }

    public void shutterTimerSound() {
        int countdownSound = shutter.load(this, R.raw.truck_horn, 1);
        shutter.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                shutter.play(countdownSound, 1f, 1f, 1, 0, 3f);
            }
        });

    }

//    private void shotCamera() {
//        cameraKitView.captureImage(new CameraKitView.ImageCallback() {
//            @Override
//            public void onImage(CameraKitView cameraKitView, final byte[] capturedImage) {
//                //File savedPhoto = new File(Environment.getExternalStorageDirectory(), "valet_imsi.jpg");
//                try {
//                    FileOutputStream outputStream = new FileOutputStream(mFile.getPath());
//                    outputStream.write(capturedImage);
//                    outputStream.close();
//
//                    fnameSeq = camera_step_seekbar.getProgress();
//
//                    resultPhoto(mFile.getPath());
//
//                } catch (java.io.IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraKitView.start();
    }

    @Override
    protected void onPause() {
        try {
            cameraKitView.stop();
        } catch(Exception e) {
            e.printStackTrace();
        }
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //cameraKitView.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onBackPressed() {
        (findViewById(R.id.camera_close_btn)).performClick();
    }

    @Override
    public void onClick(View v) {

        switch(v.getId()) {
//            case R.id.camera_get_cancel_btn:
            case R.id.camera_close_btn:
                myFinish();
                break;
            case R.id.camera_get_base_btn:
                runBaseCamera();
                break;
//            case R.id.camera_get_shot_btn:
////                shotCamera();
//                break;
            case R.id.camera_submit_btn:
                resultPhoto();
                break;
            case R.id.camera_retry_shot_btn:
//                shotCamera();
//                showCameraControl();
                resetAllCamera();
                break;
        }
    }

    private void initFilename() {
        String path = Environment.getExternalStorageDirectory() + getResources().getString(R.string.path);
        try {
            new File(path).mkdirs();
        } catch(Exception e) {
            e.printStackTrace();
            path = Environment.getExternalStorageDirectory().getPath();
        }

        mFilename = path + "/imsi_" + System.currentTimeMillis() + ".jpg";

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

    private void alertMessage(String message, final Handler handler) {
        Common.alertMessage(this,
                getResources().getString(R.string.app_name),
                message,
                getResources().getString(R.string.btn_ok),
                new Handler(Looper.getMainLooper()) {

                    @Override
                    public void handleMessage(Message msg) {
                        // TODO Auto-generated method stub
                        super.handleMessage(msg);

                        if(handler!=null) handler.sendEmptyMessage(0);
                    }

                });
    }
//
//    private void toggleShootButton() {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                Button camera_get_shot_btn = (Button)findViewById(R.id.camera_get_shot_btn);
//
//                if(areWeFocused) {
//                    camera_get_shot_btn.setBackgroundResource(R.drawable.rounded_red_bg_ov);
//                    camera_get_shot_btn.setEnabled(true);
//                    //cancelAlarm();
//                } else {
//                    camera_get_shot_btn.setBackgroundResource(R.drawable.rounded_gray_bg);
//                    camera_get_shot_btn.setEnabled(false);
//                    //setAlarm();
//                }
//            }
//        });
//    }

    private String getDotBoxInfo() {

        //cancelAlarm();

        //FrameLayout cameraFrame = (FrameLayout)findViewById(R.id.container);

        dotbox = (LinearLayout)findViewById(R.id.camera_get_area);
        RelativeLayout container = findViewById(R.id.container);

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

            intent.putExtra("boxinfo", getDotBoxInfo());
            intent.putExtra("result", CameraUtil.GET_PHOTO);

            intent.putExtra(CAMERAKIT_KEY_IDX, CAMERA_IDX);
            intent.putExtra(CAMERAKIT_KEY_SEQ, CAMERA_SEQ);
            intent.putExtra(CAMERAKIT_KEY_TYPE, CAMERA_TYPE);

            setResult(RESULT_OK, intent);

            finish();
        }
    }

    private void resultPhoto(String fname) {

        addPhoto(fname);

        //Toast.makeText(this, fname, Toast.LENGTH_SHORT).show();


        if(CAMERA_SEQ>0) {
            // 단컷을 요청한 경우!
            resultPhoto();
        } else {

            if (fnameSeq >= MAX_SHOT) {
                resultPhoto();
            } else {
                if (fnameSeq > 0) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            findViewById(R.id.camera_submit_btn).setVisibility(View.VISIBLE);
                        }
                    });
                }

                fnameSeq++;

                // for automatic submit!
//                if (fnameSeq >= MAX_SHOT) {
//                    resultPhoto();
//                    return;
//                }

                initFilename();

                updateButtonCount();
            }
        }
    }

    private void addPhoto(String fname) {
        if(fnames==null) fnames = new HashMap<>();

        if(TextUtils.isEmpty(fnames.get(String.valueOf(fnameSeq)))) {
            fnames.put(String.valueOf(fnameSeq), fname);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                fnames.replace(String.valueOf(fnameSeq), fname);
            } else {
                fnames.remove(String.valueOf(fnameSeq));
                fnames.put(String.valueOf(fnameSeq), fname);
            }
        }

        myLog.d(TAG, "*** addPhoto: "+fnames.toString());
    }

    private void setSeekBarTextStyle(int count) {
        int[] textRes = { R.id.camera_step1_text, R.id.camera_step2_text, R.id.camera_step3_text };

        for(int i=0; i<textRes.length; i++) {
            TextView textView = findViewById(textRes[i]);
            if(i==count) {
                textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    textView.setTextColor(getResources().getColor(R.color.White, null));
                } else {
                    textView.setTextColor(getResources().getColor(R.color.White));
                }
            } else {
                textView.setTypeface(textView.getTypeface(), Typeface.NORMAL);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    textView.setTextColor(getResources().getColor(R.color.theme_btn_color, null));
                } else {
                    textView.setTextColor(getResources().getColor(R.color.theme_btn_color));
                }
            }
        }
    }

    private void updateButtonCount() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Button camera_submit_btn = findViewById(R.id.camera_submit_btn);
//                Button camera_get_shot_btn = findViewById(R.id.camera_get_shot_btn);

                if(fnames==null) fnames = new HashMap<>();
                int count = fnames.size();

                for(int i=0; i<MAX_SHOT; i++) {
                    if(TextUtils.isEmpty(fnames.get(String.valueOf(i)))) {
                        camera_step_seekbar.setProgress(i);
                        break;
                    }
                }

//                camera_step_seekbar.setProgress(count);

                switch(count) {
                    case 1:
                    case 2:
                        camera_submit_btn.setVisibility(View.VISIBLE);
//                        camera_get_shot_btn.setVisibility(View.VISIBLE);
//                        camera_submit_btn.setText(getResources().getString(R.string.btn_next));
//                        camera_get_shot_btn.setText(getResources().getString(R.string.btn_shot));
                        findViewById(R.id.camera_bottom_complete_box).setVisibility(View.GONE);
                        findViewById(R.id.camera_control_box).setVisibility(View.VISIBLE);
                        break;
                    case 3:
                        camera_submit_btn.setVisibility(View.VISIBLE);
//                        camera_get_shot_btn.setVisibility(View.GONE);
//                        camera_submit_btn.setText(getResources().getString(R.string.btn_confirm));
                        findViewById(R.id.camera_bottom_complete_box).setVisibility(View.VISIBLE);
                        findViewById(R.id.camera_control_box).setVisibility(View.GONE);
                        break;
                }

            }
        });
    }

    private void showCameraControl() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.camera_bottom_complete_box).setVisibility(View.GONE);
                findViewById(R.id.camera_control_box).setVisibility(View.VISIBLE);
            }
        });

    }
}
