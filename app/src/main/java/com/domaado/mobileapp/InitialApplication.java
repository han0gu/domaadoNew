package com.domaado.mobileapp;

import android.app.Activity;
import android.os.Handler;
import android.text.TextUtils;

import com.google.firebase.messaging.FirebaseMessaging;

import java.util.UUID;

import com.domaado.mobileapp.data.CheckUpdateRequest;
import com.domaado.mobileapp.data.CheckUpdateResponse;
import com.domaado.mobileapp.network.UrlManager;
import com.domaado.mobileapp.task.CheckUpdateTask;
import com.domaado.mobileapp.task.GoogleAppIdTask;
import com.domaado.mobileapp.widget.myLog;

/**
 * Created by kbins(James Hong) on 2021,March,30
 */
public class InitialApplication {

    private String TAG = InitialApplication.class.getSimpleName();

    private Activity activity;

    private CheckUpdateTask checkUpdateTask;

    public final static int NORMAL_USER_CONDITION       = 0;
    public final static int NEED_LOGIN_CONDITION        = 1;
    public final static int NEW_USER_CONDITION          = 2;
    public final static int ERROR_RESPONSE_CONDITION    = 3;

    public interface InitialListener {

        void responseSuccess(CheckUpdateResponse response);
        void responseFailure(CheckUpdateResponse response);
        void responseTimeout(CheckUpdateResponse response);

        default void onUpdated(String deviceId, String fcmToken) {}
        default void onError(String message, String deviceId) {}
    }

    public InitialListener initialListener;

    public InitialApplication(Activity activity, InitialListener initialListener) {
        this.activity = activity;
        this.initialListener = initialListener;
    }

    public void initGoogleAppId() {
        GoogleAppIdTask googleAppIdTask = new GoogleAppIdTask(activity, new GoogleAppIdTask.TaskResultListener() {
            @Override
            public void onResulted(String adid) {
                myLog.d(TAG, "*** ADID-UUID: "+adid);

                if(!TextUtils.isEmpty(adid) && adid.startsWith("00000000")) {
                    /**
                     * IMPORTANT!
                     * 구글이 ADID를 삭제할 수 있도록 사용자에게 권한을 오픈했다
                     * 이로 인하여 ADID가 0으로 채워진 형태로 들어온다.
                     * 따라서 DEVICE ID를 임의의 값으로 생성하여 활용한다.
                     * 이렇게 하면 앱을 삭제했다 다시 설치하거나 앱 데이터를 삭제하는 경우 미가입 형태로 시작하게 되고 본인인증을 한 후에 정상적으로 사용이 가능하다.
                     */

                    String deviceId = Common.getDefaultUUID(activity, UUID.randomUUID().toString());

                    myLog.e(TAG, "*** initGoogleAppId IMPORTANT adid ("+adid+") to ("+deviceId+")");

                    adid = deviceId;

                }

                final String fadid = adid;

                FirebaseMessaging.getInstance().getToken().addOnSuccessListener(token -> {
                    initialListener.onUpdated(fadid, token);
                }).addOnFailureListener(e -> {
                    myLog.e(TAG, "*** initGoogleAppId getFcmToken ERROR: "+e.getLocalizedMessage());
                    initialListener.onError(e.getLocalizedMessage(), fadid);
                }).addOnCanceledListener(() -> {
                    myLog.e(TAG, "*** initGoogleAppId getFcmToken CANCEL! ");
                    initialListener.onError("CANCEL", fadid);
                }).addOnCompleteListener(task -> {
                    myLog.d(TAG, "*** initGoogleAppId GoogleAppIdTask Complete!");
                });

            }

            @Override
            public void onError(String message) {

                myLog.e(TAG, "*** ADID-UUID - GoogleAppIdTask: "+message);

                initialListener.onError(message, null);

//                Common.alertMessage(activity,
//                        activity.getResources().getString(R.string.app_name),
//                        activity.getResources().getString(R.string.uuid_get_error_message),
//                        activity.getResources().getString(R.string.btn_ok),
//                        new Handler(msg -> {
//                            activity.finishAffinity();
//                            return true;
//                        }));

            }
        });

        googleAppIdTask.executeSync();
    }

    public void checkAppUpdate(CheckUpdateRequest request) {

        App.setAccessToken("");

        checkUpdateTask = new CheckUpdateTask(activity, request, false, new Handler(msg -> {

            CheckUpdateResponse response = (CheckUpdateResponse) msg.obj;

            switch (msg.what) {
                case Constant.RESPONSE_SUCCESS: {
                    setDefaultValues(response);
                    initialListener.responseSuccess(response);
                    break;
                }

                case Constant.RESPONSE_FAILURE: {
                    initialListener.responseFailure(response);
                    break;
                }

                case Constant.RESPONSE_TIMEOUT: {
                    initialListener.responseTimeout(response);
                    break;
                }

            }
            return true;
        }));
        checkUpdateTask.execute(Constant.API_URL[myLog.debugMode?1:0], UrlManager.getCheckUpdateAPI(activity));

    }

    private void setDefaultValues(CheckUpdateResponse response) {

        Common.removeSharedPreferences(Constant.REQUIRE_JOIN, activity);
        Common.removeSharedPreferences(Constant.CONFIG_HAVEUPDATE, activity);

        if(response!=null) {
            App.setAccessToken(response.getAccessToken());
            App.setMemberEntry(response.getMemberEntry());

//            App.setKeyByte(response.getKey().getBytes(StandardCharsets.UTF_8));
//            App.setIvByte(response.getIv().getBytes(StandardCharsets.UTF_8));

            App.setKeyByte(response.getKeyBytes());
            App.setIvByte(response.getIvBytes());

//            myLog.e(TAG, "*** encrypt key: "+new String(response.getKeyBytes()));
//            myLog.e(TAG, "*** encrypt iv: "+new String(response.getIvBytes()));
        }
    }
}
