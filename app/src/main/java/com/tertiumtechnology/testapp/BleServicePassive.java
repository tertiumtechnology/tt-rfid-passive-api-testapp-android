package com.tertiumtechnology.testapp;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.tertiumtechnology.api.rfidpassiveapilib.EPC_tag;
import com.tertiumtechnology.api.rfidpassiveapilib.ISO14443A_tag;
import com.tertiumtechnology.api.rfidpassiveapilib.ISO15693_tag;
import com.tertiumtechnology.api.rfidpassiveapilib.PassiveReader;
import com.tertiumtechnology.api.rfidpassiveapilib.Tag;
import com.tertiumtechnology.api.rfidpassiveapilib.listener.AbstractReaderListener;
import com.tertiumtechnology.api.rfidpassiveapilib.listener.AbstractResponseListener;
import com.tertiumtechnology.api.rfidpassiveapilib.util.BleSettings;
import com.tertiumtechnology.testapp.listener.InventoryListener;
import com.tertiumtechnology.testapp.listener.ReaderListener;
import com.tertiumtechnology.testapp.listener.ResponseListener;
import com.tertiumtechnology.testapp.listener.ZhagaListener;
import com.tertiumtechnology.testapp.util.Preferences;

public class BleServicePassive extends Service {
    class LocalBinder extends Binder {
        BleServicePassive getService() {
            return BleServicePassive.this;
        }
    }

    public static final String INTENT_ACTION_DEVICE_CONNECTED = "DEVICE_CONNECTED";
    public static final String INTENT_ACTION_DEVICE_CONNECTION_OPERATION_FAILED = "DEVICE_CONNECTION_OPERATION_FAILED";
    public static final String INTENT_ACTION_DEVICE_DISCONNECTED = "DEVICE_DISCONNECTED";
    public static final String INTENT_ACTION_DEVICE_COMMAND_CALLBACK = "DEVICE_COMMAND_CALLBACK";
    public static final String INTENT_ACTION_DEVICE_COMMAND_RESULT = "DEVICE_COMMAND_RESULT";
    public static final String INTENT_ACTION_DEVICE_COMMAND_CALLBACK_RESULT = "DEVICE_COMMAND_CALLBACK_RESULT";

    // events
    public static final String INTENT_ACTION_DEVICE_EVENT_TRIGGERED = "DEVICE_EVENT_TRIGGERED";
    public static final String INTENT_ACTION_DEVICE_EVENT_RESULT = "DEVICE_EVENT_RESULT";

    public static final String INTENT_EXTRA_DATA_COMMAND_CALLBACK = "COMMAND_CALLBACK";
    public static final String INTENT_EXTRA_DATA_COMMAND_RESULT = "COMMAND_RESULT";
    public static final String INTENT_EXTRA_DATA_COMMAND_CALLBACK_RESULT = "COMMAND_CALLBACK_RESULT";

    // events
    public static final String INTENT_EXTRA_DATA_EVENT_TRIGGERED = "EVENT_TRIGGERED";
    public static final String INTENT_EXTRA_DATA_EVENT_RESULT_CODE = "EVENT_RESULT_CODE";
    public static final String INTENT_EXTRA_DATA_EVENT_RESULT_NUMBER = "EVENT_RESULT_NUMBER";

    public static final String INTENT_EXTRA_DATA_VALUE = "VALUE";
    public static final String INTENT_EXTRA_DATA_ERROR = "ERROR";

    public static final String INTENT_EXTRA_DATA_FIRMWARE_VERSION = "FIRMWARE_VERSION";
    public static final String INTENT_EXTRA_DATA_BATTERY_STATUS = "BATTERY_STATUS";
    public static final String INTENT_EXTRA_DATA_BATTERY_LEVEL = "BATTERY_LEVEL";
    public static final String INTENT_EXTRA_DATA_AVAILABILITY_STATUS = "AVAILABILITY_STATUS";
    public static final String INTENT_EXTRA_DATA_SHUTDOWN_TIME = "SHUTDOWN_TIME";
    public static final String INTENT_EXTRA_DATA_RF_POWER_LEVEL = "RF_POWER_LEVEL";
    public static final String INTENT_EXTRA_DATA_RF_POWER_MODE = "RF_POWER_MODE";
    public static final String INTENT_EXTRA_DATA_ISO15693_OPTION_BITS = "ISO15693_OPTION_BITS";
    public static final String INTENT_EXTRA_DATA_ISO15693_EXTENSION_FLAG_FLAG = "ISO15693_EXTENSION_FLAG_FLAG";
    public static final String INTENT_EXTRA_DATA_ISO15693_EXTENSION_FLAG_PERMANENT =
            "ISO15693_EXTENSION_FLAG_PERMANENT";
    public static final String INTENT_EXTRA_DATA_ISO15693_BITRATE_BITRATE = "ISO15693_BITRATE_BITRATE";
    public static final String INTENT_EXTRA_DATA_ISO15693_BITRATE_PERMANENT = "ISO15693_BITRATE_PERMANENT";
    public static final String INTENT_EXTRA_DATA_ISO15693_TUNNEL_DATA = "ISO15693_TUNNEL_DATA";
    public static final String INTENT_EXTRA_DATA_SECURITY_LEVEL = "SECURITY_LEVEL";

    public static final String INTENT_EXTRA_DATA_EPC_FREQUENCY = "EPC_FREQUENCY";

    public static final String INTENT_EXTRA_DATA_INVENTORY_TAG = "INVENTORY_TAG";

    public static final String INTENT_EXTRA_DATA_READ_VALUE = "READ_VALUE";
    public static final String INTENT_EXTRA_DATA_READ_TID_VALUE = "READ_TID_VALUE";

    // common command
    public static final String INTENT_EXTRA_DATA_DEVICE_NAME = "DEVICE_NAME";

    // ble command
    public static final String INTENT_EXTRA_DATA_BLE_FIRMWARE_VERSION = "BLE_FIRMWARE_VERSION";
    public static final String INTENT_EXTRA_DATA_ADVERTISING_INTERVAL = "ADVERTISING_INTERVAL";
    public static final String INTENT_EXTRA_DATA_BLE_POWER = "BLE_POWER";
    public static final String INTENT_EXTRA_DATA_CONNECTION_INTERVAL_MIN = "CONNECTION_INTERVAL_MIN";
    public static final String INTENT_EXTRA_DATA_CONNECTION_INTERVAL_MAX = "CONNECTION_INTERVAL_MAX";
    public static final String INTENT_EXTRA_DATA_SLAVE_LATENCY = "SLAVE_LATENCY";
    public static final String INTENT_EXTRA_DATA_SUPERVISION_TIMEOUT = "SUPERVISION_TIMEOUT";
    public static final String INTENT_EXTRA_DATA_CONNECTION_INTERVAL = "CONNECTION_INTERVAL";
    public static final String INTENT_EXTRA_DATA_MTU = "MTU";
    public static final String INTENT_EXTRA_DATA_MAC_ADDRESS = "MAC_ADDRESS";

    // memory command
    public static final String INTENT_EXTRA_DATA_USER_MEMORY = "USER_MEMORY";

    // zhaga command
    public static final String INTENT_EXTRA_DATA_HMI_LED_COLOR = "HMI_LED_COLOR";
    public static final String INTENT_EXTRA_DATA_HMI_SOUND_VIBRATION = "HMI_SOUND_VIBRATION";
    public static final String INTENT_EXTRA_DATA_HMI_BUTTON_NUMBER = "HMI_BUTTON_NUMBER";

    public static final String INTENT_EXTRA_DATA_INVENTORY_SOUND_FREQUENCY = "INVENTORY_SOUND_FREQUENCY";
    public static final String INTENT_EXTRA_DATA_INVENTORY_SOUND_ON_TIME = "INVENTORY_SOUND_ON_TIME";
    public static final String INTENT_EXTRA_DATA_INVENTORY_SOUND_OFF_TIME = "INVENTORY_SOUND_OFF_TIME";
    public static final String INTENT_EXTRA_DATA_INVENTORY_SOUND_REPETITION = "INVENTORY_SOUND_REPETITION";

    public static final String INTENT_EXTRA_DATA_COMMAND_SOUND_FREQUENCY = "COMMAND_SOUND_FREQUENCY";
    public static final String INTENT_EXTRA_DATA_COMMAND_SOUND_ON_TIME = "COMMAND_SOUND_ON_TIME";
    public static final String INTENT_EXTRA_DATA_COMMAND_SOUND_OFF_TIME = "COMMAND_SOUND_OFF_TIME";
    public static final String INTENT_EXTRA_DATA_COMMAND_SOUND_REPETITION = "COMMAND_SOUND_REPETITION";

    public static final String INTENT_EXTRA_DATA_ERROR_SOUND_FREQUENCY = "ERROR_SOUND_FREQUENCY";
    public static final String INTENT_EXTRA_DATA_ERROR_SOUND_ON_TIME = "ERROR_SOUND_ON_TIME";
    public static final String INTENT_EXTRA_DATA_ERROR_SOUND_OFF_TIME = "ERROR_SOUND_OFF_TIME";
    public static final String INTENT_EXTRA_DATA_ERROR_SOUND_REPETITION = "ERROR_SOUND_REPETITION";

    public static final String INTENT_EXTRA_DATA_INVENTORY_LED_LIGHT_COLOR = "INVENTORY_LED_LIGHT_COLOR";
    public static final String INTENT_EXTRA_DATA_INVENTORY_LED_LIGHT_ON_TIME = "INVENTORY_LED_LIGHT_ON_TIME";
    public static final String INTENT_EXTRA_DATA_INVENTORY_LED_LIGHT_OFF_TIME = "INVENTORY_LED_LIGHT_OFF_TIME";
    public static final String INTENT_EXTRA_DATA_INVENTORY_LED_LIGHT_REPETITION =
            "INVENTORY_LED_LIGHT_REPETITION";

    public static final String INTENT_EXTRA_DATA_COMMAND_LED_LIGHT_COLOR = "COMMAND_LED_LIGHT_COLOR";
    public static final String INTENT_EXTRA_DATA_COMMAND_LED_LIGHT_ON_TIME = "COMMAND_LED_LIGHT_ON_TIME";
    public static final String INTENT_EXTRA_DATA_COMMAND_LED_LIGHT_OFF_TIME = "COMMAND_LED_LIGHT_OFF_TIME";
    public static final String INTENT_EXTRA_DATA_COMMAND_LED_LIGHT_REPETITION = "COMMAND_LED_LIGHT_REPETITION";

    public static final String INTENT_EXTRA_DATA_ERROR_LED_LIGHT_COLOR = "ERROR_LED_LIGHT_COLOR";
    public static final String INTENT_EXTRA_DATA_ERROR_LED_LIGHT_ON_TIME = "ERROR_LED_LIGHT_ON_TIME";
    public static final String INTENT_EXTRA_DATA_ERROR_LED_LIGHT_OFF_TIME = "ERROR_LED_LIGHT_OFF_TIME";
    public static final String INTENT_EXTRA_DATA_ERROR_LED_LIGHT_REPETITION = "ERROR_LED_LIGHT_REPETITION";

    public static final String INTENT_EXTRA_DATA_INVENTORY_VIBRATION_ON_TIME = "INVENTORY_VIBRATION_ON_TIME";
    public static final String INTENT_EXTRA_DATA_INVENTORY_VIBRATION_OFF_TIME = "INVENTORY_VIBRATION_OFF_TIME";
    public static final String INTENT_EXTRA_DATA_INVENTORY_VIBRATION_REPETITION =
            "INVENTORY_VIBRATION_REPETITION";

    public static final String INTENT_EXTRA_DATA_COMMAND_VIBRATION_ON_TIME = "COMMAND_VIBRATION_ON_TIME";
    public static final String INTENT_EXTRA_DATA_COMMAND_VIBRATION_OFF_TIME = "COMMAND_VIBRATION_OFF_TIME";
    public static final String INTENT_EXTRA_DATA_COMMAND_VIBRATION_REPETITION = "COMMAND_VIBRATION_REPETITION";

    public static final String INTENT_EXTRA_DATA_ERROR_VIBRATION_ON_TIME = "ERROR_VIBRATION_ON_TIME";
    public static final String INTENT_EXTRA_DATA_ERROR_VIBRATION_OFF_TIME = "ERROR_VIBRATION_OFF_TIME";
    public static final String INTENT_EXTRA_DATA_ERROR_VIBRATION_REPETITION = "ERROR_VIBRATION_REPETITION";

    public static final String INTENT_EXTRA_DATA_ACTIVATED_BUTTON = "ACTIVATED_BUTTON";

    public static final String INTENT_EXTRA_DATA_RF_ON = "RF_ON";
    public static final String INTENT_EXTRA_DATA_RF_ON_OFF_RF_POWER = "RF_ON_OFF_RF_POWER";
    public static final String INTENT_EXTRA_DATA_RF_ON_OFF_RF_OFF_TIMEOUT = "RF_ON_OFF_RF_OFF_TIMEOUT";
    public static final String INTENT_EXTRA_DATA_RF_ON_OFF_RF_ON_PREACTIVATION = "RF_ON_OFF_RF_ON_PREACTIVATION";
    public static final String INTENT_EXTRA_DATA_AUTO_OFF_TIME = "AUTO_OFF_TIME";

    public static final String INTENT_EXTRA_DATA_TRANSPARENT_ANSWER = "TRANSPARENT_ANSWER";

    // events
    public static final String EVENT_BUTTON_EVENT = "BUTTON_EVENT";

    public static final String INTENT_EXTRA_DATA_BUTTON = "BUTTON";
    public static final String INTENT_EXTRA_DATA_TIME = "TIME";

    private final IBinder localBinder = new LocalBinder();

    private ReaderListener readerListener;
    private ResponseListener responseListener;
    private PassiveReader passiveReader;

    public void close() {
        passiveReader.close();
    }

    public void connect(String address) {
        passiveReader.connect(address, getApplicationContext());
    }

    public void disconnect() {
        passiveReader.disconnect();
    }

    public void init(BluetoothAdapter bluetoothAdapter) {
        InventoryListener inventoryListener = new InventoryListener(this);
        readerListener = new ReaderListener(this.getApplicationContext());
        responseListener = new ResponseListener(this);
        ZhagaListener zhagaListener = new ZhagaListener(this);

        BleSettings bleSettings = Preferences.getBleSettings(this);

        passiveReader = PassiveReader.getPassiveReaderInstance(inventoryListener, readerListener, responseListener,
                zhagaListener, bluetoothAdapter, bleSettings);
    }

    public boolean isDeviceConnected(String deviceAddress) {
        return passiveReader != null && passiveReader.isAvailable(deviceAddress, getApplicationContext());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return localBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    public void requestActivateButton(int button) {
        if (passiveReader != null) {
            passiveReader.activateButton(button);
        }
    }

    public void requestBLEfirmwareVersion() {
        if (passiveReader != null) {
            passiveReader.getBLEfirmwareVersion();
        }
    }

    public void requestBatteryLevel() {
        if (passiveReader != null) {
            passiveReader.getBatteryLevel();
        }
    }

    public void requestBatteryStatus() {
        if (passiveReader != null) {
            passiveReader.getBatteryStatus();
        }
    }

    public void requestDefaultBLEconfiguration(int mode, boolean erase_bonding) {
        if (passiveReader != null) {
            passiveReader.defaultBLEconfiguration(mode, erase_bonding);
        }
    }

    public void requestDefaultConfiguration() {
        if (passiveReader != null) {
            passiveReader.defaultConfiguration();
        }
    }

    public void requestDefaultSetup() {
        if (passiveReader != null) {
            passiveReader.defaultSetup();
        }
    }

    public void requestDoInventory() {
        if (passiveReader != null) {
            passiveReader.doInventory();
        }
    }

    public void requestGetActivatedButton() {
        if (passiveReader != null) {
            passiveReader.getActivatedButton();
        }
    }

    public void requestGetAdvertisingInterval() {
        if (passiveReader != null) {
            passiveReader.getAdvertisingInterval();
        }
    }

    public void requestGetAutoOff() {
        if (passiveReader != null) {
            passiveReader.getAutoOff();
        }
    }

    public void requestGetBLEpower() {
        if (passiveReader != null) {
            passiveReader.getBLEpower();
        }
    }

    public void requestGetConnectionInterval() {
        if (passiveReader != null) {
            passiveReader.getConnectionInterval();
        }
    }

    public void requestGetConnectionIntervalAndMtu() {
        if (passiveReader != null) {
            passiveReader.getConnectionIntervalAndMTU();
        }
    }

    public void requestGetEpcFrequency() {
        if (passiveReader != null) {
            passiveReader.getEPCfrequency();
        }
    }

    public void requestGetFirmwareVersion() {
        if (passiveReader != null) {
            passiveReader.getFirmwareVersion();
        }
    }

    public void requestGetHMIsupport() {
        if (passiveReader != null) {
            passiveReader.getHMIsupport();
        }
    }

    public void requestGetISO15693bitrate() {
        if (passiveReader != null) {
            passiveReader.getISO15693bitrate();
        }
    }

    public void requestGetISO15693extensionFlag() {
        if (passiveReader != null) {
            passiveReader.getISO15693extensionFlag();
        }
    }

    public void requestGetISO15693optionBits() {
        if (passiveReader != null) {
            passiveReader.getISO15693optionBits();
        }
    }

    public void requestGetLedForCommand() {
        if (passiveReader != null) {
            passiveReader.getLEDforCommand();
        }
    }

    public void requestGetLedForError() {
        if (passiveReader != null) {
            passiveReader.getLEDforError();
        }
    }

    public void requestGetLedForInventory() {
        if (passiveReader != null) {
            passiveReader.getLEDforInventory();
        }
    }

    public void requestGetMACAddress() {
        if (passiveReader != null) {
            passiveReader.getMACaddress();
        }
    }

    public void requestGetName() {
        if (passiveReader != null) {
            passiveReader.getName();
        }
    }

    public void requestGetRF() {
        if (passiveReader != null) {
            passiveReader.getRF();
        }
    }

    public void requestGetRFonOff() {
        if (passiveReader != null) {
            passiveReader.getRFonOff();
        }
    }

    public void requestGetRFpower() {
        if (passiveReader != null) {
            passiveReader.getRFpower();
        }
    }

    public void requestGetSecurityLevel() {
        if (passiveReader != null) {
            passiveReader.getSecurityLevel();
        }
    }

    public void requestGetShutdownTime() {
        if (passiveReader != null) {
            passiveReader.getShutdownTime();
        }
    }

    public void requestGetSlaveLatency() {
        if (passiveReader != null) {
            passiveReader.getSlaveLatency();
        }
    }

    public void requestGetSoundForCommand() {
        if (passiveReader != null) {
            passiveReader.getSoundForCommand();
        }
    }

    public void requestGetSoundForError() {
        if (passiveReader != null) {
            passiveReader.getSoundForError();
        }
    }

    public void requestGetSoundForInventory() {
        if (passiveReader != null) {
            passiveReader.getSoundForInventory();
        }
    }

    public void requestGetSupervisionTimeout() {
        if (passiveReader != null) {
            passiveReader.getSupervisionTimeout();
        }
    }

    public void requestGetVibrationForCommand() {
        if (passiveReader != null) {
            passiveReader.getVibrationForCommand();
        }
    }

    public void requestGetVibrationForError() {
        if (passiveReader != null) {
            passiveReader.getVibrationForError();
        }
    }

    public void requestGetVibrationForInventory() {
        if (passiveReader != null) {
            passiveReader.getVibrationForInventory();
        }
    }

    public boolean requestIsHF() {
        return passiveReader != null && passiveReader.isHF();
    }

    public boolean requestIsUHF() {
        return passiveReader != null && passiveReader.isUHF();
    }

    public void requestKill(EPC_tag epc_tag, String hexPassword) {
        if (passiveReader != null) {
            byte[] password;

            try {
                password = hexStringToByte(hexPassword);
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                responseListener.writeEvent(epc_tag.getID(), AbstractResponseListener
                        .READER_DRIVER_COMMAND_WRONG_PARAMETER_ERROR);
                return;
            }

            epc_tag.kill(password);
        }
    }

    public void requestLight(boolean led_status, int led_blinking) {
        if (passiveReader != null) {
            passiveReader.light(led_status, led_blinking);
        }
    }

    public void requestLock(Tag tag, int lockType, String hexPassword) {
        if (passiveReader != null) {
            if (tag instanceof ISO15693_tag) {
                ISO15693_tag iso15693_tag = (ISO15693_tag) tag;
                iso15693_tag.lock(0, 2);
            }
            else if (tag instanceof ISO14443A_tag) {
                ISO14443A_tag iso14443A_tag = (ISO14443A_tag) tag;

                responseListener.lockEvent(iso14443A_tag.getID(), AbstractResponseListener
                        .READER_DRIVER_UNKNOW_COMMAND_ERROR);
            }
            else if (tag instanceof EPC_tag) {
                EPC_tag epc_tag = (EPC_tag) tag;

                byte[] password;

                try {
                    password = hexStringToByte(hexPassword);
                } catch (NumberFormatException | IndexOutOfBoundsException e) {
                    responseListener.writeEvent(tag.getID(), AbstractResponseListener
                            .READER_DRIVER_COMMAND_WRONG_PARAMETER_ERROR);
                    return;
                }

                epc_tag.lock(lockType, password);
            }
        }
    }

    public void requestOff() {
        if (passiveReader != null) {
            passiveReader.off();
        }
    }

    public void requestRead(Tag tag, int address, int block) {
        if (passiveReader != null) {
            if (tag instanceof ISO15693_tag) {// address: 0, block: 2
                ISO15693_tag iso15693_tag = (ISO15693_tag) tag;
                iso15693_tag.read(address, block);
            }
            else if (tag instanceof ISO14443A_tag) {
                ISO14443A_tag iso14443A_tag = (ISO14443A_tag) tag;

                responseListener.readEvent(iso14443A_tag.getID(), AbstractResponseListener
                        .READER_DRIVER_UNKNOW_COMMAND_ERROR, null);
            }
            else if (tag instanceof EPC_tag) {// address: 8, block: 4
                EPC_tag epc_tag = (EPC_tag) tag;
                epc_tag.read(address, block);
            }
        }
    }

    public void requestReadTID(EPC_tag epc_tag) {
        if (passiveReader != null) {
            epc_tag.readTID(8, null);
        }
    }

    public void requestReadUserMemory(int block) {
        if (passiveReader != null) {
            passiveReader.readUserMemory(block);
        }
    }

    public void requestReboot() {
        if (passiveReader != null) {
            passiveReader.reboot();
        }
    }

    public void requestReset(boolean bootloader) {
        if (passiveReader != null) {
            passiveReader.reset(bootloader);
        }
    }

    public void requestSetAdvertisingInterval(int interval) {
        if (passiveReader != null) {
            passiveReader.setAdvertisingInterval(interval);
        }
    }

    public void requestSetAutOff(int offTime) {
        if (passiveReader != null) {
            passiveReader.setAutoOff(offTime);
        }
    }

    public void requestSetBLEpower(int power) {
        if (passiveReader != null) {
            passiveReader.setBLEpower(power);
        }
    }

    public void requestSetConnectionInterval(float min, float max) {
        if (passiveReader != null) {
            passiveReader.setConnectionInterval(min, max);
        }
    }

    public void requestSetEpcFrequency(int frequency) {
        if (passiveReader != null) {
            passiveReader.setEPCfrequency(frequency);
        }
    }

    public void requestSetHMI(int sound_frequency, int sound_on_time, int sound_off_time, int sound_repetition,
                              int light_color, int light_on_time, int light_off_time, int light_repetition,
                              int vibration_on_time, int vibration_off_time, int vibration_repetition) {
        if (passiveReader != null) {
            passiveReader.setHMI(sound_frequency, sound_on_time, sound_off_time, sound_repetition, light_color,
                    light_on_time, light_off_time, light_repetition, vibration_on_time, vibration_off_time,
                    vibration_repetition);
        }
    }

    public void requestSetISO15693bitrate(int bitrate, boolean permanent) {
        if (passiveReader != null) {
            passiveReader.setISO15693bitrate(bitrate, permanent);
        }
    }

    public void requestSetISO15693extensionFlag(boolean flag, boolean permanent) {
        if (passiveReader != null) {
            passiveReader.setISO15693extensionFlag(flag, permanent);
        }
    }

    public void requestSetISO15693optionBits(int option_bits) {
        if (passiveReader != null) {
            passiveReader.setISO15693optionBits(option_bits);
        }
    }

    public void requestSetInventoryMode(int mode) {
        if (passiveReader != null) {
            passiveReader.setInventoryMode(mode);
        }
    }

    public void requestSetInventoryParameters(int feedback, int timeout, int interval) {
        if (passiveReader != null) {
            passiveReader.setInventoryParameters(feedback, timeout, interval);
        }
    }

    public void requestSetInventoryType(int standard) {
        if (passiveReader != null) {
            passiveReader.setInventoryType(standard);
        }
    }

    public void requestSetLedForCommand(int lightColor, int lightOnTime, int lightOffTime,
                                        int lightRepetition) {
        if (passiveReader != null) {
            passiveReader.setLEDforCommand(lightColor, lightOnTime, lightOffTime, lightRepetition);
        }
    }

    public void requestSetLedForError(int lightColor, int lightOnTime, int lightOffTime,
                                      int lightRepetition) {
        if (passiveReader != null) {
            passiveReader.setLEDforError(lightColor, lightOnTime, lightOffTime, lightRepetition);
        }
    }

    public void requestSetLedForInventory(int lightColor, int lightOnTime, int lightOffTime,
                                          int lightRepetition) {
        if (passiveReader != null) {
            passiveReader.setLEDforInventory(lightColor, lightOnTime, lightOffTime, lightRepetition);
        }
    }

    public void requestSetName(String device_name) {
        if (passiveReader != null) {
            passiveReader.setName(device_name);
        }
    }

    public void requestSetRF(boolean rfOn) {
        if (passiveReader != null) {
            passiveReader.setRF(rfOn);
        }
    }

    public void requestSetRFonOff(int rfPower, int rfOffTimeout, int rfOnPreactivation) {
        if (passiveReader != null) {
            passiveReader.setRFonOff(rfPower, rfOffTimeout, rfOnPreactivation);
        }
    }

    public void requestSetRFpower(int level, int mode) {
        if (passiveReader != null) {
            passiveReader.setRFpower(level, mode);
        }
    }

    public void requestSetSecurityLevel(int level) {
        if (passiveReader != null) {
            passiveReader.setSecurityLevel(level);
        }
    }

    public void requestSetShutdownTime(int time) {
        if (passiveReader != null) {
            passiveReader.setShutdownTime(time);
        }
    }

    public void requestSetSlaveLatency(int latency) {
        if (passiveReader != null) {
            passiveReader.setSlaveLatency(latency);
        }
    }

    public void requestSetSoundForCommand(int soundFrequency, int soundOnTime, int soundOffTime,
                                          int soundRepetition) {
        if (passiveReader != null) {
            passiveReader.setSoundForCommand(soundFrequency, soundOnTime, soundOffTime, soundRepetition);
        }
    }

    public void requestSetSoundForError(int soundFrequency, int soundOnTime, int soundOffTime,
                                        int soundRepetition) {
        if (passiveReader != null) {
            passiveReader.setSoundForError(soundFrequency, soundOnTime, soundOffTime, soundRepetition);
        }
    }

    public void requestSetSoundForInventory(int soundFrequency, int soundOnTime, int soundOffTime,
                                            int soundRepetition) {
        if (passiveReader != null) {
            passiveReader.setSoundForInventory(soundFrequency, soundOnTime, soundOffTime, soundRepetition);
        }
    }

    public void requestSetSupervisionTimeout(int timeout) {
        if (passiveReader != null) {
            passiveReader.setSupervisionTimeout(timeout);
        }
    }

    public void requestSetVibrationForCommand(int vibrationOnTime, int vibrationOffTime,
                                              int vibrationRepetition) {
        if (passiveReader != null) {
            passiveReader.setVibrationForCommand(vibrationOnTime, vibrationOffTime, vibrationRepetition);
        }
    }

    public void requestSetVibrationForError(int vibrationOnTime, int vibrationOffTime, int vibrationRepetition) {
        if (passiveReader != null) {
            passiveReader.setVibrationForError(vibrationOnTime, vibrationOffTime, vibrationRepetition);
        }
    }

    public void requestSetVibrationForInventory(int vibrationOnTime, int vibrationOffTime, int vibrationRepetition) {
        if (passiveReader != null) {
            passiveReader.setVibrationForInventory(vibrationOnTime, vibrationOffTime, vibrationRepetition);
        }
    }

    public void requestSound(int frequency, int step, int duration, int interval, int repetition) {
        if (passiveReader != null) {
            passiveReader.sound(frequency, step, duration, interval, repetition);
        }
    }

    public void requestStartTunnel(String hexCommand, boolean encrypted, String hexEncryptedFlag) {

        if (passiveReader != null) {
            byte[] command;

            try {
                command = hexStringToByte(hexCommand);
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                readerListener.resultEvent(AbstractReaderListener.ISO15693_TUNNEL_COMMAND,
                        AbstractReaderListener.READER_DRIVER_COMMAND_WRONG_PARAMETER_ERROR);
                return;
            }

            if (encrypted) {
                byte encryptedFlag;

                try {
                    encryptedFlag = hexStringToByte(hexEncryptedFlag)[0];
                } catch (NumberFormatException | IndexOutOfBoundsException e) {
                    readerListener.resultEvent(AbstractReaderListener.ISO15693_ENCRYPTEDTUNNEL_COMMAND,
                            AbstractReaderListener.READER_DRIVER_COMMAND_WRONG_PARAMETER_ERROR);
                    return;
                }

                passiveReader.ISO15693encryptedTunnel(encryptedFlag, command);
            }
            else {
                passiveReader.ISO15693tunnel(command);
            }
        }
    }

    public void requestTestAvailability() {
        if (passiveReader != null) {
            passiveReader.testAvailability();
        }
    }

    public void requestTransparent(String hexCommand) {
        if (passiveReader != null) {

            byte[] command;

            try {
                command = hexStringToByte(hexCommand);
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                readerListener.resultEvent(AbstractReaderListener.ZHAGA_TRANSPARENT_COMMAND, AbstractResponseListener
                        .READER_DRIVER_UNKNOW_COMMAND_ERROR);
                return;
            }

            passiveReader.transparent(command);
        }
    }

    public void requestWrite(Tag tag, int address, String hexData, String hexPassword) {

        if (passiveReader != null) {

            byte[] data;

            try {
                data = hexStringToByte(hexData);
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                responseListener.writeEvent(tag.getID(), AbstractResponseListener
                        .READER_DRIVER_COMMAND_WRONG_PARAMETER_ERROR);
                return;
            }

            if (tag instanceof ISO15693_tag) {// address: 0
                ISO15693_tag iso15693_tag = (ISO15693_tag) tag;
                iso15693_tag.write(address, data);
            }
            else if (tag instanceof ISO14443A_tag) {
                ISO14443A_tag iso14443A_tag = (ISO14443A_tag) tag;

                responseListener.writeEvent(iso14443A_tag.getID(), AbstractResponseListener
                        .READER_DRIVER_UNKNOW_COMMAND_ERROR);
            }
            else if (tag instanceof EPC_tag) {// address: 8
                EPC_tag epc_tag = (EPC_tag) tag;

                byte[] password;

                try {
                    password = hexStringToByte(hexPassword);
                } catch (NumberFormatException | IndexOutOfBoundsException e) {
                    responseListener.writeEvent(tag.getID(), AbstractResponseListener
                            .READER_DRIVER_COMMAND_WRONG_PARAMETER_ERROR);
                    return;
                }

                epc_tag.write(address, data, password);
            }
        }
    }

    public void requestWriteAccessPassword(EPC_tag epc_tag, String hexOldPassword, String hexNewPassword) {
        if (passiveReader != null) {
            byte[] oldPassword;
            byte[] newPassword;

            try {
                oldPassword = hexStringToByte(hexOldPassword);
                newPassword = hexStringToByte(hexNewPassword);
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                responseListener.writeEvent(epc_tag.getID(), AbstractResponseListener
                        .READER_DRIVER_COMMAND_WRONG_PARAMETER_ERROR);
                return;
            }

            epc_tag.writeAccessPassword(newPassword, oldPassword);
        }
    }

    public void requestWriteID(EPC_tag epc_tag) {
        if (passiveReader != null) {
            byte[] ID = new byte[16];
            ID[0] = 0x00;
            ID[1] = 0x01;
            ID[2] = 0x02;
            ID[3] = 0x03;
            ID[4] = 0x04;
            ID[5] = 0x05;
            ID[6] = 0x06;
            ID[7] = 0x07;
            ID[8] = 0x08;
            ID[9] = 0x09;
            ID[10] = 0x0A;
            ID[11] = 0x0B;
            ID[12] = 0x0C;
            ID[13] = 0x0D;
            ID[14] = 0x0E;
            ID[15] = 0x0F;
            epc_tag.writeID(ID, (short) (0x0000));
        }
    }

    public void requestWriteKillPassword(EPC_tag epc_tag, String hexOldPassword, String hexNewPassword) {
        if (passiveReader != null) {
            byte[] oldPassword;
            byte[] newPassword;

            try {
                oldPassword = hexStringToByte(hexOldPassword);
                newPassword = hexStringToByte(hexNewPassword);
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                responseListener.writeEvent(epc_tag.getID(), AbstractResponseListener
                        .READER_DRIVER_COMMAND_WRONG_PARAMETER_ERROR);
                return;
            }

            epc_tag.writeKillPassword(newPassword, oldPassword);
        }
    }

    public void requestWriteUserMemory(int block, String hexData) {
        if (passiveReader != null) {

            byte[] data;

            try {
                data = hexStringToByte(hexData);
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                readerListener.resultEvent(AbstractReaderListener.WRITE_USER_MEMORY_COMMAND, AbstractResponseListener
                        .READER_DRIVER_COMMAND_WRONG_PARAMETER_ERROR);
                return;
            }

            passiveReader.writeUserMemory(block, data);
        }
    }

    private byte[] hexStringToByte(String hexData) throws IndexOutOfBoundsException, NumberFormatException {
        byte[] data = new byte[hexData.length() / 2];

        for (int i = 0; i < data.length; i++) {
            String chunk = hexData.substring(i * 2, (i + 1) * 2);
            data[i] = (byte) Integer.valueOf(chunk, 16).intValue();
        }

        return data;
    }
}