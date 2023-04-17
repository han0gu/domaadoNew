package com.domaado.market.data;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by jameshong on 2018. 5. 30..
 */

public class CarPhotoResponse extends ResponseBase  implements Serializable {

    public String[] fields = {};

    ArrayList<PhotoEntry> photos;

    public CarPhotoResponse() {
        photos = new ArrayList<>();
    }

    public CarPhotoResponse(String _requestId, String _responseYn, ArrayList<PhotoEntry> _photos) {
        this.requestId = _requestId;
        this.responseYn = _responseYn;

        this.photos.clear();
        this.photos.addAll(_photos);
    }

    public ArrayList<PhotoEntry> getPhotos() {
        return photos;
    }

    public void setPhotos(ArrayList<PhotoEntry> photos) {
        this.photos.clear();
        this.photos.addAll(photos);
    }

    public void addPhoto(PhotoEntry photo) {
        if(this.photos == null) this.photos = new ArrayList<>();
        this.photos.add(photo);
    }

    public void set(String key, String value) {

    }

    @Override
    public String toString() {
        return "PhotoResponse{" +
                "photos=" + photos +
                ", seq=" + seq +
                ", requestId='" + requestId + '\'' +
                ", responseYn='" + responseYn + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
