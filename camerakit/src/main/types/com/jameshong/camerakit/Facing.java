package com.jameshong.camerakit;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.jameshong.camerakit.CameraKit.Constants.FACING_BACK;
import static com.jameshong.camerakit.CameraKit.Constants.FACING_FRONT;

@IntDef({FACING_BACK, FACING_FRONT})
@Retention(RetentionPolicy.SOURCE)
public @interface Facing {
}