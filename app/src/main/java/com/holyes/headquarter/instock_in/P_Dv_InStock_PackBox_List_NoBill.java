package com.holyes.headquarter.instock_in;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.ccssend5.R;
import com.holyes.ccssend5.dao.PackingScanDao;
import com.holyes.ccssend5.entity.Para;
import com.holyes.ccssend5.lib.AccessWeb;
import com.holyes.ccssend5.lib.MySound;
import com.holyes.ccssend5.lib.ShowMessage;
import com.holyes.ccssend5.lib.SysUserInfo;
import com.holyes.ccssend5.lib.bluetooth.BluetoothManager;
import com.holyes.ccssend5.lib.bluetooth.BluetoothService;
import com.holyes.ccssend5.lib.bluetooth.BluetoothUtil;
import com.holyes.ccssend5.lib.bluetooth.BoxTag;
import com.holyes.ccssend5.lib.bluetooth.DeviceListActivity;
import com.holyes.ccssend5.myview.MyProgressDialog;
import com.holyes.ccssend5.select.SelectPackingScanDetail1;
import com.holyes.ccssend5.select.SelectProductModelColor;
import com.holyes.ccssend5.utils.PrintUtil;
import com.holyes.ccssend5.utils.SomeUtils;
import com.holyes.headquarter.other.P_Dv_InStock_PackBox_Search;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: P_Dv_InStock_PackBox_List_NoBill
 * @Description: 装盒入库
 * @Author: lijin
 * @Date: 2021/3/10 13:59
 */
public class P_Dv_InStock_PackBox_List_NoBill extends Activity implements View.OnClickListener {


    private Context mContext;
    private SysUserInfo sysUserInfo;
    private AccessWeb accessWeb;
    private Para scanPara;
    private MySound sound;
    private SimpleDateFormat simpleDateFormat;
    private BoxTag boxTag, testBoxTag;
    private PrintUtil printUtil;


    private TextView tv_instock_bill, tv_supplier, tv_stock, tv_model_color,
            tv_productid, tv_packing_number, tv_one_count, tv_all_count, tv_connect_state;

    private Button btn_select_model_color, btn_set_packing_number, btn_connect, btn_scan_detail,
            btn_scan_print, btn_again_print_boxcode, btn_eliminate, btn_finish, btn_test_packing;
    private EditText et_barcode;


    private List<String> lastSuccessList, tempCodeList;//上一盒成功的条码，本次扫描装盒的条码
    private String[] packBoxResultArray;//装盒成功后返回的结果截成的数组

    private String supplier_id, supplier_name, stock_id, stock_name,//供应商代号，供应商名称，仓库代号，仓库名称
            product_id = "", modelm, colors, brand = "", series = "",//产品代号，型号，色号，品牌，系列
            tLoginID, tWebId = "", scantime = "",//loginid,识别号,扫描时间
            pSoCompid = "", pUserId = "", pSourceBillNo = "", pScanBillNo = "",//供应商代号，用户登录代号，来源单号，扫描单号
            tBillNo = "", newTempBoxNo = "", boxCode = "", packBoxResult = "",//入库单号，临时盒标码，盒标，装盒结果
            createPackingJobResult = "", connectedDeviceName, connectedDeviceAddress;//创建任务结果，连接的蓝牙设备名

    private String tBarCodes = "";//集合转成字符串
    private String packBoxErrorResult = "";//装盒失败后的错误

    private String oneInStockCount = "0", allInStockCount = "0";//，此型号累计数量，合计数量
    private String oneScanStockCount = "0", allScanCount = "0";//，此型号累计扫描装盒数量，合计扫描装盒数量

    private int number = 0, packedNumber = 0;//每盒数量，盒里已经装好数量


    private final int Lic_SelectModel = 2;//选择产品
    private final int Lic_Eliminate = 3;//剔除
    private final int HandCreatePackingJobSuccess = 6;//创建装盒任务成功
    private final int HandPackBoxSuccess = 9;//装盒成功，返回了盒标码
    private final int HandToaskErrorMsg = 10;//弹出toask，报错声音
    private final int HandShowWaitingDialog = 11;//弹出请稍候的dialog
    private final int HandCloseWaitingDialog = 12;//弹出请稍候的dialog

    private boolean isCreatePackingJobSuccess = false;//是否创建任务成功
    private boolean isPackedBoxFail = false;//是否装盒失败，false表示成功了，可以进行下一次，true表示失败了，要去处理后才可以
    private boolean isPackedBoxReturnResult = true;//是否创建盒标成功，如果装完一盒后没有返回盒标不能进行下一盒的扫描
    private boolean isConnectedBluetooth = false;

    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothManager bluetoothManager = null;

    private boolean isTest = false;//是否测试，测试的话不需要连接蓝牙打印机。输出log.i盒标.编译的时候要false


    int i;
    boolean flag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.packing_instock);
        mContext = this;
        sysUserInfo = new SysUserInfo(mContext);
        accessWeb = new AccessWeb(mContext);
        scanPara = new Para();
        sound = MySound.getMySound(this);
        printUtil = new PrintUtil();

        tv_instock_bill = (TextView) findViewById(R.id.tv_instock_bill);
        tv_supplier = (TextView) findViewById(R.id.tv_supplier);
        tv_stock = (TextView) findViewById(R.id.tv_stock);
        tv_model_color = (TextView) findViewById(R.id.tv_model_color);
        tv_productid = (TextView) findViewById(R.id.tv_productid);
        tv_packing_number = (TextView) findViewById(R.id.tv_packing_number);
        tv_one_count = (TextView) findViewById(R.id.tv_one_count);
        tv_all_count = (TextView) findViewById(R.id.tv_all_count);
        tv_connect_state = (TextView) findViewById(R.id.tv_connect_state);

        number = sysUserInfo.getPackingNumber();
        tv_packing_number.setText((packedNumber) + "/" + number);

        btn_select_model_color = (Button) findViewById(R.id.btn_select_model_color);
        btn_set_packing_number = (Button) findViewById(R.id.btn_set_packing_number);
        btn_connect = (Button) findViewById(R.id.btn_connect);
        btn_scan_detail = (Button) findViewById(R.id.btn_scan_detail);
        btn_scan_print = (Button) findViewById(R.id.btn_scan_print);
        btn_again_print_boxcode = (Button) findViewById(R.id.btn_again_print_boxcode);
        btn_eliminate = (Button) findViewById(R.id.btn_eliminate);
        btn_finish = (Button) findViewById(R.id.btn_finish);
        btn_test_packing = (Button) findViewById(R.id.btn_test_packing);

        et_barcode = (EditText) findViewById(R.id.et_barcode);
        et_barcode.setOnKeyListener(new EtBarcodeOnKeyListener());

        btn_select_model_color.setOnClickListener(this);
        btn_set_packing_number.setOnClickListener(this);
        btn_connect.setOnClickListener(this);
        btn_scan_detail.setOnClickListener(this);
        btn_scan_print.setOnClickListener(this);
        btn_again_print_boxcode.setOnClickListener(this);
        btn_eliminate.setOnClickListener(this);
        btn_finish.setOnClickListener(this);
        btn_test_packing.setOnClickListener(this);

        simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

        Intent gIntent = getIntent();
        supplier_id = gIntent.getStringExtra("supplier_id");
        supplier_name = gIntent.getStringExtra("supplier_name");
        stock_id = gIntent.getStringExtra("stock_id");
        stock_name = gIntent.getStringExtra("stock_name");

//        Log.d("main--item",stock_id+"仓库"+stock_name);


        tLoginID = sysUserInfo.getLoginid();
        pSoCompid = supplier_id;
        pUserId = sysUserInfo.getUserid();
        pScanBillNo = sysUserInfo.getUserid() + "ZR" + SomeUtils.RandomScanOrder();// 系统

        //清空扫描数据
        PackingScanDao.deletePackingByCode(mContext, null);
        lastSuccessList = new ArrayList<String>();
        tempCodeList = new ArrayList<String>();

        tv_supplier.setText(supplier_name);
        tv_stock.setText(stock_name);
        //创建装盒任务
        P_Dv_InStock_CreatePackingJob();

        // 使用全局蓝牙管理器
        bluetoothManager = BluetoothManager.getInstance();
        mBluetoothAdapter = bluetoothManager.getBluetoothAdapter();

        if (bluetoothManager.isBluetoothAvailable()) {
            // 如果已经连接，则不需要重新初始化
            if (!bluetoothManager.isBluetoothConnected()) {
                // 自动连接上次保存的蓝牙设备
                boolean isAutoConnecting = bluetoothManager.initBluetoothServiceAndAutoConnect(this, mHandler);
                if (isAutoConnecting) {
                    // 正在自动连接，更新UI状态
                    connectedDeviceName = sysUserInfo.getConnectedBluetoothName();
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
                //选择型号色号
                case Lic_SelectModel:
                    product_id = data.getStringExtra("goodsid");
                    modelm = data.getStringExtra("modelm");
                    colors = data.getStringExtra("colors");
                    //				brand = PackingScanDao.getProductBrandOrSeries(mContext, product_id, null);
                    //				series = PackingScanDao.getProductBrandOrSeries(mContext, product_id, product_id);
                    tv_productid.setText("(" + product_id + ")");
                    tv_model_color.setText(modelm + "-" + colors);


                    break;
                case Lic_Eliminate:
                    //剔除页面返回
                    oneInStockCount = String.valueOf(PackingScanDao.getScanCount(mContext, product_id, false));
                    oneScanStockCount = String.valueOf(PackingScanDao.getScanCount(mContext, product_id, true));
                    allInStockCount = String.valueOf(PackingScanDao.getScanCount(mContext, null, false));
                    allScanCount = String.valueOf(PackingScanDao.getScanCount(mContext, null, true));

                    tv_one_count.setText(oneInStockCount + "/" + oneScanStockCount);
                    tv_all_count.setText(allInStockCount + "/" + allScanCount);

                    ArrayList<String> eliminateBarcodeList = data.getStringArrayListExtra("eliminateBarcodeList");
                    packedNumber = packedNumber - eliminateBarcodeList.size();
                    tv_packing_number.setText(packedNumber + "/" + number);
                    tempCodeList.removeAll(eliminateBarcodeList);
                    isPackedBoxFail = false;

                    break;

                default:
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        //返回键监听，下面的完成按钮点击后也执行这个方法
        if (SomeUtils.isDoubleClick(mContext, true)) {
            finish();
        }
    }


    /**
     * 执行装盒入库
     *
     * @param tLoginID
     * @param tWebId
     * @param tTempBoxNo
     * @param tProduct_id
     * @param tNum
     */
    public void doInStock_PackBox_NoBill(final String tLoginID, final String tWebId,
                                         final String tTempBoxNo, final String tProduct_id, final int tNum) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    tBarCodes = SomeUtils.getListBarcode(tempCodeList);
                    isPackedBoxReturnResult = false;
                    //					ShowMessage.ShowMsg(mHandler, HandShowWaitingDialog,"装盒入库中，请稍候...");
                    packBoxResult = accessWeb.P_Dv_InStock_PackBox_List_NoBill(tLoginID, tWebId, tBarCodes, tProduct_id, tNum);
                    ShowMessage.ShowMsg(mHandler, HandCloseWaitingDialog, "");
                    //	true;tWebId,数量,入库单号,盒标号，品牌，系列,型号,色号
                    packBoxResultArray = packBoxResult.split(",",-1);
                    if (!tWebId.equals(packBoxResultArray[0])) {
                        ShowMessage.ShowMsg(mHandler, HandToaskErrorMsg, "装盒入库传输数据出错：" + tWebId + "-" + packBoxResultArray[0]);
                        return;
                    }
                    //					oneInStockCount =packBoxResultArray[1];//此型号累计数量
                    tBillNo = packBoxResultArray[2];
                    boxCode = packBoxResultArray[3];
                    brand = packBoxResultArray[4];
                    series = packBoxResultArray[5];
                    if (isTest) {
                        //						Log.i("main", "-----盒标："+boxCode);
                    }
                    boxTag = new BoxTag(boxCode, brand, series, modelm, colors, String.valueOf(tNum),
                            pUserId, simpleDateFormat.format(new Date()).substring(0, 10),stock_name);//  //如果要截取去掉时间就补上
                    ShowMessage.ShowMsg(mHandler, HandPackBoxSuccess, boxTag);
                    isPackedBoxReturnResult = true;
                    isPackedBoxFail = false;
                    packBoxErrorResult = "";
                } catch (Exception e) {
                    isPackedBoxReturnResult = true;
                    isPackedBoxFail = true;
                    ShowMessage.ShowMsg(mHandler, HandCloseWaitingDialog, "");
                    packBoxErrorResult = "【装盒入库】失败：" + e.getMessage();
                    ShowMessage.ShowMsg(mHandler, HandToaskErrorMsg, packBoxErrorResult);
                }
            }
        }).start();
    }


    /**
     * 条码状态ok就添加数据到扫描集合
     *
     * @param barcode
     */
    public void addScanPackingBarcode(String barcode) {
        tempCodeList.add(barcode);
        scantime = simpleDateFormat.format(new Date());
        //条码存入本地
        PackingScanDao.insertPackingInstock(mContext, product_id, modelm,
                colors, barcode, newTempBoxNo, scantime);
        packedNumber++;
        //		if (tempCodeList.size()==number) {
        //			tv_packing_number.setText((packedNumber-1)+"/"+number);
        //		} else{
        tv_packing_number.setText(packedNumber + "/" + number);
        //		}
        //此型号累计数量，合计数量
        oneInStockCount = String.valueOf(PackingScanDao.getScanCount(mContext, product_id, false));
        oneScanStockCount = String.valueOf(PackingScanDao.getScanCount(mContext, product_id, true));
        allInStockCount = String.valueOf(PackingScanDao.getScanCount(mContext, null, false));
        allScanCount = String.valueOf(PackingScanDao.getScanCount(mContext, null, true));
        tv_one_count.setText(oneInStockCount + "/" + oneScanStockCount);
        tv_all_count.setText(allInStockCount + "/" + allScanCount);
        if (tempCodeList.size() == number) {
            Toast.makeText(mContext, "正在打印盒标，请稍后！！！", Toast.LENGTH_SHORT);
            doInStock_PackBox_NoBill(tLoginID, tWebId, newTempBoxNo, product_id, number);
        }
    }


    /**
     * 创建装盒入库任务
     */
    public void P_Dv_InStock_CreatePackingJob() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    tWebId = pScanBillNo;
                    scanPara.setSoCompId(pSoCompid);
                    scanPara.setOaSuserId(pUserId);
                    scanPara.setSourceBillNo(pSourceBillNo);
                    scanPara.setScanBillNo(pScanBillNo);
                    scanPara.setStockId(stock_id);
                    scanPara.setDeCompId("00");

                    createPackingJobResult = accessWeb.P_Dv_InStock_CreatePackingJob(tLoginID, tWebId, scanPara.toJson());
                    if (tWebId.equals(createPackingJobResult)) {
                        isCreatePackingJobSuccess = true;
                        ShowMessage.ShowMsg(mHandler, HandCreatePackingJobSuccess, "");
                    } else {
                        ShowMessage.ShowMsg(mHandler, HandToaskErrorMsg, "【创建装盒任务】错误：" + tWebId + "不同于" + createPackingJobResult);
                        isCreatePackingJobSuccess = false;
                    }
                } catch (Exception e) {
                    isCreatePackingJobSuccess = false;
                    ShowMessage.ShowMsg(mHandler, HandToaskErrorMsg, "【创建装盒任务】失败：" + e.getMessage());
                }
            }
        }).start();
    }


    private Handler mHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case HandPackBoxSuccess:
                    //				loading.Close();
                    //装盒成功就打印
                    printBoxCode(boxTag);
                    MyProgressDialog.close();
                    //改变数据库中的盒标一栏
                    PackingScanDao.updatePackingByCode(mContext, boxCode, newTempBoxNo);
                    tv_instock_bill.setText(tBillNo);//设置入库单号
                    lastSuccessList.clear();
                    tv_packing_number.setText(packedNumber + "/" + number);
                    lastSuccessList.addAll(tempCodeList);
                    tempCodeList.clear();
                    packedNumber = 0;
                    oneInStockCount = String.valueOf(PackingScanDao.getScanCount(mContext, product_id, false));
                    allInStockCount = String.valueOf(PackingScanDao.getScanCount(mContext, null, false));
                    tv_one_count.setText(oneInStockCount + "/" + oneScanStockCount);
                    tv_all_count.setText(allInStockCount + "/" + allScanCount);

                    break;

                case HandToaskErrorMsg:
                    MySound.errorSound();
                    ShowMessage.Show(mContext, msg.obj.toString());
                    MyProgressDialog.close();
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
                            // 从BluetoothManager获取设备信息
                            String currentDeviceName = bluetoothManager.getConnectedDeviceName();
                            String currentDeviceAddress = bluetoothManager.getConnectedDeviceAddress();

                            if (currentDeviceName != null && !currentDeviceName.isEmpty()) {
                                connectedDeviceName = currentDeviceName;
                                connectedDeviceAddress = currentDeviceAddress;
                            } else {
                                // 如果BluetoothManager中的设备名称为空，使用已设置的设备信息
                                if (connectedDeviceName == null || connectedDeviceName.isEmpty()) {
                                    connectedDeviceName = sysUserInfo.getConnectedBluetoothName();
                                    connectedDeviceAddress = sysUserInfo.getConnectedBluetoothAddress();
                                }
                                if (connectedDeviceName == null || connectedDeviceName.isEmpty()) {
                                    connectedDeviceName = "未知设备";
                                }
                            }

                            // 强制更新界面状态
                            btn_connect.setText("断开");
                            tv_connect_state.setText("已连接:" + connectedDeviceName);
                            tv_connect_state.setTextColor(Color.parseColor("#008000"));
                            isConnectedBluetooth = true;
                            sysUserInfo.setConnectedBluetoothName(connectedDeviceName);
                            sysUserInfo.setConnectedBluetoothAddress(connectedDeviceAddress);
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            // 只有在没有设备名称的情况下才显示"正在连接"
                            if (connectedDeviceName == null || connectedDeviceName.isEmpty()) {
                                btn_connect.setText("连接");
                                tv_connect_state.setText("正在连接...");
                                tv_connect_state.setTextColor(Color.BLACK);
                            } else {
                                btn_connect.setText("连接");
                                tv_connect_state.setText("正在连接:" + connectedDeviceName + "...");
                                tv_connect_state.setTextColor(Color.BLACK);
                            }
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
                case HandShowWaitingDialog:
                    MyProgressDialog.show(mContext, msg.obj.toString(), true, false);
                    break;
                case HandCloseWaitingDialog:
                    MyProgressDialog.close();
                    break;

                default:
                    break;
            }
            return false;
        }
    });


    /**
     * 弹出设置数量的dialog
     */
    public void ShowSetNumberDialog() {
        TextView tv_title = new TextView(mContext);
        tv_title.setPadding(10, 10, 10, 10);
        tv_title.setText("盒装数量设置");
        tv_title.setGravity(Gravity.CENTER);
        tv_title.setTextSize(25);
        tv_title.setTextColor(Color.parseColor("#30C0FF"));

        LayoutInflater mInflater = LayoutInflater.from(mContext);
        View view = mInflater.inflate(R.layout.set_packing_num_layout, null);
        final EditText editText = (EditText) view.findViewById(R.id.et_number);
        SomeUtils.moveFocus(editText);
        editText.setSingleLine(true);
        editText.setGravity(Gravity.CENTER);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        editText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, AlertDialog.THEME_HOLO_LIGHT);
        builder.setCustomTitle(tv_title)
                .setView(view)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String numberStr = editText.getText().toString().trim();
                        if (numberStr.isEmpty()) {
                            ShowMessage.Show(mContext, "输入不能为空");
                            return;
                        }
                        //扫码数量
                        int SacnNum=tempCodeList.size();
                        //设置数量
                        int Setnum=Integer.parseInt(numberStr);
                        if (SacnNum>0){
                            //判断修改的数量，如果小于已扫码的数量，是修改不成功
                            if (SacnNum>Setnum){
                                Toast.makeText(mContext,"当前已扫码数量大于设置数量，修改失败",Toast.LENGTH_SHORT).show();
                                MySound.errorSound();
                            }else if (SacnNum==Setnum){
                                //判断修改的数量，如果等于已扫码的数量，就要有询问提示，马上打标出来，上传数据，清除本地数量
                                Toast.makeText(mContext, "正在打印盒标，请稍后！！！", Toast.LENGTH_SHORT);
                                number = Integer.parseInt(numberStr);
                                doInStock_PackBox_NoBill(tLoginID, tWebId, newTempBoxNo, product_id, number);
                                tv_packing_number.setText(packedNumber + "/" + number);
                                sysUserInfo.setPackingNumber(numberStr);
                            }else if (SacnNum<Setnum){
                                //判断修改的数量，如果大于已扫码的数量，累计已扫码数量，就要以当前已扫码的数量为准
                                number = Integer.parseInt(numberStr);
                                tv_packing_number.setText(packedNumber + "/" + number);
                                sysUserInfo.setPackingNumber(numberStr);
                            }
                        }else{
                            number = Integer.parseInt(numberStr);
                            tv_packing_number.setText(packedNumber + "/" + number);
                            sysUserInfo.setPackingNumber(numberStr);
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .setCancelable(false)
                .create().show();
    }

    //错误弹出框
    private void showNormalDialog() {
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(mContext);
        normalDialog.setTitle("异常提示");
        normalDialog.setMessage("数量超出，请查看数量是否正确，以免窜盒!!!");
        normalDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                    }
                });
        //		normalDialog.setNegativeButton("关闭",
        //				new DialogInterface.OnClickListener() {
        //			@Override
        //			public void onClick(DialogInterface dialog, int which) {
        //				//...To-do
        //			}
        //		});
        // 显示
        normalDialog.show();
    }

    private class EtBarcodeOnKeyListener implements View.OnKeyListener {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {

            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
//                    String barcodeStr = et_barcode.getText().toString().trim();

                    String barcodeStr = "";

                    if (et_barcode.getText().toString().trim().indexOf("=") != -1||et_barcode.getText().toString().trim().indexOf("http") != -1) {
                        //包含
                        barcodeStr = SomeUtils.InterceptCode(mContext, et_barcode.getText().toString().trim());
                    } else {
                        //不包含
                        barcodeStr = SomeUtils.UpdatefirstString(mContext,et_barcode.getText().toString().trim());
                    }

                    et_barcode.setText("");

                    if (product_id.isEmpty()) {
                        ShowMessage.ShowMsg(mHandler, HandToaskErrorMsg, "请先选择产品");
                        return true;
                    }
                    if (number == 0) {
                        ShowMessage.ShowMsg(mHandler, HandToaskErrorMsg, "请先设置盒装数量");
                        return true;
                    }
                    if (!isConnectedBluetooth && !isTest) {
                        ShowMessage.ShowMsg(mHandler, HandToaskErrorMsg, "请先连接蓝牙打印机");
                        return true;
                    }

                    if (!isCreatePackingJobSuccess) {
                        ShowMessage.ShowMsg(mHandler, HandToaskErrorMsg, "装盒任务创建未成功，请稍候重试");
                        return true;
                    }
                    if (PackingScanDao.barcodeExistLocal(mContext, barcodeStr, true)) {
                        ShowMessage.ShowMsg(mHandler, HandToaskErrorMsg, barcodeStr + "已经装盒，请不要重复扫描");
                        return true;
                    }
                    if (!isPackedBoxReturnResult) {
                        showNormalDialog();
                        //MyProgressDialog.show(mContext, "数量超出，请查看数量是否正确，以免窜盒!!!", false, true);
                        //Toast.makeText(mContext, "数量超出，请查看数量是否正确，以免窜盒!!!", Toast.LENGTH_SHORT);
                        MySound.errorSoundBeyond();
                        //ShowMessage.Show(mContext, "请等装盒完成后再继续扫描，谢谢！");
                        return true;
                    }
                    if (isPackedBoxFail) {
                        ShowMessage.ShowMsg(mHandler, HandToaskErrorMsg, packBoxErrorResult + "请先去【剔除】中剔除错误的产品");
                        return true;
                    }
                    if (barcodeStr.startsWith("A")) {
                        MySound.errorSound();
                        ShowMessage.Show(mContext, "请扫描正确的物流码【" + barcodeStr + "】");
                        return true;
                    }
                    if (!SomeUtils.isAllNumber(mContext, barcodeStr)) {
                        MySound.errorSound();
                        ShowMessage.Show(mContext, "请扫描正确的物流码【" + barcodeStr + "】");
                        return true;
                    }
                    if (!SomeUtils.TextJudgmentSize(barcodeStr)) {
                        MySound.errorSound();
                        ShowMessage.Show(mContext, "请先扫描正确的条码");
                        return false;
                    } else {
                        addScanPackingBarcode(barcodeStr);
                    }
                }
                return true;
            } else {
                return false;
            }
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_select_model_color:
                if (tempCodeList.size()>0){
                    //    通过AlertDialog.Builder这个类来实例化我们的一个AlertDialog的对象
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    //    设置Title的图标
//                    builder.setIcon(R.drawable.ic_launcher);
                    //    设置Title的内容
                    builder.setTitle("确定切换型号吗？");
                    //    设置Content来显示一个信息
                    builder.setMessage("当前型号装盒还未装满，是否切换型号，这将删除未装盒的条码数据");
                    //    设置一个PositiveButton
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {

                            Boolean IsDelGoodsId=PackingScanDao.deletePackingByGoodsId(mContext,product_id,newTempBoxNo);
                            if (IsDelGoodsId){
                                packedNumber = 0;
                                tempCodeList.clear();
                                tv_packing_number.setText((packedNumber) + "/" + number);

                                allScanCount = String.valueOf(PackingScanDao.getScanCount(mContext, null, true));
                                oneScanStockCount = String.valueOf(PackingScanDao.getScanCount(mContext, product_id, true));
                                oneInStockCount = String.valueOf(PackingScanDao.getScanCount(mContext, product_id, false));
                                allInStockCount = String.valueOf(PackingScanDao.getScanCount(mContext, null, false));
                                tv_one_count.setText(oneInStockCount + "/" + oneScanStockCount);
                                tv_all_count.setText(allInStockCount + "/" + allScanCount);

                                Intent intent = new Intent(mContext, SelectProductModelColor.class);
                                startActivityForResult(intent, Lic_SelectModel);
                            }else{
                                Toast.makeText(mContext, "数据清除失败", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                    //    设置一个NegativeButton
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
//                            Toast.makeText("", "negative: " + which, Toast.LENGTH_SHORT).show();
                        }
                    });
                    //    显示出该对话框
                    builder.show();

                }else{
                    Intent intent = new Intent(mContext, SelectProductModelColor.class);
                    startActivityForResult(intent, Lic_SelectModel);
                }

                break;
            case R.id.btn_set_packing_number:
                ShowSetNumberDialog();
                break;
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
            case R.id.btn_scan_detail:
                Intent dIntent = new Intent(mContext, SelectPackingScanDetail1.class);
                startActivity(dIntent);
                break;
            case R.id.btn_scan_print:
                doPrintModelColorDetail();
                break;

            case R.id.btn_again_print_boxcode:
                //			 BoxTag boxTag = new BoxTag("B6221565195189164",  "HOLYES", "光学镜", "HL0001", "C01",
                //		     		   "12","0012", "2018-01-09 18:30：23");
                //打印上一次成功的盒标
//                printBoxCode(boxTag);
                Intent intent=new Intent(mContext, P_Dv_InStock_PackBox_Search.class);
                intent.putExtra("PageType","warehous");
                startActivity(intent);
                break;
            case R.id.btn_eliminate:
                Intent eIntent = new Intent(mContext, EliminateActivity.class);
                eIntent.putExtra("product_id", product_id);
                startActivityForResult(eIntent, Lic_Eliminate);
                break;
            case R.id.btn_finish:
                SomeUtils.clickKeyBack();
                break;
            case R.id.btn_test_packing:
                //			new Thread(new MyThread()).start();
                for (int i = 0; i < 1; i++) {
//                    int j = (int) (Math.random() * 900) + 100;
                    testBoxTag = new BoxTag("A201807300000" + i, "测试", "合力思测试", "测试", "test", "1", "test", "2024-12-03","仓库名称1");
                    printBoxCode(testBoxTag);
                }
                break;


            default:
                break;
        }

    }

    //	public class MyThread implements Runnable {
    //		@Override
    //		public void run() {
    //			// TODO Auto-generated method stub
    //			while (flag) {
    //				try {
    //					for (i=1; i <= getCode().size(); i++) {
    //						Thread.sleep(3000);// 线程暂停3秒，单位毫秒
    //						Message message = new Message();
    //						message.what = 1;
    //						handler.sendMessage(message);
    //					}
    //					break;
    //				} catch (InterruptedException e) {
    //					e.printStackTrace();
    //				}
    //			}
    //		}
    //	}


    //	private List<BoxTag> getCode(){
    //		List<BoxTag> boxTagsList=new ArrayList<BoxTag>();
    //		BoxTag b1=new BoxTag("6210158948157423","测试","测试", "测试", "test", "test", "1",  "2018-7-19 16:57:23");
    //		BoxTag b2=new BoxTag( "6215397245742271","测试","测试", "测试", "test", "test", "2", "2018-7-19 16:57:23");
    //		boxTagsList.add(b1);
    //		boxTagsList.add(b2);
    //		return boxTagsList;
    //
    //	}

    //	Handler handler = new Handler() {
    //		public void handleMessage(Message msg) {
    //			for (int i = 0; i < getCode().size(); i++) {
    //				testBoxTag=getCode().get(i);
    //				printBoxCode(testBoxTag);
    //			}
    //			super.handleMessage(msg);
    //		}
    //	};

    public void doPrintModelColorDetail() {
        List<Map<String, Object>> pList = PackingScanDao.getPackModelColorDetail(mContext, "");
//        Log.d("main","chauxn"+pList.toString());
        if (pList.size() == 0) {
            ShowMessage.Show(mContext, "没有可打印的数据");
            return;
        }

        String[] mark = new String[3];
        mark[0] = "入库单：" + tBillNo;
        mark[1] = "供应商：" + supplier_name;
        mark[2] = "仓    库：" + stock_name;

        printUtil.print(P_Dv_InStock_PackBox_List_NoBill.this, "           装盒入库", mark, pList, sysUserInfo.getUserid());
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
            message = SomeUtils.readAssetsTxt(mContext, "lab_Chuanj");
//            message = SomeUtils.readAssetsTxt(mContext, "lab_jb");
        }
//        Log.d("main",message);
        sendMessage(message, boxTag);
    }


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
        if (!bluetoothManager.isBluetoothConnected()) {
            ShowMessage.Show(mContext, "未连接蓝牙");
            return;
        }
        if (isFileExists("lab_" + sysUserInfo.getEnterpriseId().toString() + ".txt")) {
            //52万新  53帕兰德
            if (sysUserInfo.getEnterpriseId().toString().equals("52")||sysUserInfo.getEnterpriseId().toString().equals("53")){
                message = message.replace("%BOX", boxTag.getBoxNo());
                message = message.replace("%U", boxTag.getUserCode());
                message = message.replace("%B", boxTag.getBrandName());
                message = message.replace("%S", boxTag.getSerialName());
                message = message.replace("%M", boxTag.getModel());
                message = message.replace("%C", boxTag.getColor());
                message = message.replace("%N", boxTag.getNum());
//                message = message.replace("%D", boxTag.getPackDate());
            }else if (sysUserInfo.getEnterpriseId().toString().equals("12")){
//                12邦维 用汉印IT4S打印机打印
                message = message.replace("%BOX", boxTag.getBoxNo());
                message = message.replace("%U", boxTag.getUserCode());
                message = message.replace("%B", boxTag.getBrandName());
                message = message.replace("%S", boxTag.getSerialName());
                message = message.replace("%M", boxTag.getModel());
                message = message.replace("%C", boxTag.getColor());
                message = message.replace("%N", boxTag.getNum());
//                message = message.replace("%D", boxTag.getPackDate());
            }else if (sysUserInfo.getEnterpriseId().toString().equals("76")||sysUserInfo.getEnterpriseId().toString().equals("74")||sysUserInfo.getEnterpriseId().toString().equals("00")||sysUserInfo.getEnterpriseId().toString().equals("72")){
//                逸夫和阿塔那都需要加仓库，品牌代号是76和74
                message = message.replace("%BOX", boxTag.getBoxNo());
                message = message.replace("%U", boxTag.getUserCode());
                message = message.replace("%B", boxTag.getBrandName());
                message = message.replace("%S", boxTag.getSerialName());
                message = message.replace("%M", boxTag.getModel());
                message = message.replace("%C", boxTag.getColor());
                message = message.replace("%N", boxTag.getNum());
                message = message.replace("%D", boxTag.getStockName());
            }else{
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
     * 获取点击事件,是否隐藏键盘
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        SomeUtils.isNeedHideAndDo(this, ev);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        if (res.getConfiguration().fontScale != 1) {//非默认值
            Configuration newConfig = new Configuration();
            newConfig.setToDefaults();//设置默认
            res.updateConfiguration(newConfig, res.getDisplayMetrics());
        }
        return res;
    }
}

