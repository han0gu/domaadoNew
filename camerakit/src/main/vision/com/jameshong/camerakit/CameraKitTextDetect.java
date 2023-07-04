package com.jameshong.camerakit;

public class CameraKitTextDetect extends CameraKitEvent {

    private CameraKitTextBlock textBlock;

    public CameraKitTextDetect(CameraKitTextBlock textBlock) {
        super(TYPE_TEXT_DETECTED);
        this.textBlock = textBlock;
    }

    public CameraKitTextBlock getTextBlock() {
        return textBlock;
    }
}
