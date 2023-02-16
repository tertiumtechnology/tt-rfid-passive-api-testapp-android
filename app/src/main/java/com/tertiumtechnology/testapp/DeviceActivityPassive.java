package com.tertiumtechnology.testapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tertiumtechnology.api.rfidpassiveapilib.EPC_tag;
import com.tertiumtechnology.api.rfidpassiveapilib.PassiveReader;
import com.tertiumtechnology.api.rfidpassiveapilib.Tag;
import com.tertiumtechnology.api.rfidpassiveapilib.ZhagaReader;
import com.tertiumtechnology.api.rfidpassiveapilib.listener.AbstractReaderListener;
import com.tertiumtechnology.api.rfidpassiveapilib.listener.AbstractResponseListener;
import com.tertiumtechnology.api.rfidpassiveapilib.listener.AbstractZhagaListener;
import com.tertiumtechnology.testapp.util.BleUtil;
import com.tertiumtechnology.testapp.util.Chain;
import com.tertiumtechnology.testapp.util.adapters.InventoryTagsListAdapter;
import com.tertiumtechnology.testapp.util.dialogs.KillTagDialogFragment;
import com.tertiumtechnology.testapp.util.dialogs.LockTagDialogFragment;
import com.tertiumtechnology.testapp.util.dialogs.ReadTagDialogFragment;
import com.tertiumtechnology.testapp.util.dialogs.SetNameDialogFragment;
import com.tertiumtechnology.testapp.util.dialogs.TransparentDialogFragment;
import com.tertiumtechnology.testapp.util.dialogs.TransparentDialogFragment.TransparentCommandListener;
import com.tertiumtechnology.testapp.util.dialogs.TunnelDialogFragment;
import com.tertiumtechnology.testapp.util.dialogs.WriteAccessPasswordDialogFragment;
import com.tertiumtechnology.testapp.util.dialogs.WriteKillPasswordDialogFragment;
import com.tertiumtechnology.testapp.util.dialogs.WriteTagDialogFragment;
import com.tertiumtechnology.testapp.util.dialogs.WriteUserMemoryDialogFragment;
import com.tertiumtechnology.testapp.util.dialogs.WriteUserMemoryDialogFragment.WriteUserMemoryListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class DeviceActivityPassive extends AppCompatActivity implements ReadTagDialogFragment.ReadTagListener,
        WriteTagDialogFragment.WriteTagListener, LockTagDialogFragment.LockTagListener,
        WriteAccessPasswordDialogFragment.WriteAccessPasswordListener, KillTagDialogFragment.KillTagListener,
        WriteKillPasswordDialogFragment.WriteKillPasswordListener, TunnelDialogFragment.TunnelListener,
        SetNameDialogFragment.SetNameListener, WriteUserMemoryListener,
        TransparentCommandListener {

    interface CommandOperation {
        void execute();
    }

    public class BleReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.i(TAG, intent.getAction());

            if (BleServicePassive.INTENT_ACTION_DEVICE_CONNECTED.equals(intent
                    .getAction())) {
                connectionState = ConnectionState.CONNECTED;
                supportInvalidateOptionsMenu();

                initTextView.setText("");
                readTextView.setText("");

                categoriesSpinner.setEnabled(true);
                categoriesSpinner.setSelection(0);
                commandSpinner.setEnabled(true);

                clearTags();

                startInitalOperations();
            }
            else if (BleServicePassive.INTENT_ACTION_DEVICE_DISCONNECTED.equals
                    (intent.getAction())) {
                stopAllOperations();
                disableSendCommand();

                connectionState = ConnectionState.DISCONNECTED;
                supportInvalidateOptionsMenu();
            }
            else if (BleServicePassive.INTENT_ACTION_DEVICE_CONNECTION_OPERATION_FAILED.equals(intent.getAction())) {
                stopAllOperations();
                disableSendCommand();

                connectionState = ConnectionState.DISCONNECTED;
                supportInvalidateOptionsMenu();

                String stringExtra = intent.getStringExtra(BleServicePassive.INTENT_EXTRA_DATA_VALUE);

                composeAndAppendInputCommandMsg(stringExtra, getMsgColor(R.color.colorErrorText));
            }
            else if (BleServicePassive.INTENT_ACTION_DEVICE_COMMAND_CALLBACK
                    .equals(intent.getAction())) {

                Map<Object, Object> dataRead = getDataMap(intent);

                if (dataRead != null) {
                    int command = (int) dataRead.get(BleServicePassive
                            .INTENT_EXTRA_DATA_COMMAND_CALLBACK);

                    manageCommandCallback(command, dataRead);
                }
            }
            else if (BleServicePassive.INTENT_ACTION_DEVICE_COMMAND_RESULT
                    .equals(intent.getAction())) {
                Map<Object, Object> dataRead = getDataMap(intent);

                int commandCode = (int) dataRead.get(BleServicePassive
                        .INTENT_EXTRA_DATA_COMMAND_RESULT);

                manageCommandResult(commandCode, dataRead.get(BleServicePassive
                        .INTENT_EXTRA_DATA_ERROR));
            }
            else if (BleServicePassive
                    .INTENT_ACTION_DEVICE_COMMAND_CALLBACK_RESULT.equals(intent.getAction())) {
                Map<Object, Object> dataRead = getDataMap(intent);

                int commandCode = (int) dataRead.get(BleServicePassive
                        .INTENT_EXTRA_DATA_COMMAND_CALLBACK_RESULT);

                manageCommandCallback(commandCode, dataRead);

                manageCommandResult(commandCode, dataRead.get(BleServicePassive
                        .INTENT_EXTRA_DATA_ERROR));
            }
            else if (BleServicePassive
                    .INTENT_ACTION_DEVICE_EVENT_RESULT.equals(intent.getAction())) {
                Map<Object, Object> dataRead = getDataMap(intent);

                manageEventResult(dataRead);
            }
            else if (BleServicePassive
                    .INTENT_ACTION_DEVICE_EVENT_TRIGGERED.equals(intent.getAction())) {
                Map<Object, Object> dataRead = getDataMap(intent);

                if (dataRead != null) {
                    String event = (String) dataRead.get(BleServicePassive.INTENT_EXTRA_DATA_EVENT_TRIGGERED);

                    manageEventTriggered(event, dataRead);
                }
            }
            else {
                throw new UnsupportedOperationException(getString(R.string.error_invalid_action));
            }
        }

        private Map<Object, Object> getDataMap(Intent intent) {
            Map<Object, Object> dataRead;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                dataRead = (Map<Object, Object>) intent.getSerializableExtra(BleServicePassive.INTENT_EXTRA_DATA_VALUE, Serializable.class);
            }
            else{
                dataRead = (Map<Object, Object>) intent.getSerializableExtra(BleServicePassive.INTENT_EXTRA_DATA_VALUE);
            }
            return dataRead;
        }

        private void manageCommandCallback(int command, Map data) {
            switch (command) {
                case AbstractReaderListener.GET_BATTERY_STATUS_COMMAND:
                    updateBatteryStatus((int) data.get(BleServicePassive
                            .INTENT_EXTRA_DATA_BATTERY_STATUS));
                    break;
                case AbstractReaderListener.GET_BATTERY_LEVEL_COMMAND:
                    updateBatteryLevel((float) data.get(BleServicePassive
                            .INTENT_EXTRA_DATA_BATTERY_LEVEL));
                    break;
                case AbstractReaderListener.GET_FIRMWARE_VERSION_COMMAND:
                    String firmwareVersion = (String) data.get(BleServicePassive
                            .INTENT_EXTRA_DATA_FIRMWARE_VERSION);
                    composeAndAppendInitMsg(getString(R.string.firmware_version_value, firmwareVersion)
                            , getMsgColor(R.color.colorReadText));
                    break;

                case AbstractReaderListener.TEST_AVAILABILITY_COMMAND:
                    boolean available = (boolean) data.get(BleServicePassive
                            .INTENT_EXTRA_DATA_AVAILABILITY_STATUS);

                    String testAvailabiliyValue = available ? getString(R.string
                            .availability_status_available) : getString(R.string
                            .availability_status_not_available);

                    composeAndAppendInputCommandMsg(getString(R.string.test_availability_value,
                            testAvailabiliyValue)
                            , getMsgColor(R.color.colorReadText));
                    break;
                case AbstractReaderListener.GET_SHUTDOWN_TIME_COMMAND:
                    int time = (int) data.get(BleServicePassive
                            .INTENT_EXTRA_DATA_SHUTDOWN_TIME);

                    composeAndAppendInputCommandMsg(getString(R.string.get_shutdown_time_value, time),
                            getMsgColor(R
                                    .color.colorReadText));
                    break;
                case AbstractReaderListener.GET_RF_POWER_COMMAND:
                    int level = (int) data.get(BleServicePassive
                            .INTENT_EXTRA_DATA_RF_POWER_LEVEL);
                    int mode = (int) data.get(BleServicePassive
                            .INTENT_EXTRA_DATA_RF_POWER_MODE);

                    composeAndAppendInputCommandMsg(getString(R.string.get_rf_power_values, level, mode),
                            getMsgColor(R
                                    .color.colorReadText));
                    break;
                case AbstractReaderListener.GET_ISO15693_OPTION_BITS_COMMAND:
                    String optionBits = (String) data.get(BleServicePassive
                            .INTENT_EXTRA_DATA_ISO15693_OPTION_BITS);

                    composeAndAppendInputCommandMsg(getString(R.string.get_iso15693_option_bits_value,
                            optionBits),
                            getMsgColor(R
                                    .color.colorReadText));
                    break;
                case AbstractReaderListener.GET_ISO15693_EXTENSION_FLAG_COMMAND:
                    boolean flag = (boolean) data.get(BleServicePassive
                            .INTENT_EXTRA_DATA_ISO15693_EXTENSION_FLAG_FLAG);
                    boolean permanent = (boolean) data.get(BleServicePassive
                            .INTENT_EXTRA_DATA_ISO15693_EXTENSION_FLAG_PERMANENT);

                    composeAndAppendInputCommandMsg(getString(R.string.get_iso15693_extension_flag_values,
                            flag, permanent),
                            getMsgColor(R.color.colorReadText));
                    break;
                case AbstractReaderListener.GET_ISO15693_BITRATE_COMMAND:
                    int bitrate = (int) data.get(BleServicePassive
                            .INTENT_EXTRA_DATA_ISO15693_BITRATE_BITRATE);
                    boolean permanentBitrate = (boolean) data.get(BleServicePassive
                            .INTENT_EXTRA_DATA_ISO15693_BITRATE_PERMANENT);

                    composeAndAppendInputCommandMsg(getString(R.string.get_iso15693_bitrate_values, bitrate,
                            permanentBitrate),
                            getMsgColor(R.color.colorReadText));
                    break;
                case AbstractReaderListener.ISO15693_TUNNEL_COMMAND:
                case AbstractReaderListener.ISO15693_ENCRYPTEDTUNNEL_COMMAND:
                    String tunnelData = (String) data.get(BleServicePassive
                            .INTENT_EXTRA_DATA_ISO15693_TUNNEL_DATA);

                    composeAndAppendInputCommandMsg(getString(R.string.iso15693_tunnel_data_value, tunnelData),
                            getMsgColor(R.color.colorReadText));
                    break;

                case AbstractReaderListener.GET_EPC_FREQUENCY_COMMAND:
                    int frequency = (int) data.get(BleServicePassive
                            .INTENT_EXTRA_DATA_EPC_FREQUENCY);

                    composeAndAppendInputCommandMsg(getString(R.string.get_epc_frequency_values, frequency),
                            getMsgColor(R.color.colorReadText));
                    break;
                case AbstractReaderListener.GET_SECURITY_LEVEL_COMMAND:
                    int securityLevel = (int) data.get(BleServicePassive
                            .INTENT_EXTRA_DATA_SECURITY_LEVEL);

                    composeAndAppendInputCommandMsg(getString(R.string.get_security_level_values, securityLevel),
                            getMsgColor(R.color.colorReadText));
                    break;
                case AbstractReaderListener.INVENTORY_COMMAND:
                    Tag tag = (Tag) data.get(BleServicePassive
                            .INTENT_EXTRA_DATA_INVENTORY_TAG);

                    addTag(tag);

                    String text = tag.toString();
                    if (tag instanceof EPC_tag) {
                        text += " RSSI = " + ((EPC_tag) tag).getRSSI();
                    }

                    composeAndAppendInputCommandMsg(getString(R.string.inventory_tag_discovered, text),
                            getMsgColor(R.color.colorReadText));

                    break;

                case AbstractResponseListener.WRITEACCESSPASSWORD_COMMAND:
                case AbstractResponseListener.WRITEKILLPASSWORD_COMMAND:
                    if (data.get(BleServicePassive.INTENT_EXTRA_DATA_ERROR) == null) {
                        composeAndAppendInputCommandMsg(getString(R.string.password_written),
                                getMsgColor(R.color.colorReadText));
                    }
                    break;

                case AbstractResponseListener.READ_COMMAND:
                    Object dataRead = data.get(BleServicePassive
                            .INTENT_EXTRA_DATA_READ_VALUE);
                    if (dataRead != null) {
                        composeAndAppendInputCommandMsg(getString(R.string.data_read, dataRead),
                                getMsgColor(R.color.colorReadText));
                    }
                    break;
                case AbstractResponseListener.WRITE_COMMAND:
                    if (data.get(BleServicePassive.INTENT_EXTRA_DATA_ERROR) ==
                            null) {
                        composeAndAppendInputCommandMsg(getString(R.string.data_written),
                                getMsgColor(R.color.colorReadText));
                    }
                    break;
                case AbstractResponseListener.LOCK_COMMAND:
                    if (data.get(BleServicePassive.INTENT_EXTRA_DATA_ERROR) ==
                            null) {
                        composeAndAppendInputCommandMsg(getString(R.string.tag_locked),
                                getMsgColor(R.color.colorReadText));
                    }
                    break;
                case AbstractResponseListener.READ_TID_COMMAND:
                    Object tidRead = data.get(BleServicePassive
                            .INTENT_EXTRA_DATA_READ_TID_VALUE);
                    if (tidRead != null) {
                        composeAndAppendInputCommandMsg(getString(R.string.tid_read, tidRead),
                                getMsgColor(R.color.colorReadText));
                    }
                    break;
                case AbstractResponseListener.WRITEID_COMMAND:
                    if (data.get(BleServicePassive.INTENT_EXTRA_DATA_ERROR) ==
                            null) {
                        composeAndAppendInputCommandMsg(getString(R.string.tag_id_written),
                                getMsgColor(R.color.colorReadText));
                    }
                    break;
                case AbstractResponseListener.KILL_COMMAND:
                    if (data.get(BleServicePassive.INTENT_EXTRA_DATA_ERROR) ==
                            null) {
                        composeAndAppendInputCommandMsg(getString(R.string.tag_killed),
                                getMsgColor(R.color.colorReadText));
                    }
                    break;

                // new
                // common commnands
                case AbstractReaderListener.GET_DEVICE_NAME_COMMAND:
                    String deviceName = (String) data.get(BleServicePassive
                            .INTENT_EXTRA_DATA_DEVICE_NAME);

                    composeAndAppendInputCommandMsg(getString(R.string.get_name_value, deviceName),
                            getMsgColor(R.color.colorReadText));
                    break;

                // ble commands
                case AbstractReaderListener.GET_BLE_FIRMWARE_VERSION_COMMAND:
                    String bleFirmwareVersion =
                            (String) data.get(BleServicePassive.INTENT_EXTRA_DATA_BLE_FIRMWARE_VERSION);
                    composeAndAppendInputCommandMsg(getString(R.string.ble_firmware_version_value, bleFirmwareVersion)
                            , getMsgColor(R.color.colorReadText));

                    break;
                case AbstractReaderListener.GET_ADVERTISING_INTERVAL_COMMAND:
                    int advertisingInterval =
                            (int) data.get(BleServicePassive.INTENT_EXTRA_DATA_ADVERTISING_INTERVAL);
                    composeAndAppendInputCommandMsg(getString(R.string.get_advertising_interval_value,
                            advertisingInterval)
                            , getMsgColor(R.color.colorReadText));

                    break;
                case AbstractReaderListener.GET_BLE_POWER_COMMAND:
                    int blePower = (int) data.get(BleServicePassive.INTENT_EXTRA_DATA_BLE_POWER);
                    composeAndAppendInputCommandMsg(getString(R.string.ble_power_value, blePower)
                            , getMsgColor(R.color.colorReadText));

                    break;
                case AbstractReaderListener.GET_CONNECTION_INTERVAL_COMMAND:
                    float min = (float) data.get(BleServicePassive.INTENT_EXTRA_DATA_CONNECTION_INTERVAL_MIN);
                    float max = (float) data.get(BleServicePassive.INTENT_EXTRA_DATA_CONNECTION_INTERVAL_MAX);

                    composeAndAppendInputCommandMsg(getString(R.string.connection_interval_values, min, max),
                            getMsgColor(R.color.colorReadText));

                    break;
                case AbstractReaderListener.GET_SLAVE_LATENCY_COMMAND:
                    int latency = (int) data.get(BleServicePassive.INTENT_EXTRA_DATA_SLAVE_LATENCY);
                    composeAndAppendInputCommandMsg(getString(R.string.slave_latency_value, latency),
                            getMsgColor(R.color.colorReadText));
                    break;
                case AbstractReaderListener.GET_SUPERVISION_TIMEOUT_COMMAND:
                    int timeout = (int) data.get(BleServicePassive.INTENT_EXTRA_DATA_SUPERVISION_TIMEOUT);
                    composeAndAppendInputCommandMsg(getString(R.string.supervision_timeout_value, timeout),
                            getMsgColor(R.color.colorReadText));
                    break;
                case AbstractReaderListener.GET_CONNECTION_INTERVAL_AND_MTU_COMMAND:
                    float connectionInterval =
                            (float) data.get(BleServicePassive.INTENT_EXTRA_DATA_CONNECTION_INTERVAL);
                    int mtu = (int) data.get(BleServicePassive.INTENT_EXTRA_DATA_MTU);

                    composeAndAppendInputCommandMsg(getString(R.string.connection_interval_and_mtu_values,
                            connectionInterval, mtu),
                            getMsgColor(R.color.colorReadText));
                    break;
                case AbstractReaderListener.GET_MAC_ADDRESS_COMMAND:
                    String macAddress = (String) data.get(BleServicePassive.INTENT_EXTRA_DATA_MAC_ADDRESS);
                    composeAndAppendInputCommandMsg(getString(R.string.mac_address_value, macAddress),
                            getMsgColor(R.color.colorReadText));
                    break;

                // memory
                case AbstractReaderListener.READ_USER_MEMORY_COMMAND:
                    String userMemory = (String) data.get(BleServicePassive.INTENT_EXTRA_DATA_USER_MEMORY);
                    composeAndAppendInputCommandMsg(getString(R.string.user_memory_value, userMemory),
                            getMsgColor(R.color.colorReadText));
                    break;

                // zhaga
                case AbstractZhagaListener.ZHAGA_GET_HMI_SUPPORT_COMMAND:
                    int ledColor = (int) data.get(BleServicePassive.INTENT_EXTRA_DATA_HMI_LED_COLOR);
                    int soundVibration = (int) data.get(BleServicePassive.INTENT_EXTRA_DATA_HMI_SOUND_VIBRATION);
                    int buttonNumber = (int) data.get(BleServicePassive.INTENT_EXTRA_DATA_HMI_BUTTON_NUMBER);

                    composeAndAppendInputCommandMsg(getString(R.string.hmi_support_values, ledColor, soundVibration,
                            buttonNumber),
                            getMsgColor(R.color.colorReadText));
                    break;
                case AbstractZhagaListener.ZHAGA_GET_INVENTORY_SOUND_COMMAND:
                    int inventorySoundFrequency =
                            (int) data.get(BleServicePassive.INTENT_EXTRA_DATA_INVENTORY_SOUND_FREQUENCY);
                    int inventorySoundOnTime =
                            (int) data.get(BleServicePassive.INTENT_EXTRA_DATA_INVENTORY_SOUND_ON_TIME);
                    int inventorySoundOffTime =
                            (int) data.get(BleServicePassive.INTENT_EXTRA_DATA_INVENTORY_SOUND_OFF_TIME);
                    int inventorySoundRepetition =
                            (int) data.get(BleServicePassive.INTENT_EXTRA_DATA_INVENTORY_SOUND_REPETITION);

                    composeAndAppendInputCommandMsg(getString(R.string.sound_for_inventory_values,
                            inventorySoundFrequency,
                            inventorySoundOnTime, inventorySoundOffTime, inventorySoundRepetition),
                            getMsgColor(R.color.colorReadText));
                    break;
                case AbstractZhagaListener.ZHAGA_GET_COMMAND_SOUND_COMMAND:
                    int commandSoundFrequency =
                            (int) data.get(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_SOUND_FREQUENCY);
                    int commandSoundOnTime =
                            (int) data.get(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_SOUND_ON_TIME);
                    int commandSoundOffTime =
                            (int) data.get(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_SOUND_OFF_TIME);
                    int commandSoundRepetition =
                            (int) data.get(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_SOUND_REPETITION);

                    composeAndAppendInputCommandMsg(getString(R.string.sound_for_command_values, commandSoundFrequency,
                            commandSoundOnTime, commandSoundOffTime, commandSoundRepetition),
                            getMsgColor(R.color.colorReadText));
                    break;
                case AbstractZhagaListener.ZHAGA_GET_ERROR_SOUND_COMMAND:
                    int errorSoundFrequency =
                            (int) data.get(BleServicePassive.INTENT_EXTRA_DATA_ERROR_SOUND_FREQUENCY);
                    int errorSoundOnTime =
                            (int) data.get(BleServicePassive.INTENT_EXTRA_DATA_ERROR_SOUND_ON_TIME);
                    int errorSoundOffTime =
                            (int) data.get(BleServicePassive.INTENT_EXTRA_DATA_ERROR_SOUND_OFF_TIME);
                    int errorSoundRepetition =
                            (int) data.get(BleServicePassive.INTENT_EXTRA_DATA_ERROR_SOUND_REPETITION);

                    composeAndAppendInputCommandMsg(getString(R.string.sound_for_error_values, errorSoundFrequency,
                            errorSoundOnTime, errorSoundOffTime, errorSoundRepetition),
                            getMsgColor(R.color.colorReadText));
                    break;

                case AbstractZhagaListener.ZHAGA_GET_INVENTORY_LED_COMMAND:
                    int inventoryLightColor =
                            (int) data.get(BleServicePassive.INTENT_EXTRA_DATA_INVENTORY_LED_LIGHT_COLOR);
                    int inventoryLightOnTime =
                            (int) data.get(BleServicePassive.INTENT_EXTRA_DATA_INVENTORY_LED_LIGHT_ON_TIME);
                    int inventoryLightOffTime =
                            (int) data.get(BleServicePassive.INTENT_EXTRA_DATA_INVENTORY_LED_LIGHT_OFF_TIME);
                    int inventoryLightRepetition =
                            (int) data.get(BleServicePassive.INTENT_EXTRA_DATA_INVENTORY_LED_LIGHT_REPETITION);

                    composeAndAppendInputCommandMsg(getString(R.string.led_for_inventory_values,
                            inventoryLightColor, inventoryLightOnTime, inventoryLightOffTime, inventoryLightRepetition),
                            getMsgColor(R.color.colorReadText));
                    break;
                case AbstractZhagaListener.ZHAGA_GET_COMMAND_LED_COMMAND:
                    int commandLightColor =
                            (int) data.get(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_LED_LIGHT_COLOR);
                    int commandLightOnTime =
                            (int) data.get(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_LED_LIGHT_ON_TIME);
                    int commandLightOffTime =
                            (int) data.get(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_LED_LIGHT_OFF_TIME);
                    int commandLightRepetition =
                            (int) data.get(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_LED_LIGHT_REPETITION);

                    composeAndAppendInputCommandMsg(getString(R.string.led_for_command_values, commandLightColor,
                            commandLightOnTime, commandLightOffTime, commandLightRepetition),
                            getMsgColor(R.color.colorReadText));
                    break;
                case AbstractZhagaListener.ZHAGA_GET_ERROR_LED_COMMAND:
                    int errorLightColor =
                            (int) data.get(BleServicePassive.INTENT_EXTRA_DATA_ERROR_LED_LIGHT_COLOR);
                    int errorLightOnTime =
                            (int) data.get(BleServicePassive.INTENT_EXTRA_DATA_ERROR_LED_LIGHT_ON_TIME);
                    int errorLightOffTime =
                            (int) data.get(BleServicePassive.INTENT_EXTRA_DATA_ERROR_LED_LIGHT_OFF_TIME);
                    int errorLightRepetition =
                            (int) data.get(BleServicePassive.INTENT_EXTRA_DATA_ERROR_LED_LIGHT_REPETITION);

                    composeAndAppendInputCommandMsg(getString(R.string.led_for_error_values, errorLightColor,
                            errorLightOnTime, errorLightOffTime, errorLightRepetition),
                            getMsgColor(R.color.colorReadText));
                    break;

                case AbstractZhagaListener.ZHAGA_GET_INVENTORY_VIBRATION_COMMAND:
                    int inventoryVibrationOnTime =
                            (int) data.get(BleServicePassive.INTENT_EXTRA_DATA_INVENTORY_VIBRATION_ON_TIME);
                    int inventoryVibrationOffTime =
                            (int) data.get(BleServicePassive.INTENT_EXTRA_DATA_INVENTORY_VIBRATION_OFF_TIME);
                    int inventoryVibrationRepetition =
                            (int) data.get(BleServicePassive.INTENT_EXTRA_DATA_INVENTORY_VIBRATION_REPETITION);

                    composeAndAppendInputCommandMsg(getString(R.string.vibration_for_inventory_values,
                            inventoryVibrationOnTime, inventoryVibrationOffTime, inventoryVibrationRepetition),
                            getMsgColor(R.color.colorReadText));
                    break;
                case AbstractZhagaListener.ZHAGA_GET_COMMAND_VIBRATION_COMMAND:
                    int commandVibrationOnTime =
                            (int) data.get(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_VIBRATION_ON_TIME);
                    int commandVibrationOffTime =
                            (int) data.get(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_VIBRATION_OFF_TIME);
                    int commandVibrationRepetition =
                            (int) data.get(BleServicePassive.INTENT_EXTRA_DATA_COMMAND_VIBRATION_REPETITION);

                    composeAndAppendInputCommandMsg(getString(R.string.vibration_for_command_values,
                            commandVibrationOnTime, commandVibrationOffTime, commandVibrationRepetition),
                            getMsgColor(R.color.colorReadText));
                    break;
                case AbstractZhagaListener.ZHAGA_GET_ERROR_VIBRATION_COMMAND:
                    int errorVibrationOnTime =
                            (int) data.get(BleServicePassive.INTENT_EXTRA_DATA_ERROR_VIBRATION_ON_TIME);
                    int errorVibrationOffTime =
                            (int) data.get(BleServicePassive.INTENT_EXTRA_DATA_ERROR_VIBRATION_OFF_TIME);
                    int errorVibrationRepetition =
                            (int) data.get(BleServicePassive.INTENT_EXTRA_DATA_ERROR_VIBRATION_REPETITION);

                    composeAndAppendInputCommandMsg(getString(R.string.vibration_for_error_values,
                            errorVibrationOnTime, errorVibrationOffTime, errorVibrationRepetition),
                            getMsgColor(R.color.colorReadText));
                    break;

                case AbstractZhagaListener.ZHAGA_GET_ACTIVATED_BUTTON_COMMAND:
                    int activatedButton =
                            (int) data.get(BleServicePassive.INTENT_EXTRA_DATA_ACTIVATED_BUTTON);

                    composeAndAppendInputCommandMsg(getString(R.string.get_activated_button_value, activatedButton),
                            getMsgColor(R.color.colorReadText));
                    break;

                case AbstractZhagaListener.ZHAGA_GET_RF_COMMAND:
                    boolean rfOn =
                            (boolean) data.get(BleServicePassive.INTENT_EXTRA_DATA_RF_ON);

                    composeAndAppendInputCommandMsg(getString(R.string.get_rf_value, rfOn),
                            getMsgColor(R.color.colorReadText));
                    break;
                case AbstractZhagaListener.ZHAGA_GET_RF_ONOFF_COMMAND:
                    int rfPower = (int) data.get(BleServicePassive.INTENT_EXTRA_DATA_RF_ON_OFF_RF_POWER);
                    int rfOffTimeout = (int) data.get(BleServicePassive.INTENT_EXTRA_DATA_RF_ON_OFF_RF_OFF_TIMEOUT);
                    int rfOnPreactivation =
                            (int) data.get(BleServicePassive.INTENT_EXTRA_DATA_RF_ON_OFF_RF_ON_PREACTIVATION);

                    composeAndAppendInputCommandMsg(getString(R.string.get_rf_on_off_values, rfPower, rfOffTimeout,
                            rfOnPreactivation), getMsgColor(R.color.colorReadText));
                    break;
                case AbstractZhagaListener.ZHAGA_GET_AUTOOFF_COMMAND:
                    int offTime =
                            (int) data.get(BleServicePassive.INTENT_EXTRA_DATA_AUTO_OFF_TIME);

                    composeAndAppendInputCommandMsg(getString(R.string.get_auto_off_value, offTime),
                            getMsgColor(R.color.colorReadText));
                    break;

                case AbstractZhagaListener.ZHAGA_TRANSPARENT_COMMAND:
                    String transparentAnswer =
                            (String) data.get(BleServicePassive.INTENT_EXTRA_DATA_TRANSPARENT_ANSWER);

                    composeAndAppendInputCommandMsg(getString(R.string.transparent_value, transparentAnswer),
                            getMsgColor(R.color.colorReadText));
                    break;
            }
        }

        private void manageCommandResult(int commandCode, Object errorData) {
            boolean isRepeatingCommand = repeatingCommandCodes.contains
                    (commandCode);

            boolean isInitalCommand = initialCommandCodes.contains
                    (commandCode);

            if (errorData != null) {
                String errorMsg = getString(R.string.result_command_error, commandCode, errorData);

                if (isInitalCommand) {
                    composeAndAppendInitMsg(errorMsg, getMsgColor(R.color.colorErrorText));
                }
                else {
                    composeAndAppendInputCommandMsg(errorMsg, getMsgColor(R.color.colorErrorText));
                }
            }
            else if (!isRepeatingCommand) {
                // append messages to scroll windows only if not in repeating operations
                String resultMsg = getString(R.string.result_command_no_error, commandCode);

                if (isInitalCommand && !initialCommandChain.hasEnded()) {
                    composeAndAppendInitMsg(resultMsg, getMsgColor(R.color.colorReadText));
                }
                else {
                    composeAndAppendInputCommandMsg(resultMsg, getMsgColor(R.color.colorReadText));
                }
            }

            if (isInitalCommand && !initialCommandChain.hasEnded()) {
                initialCommandChain.executeNext();
            }
            else if (isRepeatingCommand) {
                repeatingCommandChain.executeNext();
            }
            else {
                allowSendCommand();
            }
        }

        private void manageEventResult(Map data) {
            int eventNumber = (int) data.get(BleServicePassive.INTENT_EXTRA_DATA_EVENT_RESULT_NUMBER);
            int eventCode = (int) data.get(BleServicePassive.INTENT_EXTRA_DATA_EVENT_RESULT_CODE);

            String resultMsg = getString(R.string.result_event, eventCode, eventNumber);
            composeAndAppendInputEventMsg(resultMsg, getMsgColor(R.color.colorEventText));
        }

        private void manageEventTriggered(String event, Map data) {

            if (BleServicePassive.EVENT_BUTTON_EVENT.equals(event)) {
                int button = (int) data.get(BleServicePassive.INTENT_EXTRA_DATA_BUTTON);
                int time = (int) data.get(BleServicePassive.INTENT_EXTRA_DATA_TIME);

                composeAndAppendInputEventMsg(getString(R.string.event_button_values,
                        button, time), getMsgColor(R.color.colorEventText));
            }
        }
    }

    private enum ConnectionState {
        DISCONNECTED, CONNECTING, CONNECTED
    }

    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final String TAG = DeviceActivityPassive.class.getSimpleName();

    private static IntentFilter getBleIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleServicePassive.INTENT_ACTION_DEVICE_CONNECTED);
        intentFilter.addAction(BleServicePassive.INTENT_ACTION_DEVICE_DISCONNECTED);
        intentFilter.addAction(BleServicePassive.INTENT_ACTION_DEVICE_CONNECTION_OPERATION_FAILED);
        intentFilter.addAction(BleServicePassive.INTENT_ACTION_DEVICE_COMMAND_CALLBACK);
        intentFilter.addAction(BleServicePassive.INTENT_ACTION_DEVICE_COMMAND_RESULT);
        intentFilter.addAction(BleServicePassive.INTENT_ACTION_DEVICE_COMMAND_CALLBACK_RESULT);
        intentFilter.addAction(BleServicePassive.INTENT_ACTION_DEVICE_EVENT_TRIGGERED);
        intentFilter.addAction(BleServicePassive.INTENT_ACTION_DEVICE_EVENT_RESULT);
        return intentFilter;
    }

    private BleReceiver bleReceiver;
    private ServiceConnection bleServiceConnection;
    private ConnectionState connectionState;
    private BleServicePassive bleServicePassive;

    private String deviceAddress;
    private boolean enableDisconnect;

    private ScrollView initScrollView;
    private AppCompatTextView initTextView;

    private ScrollView readScrollView;
    private AppCompatTextView readTextView;

    private AppCompatTextView availabilityStatusTextView;
    private AppCompatTextView batteryLevelTextView;
    private AppCompatTextView batteryStatusTextView;

    private Map<String, Map<String, CommandOperation>> commandCategoriesMap;
    private Map<String, CommandOperation> commandMap;
    private AppCompatSpinner categoriesSpinner;
    private AppCompatSpinner commandSpinner;
    private ProgressBar sendCommandProgressBar;

    private Chain initialCommandChain;
    private ArrayList<Integer> initialCommandCodes;

    private Chain repeatingCommandChain;
    private ArrayList<Integer> repeatingCommandCodes;

    private InventoryTagsListAdapter inventoryTagsListAdapter;
    private Tag selectedTag;

    private ActivityResultLauncher<Intent> activityResultLauncher;


    public void doSendCommand(String commandName) {

        CommandOperation command = commandMap.get(commandName);

        if (bleServicePassive != null && command != null) {
            composeAndAppendInputCommandMsg(commandName, getMsgColor(R.color.colorWriteText));

            disallowSendCommand();

            command.execute();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_device, menu);
        menu.findItem(R.id.menu_connect).setVisible(true);
        menu.findItem(R.id.menu_connecting).setVisible(true);
        menu.findItem(R.id.menu_disconnect).setVisible(false);
        menu.findItem(R.id.menu_refresh).setVisible(false);

        return true;
    }

    @Override
    public void onKillTag(String hexPassword) {
        if (bleServicePassive != null) {
            if (selectedTag != null) {
                composeAndAppendInputCommandMsg(getString(R.string.killing_tag, selectedTag.toString()),
                        getMsgColor(R.color.colorReadText));
                bleServicePassive.requestKill((EPC_tag) selectedTag, hexPassword);
            }
            else {
                composeAndAppendInputCommandMsg(getString(R.string.invalid_command_no_tag_found),
                        getMsgColor(R.color.colorErrorText));
                allowSendCommand();
            }
        }
    }

    @Override
    public void onLockTag(int lockType, String hexPassword) {
        if (bleServicePassive != null) {
            if (selectedTag != null) {
                composeAndAppendInputCommandMsg(getString(R.string.locking_tag, selectedTag.toString()),
                        getMsgColor(R.color.colorReadText));
                bleServicePassive.requestLock(selectedTag, lockType, hexPassword);
            }
            else {
                composeAndAppendInputCommandMsg(getString(R.string.invalid_command_no_tag_found),
                        getMsgColor(R.color.colorErrorText));
                allowSendCommand();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connect:
                bleServicePassive.connect(deviceAddress);
                connectionState = ConnectionState.CONNECTING;
                supportInvalidateOptionsMenu();
                return true;
            case R.id.menu_disconnect:
                bleServicePassive.disconnect();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (connectionState == ConnectionState.DISCONNECTED) {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_connecting).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(null);
            menu.findItem(R.id.menu_refresh).setVisible(false);
        }
        else if (connectionState == ConnectionState.CONNECTING) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_connecting).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);

            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.actionbar_indeterminate_progress);
            menu.findItem(R.id.menu_refresh).setVisible(true);
        }
        else if (connectionState == ConnectionState.CONNECTED) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_connecting).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);

            menu.findItem(R.id.menu_refresh).setActionView(null);
            menu.findItem(R.id.menu_refresh).setVisible(false);
        }

        if (menu.findItem(R.id.menu_disconnect).isVisible()) {
            menu.findItem(R.id.menu_disconnect).setEnabled(enableDisconnect);
        }

        return true;
    }

    @Override
    public void onReadTag(int address, int block) {
        if (bleServicePassive != null) {
            if (selectedTag != null) {
                composeAndAppendInputCommandMsg(getString(R.string.reading_tag, selectedTag.toString
                                ()),
                        getMsgColor(R.color.colorReadText));
                bleServicePassive.requestRead(selectedTag, address, block);
            }
            else {
                composeAndAppendInputCommandMsg(getString(R.string.invalid_command_no_tag_found),
                        getMsgColor(R.color.colorErrorText));
                allowSendCommand();
            }
        }
    }

    @Override
    public void onSetName(String name) {
        if (bleServicePassive != null) {
            if (!TextUtils.isEmpty(name)) {
                composeAndAppendInputCommandMsg(getString(R.string.setting_name, name),
                        getMsgColor(R.color.colorReadText));
                bleServicePassive.requestSetName(name);
            }
            else {
                composeAndAppendInputCommandMsg(getString(R.string.invalid_command_name_not_defined),
                        getMsgColor(R.color.colorErrorText));
                allowSendCommand();
            }
        }
    }

    @Override
    public void onStartTunnel(String hexCommand, boolean encrypted, String hexEncryptedFlag) {
        if (bleServicePassive != null) {
            composeAndAppendInputCommandMsg(getString(R.string.starting_iso15693_tunnel),
                    getMsgColor(R.color.colorReadText));
            bleServicePassive.requestStartTunnel(hexCommand, encrypted, hexEncryptedFlag);
        }
    }

    @Override
    public void onTransparentCommand(String hexCommand) {
        if (bleServicePassive != null) {
            composeAndAppendInputCommandMsg(getString(R.string.writing_transparent_command, hexCommand), getMsgColor
                    (R.color.colorReadText));
            bleServicePassive.requestTransparent(hexCommand);
        }
    }

    @Override
    public void onWriteAccessPassword(String hexOldPassword, String hexNewPassword) {
        if (bleServicePassive != null) {
            if (selectedTag != null) {
                composeAndAppendInputCommandMsg(getString(R.string.writing_access_password, selectedTag.toString()),
                        getMsgColor(R.color.colorReadText));
                bleServicePassive.requestWriteAccessPassword((EPC_tag) selectedTag, hexOldPassword, hexNewPassword);
            }
            else {
                composeAndAppendInputCommandMsg(getString(R.string.invalid_command_no_tag_found),
                        getMsgColor(R.color.colorErrorText));
                allowSendCommand();
            }
        }
    }

    @Override
    public void onWriteKillPassword(String hexOldPassword, String hexNewPassword) {
        if (bleServicePassive != null) {
            if (selectedTag != null) {
                composeAndAppendInputCommandMsg(getString(R.string.writing_kill_password, selectedTag.toString()),
                        getMsgColor(R.color.colorReadText));
                bleServicePassive.requestWriteKillPassword((EPC_tag) selectedTag, hexOldPassword, hexNewPassword);
            }
            else {
                composeAndAppendInputCommandMsg(getString(R.string.invalid_command_no_tag_found),
                        getMsgColor(R.color.colorErrorText));
                allowSendCommand();
            }
        }
    }

    @Override
    public void onWriteTag(int address, String hexData, String hexPassword) {
        if (bleServicePassive != null) {
            if (selectedTag != null) {
                composeAndAppendInputCommandMsg(getString(R.string.writing_tag, selectedTag.toString()), getMsgColor
                        (R.color.colorReadText));
                bleServicePassive.requestWrite(selectedTag, address, hexData, hexPassword);
            }
            else {
                composeAndAppendInputCommandMsg(getString(R.string.invalid_command_no_tag_found),
                        getMsgColor(R.color.colorErrorText));
                allowSendCommand();
            }
        }
    }

    @Override
    public void onWriteUserMemory(int block, String hexData) {
        if (bleServicePassive != null) {
            composeAndAppendInputCommandMsg(getString(R.string.writing_user_memory, hexData), getMsgColor
                    (R.color.colorReadText));
            bleServicePassive.requestWriteUserMemory(block, hexData);
        }
    }

    private void addTag(Tag tag) {
        inventoryTagsListAdapter.addTag(tag);
    }

    private void allowDisconnect() {
        if (!enableDisconnect) {
            enableDisconnect = true;
            supportInvalidateOptionsMenu();
        }
    }

    private void allowSendCommand() {
        sendCommandProgressBar.setVisibility(View.INVISIBLE);
        categoriesSpinner.setEnabled(true);
        commandSpinner.setEnabled(true);
        allowDisconnect();
    }

    private void clearTags() {
        inventoryTagsListAdapter.clear();

        selectedTag = null;
    }

    private void composeAndAppendInitMsg(String textMsg, int writeColor) {
        composeAndAppendMsg(textMsg, writeColor, this.initTextView, this.initScrollView);
    }

    private void composeAndAppendInputCommandMsg(String textMsg, int writeColor) {
        composeAndAppendMsg(textMsg, writeColor, this.readTextView, this.readScrollView);
    }

    private void composeAndAppendInputEventMsg(String textMsg, int writeColor) {
        composeAndAppendMsg(textMsg, writeColor, this.readTextView, this.readScrollView);
    }

    private void composeAndAppendMsg(String textMsg, int writeColor, AppCompatTextView initTextView,
                                     final ScrollView initScrollView) {
        textMsg += "\n";
        Spannable msg = new SpannableString(textMsg);
        msg.setSpan(new ForegroundColorSpan(writeColor), 0, msg.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        initTextView.append(msg);

        initScrollView.post(() -> initScrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }

    private void disableSendCommand() {
        sendCommandProgressBar.setVisibility(View.INVISIBLE);
        categoriesSpinner.setEnabled(false);
        commandSpinner.setEnabled(false);
    }

    private void disallowDisconnect() {
        if (enableDisconnect) {
            enableDisconnect = false;
            supportInvalidateOptionsMenu();
        }
    }

    private void disallowSendCommand() {
        disallowDisconnect();
        commandSpinner.setEnabled(false);
        sendCommandProgressBar.setVisibility(View.VISIBLE);
    }

    private int getMsgColor(int colorResourceId) {
        return getColor(colorResourceId);
    }

    private void initCommandMap() {
        commandCategoriesMap = new LinkedHashMap<>();

        commandMap = new LinkedHashMap<>();
        commandMap.put(getString(R.string.command_select_category_first), null);

        commandCategoriesMap.put(getString(R.string.category_command_select_category), commandMap);

        // category_command_common
        LinkedHashMap<String, CommandOperation> commonCommandMap = initCommandMapCommon();
        commandCategoriesMap.put(getString(R.string.category_command_common), commonCommandMap);

        // category_command_ble
        LinkedHashMap<String, CommandOperation> bleCommandMap = initCommandMapBle();
        commandCategoriesMap.put(getString(R.string.category_command_ble), bleCommandMap);

        // category_command_memory
        LinkedHashMap<String, CommandOperation> memoryCommandMap = initCommandMapMemory();
        commandCategoriesMap.put(getString(R.string.category_command_memory), memoryCommandMap);

        // category_command_tertium
        LinkedHashMap<String, CommandOperation> tertiumCommandMap = initCommandMapTertium();
        commandCategoriesMap.put(getString(R.string.category_command_tertium), tertiumCommandMap);

        // category_command_tag
        LinkedHashMap<String, CommandOperation> tagCommandMap = initCommandMapTag();
        commandCategoriesMap.put(getString(R.string.category_command_tag), tagCommandMap);

        // category_command_zhaga
        LinkedHashMap<String, CommandOperation> zhagaCommandMap = initCommandMapZhaga();
        commandCategoriesMap.put(getString(R.string.category_command_zhaga), zhagaCommandMap);
    }

    private LinkedHashMap<String, CommandOperation> initCommandMapBle() {
        LinkedHashMap<String, CommandOperation> bleCommandMap = new LinkedHashMap<>();
        bleCommandMap.put(getString(R.string.command_select_command), null);

        bleCommandMap.put(getString(R.string.command_ble_firmware_version), () -> bleServicePassive.requestBLEfirmwareVersion());

        bleCommandMap.put(getString(R.string.command_set_advertising_interval), () -> bleServicePassive.requestSetAdvertisingInterval(250));

        bleCommandMap.put(getString(R.string.command_get_advertising_interval), () -> bleServicePassive.requestGetAdvertisingInterval());

        bleCommandMap.put(getString(R.string.command_set_ble_power), () -> bleServicePassive.requestSetBLEpower(7));

        bleCommandMap.put(getString(R.string.command_get_ble_power), () -> bleServicePassive.requestGetBLEpower());

        bleCommandMap.put(getString(R.string.command_set_connection_interval), () -> bleServicePassive.requestSetConnectionInterval(15, 30));

        bleCommandMap.put(getString(R.string.command_get_connection_interval), () -> bleServicePassive.requestGetConnectionInterval());

        bleCommandMap.put(getString(R.string.command_set_slave_latency), () -> bleServicePassive.requestSetSlaveLatency(1));

        bleCommandMap.put(getString(R.string.command_get_slave_latency), () -> bleServicePassive.requestGetSlaveLatency());

        bleCommandMap.put(getString(R.string.command_set_supervision_timeout), () -> bleServicePassive.requestSetSupervisionTimeout(5000));

        bleCommandMap.put(getString(R.string.command_get_supervision_timeout), () -> bleServicePassive.requestGetSupervisionTimeout());

        bleCommandMap.put(getString(R.string.command_get_connection_interval_and_mtu), () -> bleServicePassive.requestGetConnectionIntervalAndMtu());

        bleCommandMap.put(getString(R.string.command_get_mac_address), () -> bleServicePassive.requestGetMACAddress());

        return bleCommandMap;
    }

    private LinkedHashMap<String, CommandOperation> initCommandMapCommon() {
        LinkedHashMap<String, CommandOperation> commonCommandMap = new LinkedHashMap<>();
        commonCommandMap.put(getString(R.string.command_select_command), null);
        commonCommandMap.put(getString(R.string.command_get_security_level), () -> bleServicePassive.requestGetSecurityLevel());
        commonCommandMap.put(getString(R.string.command_set_security_level_0), () -> bleServicePassive.requestSetSecurityLevel(0));
        commonCommandMap.put(getString(R.string.command_set_security_level_1), () -> bleServicePassive.requestSetSecurityLevel(1));
        commonCommandMap.put(getString(R.string.command_set_security_level_2), () -> bleServicePassive.requestSetSecurityLevel(2));

        commonCommandMap.put(getString(R.string.command_set_name), () -> {
            SetNameDialogFragment dialog = SetNameDialogFragment.newInstance();
            dialog.show(getSupportFragmentManager(), "SetNameDialogFragment");
        });

        commonCommandMap.put(getString(R.string.command_get_name), () -> bleServicePassive.requestGetName());
        commonCommandMap.put(getString(R.string.command_default_ble_configuration), () -> bleServicePassive.requestDefaultBLEconfiguration(1, true));

        return commonCommandMap;
    }

    private LinkedHashMap<String, CommandOperation> initCommandMapMemory() {
        LinkedHashMap<String, CommandOperation> memoryCommandMap = new LinkedHashMap<>();
        memoryCommandMap.put(getString(R.string.command_select_command), null);

        memoryCommandMap.put(getString(R.string.command_read_user_memory_0), () -> bleServicePassive.requestReadUserMemory(0));
        memoryCommandMap.put(getString(R.string.command_read_user_memory_1), () -> bleServicePassive.requestReadUserMemory(1));

        memoryCommandMap.put(getString(R.string.command_write_user_memory), () -> {
            WriteUserMemoryDialogFragment dialog = WriteUserMemoryDialogFragment.newInstance();
            dialog.show(getSupportFragmentManager(), "WriteUserMemoryDialogFragment");
        });

        return memoryCommandMap;
    }

    private LinkedHashMap<String, CommandOperation> initCommandMapTag() {
        LinkedHashMap<String, CommandOperation> tagCommandMap = new LinkedHashMap<>();
        tagCommandMap.put(getString(R.string.command_select_command), null);

        tagCommandMap.put(getString(R.string.command_write_access_password), () -> {
            if (selectedTag != null) {
                if (selectedTag instanceof EPC_tag) {
                    WriteAccessPasswordDialogFragment dialog = WriteAccessPasswordDialogFragment.newInstance
                            (selectedTag.toString());
                    dialog.show(getSupportFragmentManager(), "WriteAccessPasswordDialogFragment");
                }
                else {
                    composeAndAppendInputCommandMsg(getString(R.string
                            .invalid_command_no_epc_for_writing_access_password), getMsgColor(R.color
                            .colorErrorText));
                    allowSendCommand();
                }
            }
            else {
                composeAndAppendInputCommandMsg(getString(R.string.invalid_command_no_tag_found),
                        getMsgColor(R.color.colorErrorText));
                allowSendCommand();
            }
        });
        tagCommandMap.put(getString(R.string.command_read_tag), () -> {
            if (selectedTag != null) {
                ReadTagDialogFragment dialog = ReadTagDialogFragment.newInstance(selectedTag.toString());
                dialog.show(getSupportFragmentManager(), "ReadTagDialogFragment");
            }
            else {
                composeAndAppendInputCommandMsg(getString(R.string.invalid_command_no_tag_found),
                        getMsgColor(R.color.colorErrorText));
                allowSendCommand();
            }
        });
        tagCommandMap.put(getString(R.string.command_write_tag), () -> {
            if (selectedTag != null) {
                boolean requirePassword = selectedTag instanceof EPC_tag;
                WriteTagDialogFragment dialog = WriteTagDialogFragment.newInstance(selectedTag.toString(),
                        requirePassword);
                dialog.show(getSupportFragmentManager(), "WriteTagDialogFragment");
            }
            else {
                composeAndAppendInputCommandMsg(getString(R.string.invalid_command_no_tag_found),
                        getMsgColor(R.color.colorErrorText));
                allowSendCommand();
            }
        });
        tagCommandMap.put(getString(R.string.command_lock_tag), () -> {
            if (selectedTag != null) {
                // request password only for EPC_tag
                if (selectedTag instanceof EPC_tag) {
                    LockTagDialogFragment dialog = LockTagDialogFragment.newInstance(selectedTag.toString());
                    dialog.show(getSupportFragmentManager(), "LockTagDialogFragment");
                }
                else {
                    onLockTag(0, null);
                }
            }
            else {
                composeAndAppendInputCommandMsg(getString(R.string.invalid_command_no_tag_found),
                        getMsgColor(R.color.colorErrorText));
                allowSendCommand();
            }
        });

        tagCommandMap.put(getString(R.string.command_read_tid), () -> {
            if (selectedTag != null) {

                if (selectedTag instanceof EPC_tag) {
                    composeAndAppendInputCommandMsg(getString(R.string.reading_tid, selectedTag.toString()),
                            getMsgColor(R.color.colorReadText));
                    bleServicePassive.requestReadTID((EPC_tag) selectedTag);
                }
                else {
                    composeAndAppendInputCommandMsg(getString(R.string.invalid_command_no_epc_for_reading_tid),
                            getMsgColor(R.color.colorErrorText));
                    allowSendCommand();
                }
            }
            else {
                composeAndAppendInputCommandMsg(getString(R.string.invalid_command_no_tag_found),
                        getMsgColor(R.color.colorErrorText));
                allowSendCommand();
            }
        });

        tagCommandMap.put(getString(R.string.command_write_id), () -> {

            if (selectedTag != null) {
                if (selectedTag instanceof EPC_tag) {
                    composeAndAppendInputCommandMsg(getString(R.string.writing_id, selectedTag.toString()),
                            getMsgColor(R.color.colorReadText));
                    bleServicePassive.requestWriteID((EPC_tag) selectedTag);
                }
                else {
                    composeAndAppendInputCommandMsg(getString(R.string.invalid_command_no_epc_for_writing_tid),
                            getMsgColor(R.color.colorErrorText));
                    allowSendCommand();
                }
            }
            else {
                composeAndAppendInputCommandMsg(getString(R.string.invalid_command_no_tag_found),
                        getMsgColor(R.color.colorErrorText));
                allowSendCommand();
            }
        });

        tagCommandMap.put(getString(R.string.command_write_kill_password), () -> {
            if (selectedTag != null) {
                if (selectedTag instanceof EPC_tag) {
                    WriteKillPasswordDialogFragment dialog = WriteKillPasswordDialogFragment.newInstance
                            (selectedTag.toString());
                    dialog.show(getSupportFragmentManager(), "WriteKillPasswordDialogFragment");
                }
                else {
                    composeAndAppendInputCommandMsg(getString(R.string
                            .invalid_command_no_epc_for_writing_kill_password), getMsgColor(R.color
                            .colorErrorText));
                    allowSendCommand();
                }
            }
            else {
                composeAndAppendInputCommandMsg(getString(R.string.invalid_command_no_tag_found),
                        getMsgColor(R.color.colorErrorText));
                allowSendCommand();
            }
        });

        tagCommandMap.put(getString(R.string.command_kill), () -> {
            if (selectedTag != null) {
                if (selectedTag instanceof EPC_tag) {
                    KillTagDialogFragment dialog = KillTagDialogFragment.newInstance(selectedTag.toString());

                    dialog.show(getSupportFragmentManager(), "KillTidDialogFragment");
                }
                else {
                    composeAndAppendInputCommandMsg(getString(R.string.invalid_command_no_epc_for_killing),
                            getMsgColor(R.color.colorErrorText));
                    allowSendCommand();
                }
            }
            else {
                composeAndAppendInputCommandMsg(getString(R.string.invalid_command_no_tag_found),
                        getMsgColor(R.color.colorErrorText));
                allowSendCommand();
            }
        });
        return tagCommandMap;
    }

    private LinkedHashMap<String, CommandOperation> initCommandMapTertium() {
        LinkedHashMap<String, CommandOperation> tertiumCommandMap = new LinkedHashMap<>();
        tertiumCommandMap.put(getString(R.string.command_select_command), null);

        tertiumCommandMap.put(getString(R.string.command_test_availability), () -> bleServicePassive.requestTestAvailability());
        tertiumCommandMap.put(getString(R.string.command_sounds), () -> bleServicePassive.requestSound(1000, 1000, 1000, 500, 3));
        tertiumCommandMap.put(getString(R.string.command_light), () -> bleServicePassive.requestLight(true, 500));
        tertiumCommandMap.put(getString(R.string.command_stop_light), () -> bleServicePassive.requestLight(false, 0));

        tertiumCommandMap.put(getString(R.string.command_set_shutdown_time), () -> bleServicePassive.requestSetShutdownTime(300));
        tertiumCommandMap.put(getString(R.string.command_get_shutdown_time), () -> bleServicePassive.requestGetShutdownTime());
        tertiumCommandMap.put(getString(R.string.command_set_rf_power), () -> {
            if (bleServicePassive.requestIsHF()) {
                bleServicePassive.requestSetRFpower(PassiveReader.HF_RF_FULL_POWER, PassiveReader
                        .HF_RF_AUTOMATIC_POWER);
            }
            else if (bleServicePassive.requestIsUHF()) {
                bleServicePassive.requestSetRFpower(PassiveReader.UHF_RF_POWER_0_DB, PassiveReader
                        .UHF_RF_POWER_AUTOMATIC_MODE);
            }
        });
        tertiumCommandMap.put(getString(R.string.command_get_rf_power), () -> bleServicePassive.requestGetRFpower());
        tertiumCommandMap.put(getString(R.string.command_set_iso15693_opt_bit), () -> {
            if (bleServicePassive.requestIsHF()) {
                bleServicePassive.requestSetISO15693optionBits(PassiveReader.ISO15693_OPTION_BITS_NONE);
            }
            else {
                composeAndAppendInputCommandMsg(getString(R.string.invalid_command_not_HF_reader),
                        getMsgColor(R.color.colorErrorText));
                allowSendCommand();
            }
        });
        tertiumCommandMap.put(getString(R.string.command_get_iso15693_opt_bit), () -> {
            if (bleServicePassive.requestIsHF()) {
                bleServicePassive.requestGetISO15693optionBits();
            }
            else {
                composeAndAppendInputCommandMsg(getString(R.string.invalid_command_not_HF_reader),
                        getMsgColor(R.color.colorErrorText));
                allowSendCommand();
            }
        });
        tertiumCommandMap.put(getString(R.string.command_set_iso15693_ext_flag), () -> {
            if (bleServicePassive.requestIsHF()) {
                bleServicePassive.requestSetISO15693extensionFlag(false, false);
            }
            else {
                composeAndAppendInputCommandMsg(getString(R.string.invalid_command_not_HF_reader),
                        getMsgColor(R.color.colorErrorText));
                allowSendCommand();
            }
        });
        tertiumCommandMap.put(getString(R.string.command_get_iso15693_ext_flag), () -> {
            if (bleServicePassive.requestIsHF()) {
                bleServicePassive.requestGetISO15693extensionFlag();
            }
            else {
                composeAndAppendInputCommandMsg(getString(R.string.invalid_command_not_HF_reader),
                        getMsgColor(R.color.colorErrorText));
                allowSendCommand();
            }
        });
        tertiumCommandMap.put(getString(R.string.command_set_iso15693_bitrate), () -> {
            if (bleServicePassive.requestIsHF()) {
                bleServicePassive.requestSetISO15693bitrate(PassiveReader.ISO15693_HIGH_BITRATE, false);
            }
            else {
                composeAndAppendInputCommandMsg(getString(R.string.invalid_command_not_HF_reader),
                        getMsgColor(R.color.colorErrorText));
                allowSendCommand();
            }
        });
        tertiumCommandMap.put(getString(R.string.command_get_iso15693_bitrate), () -> {
            if (bleServicePassive.requestIsHF()) {
                bleServicePassive.requestGetISO15693bitrate();
            }
            else {
                composeAndAppendInputCommandMsg(getString(R.string.invalid_command_not_HF_reader),
                        getMsgColor(R.color.colorErrorText));
                allowSendCommand();
            }
        });
        tertiumCommandMap.put(getString(R.string.command_set_epc_freq), () -> {
            if (bleServicePassive.requestIsUHF()) {
                bleServicePassive.requestSetEpcFrequency(PassiveReader.RF_CARRIER_866_9_MHZ);
            }
            else {
                composeAndAppendInputCommandMsg(getString(R.string.invalid_command_not_UHF_reader),
                        getMsgColor(R.color.colorErrorText));
                allowSendCommand();
            }
        });
        tertiumCommandMap.put(getString(R.string.command_get_epc_freq), () -> {
            if (bleServicePassive.requestIsUHF()) {
                bleServicePassive.requestGetEpcFrequency();
            }
            else {
                composeAndAppendInputCommandMsg(getString(R.string.invalid_command_not_UHF_reader),
                        getMsgColor(R.color.colorErrorText));
                allowSendCommand();
            }
        });

        tertiumCommandMap.put(getString(R.string.command_iso15693_tunnel), () -> {
            TunnelDialogFragment dialog = TunnelDialogFragment.newInstance();
            dialog.show(getSupportFragmentManager(), "TunnelDialogFragment");
        });

        tertiumCommandMap.put(getString(R.string.command_set_inventory_mode_scan_on_input), () -> bleServicePassive.requestSetInventoryMode(PassiveReader.SCAN_ON_INPUT_MODE));
        tertiumCommandMap.put(getString(R.string.command_set_inventory_mode_normal), () -> bleServicePassive.requestSetInventoryMode(PassiveReader.NORMAL_MODE));

        tertiumCommandMap.put(getString(R.string.command_do_inventory), () -> {
            clearTags();

            bleServicePassive.requestDoInventory();
            // special management of doInventory command, without callback
            new Handler(Looper.getMainLooper()).postDelayed(this::allowSendCommand, 2000);
        });

        tertiumCommandMap.put(getString(R.string.command_reset), () -> bleServicePassive.requestReset(true));

        tertiumCommandMap.put(getString(R.string.command_default_setup), () -> bleServicePassive.requestDefaultSetup());

        return tertiumCommandMap;
    }

    private LinkedHashMap<String, CommandOperation> initCommandMapZhaga() {
        LinkedHashMap<String, CommandOperation> zhagaCommandMap = new LinkedHashMap<>();
        zhagaCommandMap.put(getString(R.string.command_select_command), null);

        zhagaCommandMap.put(getString(R.string.command_reboot), () -> bleServicePassive.requestReboot());

        zhagaCommandMap.put(getString(R.string.command_off), () -> bleServicePassive.requestOff());

        zhagaCommandMap.put(getString(R.string.command_set_hmi), () -> bleServicePassive.requestSetHMI(960, 400, 200, 2, ZhagaReader.LED_YELLOW, 200, 200, 3, 0, 0, 0));
        zhagaCommandMap.put(getString(R.string.command_get_hmi_support), () -> bleServicePassive.requestGetHMIsupport());

        zhagaCommandMap.put(getString(R.string.command_set_sound_for_inventory), () -> bleServicePassive.requestSetSoundForInventory(3000, 50, 40, 3));
        zhagaCommandMap.put(getString(R.string.command_get_sound_for_inventory), () -> bleServicePassive.requestGetSoundForInventory());

        zhagaCommandMap.put(getString(R.string.command_set_sound_for_command), () -> bleServicePassive.requestSetSoundForCommand(2730, 100, 0, 1));
        zhagaCommandMap.put(getString(R.string.command_get_sound_for_command), () -> bleServicePassive.requestGetSoundForCommand());

        zhagaCommandMap.put(getString(R.string.command_set_sound_for_error), () -> bleServicePassive.requestSetSoundForError(1000, 400, 0, 1));
        zhagaCommandMap.put(getString(R.string.command_get_sound_for_error), () -> bleServicePassive.requestGetSoundForError());

        zhagaCommandMap.put(getString(R.string.command_set_led_for_inventory), () -> bleServicePassive.requestSetLedForInventory(ZhagaReader.LED_GREEN, 50, 40, 3));
        zhagaCommandMap.put(getString(R.string.command_get_led_for_inventory), () -> bleServicePassive.requestGetLedForInventory());

        zhagaCommandMap.put(getString(R.string.command_set_led_for_command), () -> bleServicePassive.requestSetLedForCommand(ZhagaReader.LED_YELLOW, 100, 0, 1));
        zhagaCommandMap.put(getString(R.string.command_get_led_for_command), () -> bleServicePassive.requestGetLedForCommand());

        zhagaCommandMap.put(getString(R.string.command_set_led_for_error), () -> bleServicePassive.requestSetLedForError(ZhagaReader.LED_RED, 400, 0, 1));
        zhagaCommandMap.put(getString(R.string.command_get_led_for_error), () -> bleServicePassive.requestGetLedForError());

        zhagaCommandMap.put(getString(R.string.command_set_vibration_for_inventory), () -> bleServicePassive.requestSetVibrationForInventory(50, 40, 3));
        zhagaCommandMap.put(getString(R.string.command_get_vibration_for_inventory), () -> bleServicePassive.requestGetVibrationForInventory());

        zhagaCommandMap.put(getString(R.string.command_set_vibration_for_command), () -> bleServicePassive.requestSetVibrationForCommand(100, 0, 1));
        zhagaCommandMap.put(getString(R.string.command_get_vibration_for_command), () -> bleServicePassive.requestGetVibrationForCommand());

        zhagaCommandMap.put(getString(R.string.command_set_vibration_for_error), () -> bleServicePassive.requestSetVibrationForError(400, 0, 1));
        zhagaCommandMap.put(getString(R.string.command_get_vibration_for_error), () -> bleServicePassive.requestGetVibrationForError());

        zhagaCommandMap.put(getString(R.string.command_activate_button), () -> bleServicePassive.requestActivateButton(1));
        zhagaCommandMap.put(getString(R.string.command_get_activated_button), () -> bleServicePassive.requestGetActivatedButton());

        zhagaCommandMap.put(getString(R.string.command_set_rf), () -> bleServicePassive.requestSetRF(true));
        zhagaCommandMap.put(getString(R.string.command_get_rf), () -> bleServicePassive.requestGetRF());

        zhagaCommandMap.put(getString(R.string.command_set_rf_on_off), () -> bleServicePassive.requestSetRFonOff(100, 3000, 0));
        zhagaCommandMap.put(getString(R.string.command_get_rf_on_off), () -> bleServicePassive.requestGetRFonOff());

        zhagaCommandMap.put(getString(R.string.command_set_auto_off), () -> bleServicePassive.requestSetAutOff(600));
        zhagaCommandMap.put(getString(R.string.command_get_auto_off), () -> bleServicePassive.requestGetAutoOff());

        zhagaCommandMap.put(getString(R.string.command_transparent), () -> {
            TransparentDialogFragment dialog = TransparentDialogFragment.newInstance();
            dialog.show(getSupportFragmentManager(), "TransparentDialogFragment");
        });

        zhagaCommandMap.put(getString(R.string.command_default_configuration), () -> bleServicePassive.requestDefaultConfiguration());

        return zhagaCommandMap;
    }

    private void initInitialCommandChain() {
        initialCommandChain = new Chain(new Handler(Looper.getMainLooper()), false);

        ArrayList<Runnable> initialCommandList = new ArrayList<>();
        initialCommandList.add(() -> {
            if (bleServicePassive != null) {
                if (bleServicePassive.requestIsHF()) {
                    composeAndAppendInitMsg(getString(R.string.reader_is_hf),
                            getMsgColor(R.color.colorReadText));
                }
                else if (bleServicePassive.requestIsUHF()) {
                    composeAndAppendInitMsg(getString(R.string.reader_is_uhf),
                            getMsgColor(R.color.colorReadText));
                }
            }
            initialCommandChain.executeNext();
        });
        initialCommandList.add(() -> {
            if (bleServicePassive != null) {
                if (bleServicePassive.requestIsHF()) {
                    bleServicePassive.requestSetInventoryType(PassiveReader.ISO15693_AND_ISO14443A_STANDARD);
                }
                else if (bleServicePassive.requestIsUHF()) {
                    bleServicePassive.requestSetInventoryType(PassiveReader.EPC_STANDARD);
                }
            }
            else {
                initialCommandChain.executeNext();
            }
        });
        initialCommandList.add(() -> {
            if (bleServicePassive != null) {
                bleServicePassive.requestGetFirmwareVersion();
            }
            else {
                initialCommandChain.executeNext();
            }
        });
        initialCommandList.add(() -> {
            if (bleServicePassive != null) {
                bleServicePassive.requestSetInventoryMode(PassiveReader.NORMAL_MODE);
            }
            else {
                initialCommandChain.executeNext();
            }
        });
        initialCommandList.add(() -> {
            if (bleServicePassive != null) {
                bleServicePassive.requestSetInventoryParameters(PassiveReader.FEEDBACK_SOUND_AND_LIGHT, 1000,
                        1000);
            }
            else {
                initialCommandChain.executeNext();
            }
        });
        initialCommandList.add(() -> {
            // start repeating operations when initial operations end
            startRepeatingOperations();
            initialCommandChain.executeNext();
        });
        initialCommandChain.init(initialCommandList);

        initialCommandCodes = new ArrayList<>();
        initialCommandCodes.add(AbstractReaderListener.SET_INVENTORY_TYPE_COMMAND);
        initialCommandCodes.add(AbstractReaderListener.GET_FIRMWARE_VERSION_COMMAND);
        initialCommandCodes.add(AbstractReaderListener.SET_INVENTORY_MODE_COMMAND);
        initialCommandCodes.add(AbstractReaderListener.SET_INVENTORY_PARAMETERS_COMMAND);
    }

    private void initRepeatingCommandChain() {
        repeatingCommandChain = new Chain(new Handler(Looper.getMainLooper()), true);

        ArrayList<Runnable> repeatingCommandList = new ArrayList<>();
        repeatingCommandList.add(() -> {
            disallowSendCommand();
            repeatingCommandChain.executeNext();
        });
        repeatingCommandList.add(() -> {
            if (bleServicePassive != null) {
                updateAvailabilityStatus(bleServicePassive.isDeviceConnected(deviceAddress));
            }
            repeatingCommandChain.executeNext();
        });
        repeatingCommandList.add(() -> {
            if (bleServicePassive != null) {
                bleServicePassive.requestBatteryStatus();
            }
            else {
                repeatingCommandChain.executeNext();
            }
        });
        repeatingCommandList.add(() -> {
            if (bleServicePassive != null && bleServicePassive.requestIsHF()) {
                bleServicePassive.requestBatteryLevel();
            }
            else {
                repeatingCommandChain.executeNext();
            }
        });
        repeatingCommandList.add(() -> {
            allowSendCommand();
            repeatingCommandChain.executeNext();
        });
        repeatingCommandChain.init(repeatingCommandList);

        repeatingCommandCodes = new ArrayList<>();
        repeatingCommandCodes.add(AbstractReaderListener.GET_BATTERY_STATUS_COMMAND);
        repeatingCommandCodes.add(AbstractReaderListener.GET_BATTERY_LEVEL_COMMAND);

//        repeatingCommandChain = new Chain(new Handler(), false);
//        repeatingCommandCodes = new ArrayList<>();
    }

    private void startInitalOperations() {
        initialCommandChain.startExecution();
    }

    private void startRepeatingOperations() {
        repeatingCommandChain.startExecution();
    }

    private void stopAllOperations() {
        initialCommandChain.reset();

        stopRepeatingOperations();
    }

    private void stopRepeatingOperations() {
        updateAvailabilityStatus(false);
        updateBatteryStatus(0);
        updateBatteryLevel(0f);

        repeatingCommandChain.reset();
    }

    private void updateAvailabilityStatus(boolean available) {
        availabilityStatusTextView.setText(available ? R.string
                .availability_status_available : R.string
                .availability_status_not_available);
    }

    private void updateBatteryLevel(float level) {
        batteryLevelTextView.setText(getString(R.string.battery_level_value, String.format(Locale.US, "%.2f", level)));
    }

    private void updateBatteryStatus(int status) {
        batteryStatusTextView.setText(String.valueOf(status));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // commandMap
        initCommandMap();

        // initialCommandChain
        initInitialCommandChain();

        // repeatingCommandChain
        initRepeatingCommandChain();

        // manage UI
        setContentView(R.layout.activity_device_passive);
        Toolbar toolbar = findViewById(R.id.device_toolbar);
        setSupportActionBar(toolbar);

        String deviceName = getIntent().getStringExtra(EXTRAS_DEVICE_NAME);
        deviceAddress = getIntent().getStringExtra(EXTRAS_DEVICE_ADDRESS);

        getSupportActionBar().setTitle(deviceName);
        getSupportActionBar().setSubtitle(deviceAddress);

        // UI write
        final ArrayAdapter<String> commandSpinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new ArrayList<>(commandMap.keySet()));

        categoriesSpinner = findViewById(R.id.select_command_categories_spinner);

        categoriesSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                commandCategoriesMap.keySet().toArray()));

        categoriesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (view != null && view.isEnabled()) {
                    commandSpinnerAdapter.clear();

                    String categorySelected = parent.getItemAtPosition(position).toString();

                    Map<String, CommandOperation> selectedCommandMap = commandCategoriesMap.get(categorySelected);

                    if (selectedCommandMap != null) {
                        commandSpinnerAdapter.addAll(selectedCommandMap.keySet());
                        commandSpinnerAdapter.notifyDataSetChanged();
                        commandSpinner.setSelection(0);
                    }

                    commandMap = selectedCommandMap;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        commandSpinner = findViewById(R.id.select_command_spinner);

        commandSpinner.setAdapter(commandSpinnerAdapter);

        commandSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String commandAtPosition = parent.getItemAtPosition(position).toString();

                if (view != null && view.isEnabled()) {
                    doSendCommand(commandAtPosition);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        sendCommandProgressBar = findViewById(R.id.command_progress_bar);

        // UI read
        initScrollView = findViewById(R.id.init_scroll_view);
        initTextView = findViewById(R.id.init_text_view);

        RecyclerView inventoryRecyclerView = findViewById(R.id.inventory_recycler_view);

        if (inventoryRecyclerView != null) {
            inventoryRecyclerView.setHasFixedSize(true);

            inventoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));

            inventoryTagsListAdapter = new InventoryTagsListAdapter(tag -> runOnUiThread(() -> selectedTag = tag));
            inventoryRecyclerView.setAdapter(inventoryTagsListAdapter);
        }

        readScrollView = findViewById(R.id.read_scroll_view);
        readTextView = findViewById(R.id.read_text_view);

        availabilityStatusTextView = findViewById(R.id
                .availability_status_text_view_value);
        batteryStatusTextView = findViewById(R.id.battery_status_text_view_value);
        batteryLevelTextView = findViewById(R.id.battery_level_text_view_value);

        disableSendCommand();
        allowDisconnect();

        // prepare service
        final BluetoothAdapter bluetoothAdapter = BleUtil.getBtAdapter(getApplicationContext());
        if (bluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        activityResultLauncher = registerForActivityResult(new StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_CANCELED) {
                        finish();
                    }
                });

        Intent bleServiceIntent = new Intent(this, BleServicePassive.class);

        bleServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                bleServicePassive = ((BleServicePassive.LocalBinder) service)
                        .getService();

                bleServicePassive.init(bluetoothAdapter);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                bleServicePassive = null;
            }
        };

        bindService(bleServiceIntent, bleServiceConnection, BIND_AUTO_CREATE);

        // prepare broadcast receiver
        bleReceiver = new BleReceiver();

        connectionState = ConnectionState.DISCONNECTED;

        LocalBroadcastManager.getInstance(this).registerReceiver(bleReceiver, getBleIntentFilter());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(bleReceiver);
        unbindService(bleServiceConnection);
        bleServicePassive = null;
    }

    @Override
    protected void onPause() {
        stopRepeatingOperations();

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!BleUtil.isBluetoothEnabled(getApplicationContext())) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

            activityResultLauncher.launch(enableBtIntent);
        }

        if (bleServicePassive != null && connectionState == ConnectionState.CONNECTED) {
            if (!bleServicePassive.isDeviceConnected(deviceAddress)) {
                bleServicePassive.connect(deviceAddress);
            }
            else {
                startRepeatingOperations();
            }
        }
    }
}