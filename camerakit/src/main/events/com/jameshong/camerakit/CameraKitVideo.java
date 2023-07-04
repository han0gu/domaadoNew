package com.jameshong.camerakit;

import java.io.File;

public class CameraKitVideo extends CameraKitEvent {

    private File videoFile;

    CameraKitVideo(File videoFile) {
        super(TYPE_VIDEO_CAPTURED);
        this.videoFile = videoFile;
    }

    public File getVideoFile() {
        return videoFile;
    }

}
