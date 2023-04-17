package com.domaado.market.sensors;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

import com.domaado.market.widget.myLog;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static android.content.Context.BLUETOOTH_SERVICE;

/**
 * Created by jameshong on 2018. 1. 17..
 *
 * Bluetooth Tethering
 */

public class BluetoothTetheringHelper {
    private String TAG = "BluetoothHelper";

    BluetoothAdapter mBluetoothAdapter = null;
    Class<?> classBluetoothPan = null;
    Constructor<?> BTPanCtor = null;
    Object BTSrvInstance = null;
    Class<?> noparams[] = {};
    Method mIsBTTetheringOn;

    Context mContext;

    public BluetoothTetheringHelper(Context ctx) {
        mContext = ctx;
        mBluetoothAdapter = getBTAdapter();
        try {
            classBluetoothPan = Class.forName("android.bluetooth.BluetoothPan");
            mIsBTTetheringOn = classBluetoothPan.getDeclaredMethod("isTetheringOn", noparams);
            BTPanCtor = classBluetoothPan.getDeclaredConstructor(Context.class, BluetoothProfile.ServiceListener.class);
            BTPanCtor.setAccessible(true);
            BTSrvInstance = BTPanCtor.newInstance(mContext, new BTPanServiceListener(mContext));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BluetoothAdapter getBTAdapter() {
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1)
            return BluetoothAdapter.getDefaultAdapter();
        else {
            BluetoothManager bm = (BluetoothManager) mContext.getSystemService(BLUETOOTH_SERVICE);
            return bm.getAdapter();
        }
    }

    // Check whether Bluetooth tethering is enabled.
    public boolean IsBluetoothTetherEnabled() {
        try {
            if(mBluetoothAdapter != null) {
                return (Boolean) mIsBTTetheringOn.invoke(BTSrvInstance, (Object[]) noparams);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public class BTPanServiceListener implements BluetoothProfile.ServiceListener {
        private final Context context;

        public BTPanServiceListener(final Context context) {
            this.context = context;
        }

        @Override
        public void onServiceConnected(final int profile,
                                       final BluetoothProfile proxy) {
            //Some code must be here or the compiler will optimize away this callback.
            myLog.i(TAG, "BTPan proxy connected");
        }

        @Override
        public void onServiceDisconnected(final int profile) {
        }
    }
}
