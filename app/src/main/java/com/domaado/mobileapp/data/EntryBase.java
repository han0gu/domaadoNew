package com.domaado.mobileapp.data;

import android.text.TextUtils;

import com.domaado.mobileapp.network.SecureNetworkUtil;
import com.domaado.mobileapp.widget.myLog;

import java.util.ArrayList;
import java.util.Arrays;

public class EntryBase {

    private transient String TAG = EntryBase.class.getSimpleName();

    public String getNotNullString(String value) {
        if("null".equalsIgnoreCase(value) || TextUtils.isEmpty(value)) return "";
        else return value;
    }

    public String getDecString(String encString) {

        try {
            String decString = SecureNetworkUtil.getDecStringBase64(encString);
            return decString;
        } catch(Exception e) {
            e.printStackTrace();
            myLog.e(TAG, "*** getDecString Exception: "+e.getLocalizedMessage());
            return encString;
        }
    }

    public String getEncString(String string) {
        try {
            string = SecureNetworkUtil.getEncStringBase64(string);
            return string;
        } catch(Exception e) {
            e.printStackTrace();
            myLog.e(TAG, "*** getEncString Exception: "+e.getLocalizedMessage());
            return string;
        }
    }


    public String[] mixFields(String[] ... fds) {
        try {
            ArrayList<String> result = new ArrayList<>();
            for(String[] fd : fds) {
                result.addAll(Arrays.asList(fd));
            }

            return result.toArray(result.toArray(new String[0]));

//            return Arrays.asList(result).toArray(new String[result.size()]);

//            return result.toArray();
        } catch(Exception e) {
            myLog.e(TAG, "*** "+e.getLocalizedMessage());
        }

        return new String[]{};
    }
}
