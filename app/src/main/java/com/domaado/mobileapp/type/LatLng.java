package com.domaado.mobileapp.type;

import com.skt.Tmap.TMapPoint;

/**
 * Created by jameshong on 2018. 5. 29..
 */

public class LatLng extends TMapPoint {
    public static double latitude;
    public static double longitude;

    public LatLng(double latitude, double longitude) {
        super(latitude, longitude);

        this.latitude = latitude;
        this.longitude = longitude;
    }
}
