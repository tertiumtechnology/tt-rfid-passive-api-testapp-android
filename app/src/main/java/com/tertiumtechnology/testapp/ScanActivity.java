package com.tertiumtechnology.testapp;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.tertiumtechnology.api.rfidpassiveapilib.scan.AbstractScanListener;
import com.tertiumtechnology.api.rfidpassiveapilib.scan.Scanner;
import com.tertiumtechnology.testapp.util.BleUtil;
import com.tertiumtechnology.testapp.util.adapters.BleDeviceListAdapter;

public class ScanActivity extends AppCompatActivity {
    private static final int REQUEST_COARSE_LOCATION = 2;
    private static final int REQUEST_ENABLE_BT = 1;
    private BleDeviceListAdapter bleDeviceListAdapter;
    private Scanner scanner;

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

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                        &&
                        ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission
                                .ACCESS_COARSE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED
                        ) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            REQUEST_COARSE_LOCATION);

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
        if (requestCode == REQUEST_COARSE_LOCATION) {
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
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
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

        AbstractScanListener scanListener = new AbstractScanListener() {
            @Override
            public void deviceFoundEvent(final BluetoothDevice device) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bleDeviceListAdapter.addDevice(device);
                        bleDeviceListAdapter.notifyDataSetChanged();
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

        scanner = new Scanner(bluetoothAdapter, scanListener);

        RecyclerView recyclerView = findViewById(R.id.device_recycler_view);

        if (recyclerView != null) {
            recyclerView.setHasFixedSize(true);

            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            BleDeviceListAdapter.OnDeviceClickListener onDeviceClickListener = new BleDeviceListAdapter
                    .OnDeviceClickListener() {
                @Override
                public void onDeviceClick(final BluetoothDevice device) {
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