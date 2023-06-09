package com.domaado.mobileapp.camera;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import com.jameshong.camerakit.CameraKit;
import com.jameshong.camerakit.CameraKitEventCallback;
import com.jameshong.camerakit.CameraKitImage;
import com.jameshong.camerakit.CameraKitImageTimer;
import com.jameshong.camerakit.CameraKitVideo;
import com.jameshong.camerakit.CameraView;
import com.jameshong.camerakit.OnCameraKitEvent;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnTouch;
import com.domaado.mobileapp.R;

/**
 * 카메라 컨트럴 위젯
 */
public class CameraControls extends LinearLayout {

    private int cameraViewId = -1;
    private CameraView cameraView;

    private int coverViewId = -1;
    private View coverView;

    @BindView(R.id.facingButton)
    ImageView facingButton;

    @BindView(R.id.flashButton)
    ImageView flashButton;

    private long captureDownTime;
    private long captureStartTime;
    private boolean pendingVideoCapture;
    private boolean capturingVideo;

    public CameraControls(Context context) {
        this(context, null);
    }

    public CameraControls(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraControls(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(getContext()).inflate(R.layout.camera_controls, this);
        ButterKnife.bind(this);

        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.CameraControls,
                    0, 0);

            try {
                cameraViewId = a.getResourceId(R.styleable.CameraControls_camera, -1);
                coverViewId = a.getResourceId(R.styleable.CameraControls_cover, -1);
            } finally {
                a.recycle();
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (cameraViewId != -1) {
            View view = getRootView().findViewById(cameraViewId);
            if (view instanceof CameraView) {
                cameraView = (CameraView) view;
                cameraView.bindCameraKitListener(this);
                setFacingImageBasedOnCamera();
            }
        }

        if (coverViewId != -1) {
            View view = getRootView().findViewById(coverViewId);
            if (view != null) {
                coverView = view;
                coverView.setVisibility(GONE);
            }
        }
    }

    private void setFacingImageBasedOnCamera() {
        if (cameraView.isFacingFront()) {
            facingButton.setImageResource(R.drawable.ic_facing_back);
        } else {
            facingButton.setImageResource(R.drawable.ic_facing_front);
        }
    }

    //@OnCameraKitEvent(CameraKitImage.class)
    public void imageCaptured(CameraKitImage image) {
        byte[] jpeg = image.getJpeg();

        long callbackTime = System.currentTimeMillis();
        ResultHolder.dispose();
        ResultHolder.setImage(jpeg);
        ResultHolder.setNativeCaptureSize(cameraView.getCaptureSize());
        ResultHolder.setTimeToCallback(callbackTime - captureStartTime);

//        Intent intent = new Intent(getContext(), PreviewActivity.class);
//        getContext().startActivity(intent);
    }

    public void imageCapturedTimer(CameraKitImageTimer image) {
        byte[] jpeg = image.getJpeg();

        long callbackTime = System.currentTimeMillis();
        ResultHolder.dispose();
        ResultHolder.setImage(jpeg);
        ResultHolder.setNativeCaptureSize(cameraView.getCaptureSize());
        ResultHolder.setTimeToCallback(callbackTime - captureStartTime);

    }

    @OnCameraKitEvent(CameraKitVideo.class)
    public void videoCaptured(CameraKitVideo video) {
        File videoFile = video.getVideoFile();
        if (videoFile != null) {
            ResultHolder.dispose();
            ResultHolder.setVideo(videoFile);
            ResultHolder.setNativeCaptureSize(cameraView.getCaptureSize());

            Intent intent = new Intent(getContext(), CameraKitActivity.class);
            getContext().startActivity(intent);
        }
    }

    boolean nowShooting = false;

    // for long touch!
    Timer timer;
    TimerTask timerTask;
    boolean isTimerShutter = false;
    CountDownTimer countDownTimer;

    public synchronized boolean isTimerShutter() {
        return isTimerShutter;
    }

    public synchronized void setTimerShutter(boolean timerShutter) {
        isTimerShutter = timerShutter;
    }

    private void timerCountdownShutter() {

        try {
            if (countDownTimer != null) {
                countDownTimer.cancel();
                countDownTimer = null;
            }
        } catch(Exception e) {}

        countDownTimer = new CountDownTimer(cameraView.getTimerCountdown()*1000, 1000) {

            public void onTick(long millisUntilFinished) {
//                if((int)(millisUntilFinished/1000)>0)
                    cameraView.captureImageCountdown((int)(millisUntilFinished/1000)+1);
            }

            public void onFinish() {
                captureStartTime = System.currentTimeMillis();
                cameraView.captureImageTimer(new CameraKitEventCallback<CameraKitImageTimer>() {
                    @Override
                    public void callback(CameraKitImageTimer event) {
                        event.setCountDown(0);
                        imageCapturedTimer(event);
                    }
                });

            }

        }.start();

    }

    @OnTouch(R.id.captureButton)
    boolean onTouchCapture(View view, MotionEvent motionEvent) {
        handleViewTouchFeedback(view, motionEvent);

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                captureDownTime = System.currentTimeMillis();
                pendingVideoCapture = true;
                setTimerShutter(false);

                // 촬영타이머 초기화!
                try {
                    if (countDownTimer != null) {
                        countDownTimer.cancel();
                        countDownTimer = null;
                    }
                } catch(Exception e) {}

                // 셔터타이머 초기화
                try {
                    if(timer!=null) {
                        timer.cancel();
                        timer=null;
                    }

                    if(timerTask!=null) {
                        timerTask.cancel();
                        timerTask = null;
                    }

                } catch(Exception e){}

                timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        setTimerShutter(true);

                        // 카운트다운시작!
                        cameraView.captureImageCountdown(-1);

//                        nowShooting = true;
//                        captureStartTime = System.currentTimeMillis();
//                        cameraView.captureImageTimer(new CameraKitEventCallback<CameraKitImageTimer>() {
//                            @Override
//                            public void callback(CameraKitImageTimer event) {
//                                imageCapturedTimer(event);
////                                nowShooting = false;
//                            }
//                        });

                        timer.cancel();
                    }
                };
                if(timer==null) timer = new Timer();
                timer.schedule(timerTask, cameraView.getTimerShutter());

                /**
                 * 카메라촬영기능!
                 */
//                postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (pendingVideoCapture) {
//                            capturingVideo = true;
//                            cameraView.captureVideo();
//                        }
//                    }
//                }, 250);
                break;
            }

            case MotionEvent.ACTION_UP: {
                pendingVideoCapture = false;
                try {
                    timer.cancel();
                    timer = null;
                    timerTask.cancel();
                    timerTask = null;
                } catch(Exception e){}

                if (capturingVideo) {
                    capturingVideo = false;
                    cameraView.stopVideo();
                } else if(!nowShooting) {
                    nowShooting = true;
                    captureStartTime = System.currentTimeMillis();
                    if(!isTimerShutter()) {
                        cameraView.captureImage(new CameraKitEventCallback<CameraKitImage>() {
                            @Override
                            public void callback(CameraKitImage event) {
                                imageCaptured(event);
                                nowShooting = false;
                            }
                        });
                    } else {
                        timerCountdownShutter();
                        nowShooting = false;
                    }

                }
                break;
            }
        }
        return true;
    }

    @OnTouch(R.id.facingButton)
    boolean onTouchFacing(final View view, MotionEvent motionEvent) {
        handleViewTouchFeedback(view, motionEvent);
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_UP: {
                coverView.setAlpha(0);
                coverView.setVisibility(VISIBLE);
                coverView.animate()
                        .alpha(1)
                        .setStartDelay(0)
                        .setDuration(300)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                if (cameraView.isFacingFront()) {
                                    cameraView.setFacing(CameraKit.Constants.FACING_BACK);
                                    changeViewImageResource((ImageView) view, R.drawable.ic_facing_front);
                                } else {
                                    cameraView.setFacing(CameraKit.Constants.FACING_FRONT);
                                    changeViewImageResource((ImageView) view, R.drawable.ic_facing_back);
                                }

                                coverView.animate()
                                        .alpha(0)
                                        .setStartDelay(200)
                                        .setDuration(300)
                                        .setListener(new AnimatorListenerAdapter() {
                                            @Override
                                            public void onAnimationEnd(Animator animation) {
                                                super.onAnimationEnd(animation);
                                                coverView.setVisibility(GONE);
                                            }
                                        })
                                        .start();
                            }
                        })
                        .start();

                break;
            }
        }
        return true;
    }

    @OnTouch(R.id.flashButton)
    boolean onTouchFlash(View view, MotionEvent motionEvent) {
        handleViewTouchFeedback(view, motionEvent);
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_UP: {
                if (cameraView.getFlash() == CameraKit.Constants.FLASH_OFF) {
                    cameraView.setFlash(CameraKit.Constants.FLASH_ON);
                    changeViewImageResource((ImageView) view, R.drawable.ic_flash_on);
                } else if(cameraView.getFlash() == CameraKit.Constants.FLASH_ON) {
                    cameraView.setFlash(CameraKit.Constants.FLASH_AUTO);
                    changeViewImageResource((ImageView) view, R.drawable.ic_flash_auto);
                } else {
                    cameraView.setFlash(CameraKit.Constants.FLASH_OFF);
                    changeViewImageResource((ImageView) view, R.drawable.ic_flash_off);
                }

                break;
            }
        }
        return true;
    }

    boolean handleViewTouchFeedback(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                touchDownAnimation(view);
                return true;
            }

            case MotionEvent.ACTION_UP: {
                touchUpAnimation(view);
                return true;
            }

            default: {
                return true;
            }
        }
    }

    void touchDownAnimation(View view) {
        view.animate()
                .scaleX(0.88f)
                .scaleY(0.88f)
                .setDuration(300)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }

    void touchUpAnimation(View view) {
        view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }

    void changeViewImageResource(final ImageView imageView, @DrawableRes final int resId) {
        imageView.setRotation(0);
        imageView.animate()
                .rotationBy(360)
                .setDuration(400)
                .setInterpolator(new OvershootInterpolator())
                .start();

        imageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                imageView.setImageResource(resId);
            }
        }, 120);
    }

}
