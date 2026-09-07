package com.holyes.headquarter.sendgoods_d;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.ccssend5.R;
import com.google.gson.Gson;
import com.holyes.ccssend5.entity.PackMealBoxLabel;
import com.holyes.ccssend5.entity.Para;
import com.holyes.ccssend5.lib.AccessWeb;
import com.holyes.ccssend5.lib.MySound;
import com.holyes.ccssend5.lib.ShowMessage;
import com.holyes.ccssend5.lib.SqliteDataHelper;
import com.holyes.ccssend5.lib.SysUserInfo;
import com.holyes.ccssend5.lib.bluetooth.BluetoothManager;
import com.holyes.ccssend5.lib.bluetooth.BluetoothService;
import com.holyes.ccssend5.lib.bluetooth.BluetoothUtil;
import com.holyes.ccssend5.lib.bluetooth.BoxTag;
import com.holyes.ccssend5.lib.bluetooth.DeviceListActivity;
import com.holyes.ccssend5.myview.MyProgressDialog;
import com.holyes.ccssend5.dao.PackDressBoxDao;
import com.holyes.ccssend5.select.QueryScanDetail;
import com.holyes.ccssend5.select.SelectPackMealOutScanDetail;
import com.holyes.ccssend5.select.SelectSetmealDetails;
import com.holyes.ccssend5.utils.PrintUtil;
import com.holyes.ccssend5.utils.SomeUtils;
import com.holyes.headquarter.other.P_Dv_InStock_PackBox_Search;
import com.holyes.headquarter.other.P_Dv_PackDressBoxCheck;

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
 * @ClassName: P_Dv_OutStock_Z_D_PackMeal_NoBill_NoInStock
 * @Description: 无单无入库代销/直销套餐装盒出货  两个界面合并
 * @Author: lijin
 * @Date: 2026/5/11 15:47
 */
public class P_Dv_OutStock_Z_D_PackMeal_NoBill_NoInStock extends Activity {

    private Context mContext;
    private AccessWeb accWeb;//后台服务工具类
    private SysUserInfo sysUserInfo;//
    private Para para = new Para();
    private PrintUtil printbill;
    private MySound sound;
    private MyHandler handler;
    private Intent gIntent;

    private TextView tv_curqty, tv_totalqty, tv_pack_box_success, tv_company_name,tv_company_str,tv_billno, tv_model_colors, tv_stock_name, tv_goodsid;
    private EditText et_barcode;

    private TextView tv_packmeal;//套餐名

    private TextView tv_show_code;

    private PopupWindow mPopWindow;//弹出补打盒标的功能

    private List<Map<String, Object>> sacnDataList = new ArrayList<Map<String, Object>>();

    private String scanBillno = "", mBillNo = "", company_id = "", company_name = "";
    private String curcount = "0", goodsid = "", stock_id = "", stock_name;
    private String lastSuccessBarcode = "", lStar = "";
    private String modelm = "", colors = "";
    private String PackId="", PackName=""; //套餐id，套餐名称
    private final int Lic_SelectModel = 2;
    private final int Lic_OutScanDetail = 4;
    private String nScanCount = "0";//合计（界面显示为 已扫/计划）
    private int nSize = 0;//次数

    private String PackMealBoxCode="";//套标码
    private BoxTag boxTag, testBoxTag,MakeBox;
    private SimpleDateFormat simpleDateFormat;

    private Button btn_test_packing;//测试打印
    private Button btn_again_print_boxcode;//补打盒标

    private static final int MSG_PACK_DETAIL_LOADED = 51;

    private final Gson gson = new Gson();
    /** 已扫物流码（防重复） */
    private final List<String> scannedBarcodes = new ArrayList<String>();
    /** 与上传顺序一致：每条为条码 + 产品代号，用于整盒完成后批量 P_Dv_Scan */
    private final List<BarcodeGoods> outStockScanSession = new ArrayList<BarcodeGoods>();

    /** 本界面会话内整盒出货（P_Dv_Scan 批量）成功次数 */
    private int packBoxSuccessCount = 0;

    /** 扫描/上传串行锁，避免并发扫码导致条码列表与 scannum 不一致 */
    private final Object scanLock = new Object();

    private static final class BarcodeGoods {
        final String barcode;
        final String goodsId;

        BarcodeGoods(String barcode, String goodsId) {
            this.barcode = barcode;
            this.goodsId = goodsId;
        }
    }

    private Button btn_connect;//点击连接
    private TextView tv_connect_state;//连接状态
    private boolean isConnectedBluetooth = false;

    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothManager bluetoothManager = null;

    /** 当前/上次蓝牙打印机名称、地址（与全局 BluetoothManager、SysUserInfo 同步） */
    private String connectedDeviceName = "";
    private String connectedDeviceAddress = "";

    private String lsv_aim="";
    private TextView tv_title;

    private Button btn_scan_details;//当前扫描明细

    private TextView tv_sleevelabel;//显示返回的套标码
    private CheckBox checkBox_log;//显示返回的内容

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_p_dv_outstock_z_d_packmeal_nobill_noinstock);

        mContext = this;
        accWeb = new AccessWeb(this);
        handler = new MyHandler();
        gIntent = getIntent();
        printbill = new PrintUtil();

        sysUserInfo = new SysUserInfo(getApplicationContext());

        lsv_aim = getIntent().getStringExtra("aim");

        //把以前扫描的数据清空
        try {
            SqliteDataHelper.getHelper(getApplicationContext()).execSQL("delete from newscandate");
        } catch (Exception e) {
            e.printStackTrace();
        }
        tv_sleevelabel=findViewById(R.id.tv_sleevelabel);
        checkBox_log=findViewById(R.id.check_log);


//        ((Button) findViewById(R.id.btn_list))
//                .setOnClickListener(new BtnListClick());
        ((Button) findViewById(R.id.btn_print))
                .setOnClickListener(new BtnPrintClick());
//        ((Button) findViewById(R.id.btn_finish))
//                .setOnClickListener(new BtnExitClick());

        //选择型号色号
        ((Button) findViewById(R.id.btn_select_goodsid))
                .setOnClickListener(new BtnSelectProductClick());

        btn_connect = findViewById(R.id.btn_connect);
        tv_connect_state = findViewById(R.id.tv_connect_state);

        bluetoothManager = BluetoothManager.getInstance();
        mBluetoothAdapter = bluetoothManager.getBluetoothAdapter();

        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bluetoothManager == null || !bluetoothManager.isBluetoothAvailable()) {
                    ShowMessage.Show(mContext, "蓝牙未打开或不可用，请到系统设置中检查");
                    return;
                }
                if ("连接".contentEquals(btn_connect.getText())) {
                    Intent serverIntent = new Intent(mContext, DeviceListActivity.class);
                    startActivityForResult(serverIntent, BluetoothUtil.REQUEST_CONNECT_DEVICE);
                } else {
                    bluetoothManager.disconnectCurrentConnection();
                    btn_connect.setText("连接");
                    tv_connect_state.setText("未连接");
                    tv_connect_state.setTextColor(Color.RED);
                    isConnectedBluetooth = false;
                    connectedDeviceName = "";
                    connectedDeviceAddress = "";
                }
            }
        });
        tv_company_str=findViewById(R.id.tv_company_str);
        tv_title=findViewById(R.id.tv_title);
        //直销套标发货
        if (lsv_aim.equals("P_Dv_OutStock_Z_L_PackMeal_NoBill_NoInStock")){
            tv_company_str.setText("直营店：");
            tv_title.setText("【无单无入库直销套餐装盒出货】");
        }

        tv_company_name = ((TextView) findViewById(R.id.tv_company_name));
        tv_stock_name = (TextView) findViewById(R.id.tv_stock_name);
        tv_curqty = (TextView) findViewById(R.id.tv_curqty);
        tv_totalqty = (TextView) findViewById(R.id.tv_totalqty);
        tv_pack_box_success = (TextView) findViewById(R.id.tv_pack_box_success);
        tv_billno = (TextView) findViewById(R.id.tv_billno);
        tv_model_colors = (TextView) findViewById(R.id.tv_model_colors);
        tv_goodsid = (TextView) findViewById(R.id.tv_goodsid);
        tv_packmeal=findViewById(R.id.tv_packmeal);

        tv_show_code = (TextView) findViewById(R.id.tv_show_code);

        et_barcode = (EditText) findViewById(R.id.et_barcode);
        et_barcode.setOnKeyListener(new EtBarodeOnkeyListener());

        company_id = gIntent.getStringExtra("company_id");
        company_name = gIntent.getStringExtra("company_name");
        stock_id = gIntent.getStringExtra("stock_id");
        stock_name = gIntent.getStringExtra("stock_name");
        PackId= gIntent.getStringExtra("PackId");
        PackName= gIntent.getStringExtra("PackName");

        tv_packmeal.setText(PackName);

        btn_scan_details=findViewById(R.id.btn_scan_details);
        btn_scan_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, SelectPackMealOutScanDetail.class);
                startActivityForResult(intent, Lic_OutScanDetail);
            }
        });
//        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

        scanBillno = sysUserInfo.getUserid() + "DF" + SomeUtils.RandomScanOrder();// 系统

        //测试打印
        btn_test_packing=findViewById(R.id.btn_test_packing);
        btn_test_packing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i < 1; i++) {
                    testBoxTag = new BoxTag("P2020091800" + i, "卡丹路运动防护镜系列", "测试", "测试", "1", "24", "1", "2025-05-12");
                    printBoxCode(testBoxTag);

                }
            }
        });

        //补打套标
        btn_again_print_boxcode=findViewById(R.id.btn_again_print_boxcode);
        btn_again_print_boxcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopListView();
            }
        });

        tv_totalqty.setText("0");
        tv_curqty.setText("0");
        if (tv_pack_box_success != null) {
            tv_pack_box_success.setText("0");
        }
        tv_billno.setText("");
        tv_company_name.setText(company_name);
        tv_stock_name.setText(stock_name);
        //		send = new SendDatas();
        //		send.start();

        if (bluetoothManager.isBluetoothAvailable()) {
            // 如果已经连接，则不需要重新初始化
            if (!bluetoothManager.isBluetoothConnected()) {
                // 自动连接上次保存的蓝牙设备
                boolean isAutoConnecting = bluetoothManager.initBluetoothServiceAndAutoConnect(this, handler);
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

        MyProgressDialog.show(this, "正在加载套餐明细...", false, false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                String err = null;
                try {
                    SqliteDataHelper.getHelper(getApplicationContext()).execSQL("delete from packdressboxscan");
                    PackDressBoxDao.clearOutScanLines(getApplicationContext());
                    List<Map<String, Object>> dlist = accWeb.GetPackMealDetail(PackId);
                    if (dlist != null && dlist.size() > 0) {
                        List<String> sqlList = new ArrayList<String>();
                        String GoodsId, BrandName, GoodsType, SeriesName, Modelm, Colors, PackNum, sql;
                        for (Map<String, Object> map : dlist) {
                            GoodsId = (String) map.get("GoodsId");
                            GoodsType = (String) map.get("GoodsType");
                            BrandName = (String) map.get("BrandName");
                            SeriesName = (String) map.get("SeriesName");
                            Modelm = (String) map.get("Modelm");
                            Colors = (String) map.get("Colors");
                            PackNum = (String) map.get("PackNum");
                            sql = "insert into packdressboxscan(goodsid,brandname,goodstype,seriesname,modelm,colors,packnum,scannum)values('" + GoodsId + "','" + BrandName + "','"
                                    + GoodsType + "','" + SeriesName + "','" + Modelm + "','" + Colors + "','" + PackNum + "','0')";
                            sqlList.add(sql);
                        }
                        SqliteDataHelper.getHelper(getApplicationContext()).BatchOperation(sqlList);
                    }
                } catch (Exception e) {
                    err = e.getMessage();
                    if (err == null) {
                        err = "未知错误";
                    }
                }
                Message m = handler.obtainMessage(MSG_PACK_DETAIL_LOADED);
                m.obj = err;
                handler.sendMessage(m);
            }
        }).start();
    }

    /**
     * 弹出扫码的输入框
     * */
    private void showPopListView(){
        LayoutInflater inflater = LayoutInflater.from(this);
        View contentView = inflater.inflate(R.layout.select_pop, null);
        final View list= LayoutInflater.from(this).inflate(
                R.layout.activity_p_dv_packdressbox_check, null);
        if (mPopWindow == null) {
            mPopWindow = new PopupWindow(contentView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        }

        mPopWindow.update();

        mPopWindow.setFocusable(true);
        mPopWindow.setOutsideTouchable(false);
        mPopWindow.setBackgroundDrawable(null);
//        mPopWindow.getContentView().setFocusable(true); // 这个很重要
//        mPopWindow.getContentView().setFocusableInTouchMode(true);

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getWindow().setAttributes(lp);

        mPopWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        mPopWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        mPopWindow.showAtLocation(
                list,
                Gravity.CENTER , 0, 0);


        et_barcode.setFocusable(false);
        final EditText ed_code = (EditText) contentView.findViewById(R.id.ed_code);
        ed_code.setFocusable(true);
        ed_code.requestFocus();
        ed_code.setFocusableInTouchMode(true);

        ed_code.setHint("请扫描物流码");

        Button btn_cancel=(Button) contentView.findViewById(R.id.bt_cancel);
        btn_cancel.setText("关闭");
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                WindowManager.LayoutParams lp = getWindow().getAttributes();
//                lp.alpha = 1f;
//                getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
//                getWindow().setAttributes(lp);
                mPopWindow.dismiss();
                et_barcode.setFocusable(true);
                et_barcode.setFocusableInTouchMode(true);
                et_barcode.requestFocus();

            }
        });



        mPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                hintPopInput(mContext,list);
                if(getWindow()!=null){
                    WindowManager.LayoutParams params = getWindow().getAttributes();
                    params.alpha = 1.0f;
                    getWindow().setAttributes(params);
                }
            }
        });

        Button bt_pop_ok=contentView.findViewById(R.id.bt_pop_ok);
        bt_pop_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code=ed_code.getText().toString().trim();
                if (code.equals("")) {
                    ed_code.setText("");
                    ShowMessage.Show(mContext, "请重新扫描条码");

                }else {
                    //补打盒标
                    ed_code.setText("");
                    startThreadCheckCode(code);
                }

            }
        });



//        ed_code.setOnKeyListener(new View.OnKeyListener() {
//
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                if(keyCode == KeyEvent.KEYCODE_ENTER )
//                {
//                    if(event.getAction() == KeyEvent.ACTION_DOWN)
//                    {
//                        String code=ed_code.getText().toString().trim();
//                        if (code.equals("")) {
//                            ed_code.setText("");
//                            ShowMessage.Show(mContext, "请重新扫描条码");
//
//                        }else {
//                            //补打盒标
//                            ed_code.setText("");
//                            startThreadCheckCode(code);
//
//                        }
//                    }
//                    return true;
//                }
//
//                return false;
//            }
//
//        });

        ed_code.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView text, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String code=ed_code.getText().toString().trim();
                    if (code.equals("")) {
                        ed_code.setText("");
                        ShowMessage.Show(mContext, "请重新扫描条码");
                    }else {
                        //补打盒标
                        ed_code.setText("");
                        startThreadCheckCode(code);
                    }
                    return true;
                }
                return false;
            }
        });


    }


    public void hintPopInput(final Context context, final View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }


    //补打套标功能
    private void startThreadCheckCode(final String code) {
        MyProgressDialog.show(mContext, "正在获取信息...", true, true);
        Thread startCode=new Thread(new Runnable() {
            @Override
            public void run() {
                String result = "";
                try {
                    result = accWeb.GetPackMealBoxLabel(code);
//                    Log.d("main", result);
                    if (result == "")
                    {
                        sound.errorSound();
                        ShowMessage.ShowMsg(handler, "网络不给力，请稍后再试！");
                        return;
                    }
//		    "PackBoxNumber": "P20091700001",    （盒标码)
//          "PackId": "200916000001",            (套餐代号)
//          "PackName": "测试套标",              (套餐名称)
//          "PackNum": "6"                       (已装数量)
                    PackMealBoxLabel mealBoxLabel = gson.fromJson(result, PackMealBoxLabel.class);
                    MakeBox=new BoxTag(mealBoxLabel.getPackBoxNumber(), mealBoxLabel.getPackName(), "测试", "测试","测试", mealBoxLabel.getPackNum(),sysUserInfo.getUserid() ,simpleDateFormat.format(new Date()).substring(0, 10));
//
                    ShowMessage.ShowMsg(handler, ShowMessage.HandMakeDressBox, MakeBox);
                } catch (Exception e) {
                    ShowMessage.ShowMsg(handler, ShowMessage.HandShowMessage,
                            e.getMessage());
                }
            }
        });
        startCode.start();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (bluetoothManager != null && bluetoothManager.getBluetoothService() != null) {
            if (bluetoothManager.getBluetoothState() == BluetoothService.STATE_NONE) {
                bluetoothManager.start();
            }
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateBluetoothConnectionStatus();
            }
        }, 200);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateBluetoothConnectionStatus();
            }
        }, 500);
    }

    /**
     * 与套餐装盒界面一致：根据 BluetoothManager / SysUserInfo 刷新底部连接状态
     */
    private void updateBluetoothConnectionStatus() {
        if (bluetoothManager == null || tv_connect_state == null || btn_connect == null) {
            return;
        }
        if (bluetoothManager.getBluetoothService() == null) {
            bluetoothManager.initBluetoothServiceAndAutoConnect(this, handler);
        }
        if (bluetoothManager.isBluetoothConnected()) {
            String currentDeviceName = bluetoothManager.getConnectedDeviceName();
            String currentDeviceAddress = bluetoothManager.getConnectedDeviceAddress();
            if (currentDeviceName != null && !currentDeviceName.isEmpty()) {
                connectedDeviceName = currentDeviceName;
                connectedDeviceAddress = currentDeviceAddress;
            } else {
                if (connectedDeviceName == null || connectedDeviceName.isEmpty()) {
                    connectedDeviceName = sysUserInfo.getConnectedBluetoothName();
                    connectedDeviceAddress = sysUserInfo.getConnectedBluetoothAddress();
                }
                if (connectedDeviceName == null || connectedDeviceName.isEmpty()) {
                    connectedDeviceName = "未知设备";
                }
            }
            isConnectedBluetooth = true;
            tv_connect_state.setText("已连接:" + connectedDeviceName);
            tv_connect_state.setTextColor(Color.parseColor("#008000"));
            btn_connect.setText("断开");
        } else {
            isConnectedBluetooth = false;
            tv_connect_state.setText("未连接");
            tv_connect_state.setTextColor(Color.RED);
            btn_connect.setText("连接");
        }
    }




    @Override
    protected void onDestroy() {

        super.onDestroy();

        sysUserInfo.SaveConfigString("searchProductSql", "");
        //		send.interrupt();
        //		try {
        //			send.join();
        //		} catch (InterruptedException e) {
        //			e.printStackTrace();
        //		}

    }

    /**
     * 处理逻辑的handler
     */
    private class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ShowMessage.HandShowMessage: // 显示错误提示显示
                    ShowMessage.Show(mContext, msg.obj.toString());
                    break;
                case ShowMessage.HandSuccess:
                    MySound.scanSound();
                    refreshQtyLabels();
                    if (tv_billno != null) {
                        tv_billno.setText(mBillNo);
                    }
                    break;
                case ShowMessage.HandScanError:
                    MySound.errorSound();
//                    if (checkBox_log.isChecked()){
//                        ShowMessage.MessageBox(mContext,"测试显示",msg.obj.toString());
//                    }
                    ShowMessage.Show(mContext, msg.obj.toString());
                    break;

                case ShowMessage.HandUploadDetail:
                    //如果显示当前返回的log是true，就显示接口返回的数据
                    ShowMessage.MessageBox(mContext,"套标发货返回接口数据",msg.obj.toString());
                    break;

                case MSG_PACK_DETAIL_LOADED:
                    MyProgressDialog.close();
                    if (msg.obj != null) {
                        ShowMessage.Show(mContext, "加载套餐明细失败：" + msg.obj.toString());
                    } else {
                        selectFirstIncompletePackDetail();
                    }
                    refreshQtyLabels();
                    break;

                case ShowMessage.HandMakeDressBox:
                    MyProgressDialog.close();
                    ShowMessage.Show(mContext, "正在补打套标...");
                    printBoxCode(MakeBox);
                    break;
                case 8:
                    MyProgressDialog.close();
                    String[] mark = new String[3];
                    mark[0] = "发货单：" + mBillNo;
                    if (lsv_aim.equals("P_Dv_OutStock_Z_L_PackMeal_NoBill_NoInStock")) {
                        mark[1] = "直营店：" + tv_company_name.getText().toString();
                    }else{
                        mark[1] = "代理商：" + tv_company_name.getText().toString();
                    }
                    mark[2] = "仓   库 ：" + tv_stock_name.getText().toString();
                    if (lsv_aim.equals("P_Dv_OutStock_Z_L_PackMeal_NoBill_NoInStock")) {
                        printbill.print(P_Dv_OutStock_Z_D_PackMeal_NoBill_NoInStock.this, "无单无入库套标直销发货", mark, sacnDataList, sysUserInfo.getUserid());
                    }else{
                        printbill.print(P_Dv_OutStock_Z_D_PackMeal_NoBill_NoInStock.this, "无单无入库套标代销发货", mark, sacnDataList, sysUserInfo.getUserid());
                    }
                    break;
                case 9:
                    MyProgressDialog.close();
                    ShowMessage.Show(mContext, msg.obj.toString());
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
                default:
                    break;
            }

            super.handleMessage(msg);
        }

    }


    /**
     * 返回按钮监听
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                //连续按返回按钮两次就退出界面
                if (SomeUtils.isDoubleClick(mContext, true)) {
                    finish();
                }
                return true;
            case KeyEvent.KEYCODE_MINUS:

        }
        return false;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == BluetoothUtil.REQUEST_CONNECT_DEVICE) {
                connectedDeviceAddress = data.getStringExtra("deviceAddress");
                connectedDeviceName = data.getStringExtra("deviceName");
                if (connectedDeviceName == null || connectedDeviceName.isEmpty()) {
                    connectedDeviceName = connectedDeviceAddress;
                }
                if (mBluetoothAdapter != null && connectedDeviceAddress != null && !connectedDeviceAddress.isEmpty()) {
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(connectedDeviceAddress);
                    isConnectedBluetooth = false;
                    tv_connect_state.setText("正在连接:" + connectedDeviceName + "...");
                    tv_connect_state.setTextColor(Color.BLACK);
                    btn_connect.setText("连接");
                    bluetoothManager.connect(device);
                }
                super.onActivityResult(requestCode, resultCode, data);
                return;
            }
            switch (requestCode) {
                // 套餐装盒出货：明细列表仅查看，不再通过点选切换型号色号
                // case Lic_SelectModel:
                //     goodsid = data.getStringExtra("goodsid");
                //     modelm = data.getStringExtra("modelm");
                //     colors = data.getStringExtra("colors");
                //     refreshQtyLabels();
                //     break;
                case Lic_OutScanDetail:
                    if (data.getBooleanExtra(SelectPackMealOutScanDetail.EXTRA_DATA_CHANGED, false)) {
                        syncPendingScanSessionFromDb();
                        if (goodsid == null || goodsid.isEmpty()) {
                            selectFirstIncompletePackDetail();
                        }
                        refreshQtyLabels();
                    }
                    break;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    //	private class SendDatas extends Thread
    //	{
    //		@Override
    //		public void run() {
    //			try
    //			{
    //				while(!Thread.currentThread().isInterrupted()){
    //					if(codesList.size()>0)
    //					{
    //						if (access_send(codesList.get(0).toString())){
    //							codesList.remove(0);
    //						}
    //						else
    //						{
    //							codesList.remove(0);
    //						}
    //					}
    //
    //				}
    //
    //			}
    //			catch(Exception e)
    //			{
    //				Log.d("main","thread end");
    //			}
    //
    //		}
    //	}

    // 请求服务
    private void access_send(final String contents) {
        ensureGoodsidReadyForScan();
        if (goodsid == null || goodsid.isEmpty()) {
            ShowMessage.ShowMsg(handler, ShowMessage.HandScanError, "套餐明细为空，无法扫描");
            return;
        }
        if (PackDressBoxDao.isGoodsidScanFull(mContext, goodsid)
                && PackDressBoxDao.queryScanNum(mContext)) {
            ShowMessage.ShowMsg(handler, ShowMessage.HandScanError, "全部型号已扫满，请等待整盒提交");
            return;
        }
        final String scanGoodsId = goodsid;
        final String scanModelm = modelm;
        final String scanColors = colors;
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (scanLock) {
                    int curScanBefore = 0;
                    try {
                        accWeb.mWebId = contents;

                        if (scannedBarcodes.contains(contents)) {
                            ShowMessage.ShowMsg(handler, ShowMessage.HandScanError, "该条码已扫描过，请勿重复");
                            return;
                        }
                        if (!PackDressBoxDao.queryGoodsid(mContext, scanGoodsId)) {
                            ShowMessage.ShowMsg(handler, ShowMessage.HandScanError, "当前产品不在本套餐明细内");
                            return;
                        }
                        if (!PackDressBoxDao.queryNum(mContext, scanGoodsId)) {
                            Map<String, Object> next = PackDressBoxDao.findNextIncompleteAfter(mContext, scanGoodsId);
                            if (next != null) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        applyPackDetailRow(next);
                                        refreshQtyLabels();
                                        ShowMessage.Show(mContext, "当前型号已扫满，已切换至："
                                                + modelm + "-" + colors + "，请继续扫描");
                                    }
                                });
                            } else {
                                ShowMessage.ShowMsg(handler, ShowMessage.HandScanError,
                                        "当前型号已扫满，整套即将提交");
                            }
                            return;
                        }

                        JSONObject checkPara = new JSONObject();
                        checkPara.put("Barcode", contents);
                        checkPara.put("GoodsId", scanGoodsId);
                        accWeb.P_Dv_PackDressBoxCheck(checkPara.toString());

                        String snStr = PackDressBoxDao.queryGoodsidNum(mContext, scanGoodsId);
                        if (snStr != null && !snStr.isEmpty()) {
                            curScanBefore = Integer.parseInt(snStr);
                        }
                        PackDressBoxDao.updatePackingByNum(mContext, curScanBefore + 1, scanGoodsId, "");
                        scannedBarcodes.add(contents);
                        outStockScanSession.add(new BarcodeGoods(contents, scanGoodsId));
                        PackDressBoxDao.insertOutScanLine(mContext, scanGoodsId, scanModelm, scanColors, contents);

                        final boolean readyUpload = PackDressBoxDao.queryScanNum(mContext);
                        final String triggerBarcode = contents;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                // 先刷新界面合计（最后一条扫完后显示 扫描总数/合计 一致）
                                refreshQtyLabels();
                                if (readyUpload) {
                                    submitPackMealUploadAfterUiReady(triggerBarcode);
                                } else {
                                    advanceToNextIncompleteIfCurrentFull(true);
                                    refreshQtyLabels();
                                    MySound.scanSound();
                                    if (tv_billno != null) {
                                        tv_billno.setText(mBillNo);
                                    }
                                }
                            }
                        });
                        lStar = "";
                    } catch (Exception e) {
                        ShowMessage.ShowMsg(handler, ShowMessage.HandScanError,
                                e.getMessage());
                        lStar = SomeUtils.isNotFromServiceError(e.getMessage());
                    }
                }
            }
        });
        sendCode.start();
    }

    /**
     * 界面合计已显示为一致后，再请求整盒上传接口
     */
    private void submitPackMealUploadAfterUiReady(final String triggerBarcode) {
        int totalScan = PackDressBoxDao.querySumScannum(mContext);
        int totalPack = PackDressBoxDao.queryTotalPacknum(mContext);
        MyProgressDialog.show(mContext,
                "合计已齐（" + totalScan + "/" + totalPack + "），正在提交出货...", false, false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    uploadPackMealBox(triggerBarcode);
                    ShowMessage.ShowMsg(handler, ShowMessage.HandSuccess, "ok");
                } catch (Exception e) {
                    ShowMessage.ShowMsg(handler, ShowMessage.HandScanError, e.getMessage());
                    lStar = SomeUtils.isNotFromServiceError(e.getMessage());
                } finally {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            MyProgressDialog.close();
                        }
                    });
                }
            }
        }).start();
    }

    /**
     * 已扫合计与计划合计一致时，批量上传并生成套标
     */
    private void uploadPackMealBox(final String triggerBarcode) throws Exception {
        String mismatch = PackDressBoxDao.getScanTotalMismatchReason(mContext);
        if (mismatch != null) {
            return;
        }

        List<HashMap<Object, Object>> packList = new ArrayList<HashMap<Object, Object>>();
        for (BarcodeGoods line : outStockScanSession) {
            HashMap<Object, Object> packmap = new HashMap<Object, Object>();
            packmap.put("PackId", PackId);
            packmap.put("Barcode", line.barcode);
            packmap.put("DeCompId", company_id);
            packmap.put("GoodsId", line.goodsId);
            packmap.put("StockId", stock_id);
            packmap.put("ScanBillNo", scanBillno);
            packmap.put("BillNo", mBillNo);
            packmap.put("OaSuserId", sysUserInfo.getUserid());
            packList.add(packmap);
        }

        accWeb.mWebId = triggerBarcode;
        String uploadRes;
        if (lsv_aim.equals("P_Dv_OutStock_Z_L_PackMeal_NoBill_NoInStock")) {
            uploadRes = accWeb.P_Dv_Scan("P_Dv_OutStock_Z_L_PackMeal_NoBill_NoInStock", gson.toJson(packList));
        } else {
            uploadRes = accWeb.P_Dv_Scan("P_Dv_OutStock_Z_D_PackMeal_NoBill_NoInStock", gson.toJson(packList));
        }
//        Log.d("main", uploadRes);
        if (checkBox_log.isChecked())
        {
            //如果是显示log数据就显示接口返回的数据
            ShowMessage.ShowMsg(handler, ShowMessage.HandUploadDetail, uploadRes);
        }
        if (uploadRes == null || uploadRes.trim().isEmpty()) {
            throw new Exception("整盒出货上传失败：无返回"+uploadRes);
        }
        JSONArray listjson = new JSONArray(uploadRes);
        if (listjson.length() == 0) {
            throw new Exception("整盒出货上传成功：但无有效数据"+uploadRes);
        }
        JSONObject jsonObject2 = listjson.getJSONObject(0);
        if ((mBillNo == null || mBillNo.isEmpty()) && jsonObject2.has("BillNo")) {
            mBillNo = jsonObject2.getString("BillNo");
        }

        PackMealBoxCode = jsonObject2.getString("BoxCode");
        String PackMealnum = PackDressBoxDao.queryAllScanNum(mContext);
        boxTag = new BoxTag(PackMealBoxCode, PackName, "测试", "", "", PackMealnum,sysUserInfo.getUserid(), simpleDateFormat.format(new Date()).substring(0, 10));

        packBoxSuccessCount++;
        PackDressBoxDao.emptyPackingByNum(mContext);
        PackDressBoxDao.clearOutScanLines(mContext);
        scannedBarcodes.clear();
        outStockScanSession.clear();

        handler.post(new Runnable() {
            @Override
            public void run() {
                selectFirstIncompletePackDetail();
                MySound.scanSound();
                tv_sleevelabel.setText(PackMealBoxCode);
                refreshQtyLabels();
                printBoxCode(boxTag);
                ShowMessage.Show(mContext, "整套套餐明细已扫满，已提交出货");
            }
        });
    }


    /**
     * 输入框监听
     */
    private class EtBarodeOnkeyListener implements View.OnKeyListener {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {

            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {

                    String tBarcode = "";
                    if (et_barcode.getText().toString().trim().indexOf("=") != -1||et_barcode.getText().toString().trim().indexOf("http") != -1) {
                        //包含
                        tBarcode = SomeUtils.InterceptCode(mContext, et_barcode.getText().toString().trim());
                    } else {
                        //不包含
                        tBarcode = SomeUtils.UpdatefirstString(mContext,et_barcode.getText().toString().trim());
                    }
                    tv_show_code.setText(tBarcode);
//                    String tBarcode = et_barcode.getText().toString().trim();
                    if (tBarcode.isEmpty()) {
                        MySound.errorSound();
                        ShowMessage.Show(getApplicationContext(), "请扫描二维码，谢谢！");
                        return true;
                    }
                    if (!isConnectedBluetooth) {
                        MySound.errorSound();
                        ShowMessage.Show(mContext, "请先连接蓝牙打印机");
                        return true;
                    }
                    if (!SomeUtils.isAllNumber(mContext, tBarcode)) {
                        MySound.errorSound();
                        ShowMessage.Show(mContext, "请扫描正确的物流码【" + tBarcode + "】");
                        et_barcode.setText("");
                        return true;
                    }
                    if (!et_barcode.getText().toString().trim().isEmpty()) {
                        access_send(tBarcode);
                        et_barcode.setText("");
                    }
                }
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * 明细按钮监听类
     */
    private class BtnListClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            Intent intent = new Intent(mContext, QueryScanDetail.class);
            intent.putExtra("mBillNo", scanBillno);
            startActivity(intent);
        }
    }

    /**
     * 打印按钮监听类
     */
    private class BtnPrintClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            MyProgressDialog.show(mContext, "正在打印...", false, true);
            Thread sendprint = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        sacnDataList = accWeb.GetDowLoadBilldetail(sysUserInfo.getLoginid(), scanBillno);
//                        Log.d("main", sacnDataList.toString());
                        if (sacnDataList.size() == 0) {
                            ShowMessage.ShowMsg(handler, 9, "没有可打印的数据");
                            return;
                        }
                        ShowMessage.ShowMsg(handler, 8, "打印");
                    } catch (Exception e) {
                        ShowMessage.ShowMsg(handler, 9, e.getMessage());
                    }
                }
            });
            sendprint.start();
        }
    }

    /**
     * 退出按钮监听类
     */
    private class BtnExitClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (SomeUtils.isDoubleClick(mContext, true)) {
                finish();
            }
        }
    }

    /**
     * 选择型号色号按钮监听
     */
    private class BtnSelectProductClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            // 原逻辑：未扫满时不允许切换型号（已改为自动按序扫描，此处仅打开明细查看）
            // if (goodsid != null && !goodsid.isEmpty()) {
            //     int pack = PackDressBoxDao.queryPacknumForGoodsid(mContext, goodsid);
            //     int scan = 0;
            //     try {
            //         String sn = PackDressBoxDao.queryGoodsidNum(mContext, goodsid);
            //         if (sn != null && !sn.isEmpty()) {
            //             scan = Integer.parseInt(sn);
            //         }
            //     } catch (Exception ignored) {
            //     }
            //     if (scan>0&&scan < pack) {
            //         ShowMessage.Show(mContext, "当前产品尚未扫满，请继续扫描；扫满后再选择其它明细产品");
            //         return;
            //     }
            // }
            Intent intent = new Intent(mContext, SelectSetmealDetails.class);
            intent.putExtra("SetmealId", PackId);
            intent.putExtra("use_local_detail", true);
            intent.putExtra("view_only", true);
            startActivity(intent);
            // startActivityForResult(intent, Lic_SelectModel);
        }
    }

    private void applyPackDetailRow(Map<String, Object> row) {
        if (row == null) {
            goodsid = "";
            modelm = "";
            colors = "";
            return;
        }
        Object g = row.get("goodsid");
        goodsid = g == null ? "" : g.toString();
        Object m = row.get("modelm");
        modelm = m == null ? "" : m.toString();
        Object c = row.get("colors");
        colors = c == null ? "" : c.toString();
    }

    /** 默认选中第一个尚未扫满的型号色号 */
    private void selectFirstIncompletePackDetail() {
        applyPackDetailRow(PackDressBoxDao.findFirstIncompleteDetail(mContext));
    }

    /**
     * 扫描前：若无当前型号或当前已扫满，则自动切到下一个未扫满的型号
     */
    private void ensureGoodsidReadyForScan() {
        if (goodsid == null || goodsid.isEmpty()) {
            selectFirstIncompletePackDetail();
            return;
        }
        if (PackDressBoxDao.isGoodsidScanFull(mContext, goodsid)) {
            Map<String, Object> next = PackDressBoxDao.findNextIncompleteAfter(mContext, goodsid);
            if (next != null) {
                applyPackDetailRow(next);
            }
        }
    }

    /**
     * 当前型号扫满后自动切换到下一个；showTip 为 true 时提示用户
     */
    private void advanceToNextIncompleteIfCurrentFull(boolean showTip) {
        if (goodsid == null || goodsid.isEmpty()) {
            return;
        }
        if (!PackDressBoxDao.isGoodsidScanFull(mContext, goodsid)) {
            return;
        }
        Map<String, Object> next = PackDressBoxDao.findNextIncompleteAfter(mContext, goodsid);
        if (next == null) {
            return;
        }
        String prevLabel = modelm + "-" + colors;
        applyPackDetailRow(next);
        String nextLabel = modelm + "-" + colors;
        if (showTip && !nextLabel.equals(prevLabel)) {
            ShowMessage.Show(mContext, "当前型号已扫满，已切换至：" + nextLabel);
        }
    }

    /**
     * 从本地未提交条码表恢复内存中的防重列表与上传会话列表
     */
    private void syncPendingScanSessionFromDb() {
        scannedBarcodes.clear();
        outStockScanSession.clear();
        List<Map<String, Object>> lines = PackDressBoxDao.queryOutScanLines(mContext, "");
        if (lines == null || lines.isEmpty()) {
            return;
        }
        for (Map<String, Object> line : lines) {
            Object bcObj = line.get("barcode");
            Object gidObj = line.get("goodsid");
            if (bcObj == null || gidObj == null) {
                continue;
            }
            String bc = bcObj.toString();
            String gid = gidObj.toString();
            if (bc.isEmpty() || gid.isEmpty()) {
                continue;
            }
            scannedBarcodes.add(bc);
            outStockScanSession.add(new BarcodeGoods(bc, gid));
        }
    }

    /**
     * 此型号累计、合计与套餐本地明细 packdressboxscan 一致（已扫/计划）
     */
    private void refreshQtyLabels() {
        int totalPack = 0;
        try {
            String tp = PackDressBoxDao.queryAllScanNum(mContext);
            if (tp != null && !tp.isEmpty()) {
                totalPack = Integer.parseInt(tp);
            }
        } catch (Exception ignored) {
        }
        int totalScan = PackDressBoxDao.querySumScannum(mContext);
        tv_totalqty.setText(totalScan + "/" + totalPack);
        if (totalPack > 0 && totalScan == totalPack) {
            tv_totalqty.setTextColor(Color.parseColor("#008000"));
        } else {
            tv_totalqty.setTextColor(Color.BLACK);
        }
        if (tv_pack_box_success != null) {
            tv_pack_box_success.setText(String.valueOf(packBoxSuccessCount));
        }

        if (goodsid == null || goodsid.isEmpty()) {
            tv_curqty.setText("0/0");
            tv_model_colors.setText("（套餐明细加载中…）");
            tv_goodsid.setText("");
            return;
        }
        int pn = PackDressBoxDao.queryPacknumForGoodsid(mContext, goodsid);
        String snStr = PackDressBoxDao.queryGoodsidNum(mContext, goodsid);
        int sn = 0;
        try {
            if (snStr != null && !snStr.isEmpty()) {
                sn = Integer.parseInt(snStr);
            }
        } catch (Exception ignored) {
        }
        String qtyPart = sn + "/" + pn;
        tv_curqty.setText(qtyPart);
        tv_model_colors.setText(modelm + "-" + colors + "  " + qtyPart);
        tv_goodsid.setText("(" + goodsid + ")");
        tv_billno.setText(mBillNo);
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

    public void printBoxCode(BoxTag boxTag) {

        if (boxTag == null) {
            ShowMessage.ShowMsg(handler, ShowMessage.HandShowMessage, "没有数据可以打印");
            return;
        }
//		lab_jb lab_meal
        String message = "";
        if (isFileExists("lab_meal_" + sysUserInfo.getEnterpriseId().toString() + ".txt")) {
            message = SomeUtils.readAssetsTxt(mContext, "lab_meal_" + sysUserInfo.getEnterpriseId().toString());
        }else {
            message = SomeUtils.readAssetsTxt(mContext, "lab_meal");
        }
//		Log.i("main", "sendMessage--------判断boxTag");
        sendMessage(message, boxTag);
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
        if (sysUserInfo.getEnterpriseId().toString().equals("05")) {
            //派丽蒙要求去掉工号和日期
        }else{
            message = message.replace("%U", boxTag.getUserCode());
            message = message.replace("%D", boxTag.getPackDate());
        }
        message = message.replace("%BOX", boxTag.getBoxNo());
        if (boxTag.getBrandName().length() > 7) {
            message = message.replace("%B", boxTag.getBrandName().substring(0, 7));
            message = message.replace("%S", boxTag.getBrandName().substring(8));
        } else {
            message = message.replace("%B", boxTag.getBrandName());
            message = message.replace("%S", " ");
        }
        message = message.replace("%N", boxTag.getNum());

        //byte[] send = readFileByte();
        byte[] send;
        try {
            send = message.getBytes("GBK");
            bluetoothManager.write(send);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
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

}


