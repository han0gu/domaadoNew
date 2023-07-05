package com.domaado.mobileapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Handler;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationManagerCompat;

import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.util.ArrayList;
import java.util.Set;

import com.domaado.mobileapp.widget.myLog;

/**
 * 퍼미션관리자
 *
 * Created by kbins(James Hong) on 2018,December,10
 */
public class PermissionManager {

    private String TAG = PermissionManager.class.getSimpleName();

    private Activity activity;

    private Handler resultHandler;
    private CallMethodObject callable;

    public PermissionManager(Activity activity, Handler handler) {
        this.activity = activity;
        this.resultHandler = handler;
        this.callable = null;
    }

    public PermissionManager(Activity activity, CallMethodObject callable) {
        this.activity = activity;
        this.resultHandler = null;
        this.callable = callable;
    }

    /**
     * TODO: 실행시 채크권한.
     */
    public void checkPermissionDefault() {
        String[] requestPermissions = new String[]{
//                Manifest.permission.CALL_PHONE,
//                Manifest.permission.READ_PHONE_STATE,
//                Manifest.permission.GET_ACCOUNTS,
                Manifest.permission.WRITE_EXTERNAL_STORAGE };

        String rationale = activity.getResources().getString(R.string.permission_alert_body);
        Permissions.Options options = new Permissions.Options()
                .setRationaleDialogTitle(activity.getResources().getString(R.string.permission_alert_title))
                .setSettingsDialogTitle(activity.getResources().getString(R.string.permission_storage_message));

        myLog.d(TAG, "*** checkPermissionDefault: request permissions : "+requestPermissions.length);

        Permissions.check(activity, requestPermissions, rationale, options, new PermissionHandler() {
            @Override
            public void onGranted() {
                // do your task.
                if(resultHandler!=null) resultHandler.sendEmptyMessage(0);
                else if(callable!=null) {
                    try {
                        callable.setValue(true);
                        callable.call();
                    } catch(Exception e) {}
                }
            }

            @Override
            public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                // permission denied, block the feature.
                if(resultHandler!=null) resultHandler.sendEmptyMessage(1);
                else if(callable!=null) {
                    try {
                        callable.setValue(false);
                        callable.call();
                    } catch(Exception e) {}
                }
            }
        });
    }

    public void checkPermissionAll() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            checkPermissionAll_Q();
            return;
        }

        String[] requestPermissions = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        String rationale = activity.getResources().getString(R.string.permission_alert_body);
        Permissions.Options options = new Permissions.Options()
                .setRationaleDialogTitle(activity.getResources().getString(R.string.permission_alert_title))
                .setSettingsDialogTitle(activity.getResources().getString(R.string.permission_storage_message));

        myLog.d(TAG, "*** checkPermissionDefault: request permissions : "+requestPermissions.length);

        Permissions.check(activity, requestPermissions, rationale, options, new PermissionHandler() {
            @Override
            public void onGranted() {
                // do your task.
                if(resultHandler!=null) resultHandler.sendEmptyMessage(0);
                else if(callable!=null) {
                    try {
                        callable.setValue(true);
                        callable.call();
                    } catch(Exception e) {}
                }
            }

            @Override
            public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                // permission denied, block the feature.
                if(resultHandler!=null) resultHandler.sendEmptyMessage(1);
                else if(callable!=null) {
                    try {
                        callable.setValue(false);
                        callable.call();
                    } catch(Exception e) {}
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void checkPermissionAll_Q() {
        String[] requestPermissions = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        String rationale = activity.getResources().getString(R.string.permission_alert_body);
        Permissions.Options options = new Permissions.Options()
                .setRationaleDialogTitle(activity.getResources().getString(R.string.permission_alert_title))
                .setSettingsDialogTitle(activity.getResources().getString(R.string.permission_storage_message));

        myLog.d(TAG, "*** checkPermissionDefault: request permissions : "+requestPermissions.length);

        Permissions.check(activity, requestPermissions, rationale, options, new PermissionHandler() {
            @Override
            public void onGranted() {
                // do your task.
                if(resultHandler!=null) resultHandler.sendEmptyMessage(0);
                else if(callable!=null) {
                    try {
                        callable.setValue(true);
                        callable.call();
                    } catch(Exception e) {}
                }
            }

            @Override
            public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                // permission denied, block the feature.
                if(resultHandler!=null) resultHandler.sendEmptyMessage(1);
                else if(callable!=null) {
                    try {
                        callable.setValue(false);
                        callable.call();
                    } catch(Exception e) {}
                }
            }
        });
    }

    public void checkPermissionLocation() {
        String[] requestPermissions = new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE };

        String rationale = activity.getResources().getString(R.string.permission_gps_message);
        Permissions.Options options = new Permissions.Options()
                .setRationaleDialogTitle(activity.getResources().getString(R.string.permission_alert_title))
                .setSettingsDialogTitle(activity.getResources().getString(R.string.permission_alert_body));

        myLog.d(TAG, "*** checkPermissionLocation: request permissions : "+requestPermissions.length);

        Permissions.check(activity, requestPermissions, rationale, options, new PermissionHandler() {
            @Override
            public void onGranted() {
                // do your task.
                if(resultHandler!=null) resultHandler.sendEmptyMessage(0);
                else if(callable!=null) {
                    try {
                        callable.setValue(true);
                        callable.call();
                    } catch(Exception e) {}
                }
            }

            @Override
            public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                // permission denied, block the feature.
                if(resultHandler!=null) resultHandler.sendEmptyMessage(1);
                else if(callable!=null) {
                    try {
                        callable.setValue(false);
                        callable.call();
                    } catch(Exception e) {}
                }
            }
        });
    }

    public void checkPermissionAudio() {
        String[] requestPermissions = new String[]{
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE };

        String rationale = activity.getResources().getString(R.string.permission_audio_record);
        Permissions.Options options = new Permissions.Options()
                .setRationaleDialogTitle(activity.getResources().getString(R.string.permission_alert_title))
                .setSettingsDialogTitle(activity.getResources().getString(R.string.permission_alert_body));

        myLog.d(TAG, "*** checkPermissionAudio: request permissions : "+requestPermissions.length);

        Permissions.check(activity, requestPermissions, rationale, options, new PermissionHandler() {
            @Override
            public void onGranted() {
                // do your task.
                if(resultHandler!=null) resultHandler.sendEmptyMessage(0);
                else if(callable!=null) {
                    try {
                        callable.setValue(true);
                        callable.call();
                    } catch(Exception e) {}
                }
            }

            @Override
            public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                // permission denied, block the feature.
                if(resultHandler!=null) resultHandler.sendEmptyMessage(1);
                else if(callable!=null) {
                    try {
                        callable.setValue(false);
                        callable.call();
                    } catch(Exception e) {}
                }
            }
        });
    }

    public void checkPermissionCamera() {
        String[] requestPermissions = new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE };

        String rationale = activity.getResources().getString(R.string.permission_camera_message);
        Permissions.Options options = new Permissions.Options()
                .setRationaleDialogTitle(activity.getResources().getString(R.string.permission_alert_title))
                .setSettingsDialogTitle(activity.getResources().getString(R.string.permission_alert_body));

        myLog.d(TAG, "*** checkPermissionCamera: request permissions : "+requestPermissions.length);

        Permissions.check(activity, requestPermissions, rationale, options, new PermissionHandler() {
            @Override
            public void onGranted() {
                // do your task.
                if(resultHandler!=null) resultHandler.sendEmptyMessage(0);
                else if(callable!=null) {
                    try {
                        callable.setValue(true);
                        callable.call();
                    } catch(Exception e) {}
                }
            }

            @Override
            public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                // permission denied, block the feature.
                if(resultHandler!=null) resultHandler.sendEmptyMessage(1);
                else if(callable!=null) {
                    try {
                        callable.setValue(false);
                        callable.call();
                    } catch(Exception e) {}
                }
            }
        });
    }

    public void checkPermissionCallphone() {
        String[] requestPermissions = new String[]{
                Manifest.permission.CALL_PHONE };

        String rationale = activity.getResources().getString(R.string.permission_phone_message);
        Permissions.Options options = new Permissions.Options()
                .setRationaleDialogTitle(activity.getResources().getString(R.string.permission_alert_title))
                .setSettingsDialogTitle(activity.getResources().getString(R.string.permission_alert_body));

        myLog.d(TAG, "*** checkPermissionCallphone: request permissions : "+requestPermissions.length);

        Permissions.check(activity, requestPermissions, rationale, options, new PermissionHandler() {
            @Override
            public void onGranted() {
                // do your task.
                if(resultHandler!=null) resultHandler.sendEmptyMessage(0);
                else if(callable!=null) {
                    try {
                        callable.setValue(true);
                        callable.call();
                    } catch(Exception e) {}
                }
            }

            @Override
            public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                // permission denied, block the feature.
                if(resultHandler!=null) resultHandler.sendEmptyMessage(1);
                else if(callable!=null) {
                    try {
                        callable.setValue(false);
                        callable.call();
                    } catch(Exception e) {}
                }
            }
        });
    }

    public void checkPermissionContacts() {
        String[] requestPermissions = new String[]{
                Manifest.permission.READ_CONTACTS };

        String rationale = activity.getResources().getString(R.string.permission_contacts_message);
        Permissions.Options options = new Permissions.Options()
                .setRationaleDialogTitle(activity.getResources().getString(R.string.permission_alert_title))
                .setSettingsDialogTitle(activity.getResources().getString(R.string.permission_alert_body));

        Permissions.check(activity, requestPermissions, rationale, options, new PermissionHandler() {
            @Override
            public void onGranted() {
                // do your task.
                myLog.d(TAG, "*** checkPermissionContacts: permissions onGranted");
                if(resultHandler!=null) resultHandler.sendEmptyMessage(0);
                else if(callable!=null) {
                    try {
                        callable.setValue(true);
                        callable.call();
                    } catch(Exception e) {}
                }
            }

            @Override
            public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                // permission denied, block the feature.
                myLog.d(TAG, "*** checkPermissionContacts: permissions onDenied");
                if(resultHandler!=null) resultHandler.sendEmptyMessage(1);
                else if(callable!=null) {
                    try {
                        callable.setValue(false);
                        callable.call();
                    } catch(Exception e) {}
                }
            }
        });
    }

    public void checkPermissionPhoneNumberOverAPI29() {
        String[] requestPermissions = new String[]{
                Manifest.permission.READ_PHONE_NUMBERS };

        String rationale = activity.getResources().getString(R.string.permission_phonenumber_message);
        Permissions.Options options = new Permissions.Options()
                .setRationaleDialogTitle(activity.getResources().getString(R.string.permission_alert_title))
                .setSettingsDialogTitle(activity.getResources().getString(R.string.permission_alert_body));

        myLog.d(TAG, "*** checkPermissionPhoneNumberOverAPI29: request permissions : "+requestPermissions.length);

        Permissions.check(activity, requestPermissions, rationale, options, new PermissionHandler() {
            @Override
            public void onGranted() {
                // do your task.
                if(resultHandler!=null) resultHandler.sendEmptyMessage(0);
                else if(callable!=null) {
                    try {
                        callable.setValue(true);
                        callable.call();
                    } catch(Exception e) {}
                }
            }

            @Override
            public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                // permission denied, block the feature.
                if(resultHandler!=null) resultHandler.sendEmptyMessage(1);
                else if(callable!=null) {
                    try {
                        callable.setValue(false);
                        callable.call();
                    } catch(Exception e) {}
                }
            }
        });
    }

    public void checkPermissionPhoneNumberUnderAPI29() {
        String[] requestPermissions = new String[]{
                Manifest.permission.READ_PHONE_STATE };

        String rationale = activity.getResources().getString(R.string.permission_phonenumber_message);
        Permissions.Options options = new Permissions.Options()
                .setRationaleDialogTitle(activity.getResources().getString(R.string.permission_alert_title))
                .setSettingsDialogTitle(activity.getResources().getString(R.string.permission_alert_body));

        myLog.d(TAG, "*** checkPermissionPhoneNumberUnderAPI29: request permissions : "+requestPermissions.length);

        Permissions.check(activity, requestPermissions, rationale, options, new PermissionHandler() {
            @Override
            public void onGranted() {
                // do your task.
                if(resultHandler!=null) resultHandler.sendEmptyMessage(0);
                else if(callable!=null) {
                    try {
                        callable.setValue(true);
                        callable.call();
                    } catch(Exception e) {}
                }
            }

            @Override
            public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                // permission denied, block the feature.
                if(resultHandler!=null) resultHandler.sendEmptyMessage(1);
                else if(callable!=null) {
                    try {
                        callable.setValue(false);
                        callable.call();
                    } catch(Exception e) {}
                }
            }
        });
    }

    public void checkPermissionStorage() {
        String[] requestPermissions = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE };

        String rationale = activity.getResources().getString(R.string.permission_storage_message);
        Permissions.Options options = new Permissions.Options()
                .setRationaleDialogTitle(activity.getResources().getString(R.string.permission_alert_title))
                .setSettingsDialogTitle(activity.getResources().getString(R.string.permission_alert_body));

        myLog.d(TAG, "*** checkPermissionStorage: request permissions : "+requestPermissions.length);

        Permissions.check(activity, requestPermissions, rationale, options, new PermissionHandler() {
            @Override
            public void onGranted() {
                // do your task.
                if(resultHandler!=null) resultHandler.sendEmptyMessage(0);
                else if(callable!=null) {
                    try {
                        callable.setValue(true);
                        callable.call();
                    } catch(Exception e) {}
                }
            }

            @Override
            public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                // permission denied, block the feature.
                if(resultHandler!=null) resultHandler.sendEmptyMessage(1);
                else if(callable!=null) {
                    try {
                        callable.setValue(false);
                        callable.call();
                    } catch(Exception e) {}
                }
            }
        });
    }

    public void checkPermissionSMS() {
        String[] requestPermissions = new String[]{
                Manifest.permission.READ_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.WRITE_EXTERNAL_STORAGE };

        String rationale = activity.getResources().getString(R.string.permission_incomming_message);
        Permissions.Options options = new Permissions.Options()
                .setRationaleDialogTitle(activity.getResources().getString(R.string.permission_alert_title))
                .setSettingsDialogTitle(activity.getResources().getString(R.string.permission_alert_body));

        myLog.d(TAG, "*** checkPermissionSMS: request permissions : "+requestPermissions.length);

        Permissions.check(activity, requestPermissions, rationale, options, new PermissionHandler() {
            @Override
            public void onGranted() {
                // do your task.
                if(resultHandler!=null) resultHandler.sendEmptyMessage(0);
                else if(callable!=null) {
                    try {
                        callable.setValue(true);
                        callable.call();
                    } catch(Exception e) {}
                }
            }

            @Override
            public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                // permission denied, block the feature.
                if(resultHandler!=null) resultHandler.sendEmptyMessage(1);
                else if(callable!=null) {
                    try {
                        callable.setValue(false);
                        callable.call();
                    } catch(Exception e) {}
                }
            }
        });
    }

    public boolean checkPermissionNotification() {
        Set<String> notiListenerSet = NotificationManagerCompat.getEnabledListenerPackages(activity);
        String myPackageName = activity.getPackageName();

        for(String packageName : notiListenerSet) {
            if(packageName == null) {
                continue;
            }
            if(packageName.equals(myPackageName)) {
                return true;
            }
        }

        return false;
    }
}
