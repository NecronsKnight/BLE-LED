package com.example.yfhuang.ble_led_controller;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class OpenSearchBLEActivity extends AppCompatActivity {
    private ArrayList<Integer> rssis;
    private Button btnSearchBLE;
    private Button gobtn;
    private BluetoothDevice terget_device = null;
    private BluetoothGatt mBluetoothGatt = null;
    private ListView BLEList;
    private BluetoothAdapter mBluetoothAdapter;
    private int REQUEST_ENABLE_BT = 1;
    private boolean mScanning;
    private Handler mHandler;
    private static final long SCAN_PERIOD = 10000;
//    private LeDeviceListAdapter mleDeviceListAdapter;
    private static final int ACCESS_LOCATION = 1001;
    private ArrayList<BluetoothDevice> strArr;
    private BluetoothAdapter blueToothAdapter;
    private ListViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        strArr = new ArrayList<>();
        adapter = new ListViewAdapter();
        setContentView(R.layout.activity_open_search_ble);
//        mleDeviceListAdapter = new LeDeviceListAdapter();
        mayRequestLocation();

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "不支持BLE", Toast.LENGTH_SHORT).show();
            finish();
        }
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        mHandler = new Handler();
        BLEList = (ListView) this.findViewById(R.id.BLEList);
        BLEList.setAdapter(adapter);
        btnSearchBLE = (Button) this.findViewById(R.id.btnSearchBLE);
        btnSearchBLE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mScanning = true;
                search();
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        mBluetoothAdapter.cancelDiscovery();
                        mScanning = false;
                    }
                }.start();
//     scanLeDevice(true);
            }
        });

        BLEList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
                // TODO Auto-generated method stub
                final BluetoothDevice device = strArr.get(position);
                if (device == null) return;
                final Intent intent = new Intent(OpenSearchBLEActivity.this, BLEServerActivity.class);
                intent.putExtra(BLEServerActivity.EXTRAS_DEVICE_NAME, device.getName());
                intent.putExtra(BLEServerActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
//                intent.putExtra(BLEServerActivity.EXTRAS_DEVICE_RSSI, rssis.get(position).toString());
                if (mScanning) {
                    mBluetoothAdapter.cancelDiscovery();
                    mScanning = false;
                }
                startActivity(intent);
            }
        });


        gobtn = (Button) this.findViewById(R.id.gobtn);
        gobtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(OpenSearchBLEActivity.this, ServerLEDControllerActivity.class);
                startActivity(intent);
            }
        });
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(new BluetoothReceiver(), intentFilter);

    }

    public void search() {
        if (mBluetoothAdapter.isDiscovering())
            mBluetoothAdapter.cancelDiscovery();

        mBluetoothAdapter.startDiscovery();
        Log.e(getPackageName(), "开始搜索");
    }
    private static final int REQUEST_COARSE_LOCATION = 0;

    private void mayRequestLocation() {
        if (Build.VERSION.SDK_INT >= 23) {
            int checkCallPhonePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
            if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                //判断是否需要 向用户解释，为什么要申请该权限
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION))
                    Toast.makeText(this, "动态请求权限", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_COARSE_LOCATION);
                return;
            } else {

            }
        } else {

        }
    }
    //系统方法,从requestPermissions()方法回调结果
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //确保是我们的请求
        if (requestCode == REQUEST_COARSE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "权限被授予", Toast.LENGTH_SHORT).show();
            } else if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

//    private void scanLeDevice(final boolean enable) {
//        if (enable) {
//            // Stops scanning after a pre-defined scan period.
//            mHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    mScanning = false;
//                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
//                }
//            }, SCAN_PERIOD);
//
//            mScanning = true;
//            mBluetoothAdapter.startLeScan(mLeScanCallback);
//            mBluetoothAdapter.startDiscovery();
//        } else {
//            mScanning = false;
//            mBluetoothAdapter.stopLeScan(mLeScanCallback);
//        }
//
//    }

//    private BluetoothAdapter.LeScanCallback mLeScanCallback =
//            new BluetoothAdapter.LeScanCallback() {
//
//
//                @Override
//                public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
//                    // TODO Auto-generated method stub
//
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            mleDeviceListAdapter.addDevice(device,rssi);
//                            mleDeviceListAdapter.notifyDataSetChanged();
//                        }
//                    });
//
//                    System.out.println("Address:"+device.getAddress());
//                    System.out.println("Name:"+device.getName());
//                    System.out.println("rssi:"+rssi);
//
//                }
//            };


//    // Adapter for holding devices found through scanning.
//    private class LeDeviceListAdapter extends BaseAdapter {
//        private ArrayList<BluetoothDevice> mLeDevices;
//
//        private LayoutInflater mInflator;
//
//
//        public LeDeviceListAdapter() {
//            super();
//            rssis=new ArrayList<Integer>();
//            mLeDevices = new ArrayList<BluetoothDevice>();
//            mInflator = getLayoutInflater();
//        }
//
////        public void addDevice(BluetoothDevice device,int rssi) {
////            if(!mLeDevices.contains(device)) {
////                mLeDevices.add(device);
////                rssis.add(rssi);
////            }
////        }
//
//        public BluetoothDevice getDevice(int position) {
//            return mLeDevices.get(position);
//        }
//
////        public void clear() {
////            mLeDevices.clear();
////            rssis.clear();
////        }
//
//        @Override
//        public int getCount() {
//            return mLeDevices.size();
//        }
//
//        @Override
//        public Object getItem(int i) {
//            return mLeDevices.get(i);
//        }
//
//        @Override
//        public long getItemId(int i) {
//            return i;
//        }
//
//        @Override
//        public View getView(int i, View view, ViewGroup viewGroup) {
//
//            // General ListView optimization code.
//
//            view = mInflator.inflate(R.layout.listitem, null);
//
//            TextView deviceAddress = (TextView) view.findViewById(R.id.tv_deviceAddr);
//            TextView deviceName = (TextView) view.findViewById(R.id.tv_deviceName);
//            TextView rssi = (TextView) view.findViewById(R.id.tv_rssi);
//
//
//            BluetoothDevice device = mLeDevices.get(i);
//            deviceAddress.setText( device.getAddress());
//            deviceName.setText(device.getName());
//            rssi.setText(""+rssis.get(i));
//
//
//
//            return view;
//        }
//    }


    class ListViewAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return strArr.size();
        }

        @Override
        public Object getItem(int i) {
            return strArr.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = LayoutInflater.from(OpenSearchBLEActivity.this).inflate(android.R.layout.simple_list_item_1, viewGroup, false);
            BluetoothDevice device = strArr.get(i);
            ((TextView) view).setText(device.getName() + "-----" + device.getAddress());
            return view;
        }
    }

    class BluetoothReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {
                Log.e(getPackageName(), "找到新设备了");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                boolean addFlag = true;
                for (BluetoothDevice bluetoothDevice : strArr) {
                    if (device.getAddress().equals(bluetoothDevice.getAddress())) {
                        addFlag = false;
                    }
                }

                if (addFlag) {
                    strArr.add(device);
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

}
