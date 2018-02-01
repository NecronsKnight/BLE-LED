package com.example.yfhuang.ble_led_controller;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
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
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BLEServerActivity extends AppCompatActivity {


    protected static String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    private final static String TAG = BLEServerActivity.class.getSimpleName();
    protected static String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    protected static String EXTRAS_DEVICE_RSSI = "RSSI";
    TextView tv_addr, tv_name, tv_status, tv_uuid;
    ExpandableListView lv;
    private String status = "disconnected";
    private Bundle b;
    private String mDeviceAddress;
    private boolean mConnected = false;
    private String mDeviceName;
    private static Handler mHandler;
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    private static BluetoothGattCharacteristic target_chara = null;
    private static BluetoothGattCharacteristic mNotifyCharacteristic;
    private static BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bleserver);

        tv_addr = (TextView) this.findViewById(R.id.tv_info_deviceaddr);
        tv_name = (TextView) this.findViewById(R.id.tv_info_devicename);
        tv_status = (TextView) this.findViewById(R.id.tv_info_constatus);
        tv_uuid = (TextView) this.findViewById(R.id.tv_targetuuid);
//        tv_rssi = (TextView) findViewById(R.id.rssi_value);
        tv_uuid.setText("还没有选择目标特征值");
        lv = (ExpandableListView) this.findViewById(R.id.expandableListView1);
        lv.setOnChildClickListener(servicesListClickListner);
        b = getIntent().getExtras();
        tv_addr.setText(b.getString(EXTRAS_DEVICE_ADDRESS));
        mDeviceAddress = b.getString(EXTRAS_DEVICE_ADDRESS);
        tv_name.setText(b.getString(EXTRAS_DEVICE_NAME));
        mDeviceName = b.getString(EXTRAS_DEVICE_NAME);
//        tv_rssi.setText(b.getString(EXTRAS_DEVICE_RSSI));
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    private final ExpandableListView.OnChildClickListener servicesListClickListner =
            new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                            int childPosition, long id) {
                    if (mGattCharacteristics != null) {
                        final BluetoothGattCharacteristic characteristic =
                                mGattCharacteristics.get(groupPosition).get(childPosition);

                        //当前目标特征值
                        target_chara = characteristic;


                        final int charaProp = characteristic.getProperties();
                          /*  if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                                if (mNotifyCharacteristic != null) {
	                                mBluetoothLeService.setCharacteristicNotification(
	                                        mNotifyCharacteristic, false);
	                                mNotifyCharacteristic = null;
	                            }
	                            mBluetoothLeService.readCharacteristic(characteristic);

	                        }*/
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            mNotifyCharacteristic = characteristic;
                            System.out.println("kkkkkkkkkk+=" + characteristic.getUuid());
                            mBluetoothLeService.setCharacteristicNotification(
                                    characteristic, true);
                        }
                        tv_uuid.setText(characteristic.getUuid().toString());
                        Intent intent = new Intent();
                        b.putString("CONNET_SATE", status);
                        b.putString("UUID", characteristic.getUuid().toString());
                        intent.putExtras(b);
                        intent.setClass(BLEServerActivity.this, ServerLEDControllerActivity.class);
                        startActivity(intent);
                        return true;
                    }
                    return false;
                }
            };
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            // 在成功启动初始化时自动连接到设备。
            mBluetoothLeService.connect(mDeviceAddress);

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBluetoothLeService = null;

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mGattUpdateReceiver);
        mBluetoothLeService = null;
    }

    //Activity出来时候，绑定广播接收器，监听蓝牙连接服务传过来的事件
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    private static BluetoothLeService.OnDataAvailableListener mOnDataAvailable = new BluetoothLeService.OnDataAvailableListener() {
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "onCharacteristicRead " + gatt.getDevice().getName()
                        + " read "
                        + characteristic.getUuid().toString()
                        + " -> "
                        + characteristic.getValue());
                mBluetoothLeService.broadcastUpdate(BluetoothLeService.ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            mBluetoothLeService.broadcastUpdate(BluetoothLeService.ACTION_DATA_AVAILABLE, characteristic);
            Log.e(TAG, "onCharacteristicWrite " + gatt.getDevice().getName()
                    + " write "
                    + characteristic.getUuid().toString()
                    + " -> "
                    + new String(characteristic.getValue()));
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.e(TAG, "onCharacteristicChanged " + gatt.getDevice().getName()
                    + " write "
                    + characteristic.getUuid().toString()
                    + " -> "
                    + new String(characteristic.getValue()));

        }
    };


    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                status = "connected";
                updateConnectionState(status);
                System.out.println("BroadcastReceiver :" + "device connected");

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                status = "disconnected";
                updateConnectionState(status);
                System.out.println("BroadcastReceiver :" + "device disconnected");


            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
                System.out.println("BroadcastReceiver :" + "device SERVICES_DISCOVERED");
            }
//            	 else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
//                //displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
//            	 System.out.println("BroadcastReceiver onData:"+intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
//            }
        }
    };


    private void updateConnectionState(final String status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_status.setText(status);
            }
        });
    }


    private void displayGattServices(List<BluetoothGattService> gattServices) {

        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = "unknown_service/未知服务";
        String unknownCharaString = "unknown_characteristic/未知特征";

        //服务数据,可扩展下拉列表的第一级数据
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();

        //特征数据（隶属于某一级服务下面的特征值集合）
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();

        //部分层次，所有特征值集合
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {

            //获取服务列表
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();

            //查表，根据该uuid获取对应的服务名称。SampleGattAttributes这个表需要自定义。
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            System.out.println("Service uuid:" + uuid);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();

            //从当前循环所指向的服务中读取特征值列表
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();

            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            //对于当前循环所指向的服务中的每一个特征值
            for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();

                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                if (gattCharacteristic.getUuid().toString().equals(SampleGattAttributes.HEART_RATE_MEASUREMENT)) {
                    //测试读取当前Characteristic数据，会触发mOnDataAvailable.onCharacteristicRead()
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mBluetoothLeService.readCharacteristic(gattCharacteristic);
                        }
                    }, 500);

                    //接受Characteristic被写的通知,收到蓝牙模块的数据后会触发mOnDataAvailable.onCharacteristicWrite()
                    mBluetoothLeService.setCharacteristicNotification(gattCharacteristic, true);
                    //设置数据内容
                    //往蓝牙模块写入数据
                    //mBluetoothLeService.writeCharacteristic(gattCharacteristic);
                }
                List<BluetoothGattDescriptor> descriptors = gattCharacteristic.getDescriptors();
                for (BluetoothGattDescriptor descriptor : descriptors) {
                    System.out.println("---descriptor UUID:" + descriptor.getUuid());
                    //获取特征值的描述
                    mBluetoothLeService.getCharacteristicDescriptor(descriptor);
                    //mBluetoothLeService.setCharacteristicNotification(gattCharacteristic, true);
                }

                gattCharacteristicGroupData.add(currentCharaData);
            }
            //按先后顺序，分层次放入特征值集合中，只有特征值
            mGattCharacteristics.add(charas);
            //构件第二级扩展列表（服务下面的特征值）
            gattCharacteristicData.add(gattCharacteristicGroupData);

        }

        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[]{LIST_NAME, LIST_UUID},
                new int[]{android.R.id.text1, android.R.id.text2},
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[]{LIST_NAME, LIST_UUID},
                new int[]{android.R.id.text1, android.R.id.text2}
        );

        lv.setAdapter(gattServiceAdapter);

    }


    public static void write(byte[] s) {
        final int charaProp = target_chara.getProperties();
        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
            //注意: 以下读取的值 通过 BluetoothGattCallback#onCharacteristicRead() 函数返回
            target_chara.setValue(s);
            mBluetoothLeService.writeCharacteristic(target_chara);
        }

    }

    public static void read() {
        mBluetoothLeService.setOnDataAvailableListener(mOnDataAvailable);
        mBluetoothLeService.readCharacteristic(target_chara);


    }


}
