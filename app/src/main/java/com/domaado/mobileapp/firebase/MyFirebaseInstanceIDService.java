package com.domaado.mobileapp.firebase;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.domaado.mobileapp.widget.myLog;

/**
 * Created by jameshong on 2018. 6. 19..
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private static final String TAG = MyFirebaseInstanceIDService.class.getSimpleName();

    // 토큰 재생성
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String token = FirebaseInstanceId.getInstance().getToken();
        myLog.d(TAG, "*** FCM token = " + token);

        //Common.saveSharedPreferencesString("fcm_token", token, this);
    }
}
