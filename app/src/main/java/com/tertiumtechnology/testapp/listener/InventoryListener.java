package com.tertiumtechnology.testapp.listener;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.tertiumtechnology.api.rfidpassiveapilib.Tag;
import com.tertiumtechnology.api.rfidpassiveapilib.listener.AbstractInventoryListener;
import com.tertiumtechnology.api.rfidpassiveapilib.listener.AbstractReaderListener;
import com.tertiumtechnology.testapp.BleServicePassive;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.HashMap;


public class InventoryListener extends AbstractInventoryListener {
    private static final String TAG = InventoryListener.class.getSimpleName();

    private final WeakReference<Context> weakReferenceContext;

    public InventoryListener(Context service) {
        this.weakReferenceContext = new WeakReference<>(service);
    }

    @Override
    public void inventoryEvent(Tag tag) {
        Log.d(TAG, "Discovered tag ID = " + tag);

        HashMap<Object, Object> commandValueMap = new HashMap<>();
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_CALLBACK, AbstractReaderListener
                .INVENTORY_COMMAND);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_INVENTORY_TAG, tag);

        sendCommandCallback(commandValueMap);
    }

    private void sendCommandCallback(Serializable extraData) {
        Intent intent = new Intent(BleServicePassive.INTENT_ACTION_DEVICE_COMMAND_CALLBACK);

        if (extraData != null) {
            intent.putExtra(BleServicePassive.INTENT_EXTRA_DATA_VALUE, extraData);
        }

        sendIntent(intent);
    }

    private void sendIntent(Intent intent) {
        if (weakReferenceContext.get() != null) {
            LocalBroadcastManager.getInstance(weakReferenceContext.get()).sendBroadcast(intent);
        }
    }
}