package com.tertiumtechnology.testapp.listener;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.tertiumtechnology.api.rfidpassiveapilib.listener.AbstractReaderListener;
import com.tertiumtechnology.testapp.BleServicePassive;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class ReaderListener extends AbstractReaderListener {
    private static final String TAG = ReaderListener.class.getSimpleName();

    private final WeakReference<Context> weakReferenceContext;

    public ReaderListener(Context service) {
        this.weakReferenceContext = new WeakReference<>(service);
    }

    @Override
    public void BLEfirmwareVersionEvent(int major, int minor) {
        logResponse("BLE firmware version: major = " + major + " minor = " + minor);

        HashMap<Object, Object> commandValueMap = new HashMap<>();
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_CALLBACK, AbstractReaderListener
                .GET_BLE_FIRMWARE_VERSION_COMMAND);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_BLE_FIRMWARE_VERSION, major + "." + minor);

        sendCommandCallback(commandValueMap);
    }

    @Override
    public void BLEpowerEvent(int BLE_power) {
        logResponse("BLE power = " + BLE_power);

        HashMap<Object, Object> commandValueMap = new HashMap<>();
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_CALLBACK, AbstractReaderListener
                .GET_BLE_POWER_COMMAND);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_BLE_POWER, BLE_power);

        sendCommandCallback(commandValueMap);
    }

    @Override
    public void EPCfrequencyEvent(int frequency) {
        logResponse("EPC-frequency = " + frequency);

        HashMap<Object, Object> commandValueMap = new HashMap<>();
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_CALLBACK, AbstractReaderListener
                .GET_EPC_FREQUENCY_COMMAND);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_EPC_FREQUENCY, frequency);

        sendCommandCallback(commandValueMap);
    }

    @Override
    public void ISO15693bitrateEvent(int bitrate, boolean permanent) {
        logResponse("ISO15693 bit-rate: rate = " + bitrate + " permanent = " + permanent);

        HashMap<Object, Object> commandValueMap = new HashMap<>();
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_CALLBACK, AbstractReaderListener
                .GET_ISO15693_BITRATE_COMMAND);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_ISO15693_BITRATE_BITRATE, bitrate);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_ISO15693_BITRATE_PERMANENT, permanent);

        sendCommandCallback(commandValueMap);
    }

    @Override
    public void ISO15693extensionFlagEvent(boolean flag, boolean permanent) {
        logResponse("ISO15693 extension-flag: flag = " + flag + " permanent = " + permanent);

        HashMap<Object, Object> commandValueMap = new HashMap<>();
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_CALLBACK, AbstractReaderListener
                .GET_ISO15693_EXTENSION_FLAG_COMMAND);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_ISO15693_EXTENSION_FLAG_FLAG, flag);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_ISO15693_EXTENSION_FLAG_PERMANENT, permanent);

        sendCommandCallback(commandValueMap);
    }

    @Override
    public void ISO15693optionBitsEvent(int option_bits) {
        String strOptBits = Integer.toString(option_bits, 16);
        logResponse("ISO-15693 option-bits = " + strOptBits + "H");

        HashMap<Object, Object> commandValueMap = new HashMap<>();
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_CALLBACK, AbstractReaderListener
                .GET_ISO15693_OPTION_BITS_COMMAND);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_ISO15693_OPTION_BITS, strOptBits);

        sendCommandCallback(commandValueMap);
    }

    @Override
    public void MACaddressEvent(byte[] MAC_address) {
        String macAddress = extractStringFromByteArray(MAC_address);

        logResponse("MAC address = " + macAddress);

        HashMap<Object, Object> commandValueMap = new HashMap<>();
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_CALLBACK, AbstractReaderListener
                .GET_MAC_ADDRESS_COMMAND);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_MAC_ADDRESS, macAddress);

        sendCommandCallback(commandValueMap);
    }

    @Override
    public void RFforISO15693tunnelEvent(int delay, int timeout) {
        logResponse("RF-ISO15693-tunnel: delay = " + delay + "s timoeut = " + timeout + "ms");
    }

    @Override
    public void RFpowerEvent(int level, int mode) {
        logResponse("RF-power: level = " + level + " mode = " + mode);

        HashMap<Object, Object> commandValueMap = new HashMap<>();
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_CALLBACK, AbstractReaderListener
                .GET_RF_POWER_COMMAND);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_RF_POWER_LEVEL, level);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_RF_POWER_MODE, mode);

        sendCommandCallback(commandValueMap);
    }

    @Override
    public void advertisingIntervalEvent(int advertising_interval) {
        logResponse("Advertising interval = " + advertising_interval);

        HashMap<Object, Object> commandValueMap = new HashMap<>();
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_CALLBACK, AbstractReaderListener
                .GET_ADVERTISING_INTERVAL_COMMAND);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_ADVERTISING_INTERVAL, advertising_interval);

        sendCommandCallback(commandValueMap);
    }

    @Override
    public void availabilityEvent(boolean available) {
        logResponse("Availibility = " + available);

        HashMap<Object, Object> commandValueMap = new HashMap<>();
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_CALLBACK, AbstractReaderListener
                .TEST_AVAILABILITY_COMMAND);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_AVAILABILITY_STATUS, available);

        sendCommandCallback(commandValueMap);
    }

    @Override
    public void batteryLevelEvent(float level) {
        logResponse("Battery-level = " + level + "V");

        HashMap<Object, Object> commandValueMap = new HashMap<>();
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_CALLBACK, AbstractReaderListener
                .GET_BATTERY_LEVEL_COMMAND);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_BATTERY_LEVEL, level);

        sendCommandCallback(commandValueMap);
    }

    @Override
    public void batteryStatusEvent(int status) {
        logResponse("Battery-status = " + status);

        HashMap<Object, Object> commandValueMap = new HashMap<>();
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_CALLBACK, AbstractReaderListener
                .GET_BATTERY_STATUS_COMMAND);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_BATTERY_STATUS, status);

        sendCommandCallback(commandValueMap);
    }

    @Override
    public void connectionFailedEvent(int error) {
        logResponse("Connection failed: error = " + error);

        Intent intent = new Intent(BleServicePassive.INTENT_ACTION_DEVICE_CONNECTION_OPERATION_FAILED);
        intent.putExtra(BleServicePassive.INTENT_EXTRA_DATA_VALUE, "Connection failed: error = " + error);

        sendIntent(intent);
    }

    @Override
    public void connectionIntervalAndMTUevent(float connection_interval, int MTU) {
        logResponse("Connection Interval and MTU: connection interval = " + connection_interval + " MTU = " + MTU);

        HashMap<Object, Object> commandValueMap = new HashMap<>();
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_CALLBACK, AbstractReaderListener
                .GET_CONNECTION_INTERVAL_AND_MTU_COMMAND);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_CONNECTION_INTERVAL, connection_interval);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_MTU, MTU);

        sendCommandCallback(commandValueMap);
    }

    @Override
    public void connectionIntervalEvent(float min_interval, float max_interval) {
        logResponse("Connection Interval: min = " + min_interval + "  max = " + max_interval);

        HashMap<Object, Object> commandValueMap = new HashMap<>();
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_CALLBACK, AbstractReaderListener
                .GET_CONNECTION_INTERVAL_COMMAND);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_CONNECTION_INTERVAL_MIN, min_interval);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_CONNECTION_INTERVAL_MAX, max_interval);

        sendCommandCallback(commandValueMap);
    }

    @Override
    public void connectionSuccessEvent() {
        logResponse("Successful connection");
        Intent intent = new Intent(BleServicePassive.INTENT_ACTION_DEVICE_CONNECTED);

        sendIntent(intent);
    }

    @Override
    public void disconnectionSuccessEvent() {
        logResponse("Successful disconnection");
        Intent intent = new Intent(BleServicePassive.INTENT_ACTION_DEVICE_DISCONNECTED);

        sendIntent(intent);
    }

    @Override
    public void firmwareVersionEvent(int major, int minor) {
        logResponse("Firmware-version = " + major + "." + minor);

        HashMap<Object, Object> commandValueMap = new HashMap<>();
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_CALLBACK, AbstractReaderListener
                .GET_FIRMWARE_VERSION_COMMAND);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_FIRMWARE_VERSION, major + "." + minor);

        sendCommandCallback(commandValueMap);
    }

    @Override
    public void nameEvent(String device_name) {
        logResponse("Device name = " + device_name);

        HashMap<Object, Object> commandValueMap = new HashMap<>();
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_CALLBACK, AbstractReaderListener
                .GET_DEVICE_NAME_COMMAND);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_DEVICE_NAME, device_name);

        sendCommandCallback(commandValueMap);
    }

    @Override
    public void resultEvent(int command, int error) {
        HashMap<Object, Object> commandValueMap = new HashMap<>();
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_RESULT, command);

        if (error != 0) {
            commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_ERROR, error);
            logResponse("Result: command = " + command + " error = " + error);
        }
        else {
            logResponse("Result: command = " + command + " error = " + error);
        }

        Intent intent = new Intent(BleServicePassive.INTENT_ACTION_DEVICE_COMMAND_RESULT);
        intent.putExtra(BleServicePassive.INTENT_EXTRA_DATA_VALUE, commandValueMap);

        sendIntent(intent);
    }

    @Override
    public void securityLevelEvent(int level) {
        logResponse("Security level: level = " + level);

        HashMap<Object, Object> commandValueMap = new HashMap<>();
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_CALLBACK, AbstractReaderListener
                .GET_SECURITY_LEVEL_COMMAND);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_SECURITY_LEVEL, level);

        sendCommandCallback(commandValueMap);
    }

    @Override
    public void shutdownTimeEvent(int time) {
        logResponse("Shutdown-time = " + time + "s");

        HashMap<Object, Object> commandValueMap = new HashMap<>();
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_CALLBACK, AbstractReaderListener
                .GET_SHUTDOWN_TIME_COMMAND);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_SHUTDOWN_TIME, time);

        sendCommandCallback(commandValueMap);
    }

    @Override
    public void slaveLatencyEvent(int slave_latency) {
        logResponse("Slave latency = " + slave_latency);

        HashMap<Object, Object> commandValueMap = new HashMap<>();
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_CALLBACK, AbstractReaderListener
                .GET_SLAVE_LATENCY_COMMAND);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_SLAVE_LATENCY, slave_latency);

        sendCommandCallback(commandValueMap);
    }

    @Override
    public void supervisionTimeoutEvent(int supervision_timeout) {
        logResponse("Supervision Timeout = " + supervision_timeout);

        HashMap<Object, Object> commandValueMap = new HashMap<>();
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_CALLBACK, AbstractReaderListener
                .GET_SUPERVISION_TIMEOUT_COMMAND);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_SUPERVISION_TIMEOUT, supervision_timeout);

        sendCommandCallback(commandValueMap);
    }

    @Override
    public void tunnelEvent(byte[] data) {
        logResponse("Tag tunnel data: ");
        String strData = extractStringFromByteArray(data);

        logResponse("Tunnel data: " + strData);

        HashMap<Object, Object> commandValueMap = new HashMap<>();
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_CALLBACK, AbstractReaderListener
                .ISO15693_TUNNEL_COMMAND);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_ISO15693_TUNNEL_DATA, strData);

        sendCommandCallback(commandValueMap);
    }

    @Override
    public void userMemoryEvent(byte[] data_block) {
        String strData = extractStringFromByteArray(data_block);

        logResponse("User Memory = " + strData);

        HashMap<Object, Object> commandValueMap = new HashMap<>();
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_CALLBACK, AbstractReaderListener
                .READ_USER_MEMORY_COMMAND);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_USER_MEMORY, strData);

        sendCommandCallback(commandValueMap);
    }

    @NonNull
    private String extractStringFromByteArray(byte[] data) {
        StringBuilder value = new StringBuilder();
        for (byte aData : data) {
            value.append(String.format("%02X", aData));
        }
        return value.toString();
    }

    private void logResponse(String response) {
        Log.d(TAG, response);
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