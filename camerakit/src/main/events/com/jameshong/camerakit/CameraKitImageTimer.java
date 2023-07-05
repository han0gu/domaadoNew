package com.jameshong.camerakit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class CameraKitImageTimer extends CameraKitEvent {

    private byte[] jpeg;
    private int countDown;

    CameraKitImageTimer(byte[] jpeg) {
        super(TYPE_IMAGE_CAPTURED);
        this.jpeg = jpeg;
    }

    public byte[] getJpeg() {
        return jpeg;
    }

    public Bitmap getBitmap() {
        return BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length);
    }

    public int getCountDown() {
        return countDown;
    }

    public void setCountDown(int countDown) {
        this.countDown = countDown;
    }
}
