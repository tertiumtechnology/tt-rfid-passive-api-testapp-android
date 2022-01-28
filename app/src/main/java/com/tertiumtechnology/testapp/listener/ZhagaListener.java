package com.tertiumtechnology.testapp.listener;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.tertiumtechnology.api.rfidpassiveapilib.listener.AbstractReaderListener;
import com.tertiumtechnology.api.rfidpassiveapilib.listener.AbstractZhagaListener;
import com.tertiumtechnology.testapp.BleServicePassive;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class ZhagaListener extends AbstractZhagaListener {

    private static final String TAG = ZhagaListener.class.getSimpleName();

    private final WeakReference<Context> weakReferenceContext;

    public ZhagaListener(Context service) {
        this.weakReferenceContext = new WeakReference<>(service);
    }

    @Override
    public void HMIevent(int LED_color, int sound_vibration, int button_number) {
        logResponse("HMI support: led color = " + LED_color + " sound vibration = " + sound_vibration +
                " button number = " + button_number);

        HashMap<Object, Object> commandValueMap = new HashMap<>();
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_CALLBACK, AbstractZhagaListener
                .ZHAGA_GET_HMI_SUPPORT_COMMAND);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_HMI_LED_COLOR, LED_color);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_HMI_SOUND_VIBRATION, sound_vibration);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_HMI_BUTTON_NUMBER, button_number);

        sendCommandCallback(commandValueMap);
    }

    @Override
    public void LEDforCommandEvent(int light_color, int light_on_time, int light_off_time, int light_repetition) {
        logResponse("Led for command: light color = " + light_color + ", light on time = " + light_on_time +
                ", light off time = " + light_off_time + ", light repetition" + light_repetition);

        HashMap<Object, Object> commandValueMap = new HashMap<>();
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_CALLBACK, AbstractZhagaListener
                .ZHAGA_GET_COMMAND_LED_COMMAND);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_LED_LIGHT_COLOR, light_color);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_LED_LIGHT_ON_TIME, light_on_time);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_LED_LIGHT_OFF_TIME, light_off_time);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_LED_LIGHT_REPETITION, light_repetition);

        sendCommandCallback(commandValueMap);
    }

    @Override
    public void LEDforErrorEvent(int light_color, int light_on_time, int light_off_time, int light_repetition) {
        logResponse("Led for error: light color = " + light_color + ", light on time = " + light_on_time +
                ", light off time = " + light_off_time + ", light repetition" + light_repetition);

        HashMap<Object, Object> commandValueMap = new HashMap<>();
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_CALLBACK, AbstractZhagaListener
                .ZHAGA_GET_ERROR_LED_COMMAND);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_ERROR_LED_LIGHT_COLOR, light_color);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_ERROR_LED_LIGHT_ON_TIME, light_on_time);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_ERROR_LED_LIGHT_OFF_TIME, light_off_time);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_ERROR_LED_LIGHT_REPETITION, light_repetition);

        sendCommandCallback(commandValueMap);
    }

    @Override
    public void LEDforInventoryEvent(int light_color, int light_on_time, int light_off_time, int light_repetition) {
        logResponse("Led for inventory: light color = " + light_color + ", light on time = " + light_on_time +
                ", light off time = " + light_off_time + ", light repetition" + light_repetition);

        HashMap<Object, Object> commandValueMap = new HashMap<>();
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_CALLBACK, AbstractZhagaListener
                .ZHAGA_GET_INVENTORY_LED_COMMAND);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_INVENTORY_LED_LIGHT_COLOR, light_color);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_INVENTORY_LED_LIGHT_ON_TIME, light_on_time);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_INVENTORY_LED_LIGHT_OFF_TIME, light_off_time);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_INVENTORY_LED_LIGHT_REPETITION, light_repetition);

        sendCommandCallback(commandValueMap);
    }

    @Override
    public void RFevent(boolean RF_on) {
        logResponse("Rf on = " + RF_on);

        HashMap<Object, Object> commandValueMap = new HashMap<>();
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_CALLBACK, AbstractZhagaListener
                .ZHAGA_GET_RF_COMMAND);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_RF_ON, RF_on);

        sendCommandCallback(commandValueMap);
    }

    @Override
    public void RFonOffEvent(int RF_power, int RF_off_timeout, int RF_on_preactivation) {
        logResponse("RF on off: RF power = " + RF_power + ", RF off timeout = " + RF_off_timeout +
                ", RF on preactivation = " + RF_on_preactivation);

        HashMap<Object, Object> commandValueMap = new HashMap<>();
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_CALLBACK, AbstractZhagaListener
                .ZHAGA_GET_RF_ONOFF_COMMAND);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_RF_ON_OFF_RF_POWER, RF_power);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_RF_ON_OFF_RF_OFF_TIMEOUT, RF_off_timeout);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_RF_ON_OFF_RF_ON_PREACTIVATION, RF_on_preactivation);

        sendCommandCallback(commandValueMap);
    }

    @Override
    public void activatedButtonEvent(int activated_button) {
        logResponse("Activated button = " + activated_button);

        HashMap<Object, Object> commandValueMap = new HashMap<>();
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_CALLBACK, AbstractZhagaListener
                .ZHAGA_GET_ACTIVATED_BUTTON_COMMAND);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_ACTIVATED_BUTTON, activated_button);

        sendCommandCallback(commandValueMap);
    }

    @Override
    public void autoOffEvent(int OFF_time) {
        logResponse("Auto off time = " + OFF_time);

        HashMap<Object, Object> commandValueMap = new HashMap<>();
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_CALLBACK, AbstractZhagaListener
                .ZHAGA_GET_AUTOOFF_COMMAND);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_AUTO_OFF_TIME, OFF_time);

        sendCommandCallback(commandValueMap);
    }

    @Override
    public void buttonEvent(int button, int time) {
        logResponse("Button event: button = " + button + ", time = " + time);

        HashMap<Object, Object> eventValueMap = new HashMap<>();
        eventValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_EVENT_TRIGGERED,
                BleServicePassive.EVENT_BUTTON_EVENT);
        eventValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_BUTTON, button);
        eventValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_TIME, time);

        sendEventTriggered(eventValueMap);
    }

    @Override
    public void connectionFailedEvent(int error) {
        // already implemented in ReaderListener
    }

    @Override
    public void connectionSuccessEvent() {
        // already implemented in ReaderListener
    }

    @Override
    public void deviceEventEvent(int event_number, int event_code) {
        logResponse("Device Event: code = " + event_code + ", number = " + event_number);

        HashMap<Object, Object> eventValueMap = new HashMap<>();
        eventValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_EVENT_RESULT_CODE, event_code);
        eventValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_EVENT_RESULT_NUMBER, event_number);

        Intent intent = new Intent(BleServicePassive.INTENT_ACTION_DEVICE_EVENT_RESULT);
        intent.putExtra(BleServicePassive.INTENT_EXTRA_DATA_VALUE, eventValueMap);

        sendIntent(intent);
    }

    @Override
    public void disconnectionSuccessEvent() {
        // already implemented in ReaderListener
    }

    @Override
    public void nameEvent(String device_name) {
        // already implemented in ReaderListener
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
        // already implemented in ReaderListener
    }

    @Override
    public void soundForCommandEvent(int sound_frequency, int sound_on_time, int sound_off_time, int sound_repetition) {
        logResponse("Sound for command: sound frequency = " + sound_frequency + ", sound on time = " + sound_on_time +
                ", sound off time = " + sound_off_time + ", sound repetition" + sound_repetition);

        HashMap<Object, Object> commandValueMap = new HashMap<>();
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_CALLBACK, AbstractZhagaListener
                .ZHAGA_GET_COMMAND_SOUND_COMMAND);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_SOUND_FREQUENCY, sound_frequency);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_SOUND_ON_TIME, sound_on_time);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_SOUND_OFF_TIME, sound_off_time);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_SOUND_REPETITION, sound_repetition);

        sendCommandCallback(commandValueMap);
    }

    @Override
    public void soundForErrorEvent(int sound_frequency, int sound_on_time, int sound_off_time, int sound_repetition) {
        logResponse("Sound for error: sound frequency = " + sound_frequency + ", sound on time = " + sound_on_time +
                ", sound off time = " + sound_off_time + ", sound repetition" + sound_repetition);

        HashMap<Object, Object> commandValueMap = new HashMap<>();
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_CALLBACK, AbstractZhagaListener
                .ZHAGA_GET_ERROR_SOUND_COMMAND);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_ERROR_SOUND_FREQUENCY, sound_frequency);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_ERROR_SOUND_ON_TIME, sound_on_time);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_ERROR_SOUND_OFF_TIME, sound_off_time);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_ERROR_SOUND_REPETITION, sound_repetition);

        sendCommandCallback(commandValueMap);
    }

    @Override
    public void soundForInventoryEvent(int sound_frequency, int sound_on_time, int sound_off_time,
                                       int sound_repetition) {
        logResponse("Sound for inventory: sound frequency = " + sound_frequency + ", sound on time = " + sound_on_time +
                ", sound off time = " + sound_off_time + ", sound repetition " + sound_repetition);

        HashMap<Object, Object> commandValueMap = new HashMap<>();
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_CALLBACK, AbstractZhagaListener
                .ZHAGA_GET_INVENTORY_SOUND_COMMAND);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_INVENTORY_SOUND_FREQUENCY, sound_frequency);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_INVENTORY_SOUND_ON_TIME, sound_on_time);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_INVENTORY_SOUND_OFF_TIME, sound_off_time);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_INVENTORY_SOUND_REPETITION, sound_repetition);

        sendCommandCallback(commandValueMap);
    }

    @Override
    public void transparentEvent(byte[] answer) {
        String strAnswer = extractStringFromByteArray(answer);

        logResponse("Transparent = " + strAnswer);

        HashMap<Object, Object> commandValueMap = new HashMap<>();
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_CALLBACK, AbstractReaderListener
                .ZHAGA_TRANSPARENT_COMMAND);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_TRANSPARENT_ANSWER, strAnswer);

        sendCommandCallback(commandValueMap);
    }

    @Override
    public void vibrationForCommandEvent(int vibration_on_time, int vibration_off_time, int vibration_repetition) {
        logResponse("Vibration for command: vibration on time = " + vibration_on_time + ", vibration off time = " +
                vibration_off_time + ", vibration repetition" + vibration_repetition);

        HashMap<Object, Object> commandValueMap = new HashMap<>();
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_CALLBACK, AbstractZhagaListener
                .ZHAGA_GET_COMMAND_VIBRATION_COMMAND);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_VIBRATION_ON_TIME, vibration_on_time);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_VIBRATION_OFF_TIME, vibration_off_time);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_VIBRATION_REPETITION,
                vibration_repetition);

        sendCommandCallback(commandValueMap);
    }

    @Override
    public void vibrationForErrorEvent(int vibration_on_time, int vibration_off_time, int vibration_repetition) {
        logResponse("Vibration for error: vibration on time = " + vibration_on_time + ", vibration off time = " +
                vibration_off_time + ", vibration repetition" + vibration_repetition);

        HashMap<Object, Object> commandValueMap = new HashMap<>();
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_CALLBACK, AbstractZhagaListener
                .ZHAGA_GET_ERROR_VIBRATION_COMMAND);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_ERROR_VIBRATION_ON_TIME, vibration_on_time);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_ERROR_VIBRATION_OFF_TIME, vibration_off_time);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_ERROR_VIBRATION_REPETITION,
                vibration_repetition);

        sendCommandCallback(commandValueMap);
    }

    @Override
    public void vibrationForInventoryEvent(int vibration_on_time, int vibration_off_time, int vibration_repetition) {
        logResponse("Vibration for inventory: vibration on time = " + vibration_on_time + ", vibration off time = " +
                vibration_off_time + ", vibration repetition" + vibration_repetition);

        HashMap<Object, Object> commandValueMap = new HashMap<>();
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_CALLBACK, AbstractZhagaListener
                .ZHAGA_GET_INVENTORY_VIBRATION_COMMAND);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_INVENTORY_VIBRATION_ON_TIME, vibration_on_time);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_INVENTORY_VIBRATION_OFF_TIME, vibration_off_time);
        commandValueMap.put(BleServicePassive.INTENT_EXTRA_DATA_INVENTORY_VIBRATION_REPETITION,
                vibration_repetition);

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

    private void sendEventTriggered(Serializable extraData) {
        Intent intent = new Intent(BleServicePassive.INTENT_ACTION_DEVICE_EVENT_TRIGGERED);

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
