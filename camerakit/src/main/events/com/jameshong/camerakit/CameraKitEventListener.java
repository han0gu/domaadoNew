package com.jameshong.camerakit;

public interface CameraKitEventListener {
    void onEvent(CameraKitEvent event);
    void onError(CameraKitError error);
    void onImage(CameraKitImage image);
    void onVideo(CameraKitVideo video);
    void onImageTimer(CameraKitImageTimer image);
}