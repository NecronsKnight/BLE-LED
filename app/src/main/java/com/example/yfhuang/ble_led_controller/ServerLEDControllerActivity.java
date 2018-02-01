package com.example.yfhuang.ble_led_controller;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.IdRes;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class ServerLEDControllerActivity extends AppCompatActivity {

    private Button btnWord, btnPaint, btnChess, btnOther, btnWriteRead, btnSendOut, btnStatic, btnFlashing, btnFlow, btnRotation, btnMarching, btnSet;
    private MyView myview;
    private PaintView mypaint;
    public LinkedList xxList;
    public LinkedList yyList;
    public LinkedList zeroList;
    private int left;
    private int top;
    private int right;
    private int bottom;
    byte state = 0;
    byte onoff = 0;
    private RadioButton btnRadioLight, btnRadioScreen, btnRadioNightLight;
    private SeekBar barBrightness;
    private Context mContext;
    protected static String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    protected static String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private BluetoothLeService mBluetoothLeService;
    private String mDeviceAddress;
    private BluetoothGattCharacteristic target_chara = null;
    private HashMap<String, String> zeromap;
    private ToggleButton btnOnOff;
    private View moreTime;
    private TimePicker timePicker;
    private DatePicker datePicker;
    private Button btnOK;
    private Button btnCancel;
    private RadioGroup radioGroup;
    private String str;
    private ImageView myImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_ledcontroller);
        PermisionUtils.verifyStoragePermissions(this);
        String path = Environment.getExternalStorageDirectory().toString() + File.separator + "/notes";
        isExist(path);
        zeroList = new LinkedList();
        xxList = new LinkedList();
        yyList = new LinkedList();
        init();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mDeviceAddress = bundle.getString(EXTRAS_DEVICE_ADDRESS).toString();
        }

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        if (bundle != null) {
            BLEServerActivity.read();
        }
        IntentFilter intentFilter = new IntentFilter(
                "com.example.bluetooth.le.ACTION_DATA_AVAILABLE");
        registerReceiver(myReceiver, intentFilter);//注册广播
    }


    private void init() {
        mContext = ServerLEDControllerActivity.this;
        btnSet = (Button) findViewById(R.id.btnSet);
        myview = (MyView) findViewById(R.id.myView);
        btnWord = (Button) findViewById(R.id.btnWord);
        btnFlow = (Button) findViewById(R.id.btnFlow);
        myImg = (ImageView) findViewById(R.id.myImg);
        btnPaint = (Button) findViewById(R.id.btnPaint);
        btnChess = (Button) findViewById(R.id.btnChess);
        btnOther = (Button) findViewById(R.id.btnOther);
        mypaint = (PaintView) findViewById(R.id.myPaint);
        btnStatic = (Button) findViewById(R.id.btnStatic);
        btnSendOut = (Button) findViewById(R.id.btnSendOut);
        btnFlashing = (Button) findViewById(R.id.btnFlashing);
        btnRotation = (Button) findViewById(R.id.btnRotation);
        btnMarching = (Button) findViewById(R.id.btnMarching);
        btnOnOff = (ToggleButton) findViewById(R.id.btnOnOff);
        btnWriteRead = (Button) findViewById(R.id.btnWriteRead);
        barBrightness = (SeekBar) findViewById(R.id.barBrightness);
        btnRadioLight = (RadioButton) findViewById(R.id.btnRadioLight);
        btnRadioScreen = (RadioButton) findViewById(R.id.btnRadioScreen);
        btnRadioNightLight = (RadioButton) findViewById(R.id.btnRadioNightLight);
//        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        mypaint.setOnTouchListener(new PicOnTouchListener());
        zeromap = new HashMap<String, String>();
        for (int i = 0; i < 32; i++) {
            zeromap.put(i + "", 00 + "");
        }
        barBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                zeromap.put(7 + "", progress + "");
                Toast.makeText(mContext, "当前亮度：" + progress + "%", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        btnRadioLight.setChecked(true);
        btnOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (btnOnOff.isChecked()) {
                    btnRadioLight.setChecked(false);
                    btnRadioScreen.setChecked(false);
                    btnRadioNightLight.setChecked(false);
                    zeromap.put(5 + "", 00 + "");
                } else {
                    btnRadioLight.setChecked(true);
                    zeromap.put(5 + "", 01 + "");
                }
            }
        });
        btnRadioLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnOnOff.setChecked(false);
                btnRadioScreen.setChecked(false);
                btnRadioNightLight.setChecked(false);
                zeromap.put(5 + "", 01 + "");

            }
        });
        btnRadioScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                btnOnOff.setChecked(false);
                btnRadioLight.setChecked(false);
                btnRadioNightLight.setChecked(false);
                zeromap.put(5 + "", 03 + "");
            }
        });
        btnRadioNightLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                btnOnOff.setChecked(false);
                btnRadioLight.setChecked(false);
                btnRadioScreen.setChecked(false);
                zeromap.put(5 + "", 02 + "");
            }
        });

        btnWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeString();
            }
        });

        btnPaint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        btnChess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "围棋模式不可用", Toast.LENGTH_SHORT).show();
            }
        });

        btnOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setBtnOtherpop();
            }
        });

        btnWriteRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setBtnWriteReadpop();
            }
        });

        btnSendOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDeviceAddress != null) {
                    send();
                }
            }
        });

        btnStatic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setBtnYespop();
            }
        });

//        btnFlashing.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//            }
//        });

//        btnFlow.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });

//        btnRotation.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//            }
//        });

        btnMarching.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                state = 0x1E;
                zeromap.put(6 + "", 30 + "");
            }
        });

//        btnSet.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
        setBtnFlashingpop();
        setbtnRotationpop();
        setBtnSettingpop();
        setBtnFlowpop();
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName,
                                       IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service)
                    .getService();
            if (!mBluetoothLeService.initialize()) {
                finish();
            }
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private BroadcastReceiver myReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(
                    BluetoothLeService.ACTION_DATA_AVAILABLE)) {
//                zhushouTextView.setText(intent.getExtras().getString(
//                        BluetoothLeService.EXTRA_DATA));
//                send_recive.setText("接受" + intent.getExtras().getString(
//                        BluetoothLeService.EXTRA_DATA).toString().getBytes().length
//                        + "字节，" + "发送" + hex_edit.getText().toString().getBytes().length + "字节");
            }
        }

    };

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        unbindService(mServiceConnection);
        unregisterReceiver(myReceiver);
    }

    private StringBuilder getWritePaintLED() {
        StringBuilder list = new StringBuilder("");
        int z = 1;
        int iiz = 0;
        xxList = myview.getxList();
        yyList = myview.getyList();
        int xsize = xxList.size() - 1;
        int ysize = yyList.size() - 1;
        int xysize = (xsize * ysize) / 8;
        for (int i = 0; i < xysize; i++) {
            String byteString = "";
            int de = 0;
            int ea = 1;
            for (int a = 0; a < 8; a++) {
                boolean tf = false;
                if (zeroList.size() > 0) {
                    for (int b = 0; b < zeroList.size(); b++) {
                        int zer = (int) zeroList.get(b);
                        if (z == zer) {
                            tf = true;
                            break;
                        }
                    }
                    if (tf) {
                        byteString = byteString + ea;
                    } else {
                        byteString = byteString + de;
                    }
                } else {
                    byteString = byteString + de;
                }
                z++;
            }
            list.append(byteString);
            iiz++;
        }
        return list;
    }


    private StringBuilder strbitmap(Bitmap bitmap) {
        StringBuilder list = new StringBuilder("");
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // 保存所有的像素的数组，图片宽×高
        int[] pixels = new int[width * height];

        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        for (int i = 0; i < pixels.length; i++) {
            int clr = pixels[i];
//            int red = (clr & 0x00ff0000) >> 16; // 取高两位
//            int green = (clr & 0x0000ff00) >> 8; // 取中两位
//            int blue = clr & 0x000000ff; // 取低两位
//            Log.d("tag", "r=" + red + ",g=" + green + ",b=" + blue);
            if (clr == -1) {
                list.append("0");
            } else {
                list.append("1");
            }
        }
        return list;
    }

    private void getLED() {
        int z = 1;
        int iiz = 33;
        Calendar c = Calendar.getInstance();
//        int YEAR =17;
        int YEAR = c.get(Calendar.YEAR);
        int MONTH = c.get(Calendar.MONTH) + 1;
        int DAY_OF_MONTH = c.get(Calendar.DAY_OF_MONTH);
        int HOUR_OF_DAY = c.get(Calendar.HOUR_OF_DAY);
        int MINUTE = c.get(Calendar.MINUTE);
        zeromap.put(12 + "", YEAR + "");
        zeromap.put(13 + "", MONTH + "");
        zeromap.put(14 + "", DAY_OF_MONTH + "");
        zeromap.put(15 + "", HOUR_OF_DAY + "");
        zeromap.put(16 + "", MINUTE + "");

        zeromap.put(17 + "", 100 + "");
        xxList = myview.getxList();
        yyList = myview.getyList();
        int xsize = xxList.size() - 1;
        int ysize = yyList.size() - 1;
        int xysize = (xsize * ysize) / 8;
        for (int i = 0; i < xysize; i++) {
            String byteString = "";
            int de = 0;
            int ea = 1;
            for (int a = 0; a < 8; a++) {
                boolean tf = false;
                if (zeroList.size() > 0) {
                    for (int b = 0; b < zeroList.size(); b++) {
                        int zer = (int) zeroList.get(b);
                        if (z == zer) {
                            tf = true;
                            break;
                        }
                    }
                    if (tf) {
                        byteString = byteString + ea;
                    } else {
                        byteString = byteString + de;
                    }
                } else {
                    byteString = byteString + de;
                }
                z++;
            }
            zeromap.put(iiz + "", byteString);
            iiz++;
        }
    }

    //OnTouch监听器
    private class PicOnTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            //event.getX获取X坐标；event.getY()获取Ｙ坐标
            int aa = 0;
            int bb = 0;
            int zero = 0;
            xxList = myview.getxList();
            yyList = myview.getyList();
//            Log.i("---xxList---", xxList + "");
//            Log.i("---yyList---", yyList + "");
//            Log.i("---xxList---", xxList.size() + "");
//            Log.i("---yyList---", yyList.size() + "");
            int xsize = xxList.size();
            int ysize = yyList.size();
            String xx = String.valueOf(event.getX());
            String yy = String.valueOf(event.getY());
            double ad = Double.valueOf(xx).doubleValue();
            double bd = Double.valueOf(yy).doubleValue();
            for (int a = 0; a < xsize - 1; a++) {
                int c = (int) xxList.get(a);
                int d = (int) xxList.get(a + 1);
                if (c < ad && ad < d) {
                    left = c;
                    aa = a;
                    right = d;
                }

            }
            for (int a = 0; a < ysize - 1; a++) {
                int c = (int) yyList.get(a);
                int d = (int) yyList.get(a + 1);
                if (c < bd && bd < d) {
                    top = c;
                    bb = a;
                    bottom = d;
                }
            }
            zero = (bb * (xsize - 1)) + aa + 1;
            if (zeroList.size() == 0) {
                zeroList.add(zero);
            } else {
                boolean aaaa = true;
                int aaaaa = zeroList.size();
                for (int a = 0; a < aaaaa; a++) {
                    int cc = (int) zeroList.get(a);
                    if (cc == zero) {
                        aaaa = false;
                    }
                }
                if (aaaa) {
                    zeroList.add(zero);
                }
            }
//            Toast.makeText(ServerLEDControllerActivity.this, zeroList + "", Toast.LENGTH_LONG).show();
            mypaint.setleft(left);
            mypaint.settop(top);
            mypaint.setright(right);
            mypaint.setbottom(bottom);
            mypaint.invalidate();
            return true;
        }
    }

    private View Flashingpop;
    private ListView popFlashinglist;
    private PopupWindow popFlashing;
    private MyAdapter<NameModle> FlashinglistAdapter = null;
    private List<NameModle> FlashinglistData = null;

    public void setBtnFlashingpop() {
        Flashingpop = getLayoutInflater().inflate(R.layout.layout_popup, null);
        popFlashinglist = (ListView) Flashingpop.findViewById(R.id.listview);
        FlashinglistData = new ArrayList<NameModle>();
        FlashinglistData.add(new NameModle("闪烁"));
        FlashinglistData.add(new NameModle("25%占空比闪烁"));
        FlashinglistData.add(new NameModle("75%占空比闪烁"));
        FlashinglistData.add(new NameModle("双色交替闪烁"));
        FlashinglistData.add(new NameModle("取消"));
        FlashinglistAdapter = new MyAdapter<NameModle>((ArrayList) FlashinglistData, R.layout.layout_poplist) {
            @Override
            public void bindView(ViewHolder holder, NameModle obj) {
                holder.setText(R.id.text, obj.getname());
            }
        };

        popFlashinglist.setAdapter(FlashinglistAdapter);
        popFlashinglist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView text = (TextView) view.findViewById(R.id.text);
                String textview = text.getText().toString();
                switch (textview) {
                    case "闪烁":
//                        state = 0x0E;
                        zeromap.put(6 + "", 20 + "");
                        popFlashing.dismiss();
                        break;
                    case "25%占空比闪烁":
//                        state = 0x0F;
                        zeromap.put(6 + "", 21 + "");
                        popFlashing.dismiss();
                        break;
                    case "75%占空比闪烁":
//                        state = 0x10;
                        zeromap.put(6 + "", 22 + "");
                        popFlashing.dismiss();
                        break;
                    case "双色交替闪烁":
//                        state = 0x11;
                        zeromap.put(6 + "", 23 + "");
                        popFlashing.dismiss();
                        break;
                    case "取消":
                        popFlashing.dismiss();
                        break;

                }
            }
        });

        popFlashing = new PopupWindow(Flashingpop, ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, true);
        popFlashing.setBackgroundDrawable(new BitmapDrawable());
        popFlashing.setFocusable(true);
        popFlashing.setTouchable(true);
        popFlashing.setOutsideTouchable(true);

        btnFlashing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popFlashing.showAtLocation(findViewById(R.id.btnFlashing), Gravity.CENTER_HORIZONTAL, 0, 0);
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 0.5f;
                getWindow().setAttributes(lp);

            }
        });

        popFlashing.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1f;
                getWindow().setAttributes(lp);
            }
        });

    }

    public void intentURL() {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        Uri content_url = Uri.parse("http://www.baidu.com");
        intent.setData(content_url);
        startActivity(intent);
    }

    private View Flowpop;
    private ListView popFlowlist;
    private PopupWindow popFlow;
    private MyAdapter<NameModle> FlowlistAdapter = null;
    private List<NameModle> FlowlistData = null;

    public void setBtnFlowpop() {
        Flowpop = getLayoutInflater().inflate(R.layout.layout_popup, null);
        popFlowlist = (ListView) Flowpop.findViewById(R.id.listview);
        FlowlistData = new ArrayList<NameModle>();
        FlowlistData.add(new NameModle("流水"));
        FlowlistData.add(new NameModle("双行顺序流水"));
        FlowlistData.add(new NameModle("双行并行流水"));
        FlowlistData.add(new NameModle("三行顺序流水"));
        FlowlistData.add(new NameModle("三行并行流水"));
        FlowlistData.add(new NameModle("左向流水"));
        FlowlistData.add(new NameModle("左向双行顺序流水"));
        FlowlistData.add(new NameModle("左向双行并行流水"));
        FlowlistData.add(new NameModle("左向三行顺序流水"));
        FlowlistData.add(new NameModle("左向三行并行流水"));
        FlowlistData.add(new NameModle("首尾相连流水"));
        FlowlistData.add(new NameModle("首尾相连双行流水"));
        FlowlistData.add(new NameModle("左向并行首尾相连双行流水"));
        FlowlistData.add(new NameModle("取消"));
        FlowlistAdapter = new MyAdapter<NameModle>((ArrayList) FlowlistData, R.layout.layout_poplist) {
            @Override
            public void bindView(ViewHolder holder, NameModle obj) {
                holder.setText(R.id.text, obj.getname());
            }
        };

        popFlowlist.setAdapter(FlowlistAdapter);
        popFlowlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView text = (TextView) view.findViewById(R.id.text);
                String textview = text.getText().toString();
                switch (textview) {
                    case "流水":
//                        state = 0x05;
                        zeromap.put(6 + "", 5 + "");
                        popFlow.dismiss();
                        break;
                    case "双行顺序流水":
//                        state = 0x06;
                        zeromap.put(6 + "", 6 + "");
                        popFlow.dismiss();
                        break;
                    case "双行并行流水":
//                        state = 0x07;
                        zeromap.put(6 + "", 7 + "");
                        popFlow.dismiss();
                        break;
                    case "三行顺序流水":
//                        state = 0x08;
                        zeromap.put(6 + "", 8 + "");
                        popFlow.dismiss();
                        break;
                    case "三行并行流水":
//                        state = 0x09;
                        zeromap.put(6 + "", 9 + "");
                        popFlow.dismiss();
                        break;
                    case "左向流水":
//                        state = 0x0A;
                        zeromap.put(6 + "", 10 + "");
                        popFlow.dismiss();
                        break;
                    case "左向双行顺序流水":
//                        state = 0x0B;
                        zeromap.put(6 + "", 11 + "");
                        popFlow.dismiss();
                        break;
                    case "左向双行并行流水":
//                        state = 0x0C;
                        zeromap.put(6 + "", 12 + "");
                        popFlow.dismiss();
                        break;
                    case "左向三行顺序流水":
//                        state = 0x0D;
                        zeromap.put(6 + "", 13 + "");
                        popFlow.dismiss();
                        break;
                    case "左向三行并行流水":
//                        state = 0x0E;
                        zeromap.put(6 + "", 14 + "");
                        popFlow.dismiss();
                        break;
                    case "首尾相连流水":
//                        state = 0x0F;
                        zeromap.put(6 + "", 15 + "");
                        popFlow.dismiss();
                        break;
                    case "首尾相连双行流水":
//                        state = 0x10;
                        zeromap.put(6 + "", 16 + "");
                        popFlow.dismiss();
                        break;
                    case "左向并行首尾相连双行流水":
//                        state = 0x11;
                        zeromap.put(6 + "", 17 + "");
                        popFlow.dismiss();
                        break;
                    case "取消":
                        popFlow.dismiss();
                        break;

                }
            }
        });

        popFlow = new PopupWindow(Flowpop, ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, true);
        popFlow.setBackgroundDrawable(new BitmapDrawable());
        popFlow.setFocusable(true);
        popFlow.setTouchable(true);
        popFlow.setOutsideTouchable(true);

        btnFlow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popFlow.showAtLocation(findViewById(R.id.btnFlow), Gravity.CENTER_HORIZONTAL, 0, 0);
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 0.5f;
                getWindow().setAttributes(lp);

            }
        });

        popFlow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1f;
                getWindow().setAttributes(lp);
            }
        });

    }

    private View WriteReadpop;
    private ListView popWriteReadlist;
    private PopupWindow popWriteRead;
    private MyAdapter<NameModle> WriteReadlistAdapter = null;
    private List<NameModle> WriteReadlistData = null;

    public void setBtnWriteReadpop() {
        WriteReadpop = getLayoutInflater().inflate(R.layout.layout_popup, null);
        popWriteReadlist = (ListView) WriteReadpop.findViewById(R.id.listview);
        WriteReadlistData = new ArrayList<NameModle>();
        WriteReadlistData.add(new NameModle("写"));
        WriteReadlistData.add(new NameModle("读"));
        WriteReadlistData.add(new NameModle("取消"));
        WriteReadlistAdapter = new MyAdapter<NameModle>((ArrayList) WriteReadlistData, R.layout.layout_poplist) {
            @Override
            public void bindView(ViewHolder holder, NameModle obj) {
                holder.setText(R.id.text, obj.getname());
            }
        };

        popWriteReadlist.setAdapter(WriteReadlistAdapter);
        popWriteReadlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView text = (TextView) view.findViewById(R.id.text);
                String textview = text.getText().toString();
                switch (textview) {
                    case "写":
                        StringBuilder WriteLED = getWritePaintLED();
                        String writeLED = WriteLED.toString();
                        saveFile(writeLED);
                        popWriteRead.dismiss();
                        break;
                    case "读":
                        popWriteRead.dismiss();
                        break;
                    case "取消":
                        popWriteRead.dismiss();
                        break;

                }
            }
        });

        popWriteRead = new PopupWindow(WriteReadpop, ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, true);
        popWriteRead.setBackgroundDrawable(new BitmapDrawable());
        popWriteRead.setFocusable(true);
        popWriteRead.setTouchable(true);
        popWriteRead.setOutsideTouchable(true);

        popWriteRead.showAtLocation(findViewById(R.id.btnWriteRead), Gravity.CENTER_HORIZONTAL, 0, 0);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;
        getWindow().setAttributes(lp);


        popWriteRead.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1f;
                getWindow().setAttributes(lp);
            }
        });

    }

    public static Bitmap textAsBitmap(String text, float textSize) {
        TextPaint textPaint = new TextPaint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(textSize);
        StaticLayout layout = new StaticLayout(text, textPaint, 800, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, true);
        Bitmap bitmap = Bitmap.createBitmap(layout.getWidth(), layout.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.translate(0, 0);
        canvas.drawColor(Color.WHITE);
        layout.draw(canvas);
        Log.d("textAsBitmap", String.format("1:%d %d", layout.getWidth(), layout.getHeight()));
        return bitmap;
    }

    public static boolean isWordNumeric(String str) {

        Pattern pattern = Pattern.compile("[a-zA-Z_0-9]*");

        return pattern.matcher(str).matches();

    }

    private View writePop;
    private PopupWindow popWrite;
    private EditText btnWrite;
    private Button btnWriteOk;
    private Button btnWriteCancel;
    private String name;

    public void writeString() {
        writePop = getLayoutInflater().inflate(R.layout.layout_write, null);
        btnWrite = (EditText) writePop.findViewById(R.id.btnWrite);
        btnWriteOk = (Button) writePop.findViewById(R.id.btnWriteOk);
        btnWriteCancel = (Button) writePop.findViewById(R.id.btnWriteCancel);
        btnWriteOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = btnWrite.getText().toString();
                boolean nameedge = true;
                int number = name.length();
                int wordsize = 0;
                for (int i = 0; i < number; i++) {
                    String subString = name.substring(i, i + 1);
                    if (isWordNumeric(subString)) {
                        wordsize = wordsize + 1;
                    } else {
                        wordsize = wordsize + 2;
                    }
                }
                popWrite.dismiss();
                if (wordsize <= 8 && wordsize > 0) {
                    xxList.clear();
                    yyList.clear();
                    mypaint.invalidate();
                    myview.setLineX(64);
                    myview.setLineY(32);
                    mypaint.invalidate();
                    mypaint.clearList();
                    stringString(name, wordsize, 64, 32);
//                    Bitmap  wordMap1 = textAsBitmap(name, 400);
//                    Bitmap wordMap = setbitmap(wordMap1, 64, 32);
////                    myImg.setImageBitmap(wordMap);
//                    StringBuilder wordString = strbitmap(wordMap);
//                    int ya=wordMap.getHeight();
//                    int xa=wordMap.getWidth();
//                    Log.i(ya+" "+xa,wordString+"");
//                    setled(wordString+"");
                } else if (wordsize <= 32 && wordsize > 8) {
                    myview.setLineX(64);
                    myview.setLineY(64);
                    myview.invalidate();
//                    stringString(name,wordsize,64,64);
                } else if (wordsize <= 64 && wordsize > 32) {
                    myview.setLineX(128);
                    myview.setLineY(64);
                    myview.invalidate();
//                    stringString(name,wordsize,128,64);
                } else if (wordsize <= 128 && wordsize > 64) {
                    myview.setLineX(128);
                    myview.setLineY(128);
                    myview.invalidate();
//                    stringString(name,wordsize,128,128);
                } else if (wordsize > 128) {
                    nameedge = false;
                }
//
                if (nameedge) {
//                    Bitmap bmap = textAsBitmap(name, foldNumber);
//                    myImg.setImageBitmap(bmap);
                } else {
                    Toast.makeText(getApplicationContext(), "字数超过上限，最多输入64个汉字或128个字母", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnWriteCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popWrite.dismiss();
            }
        });

        popWrite = new PopupWindow(writePop, ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, true);
        popWrite.setBackgroundDrawable(new BitmapDrawable());
        popWrite.setFocusable(true);
        popWrite.setTouchable(true);
        popWrite.setOutsideTouchable(true);

        popWrite.showAtLocation(findViewById(R.id.btnOther), Gravity.CENTER_HORIZONTAL, 0, 0);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;
        getWindow().setAttributes(lp);


        popWrite.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1f;
                getWindow().setAttributes(lp);
            }
        });

    }

    private void stringString(String str, int a, int x, int y) {
        Bitmap bitmap = Bitmap.createBitmap(x, y, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        for (int i = 0; i < str.length(); i++) {
            int why = 0;
            String word = str.substring(i, i + 1);
            Bitmap wordMap = null;
            if (isWordNumeric(word) && i + 2 < str.length()) {
                String word2 = str.substring(i + 1, i + 2);
                if (isWordNumeric(word2)) {
                    wordMap = textAsBitmap(word + word2, 400);
                    why++;
                    i = i + 1;
                } else {
                    wordMap = textAsBitmap(word, 800);
                    why++;
                }
            } else {
                wordMap = textAsBitmap(word, 800);
                why++;
            }
//            wordMap = setbitmap(wordMap, 32, 32);
//            canvas.drawBitmap(wordMap, i * 32, 0, null);
            stringa(canvas , a, why,wordMap );
//            stringa(canvas,i,wordMap);
        }
//        int ya=bitmap.getHeight();
//
//        int xa=bitmap.getWidth();
//                    myImg.setImageBitmap(bitmap);
        StringBuilder wordString = strbitmap(bitmap);
//        Log.i(ya+" "+xa,wordString+"");
        int end = wordString.length();
        setled(wordString + "");
    }

    private void setled(String a) {
        int xxx = 1;
        xxList = myview.getxList();
        yyList = myview.getyList();
        Log.i("xxList ",xxList.size()+"");
        Log.i("yyList ",yyList.size()+"");
        for (int y = 0; y < yyList.size() - 1; y++) {
            for (int x = 0; x < xxList.size() - 1; x++) {
                String str = a.substring(xxx - 1, xxx);
                if (str.equals("1")) {
                    mypaint.setleft((int) xxList.get(x));
                    mypaint.settop((int) yyList.get(y));
                    mypaint.setright((int) xxList.get(x + 1));
                    mypaint.setbottom((int) yyList.get(y + 1));
                    mypaint.invalidate();
                }
                xxx++;
            }
        }

    }

    //    private void setled(StringBuilder a,int n){
//        xxList = myview.getxList();
//        yyList = myview.getyList();
//        for (int y=0;y<yyList.size()-1;y++){
//            String str = a.substring(y*n,(y+1)*n);
//            for (int x=0;x<(xxList.size()-1)/8;x++){
//                String str1=str.substring(x,x+8);
//                for (int z=0;z<8;z++){
//                    String str2 = str1.substring(z,z+1);
//                    if (str2.equals("1")){
//                        mypaint.setleft((int)xxList.get(x));
//                        mypaint.settop((int)yyList.get(y));
//                        mypaint.setright((int)xxList.get(x+1));
//                        mypaint.setbottom((int)yyList.get(y+1));
//                        mypaint.invalidate();
//                    }
//                }
//            }
//
//        }
//
//    }
    private void stringa(Canvas canvas, int a, int i, Bitmap bmap) {
        Bitmap wordMap = null;
        if (a <= 2) {
            wordMap = setbitmap(bmap, 64, 32);
            canvas.drawBitmap(wordMap, 0, 0, null);
        } else if (a <= 4 && a > 2) {
            wordMap = setbitmap(bmap, 32, 32);
            canvas.drawBitmap(wordMap, i * 32, 0, null);
        } else if (a <= 8 && a > 4) {
            int x=i%2;
            int y=i/2;
            wordMap = setbitmap(bmap, 32, 16);
            canvas.drawBitmap(wordMap, x*32, y*16, null);
        } else if (a <= 16 && a > 8) {
        } else if (a <= 32 && a > 16) {
        } else if (a <= 64 && a > 32) {
        } else if (a <= 128 && a > 64) {
        }
//        Bitmap wordMap = setbitmap(bmap, 32, 32);
//        canvas.drawBitmap(wordMap, i * 32, 0, null);
    }
//    private void stringa(Bitmap bmap, int a, int why, Canvas canvas) {
//        Bitmap wordMap = null;
//        if (a <= 2) {
////            wordMap = setbitmap(bmap, 64, 32);
////            canvas.drawBitmap(wordMap, 0, 0, null);
//        } else if (a <= 4 && a > 2) {
////            int w = why / 2;
////            int h = why % 2;
////            wordMap = setbitmap(bmap, 32, 32);
////            canvas.drawBitmap(wordMap, w * 32, h * 32, null);
//        } else if (a <= 8 && a > 4) {
////            int w = why / 2;
////            int h = why % 2;
////            wordMap = setbitmap(bmap, 32, 16);
////            canvas.drawBitmap(wordMap, w * 32, h * 16, null);
//        } else if (a <= 16 && a > 8) {
////            int w = why / 4;
////            int h = why % 4;
////            wordMap = setbitmap16(bmap);
////            canvas.drawBitmap(wordMap, w * 16, h * 16, null);
//        } else if (a <= 32 && a > 16) {
////            int w = why / 4;
////            int h = why % 4;
////            wordMap = setbitmap16(bmap);
////            canvas.drawBitmap(wordMap, w * 16, h * 16, null);
//        } else if (a <= 64 && a > 32) {
////            int w = why / 8;
////            int h = why % 8;
////            wordMap = setbitmap16(bmap);
////            canvas.drawBitmap(wordMap, w * 16, h * 16, null);
//        } else if (a <= 128 && a > 64) {
////            int w = why / 8;
////            int h = why % 8;
////            wordMap = setbitmap16(bmap);
////            canvas.drawBitmap(wordMap, w * 16, h * 16, null);
//        }
// wordMap = setbitmap(wordMap, 32, 32);
//            canvas.drawBitmap(wordMap, i * 32, 0, null);
//    }

    private Bitmap setbitmap(Bitmap bitmap, int a, int b) {
        int bmpWidth = bitmap.getWidth();

        int bmpHeight = bitmap.getHeight();

//缩放图片的尺寸

        float scaleWidth = (float) a / bmpWidth;     //按固定大小缩放  sWidth 写多大就多大

        float scaleHeight = (float) b / bmpHeight;  //

        Matrix matrix = new Matrix();

        matrix.postScale(scaleWidth, scaleHeight);//产生缩放后的Bitmap对象

        Bitmap resizeBitmap = Bitmap.createBitmap(bitmap, 0, 0, bmpWidth, bmpHeight, matrix, false);

        return resizeBitmap;
    }


    private Bitmap setbitmap16(Bitmap bitmap) {
        int bmpWidth = bitmap.getWidth();

        int bmpHeight = bitmap.getHeight();

//缩放图片的尺寸

        float scaleWidth = (float) 16 / bmpWidth;     //按固定大小缩放  sWidth 写多大就多大

        float scaleHeight = (float) 16 / bmpHeight;  //

        Matrix matrix = new Matrix();

        matrix.postScale(scaleWidth, scaleHeight);//产生缩放后的Bitmap对象

        Bitmap resizeBitmap = Bitmap.createBitmap(bitmap, 0, 0, bmpWidth, bmpHeight, matrix, false);

        return resizeBitmap;
    }

    private View Spop;
    private ListView popSlist;
    private PopupWindow popS;
    private MyAdapter<NameModle> SlistAdapter = null;
    private List<NameModle> SlistData = null;

    public void setSpop() {
        Spop = getLayoutInflater().inflate(R.layout.layout_popup, null);
        popSlist = (ListView) Spop.findViewById(R.id.listview);
        SlistData = new ArrayList<NameModle>();
        SlistData.add(new NameModle("编辑基本图形"));
        SlistData.add(new NameModle("收敛模式"));
        SlistData.add(new NameModle("迸发模式"));
        SlistData.add(new NameModle("旋转模式"));
        SlistData.add(new NameModle("左旋模式"));
        SlistData.add(new NameModle("右旋模式"));
        SlistData.add(new NameModle("速度设置"));
        SlistData.add(new NameModle("取消"));
        SlistAdapter = new MyAdapter<NameModle>((ArrayList) SlistData, R.layout.layout_poplist) {
            @Override
            public void bindView(ViewHolder holder, NameModle obj) {
                holder.setText(R.id.text, obj.getname());
            }
        };

        popSlist.setAdapter(SlistAdapter);
        popSlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView text = (TextView) view.findViewById(R.id.text);
                String textview = text.getText().toString();
                switch (textview) {
                    case "编辑基本图形":
                        popS.dismiss();
                        break;
                    case "收敛模式":
                        popS.dismiss();
                        break;
                    case "迸发模式":
                        popS.dismiss();
                        break;
                    case "旋转模式":
                        popS.dismiss();
                        break;
                    case "左旋模式":
                        popS.dismiss();
                        break;
                    case "右旋模式":
                        popS.dismiss();
                        break;
                    case "速度设置":
                        popS.dismiss();
                        break;
                    case "取消":
                        popS.dismiss();
                        break;

                }
            }
        });

        popS = new PopupWindow(Spop, ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, true);
        popS.setBackgroundDrawable(new BitmapDrawable());
        popS.setFocusable(true);
        popS.setTouchable(true);
        popS.setOutsideTouchable(true);

        popS.showAtLocation(findViewById(R.id.btnOther), Gravity.CENTER_HORIZONTAL, 0, 0);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;
        getWindow().setAttributes(lp);


        popS.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1f;
                getWindow().setAttributes(lp);
            }
        });

    }

    private View ADpop;
    private ListView popADlist;
    private PopupWindow popAD;
    private MyAdapter<NameModle> ADlistAdapter = null;
    private List<NameModle> ADlistData = null;

    public void setADpop() {
        ADpop = getLayoutInflater().inflate(R.layout.layout_popup, null);
        popADlist = (ListView) ADpop.findViewById(R.id.listview);
        ADlistData = new ArrayList<NameModle>();
        ADlistData.add(new NameModle("文字编辑"));
        ADlistData.add(new NameModle("图形编辑"));
        ADlistData.add(new NameModle("闪烁频率"));
        ADlistData.add(new NameModle("取消"));
        ADlistAdapter = new MyAdapter<NameModle>((ArrayList) ADlistData, R.layout.layout_poplist) {
            @Override
            public void bindView(ViewHolder holder, NameModle obj) {
                holder.setText(R.id.text, obj.getname());
            }
        };

        popADlist.setAdapter(ADlistAdapter);
        popADlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView text = (TextView) view.findViewById(R.id.text);
                String textview = text.getText().toString();
                switch (textview) {
                    case "文字编辑":
                        popAD.dismiss();
                        break;
                    case "图形编辑":
                        popAD.dismiss();
                        break;
                    case "闪烁频率":
                        popAD.dismiss();
                        break;
                    case "取消":
                        popAD.dismiss();
                        break;

                }
            }
        });

        popAD = new PopupWindow(ADpop, ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, true);
        popAD.setBackgroundDrawable(new BitmapDrawable());
        popAD.setFocusable(true);
        popAD.setTouchable(true);
        popAD.setOutsideTouchable(true);

        popAD.showAtLocation(findViewById(R.id.btnOther), Gravity.CENTER_HORIZONTAL, 0, 0);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;
        getWindow().setAttributes(lp);


        popAD.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1f;
                getWindow().setAttributes(lp);
            }
        });

    }

    private View Starpop;
    private ListView popStarlist;
    private PopupWindow popStar;
    private MyAdapter<NameModle> StarlistAdapter = null;
    private List<NameModle> StarlistData = null;

    public void setStarpop() {
        Starpop = getLayoutInflater().inflate(R.layout.layout_popup, null);
        popStarlist = (ListView) Starpop.findViewById(R.id.listview);
        StarlistData = new ArrayList<NameModle>();
        StarlistData.add(new NameModle("小熊星座"));
        StarlistData.add(new NameModle("大熊星座"));
        StarlistData.add(new NameModle("猎户星座"));
        StarlistData.add(new NameModle("天琴座"));
        StarlistData.add(new NameModle("天狼星"));
        StarlistData.add(new NameModle("取消"));
        StarlistAdapter = new MyAdapter<NameModle>((ArrayList) StarlistData, R.layout.layout_poplist) {
            @Override
            public void bindView(ViewHolder holder, NameModle obj) {
                holder.setText(R.id.text, obj.getname());
            }
        };

        popStarlist.setAdapter(StarlistAdapter);
        popStarlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView text = (TextView) view.findViewById(R.id.text);
                String textview = text.getText().toString();
                switch (textview) {
                    case "小熊星座":
                        popStar.dismiss();
                        break;
                    case "大熊星座":
                        popStar.dismiss();
                        break;
                    case "猎户星座":
                        popStar.dismiss();
                        break;
                    case "天琴座":
                        popStar.dismiss();
                        break;
                    case "天狼星":
                        popStar.dismiss();
                        break;
                    case "取消":
                        popStar.dismiss();
                        break;

                }
            }
        });

        popStar = new PopupWindow(Starpop, ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, true);
        popStar.setBackgroundDrawable(new BitmapDrawable());
        popStar.setFocusable(true);
        popStar.setTouchable(true);
        popStar.setOutsideTouchable(true);

        popStar.showAtLocation(findViewById(R.id.btnOther), Gravity.CENTER_HORIZONTAL, 0, 0);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;
        getWindow().setAttributes(lp);


        popStar.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1f;
                getWindow().setAttributes(lp);
            }
        });

    }

    private View Timepop;
    private ListView popTimelist;
    private PopupWindow poptime;
    private MyAdapter<NameModle> TimelistAdapter = null;
    private List<NameModle> TimelistData = null;

    public void setTimepop() {
        Timepop = getLayoutInflater().inflate(R.layout.layout_popup, null);
        popTimelist = (ListView) Timepop.findViewById(R.id.listview);
        OtherlistData = new ArrayList<NameModle>();
        OtherlistData.add(new NameModle("设置时间"));
        OtherlistData.add(new NameModle("取消"));
        OtherlistData.add(new NameModle("确认"));
        TimelistAdapter = new MyAdapter<NameModle>((ArrayList) TimelistData, R.layout.layout_poplist) {
            @Override
            public void bindView(ViewHolder holder, NameModle obj) {
                holder.setText(R.id.text, obj.getname());
            }
        };

        popTimelist.setAdapter(TimelistAdapter);
        popTimelist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView text = (TextView) view.findViewById(R.id.text);
                String textview = text.getText().toString();
                switch (textview) {
                    case "设置时间":
                        setPopTime();
                        poptime.dismiss();
                        break;
                    case "确认":
                        poptime.dismiss();
                        break;
                    case "取消":
                        poptime.dismiss();
                        break;

                }
            }
        });

        poptime = new PopupWindow(Timepop, ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, true);
        poptime.setBackgroundDrawable(new BitmapDrawable());
        poptime.setFocusable(true);
        poptime.setTouchable(true);
        poptime.setOutsideTouchable(true);

        poptime.showAtLocation(findViewById(R.id.btnOther), Gravity.CENTER_HORIZONTAL, 0, 0);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;
        getWindow().setAttributes(lp);


        poptime.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1f;
                getWindow().setAttributes(lp);
            }
        });

    }


    private View Otherpop;
    private ListView popOtherlist;
    private PopupWindow popOther;
    private MyAdapter<NameModle> OtherlistAdapter = null;
    private List<NameModle> OtherlistData = null;

    public void setBtnOtherpop() {
        Otherpop = getLayoutInflater().inflate(R.layout.layout_popup, null);
        popOtherlist = (ListView) Otherpop.findViewById(R.id.listview);
        OtherlistData = new ArrayList<NameModle>();
        OtherlistData.add(new NameModle("联网"));
        OtherlistData.add(new NameModle("失眠"));
        OtherlistData.add(new NameModle("老年AD症"));
        OtherlistData.add(new NameModle("星空"));
        OtherlistData.add(new NameModle("闹钟"));
        OtherlistData.add(new NameModle("取消"));
        OtherlistAdapter = new MyAdapter<NameModle>((ArrayList) OtherlistData, R.layout.layout_poplist) {
            @Override
            public void bindView(ViewHolder holder, NameModle obj) {
                holder.setText(R.id.text, obj.getname());
            }
        };

        popOtherlist.setAdapter(OtherlistAdapter);
        popOtherlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView text = (TextView) view.findViewById(R.id.text);
                String textview = text.getText().toString();
                switch (textview) {
                    case "联网":
                        intentURL();
                        popOther.dismiss();
                        break;
                    case "失眠":
                        setSpop();
                        popOther.dismiss();
                        break;
                    case "老年AD症":
                        setADpop();
                        popOther.dismiss();
                        break;
                    case "星空":
                        setStarpop();
                        popOther.dismiss();
                        break;
                    case "闹钟":
                        setTimepop();
                        popOther.dismiss();
                        break;
                    case "取消":
                        popOther.dismiss();
                        break;

                }
            }
        });

        popOther = new PopupWindow(Otherpop, ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, true);
        popOther.setBackgroundDrawable(new BitmapDrawable());
        popOther.setFocusable(true);
        popOther.setTouchable(true);
        popOther.setOutsideTouchable(true);

        popOther.showAtLocation(findViewById(R.id.btnOther), Gravity.CENTER_HORIZONTAL, 0, 0);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;
        getWindow().setAttributes(lp);


        popOther.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1f;
                getWindow().setAttributes(lp);
            }
        });

    }

    private View Dotpop;
    private ListView popDotlist;
    private PopupWindow popDot;
    private MyAdapter<NameModle> DotlistAdapter = null;
    private List<NameModle> DotlistData = null;

    public void setBtndotpop() {
        Dotpop = getLayoutInflater().inflate(R.layout.layout_popup, null);
        popDotlist = (ListView) Dotpop.findViewById(R.id.listview);
        DotlistData = new ArrayList<NameModle>();
        DotlistData.add(new NameModle("点阵64X32"));
        DotlistData.add(new NameModle("点阵64X64"));
        DotlistData.add(new NameModle("点阵128X64"));
        DotlistData.add(new NameModle("点阵128X128"));
        DotlistData.add(new NameModle("取消"));
        DotlistAdapter = new MyAdapter<NameModle>((ArrayList) DotlistData, R.layout.layout_poplist) {
            @Override
            public void bindView(ViewHolder holder, NameModle obj) {
                holder.setText(R.id.text, obj.getname());
            }
        };

        popDotlist.setAdapter(DotlistAdapter);
        popDotlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView text = (TextView) view.findViewById(R.id.text);
                String textview = text.getText().toString();
                switch (textview) {
                    case "点阵64X32":
                        myview.setLineX(64);
                        myview.setLineY(32);
                        myview.invalidate();
                        mypaint.clearList();
                        mypaint.invalidate();
                        zeromap.put(4 + "", 00 + "");
                        popDot.dismiss();
                        break;
                    case "点阵64X64":
                        myview.setLineX(64);
                        myview.setLineY(64);
                        myview.invalidate();
                        mypaint.clearList();
                        mypaint.invalidate();
                        zeromap.put(4 + "", 01 + "");
                        popDot.dismiss();
                        break;
                    case "点阵128X64":
                        myview.setLineX(128);
                        myview.setLineY(64);
                        myview.invalidate();
                        mypaint.clearList();
                        mypaint.invalidate();
                        zeromap.put(4 + "", 02 + "");
                        popDot.dismiss();
                        break;
                    case "点阵128X128":
                        myview.setLineX(128);
                        myview.setLineY(128);
                        myview.invalidate();
                        mypaint.clearList();
                        mypaint.invalidate();
                        zeromap.put(4 + "", 03 + "");
                        popDot.dismiss();
                        break;
                    case "取消":
                        popDot.dismiss();
                        break;

                }
            }
        });

        popDot = new PopupWindow(Dotpop, ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, true);
        popDot.setBackgroundDrawable(new BitmapDrawable());
        popDot.setFocusable(true);
        popDot.setTouchable(true);
        popDot.setOutsideTouchable(true);

        popDot.showAtLocation(findViewById(R.id.btnFlashing), Gravity.CENTER_HORIZONTAL, 0, 0);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;
        getWindow().setAttributes(lp);


        popDot.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1f;
                getWindow().setAttributes(lp);
            }
        });

    }

    private View Settingpop;
    private ListView popSettinglist;
    private PopupWindow popSetting;
    private MyAdapter<NameModle> SettinglistAdapter = null;
    private List<NameModle> SettinglistData = null;

    public void setBtnSettingpop() {
        Settingpop = getLayoutInflater().inflate(R.layout.layout_popup, null);
        popSettinglist = (ListView) Settingpop.findViewById(R.id.listview);
        SettinglistData = new ArrayList<NameModle>();
        SettinglistData.add(new NameModle("重新联机"));
        SettinglistData.add(new NameModle("点阵规模设置"));
        SettinglistData.add(new NameModle("关灯设置和定时设置"));
        SettinglistData.add(new NameModle("取消"));
        SettinglistAdapter = new MyAdapter<NameModle>((ArrayList) SettinglistData, R.layout.layout_poplist) {
            @Override
            public void bindView(ViewHolder holder, NameModle obj) {
                holder.setText(R.id.text, obj.getname());
            }
        };

        popSettinglist.setAdapter(SettinglistAdapter);
        popSettinglist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView text = (TextView) view.findViewById(R.id.text);
                String textview = text.getText().toString();
                switch (textview) {
                    case "重新联机":
                        popSetting.dismiss();
                        break;
                    case "点阵规模设置":
                        setBtndotpop();
                        popSetting.dismiss();
                        break;
                    case "关灯设置和定时设置":
                        setPopTime();
                        popSetting.dismiss();
                        break;
                    case "取消":
                        popSetting.dismiss();
                        break;

                }
            }
        });

        popSetting = new PopupWindow(Settingpop, ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT, true);
        popSetting.setBackgroundDrawable(new BitmapDrawable());
        popSetting.setFocusable(true);
        popSetting.setTouchable(true);
        popSetting.setOutsideTouchable(true);

        btnSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popSetting.showAtLocation(findViewById(R.id.btnSet), Gravity.CENTER_HORIZONTAL, 0, 0);
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 0.5f;
                getWindow().setAttributes(lp);

            }
        });

        popSetting.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1f;
                getWindow().setAttributes(lp);
            }
        });

    }

    private PopupWindow popTime;

    private void setPopTime() {
        moreTime = getLayoutInflater().inflate(R.layout.layout_start_timefilter, null);
        datePicker = (DatePicker) moreTime.findViewById(R.id.datePicker);
        timePicker = (TimePicker) moreTime.findViewById(R.id.timePicker);
        btnOK = (Button) moreTime.findViewById(R.id.btnOK);
        btnCancel = (Button) moreTime.findViewById(R.id.btnCancel);
        popTime = new PopupWindow(moreTime, ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, true);
        popTime.setBackgroundDrawable(new BitmapDrawable());
        popTime.setFocusable(true);
        popTime.setTouchable(true);
        popTime.setOutsideTouchable(true);
        popTime.showAtLocation(findViewById(R.id.btnSet), Gravity.CENTER, 0, 0);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;
        getWindow().setAttributes(lp);


        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int year = datePicker.getYear();
                int month = datePicker.getMonth() + 1;
                int day = datePicker.getDayOfMonth();
                String date = year + "-" + month + "-" + day;
                popTime.dismiss();
            }
        });


        btnCancel.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                popTime.dismiss();
            }
        });

        popTime.setOnDismissListener(new PopupWindow.OnDismissListener()

        {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1f;
                getWindow().setAttributes(lp);
            }
        });

    }

    private View Staticpop;
    private ListView popStaticlist;
    private PopupWindow popStatic;
    private MyAdapter<NameModle> StaticlistAdapter = null;
    private List<NameModle> StaticlistData = null;

    public void setBtnYespop() {
        Staticpop = getLayoutInflater().inflate(R.layout.layout_popup, null);
        popStaticlist = (ListView) Staticpop.findViewById(R.id.listview);
        StaticlistData = new ArrayList<NameModle>();
        StaticlistData.add(new NameModle("确定"));
        StaticlistData.add(new NameModle("取消"));
        StaticlistAdapter = new MyAdapter<NameModle>((ArrayList) StaticlistData, R.layout.layout_poplist) {
            @Override
            public void bindView(ViewHolder holder, NameModle obj) {
                holder.setText(R.id.text, obj.getname());
            }
        };

        popStaticlist.setAdapter(StaticlistAdapter);
        popStaticlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView text = (TextView) view.findViewById(R.id.text);
                String textview = text.getText().toString();
                switch (textview) {
                    case "确定":
//                        state = 0x04;
                        zeromap.put(6 + "", 4 + "");
                        popStatic.dismiss();
                        break;
                    case "取消":
                        popStatic.dismiss();
                        break;

                }
            }
        });

        popStatic = new PopupWindow(Staticpop, ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT, true);
        popStatic.setBackgroundDrawable(new BitmapDrawable());
        popStatic.setFocusable(true);
        popStatic.setTouchable(true);
        popStatic.setOutsideTouchable(true);

        popStatic.showAtLocation(findViewById(R.id.btnStatic), Gravity.CENTER_HORIZONTAL, 0, 0);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;
        getWindow().setAttributes(lp);


        popStatic.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1f;
                getWindow().setAttributes(lp);
            }
        });

    }


    private View Rotationpop;
    private ListView popRotationlist;
    private PopupWindow popRotation;
    private MyAdapter<NameModle> RotationlistAdapter = null;
    private List<NameModle> RotationlistData = null;

    public void setbtnRotationpop() {
        Rotationpop = getLayoutInflater().inflate(R.layout.layout_popup, null);
        popRotationlist = (ListView) Rotationpop.findViewById(R.id.listview);
        RotationlistData = new ArrayList<NameModle>();
        RotationlistData.add(new NameModle("旋转"));
        RotationlistData.add(new NameModle("满屏旋转"));
        RotationlistData.add(new NameModle("左旋转"));
        RotationlistData.add(new NameModle("满屏左旋转"));
        RotationlistData.add(new NameModle("取消"));
        RotationlistAdapter = new MyAdapter<NameModle>((ArrayList) RotationlistData, R.layout.layout_poplist) {
            @Override
            public void bindView(ViewHolder holder, NameModle obj) {
                holder.setText(R.id.text, obj.getname());
            }
        };

        popRotationlist.setAdapter(RotationlistAdapter);
        popRotationlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView text = (TextView) view.findViewById(R.id.text);
                String textview = text.getText().toString();
                switch (textview) {
                    case "旋转":
//                        state = 0x2A;
                        zeromap.put(6 + "", 42 + "");
                        popRotation.dismiss();
                        break;
                    case "满屏旋转":
//                        state = 0x2B;
                        zeromap.put(6 + "", 43 + "");
                        popRotation.dismiss();
                        break;
                    case "左旋转":
//                        state = 0x2C;
                        zeromap.put(6 + "", 44 + "");
                        popRotation.dismiss();
                        break;
                    case "满屏左旋转":
//                        state = 0x2D;
                        zeromap.put(6 + "", 45 + "");
                        popRotation.dismiss();
                        break;
                    case "取消":
                        popRotation.dismiss();
                        break;

                }
            }
        });

        popRotation = new PopupWindow(Rotationpop, ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT, true);
        popRotation.setBackgroundDrawable(new BitmapDrawable());
        popRotation.setFocusable(true);
        popRotation.setTouchable(true);
        popRotation.setOutsideTouchable(true);

        btnRotation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popRotation.showAtLocation(findViewById(R.id.btnRotation), Gravity.CENTER_HORIZONTAL, 0, 0);
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 0.5f;
                getWindow().setAttributes(lp);

            }
        });

        popRotation.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1f;
                getWindow().setAttributes(lp);
            }
        });

    }

    public static byte intToByte(int x) {
        return (byte) x;
    }

    private void send() {
        getLED();
        int zsize = zeromap.size();
        int forsize = 0;
        if (zsize % 17 > 0) {
            forsize = zsize / 17 + 1;
        } else {
            forsize = zsize / 17;
        }
        for (int a = 0; a < forsize + 1; a++) {
            if (a == 0) {
                int b = forsize & 0xff;
                byte c = intToByte(b);
                byte[] aa = {0x04, 0x00, c};
                BLEServerActivity.write(aa);
            } else {
                int a0, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16;
                byte b0, b1, b2, b3, b4, b5, b6, b7, b8, b9, b10, b11, b12, b13, b14, b15, b16;
                int b = a & 0xff;
                byte c = intToByte(b);
                int a17 = (a - 1) * 17;
                int x0 = a17 + 1, x1 = a17 + 2, x2 = a17 + 3, x3 = a17 + 4, x4 = a17 + 5, x5 = a17 + 6, x6 = a17 + 7, x7 = a17 + 8, x8 = a17 + 9, x9 = a17 + 10, x10 = a17 + 11, x11 = a17 + 12, x12 = a17 + 13, x13 = a17 + 14, x14 = a17 + 15, x15 = a17 + 16, x16 = a17 + 17;
                if (zeromap.get(x0 + "") != null) {
                    String get1 = zeromap.get(x0 + "");
                    if (x0 > 32) {
                        a0 = Integer.valueOf(get1, 2);
                    } else {
                        a0 = Integer.valueOf(get1, 10);
                    }
                    b0 = intToByte(a0);
                } else {
                    b0 = 0x00;
                }
                if (zeromap.get(x1 + "") != null) {
                    String get2 = zeromap.get(x1 + "");
                    if (x1 > 32) {
                        a1 = Integer.valueOf(get2, 2);
                    } else {
                        a1 = Integer.valueOf(get2, 10);
                    }
                    b1 = intToByte(a1);
                } else {
                    byte[] aa = {0x04, c, c, b0};
                    sendWrite(aa);
                    break;
                }
                if (zeromap.get(x2 + "") != null) {
                    String get3 = zeromap.get(x2 + "");
                    if (x2 > 32) {
                        a2 = Integer.valueOf(get3, 2);
                    } else {
                        a2 = Integer.valueOf(get3, 10);
                    }
                    b2 = intToByte(a2);
                } else {
                    byte[] aa = {0x04, c, c, b0, b1};
                    sendWrite(aa);
                    break;
                }
                if (zeromap.get(x3 + "") != null) {
                    String get4 = zeromap.get(x3 + "");
                    if (x3 > 32) {
                        a3 = Integer.valueOf(get4, 2);
                    } else {
                        a3 = Integer.valueOf(get4, 10);
                    }
                    b3 = intToByte(a3);
                } else {
                    byte[] aa = {0x04, c, c, b0, b1, b2};
                    sendWrite(aa);
                    break;
                }
                if (zeromap.get(x4 + "") != null) {
                    String get5 = zeromap.get(x4 + "");
                    if (x4 > 32) {
                        a4 = Integer.valueOf(get5, 2);
                    } else {
                        a4 = Integer.valueOf(get5, 10);
                    }
                    b4 = intToByte(a4);
                } else {
                    byte[] aa = {0x04, c, c, b0, b1, b2, b3};
                    sendWrite(aa);
                    break;
                }
                if (zeromap.get(x5 + "") != null) {
                    String get6 = zeromap.get(x5 + "");
                    if (x5 > 32) {
                        a5 = Integer.valueOf(get6, 2);
                    } else {
                        a5 = Integer.valueOf(get6, 10);
                    }
                    b5 = intToByte(a5);
                } else {
                    byte[] aa = {0x04, c, c, b0, b1, b2, b3, b4};
                    sendWrite(aa);
                    break;
                }
                if (zeromap.get(x6 + "") != null) {
                    String get7 = zeromap.get(x6 + "");
                    if (x6 > 32) {
                        a6 = Integer.valueOf(get7, 2);
                    } else {
                        a6 = Integer.valueOf(get7, 10);
                    }
                    b6 = intToByte(a6);
                } else {
                    byte[] aa = {0x04, c, c, b0, b1, b2, b3, b4, b5};
                    sendWrite(aa);
                    break;
                }
                if (zeromap.get(x7 + "") != null) {
                    String get8 = zeromap.get(x7 + "");
                    if (x7 > 32) {
                        a7 = Integer.valueOf(get8, 2);
                    } else {
                        a7 = Integer.valueOf(get8, 10);
                    }
                    b7 = intToByte(a7);
                } else {
                    byte[] aa = {0x04, c, c, b0, b1, b2, b3, b4, b5, b6};
                    sendWrite(aa);
                    break;
                }
                if (zeromap.get(x8 + "") != null) {
                    String get9 = zeromap.get(x8 + "");
                    if (x8 > 32) {
                        a8 = Integer.valueOf(get9, 2);
                    } else {
                        a8 = Integer.valueOf(get9, 10);
                    }
                    b8 = intToByte(a8);
                } else {
                    byte[] aa = {0x04, c, c, b0, b1, b2, b3, b4, b5, b6, b7};
                    sendWrite(aa);
                    break;
                }
                if (zeromap.get(x9 + "") != null) {
                    String get10 = zeromap.get(x9 + "");
                    if (x9 > 32) {
                        a9 = Integer.valueOf(get10, 2);
                    } else {
                        a9 = Integer.valueOf(get10, 10);
                    }
                    b9 = intToByte(a9);
                } else {
                    byte[] aa = {0x04, c, c, b0, b1, b2, b3, b4, b5, b6, b7, b8};
                    sendWrite(aa);
                    break;
                }
                if (zeromap.get(x10 + "") != null) {
                    String get11 = zeromap.get(x10 + "");
                    if (x10 > 32) {
                        a10 = Integer.valueOf(get11, 2);
                    } else {
                        a10 = Integer.valueOf(get11, 10);
                    }
                    b10 = intToByte(a10);
                } else {
                    byte[] aa = {0x04, c, c, b0, b1, b2, b3, b4, b5, b6, b7, b8, b9};
                    sendWrite(aa);
                    break;
                }
                if (zeromap.get(x11 + "") != null) {
                    String get12 = zeromap.get(x11 + "");
                    if (x11 == 12) {
                        a11 = Integer.valueOf(get12, 10);
                    } else if (x11 > 32) {
                        a11 = Integer.valueOf(get12, 2);
                    } else {
                        a11 = Integer.valueOf(get12, 10);
                    }
                    b11 = intToByte(a11);
                } else {
                    byte[] aa = {0x04, c, c, b0, b1, b2, b3, b4, b5, b6, b7, b8, b9, b10};
                    sendWrite(aa);
                    break;
                }
                if (zeromap.get(x12 + "") != null) {
                    String get13 = zeromap.get(x12 + "");
                    if (x12 == 13) {
                        a12 = Integer.valueOf(get13, 10);
                    } else if (x11 > 32) {
                        a12 = Integer.valueOf(get13, 2);
                    } else {
                        a12 = Integer.valueOf(get13, 10);
                    }
                    b12 = intToByte(a12);
                } else {
                    byte[] aa = {0x04, c, c, b0, b1, b2, b3, b4, b5, b6, b7, b8, b9, b10, b11};
                    sendWrite(aa);
                    break;
                }
                if (zeromap.get(x13 + "") != null) {
                    String get14 = zeromap.get(x13 + "");
                    if (x13 == 14) {
                        a13 = Integer.valueOf(get14, 10);
                    } else if (x11 > 32) {
                        a13 = Integer.valueOf(get14, 2);
                    } else {
                        a13 = Integer.valueOf(get14, 10);
                    }
                    b13 = intToByte(a13);
                } else {
                    byte[] aa = {0x04, c, c, b0, b1, b2, b3, b4, b5, b6, b7, b8, b9, b10, b11, b12};
                    sendWrite(aa);
                    break;
                }
                if (zeromap.get(x14 + "") != null) {
                    String get15 = zeromap.get(x14 + "");
                    if (x14 == 15) {
                        a14 = Integer.valueOf(get15, 10);
                    } else if (x11 > 32) {
                        a14 = Integer.valueOf(get15, 2);
                    } else {
                        a14 = Integer.valueOf(get15, 10);
                    }
                    b14 = intToByte(a14);
                } else {
                    byte[] aa = {0x04, c, c, b0, b1, b2, b3, b4, b5, b6, b7, b8, b9, b10, b11, b12, b13};
                    sendWrite(aa);
                    break;
                }
                if (zeromap.get(x15 + "") != null) {
                    String get16 = zeromap.get(x15 + "");
                    if (x15 == 16) {
                        a15 = Integer.valueOf(get16, 10);
                    } else if (x11 > 32) {
                        a15 = Integer.valueOf(get16, 2);
                    } else {
                        a15 = Integer.valueOf(get16, 10);
                    }
                    b15 = intToByte(a15);
                } else {
                    byte[] aa = {0x04, c, c, b0, b1, b2, b3, b4, b5, b6, b7, b8, b9, b10, b11, b12, b13, b14};
                    sendWrite(aa);
                    break;
                }
                if (zeromap.get(x16 + "") != null) {
                    String get17 = zeromap.get(x16 + "");
                    if (x16 > 32) {
                        a16 = Integer.valueOf(get17, 2);
                    } else {
                        a16 = Integer.valueOf(get17, 10);
                    }
                    b16 = intToByte(a16);
                } else {
                    byte[] aa = {0x04, c, c, b0, b1, b2, b3, b4, b5, b6, b7, b8, b9, b10, b11, b12, b13, b14, b15};
                    sendWrite(aa);
                    break;
                }

                byte[] aa = {0x04, c, c, b0, b1, b2, b3, b4, b5, b6, b7, b8, b9, b10, b11, b12, b13, b14, b15, b16};
                sendWrite(aa);
            }
        }
    }

    private void sendWrite(byte[] aa) {
        try {
            Thread.sleep(100);
            BLEServerActivity.write(aa);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void isExist(String path) {
        File file = new File(path);
        //判断文件夹是否存在,如果不存在则创建文件夹
        if (!file.exists()) {
            file.mkdir();
        }
    }

    public static class PermisionUtils {

        // Storage Permissions
        private static final int REQUEST_EXTERNAL_STORAGE = 1;
        private static String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};

        /**
         * Checks if the app has permission to write to device storage
         * If the app does not has permission then the user will be prompted to
         * grant permissions
         *
         * @param activity
         */
        public static void verifyStoragePermissions(Activity activity) {
            // Check if we have write permission
            int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (permission != PackageManager.PERMISSION_GRANTED) {
                // We don't have permission so prompt the user
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
        }

    }

    public static void saveFile(String str) {
        String filePath = null;
        boolean hasSDCard = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        String fileName = new SimpleDateFormat("yyyyMMddHHmmss")
                .format(new Date()) + ".jpg";
        if (hasSDCard) { // SD卡根目录的hello.text
            filePath = Environment.getExternalStorageDirectory().toString() + File.separator + "/notes" + "/" + fileName + ".txt";
        } else  // 系统下载缓存根目录的hello.text
            filePath = Environment.getDownloadCacheDirectory().toString() + File.separator + "/notes" + "/" + fileName + ".txt";

        try {
            File file = new File(filePath);
            if (!file.exists()) {
                File dir = new File(file.getParent());
                dir.mkdirs();
                file.createNewFile();
            }
            FileOutputStream outStream = new FileOutputStream(file);
            outStream.write(str.getBytes());
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
