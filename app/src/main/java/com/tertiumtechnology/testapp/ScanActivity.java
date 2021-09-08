package com.tertiumtechnology.testapp;

import android.Manifest.permission;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.tertiumtechnology.api.rfidpassiveapilib.scan.AbstractScanListener;
import com.tertiumtechnology.api.rfidpassiveapilib.scan.BleDevice;
import com.tertiumtechnology.api.rfidpassiveapilib.scan.PassiveScanner;
import com.tertiumtechnology.testapp.util.BleUtil;
import com.tertiumtechnology.testapp.util.adapters.BleDeviceListAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ScanActivity extends AppCompatActivity {
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_LOCATION = 2;
    private BleDeviceListAdapter bleDeviceListAdapter;
    private PassiveScanner scanner;

    private ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        if (!scanner.isScanning()) {
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(null);
            menu.findItem(R.id.menu_refresh).setVisible(false);
        }
        else {
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                bleDeviceListAdapter.clear();

                checkForBluetoothEnabled();

                boolean permissionGranted = true;

                List<String> permissions = new ArrayList<>(Arrays.asList(permission.ACCESS_COARSE_LOCATION,
                        permission.ACCESS_FINE_LOCATION));

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !checkPermissions(permissions)) {

                    String[] permissionsArray = new String[permissions.size()];
                    permissionsArray = permissions.toArray(permissionsArray);

                    ActivityCompat.requestPermissions(this, permissionsArray, REQUEST_LOCATION);

                    permissionGranted = false;
                }

                if (permissionGranted) {
                    scanner.startScan();
                    supportInvalidateOptionsMenu();
                }
                break;
            case R.id.menu_stop:
                scanner.stopScan();
                supportInvalidateOptionsMenu();
                break;
            case R.id.menu_settings:

                Intent intent = new Intent(ScanActivity.this, SettingsActivity.class);
                if (scanner.isScanning()) {
                    scanner.stopScan();
                }
                startActivity(intent);
                break;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scanner.startScan();
                supportInvalidateOptionsMenu();
            }
        }
    }

    private void checkForBluetoothEnabled() {
        if (!BleUtil.isBluetoothEnabled(getApplicationContext())) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

            activityResultLauncher.launch(enableBtIntent);
        }
    }

    private boolean checkPermissions(List<String> permissions) {
        if (permissions != null) {
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.device_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.title_activity_main);

        if (!BleUtil.isBleSupported(getApplicationContext())) {
            Toast.makeText(this, R.string.error_ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        BluetoothAdapter bluetoothAdapter = BleUtil.getBtAdapter(getApplicationContext());
        if (bluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        activityResultLauncher = registerForActivityResult(new StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_CANCELED) {
                            finish();
                        }
                    }
                });

        AbstractScanListener scanListener = new AbstractScanListener() {
            @Override
            public void deviceFoundEvent(final BleDevice device) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bleDeviceListAdapter.addDevice(device);
                    }
                });
            }

            @Override
            public void stopScanEvent() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        supportInvalidateOptionsMenu();
                    }
                });
            }
        };

        scanner = new PassiveScanner(bluetoothAdapter, scanListener);

        RecyclerView recyclerView = findViewById(R.id.device_recycler_view);

        if (recyclerView != null) {
            recyclerView.setHasFixedSize(true);

            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            BleDeviceListAdapter.OnDeviceClickListener onDeviceClickListener = new BleDeviceListAdapter
                    .OnDeviceClickListener() {
                @Override
                public void onDeviceClick(final BleDevice device) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(ScanActivity.this, DeviceActivityPassive.class);
                            intent.putExtra(DeviceActivityPassive.EXTRAS_DEVICE_NAME, device.getName());
                            intent.putExtra(DeviceActivityPassive.EXTRAS_DEVICE_ADDRESS, device.getAddress());

                            if (scanner.isScanning()) {
                                scanner.stopScan();
                            }

                            startActivity(intent);
                        }
                    });
                }
            };

            bleDeviceListAdapter = new BleDeviceListAdapter(onDeviceClickListener);
            recyclerView.setAdapter(bleDeviceListAdapter);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (scanner.isScanning()) {
            scanner.stopScan();
        }
        bleDeviceListAdapter.clear();
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkForBluetoothEnabled();
    }
}