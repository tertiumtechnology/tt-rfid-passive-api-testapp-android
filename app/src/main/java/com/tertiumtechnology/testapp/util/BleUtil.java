package com.tertiumtechnology.testapp.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;

public class BleUtil {

    public static BluetoothAdapter getBtAdapter(Context context) {
        BluetoothAdapter adapter = null;

        BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (manager != null) {
            adapter = manager.getAdapter();
        }

        return adapter;
    }

    public static boolean isBleSupported(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    public static boolean isBluetoothEnabled(Context context) {
        BluetoothAdapter adapter = getBtAdapter(context);
        return adapter != null && adapter.isEnabled();
    }
}