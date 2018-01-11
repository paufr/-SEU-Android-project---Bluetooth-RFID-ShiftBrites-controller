package com.seuproject.pau.seuprojectbluetoothle;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {


    private static final int REQUEST_ENABLE_BT = 1;

    private static final long SCAN_PERIOD = 10000;
    private static boolean mConnected = false;
    private static final String DEVICE_NAME = "Pau";
    private Handler mHandler;
    private BluetoothLeService mBluetoothService;


    private enum ConnectionState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED
    }

    private ConnectionState currentConntectionState;
    private TextView StatusLabel;
    private Button ConnectionButton;

    private ArrayList<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();

    //UI Device parameters
    private ArrayList<DeviceParameters> deviceParameters;


    public void Initialize() {
        InitializeUIArray();
        currentConntectionState = ConnectionState.DISCONNECTED;
        StatusLabel = (TextView) findViewById(R.id.StateLabel);
        ConnectionButton = (Button) findViewById(R.id.ConnectionButton);
        ConnectionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (currentConntectionState == ConnectionState.CONNECTED)
                    mBluetoothService.disconnect();
                else {
                    scanLeDevice(true);
                }
            }
        });

        mHandler = new Handler();
        mBluetoothService = new BluetoothLeService();
        mBluetoothService.initialize(this);
        if (mBluetoothService.mBluetoothAdapter != null && !mBluetoothService.mBluetoothAdapter.isEnabled())
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }


    public void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothService.stopLeScan(mLeScanCallback);
                    if (findDeviceAndConnect()) {
                        Log.v("BLE APP", "CONNECTED");
                    }
                    else {
                        Log.v("BLE APP", "COULD NOT CONNECT");
                        devices = new ArrayList<BluetoothDevice>();
                        if (currentConntectionState != ConnectionState.CONNECTED) {
                            updateConnectionState(ConnectionState.DISCONNECTED);
                        }
                        scanLeDevice(enable);
                    }
                }
            }, SCAN_PERIOD);

            mBluetoothService.startLeScan(mLeScanCallback);
            updateConnectionState(ConnectionState.CONNECTING);
        } else {
            mBluetoothService.stopLeScan(mLeScanCallback);
        }
    }

    public BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            devices.add(device);
                        }
                    });
                }
            };


    public boolean findDeviceAndConnect() {
        try {
            for (int i = 0; i < devices.size(); ++i) {
                if (devices.get(i).getName() != null && devices.get(i).getName().equals(DEVICE_NAME)) {
                    return mBluetoothService.connect(devices.get(i).getAddress());
                }
            }
        }
        catch(Exception e) {
            return false;
        }
        return false;
    }

    private void InitializeUIArray() {
        deviceParameters = new ArrayList<>();

        deviceParameters.add(new DeviceParameters(
                (EditText)findViewById(R.id.ID1),
                (EditText)findViewById(R.id.Red1),
                (EditText)findViewById(R.id.Green1),
                (EditText)findViewById(R.id.Blue1),
                (ToggleButton) findViewById(R.id.Access1)
        ));

        deviceParameters.add(new DeviceParameters(
                (EditText)findViewById(R.id.ID2),
                (EditText)findViewById(R.id.Red2),
                (EditText)findViewById(R.id.Green2),
                (EditText)findViewById(R.id.Blue2),
                (ToggleButton) findViewById(R.id.Access2)
        ));

        deviceParameters.add(new DeviceParameters(
                (EditText)findViewById(R.id.ID3),
                (EditText)findViewById(R.id.Red3),
                (EditText)findViewById(R.id.Green3),
                (EditText)findViewById(R.id.Blue3),
                (ToggleButton) findViewById(R.id.Access3)
        ));


        deviceParameters.add(new DeviceParameters(
                (EditText)findViewById(R.id.ID4),
                (EditText)findViewById(R.id.Red4),
                (EditText)findViewById(R.id.Green4),
                (EditText)findViewById(R.id.Blue4),
                (ToggleButton) findViewById(R.id.Access4)
        ));

        deviceParameters.add(new DeviceParameters(
                (EditText)findViewById(R.id.ID5),
                (EditText)findViewById(R.id.Red5),
                (EditText)findViewById(R.id.Green5),
                (EditText)findViewById(R.id.Blue5),
                (ToggleButton) findViewById(R.id.Access5)
        ));

    }



    @Override
protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            Initialize();
            registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
            scanLeDevice(true);
        }
        catch(Exception e){
            Log.e("BLE APP", e.getMessage());
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothService != null) {
            final boolean result = findDeviceAndConnect();
            Log.d("BLE APP", "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothService = null;
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothService.initialize(getApplicationContext())) {
                Log.e("BLE APP", "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            findDeviceAndConnect();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothService = null;
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.EXTRA_DATA);
        return intentFilter;
    }

    // Handles various events fired by the Service.
// ACTION_GATT_CONNECTED: connected to a GATT server.
// ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
// ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
// ACTION_DATA_AVAILABLE: received data from the device. This can be a
// result of read or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(ConnectionState.CONNECTED);
                Log.i("BLE APP", "connected");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(ConnectionState.DISCONNECTED);
                Log.i("BLE APP", "disconnected");
            } else if (BluetoothLeService.
                    ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the
                // user interface.
                List<BluetoothGattService> services = mBluetoothService.getSupportedGattServices();
                for (int i = 0; i < services.size(); ++i) {
                    if (services.get(i).getUuid().equals(UUID.fromString(SampleGattAttributes.HM_10_CUSTOM_SERVICE))) {
                        for (int j = 0; j < services.get(i).getCharacteristics().size(); ++j) {
                            BluetoothGattCharacteristic characteristic = services.get(i).getCharacteristics().get(j);
                            if (characteristic.getUuid().equals(UUID.fromString(SampleGattAttributes.HM_10))) {
                                mBluetoothService.setCharacteristicNotification(characteristic, true);
                                mBluetoothService.readCharacteristic(characteristic);
                            }
                        }
                    }
                }
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                String data = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                Log.i("BLE APP", "Data: " + data);
                if (!DeviceParameters.isAccessParametersInfo(data)) {
                    String contentToSend = DeviceParameters.DEFAULT_NO_ACCESS_STRING;
                    for (int i = 0; i < deviceParameters.size(); ++i) {
                        if (deviceParameters.get(i).getID().equals(data)) {
                            contentToSend = deviceParameters.get(i).getAccessString();
                        }
                    }
                    BluetoothGattCharacteristic characteristic = mBluetoothService.getLastCharactesticSent();
                    if (characteristic != null) {
                        characteristic.setValue(contentToSend);
                        mBluetoothService.writeCharacteristic(characteristic);
                    }
                }
            }
            else if (BluetoothLeService.EXTRA_DATA.equals(action)) {
                String data = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                Log.i("BLE APP", "Data: " + data);
                if (!DeviceParameters.isAccessParametersInfo(data)) {
                    String contentToSend = DeviceParameters.DEFAULT_NO_ACCESS_STRING;
                    for (int i = 0; i < deviceParameters.size(); ++i) {
                        if (deviceParameters.get(i).getID().equals(data)) {
                            contentToSend = deviceParameters.get(i).getAccessString();
                        }
                    }
                    BluetoothGattCharacteristic characteristic = mBluetoothService.getLastCharactesticSent();
                    if (characteristic != null) {
                        characteristic.setValue(contentToSend);
                        mBluetoothService.writeCharacteristic(characteristic);
                    }
                }
            }
        }
    };

    private void updateConnectionState(ConnectionState state) {
        switch (state) {
            case CONNECTED:
                StatusLabel.setText("CONNECTED");
                ConnectionButton.setText("Disconnect");
                ConnectionButton.setEnabled(true);
                break;
            case CONNECTING:
                StatusLabel.setText("CONNECTING...");
                ConnectionButton.setEnabled(false);
                break;
            case DISCONNECTED:
                StatusLabel.setText("DISCONNECTED");
                ConnectionButton.setText("Connect");
                ConnectionButton.setEnabled(true);
                break;
        }
        currentConntectionState = state;
    }
}
