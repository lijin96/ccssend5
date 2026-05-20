package com.holyes.headquarter.other;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
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
import com.holyes.ccssend5.lib.bluetooth.BluetoothManager;
import com.holyes.ccssend5.lib.bluetooth.BluetoothService;
import com.holyes.ccssend5.lib.bluetooth.BluetoothUtil;
import com.holyes.ccssend5.lib.bluetooth.BoxTag;
import com.holyes.ccssend5.lib.bluetooth.DeviceListActivity;
import com.holyes.ccssend5.utils.SomeUtils;

import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.Set;

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
    private String BoxNo, BrandName, SerialName, Model, color, Num, UserCode, PackDate,StockName;

    private final int HandToaskErrorMsg = 7;
    private final int HandSuccessUpdateTextUi = 8;
    private final int HandFailUpdateTextUi = 9;

    private boolean isConnectedBluetooth = false;

    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothManager bluetoothManager = null;
    private String connectedDeviceName = "", connectedDeviceAddress="";
    //	private Handler mHandler;


    private String  tPageType="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_p_dv_instock_packbox_search);
        mContext = this;
        sysUserInfo = new SysUserInfo(mContext);
        accessWeb = new AccessWeb(mContext);
        sound = MySound.getMySound(this);
        //		mHandler = new handShowMsg();
        tPageType = getIntent().getStringExtra("PageType");

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

        // 使用全局蓝牙管理器
        bluetoothManager = BluetoothManager.getInstance();
        mBluetoothAdapter = bluetoothManager.getBluetoothAdapter();

        connectedDeviceName = sysUserInfo.getConnectedBluetoothName();
        connectedDeviceAddress = sysUserInfo.getConnectedBluetoothAddress();

        if (bluetoothManager.isBluetoothAvailable()) {
            // 如果已经连接，则不需要重新初始化
            if (!bluetoothManager.isBluetoothConnected()) {
                // 自动连接上次保存的蓝牙设备
                boolean isAutoConnecting = bluetoothManager.initBluetoothServiceAndAutoConnect(this, mHandler);
                if (isAutoConnecting) {
                    // 正在自动连接，更新UI状态
                    tv_connect_state.setText("正在连接:" + connectedDeviceName + "...");
                    tv_connect_state.setTextColor(Color.BLACK);
                    btn_connect.setText("连接");
                } else {
                    // 没有保存的设备地址，显示未连接状态
                    tv_connect_state.setText("未连接");
                    tv_connect_state.setTextColor(Color.RED);
                    btn_connect.setText("连接");
                }
            } else {
                // 已经连接，从BluetoothManager获取当前连接的设备信息
                String currentDeviceName = bluetoothManager.getConnectedDeviceName();
                String currentDeviceAddress = bluetoothManager.getConnectedDeviceAddress();

                if (currentDeviceName != null && !currentDeviceName.isEmpty()) {
                    connectedDeviceName = currentDeviceName;
                    connectedDeviceAddress = currentDeviceAddress;
                } else {
                    // 如果BluetoothManager中的设备名称为空，使用保存的设备信息
                    // 这种情况可能发生在连接成功但设备名称为空的情况下
                    if (connectedDeviceName == null || connectedDeviceName.isEmpty()) {
                        connectedDeviceName = "未知设备";
                    }
                }

                // 直接更新UI状态
                isConnectedBluetooth = true;
                tv_connect_state.setText("已连接:" + connectedDeviceName);
                tv_connect_state.setTextColor(Color.parseColor("#008000"));
                btn_connect.setText("断开");
            }
        } else {
            ShowMessage.Show(mContext, "蓝牙未打开或不可用，请到系统设置中检查");
        }
    }

    /**
     * 尝试连接上次连接的蓝牙（手动连接时使用）
     */
    public void tryConnectLastBluetooth() {
        connectedDeviceName = sysUserInfo.getConnectedBluetoothName();
        connectedDeviceAddress = sysUserInfo.getConnectedBluetoothAddress();
        if (!connectedDeviceAddress.isEmpty()) {
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(connectedDeviceAddress);
            bluetoothManager.connect(device);
        }
    }

    //判断当前蓝牙是否正在连接中（已优化为使用 BluetoothManager）
    public static boolean isAnyBluetoothPrinterConnected() {
        BluetoothManager bluetoothManager = BluetoothManager.getInstance();
        return bluetoothManager.isBluetoothConnected();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bluetoothManager != null && bluetoothManager.getBluetoothService() != null) {
            if (bluetoothManager.getBluetoothState() == BluetoothService.STATE_NONE) {
                bluetoothManager.start();
            }
        }
        // 延迟更新蓝牙状态，确保蓝牙服务已经初始化完成
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                updateBluetoothConnectionStatus();
            }
        }, 200); // 延迟200ms，给更多时间让蓝牙服务初始化

        // 再次延迟检查，确保状态同步
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                updateBluetoothConnectionStatus();
            }
        }, 500); // 延迟500ms再次检查
    }

    /**
     * 更新蓝牙连接状态
     */
    private void updateBluetoothConnectionStatus() {
        if (bluetoothManager == null) {
            return;
        }

        // 检查蓝牙服务是否存在
        if (bluetoothManager.getBluetoothService() == null) {
            // 如果蓝牙服务不存在，尝试重新初始化
            bluetoothManager.initBluetoothServiceAndAutoConnect(this, mHandler);
        }

        if (bluetoothManager.isBluetoothConnected()) {
            // 如果蓝牙已连接，更新界面状态
            String currentDeviceName = bluetoothManager.getConnectedDeviceName();
            String currentDeviceAddress = bluetoothManager.getConnectedDeviceAddress();

            if (currentDeviceName != null && !currentDeviceName.isEmpty()) {
                connectedDeviceName = currentDeviceName;
                connectedDeviceAddress = currentDeviceAddress;
            } else {
                // 如果BluetoothManager中的设备名称为空，使用保存的设备信息
                if (connectedDeviceName == null || connectedDeviceName.isEmpty()) {
                    connectedDeviceName = sysUserInfo.getConnectedBluetoothName();
                    connectedDeviceAddress = sysUserInfo.getConnectedBluetoothAddress();
                }
                if (connectedDeviceName == null || connectedDeviceName.isEmpty()) {
                    connectedDeviceName = "未知设备";
                }
            }

            // 更新UI状态
            isConnectedBluetooth = true;
            tv_connect_state.setText("已连接:" + connectedDeviceName);
            tv_connect_state.setTextColor(Color.parseColor("#008000"));
            btn_connect.setText("断开");
        } else {
            // 如果蓝牙未连接，更新界面状态
            isConnectedBluetooth = false;
            tv_connect_state.setText("未连接");
            tv_connect_state.setTextColor(Color.RED);
            btn_connect.setText("连接");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注意：不要在这里 stop，因为是全局的蓝牙服务，其他界面可能还在使用
        // 如果需要断开，应该在应用退出时调用 BluetoothManager.destroy()

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case BluetoothUtil.REQUEST_CONNECT_DEVICE:
                    if (resultCode == Activity.RESULT_OK) {
                        connectedDeviceAddress = data.getStringExtra("deviceAddress");
                        connectedDeviceName = data.getStringExtra("deviceName");

                        // 处理设备名称为空的情况
                        if (connectedDeviceName == null || connectedDeviceName.isEmpty()) {
                            connectedDeviceName = connectedDeviceAddress;
                        }

                        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(connectedDeviceAddress);

                        // 立即更新界面状态为"正在连接"
                        isConnectedBluetooth = false;
                        tv_connect_state.setText("正在连接:" + connectedDeviceName + "...");
                        tv_connect_state.setTextColor(Color.BLACK);
                        btn_connect.setText("连接");

                        // 开始连接
                        bluetoothManager.connect(device);
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
                        tBarcode = SomeUtils.UpdatefirstString(mContext,et_barcode.getText().toString().trim());
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
                    StockName = jsonObject.getString("StockName");
                    PackDate = PackDate.replace("/", ".");//把日期格式转换一下
                    boxTag = new BoxTag(BoxNo, BrandName, SerialName, Model, color, Num, UserCode, PackDate,StockName);
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


                case BluetoothUtil.MESSAGE_DEVICE_NAME:
                    // 获取连接的设备名称和地址
                    String deviceName = msg.getData().getString(BluetoothUtil.DEVICE_NAME);
                    String deviceAddress = msg.getData().getString(BluetoothUtil.DEVICE_ADDRESS);

                    // 确保设备名称不为空，如果为空则使用设备地址
                    if (deviceName == null || deviceName.isEmpty()) {
                        deviceName = deviceAddress;
                    }
                    if (deviceName == null || deviceName.isEmpty()) {
                        deviceName = "未知设备";
                    }

                    connectedDeviceName = deviceName;
                    if (deviceAddress != null && !deviceAddress.isEmpty()) {
                        connectedDeviceAddress = deviceAddress;
                    } else {
                        // 如果消息中没有设备地址，从BluetoothManager获取
                        connectedDeviceAddress = bluetoothManager.getConnectedDeviceAddress();
                    }

                    // 更新UI显示（不依赖连接状态检查，因为消息顺序可能不确定）
                    tv_connect_state.setText("已连接:" + connectedDeviceName);
                    tv_connect_state.setTextColor(Color.parseColor("#008000"));
                    btn_connect.setText("断开");
                    isConnectedBluetooth = true;

                    // 保存连接信息到sysUserInfo
                    sysUserInfo.setConnectedBluetoothName(connectedDeviceName);
                    sysUserInfo.setConnectedBluetoothAddress(connectedDeviceAddress);
                    break;

                case BluetoothUtil.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            // 连接成功，强制更新界面
                            // 使用已设置的设备信息或从BluetoothManager获取
                            String currentDeviceName = connectedDeviceName;
                            String currentDeviceAddress = connectedDeviceAddress;

                            if (currentDeviceName == null || currentDeviceName.isEmpty()) {
                                currentDeviceName = bluetoothManager.getConnectedDeviceName();
                                currentDeviceAddress = bluetoothManager.getConnectedDeviceAddress();
                            }

                            if (currentDeviceName == null || currentDeviceName.isEmpty()) {
                                currentDeviceName = "未知设备";
                            }

                            connectedDeviceName = currentDeviceName;
                            connectedDeviceAddress = currentDeviceAddress;

                            // 强制更新界面状态
                            btn_connect.setText("断开");
                            tv_connect_state.setText("已连接:" + connectedDeviceName);
                            tv_connect_state.setTextColor(Color.parseColor("#008000"));
                            isConnectedBluetooth = true;

                            // 保存连接信息
                            sysUserInfo.setConnectedBluetoothName(connectedDeviceName);
                            sysUserInfo.setConnectedBluetoothAddress(connectedDeviceAddress);
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            // 连接中状态（作为备用，因为onActivityResult已经设置了）
                            isConnectedBluetooth = false;
                            if (connectedDeviceName != null && !connectedDeviceName.isEmpty()) {
                                tv_connect_state.setText("正在连接:" + connectedDeviceName + "...");
                            } else {
                                tv_connect_state.setText("正在连接...");
                            }
                            tv_connect_state.setTextColor(Color.BLACK);
                            btn_connect.setText("连接");
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            // 确保状态重置
                            isConnectedBluetooth = false;
                            btn_connect.setText("连接");
                            tv_connect_state.setText("未连接");
                            tv_connect_state.setTextColor(Color.RED);
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
                    bluetoothManager.disconnectCurrentConnection();
                    // 立即更新界面状态并清理设备信息
                    btn_connect.setText("连接");
                    tv_connect_state.setText("未连接");
                    tv_connect_state.setTextColor(Color.RED);
                    isConnectedBluetooth = false;
                    connectedDeviceName = "";
                    connectedDeviceAddress = "";
                }
                break;
            case R.id.btn_again_print_boxcode:
                if (isConnectedBluetooth) {
//                    if (boxTag.getBoxNo().indexOf("P")>0){
//                        //如果码包含P，就是套标
//                        printBoxCodeV1(boxTag);
//                    }else{
                        printBoxCode(boxTag);
//                    }

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

    //盒标补打
    public void printBoxCode(BoxTag boxTag) {
        if (boxTag == null) {
            ShowMessage.ShowMsg(mHandler, HandToaskErrorMsg, "没有数据可以打印");
            return;
        }
        String message = "";
        if (tPageType.equals("factorypack")){
            message =SomeUtils.readAssetsTxt(mContext, "lab_gys");
        }else{
            if (isFileExists("lab_" + sysUserInfo.getEnterpriseId().toString() + ".txt")) {
                message = SomeUtils.readAssetsTxt(mContext, "lab_" + sysUserInfo.getEnterpriseId().toString());
            } else {
//			message = SomeUtils.readFileString(mContext,BluetoothUtil.BoxTagModel);
//            川久2022年2月25日17:20:05要求不显示时间
                message = SomeUtils.readAssetsTxt(mContext, "lab_Chuanj");
//            message = SomeUtils.readAssetsTxt(mContext, "lab_jb");
            }
        }

        sendMessage(message, boxTag);
    }


    //套标补打
    public void printBoxCodeV1(BoxTag boxTag) {

        if (boxTag == null) {
            ShowMessage.ShowMsg(mHandler, ShowMessage.HandShowMessage, "没有数据可以打印");
            return;
        }
//		lab_jb lab_meal
        String message = SomeUtils.readAssetsTxt(mContext, "lab_meal");
//		Log.i("main", "sendMessage--------判断boxTag");
        sendMessageV1(message, boxTag);

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
     * 套标发送给蓝牙打印
     *
     * @param message 模本字符串
     * @param boxTag  要打印的数据封装成的类
     */
    private void sendMessageV1(String message, BoxTag boxTag) {

        if (!bluetoothManager.isBluetoothConnected()) {
            ShowMessage.Show(mContext, "未连接蓝牙");
            return;
        }

        message = message.replace("%BOX", boxTag.getBoxNo());
        message = message.replace("%U", boxTag.getUserCode());
        if (boxTag.getBrandName().length() > 7) {
            message = message.replace("%B", boxTag.getBrandName().substring(0, 7));
            message = message.replace("%S", boxTag.getBrandName().substring(8));

        } else {
            message = message.replace("%B", boxTag.getBrandName());
            message = message.replace("%S", " ");
        }
        message = message.replace("%N", boxTag.getNum());
        message = message.replace("%D", boxTag.getPackDate());


        //byte[] send = readFileByte();
        byte[] send;
        try {
            send = message.getBytes("GBK");
            bluetoothManager.write(send);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }



    /**
     * 盒标发送给蓝牙打印
     *
     * @param message 模本字符串
     * @param boxTag  要打印的数据封装成的类
     */
    private void sendMessage(String message, BoxTag boxTag) {
        if (!bluetoothManager.isBluetoothConnected()) {
            ShowMessage.Show(mContext, "未连接蓝牙");
            return;
        }

        if (tPageType.equals("factorypack")){
            message = message.replace("%BOX", boxTag.getBoxNo());
            message = message.replace("%U", boxTag.getUserCode());
            message = message.replace("%B", boxTag.getBrandName());
            message = message.replace("%S", boxTag.getSerialName());
            message = message.replace("%M", boxTag.getModel());
            message = message.replace("%C", boxTag.getColor());
            message = message.replace("%N", boxTag.getNum());
            message = message.replace("%D", boxTag.getPackDate());
        }else {
            if (isFileExists("lab_" + sysUserInfo.getEnterpriseId().toString() + ".txt")) {
                //52万新  53帕兰德
                if (sysUserInfo.getEnterpriseId().toString().equals("52") || sysUserInfo.getEnterpriseId().toString().equals("53")) {
                    message = message.replace("%BOX", boxTag.getBoxNo());
                    message = message.replace("%U", boxTag.getUserCode());
                    message = message.replace("%B", boxTag.getBrandName());
                    message = message.replace("%S", boxTag.getSerialName());
                    message = message.replace("%M", boxTag.getModel());
                    message = message.replace("%C", boxTag.getColor());
                    message = message.replace("%N", boxTag.getNum());
//                message = message.replace("%D", boxTag.getPackDate());
                } else if (sysUserInfo.getEnterpriseId().toString().equals("12")) {
//                12邦维 用汉印IT4S打印机打印
                    message = message.replace("%BOX", boxTag.getBoxNo());
                    message = message.replace("%U", boxTag.getUserCode());
                    message = message.replace("%B", boxTag.getBrandName());
                    message = message.replace("%S", boxTag.getSerialName());
                    message = message.replace("%M", boxTag.getModel());
                    message = message.replace("%C", boxTag.getColor());
                    message = message.replace("%N", boxTag.getNum());
//                message = message.replace("%D", boxTag.getPackDate());
                } else if (sysUserInfo.getEnterpriseId().toString().equals("76") || sysUserInfo.getEnterpriseId().toString().equals("74") || sysUserInfo.getEnterpriseId().toString().equals("00")||sysUserInfo.getEnterpriseId().toString().equals("72")) {
//                逸夫和阿塔那都需要加仓库，品牌代号是76和74
                    message = message.replace("%BOX", boxTag.getBoxNo());
                    message = message.replace("%U", boxTag.getUserCode());
                    message = message.replace("%B", boxTag.getBrandName());
                    message = message.replace("%S", boxTag.getSerialName());
                    message = message.replace("%M", boxTag.getModel());
                    message = message.replace("%C", boxTag.getColor());
                    message = message.replace("%N", boxTag.getNum());
                    message = message.replace("%D", boxTag.getStockName());
                } else {
                    //51
                    message = message.replace("%BOX", boxTag.getBoxNo());
                    message = message.replace("%B", boxTag.getBrandName());
                    message = message.replace("%M", boxTag.getModel());
                    message = message.replace("%C", boxTag.getColor());
                    message = message.replace("%N", boxTag.getNum());
                    message = message.replace("%D", boxTag.getPackDate());
                }
            } else {
                message = message.replace("%BOX", boxTag.getBoxNo());
                message = message.replace("%U", boxTag.getUserCode());
                message = message.replace("%B", boxTag.getBrandName());
                message = message.replace("%S", boxTag.getSerialName());
                message = message.replace("%M", boxTag.getModel());
                message = message.replace("%C", boxTag.getColor());
                message = message.replace("%N", boxTag.getNum());
                message = message.replace("%D", boxTag.getStockName());
            }
        }

        //	        	byte[] send = readFileByte();
        byte[] send;
        try {
            send = message.getBytes("GBK");
            bluetoothManager.write(send);
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

