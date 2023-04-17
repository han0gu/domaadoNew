package com.domaado.market.sensors;

import android.location.Location;

public interface GPSTrackerListener {
    void onUpdateLocation(Location location);
}
