package com.domaado.mobileapp.data;

import android.graphics.Bitmap;

import com.domaado.mobileapp.Common;

import java.io.Serializable;

/**
 * Created by jameshong on 2018. 5. 30..
 */

public class PhotoEntry  implements Serializable {
    public String[] fields = { "photo_idx", "photo_name", "photo_url", "photo_data" };

    String photoIdx;
    String photoName;
    String photoUrl;
    String photoData;

    public String getPhotoIdx() {
        return photoIdx;
    }

    public void setPhotoIdx(String photoIdx) {
        this.photoIdx = photoIdx;
    }

    public String getPhotoName() {
        return photoName;
    }

    public void setPhotoName(String photoName) {
        this.photoName = photoName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getPhotoData() {
        return photoData;
    }

    public void setPhotoData(String photoData) {
        this.photoData = photoData;
    }

    public void setPhotoData(Bitmap image) {
        this.photoData = Common.getBase64encodeImage(image);
    }

    public Bitmap getPhotoBitmap() {
        Bitmap bitmap = Common.getBase64decodeImage(photoData);

        return bitmap;
    }

    public void set(String key, String value) {
        if(fields[0].equals(key)) setPhotoIdx(value);
        else if(fields[1].equals(key)) setPhotoName(value);
        else if(fields[2].equals(key)) setPhotoUrl(value);
        else if(fields[3].equals(key)) setPhotoData(value);
    }
}
