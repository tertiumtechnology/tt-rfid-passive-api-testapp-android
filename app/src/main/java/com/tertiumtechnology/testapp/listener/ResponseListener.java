package com.tertiumtechnology.testapp.listener;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.tertiumtechnology.api.rfidpassiveapilib.listener.AbstractResponseListener;
import com.tertiumtechnology.testapp.BleServicePassive;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.HashMap;

public class ResponseListener extends AbstractResponseListener {
    private static final String TAG = ResponseListener.class.getSimpleName();

    private final WeakReference<Context> weakReferenceContext;

    public ResponseListener(Context service) {
        this.weakReferenceContext = new WeakReference<>(service);
    }

    @Override
    public void killEvent(byte[] tag_ID, int error) {
        HashMap<Object, Object> commandValueMap = initCommandValueMap(AbstractResponseListener
                .KILL_COMMAND);

        if (error != AbstractResponseListener.NO_ERROR) {
            logResponse("Tag #" + extractStringFromByteArray(tag_ID) + " kill error: " + error);
            commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_ERROR, error);
        }
        else {
            logResponse("Tag #" + extractStringFromByteArray(tag_ID) + " killed");
        }

        sendCommandCallbackResult(commandValueMap);
    }

    @Override
    public void lockEvent(byte[] tag_ID, int error) {
        HashMap<Object, Object> commandValueMap = initCommandValueMap(AbstractResponseListener
                .LOCK_COMMAND);

        if (error != AbstractResponseListener.NO_ERROR) {
            logResponse("Tag #" + extractStringFromByteArray(tag_ID) + " lock error: " + error);
            commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_ERROR, error);
        }
        else {
            logResponse("Tag #" + extractStringFromByteArray(tag_ID) + " locked");
        }

        sendCommandCallbackResult(commandValueMap);
    }

    @Override
    public void readEvent(byte[] tag_ID, int error, byte data[]) {
        HashMap<Object, Object> commandValueMap = initCommandValueMap(AbstractResponseListener
                .READ_COMMAND);

        if (error != AbstractResponseListener.NO_ERROR) {
            logResponse("Tag #" + extractStringFromByteArray(tag_ID) + " read error: " + error);
            commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_ERROR, error);
        }
        else {
            logResponse("Tag #" + extractStringFromByteArray(tag_ID) + " read data: " + extractStringFromByteArray
                    (data));
            commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_READ_VALUE, extractStringFromByteArray(data));
        }

        sendCommandCallbackResult(commandValueMap);
    }

    @Override
    public void readTIDevent(byte[] tag_ID, int error, byte data[]) {
        HashMap<Object, Object> commandValueMap = initCommandValueMap(AbstractResponseListener
                .READ_TID_COMMAND);

        if (error != AbstractResponseListener.NO_ERROR) {
            logResponse("Tag #" + extractStringFromByteArray(tag_ID) + " TID read error: " + error);
            commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_ERROR, error);
        }
        else {
            logResponse("Tag #" + extractStringFromByteArray(tag_ID) + " TID: " + extractStringFromByteArray(data));
            commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_READ_TID_VALUE, extractStringFromByteArray(data));
        }

        sendCommandCallbackResult(commandValueMap);
    }

    @Override
    public void writeEvent(byte[] tag_ID, int error) {
        HashMap<Object, Object> commandValueMap = initCommandValueMap(AbstractResponseListener
                .WRITE_COMMAND);

        if (error != AbstractResponseListener.NO_ERROR) {
            logResponse("Tag #" + extractStringFromByteArray(tag_ID) + " write error: " + error);
            commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_ERROR, error);
        }
        else {
            logResponse("Tag #" + extractStringFromByteArray(tag_ID) + " data written");
        }

        sendCommandCallbackResult(commandValueMap);
    }

    @Override
    public void writeIDevent(byte[] tag_ID, int error) {
        HashMap<Object, Object> commandValueMap = initCommandValueMap(AbstractResponseListener
                .WRITEID_COMMAND);

        if (error != AbstractResponseListener.NO_ERROR) {
            logResponse("Tag #" + extractStringFromByteArray(tag_ID) + " writeID error: " + error);
            commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_ERROR, error);
        }
        else {
            logResponse("Tag #" + extractStringFromByteArray(tag_ID) + " ID written");
        }

        sendCommandCallbackResult(commandValueMap);
    }

    @Override
    public void writePasswordEvent(byte[] tag_ID, int error) {
        HashMap<Object, Object> commandValueMap = initCommandValueMap(AbstractResponseListener
                .WRITEACCESSPASSWORD_COMMAND);

        if (error != AbstractResponseListener.NO_ERROR) {
            logResponse("Tag #" + extractStringFromByteArray(tag_ID) + " write-password error: " + error);
            commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_ERROR, error);
        }
        else {
            logResponse("Tag #" + extractStringFromByteArray(tag_ID) + " password written");
        }

        sendCommandCallbackResult(commandValueMap);
    }

    @NonNull
    private String extractStringFromByteArray(byte[] data) {
        StringBuilder value = new StringBuilder();
        for (byte aData : data) {
            value.append(String.format("%02X", aData));
        }
        return value.toString();
    }

    @NonNull
    private HashMap<Object, Object> initCommandValueMap(int command) {
        HashMap<Object, Object> commandValueMap = new HashMap<>();
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_CALLBACK_RESULT, command);
        return commandValueMap;
    }

    private void logResponse(String response) {
        Log.d(TAG, response);
    }

    private void sendCommandCallbackResult(Serializable extraData) {
        if (weakReferenceContext.get() != null) {
            Intent intent = new Intent(BleServicePassive.INTENT_ACTION_DEVICE_COMMAND_CALLBACK_RESULT);

            if (extraData != null) {
                intent.putExtra(BleServicePassive.INTENT_EXTRA_DATA_VALUE, extraData);
            }

            LocalBroadcastManager.getInstance(weakReferenceContext.get()).sendBroadcast(intent);
        }
    }
}
