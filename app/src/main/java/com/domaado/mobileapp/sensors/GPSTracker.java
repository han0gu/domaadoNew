package com.domaado.mobileapp.sensors;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;

import androidx.core.app.ActivityCompat;

import com.domaado.mobileapp.Common;
import com.domaado.mobileapp.widget.CustomAlertDialog;
import com.domaado.mobileapp.widget.myLog;

public class GPSTracker extends Service implements LocationListener {

    private final String TAG = "GPSTracker";
    private final Context mContext;

    // Flag for GPS status
    boolean isGPSEnabled = false;

    // Flag for network status
    boolean isNetworkEnabled = false;

    // Flag for GPS status
    boolean canGetLocation = false;

    Location location; // Location
    double latitude; // Latitude
    double longitude; // Longitude

    // 갱신을 위한 최소 이동거리
    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // meters

    // 최소갱신주기!
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000; //1000 * 60 * 10; // minute

    // 위 두값은 OR다! 둘중 하나의 조건이 만족할대 EVENT가 걸린다!

    public static final String LAT      = "GeoLocationLat";
    public static final String LON      = "GeoLocationLon";
    public static final String ACCURACY = "GeoLocationAccuracy";
    public static final String PROVIDER = "GeoLocationProvider";
    public static final String UDT      = "GeoLocationTime";

    // Declaring a Location Manager
    protected LocationManager locationManager;

    //    private volatile Looper mMyLooper;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private LocationListener myLocationListener;

    public static String isType = "Unknown";

    private String bestProvider;

    private GPSTrackerListener gpsTrackerListener;

    /**
     * 좌표를 리슨하지 않는...
     *
     * @param context
     * @param donotuse
     */
    public GPSTracker(Context context, String donotuse) {
        this.mContext = context;

        initGPSTracker();

        Location nowLocation = getLocation();
        if(nowLocation!=null) onLocationChanged(nowLocation);
    }

    public GPSTracker(Context context, GPSTrackerListener listener) {
        this.mContext = context;

        this.myLocationListener = this;
        this.gpsTrackerListener = listener;

        initGPSTracker();

        Location nowLocation = getLocation();
        if(nowLocation!=null) onLocationChanged(nowLocation);
    }

    public void setGpsTrackerListener(GPSTrackerListener listener) {
        this.gpsTrackerListener = listener;
    }

    public Criteria criteria;

    public void initGPSTracker() {
        locationManager = (LocationManager) mContext
                .getSystemService(LOCATION_SERVICE);

        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);

        bestProvider = locationManager.getBestProvider(criteria, true);
    }

    /**
     * 인스턴트 좌표 제공!
     *
     * @return
     */
    public Location getLocation() {
        try {

            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                //return TODO;
                return null;
            }

            if (!locationManager.isProviderEnabled(bestProvider) && locationManager.getLastKnownLocation(bestProvider) != null) {
//            	updateWithNewLocation(location);
//            	requestUpdateGPS();
            } else {
                criteria.setAccuracy(Criteria.ACCURACY_COARSE);
                bestProvider = locationManager.getBestProvider(criteria, true);
                location = locationManager.getLastKnownLocation(bestProvider);
//            	updateWithNewLocation(location);
//            	requestUpdateGPS();
            }
            //bestProvider = locationManager.getBestProvider(criteria, true);

            if(!Common.getUSIMState(mContext) && LocationManager.NETWORK_PROVIDER.equals(bestProvider)) {
                bestProvider = LocationManager.GPS_PROVIDER;
            }

            // Getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // Getting network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            myLog.d(TAG, "*** bestProvider is "+bestProvider);

            if (bestProvider.equals(LocationManager.GPS_PROVIDER)) {
                isGPSEnabled = true;
                isNetworkEnabled = false;
            } else if (bestProvider.equals(LocationManager.NETWORK_PROVIDER)) {
                isGPSEnabled = false;
                isNetworkEnabled = true;
            }

            if (!isGPSEnabled && !isNetworkEnabled) {
                // No network provider is enabled
                myLog.w(TAG, "*** getLocation is GPS and NET location service is not available!");
            } else {
                this.canGetLocation = true;

                isType = "Unknown";

                if (isNetworkEnabled) {
                    isType = "NET";
                    myLog.d(TAG, "*** getLocation type is Network");

                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                // If GPS enabled, get latitude/longitude using GPS Services
                else if (isGPSEnabled) {
                    isType = "GPS";
                    myLog.d(TAG, "*** getLocation type is GPS");

                    if (location == null) {

                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    public void requestUpdateGPS() {

        mHandler.post(new Runnable() {
            public void run() {

                if (locationManager != null) {
                    if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.

                        myLog.e(TAG, "*** GPSTracker is require permission!");

                        return;
                    }

                    try {

                        locationManager.requestLocationUpdates(bestProvider,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, myLocationListener);

                        myLog.d(TAG, "GPSTracker is start!");
                    } catch(IllegalArgumentException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private String printProvider(String provider) {

        LocationProvider info = locationManager.getProvider(provider);
        return info.toString();
    }

    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app.
     * */
    public void stopUsingGPS() {
        if (locationManager != null) {
            locationManager.removeUpdates(GPSTracker.this);

            myLog.d(TAG, "*** GPSTracker is stop!");
        }
    }

    /**
     * Function to get latitude
     * */
    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }

        // return latitude
        return latitude;
    }

    /**
     * Function to get longitude
     * */
    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }

        // return longitude
        return longitude;
    }

    /**
     * Function to check GPS/Wi-Fi enabled
     * @return boolean
     * */
    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    /**
     * 위치정보 획득 종류
     * @return
     */
    public String getType() {
        return isType;
    }

    /**
     * Function to show settings alert dialog.
     * On pressing the Settings button it will launch Settings Options.
     * */
    public void showSettingsAlert() {
        final CustomAlertDialog alertDialog = new CustomAlertDialog(mContext);

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing the Settings button.
        alertDialog.setPositiveButton("Settings", new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        // On pressing the cancel button
        alertDialog.setNegativeButton("Cancel", new View.OnClickListener() {
            public void onClick(View view) {
                alertDialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    public void onLocationChanged(Location location) {

        myLog.d(TAG, "*** onLocationChanged: Location update!");

        if(this.gpsTrackerListener!=null) this.gpsTrackerListener.onUpdateLocation(location);

        double lat = location.getLatitude();
        double lon = location.getLongitude();
        float acc = location.getAccuracy(); // 신뢰도!
        String provider = location.getProvider();
        long gettime = location.getTime();

        myLog.d(TAG, "*** onLocationChanged: lat:" + lat + ", lon:" + lon + ", acc:" + acc);
        if (mContext != null) {
            Common.saveSharedPreferences(LAT, false, String.valueOf(lat), mContext);
            Common.saveSharedPreferences(LON, false, String.valueOf(lon), mContext);

            Common.saveSharedPreferences(ACCURACY, false, String.valueOf(acc), mContext);
            Common.saveSharedPreferences(PROVIDER, false, provider, mContext);
            Common.saveSharedPreferences(UDT, false, String.valueOf(gettime), mContext);

        } else {
            myLog.d(TAG, "*** onLocationChanged: Context is null");
        }
    }

    /**
     * 마지막 GPS좌표값(lat,lng)을 빠르게 반환한다.
     *
     */
    public Location getFastLocation() {

        String provider = LocationManager.GPS_PROVIDER;
        locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //return TODO;
            return null;
        }

        Location fastLocation = locationManager.getLastKnownLocation(provider);

        if(fastLocation != null) {
    		//ret = String.format("%s,%s", fastLocation.getLatitude(), fastLocation.getLongitude());
    	} else {
            String lat = Common.getSharedPreferencesString(LAT, mContext);
            String lon = Common.getSharedPreferencesString(LON, mContext);

            fastLocation = new Location("dummpy");

            if(!TextUtils.isEmpty(lat) && !TextUtils.isEmpty(lon)) {
                try {
                    fastLocation.setLatitude(Double.parseDouble(lat));
                    fastLocation.setLongitude(Double.parseDouble(lon));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                myLog.d(TAG, "*** saved LAT, LON value is null!");

                //37°33'58.87"N 126°58'40.63"E - 서울시청!
                double latd = 37.335887;
                double lond = 126.584063;

                fastLocation.setLatitude(latd);
                fastLocation.setLongitude(lond);
            }

            //ret = String.format("%s,%s", lat, lon);
    	}

    	return fastLocation;
    }

    @Override
    public void onProviderDisabled(String provider) {
    }


    @Override
    public void onProviderEnabled(String provider) {
    }

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
		myLog.d(TAG, "*** onStatusChanged: "+provider);
		
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}