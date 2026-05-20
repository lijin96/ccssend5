package com.holyes.headquarter.other;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;


import androidx.annotation.NonNull;

import com.example.ccssend5.R;
import com.google.gson.Gson;
import com.holyes.ccssend5.dao.PackDressBoxDao;
import com.holyes.ccssend5.entity.PackMealBoxLabel;
import com.holyes.ccssend5.entity.PackPara;
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
import com.holyes.ccssend5.utils.PrintUtil;
import com.holyes.ccssend5.utils.SomeUtils;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: P_Dv_PackDressBoxCheck
 * @Description: 套餐装盒
 * @Author: lijin
 * @Date: 2021/3/10 14:01
 */
public class P_Dv_PackDressBoxCheck extends Activity implements View.OnClickListener {

    private Context mContext;
    private AccessWeb accWeb;
    private Thread downloadThread;
    private Handler hand;
    private MySound sound;
    private SysUserInfo sysUserInfo;
    Gson gson = new Gson();
    private BoxTag boxTag, testBoxTag,MakeBox;
    private PrintUtil printUtil;
    private SimpleDateFormat simpleDateFormat;


    private List<Map<String, Object>> list;
    private String ScanCode;
    private String PackId, PackName;//套餐id，套餐名称
    private String connectedDeviceName, connectedDeviceAddress;

    private EditText et_barcode;
    private TextView tv_PackDressBox_name, tv_PackDressBox_Code;
    private TextView tv_connect_state;
    private ListView PackDressBox_list;
    private Button btn_finish, btn_connect, btn_scan_print, btn_test_packing, btn_again_print_boxcode;

    private PopupWindow mPopWindow;
    private boolean isConnectedBluetooth = false;

    private List<String> code_list = new ArrayList<String>();

    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothManager bluetoothManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_p_dv_packdressbox_check);

        accWeb = new AccessWeb(getApplicationContext());
        hand = new handShowMsg();
        mContext = this;
        sound = MySound.getMySound(this);
        sysUserInfo = new SysUserInfo(mContext);
        printUtil = new PrintUtil();

        simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

        PackId = getIntent().getStringExtra("PackId");
        PackName = getIntent().getStringExtra("PackName");
        code_list.clear();
        try {
            SqliteDataHelper.getHelper(getApplicationContext()).execSQL("delete from packdressboxscan");
        } catch (Exception e) {
            e.printStackTrace();
        }

        tv_PackDressBox_Code = (TextView) findViewById(R.id.tv_PackDressBox_Code);
        tv_PackDressBox_name = (TextView) findViewById(R.id.tv_PackDressBox_name);
        tv_PackDressBox_Code.setText(PackId);
        tv_PackDressBox_name.setText(PackName);

        tv_connect_state = (TextView) findViewById(R.id.tv_connect_state);

        et_barcode = (EditText) findViewById(R.id.et_PackDressBox_barcode);
        et_barcode.setOnKeyListener(new EtBarcodeOnKeyListener());

        PackDressBox_list = (ListView) findViewById(R.id.PackDressBox_list);

        btn_scan_print = (Button) findViewById(R.id.btn_scan_print);
        btn_scan_print.setOnClickListener(this);
        btn_connect = (Button) findViewById(R.id.btn_connect);
        btn_connect.setOnClickListener(this);

        btn_again_print_boxcode = (Button) findViewById(R.id.btn_again_print_boxcode);
        btn_again_print_boxcode.setOnClickListener(this);

        btn_test_packing = (Button) findViewById(R.id.btn_test_packing);
        btn_test_packing.setOnClickListener(this);
        btn_finish = (Button) findViewById(R.id.btn_finish);
        btn_finish.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                SomeUtils.clickKeyBack();
            }
        });


        MyProgressDialog.show(this, "正在获取数据...", false, false);
        downloadThread = new Thread(new DownLoadDataThread());
        downloadThread.start();


        // 使用全局蓝牙管理器
        bluetoothManager = BluetoothManager.getInstance();
        mBluetoothAdapter = bluetoothManager.getBluetoothAdapter();
        
        if (bluetoothManager.isBluetoothAvailable()) {
            // 如果已经连接，则不需要重新初始化
            if (!bluetoothManager.isBluetoothConnected()) {
                // 自动连接上次保存的蓝牙设备
                boolean isAutoConnecting = bluetoothManager.initBluetoothServiceAndAutoConnect(this, hand);
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
            bluetoothManager.initBluetoothServiceAndAutoConnect(this, hand);
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


                default:
                    break;
            }
        }
    }

    public void doPrintModelColorDetail() {
        List<Map<String, Object>> pList = PackDressBoxDao.getPackModelColorDetail(mContext, "");
        if (pList.size() == 0) {
            ShowMessage.Show(mContext, "没有可打印的数据");
            return;
        }

        String[] mark = new String[2];
        mark[0] = "套餐单：" + PackId;
        mark[1] = "名    称：" + PackName;
        //		mark[2] = "仓    库："+stock_name;

        printUtil.print(P_Dv_PackDressBoxCheck.this, "           套餐装盒", mark, pList, sysUserInfo.getUserid());
    }

    @Override
    public void onClick(View arg0) {
        // TODO Auto-generated method stub
        switch (arg0.getId()) {
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

            case R.id.btn_scan_print:
                doPrintModelColorDetail();
                break;
            case R.id.btn_again_print_boxcode:
//                printBoxCode(boxTag);
                showPopListView();
                break;
            case R.id.btn_test_packing:
                for (int i = 0; i < 1; i++) {

                    testBoxTag = new BoxTag("P2020091800" + i, "卡丹路运动防护镜系列", "测试", "测试", "1", "24", "1", "2020-9-18");
                    printBoxCode(testBoxTag);
//				int j=(int)(Math.random()*900)+100;
//				testBoxTag=new BoxTag("A201807300000"+j,"测试","测试", "测试", "test", "test", "1",  "2018-7-19 16:57:23");
//				printBoxCode(testBoxTag);

                }
                break;

            default:
                break;
        }
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
                hintPopInput(P_Dv_PackDressBoxCheck.this,list);
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
        Thread startCode=new Thread(new Runnable() {

            @Override
            public void run() {
                String result = "";
                try {
                    result = accWeb.GetPackMealBoxLabel(code);
                    if (result == "")
                    {
                        sound.errorSound();
                        ShowMessage.ShowMsg(hand, "网络不给力，请稍后再试！");
                        return;
                    }
//					   "PackBoxNumber": "P20091700001",    （盒标码)
//        "PackId": "200916000001",            (套餐代号)
//        "PackName": "测试套标",              (套餐名称)
//        "PackNum": "6"                       (已装数量)

                    PackMealBoxLabel mealBoxLabel = gson.fromJson(result, PackMealBoxLabel.class);


                    MakeBox=new BoxTag(mealBoxLabel.getPackBoxNumber(), mealBoxLabel.getPackName(), "测试", "测试","测试", mealBoxLabel.getPackNum(),
                            sysUserInfo.getUserid() ,simpleDateFormat.format(new Date()).substring(0, 10));
//
                    ShowMessage.ShowMsg(hand, ShowMessage.HandMakeDressBox, MakeBox);


                } catch (Exception e) {
                    ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage,
                            e.getMessage());


                }
            }
        });
        startCode.start();
    }




    public void printBoxCode(BoxTag boxTag) {

        if (boxTag == null) {
            ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage, "没有数据可以打印");
            return;
        }
//		lab_jb lab_meal
        String message = SomeUtils.readAssetsTxt(mContext, "lab_meal");
//		Log.i("main", "sendMessage--------判断boxTag");
        sendMessage(message, boxTag);

    }


    private class EtBarcodeOnKeyListener implements View.OnKeyListener {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {

            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    String barcodeStr = et_barcode.getText().toString().trim();
                    et_barcode.setText("");
                    if (!isConnectedBluetooth) {
                        MySound.errorSound();
                        ShowMessage.Show(mContext, "请先连接蓝牙打印机");
                        return true;
                    }

                    if (!SomeUtils.TextJudgmentSize(barcodeStr)) {
                        MySound.errorSound();
                        ShowMessage.Show(mContext, "请先扫描正确的条码");
                        return false;
                    } else {
                        P_Dv_PackDressBoxCheck(barcodeStr);
                    }
                }
                return true;
            } else {
                return false;
            }
        }
    }


    //发送条码到服务器

    public void P_Dv_PackDressBoxCheck(final String code) {

        MyProgressDialog.show(this, "正在发送数据...", false, false);
        Thread send = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    accWeb.mWebId = code;
                    ScanCode = code;

                    JSONObject js = new JSONObject();
                    js.put("Barcode", code);

                    String result = accWeb.P_Dv_PackDressBoxCheck(js.toString());
                    ShowMessage.ShowMsg(hand, ShowMessage.HandFailed, result);

                } catch (Exception e) {
                    ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage, e.getMessage());
                    return;
                }
            }
        });
        send.start();
    }


    //套餐装盒完成上传

    public void P_Dv_PackDressBoxWrite(final String tPara) {

        MyProgressDialog.show(this, "正在发送数据...", false, false);
        Thread send = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    accWeb.mWebId = ScanCode;
                    String result = accWeb.P_Dv_PackDressBoxWrite(tPara);

                    List<Map<String, Object>> datalist = new ArrayList<Map<String, Object>>();
                    String listsql = "select * from packdressboxscan ";
                    datalist = SqliteDataHelper.getHelper(mContext).QueryDbList(listsql, null);
                    String model = datalist.get(0).get("modelm").toString();
                    String color = datalist.get(0).get("colors").toString();
                    String num = PackDressBoxDao.queryAllScanNum(mContext);

                    boxTag = new BoxTag(result.trim(), PackName, "测试", model, color, num,
                            sysUserInfo.getUserid(), simpleDateFormat.format(new Date()).substring(0, 10));//  //如果要截取去掉时间就补上

                    ShowMessage.ShowMsg(hand, 6, "");

                } catch (Exception e) {
                    ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage, e.getMessage());
                    return;
                }
            }
        });
        send.start();
    }


    private class DownLoadDataThread implements Runnable {
        @Override
        public void run() {

            try {
                list = accWeb.GetPackMealDetail(PackId);
                ShowMessage.ShowMsg(hand, ShowMessage.HandSuccess, "success");
            } catch (Exception e) {
                ShowMessage.ShowMsg(hand, "下载出错" + e.getMessage());
            }
        }

    }


    private class handShowMsg extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ShowMessage.HandShowMessage: // 显示错误提示显示
                    ShowMessage.Show(mContext, msg.obj.toString());
                    break;
                case ShowMessage.HandSuccess:
                    List<String> sqlList = new ArrayList<String>();
                    if (list.size() > 0) {
                        String GoodsId, BrandName, GoodsType, SeriesName, Modelm, Colors, PackNum, sql;
                        for (Map<String, Object> map : list) {
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
                        String listsql = "select * from packdressboxscan";
                        list = SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList(listsql, null);
                        initListView(list);
                    }

                    break;
                case ShowMessage.HandFailed:
                    String id = msg.obj.toString();

                    //该套餐里产品id是否存在
                    if (PackDressBoxDao.queryGoodsid(mContext, id)) {
                        //条码是否存在
                        if (IsExistence(ScanCode)) {
                            if (PackDressBoxDao.queryNum(mContext, id)) {

                                int num = Integer.parseInt(PackDressBoxDao.queryGoodsidNum(mContext, id));
                                PackDressBoxDao.updatePackingByNum(mContext, num + 1, id, ScanCode);
                                code_list.add(ScanCode);
                                initListView(PackDressBoxDao.queryAllGoods(mContext));
                                //扫描数量和产品数量合计数相同时装盒上传
                                if (PackDressBoxDao.queryScanNum(mContext)) {
                                    List<PackPara> packParas = new ArrayList<PackPara>();
                                    for (int i = 0; i < code_list.size(); i++) {
                                        PackPara packPara = new PackPara();
                                        packPara.setBarcode(code_list.get(i).toString());
                                        packPara.setPackId(PackId);
                                        packPara.setOaSuserId(sysUserInfo.getUserid());
                                        packParas.add(packPara);
                                    }
                                    P_Dv_PackDressBoxWrite(gson.toJson(packParas).toString());
                                }


                            } else {
                                ShowMessage.Show(mContext, "当前扫描产品数量已满，请扫描其它产品");
                            }
                        } else {
                            ShowMessage.Show(mContext, "当前扫描条码已存在,请勿重复扫描");
                        }

                    } else {
                        ShowMessage.Show(mContext, "当前扫描产品不在本套餐内");
                    }


                    break;


                case ShowMessage.HandMakeDressBox:
                    printBoxCode(MakeBox);
                    MyProgressDialog.close();
                    ShowMessage.Show(mContext, "正在补打套标...");
                    break;

                case 6:
//				Log.i("main", "sendMessage--------判断boxTag="+boxTag.toString6());

                    printBoxCode(boxTag);

                    MyProgressDialog.close();

                    ShowMessage.Show(mContext, "正在装盒...");

                    PackDressBoxDao.emptyPackingByNum(mContext);

                    String listsql = "select * from packdressboxscan";
                    list = SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList(listsql, null);
                    initListView(list);

                    code_list.clear();
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

            MyProgressDialog.close();
        }
    }

    //条码集合里面是否包含这个条码
    private boolean IsExistence(String code) {

        if (code_list.contains(code)) {
            return false;
        } else {
            return true;
        }
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


    public void initListView(List<Map<String, Object>> mList) {
        //		Collections.sort(list,new SortListMapComparator("purchecklno"));

        SimpleAdapter adapter = new SimpleAdapter(this, mList, R.layout.item_packdressboxcheck,
                new String[]{"goodsid", "modelm", "colors", "packnum", "scannum"},
                new int[]{R.id.item_PackDressBox_id, R.id.item_PackDressBox_Modelm, R.id.item_PackDressBox_Color, R.id.item_PackDressBox_total, R.id.item_PackDressBox_scannum});
        PackDressBox_list.setAdapter(adapter);
        MyProgressDialog.close();

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

