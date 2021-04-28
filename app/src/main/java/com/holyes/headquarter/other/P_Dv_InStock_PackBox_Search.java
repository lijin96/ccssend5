package com.holyes.headquarter.other;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.ccssend5.R;
import com.holyes.ccssend5.lib.AccessWeb;
import com.holyes.ccssend5.lib.MySound;
import com.holyes.ccssend5.lib.ShowMessage;
import com.holyes.ccssend5.lib.SysUserInfo;
import com.holyes.ccssend5.lib.bluetooth.BluetoothService;
import com.holyes.ccssend5.lib.bluetooth.BluetoothUtil;
import com.holyes.ccssend5.lib.bluetooth.BoxTag;
import com.holyes.ccssend5.lib.bluetooth.DeviceListActivity;
import com.holyes.ccssend5.utils.SomeUtils;

import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * @ClassName: P_Dv_InStock_PackBox_Search
 * @Description: 总公司检查盒标信息，补打盒标
 * @Author: lijin
 * @Date: 2021/3/10 14:00
 */
public class P_Dv_InStock_PackBox_Search extends Activity implements View.OnClickListener {

    private Context mContext;
    private SysUserInfo sysUserInfo;
    private AccessWeb accessWeb;
    private BoxTag boxTag;
    private MySound sound;

    private EditText et_barcode;
    private TextView tv_box_message, tv_connect_state;
    private Button btn_connect, btn_again_print_boxcode, btn_finish;

    private String tLoginId = "", tTempBoxNo = "", result = "", boxMessage = "";
    private String BoxNo, BrandName, SerialName, Model, color, Num, UserCode, PackDate;

    private final int HandToaskErrorMsg = 7;
    private final int HandSuccessUpdateTextUi = 8;
    private final int HandFailUpdateTextUi = 9;

    private boolean isConnectedBluetooth = false;

    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothService mService = null;
    private String connectedDeviceName = "", connectedDeviceAddress;
    //	private Handler mHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_p_dv_instock_packbox_search);
        mContext = this;
        sysUserInfo = new SysUserInfo(mContext);
        accessWeb = new AccessWeb(mContext);
        sound = MySound.getMySound(this);
        //		mHandler = new handShowMsg();

        et_barcode = (EditText) findViewById(R.id.et_barcode);
        tv_box_message = (TextView) findViewById(R.id.tv_box_message);
        tv_connect_state = (TextView) findViewById(R.id.tv_connect_state);
        btn_connect = (Button) findViewById(R.id.btn_connect);
        btn_again_print_boxcode = (Button) findViewById(R.id.btn_again_print_boxcode);
        btn_finish = (Button) findViewById(R.id.btn_finish);

        et_barcode.setOnKeyListener(new EtOnKeyListener());
        btn_connect.setOnClickListener(this);
        btn_again_print_boxcode.setOnClickListener(this);
        btn_finish.setOnClickListener(this);

        tLoginId = sysUserInfo.getLoginid();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            mService = new BluetoothService(this, mHandler);
            tryConnectLastBluetooth();
        } else {
            ShowMessage.Show(mContext, "蓝牙未打开或不可用，请到系统设置中检查");
        }
    }

    /**
     * 尝试连接上次连接的蓝牙
     */
    public void tryConnectLastBluetooth() {
        connectedDeviceName = sysUserInfo.getConnectedBluetoothName();
        connectedDeviceAddress = sysUserInfo.getConnectedBluetoothAddress();
        if (!connectedDeviceAddress.isEmpty()) {
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(connectedDeviceAddress);
            mService.connect(device);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mService != null) {
            if (mService.getState() == BluetoothService.STATE_NONE) {
                mService.start();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mService != null) mService.stop();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case BluetoothUtil.REQUEST_CONNECT_DEVICE:
                    if (resultCode == Activity.RESULT_OK) {
                        connectedDeviceAddress = data.getStringExtra("deviceAddress");
                        connectedDeviceName = data.getStringExtra("deviceName");
                        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(connectedDeviceAddress);
                        mService.connect(device);
                    }
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (SomeUtils.isDoubleClick(mContext, true)) {
            finish();
        }
    }

    class EtOnKeyListener implements View.OnKeyListener {

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (et_barcode.getText().toString().trim().isEmpty()) {
                        return true;
                    }


                    String tBarcode = "";
                    if (et_barcode.getText().toString().trim().indexOf("=") != -1) {
                        //包含
                        tBarcode = SomeUtils.InterceptCode(mContext, et_barcode.getText().toString().trim());
                    } else {
                        //不包含
                        tBarcode = et_barcode.getText().toString().trim();
                    }


                    if (!SomeUtils.isAllNumber(mContext, tBarcode)) {
                        MySound.errorSound();
                        ShowMessage.Show(mContext, "请扫描正确的物流码【" + tBarcode + "】");
                        et_barcode.setText("");
                        return true;
                    }

                    tTempBoxNo = tBarcode;
                    doP_Dv_InStock_PackBox_Search(tTempBoxNo);
                    et_barcode.setText("");
                }
                return true;
            } else {

                return false;
            }
        }

    }

    /**
     * 查找盒标信息
     */
    public void doP_Dv_InStock_PackBox_Search(final String tTempBoxNo) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    result = accessWeb.P_Dv_InStock_PackBox_Search(tLoginId, tTempBoxNo);
                    JSONObject jsonObject = new JSONObject(result);
                    //BoxNo,SerialName,Num,UserCode,BrandName,Model,Color,PackDate;
                    BoxNo = jsonObject.getString("BoxNo");
                    BrandName = jsonObject.getString("BrandName");
                    SerialName = jsonObject.getString("SerialName");
                    Model = jsonObject.getString("Model");
                    color = jsonObject.getString("Color");
                    Num = jsonObject.getString("Num");
                    UserCode = jsonObject.getString("UserCode");
                    PackDate = jsonObject.getString("PackDate");
                    PackDate = PackDate.replace("/", ".");//把日期格式转换一下
                    boxTag = new BoxTag(BoxNo, BrandName, SerialName, Model, color, Num, UserCode, PackDate);
                    boxMessage = "盒标：" + BoxNo + "\n品牌：" + BrandName + "\n系列：" + SerialName + "\n型号："
                            + Model + "\n色号：" + color + "\n数量：" + Num + "\n工号：" + UserCode + "\n日期：" + PackDate;
                    ShowMessage.ShowMsg(mHandler, HandSuccessUpdateTextUi, boxMessage);

                } catch (Exception e) {
                    ShowMessage.ShowMsg(mHandler, HandFailUpdateTextUi, e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();
    }


    private Handler mHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case HandToaskErrorMsg:
                    MySound.errorSound();
                    ShowMessage.Show(mContext, msg.obj.toString());
                    break;
                case HandSuccessUpdateTextUi:
                    MySound.scanSound();
                    tv_box_message.setText(msg.obj.toString());
                    break;
                case HandFailUpdateTextUi:
                    MySound.errorSound();
                    tv_box_message.setText("查找盒标错误：" + msg.obj.toString());
                    break;


                case BluetoothUtil.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            btn_connect.setText("断开");
                            tv_connect_state.setText("已连接:" + connectedDeviceName);
                            tv_connect_state.setTextColor(Color.parseColor("#008000"));
                            isConnectedBluetooth = true;
                            sysUserInfo.setConnectedBluetoothName(connectedDeviceName);
                            sysUserInfo.setConnectedBluetoothAddress(connectedDeviceAddress);
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            btn_connect.setText("连接");
                            tv_connect_state.setText("正在连接:" + connectedDeviceName + "...");
                            tv_connect_state.setTextColor(Color.BLACK);
                            isConnectedBluetooth = false;
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            btn_connect.setText("连接");
                            tv_connect_state.setText("未连接");
                            tv_connect_state.setTextColor(Color.RED);
                            isConnectedBluetooth = false;
                            break;
                    }
                    break;

                default:
                    break;

            }
            return false;
        }
    });


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_connect:
                if (btn_connect.getText().equals("连接")) {
                    Intent serverIntent = new Intent(mContext, DeviceListActivity.class);
                    startActivityForResult(serverIntent, BluetoothUtil.REQUEST_CONNECT_DEVICE);
                } else {
                    mService.stop();
                }
                break;
            case R.id.btn_again_print_boxcode:
                if (isConnectedBluetooth) {
                    printBoxCode(boxTag);
                } else {
                    ShowMessage.ShowMsg(mHandler, HandToaskErrorMsg, "未连接蓝牙，请先连接蓝牙");
                }

                break;
            case R.id.btn_finish:
                SomeUtils.clickKeyBack();
                break;

            default:
                break;
        }
    }

    public void printBoxCode(BoxTag boxTag) {
        if (boxTag == null) {
            ShowMessage.ShowMsg(mHandler, HandToaskErrorMsg, "没有数据可以打印");
            return;
        }
        String message = "";

        if (isFileExists("lab_" + sysUserInfo.getEnterpriseId().toString() + ".txt")) {
            message = SomeUtils.readAssetsTxt(mContext, "lab_" + sysUserInfo.getEnterpriseId().toString());
        } else {
//			message = SomeUtils.readFileString(mContext,BluetoothUtil.BoxTagModel);
            message = SomeUtils.readAssetsTxt(mContext, "lab_jb");
        }

        sendMessage(message, boxTag);
    }


    //判断assets文件夹 文件是否存在
    private boolean isFileExists(String filename) {
        AssetManager assetManager = getAssets();
        try {
            String[] names = assetManager.list("");
            for (int i = 0; i < names.length; i++) {
//	            LogUtil.e(names[i]);
                if (names[i].equals(filename.trim())) {
                    System.out.println(filename + "存在");
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(filename + "不存在");
            return false;
        }
        System.out.println(filename + "不存在");
        return false;
    }

    /**
     * 发送给蓝牙打印
     *
     * @param message 模本字符串
     * @param boxTag  要打印的数据封装成的类
     */
    private void sendMessage(String message, BoxTag boxTag) {
        if (mService.getState() != BluetoothService.STATE_CONNECTED) {
            ShowMessage.Show(mContext, "未连接蓝牙");
            return;
        }
        if (isFileExists("lab_" + sysUserInfo.getEnterpriseId().toString() + ".txt")) {
            message = message.replace("%BOX", boxTag.getBoxNo());
            message = message.replace("%B", boxTag.getBrandName());
            message = message.replace("%M", boxTag.getModel());
            message = message.replace("%C", boxTag.getColor());
            message = message.replace("%N", boxTag.getNum());
            message = message.replace("%D", boxTag.getPackDate());
        } else {
            message = message.replace("%BOX", boxTag.getBoxNo());
            message = message.replace("%U", boxTag.getUserCode());
            message = message.replace("%B", boxTag.getBrandName());
            message = message.replace("%S", boxTag.getSerialName());
            message = message.replace("%M", boxTag.getModel());
            message = message.replace("%C", boxTag.getColor());
            message = message.replace("%N", boxTag.getNum());
            message = message.replace("%D", boxTag.getPackDate());
        }
        //	        	byte[] send = readFileByte();
        byte[] send;
        try {
            send = message.getBytes("GBK");
            mService.write(send);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取点击事件,是否隐藏键盘
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        SomeUtils.isNeedHideAndDo(this, ev);
        return super.dispatchTouchEvent(ev);
    }

    //设置字体为默认大小，不随系统字体大小改而改变
    @Override
    public Resources getResources() {
        Resources resources = super.getResources();
        if (resources != null) {
            Configuration configuration = resources.getConfiguration();
            if (configuration != null && configuration.fontScale != 1.0f) {
                configuration.fontScale = 1.0f;//这里只设置字体，故不使用下面注释的方法
//	                configuration.setToDefaults();
                resources.updateConfiguration(configuration, resources.getDisplayMetrics());
            }
        }
        return resources;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}

