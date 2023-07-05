package com.domaado.mobileapp.task;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import java.io.IOException;

import com.domaado.mobileapp.widget.myLog;

/**
 * Created by kbins(James Hong) on 2020,December,10
 */
public class GoogleAppIdTask extends AsyncTask<Void, Void, GoogleAppIdTask.GoogleAppIdData> {

    private String TAG = GoogleAppIdTask.class.getSimpleName();

    protected Context mContext = null;
    protected TaskResultListener taskResultListener;

    public interface TaskResultListener {
        void onResulted(String adid);
        void onError(String message);
    }

    public GoogleAppIdTask(Context context, TaskResultListener listener) {
        mContext = context;
        taskResultListener = listener;
    }

    protected GoogleAppIdData doInBackground(final Void... params) {
        GoogleAppIdData googleAppIdData = new GoogleAppIdData();

        try {
            googleAppIdData.adid = AdvertisingIdClient.getAdvertisingIdInfo(mContext).getId();
            myLog.d(TAG, "adid : " + googleAppIdData.adid);

        } catch (IllegalStateException ex) {
            myLog.d(TAG, "IllegalStateException" + ex);
            googleAppIdData.errorMessage = ex.getLocalizedMessage();
        } catch (GooglePlayServicesRepairableException ex) {
            myLog.d(TAG, "GooglePlayServicesRepairableException" + ex);
            googleAppIdData.errorMessage = ex.getLocalizedMessage();
        } catch (IOException ex) {
            myLog.d(TAG, "IOException" + ex);
            googleAppIdData.errorMessage = ex.getLocalizedMessage();
        } catch (GooglePlayServicesNotAvailableException ex) {
            myLog.d(TAG, "GooglePlayServicesNotAvailableException" + ex);
            googleAppIdData.errorMessage = ex.getLocalizedMessage();
        } catch (Exception ex) {
            myLog.d(TAG, "Exception" + ex);
            googleAppIdData.errorMessage = ex.getLocalizedMessage();
        }

        return googleAppIdData;
    }

    protected void onPostExecute(GoogleAppIdData googleAppIdData) {
        super.onPostExecute(googleAppIdData);

        if(taskResultListener!=null) {
            if(!TextUtils.isEmpty(googleAppIdData.errorMessage)) taskResultListener.onError(googleAppIdData.errorMessage);
            else taskResultListener.onResulted(googleAppIdData.adid);
        }
    }

    public void executeSync() {
        // execute
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.HONEYCOMB) {
            this.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            this.execute();
        }
    }

    class GoogleAppIdData {
        String adid;
        String errorMessage;
    }
}
