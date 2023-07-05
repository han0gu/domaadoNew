package com.domaado.mobileapp.firebase;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.text.TextUtils;

import com.domaado.mobileapp.App;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.domaado.mobileapp.Common;
import com.domaado.mobileapp.Constant;
import com.domaado.mobileapp.R;
import com.domaado.mobileapp.SplashScreen;
import com.domaado.mobileapp.data.QueryParams;
import com.domaado.mobileapp.submenus.WebContentActivity;
import com.domaado.mobileapp.widget.myLog;

import java.util.Map;

/**
 * Created by jameshong on 2018. 6. 19..
 *
 * 중요: FCM의 수신은 기본적으로 OS에서 처리한다. 하지만 앱이 동작중인 경우 본 클래스를 탄다.
 * OS에서 처리되는 FCM데이터 영역 "data" 부분의 값은 Intent로 그대로 앱의 LAUNCH 카테고리를 소유한 액티비티에 전달된다.
 *
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();
    private int NOTIFICATION_ID = 542701;

    public static final String FCM_DATA_DATA        = "data";
    public static final String FCM_DATA_RESPONSE_ID  = "response_id";
    public static final String FCM_DATA_TITLE        = "title";
    public static final String FCM_DATA_ACTION       = "action";
    public static final String FCM_DATA_MESSAGE      = "body";

    public static final String FCM_DATA_LAT          = "lat";
    public static final String FCM_DATA_LON          = "lon";
    public static final String FCM_DATA_URL          = "url";

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);

        myLog.d(TAG, "*** onNewToken - FCM token = " + s);
    }

    // 메시지 수신
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        myLog.i(TAG, "*** onMessageReceived");

        Map<String, String> data = remoteMessage.getData();

        String mdata = data.get(FCM_DATA_DATA);

        String responseId = data.get(FCM_DATA_RESPONSE_ID);
        String action = data.get(FCM_DATA_ACTION);

        String title = data.get(FCM_DATA_TITLE);
        String message = data.get(FCM_DATA_MESSAGE);

        if(TextUtils.isEmpty(title)) title = remoteMessage.getNotification()!=null ? remoteMessage.getNotification().getTitle() : getResources().getString(R.string.app_name);
        if(TextUtils.isEmpty(message)) message = remoteMessage.getNotification()!=null ? remoteMessage.getNotification().getBody() : getResources().getString(R.string.app_name);

        String lat = data.get(FCM_DATA_LAT);
        String lon = data.get(FCM_DATA_LON);
        String url = data.get(FCM_DATA_URL);

        myLog.d(TAG, "*** ********************************");
        for (Map.Entry<?, ?> entry : data.entrySet()) {
            /*
             * Deviate from the original by checking that keys are non-null and
             * of the proper type. (We still defer validating the values).
             */
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();

            myLog.d(TAG, "*** "+key+": "+value);
        }
        myLog.d(TAG, "*** ********************************");

        // 이 메소드는 사실상 앱이 동작중일 때만 호출 되기 때문에 else를 탈 일이 없다.......개 삽질..
        String enableNotification = "1"; //Common.getSharedPreferencesString(SetupActivity.SETUP_STOP_NOTIFICATION, this);

        QueryParams queryParams = new QueryParams(title, message, mdata, action, lat, lon, url);
        App.setQueryParam(queryParams);

        if (Common.isRunning(this)) {

            if(Constant.PUSH_ACTION_REFRESH_LIST.equalsIgnoreCase(action)) {
                actionBroadcast(Constant.REFRESH_FILTER, queryParams);
            } else if(Constant.PUSH_ACTION_OPEN_URL.equalsIgnoreCase(action)) {
                actionBroadcast(Constant.OPENURL_FILTER, queryParams);
            } else
                actionBroadcast(WebContentActivity.ACTION_FILTER, queryParams);
        } else {
            if(TextUtils.isEmpty(enableNotification)) createAndShowForegroundNotification(this, NOTIFICATION_ID, title, message, responseId, action, url);
        }

    }


    private int lastShownNotificationId;

    private void createAndShowForegroundNotification(Service yourService, int notificationId, String title, String message, String responseId, String action, String url) {

        Intent i = new Intent(getApplicationContext(), SplashScreen.class);
        i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        i.putExtra(FCM_DATA_TITLE, title); //mComposing.toString());	// Method URL을 통해 적립금 적립.
        i.putExtra(FCM_DATA_RESPONSE_ID, responseId);
        i.putExtra(FCM_DATA_ACTION, action);
        i.putExtra(FCM_DATA_URL, url);

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
                "com.google.firebase.MESSAGING_EVENT", // Channel id
                message,
                NotificationManagerCompat.IMPORTANCE_LOW); //Low importance prevent visual appearance for this notification channel on top

        builder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.push_icon))
                .setContentIntent(mPendingIntent)
                .setContentTitle(!TextUtils.isEmpty(title) ? title : yourService.getString(R.string.app_name))
                .setContentText(message);


        Notification notification = builder.build();

        yourService.startForeground(notificationId, notification);

        if (notificationId != lastShownNotificationId) {
            // Cancel previous notification
            final NotificationManager nm = (NotificationManager) yourService.getSystemService(Activity.NOTIFICATION_SERVICE);
            nm.cancel(lastShownNotificationId);
        }

        lastShownNotificationId = notificationId;
    }

    public static NotificationCompat.Builder getNotificationBuilder(Context context, String channelId, String message, int importance) {
        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            prepareChannel(context, channelId, message, importance);
            builder = new NotificationCompat.Builder(context, channelId);
        } else {
            builder = new NotificationCompat.Builder(context);
        }
        return builder;
    }

    @TargetApi(26)
    private static void prepareChannel(Context context, String id, String description, int importance) {
        final String appName = context.getString(R.string.app_name);

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

//    /**
//     * queryParams 값은 이미 이전에 Application 컨텍스에 저장했다.
//     *
//     * @param queryParams
//     * @return
//     */
//    private Intent callActivityView(QueryParams queryParams) {
//        myLog.d(TAG, "*** callActivityView: "+queryParams.toString());
//
//        Intent i = new Intent(getApplicationContext(), CallListActivity.class);
//        //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//
//        return i;
//    }

    private void actionBroadcast(String action, QueryParams queryParams) {
        myLog.d(TAG, "*** actionBroadcast: "+action);

        Intent intent = new Intent(action);
        intent.putExtra("queryParams", queryParams);

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.sendBroadcast(intent);
    }

    public static void setTopicSubscribe(Context context, String topic) {
        String key = "FCM_TOPIC";

        final String lastTopic = Common.getSharedPreferencesString(key, context);
        if(!TextUtils.isEmpty(lastTopic))  {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(lastTopic).addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    if(task.getException()!=null)
                        myLog.e(TAG, "*** unsubscribeFromTopic unsuccessful: "+ task.getException().getLocalizedMessage());
                    else
                        myLog.e(TAG, "*** unsubscribeFromTopic ERROR : "+ lastTopic);
                } else {
                    myLog.d(TAG, "*** unsubscribeFromTopic successful: "+ lastTopic);
                }
            });
        }

        if(!TextUtils.isEmpty(topic)) {

            Common.saveSharedPreferencesString(key, topic, context);

            FirebaseMessaging.getInstance().subscribeToTopic(topic)
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            if (task.getException() != null)
                                myLog.e(TAG, "*** setTopicSubscribe unsuccessful: " + task.getException().getLocalizedMessage());
                            else
                                myLog.e(TAG, "*** setTopicSubscribe ERROR : " + topic);
                        } else {
                            myLog.d(TAG, "*** setTopicSubscribe successful: " + topic);
                        }

//                        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
                    });
        } else {
            Common.removeSharedPreferences(key, context);

        }
    }
}
