package com.jameshong.camerakit;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.jameshong.camerakit.CameraKit.Constants.PERMISSIONS_LAZY;
import static com.jameshong.camerakit.CameraKit.Constants.PERMISSIONS_PICTURE;
import static com.jameshong.camerakit.CameraKit.Constants.PERMISSIONS_STRICT;

@Retention(RetentionPolicy.SOURCE)
@IntDef({PERMISSIONS_STRICT, PERMISSIONS_LAZY, PERMISSIONS_PICTURE})
public @interface Permissions {
}
