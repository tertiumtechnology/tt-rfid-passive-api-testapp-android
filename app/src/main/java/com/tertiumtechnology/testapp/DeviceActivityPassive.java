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
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
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

import com.tertiumtechnology.api.rfidpassiveapilib.EPC_tag;
import com.tertiumtechnology.api.rfidpassiveapilib.PassiveReader;
import com.tertiumtechnology.api.rfidpassiveapilib.Tag;
import com.tertiumtechnology.api.rfidpassiveapilib.listener.AbstractReaderListener;
import com.tertiumtechnology.api.rfidpassiveapilib.listener.AbstractResponseListener;
import com.tertiumtechnology.testapp.util.BleUtil;
import com.tertiumtechnology.testapp.util.Chain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DeviceActivityPassive extends AppCompatActivity {

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
                commandSpinner.setSelection(0);
                commandSpinner.setEnabled(true);
                tags.clear();

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

                composeAndAppendInputCommandMsg(intent.getStringExtra(BleServicePassive.INTENT_EXTRA_DATA_VALUE),
                        getMsgColor(R
                                .color.colorErrorText));
            }
            else if (BleServicePassive.INTENT_ACTION_DEVICE_COMMAND_CALLBACK
                    .equals(intent.getAction())) {
                Map dataRead = (Map) intent.getSerializableExtra(BleServicePassive.INTENT_EXTRA_DATA_VALUE);

                if (dataRead != null) {
                    int command = (int) dataRead.get(BleServicePassive
                            .INTENT_EXTRA_DATA_COMMAND_CALLBACK);

                    manageCommandCallback(command, dataRead);
                }
            }
            else if (BleServicePassive.INTENT_ACTION_DEVICE_COMMAND_RESULT
                    .equals(intent.getAction())) {
                Map dataRead = (Map) intent.getSerializableExtra(BleServicePassive.INTENT_EXTRA_DATA_VALUE);

                int commandCode = (int) dataRead.get(BleServicePassive
                        .INTENT_EXTRA_DATA_COMMAND_RESULT);

                manageCommandResult(commandCode, dataRead.get(BleServicePassive
                        .INTENT_EXTRA_DATA_ERROR));
            }
            else if (BleServicePassive
                    .INTENT_ACTION_DEVICE_COMMAND_CALLBACK_RESULT.equals(intent.getAction())) {
                Map dataRead = (Map) intent.getSerializableExtra(BleServicePassive.INTENT_EXTRA_DATA_VALUE);

                int commandCode = (int) dataRead.get(BleServicePassive
                        .INTENT_EXTRA_DATA_COMMAND_CALLBACK_RESULT);

                manageCommandCallback(commandCode, dataRead);

                manageCommandResult(commandCode, dataRead.get(BleServicePassive
                        .INTENT_EXTRA_DATA_ERROR));
            }
            else {
                throw new UnsupportedOperationException(getString(R.string.error_invalid_action));
            }
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

                case AbstractReaderListener.GET_EPC_FREQUENCY_COMMAND:
                    int frequency = (int) data.get(BleServicePassive
                            .INTENT_EXTRA_DATA_EPC_FREQUENCY);

                    composeAndAppendInputCommandMsg(getString(R.string.get_epc_frequency_values, frequency),
                            getMsgColor(R.color.colorReadText));
                    break;
                case AbstractReaderListener.INVENTORY_COMMAND:
                    Tag tag = (Tag) data.get(BleServicePassive
                            .INTENT_EXTRA_DATA_INVENTORY_TAG);

                    tags.add(tag);

                    composeAndAppendInputCommandMsg(getString(R.string.inventory_tag_discovered, tag.toString()),
                            getMsgColor(R.color.colorReadText));

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
            }
        }

        private void manageCommandResult(int commandCode, Object errorData) {
            boolean isRepeatingCommand = repeatingCommandCodes.contains
                    (commandCode);

            boolean isInitalCommand = initialCommandCodes.contains
                    (commandCode);

            if (errorData != null) {
                String errorMsg = getString(R.string.result_command_error, commandCode, (int) errorData);

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
        intentFilter.addAction(BleServicePassive
                .INTENT_ACTION_DEVICE_DISCONNECTED);
        intentFilter.addAction(BleServicePassive
                .INTENT_ACTION_DEVICE_CONNECTION_OPERATION_FAILED);
        intentFilter.addAction(BleServicePassive
                .INTENT_ACTION_DEVICE_COMMAND_CALLBACK);
        intentFilter.addAction(BleServicePassive
                .INTENT_ACTION_DEVICE_COMMAND_RESULT);
        intentFilter.addAction(BleServicePassive
                .INTENT_ACTION_DEVICE_COMMAND_CALLBACK_RESULT);
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

    private Map<String, CommandOperation> commandMap;
    private AppCompatSpinner commandSpinner;
    private ProgressBar sendCommandProgressBar;

    private Chain initialCommandChain;
    private ArrayList<Integer> initialCommandCodes;

    private Chain repeatingCommandChain;
    private ArrayList<Integer> repeatingCommandCodes;

    private List<Tag> tags;

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
            if (enableDisconnect) {
                menu.findItem(R.id.menu_disconnect).setEnabled(true);
            }
            else {
                menu.findItem(R.id.menu_disconnect).setEnabled(false);
            }
        }

        return true;
    }

    private void allowDisconnect() {
        if (!enableDisconnect) {
            enableDisconnect = true;
            supportInvalidateOptionsMenu();
        }
    }

    private void allowSendCommand() {
        sendCommandProgressBar.setVisibility(View.INVISIBLE);
        commandSpinner.setEnabled(true);
        allowDisconnect();
    }

    private void composeAndAppendInitMsg(String textMsg, int writeColor) {
        composeAndAppendMsg(textMsg, writeColor, this.initTextView, this.initScrollView);
    }

    private void composeAndAppendInputCommandMsg(String textMsg, int writeColor) {
        composeAndAppendMsg(textMsg, writeColor, this.readTextView, this.readScrollView);
    }

    private void composeAndAppendMsg(String textMsg, int writeColor, AppCompatTextView initTextView,
                                     final ScrollView initScrollView) {
        textMsg += "\n";
        Spannable msg = new SpannableString(textMsg);
        msg.setSpan(new ForegroundColorSpan(writeColor), 0, msg.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        initTextView.append(msg);

        initScrollView.post(new Runnable() {
            @Override
            public void run() {
                initScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    private void disableSendCommand() {
        sendCommandProgressBar.setVisibility(View.INVISIBLE);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return getColor(colorResourceId);
        }
        else {
            //noinspection deprecation
            return getResources().getColor(colorResourceId);
        }
    }

    private void initCommandMap() {
        commandMap = new LinkedHashMap<>();
        commandMap.put(getString(R.string.command_select_command), null);
        commandMap.put(getString(R.string.command_test_availability), new CommandOperation() {
            @Override
            public void execute() {
                bleServicePassive.requestTestAvailability();
            }
        });
        commandMap.put(getString(R.string.command_sounds), new CommandOperation() {
            @Override
            public void execute() {
                bleServicePassive.requestSound(1000, 1000, 1000, 500, 3);
            }
        });
        commandMap.put(getString(R.string.command_light), new CommandOperation() {
            @Override
            public void execute() {
                bleServicePassive.requestLight(true, 500);
            }
        });
        commandMap.put(getString(R.string.command_stop_light), new CommandOperation() {
            @Override
            public void execute() {
                bleServicePassive.requestLight(false, 0);
            }
        });

        commandMap.put(getString(R.string.command_set_shutdown_time), new CommandOperation() {
            @Override
            public void execute() {
                bleServicePassive.requestSetShutdownTime(300);
            }
        });
        commandMap.put(getString(R.string.command_get_shutdown_time), new CommandOperation() {
            @Override
            public void execute() {
                bleServicePassive.requestGetShutdownTime();
            }
        });
        commandMap.put(getString(R.string.command_set_rf_power), new CommandOperation() {
            @Override
            public void execute() {
                if (bleServicePassive.requestIsHF()) {
                    bleServicePassive.requestSetRFpower(PassiveReader.HF_RF_FULL_POWER, PassiveReader
                            .HF_RF_AUTOMATIC_POWER);
                }
                else if (bleServicePassive.requestIsUHF()) {
                    bleServicePassive.requestSetRFpower(PassiveReader.UHF_RF_POWER_0_DB, PassiveReader
                            .UHF_RF_POWER_AUTOMATIC_MODE);
                }
            }
        });
        commandMap.put(getString(R.string.command_get_rf_power), new CommandOperation() {
            @Override
            public void execute() {
                bleServicePassive.requestGetRFpower();
            }
        });
        commandMap.put(getString(R.string.command_set_iso15693_opt_bit), new CommandOperation() {
            @Override
            public void execute() {
                if (bleServicePassive.requestIsHF()) {
                    bleServicePassive.requestSetISO15693optionBits(PassiveReader.ISO15693_OPTION_BITS_NONE);
                }
                else {
                    composeAndAppendInputCommandMsg(getString(R.string.invalid_command_not_HF_reader),
                            getMsgColor(R.color.colorErrorText));
                    allowSendCommand();
                }
            }
        });
        commandMap.put(getString(R.string.command_get_iso15693_opt_bit), new CommandOperation() {
            @Override
            public void execute() {
                if (bleServicePassive.requestIsHF()) {
                    bleServicePassive.requestGetISO15693optionBits();
                }
                else {
                    composeAndAppendInputCommandMsg(getString(R.string.invalid_command_not_HF_reader),
                            getMsgColor(R.color.colorErrorText));
                    allowSendCommand();
                }
            }
        });
        commandMap.put(getString(R.string.command_set_iso15693_ext_flag), new CommandOperation() {
            @Override
            public void execute() {
                if (bleServicePassive.requestIsHF()) {
                    bleServicePassive.requestSetISO15693extensionFlag(false, false);
                }
                else {
                    composeAndAppendInputCommandMsg(getString(R.string.invalid_command_not_HF_reader),
                            getMsgColor(R.color.colorErrorText));
                    allowSendCommand();
                }
            }
        });
        commandMap.put(getString(R.string.command_get_iso15693_ext_flag), new CommandOperation() {
            @Override
            public void execute() {
                if (bleServicePassive.requestIsHF()) {
                    bleServicePassive.requestGetISO15693extensionFlag();
                }
                else {
                    composeAndAppendInputCommandMsg(getString(R.string.invalid_command_not_HF_reader),
                            getMsgColor(R.color.colorErrorText));
                    allowSendCommand();
                }
            }
        });
        commandMap.put(getString(R.string.command_set_iso15693_bitrate), new CommandOperation() {
            @Override
            public void execute() {
                if (bleServicePassive.requestIsHF()) {
                    bleServicePassive.requestSetISO15693bitrate(PassiveReader.ISO15693_HIGH_BITRATE, false);
                }
                else {
                    composeAndAppendInputCommandMsg(getString(R.string.invalid_command_not_HF_reader),
                            getMsgColor(R.color.colorErrorText));
                    allowSendCommand();
                }
            }
        });
        commandMap.put(getString(R.string.command_get_iso15693_bitrate), new CommandOperation() {
            @Override
            public void execute() {
                if (bleServicePassive.requestIsHF()) {
                    bleServicePassive.requestGetISO15693bitrate();
                }
                else {
                    composeAndAppendInputCommandMsg(getString(R.string.invalid_command_not_HF_reader),
                            getMsgColor(R.color.colorErrorText));
                    allowSendCommand();
                }
            }
        });
        commandMap.put(getString(R.string.command_set_epc_freq), new CommandOperation() {
            @Override
            public void execute() {
                if (bleServicePassive.requestIsUHF()) {
                    bleServicePassive.requestSetEpcFrequency(PassiveReader.RF_CARRIER_866_9_MHZ);
                }
                else {
                    composeAndAppendInputCommandMsg(getString(R.string.invalid_command_not_UHF_reader),
                            getMsgColor(R.color.colorErrorText));
                    allowSendCommand();
                }
            }
        });
        commandMap.put(getString(R.string.command_get_epc_freq), new CommandOperation() {
            @Override
            public void execute() {
                if (bleServicePassive.requestIsUHF()) {
                    bleServicePassive.requestGetEpcFrequency();
                }
                else {
                    composeAndAppendInputCommandMsg(getString(R.string.invalid_command_not_UHF_reader),
                            getMsgColor(R.color.colorErrorText));
                    allowSendCommand();
                }
            }
        });
        commandMap.put(getString(R.string.command_set_inventory_mode_scan_on_input), new CommandOperation() {
            @Override
            public void execute() {
                bleServicePassive.requestSetInventoryMode(PassiveReader.SCAN_ON_INPUT_MODE);
            }
        });
        commandMap.put(getString(R.string.command_set_inventory_mode_normal), new CommandOperation() {
            @Override
            public void execute() {
                bleServicePassive.requestSetInventoryMode(PassiveReader.NORMAL_MODE);
            }
        });
        commandMap.put(getString(R.string.command_do_inventory), new CommandOperation() {
            @Override
            public void execute() {
                tags.clear();

                bleServicePassive.requestDoInventory();
                // special management of doInventory command, without callback
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        allowSendCommand();
                    }
                }, 2000);
            }
        });
        commandMap.put(getString(R.string.command_read_tag), new CommandOperation() {
            @Override
            public void execute() {
                if (tags.size() > 0) {
                    Tag firstTag = tags.get(0);

                    composeAndAppendInputCommandMsg(getString(R.string.reading_tag, firstTag.toString
                                    ()),
                            getMsgColor(R.color.colorReadText));
                    bleServicePassive.requestRead(firstTag);
                }
                else {
                    composeAndAppendInputCommandMsg(getString(R.string.invalid_command_no_tag_found),
                            getMsgColor(R.color.colorErrorText));
                    allowSendCommand();
                }
            }
        });
        commandMap.put(getString(R.string.command_write_tag), new CommandOperation() {
            @Override
            public void execute() {
                if (tags.size() > 0) {
                    Tag firstTag = tags.get(0);

                    composeAndAppendInputCommandMsg(getString(R.string.writing_tag, firstTag.toString
                                    ()),
                            getMsgColor(R.color.colorReadText));
                    bleServicePassive.requestWrite(firstTag);
                }
                else {
                    composeAndAppendInputCommandMsg(getString(R.string.invalid_command_no_tag_found),
                            getMsgColor(R.color.colorErrorText));
                    allowSendCommand();
                }
            }
        });
        commandMap.put(getString(R.string.command_lock_tag), new CommandOperation() {
            @Override
            public void execute() {
                if (tags.size() > 0) {
                    Tag firstTag = tags.get(0);

                    composeAndAppendInputCommandMsg(getString(R.string.locking_tag, firstTag.toString
                                    ()),
                            getMsgColor(R.color.colorReadText));
                    bleServicePassive.requestLock(firstTag);
                }
                else {
                    composeAndAppendInputCommandMsg(getString(R.string.invalid_command_no_tag_found),
                            getMsgColor(R.color.colorErrorText));
                    allowSendCommand();
                }
            }
        });

        commandMap.put(getString(R.string.command_read_tid), new CommandOperation() {
            @Override
            public void execute() {
                if (tags.size() > 0) {
                    Tag firstTag = tags.get(0);
                    if (firstTag instanceof EPC_tag) {
                        composeAndAppendInputCommandMsg(getString(R.string.reading_tid, firstTag.toString
                                        ()),
                                getMsgColor(R.color.colorReadText));
                        bleServicePassive.requestReadTID((EPC_tag) firstTag);
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
            }
        });

        commandMap.put(getString(R.string.command_write_id), new CommandOperation() {
            @Override
            public void execute() {
                if (tags.size() > 0) {
                    Tag firstTag = tags.get(0);

                    if (firstTag instanceof EPC_tag) {
                        composeAndAppendInputCommandMsg(getString(R.string.writing_id, firstTag.toString()),
                                getMsgColor(R.color.colorReadText));
                        bleServicePassive.requestWriteID((EPC_tag) firstTag);
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
            }
        });

        commandMap.put(getString(R.string.command_kill), new CommandOperation() {
            @Override
            public void execute() {
                if (tags.size() > 0) {
                    Tag firstTag = tags.get(0);

                    if (firstTag instanceof EPC_tag) {
                        composeAndAppendInputCommandMsg(getString(R.string.killing_tag, firstTag.toString()),
                                getMsgColor(R.color.colorReadText));
                        bleServicePassive.requestKill((EPC_tag) firstTag);
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
            }
        });

    }

    private void initInitialCommandChain() {
        initialCommandChain = new Chain(new Handler(), false);

        ArrayList<Runnable> initialCommandList = new ArrayList<>();
        initialCommandList.add(new Runnable() {
            @Override
            public void run() {
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
            }
        });
        initialCommandList.add(new Runnable() {
            @Override
            public void run() {
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
            }
        });
        initialCommandList.add(new Runnable() {
            @Override
            public void run() {
                if (bleServicePassive != null) {
                    bleServicePassive.requestGetFirmwareVersion();
                }
                else {
                    initialCommandChain.executeNext();
                }
            }
        });
        initialCommandList.add(new Runnable() {
            @Override
            public void run() {
                if (bleServicePassive != null) {
                    bleServicePassive.requestSetInventoryMode(PassiveReader.NORMAL_MODE);
                }
                else {
                    initialCommandChain.executeNext();
                }
            }
        });
        initialCommandList.add(new Runnable() {
            @Override
            public void run() {
                if (bleServicePassive != null) {
                    bleServicePassive.requestSetInventoryParameters(PassiveReader.FEEDBACK_SOUND_AND_LIGHT, 1000,
                            1000);
                }
                else {
                    initialCommandChain.executeNext();
                }
            }
        });
        initialCommandList.add(new Runnable() {
            @Override
            public void run() {
                // start repeating operations when initial operations end
                startRepeatingOperations();
                initialCommandChain.executeNext();
            }
        });
        initialCommandChain.init(initialCommandList);

        initialCommandCodes = new ArrayList<>();
        initialCommandCodes.add(AbstractReaderListener.SET_INVENTORY_TYPE_COMMAND);
        initialCommandCodes.add(AbstractReaderListener.GET_FIRMWARE_VERSION_COMMAND);
        initialCommandCodes.add(AbstractReaderListener.SET_INVENTORY_MODE_COMMAND);
        initialCommandCodes.add(AbstractReaderListener.SET_INVENTORY_PARAMETERS_COMMAND);
    }

    private void initRepeatingCommandChain() {
        repeatingCommandChain = new Chain(new Handler(), true);

        ArrayList<Runnable> repeatingCommandList = new ArrayList<>();
        repeatingCommandList.add(new Runnable() {
            @Override
            public void run() {
                disallowSendCommand();
                repeatingCommandChain.executeNext();
            }
        });
        repeatingCommandList.add(new Runnable() {
            @Override
            public void run() {
                if (bleServicePassive != null) {
                    updateAvailabilityStatus(bleServicePassive.isDeviceConnected(deviceAddress));
                }
                repeatingCommandChain.executeNext();
            }
        });
        repeatingCommandList.add(new Runnable() {
            @Override
            public void run() {
                if (bleServicePassive != null) {
                    bleServicePassive.requestBatteryStatus();
                }
                else {
                    repeatingCommandChain.executeNext();
                }
            }
        });
        repeatingCommandList.add(new Runnable() {
            @Override
            public void run() {
                if (bleServicePassive != null && bleServicePassive.requestIsHF()) {
                    bleServicePassive.requestBatteryLevel();
                }
                else {
                    repeatingCommandChain.executeNext();
                }
            }
        });
        repeatingCommandList.add(new Runnable() {
            @Override
            public void run() {
                allowSendCommand();
                repeatingCommandChain.executeNext();
            }
        });
        repeatingCommandChain.init(repeatingCommandList);

        repeatingCommandCodes = new ArrayList<>();
        repeatingCommandCodes.add(AbstractReaderListener.GET_BATTERY_STATUS_COMMAND);
        repeatingCommandCodes.add(AbstractReaderListener.GET_BATTERY_LEVEL_COMMAND);
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

        tags = new ArrayList<>();

        // manage UI
        setContentView(R.layout.activity_device_passive);
        Toolbar toolbar = findViewById(R.id.device_toolbar);
        setSupportActionBar(toolbar);

        String deviceName = getIntent().getStringExtra(EXTRAS_DEVICE_NAME);
        deviceAddress = getIntent().getStringExtra(EXTRAS_DEVICE_ADDRESS);

        getSupportActionBar().setTitle(deviceName);
        getSupportActionBar().setSubtitle(deviceAddress);

        // UI write
        commandSpinner = findViewById(R.id.select_command_spinner);

        commandSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                commandMap.keySet().toArray()));

        commandSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String commandAtPosition = parent.getItemAtPosition(position).toString();

                if (view.isEnabled()) {
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

//        serverSocketThread = null;
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
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
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