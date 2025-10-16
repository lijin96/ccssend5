package com.holyes.factoryscan.activity;

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
import androidx.annotation.Nullable;

import com.example.ccssend5.R;
import com.google.gson.JsonObject;
import com.holyes.ccssend5.dao.PackingScanDao;
import com.holyes.ccssend5.lib.AccessWeb;
import com.holyes.ccssend5.lib.MySound;
import com.holyes.ccssend5.lib.ShowMessage;
import com.holyes.ccssend5.lib.SysInfoFile;
import com.holyes.ccssend5.lib.SysUserInfo;
import com.holyes.ccssend5.lib.bluetooth.BluetoothManager;
import com.holyes.ccssend5.lib.bluetooth.BluetoothService;
import com.holyes.ccssend5.lib.bluetooth.BluetoothUtil;
import com.holyes.ccssend5.lib.bluetooth.BoxTag;
import com.holyes.ccssend5.lib.bluetooth.DeviceListActivity;
import com.holyes.ccssend5.myview.MyProgressDialog;
import com.holyes.ccssend5.select.SelectRetailStore;
import com.holyes.ccssend5.utils.SomeUtils;
import com.holyes.headquarter.instock_in.EliminateActivity;
import com.holyes.headquarter.instock_in.P_Dv_InStock_PackBox_List_NoBill;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: Holyes_Dv_Factory_PackBox_NoBill
 * @Description: 工厂装盒入库
 * @Author: lijin
 * @Date: 2025/7/31 14:36
 */
public class Holyes_Dv_Factory_PackBox_NoBill extends Activity {

    private Context mContext;
    private SysUserInfo sysUserInfo;
    private AccessWeb accessWeb;

    private String GoodsModelm="",GoodsColor="",GoodsId="";//产品型号 产品色号 产品id
    private String BoxNoCode="";//盒标码
    private String OneCount="0",AllCount="0",AllBox="0";//当前型号已装数 当前已装产品总数 当前已装盒数

    private String connectedDeviceName, connectedDeviceAddress;//连接的蓝牙设备名和蓝牙地址

    private String scanBillno = "";//扫描单号
    private int nSize = 0;//扫描序号

    private String lStar = "";

    private EditText et_barcode;//条码输入框

    private TextView tv_model_color,tv_productid;//型号色号 产品id
    private TextView tv_BoxNo_Code;//盒标码展示
    private TextView tv_packing_number;//显示设置的盒装数

    private TextView tv_one_count,tv_all_count,tv_all_box;//当前型号已装数 当前已装产品总数 当前已装盒数
    private TextView tv_box_count;//当前盒已装数量

    private Button btn_select_model_color,btn_set_packing_number,btn_eliminate,btn_history_eliminate;//选择型号色号 设置盒装数 剔除当前盒 剔除历史盒

    private Button btn_connect;//连接蓝牙
    private TextView tv_connect_state;//蓝牙连接状态

    private String BoxNoNum="12";//盒装数 默认12个，必须有盒标码才可以修改数量
    private String BoxActNum="0";//已装数量


    private final int Lic_SelectModel = 2;//选择产品
    private final int Lic_Eliminate = 3;//剔除

    private final int HandPackBoxSuccess = 9;//扫描成功，返回盒标码等参数
    private final int HandToaskErrorMsg = 10;//弹出toask，报错声音
    private final int HandShowWaitingDialog = 11;//弹出请稍候的dialog
    private final int HandCloseWaitingDialog = 12;//弹出请稍候的dialog

    private boolean isConnectedBluetooth = false;

    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothManager bluetoothManager = null;

    private boolean isTest = true;//是否测试，测试的话不需要连接蓝牙打印机。输出log.i盒标.编译的时候要false

    private BoxTag boxTag;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dv_factory_packbox_nobill);
        mContext=this;
        sysUserInfo=new SysUserInfo(mContext);
        accessWeb=new AccessWeb(mContext);

        scanBillno = sysUserInfo.getUserid() + "S" + SomeUtils.RandomScanOrder();// 系统

        initView();

    }
    
    @Override
    protected void onResume() {
        super.onResume();
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

    private  void initView(){

        tv_model_color=findViewById(R.id.tv_model_color);
        tv_productid=findViewById(R.id.tv_productid);

        tv_BoxNo_Code=findViewById(R.id.tv_BoxNo_Code);

        tv_packing_number=findViewById(R.id.tv_packing_number);
        tv_packing_number.setText(BoxNoNum);

        tv_one_count=findViewById(R.id.tv_one_count);
        tv_all_count=findViewById(R.id.tv_all_count);
        tv_all_box=findViewById(R.id.tv_all_box);

        tv_box_count=findViewById(R.id.tv_box_count);

        et_barcode=findViewById(R.id.et_barcode);
        et_barcode.setOnKeyListener(new EtBarcodeOnKeyListener());

        btn_connect=findViewById(R.id.btn_connect);
        tv_connect_state=findViewById(R.id.tv_connect_state);


        btn_select_model_color=findViewById(R.id.btn_select_model_color);
        btn_select_model_color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!BoxNoCode.equals("")&&Integer.parseInt(BoxNoNum)>Integer.parseInt(BoxActNum)){
                    ShowMessage.Show(mContext, "当前装盒任务未扫码完成，请继续扫码或者切换盒装数");
                }else{
                    Intent intent = new Intent(mContext, SelectFactoryGoods.class);
                    startActivityForResult(intent, Lic_SelectModel);
                }
            }
        });



        btn_set_packing_number=findViewById(R.id.btn_set_packing_number);
        btn_set_packing_number.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (BoxNoCode.equals("")){
                    ShowMessage.Show(mContext, "请先扫码装盒生成盒标码后，再修改装盒数");
                }else {
                    ShowSetNumberDialog();
                }
            }
        });

        btn_eliminate=findViewById(R.id.btn_eliminate);
        btn_eliminate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (BoxNoCode.equals("")) {
                    ShowMessage.Show(mContext, "请先扫码装盒生成盒标码后，再点击剔除");
                }else{
                    Intent eIntent = new Intent(mContext, FactoryEliminateActivity.class);
                    eIntent.putExtra("PackBoxNoCode", BoxNoCode);
                    eIntent.putExtra("ScanBillno", scanBillno);
                    startActivityForResult(eIntent, Lic_Eliminate);
                }
            }
        });

        //剔除历史盒
        btn_history_eliminate=findViewById(R.id.btn_history_eliminate);
        btn_history_eliminate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (BoxNoCode!=""){
                    ShowMessage.Show(mContext,"请先完成当前装盒入库");
                }else {
                    Intent eIntent = new Intent(mContext, FactoryHistoryEliminateActivity.class);
//                eIntent.putExtra("PackBoxNoCode", "");
                    eIntent.putExtra("ScanBillno", scanBillno);
                    startActivityForResult(eIntent, 4);
                }
            }
        });

        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
            }
        });

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


        Holyes_Dv_Factory_GetUnFillBox();//读取上一次未完成的装盒入库任务
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

//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (bluetoothManager != null && bluetoothManager.getBluetoothService() != null) {
//            if (bluetoothManager.getBluetoothState() == BluetoothService.STATE_NONE) {
//                bluetoothManager.start();
//            }
//        }
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注意：不要在这里 stop，因为是全局的蓝牙服务，其他界面可能还在使用
        // 如果需要断开，应该在应用退出时调用 BluetoothManager.destroy()

    }



    /**
     * 读取未满盒盒标码信息（上一次未完成扫码入库的装盒）
     */
    public void Holyes_Dv_Factory_GetUnFillBox() {
        MyProgressDialog.show(mContext, "正在获取上一次任务", true, true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // SoCompId：供应商代号(必传)
                    // OaSuserId：扫描人员代号(必传)
                    JsonObject PackBoxObj = new JsonObject();
                    PackBoxObj.addProperty("SoCompId", sysUserInfo.getCompanyid());
                    PackBoxObj.addProperty("OaSuserId", sysUserInfo.getUserid());

                    String unFillBoxResult = accessWeb.Holyes_Dv_Factory_GetUnFillBox(PackBoxObj.toString());

//                    Log.d("main", unFillBoxResult);

                    JSONArray listjson = new JSONArray(unFillBoxResult);
                    List<Map<String, Object>>  PackBoxlist = new ArrayList<Map<String, Object>>();
                    for (int i = 0; i < listjson.length(); i++) {
                        JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
                        Map<String, Object> map1 = new HashMap<String, Object>();
                        map1.put("BoxNo", jsonObject2.optString("BoxNo"));
                        map1.put("SetNum", jsonObject2.optString("SetNum"));
                        map1.put("ActNum", jsonObject2.optString("ActNum"));
//                      "SetNum":"盒装数",
//                      "ActNum":"实装数",
                        map1.put("GoodsId", jsonObject2.optString("GoodsId"));
                        map1.put("Modelm", jsonObject2.optString("Modelm"));
                        map1.put("Colors", jsonObject2.optString("Colors"));
                        PackBoxlist.add(map1);
                    }

//                    [{"BoxNo":"A202508010000002","SetNum":3,"ActNum":2,"GoodsId":"90450C1","Modelm":"90450","Colors":"C1"}]

                    if (PackBoxlist.size()>0){
                        BoxNoNum=PackBoxlist.get(0).get("SetNum").toString();
                        BoxNoCode=PackBoxlist.get(0).get("BoxNo").toString();

                        OneCount=PackBoxlist.get(0).get("ActNum").toString();

                        BoxActNum=PackBoxlist.get(0).get("ActNum").toString();

                        GoodsId=PackBoxlist.get(0).get("GoodsId").toString();
                        GoodsModelm=PackBoxlist.get(0).get("Modelm").toString();
                        GoodsColor=PackBoxlist.get(0).get("Colors").toString();

                        ShowMessage.ShowMsg(mHandler, 5, unFillBoxResult);

                    }else{
                        ShowMessage.ShowMsg(mHandler, 7, "暂无上一次任务");
                    }

                } catch (Exception e) {
                    ShowMessage.ShowMsg(mHandler, 7, e.getMessage());
                }
            }
        }).start();
    }

    /**
     * 扫描装盒入库
     */
    public void Holyes_Dv_Factory_PackBox_NoBill(final String tBarcode) {
        MyProgressDialog.show(mContext, "正在扫描装盒", true, true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    accessWeb.mWebId = lStar + tBarcode;
//                    Barcode：物流码(必传)
//                    BoxNo：盒标码(必传)
//                    PackingNum：盒装数(必传)
//                    SoCompId：供应商代号(必传)
//                    GoodsId：产品编码(必传)
//                    OaSuserId：操作员代码(必传)
//                    ScanSn：扫描序号(必传)
//                    ScanBillNo：扫描号(唯一性，必传)
                    JsonObject PackBoxObj = new JsonObject();
                    PackBoxObj.addProperty("Barcode", tBarcode);
                    PackBoxObj.addProperty("BoxNo", BoxNoCode);
                    PackBoxObj.addProperty("PackingNum", String.valueOf(BoxNoNum));
                    PackBoxObj.addProperty("SoCompId", sysUserInfo.getCompanyid());
                    PackBoxObj.addProperty("GoodsId", GoodsId);
                    PackBoxObj.addProperty("OaSuserId", sysUserInfo.getUserid());
                    PackBoxObj.addProperty("ScanSn", String.valueOf(nSize));
                    PackBoxObj.addProperty("ScanBillNo", scanBillno);

                    String packBoxResult = accessWeb.Holyes_Dv_Factory_PackBox_NoBill(PackBoxObj.toString());

                    JSONArray listjson = new JSONArray(packBoxResult);
                    List<Map<String, Object>>  packBoxResultList = new ArrayList<Map<String, Object>>();
                    for (int i = 0; i < listjson.length(); i++) {
                        JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
                        Map<String, Object> map1 = new HashMap<String, Object>();
                        map1.put("BoxNo", jsonObject2.optString("BoxNo"));//盒标码
                        map1.put("Barcode", jsonObject2.optString("Barcode"));//物流码
                        map1.put("Modelm", jsonObject2.optString("Modelm"));//型号
                        map1.put("Colors", jsonObject2.optString("Colors"));//色号
                        map1.put("GoodsNum", jsonObject2.optString("GoodsNum"));//当前型号数量
                        map1.put("TotalNum", jsonObject2.optString("TotalNum"));//当前合计数
                        map1.put("BoxNum", jsonObject2.optString("BoxNum"));//当前装盒成功数

                        map1.put("SetNum", jsonObject2.optString("SetNum"));//设置盒装数
                        map1.put("ActNum", jsonObject2.optString("ActNum"));//当前盒已装数


                        packBoxResultList.add(map1);
                    }

                    if (packBoxResultList.size()>0){
                        nSize++;
                        BoxNoCode=packBoxResultList.get(0).get("BoxNo").toString();//盒标码

                        OneCount=packBoxResultList.get(0).get("GoodsNum").toString();//当前型号数量
                        AllCount=packBoxResultList.get(0).get("TotalNum").toString();//合计数
                        AllBox=packBoxResultList.get(0).get("BoxNum").toString();//当前装盒成功数


                        BoxActNum=packBoxResultList.get(0).get("ActNum").toString();

                        ShowMessage.ShowMsg(mHandler, HandPackBoxSuccess, packBoxResult);

                    }else{
                        ShowMessage.ShowMsg(mHandler, HandToaskErrorMsg, "【装盒入库】失败：扫描返回无数据" +packBoxResult);
                    }

                    lStar = "";

                } catch (Exception e) {
                    ShowMessage.ShowMsg(mHandler, HandToaskErrorMsg, "【装盒入库】失败：" + e.getMessage());
                    lStar = SomeUtils.isNotFromServiceError(e.getMessage());
                }
            }
        }).start();
    }


    /**
     * 设置盒装数
     */
    public void Holyes_Dv_Factory_SetFillBoxNum(final String tBoxBarcode,final String tPackingNum) {
        MyProgressDialog.show(mContext, "正在设置盒装数", true, true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
//                    BoxNo：盒标码(必传)
//                    PackingNum：盒装数(必传)
//                    SoCompId：供应商代号(必传)
//                    OaSuserId：操作员代码(必传)
                    JsonObject SetNumObj = new JsonObject();
                    SetNumObj.addProperty("BoxNo", tBoxBarcode);
                    SetNumObj.addProperty("PackingNum", tPackingNum);
                    SetNumObj.addProperty("SoCompId", sysUserInfo.getCompanyid());
                    SetNumObj.addProperty("OaSuserId", sysUserInfo.getUserid());

                    String setFillBoxResult = accessWeb.Holyes_Dv_Factory_SetFillBoxNum(SetNumObj.toString());

//                    Log.d("mian", setFillBoxResult);

                    JSONArray listjson = new JSONArray(setFillBoxResult);
                    List<Map<String, Object>> unFillBoxList = new ArrayList<Map<String, Object>>();
                    for (int i = 0; i < listjson.length(); i++) {
                        JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
                        Map<String, Object> map1 = new HashMap<String, Object>();

                        map1.put("BoxNo", jsonObject2.optString("BoxNo"));
                        map1.put("SetNum", jsonObject2.optString("SetNum"));
                        map1.put("ActNum", jsonObject2.optString("ActNum"));
                        map1.put("Modelm", jsonObject2.optString("Modelm"));
                        map1.put("Colors", jsonObject2.optString("Colors"));
                        map1.put("GoodsId", jsonObject2.optString("GoodsId"));
                        unFillBoxList.add(map1);
                    }

                    if (unFillBoxList.size()>0){

                        BoxNoNum=unFillBoxList.get(0).get("SetNum").toString();

                        BoxActNum=unFillBoxList.get(0).get("ActNum").toString();

                        ShowMessage.ShowMsg(mHandler,  8, BoxNoNum);

                    }else{
                        ShowMessage.ShowMsg(mHandler, HandToaskErrorMsg, "修改盒装数接口返回空"+setFillBoxResult);
                    }

                } catch (Exception e) {
                    ShowMessage.ShowMsg(mHandler, HandToaskErrorMsg, e.getMessage());

                }
            }
        }).start();
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

                    if (GoodsId.isEmpty()) {
                        ShowMessage.ShowMsg(mHandler, HandToaskErrorMsg, "请先选择产品");
                        return true;
                    }
                    if (Integer.parseInt(BoxNoNum)  == 0) {
                        ShowMessage.ShowMsg(mHandler, HandToaskErrorMsg, "请先设置盒装数量");
                        return true;
                    }
                    if (!isConnectedBluetooth && !isTest) {
                        ShowMessage.ShowMsg(mHandler, HandToaskErrorMsg, "请先连接蓝牙打印机");
                        return true;
                    }



//                    if (!isPackedBoxReturnResult) {
//                        showNormalDialog();
//                        //MyProgressDialog.show(mContext, "数量超出，请查看数量是否正确，以免窜盒!!!", false, true);
//                        //Toast.makeText(mContext, "数量超出，请查看数量是否正确，以免窜盒!!!", Toast.LENGTH_SHORT);
//                        MySound.errorSoundBeyond();
//                        //ShowMessage.Show(mContext, "请等装盒完成后再继续扫描，谢谢！");
//                        return true;
//                    }

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
//                        addScanPackingBarcode(barcodeStr);
                        //扫描装盒
                        Holyes_Dv_Factory_PackBox_NoBill(barcodeStr);
                    }
                }
                return true;
            } else {
                return false;
            }
        }
    }

    private Handler mHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {

                case 5:
                    //获取上一次未完成的装盒入库任务
                    MyProgressDialog.close();

                    tv_packing_number.setText(BoxNoNum);
                    tv_BoxNo_Code.setText(BoxNoCode);

                    tv_one_count.setText(OneCount);
                    tv_box_count.setText(BoxActNum);

                    tv_model_color.setText(GoodsModelm+" "+GoodsColor);
                    tv_productid.setText(GoodsId);

                    break;
                case 7:
                    //获取上一次未完成的装盒入库任务不存在或者报错时候，跳转选择入库产品界面
                    MyProgressDialog.close();
                    Intent intent = new Intent(mContext, SelectFactoryGoods.class);
                    startActivityForResult(intent, Lic_SelectModel);
                    break;

                case 8:
                    //设置盒装数
                    MyProgressDialog.close();

                    int tSacnNum=Integer.parseInt(BoxActNum);//当前盒已扫码数量
                    int tSetnum=Integer.parseInt(BoxNoNum);//设置的装盒数量

                    if (tSacnNum==tSetnum){
                        //判断修改的数量，如果等于已扫码的数量，就要有询问提示，马上打标出来，上传数据，清除本地数量
                        tv_packing_number.setText(BoxNoNum);

                        AllBox=String.valueOf(Integer.parseInt(AllBox)+1);
                        tv_all_box.setText(AllBox);

                        ShowMessage.Show(mContext,"正在打印盒标，请稍后！！！");

                        SimpleDateFormat  simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
                        boxTag = new BoxTag(BoxNoCode, "", "", GoodsModelm, GoodsColor, BoxNoNum, sysUserInfo.getUserid(), simpleDateFormat.format(new Date()).substring(0, 10),"");
                        printBoxCode(boxTag);

                        BoxNoCode="";
                        tv_BoxNo_Code.setText(BoxNoCode);

                    }else if (tSacnNum<tSetnum){
                        //判断修改的数量，如果大于已扫码的数量，累计已扫码数量，就要以当前已扫码的数量为准
                        tv_packing_number.setText(BoxNoNum);
                    }else {
                        tv_packing_number.setText(BoxNoNum);
                    }

                    break;

                case HandPackBoxSuccess:
                    //装盒扫码成任务
                    //				loading.Close();
                    MyProgressDialog.close();

                    tv_BoxNo_Code.setText(BoxNoCode);

                    tv_box_count.setText(BoxActNum);

                    tv_one_count.setText(OneCount);
                    tv_all_count.setText(AllCount);
                    tv_all_box.setText(AllBox);
                    if (Integer.parseInt(BoxActNum)==Integer.parseInt(BoxNoNum))
                    {

                        ShowMessage.Show(mContext,"正在打印盒标，请稍后！！！");

                        SimpleDateFormat  simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
                        boxTag = new BoxTag(BoxNoCode, "", "", GoodsModelm, GoodsColor, BoxNoNum, sysUserInfo.getUserid(), simpleDateFormat.format(new Date()).substring(0, 10),"");
                        printBoxCode(boxTag);

                        BoxNoCode="";
                        tv_BoxNo_Code.setText(BoxNoCode);
                    }


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
        editText.setText(BoxNoNum);
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
                        //当前盒已扫码数量
                        int SacnNum=Integer.parseInt(BoxActNum);
                        //设置数量
                        int Setnum=Integer.parseInt(numberStr);
                        if (SacnNum>0){
                            //判断修改的数量，如果小于已扫码的数量，是修改不成功
                            if (SacnNum>Setnum){
                                Toast.makeText(mContext,"当前已扫码数量大于设置数量，修改失败",Toast.LENGTH_SHORT).show();
                                MySound.errorSound();
                            }else if (SacnNum==Setnum){
                                //判断修改的数量，如果等于已扫码的数量，就要有询问提示，马上打标出来，上传数据，清除本地数量
                                Holyes_Dv_Factory_SetFillBoxNum(BoxNoCode,numberStr);

//                                BoxNoNum =numberStr;
//                                doInStock_PackBox_NoBill(tLoginID, tWebId, newTempBoxNo, product_id, number);
//                                tv_packing_number.setText(BoxNoNum);
                            }else if (SacnNum<Setnum){
                                //判断修改的数量，如果大于已扫码的数量，累计已扫码数量，就要以当前已扫码的数量为准
//                                BoxNoNum = numberStr;
//                                tv_packing_number.setText(BoxNoNum);
                                Holyes_Dv_Factory_SetFillBoxNum(BoxNoCode,numberStr);
                            }
                        }else{
//                            BoxNoNum = numberStr;
//                            tv_packing_number.setText(BoxNoNum);
                            Holyes_Dv_Factory_SetFillBoxNum(BoxNoCode,numberStr);
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .setCancelable(false)
                .create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (data == null) {
                return;
            }
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

                case Lic_SelectModel:
//                    Log.d("main", GoodsId);
//                    Log.d("main", data.getStringExtra("GoodsId"));
                    if (!GoodsId.equals(data.getStringExtra("GoodsId"))){
                        GoodsModelm=data.getStringExtra("Modelm");
                        GoodsColor=data.getStringExtra("Colors");
                        GoodsId=data.getStringExtra("GoodsId");

                        tv_model_color.setText(GoodsModelm+" "+GoodsColor);
                        tv_productid.setText(GoodsId);
                        BoxActNum="0";
                        OneCount="0";
                        tv_box_count.setText(BoxActNum);
                        tv_one_count.setText(OneCount);

                    }
                    break;

                case Lic_Eliminate:

                    OneCount=data.getStringExtra("GoodsNum");
                    AllCount=data.getStringExtra("TotalNum");
                    AllBox=data.getStringExtra("BoxNum");

                    BoxActNum=data.getStringExtra("BoxActNum");

                    tv_one_count.setText(OneCount);
                    tv_all_count.setText(AllCount);
                    tv_all_box.setText(AllBox);

                    tv_box_count.setText(BoxActNum);


                    break;

                case 4:

                    GoodsModelm=data.getStringExtra("Modelm");
                    GoodsColor=data.getStringExtra("Colors");
                    GoodsId=data.getStringExtra("GoodsId");
                    BoxNoNum=data.getStringExtra("PackingNum");
                    BoxNoCode=data.getStringExtra("BoxNo");

                    OneCount=data.getStringExtra("GoodsNum");
                    AllCount=data.getStringExtra("TotalNum");
                    AllBox=data.getStringExtra("BoxNum");

                    BoxActNum=data.getStringExtra("BoxActNum");

                    tv_one_count.setText(OneCount);
                    tv_all_count.setText(AllCount);
                    tv_all_box.setText(AllBox);

                    tv_model_color.setText(GoodsModelm+" "+GoodsColor);
                    tv_productid.setText(GoodsId);
                    tv_BoxNo_Code.setText(BoxNoCode);
                    tv_packing_number.setText(BoxNoNum);

                    tv_box_count.setText(BoxActNum);

                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }



    //打印盒标
    public void printBoxCode(BoxTag boxTag) {
        if (boxTag == null) {
            ShowMessage.ShowMsg(mHandler, HandToaskErrorMsg, "没有数据可以打印");
            return;
        }

        String message =SomeUtils.readAssetsTxt(mContext, "lab_gys");

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

        message = message.replace("%BOX", boxTag.getBoxNo());
        message = message.replace("%U", boxTag.getUserCode());
        message = message.replace("%B", boxTag.getBrandName());
        message = message.replace("%S", boxTag.getSerialName());
        message = message.replace("%M", boxTag.getModel());
        message = message.replace("%C", boxTag.getColor());
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
