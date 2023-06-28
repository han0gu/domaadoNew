package com.domaado.mobileapp.service;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.skt.Tmap.TMapGpsManager;
import com.domaado.mobileapp.Constant;

import com.domaado.mobileapp.R;
import com.domaado.mobileapp.data.ReportLocationRequest;
import com.domaado.mobileapp.data.ReportLocationResponse;
import com.domaado.mobileapp.submenus.WebContentActivity;
import com.domaado.mobileapp.task.ReportLocationTask;
import com.domaado.mobileapp.widget.myLog;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 기사위치(위도,경도)를 서버에 주기적으로 보고한다.
 * 이 부분은 추후 보안이슈가 관계될 수도 있다.
 *
 * - MapMainActivity 가 떠 있을 때만 동작함.
 */
public class ReportLocationService extends Service implements TMapGpsManager.onLocationChangedCallback {

    private IBinder mBinder = new LocalBinder();

    private String TAG = ReportLocationService.class.getSimpleName();

    public static final String DELAY_SECONDS_KEY    = "delaySeconds";
    public static final String TITLE_KEY            = "title";
    public static final String RESPONSE_ID_KEY      = "response_id";
    public static final String ACTION_KEY           = "action";

    public ReportLocationRequest reportLocationRequest;
    public boolean driverLocationReport = true;

    public Timer timer;
    public TMapGpsManager gps = null;
    public boolean isDetailedPosition = false;

    public boolean onLineLocation = false;
    public Location currentLocation;

    public void init(Context ctx) {
        reportLocationRequest = new ReportLocationRequest(ctx);
        if(timer==null) timer = new Timer();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        myLog.d(TAG, "*** onBind");

        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        myLog.d(TAG, "*** onCreate");

        initPosition();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        init(getApplicationContext());

        int delaySeconds = intent.getExtras().getInt(DELAY_SECONDS_KEY, 60);
        String title = intent.getExtras().getString(TITLE_KEY);
        String responseId = intent.getExtras().getString(RESPONSE_ID_KEY);
        String action = intent.getExtras().getString(ACTION_KEY);

        driverLocationReport = true;

        myLog.d(TAG, "*** updateDriverLocation: onStartCommand - "+delaySeconds+" seconds!");

        //showNotification();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateDriverLocation();
            }
        }, 0, delaySeconds * 1000);

        createAndShowForegroundNotification(this, 542702, title, responseId, action);

        return START_STICKY;

    }

    private int lastShownNotificationId;

    private void createAndShowForegroundNotification(Service yourService, int notificationId, String title, String responseId, String action) {

        Intent i = new Intent(getApplicationContext(), WebContentActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        i.putExtra("title", title); //mComposing.toString());	// Method URL을 통해 적립금 적립.
        i.putExtra("response_id", responseId);
        i.putExtra("action", action);

        PendingIntent mPendingIntent;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            mPendingIntent = PendingIntent.getActivity(
                    this, 1, i, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
            );
        } else {
            mPendingIntent = PendingIntent.getActivity(
                    this, 1, i, PendingIntent.FLAG_UPDATE_CURRENT
            );
        }

        final NotificationCompat.Builder builder = getNotificationBuilder(yourService,
                "com.domaado.mobileapp.REPORT_USER_LOCATION", // Channel id
                NotificationManagerCompat.IMPORTANCE_LOW); //Low importance prevent visual appearance for this notification channel on top

        builder.setOngoing(true)
                .setSmallIcon(R.drawable.push_icon)
                .setContentIntent(mPendingIntent)
                .setContentTitle(yourService.getString(R.string.app_name))
                .setContentText(yourService.getString(R.string.driver_location_reporting));


        Notification notification = builder.build();

        yourService.startForeground(notificationId, notification);

        if (notificationId != lastShownNotificationId) {
            // Cancel previous notification
            final NotificationManager nm = (NotificationManager) yourService.getSystemService(Activity.NOTIFICATION_SERVICE);
            nm.cancel(lastShownNotificationId);
        }

        lastShownNotificationId = notificationId;
    }

    public static NotificationCompat.Builder getNotificationBuilder(Context context, String channelId, int importance) {
        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            prepareChannel(context, channelId, importance);
            builder = new NotificationCompat.Builder(context, channelId);
        } else {
            builder = new NotificationCompat.Builder(context);
        }
        return builder;
    }

    @TargetApi(26)
    private static void prepareChannel(Context context, String id, int importance) {
        final String appName = context.getString(R.string.app_name);
        String description = context.getString(R.string.driver_location_reporting);
        final NotificationManager nm = (NotificationManager) context.getSystemService(Activity.NOTIFICATION_SERVICE);

        if(nm != null) {
            NotificationChannel nChannel = nm.getNotificationChannel(id);

            if (nChannel == null) {
                nChannel = new NotificationChannel(id, appName, importance);
                nChannel.setDescription(description);
                nm.createNotificationChannel(nChannel);
            }
        }
    }

    public void updateDriverLocation() {

        if(!this.onLineLocation) {
            myLog.d(TAG, "*** updateDriverLocation: driver location is not ready!");
            return;
        }

        if(driverLocationReport) {
            myLog.d(TAG, "*** updateDriverLocation: report start!");

            if(reportLocationRequest!=null && currentLocation!=null) {
                reportLocationRequest.setDriverLat(currentLocation.getLatitude());
                reportLocationRequest.setDriverLon(currentLocation.getLongitude());
            }

            ReportLocationTask reportLocationTask = new ReportLocationTask(getApplicationContext(), reportLocationRequest, new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);

                    ReportLocationResponse response = (ReportLocationResponse) msg.obj;

                    switch (msg.what) {
                        case Constant.RESPONSE_SUCCESS:
                            break;
                        case Constant.RESPONSE_FAILURE:
                        case Constant.RESPONSE_TIMEOUT: {
                            String message = response != null ? response.getMessage() : getResources().getString(R.string.driver_delevery_report_fail);
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }
                }
            });
            reportLocationTask.execute(getResources().getString(R.string.url_api_site), getResources().getString(R.string.url_update_location));
        } else {
            myLog.d(TAG, "*** updateDriverLocation: report finish!");
            timer.cancel();
        }
    }

    @Override
    public void onDestroy() {

        myLog.d(TAG, "*** onDestroy");

        gps.CloseGps();

        if(timer!=null) {
            timer.cancel();
        }

        driverLocationReport = false;

        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        myLog.d(TAG, "*** onUnbind");

        driverLocationReport = false;

        if(timer!=null) {
            timer.cancel();
        }

        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);

        myLog.d(TAG, "*** onRebind");

    }

    @Override
    public void onLocationChange(Location location) {
        myLog.d(TAG, "*** onLocationChange: "+ location.toString());

        this.onLineLocation = true;

        this.currentLocation = location;

        if (!isDetailedPosition) setDetailPosition();

    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        myLog.d(TAG, "*** onTaskRemoved");
    }

    public class LocalBinder extends Binder {

        public ReportLocationService getService() {
            return ReportLocationService.this;
        }

        public void stopReport() {
            driverLocationReport = false;
        }

    }

    private void initPosition() {
        myLog.d(TAG, "*** initPosition!");

        gps = new TMapGpsManager(this);
        gps.setMinTime(1000);
        gps.setMinDistance(5);
        gps.setProvider(gps.NETWORK_PROVIDER);
        //gps.setProvider(gps.GPS_PROVIDER);
        gps.OpenGps();
    }

    private void setDetailPosition() {
        gps.CloseGps();
        gps.setProvider(gps.GPS_PROVIDER);
        gps.OpenGps();

        isDetailedPosition = true;
    }

}
