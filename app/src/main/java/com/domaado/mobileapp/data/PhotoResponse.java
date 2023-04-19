package com.domaado.mobileapp.data;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by jameshong on 2018. 5. 30..
 */

public class PhotoResponse extends ResponseBase  implements Serializable {

    ArrayList<PhotoEntry> photos;

    public PhotoResponse() {
        photos = new ArrayList<>();
    }

    public PhotoResponse(String _requestId, String _responseYn, ArrayList<PhotoEntry> _photos) {
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
