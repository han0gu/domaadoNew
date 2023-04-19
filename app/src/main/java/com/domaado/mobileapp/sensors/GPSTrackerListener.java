package com.domaado.mobileapp.sensors;

import android.location.Location;

public interface GPSTrackerListener {
    void onUpdateLocation(Location location);
}
