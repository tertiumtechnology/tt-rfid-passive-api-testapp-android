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
import com.tertiumtechnology.api.rfidpassiveapilib.listener.AbstractResponseListener;
import com.tertiumtechnology.api.rfidpassiveapilib.util.BleSettings;
import com.tertiumtechnology.testapp.listener.InventoryListener;
import com.tertiumtechnology.testapp.listener.ReaderListener;
import com.tertiumtechnology.testapp.listener.ResponseListener;
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

    public static final String INTENT_EXTRA_DATA_COMMAND_CALLBACK = "COMMAND_CALLBACK";
    public static final String INTENT_EXTRA_DATA_COMMAND_RESULT = "COMMAND_RESULT";
    public static final String INTENT_EXTRA_DATA_COMMAND_CALLBACK_RESULT = "COMMAND_CALLBACK_RESULT";

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
    public static final String INTENT_EXTRA_DATA_EPC_FREQUENCY = "EPC_FREQUENCY";

    public static final String INTENT_EXTRA_DATA_INVENTORY_TAG = "INVENTORY_TAG";

    public static final String INTENT_EXTRA_DATA_READ_VALUE = "READ_VALUE";
    public static final String INTENT_EXTRA_DATA_READ_TID_VALUE = "READ_TID_VALUE";

    private final IBinder localBinder = new LocalBinder();

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
        ReaderListener readerListener = new ReaderListener(this.getApplicationContext());
        responseListener = new ResponseListener(this);
        BleSettings bleSettings = Preferences.getBleSettings(this);

        passiveReader = PassiveReader.getInstance(inventoryListener, readerListener, responseListener,
                bluetoothAdapter, bleSettings);
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

    public void requestDoInventory() {
        if (passiveReader != null) {
            passiveReader.doInventory();
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

    public void requestGetRFpower() {
        if (passiveReader != null) {
            passiveReader.getRFpower();
        }
    }

    public void requestGetShutdownTime() {
        if (passiveReader != null) {
            passiveReader.getShutdownTime();
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

    public void requestSetEpcFrequency(int frequency) {
        if (passiveReader != null) {
            passiveReader.setEPCfrequency(frequency);
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

    public void requestSetRFpower(int level, int mode) {
        if (passiveReader != null) {
            passiveReader.setRFpower(level, mode);
        }
    }

    public void requestSetShutdownTime(int time) {
        if (passiveReader != null) {
            passiveReader.setShutdownTime(time);
        }
    }

    public void requestSound(int frequency, int step, int duration, int interval, int repetition) {
        if (passiveReader != null) {
            passiveReader.sound(frequency, step, duration, interval, repetition);
        }
    }

    public void requestTestAvailability() {
        if (passiveReader != null) {
            passiveReader.testAvailability();
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
            byte ID[] = new byte[16];
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

    private byte[] hexStringToByte(String hexData) throws IndexOutOfBoundsException, NumberFormatException {
        byte[] data = new byte[hexData.length() / 2];

        for (int i = 0; i < data.length; i++) {
            String chunk = hexData.substring(i * 2, (i + 1) * 2);
            data[i] = (byte) Integer.valueOf(chunk, 16).intValue();
        }

        return data;
    }
}