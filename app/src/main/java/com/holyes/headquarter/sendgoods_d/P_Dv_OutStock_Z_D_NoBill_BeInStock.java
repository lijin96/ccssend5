package com.holyes.headquarter.sendgoods_d;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.ccssend5.R;
import com.holyes.ccssend5.entity.Para;
import com.holyes.ccssend5.lib.AccessWeb;
import com.holyes.ccssend5.lib.MySound;
import com.holyes.ccssend5.lib.ShowMessage;
import com.holyes.ccssend5.lib.SqliteDataHelper;
import com.holyes.ccssend5.lib.SysUserInfo;
import com.holyes.ccssend5.myview.MyProgressDialog;
import com.holyes.ccssend5.select.QueryScanDetail;
import com.holyes.ccssend5.select.SelectProductModelColor;
import com.holyes.ccssend5.utils.DisplayUtil;
import com.holyes.ccssend5.utils.PrintUtil;
import com.holyes.ccssend5.utils.SomeUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: P_Dv_OutStock_Z_D_NoBill_BeInStock
 * @Description: 无单有入库代销发货
 * @Author: lijin
 * @Date: 2021/3/10 14:02
 */
public class P_Dv_OutStock_Z_D_NoBill_BeInStock extends Activity {

    private Context mContext;
    private AccessWeb accWeb;//后台服务工具类
    private SysUserInfo sysUserInfo;//
    private Para para = new Para();
    private PrintUtil printbill;
    private MySound sound;
    private MyHandler handler;
    private Intent gIntent;

    private CheckBox checkBox;
    private EditText et_barcode;
    private TextView tv_curqty, tv_totalqty, tv_company_name,
            tv_billno, tv_model_colors, tv_stock_name, tv_title, tv_goodsid;
    private TextView tv_show_code;

    private List<Map<String, Object>> sacnDataList = new ArrayList<Map<String, Object>>();

    private String scanBillno = "", mBillNo = "", company_id = "", company_name = "";
    private String curcount = "0", goodsid = "", stock_id = "", stock_name = "";
    private String lastSuccessBarcode = "", lStar = "";
    private String modelm = "", colors = "";

    private final int Lic_SelectModel = 2;
    private String nScanCount = "0";//合计
    private int nSize = 0;//次数
    private int cSize = 0;//撤销发货的参数-次数


    //撤销的型号色号，产品id，单号，当前型号数量，合计
    private String Cancelmodelm = "", Cancelcolors = "", Cancelgoodsid = "", CancelmBillNo = "",
            Cancelcurcount = "";

    private String type = "";

//	private List<String> codesList= new ArrayList<String>();
//
//	Thread send ;

    private Button btn_d_nobill_beinstock_revoke;

    private PopupWindow mPopWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        DisplayUtil.setDefaultDisplay(this);
        setContentView(R.layout.new_p_dv_outstock_z_d_nobill_beinstock);

        mContext = this;
        accWeb = new AccessWeb(this);
        handler = new MyHandler();
        gIntent = getIntent();
        printbill = new PrintUtil();

        sysUserInfo = new SysUserInfo(getApplicationContext());

        //把以前扫描的数据清空
        try {
            SqliteDataHelper.getHelper(getApplicationContext()).execSQL("delete from newscandate");
        } catch (Exception e) {
            e.printStackTrace();
        }

        ((Button) findViewById(R.id.btn_list))
                .setOnClickListener(new BtnListClick());
        ((Button) findViewById(R.id.btn_print))
                .setOnClickListener(new BtnPrintClick());
        ((Button) findViewById(R.id.btn_exit))
                .setOnClickListener(new BtnExitClick());

        tv_company_name = ((TextView) findViewById(R.id.tv_company_name));
        tv_stock_name = (TextView) findViewById(R.id.tv_stock_name);
        tv_curqty = (TextView) findViewById(R.id.tv_curqty);
        tv_totalqty = (TextView) findViewById(R.id.tv_totalqty);
        tv_billno = (TextView) findViewById(R.id.tv_billno);
        tv_model_colors = (TextView) findViewById(R.id.tv_model_colors);
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_goodsid = (TextView) findViewById(R.id.tv_goodsid);

        checkBox = (CheckBox) findViewById(R.id.sacn_change);
        if (sysUserInfo.getChange()) {
            checkBox.setChecked(sysUserInfo.getChange());
        }

        checkBox.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (checkBox.isChecked()) {
                    sysUserInfo.setChange(true);
                } else {
                    sysUserInfo.setChange(false);
                }
            }
        });

        tv_show_code = (TextView) findViewById(R.id.tv_show_code);

        et_barcode = (EditText) findViewById(R.id.et_barcode);
        et_barcode.setOnKeyListener(new EtBarodeOnkeyListener());

        company_id = gIntent.getStringExtra("company_id");
        company_name = gIntent.getStringExtra("company_name");

        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        scanBillno = sysUserInfo.getUserid() + "DF" + SomeUtils.RandomScanOrder();// 系统

        tv_title.setText("【无单有入库代销发货】");
        tv_totalqty.setText("0");
        tv_curqty.setText("0");
        tv_billno.setText("");
        tv_company_name.setText(company_name);

//		send = new SendDatas();
//		send.start();

        btn_d_nobill_beinstock_revoke = (Button) findViewById(R.id.btn_d_nobill_beinstock_revoke);
        btn_d_nobill_beinstock_revoke.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub

                showPopListView();

            }
        });

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        //		sysUserInfo.SaveConfigString("searchProductSql", "");
//		if (send!=null) {
//			send.interrupt();
//			try {
//				send.join();
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
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
                case ShowMessage.HandScanSuccess:
                    MySound.scanSound();
                    if (sysUserInfo.getChange()) {
                        if (!tv_model_colors.getText().toString().equals("")) {
                            if (!type.equals(tv_model_colors.getText().toString())) {
                                MySound.errorChange();
                                SomeUtils.ShowAlertdialog(mContext, type);
                                tv_model_colors.setText(modelm + "-" + colors);
                            }
                        }
                    }
                    tv_model_colors.setText(modelm + "-" + colors);
                    tv_curqty.setText(curcount);
                    tv_totalqty.setText(nScanCount);

                    if (tv_billno != null) {
                        tv_billno.setText(mBillNo);
                    }
                    tv_goodsid.setText("(" + goodsid + ")");

                    break;
                case ShowMessage.HandScanError:
                    MySound.errorSound();
                    ShowMessage.Show(mContext, msg.obj.toString());

                    break;

                case 5:
                    MySound.scanSound();
                    tv_model_colors.setText(Cancelmodelm + "-" + Cancelcolors);
                    tv_curqty.setText(Cancelcurcount);
                    tv_totalqty.setText(nScanCount);

                    if (tv_billno != null) {
                        tv_billno.setText(CancelmBillNo);
                    }
                    tv_goodsid.setText("(" + Cancelgoodsid + ")");

                    ShowMessage.Show(mContext, "撤销成功");


                    break;

                case 8:
                    MyProgressDialog.close();
                    String[] mark = new String[2];
                    mark[0] = "发货单：" + mBillNo;
                    mark[1] = "代理商：" + tv_company_name.getText().toString();
                    //mark[2] = "仓   库 ："+tv_stock_name.getText().toString();

                    printbill.print(P_Dv_OutStock_Z_D_NoBill_BeInStock.this, "    无单有入库代销发货", mark, sacnDataList, sysUserInfo.getUserid());
                    break;
                case 9:
                    MyProgressDialog.close();
                    ShowMessage.Show(mContext, msg.obj.toString());
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }

    }

    /**
     * 弹出撤销扫码的输入框
     */
    private void showPopListView() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View contentView = inflater.inflate(R.layout.select_pop, null);
        View list = LayoutInflater.from(this).inflate(
                R.layout.new_p_dv_outstock_z_d_nobill_beinstock, null);
        if (mPopWindow == null) {
            mPopWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }


        mPopWindow.setFocusable(true);
        mPopWindow.setOutsideTouchable(false);
        mPopWindow.setBackgroundDrawable(null);
        mPopWindow.getContentView().setFocusable(true); // 这个很重要
        mPopWindow.getContentView().setFocusableInTouchMode(true);

        ColorDrawable dw = new ColorDrawable(0x00000000);
        //设置SelectPicPopupWindow弹出窗体的背景
        mPopWindow.setBackgroundDrawable(dw);

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getWindow().setAttributes(lp);

        mPopWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        mPopWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        mPopWindow.showAtLocation(
                list,
                Gravity.CENTER, 0, 0);
        mPopWindow.update();
        et_barcode.setFocusable(false);
        final EditText ed_code = (EditText) contentView.findViewById(R.id.ed_code);
        ed_code.setFocusable(true);
        ed_code.setFocusableInTouchMode(true);
        ed_code.requestFocus();
        ed_code.setHint("请扫描要撤销的条码");
        Button btn_cancel = (Button) contentView.findViewById(R.id.bt_cancel);
        btn_cancel.setText("关闭");
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1f;
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                getWindow().setAttributes(lp);
                mPopWindow.dismiss();
                et_barcode.setFocusable(true);
                et_barcode.setFocusableInTouchMode(true);
                et_barcode.requestFocus();
            }
        });
        ed_code.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        String code = ed_code.getText().toString().trim();
                        if (code.equals("")) {
                            ed_code.setText("");
                            ShowMessage.Show(mContext, "请重新扫描条码");

                        } else if (!SomeUtils.TextJudgmentSize(code)) {
                            ed_code.setText("");
                            ShowMessage.Show(mContext, "请扫描正确的条码");

                        } else {
                            //发货撤销
                            ed_code.setText("");
                            startThreadCheckCode(code);

                        }
                    }
                    return true;
                }

                return false;
            }

        });

    }


    //发货撤销功能
    private void startThreadCheckCode(final String code) {
        Thread startCode = new Thread(new Runnable() {

            @Override
            public void run() {
                String result = "";
                try {

                    accWeb.mWebId = lStar + code;

//					Barcode：扫描的条码(必传)
//	                 GoodsId：产品编号(传空)
//	                 SoCompId：总公司代号(固定值：00)(传空)
//	                 DeCompId：代理商代号(传空)
//	                 OaSuserId：扫描人员代号(必传)
//	                 StockId：仓库代号(传空)
//	                 ScanSn：扫描序号(必传)
//	                 ScanBillNo：扫描单号(必传)
//	                 BillNo：发货单号(传空)
//	                 SourceBillNo：来源单号(传空)
                    para.setBarcode(code);
                    para.setGoodsId("");
                    para.setSoCompId("");
                    para.setDeCompId("");
                    para.setOaSuserId(sysUserInfo.getUserid());
                    para.setStockId("");

                    para.setScanSn(String.valueOf(cSize));
                    para.setScanBillNo(scanBillno);
                    para.setBillNo("");
                    para.setSourceBillNo("");

                    result = accWeb.P_Dv_Scan("P_Dv_CurrentTradeCancel", para.toJson());

                    if (result == "") {
                        MySound.errorSound();
                        ShowMessage.ShowMsg(handler, "网络不给力，请稍后再试！");
                    }
                    //true;产品编号,型号,色号,当前型号数量,当前扫描的条码,发货单号
                    String[] rest = result.split(",",-1);

                    if (rest.length < 6) {
                        MySound.errorSound();
                        ShowMessage.ShowMsg(handler, "服务器返回参数不足，当前" + rest.length + "位！");

                    }
                    cSize++;
                    //					true;产品编号,型号,色号,当前型号数量,当前扫描的条码,发货单号
                    Cancelgoodsid = rest[0].trim();
                    Cancelmodelm = rest[1].trim();
                    Cancelcolors = rest[2].trim();

                    Cancelcurcount = rest[3].trim();
//					lastSuccessBarcode = rest[4].trim();


                    CancelmBillNo = rest[5];


                    nScanCount = rest[6];

                    ShowMessage.ShowMsg(handler, 5, "ok");
                    lStar = "";

                } catch (Exception e) {
                    ShowMessage.ShowMsg(handler, ShowMessage.HandScanError,
                            e.getMessage());
                    lStar = SomeUtils.isNotFromServiceError(e.getMessage());

                }
            }
        });
        startCode.start();
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

        if (resultCode == RESULT_OK) {
            if (data == null) {
                return;
            }
            switch (requestCode) {
                case Lic_SelectModel:
                    goodsid = data.getStringExtra("goodsid");
                    modelm = data.getStringExtra("modelm");
                    colors = data.getStringExtra("colors");
                    String productinfo = modelm + "-" + colors;
                    tv_model_colors.setText(productinfo);
                    tv_goodsid.setText("(" + goodsid + ")");
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

        sysUserInfo.setChange(checkBox.isChecked());
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    //			  Barcode：扫描的条码(必传)
                    //              GoodsId：退回的产品(必传)
                    //              SoCompId：总公司代号(固定值：00)
                    //              DeCompId：代理商代号(必传)
                    //              OaSuserId：扫描人员代号(必传)
                    //              StockId：仓库代号(必传)
                    //              ScanSn：扫描序号(必传)
                    //              ScanBillNo：扫描单号(必传)
                    //              BillNo：发货单号(首次扫码传空，成功再扫码时传返回的发货单号)
                    //              SourceBillNo：来源单号(传空)
                    //
                    String result = "";
                    accWeb.mWebId = lStar + contents;
                    para.setBarcode(contents);
                    para.setGoodsId(goodsid);
                    para.setSoCompId("00");
                    para.setDeCompId(company_id);
                    para.setOaSuserId(sysUserInfo.getUserid());
                    para.setStockId(stock_id);
                    para.setScanSn(String.valueOf(nSize));
                    para.setScanBillNo(scanBillno);
                    para.setBillNo(mBillNo);
                    para.setSourceBillNo("");

                    result = accWeb.P_Dv_Scan("P_Dv_OutStock_Z_D_NoBill_BeInStock", para.toJson());
                    if (result == "") {
                        MySound.errorSound();
                        ShowMessage.ShowMsg(handler, "网络不给力，请稍后再试！");
                        return;
                    }


                    nSize++;
                    if (contents.startsWith("P")) {
                        JSONArray listjson = new JSONArray(result);
                        //"GoodsId": "C00001",
                        //"Modelm": "1357",
                        //"Colors": "C01",
                        // "CurNum": "80",
                        //"PackNumber": "P200917000001",
                        // "TranLno" :"DX-00-200000001"
                        for (int i = 0; i < listjson.length(); i++) {
                            JSONObject jsonObject1 = (JSONObject) listjson.opt(i);

                            JSONObject jsonObject2 = (JSONObject) listjson.opt(0);

                            type = jsonObject2.getString("Modelm") + "-" + jsonObject2.getString("Colors");
                            goodsid = jsonObject2.getString("GoodsId");
                            modelm = jsonObject2.getString("Modelm");
                            colors = jsonObject2.getString("Colors");
                            curcount = jsonObject2.getString("CurNum");
                            //					lastSuccessBarcode = rest[4].trim();
                            if (mBillNo == null || mBillNo.isEmpty()) {
                                mBillNo = jsonObject2.getString("TranLno");
                            }

                            if (listjson.length() == 1) {
                                nScanCount = jsonObject1.getString("CurNum");
                            } else {
                                nScanCount = String.valueOf(Integer.parseInt(curcount) + Integer.parseInt(jsonObject1.getString("CurNum")));
                            }


                        }
                    } else {
                        //true;产品编号,型号,色号,当前型号数量,当前扫描的条码,入库单号
                        String[] rest = result.split(",",-1);

                        if (rest.length < 6) {
                            MySound.errorSound();
                            ShowMessage.ShowMsg(handler, "服务器返回参数不足，当前" + rest.length + "位！");
                            return;
                        }
                        //true;产品编号,型号,色号,当前型号数量,当前扫描的条码,入库单号，合计
                        type = rest[1].trim() + "-" + rest[2].trim();
                        goodsid = rest[0].trim();
                        modelm = rest[1].trim();
                        colors = rest[2].trim();
                        curcount = rest[3].trim();
                        lastSuccessBarcode = rest[4].trim();

                        if (mBillNo == null || mBillNo.isEmpty()) {
                            mBillNo = rest[5];
                        }
                        if (Integer.parseInt(nScanCount) < Integer.parseInt(rest[6].trim())) {
                            nScanCount = rest[6].trim();
                        }
                    }
                    ShowMessage.ShowMsg(handler, ShowMessage.HandScanSuccess, "");
                    lStar = "";

                } catch (Exception e) {
                    ShowMessage.ShowMsg(handler, ShowMessage.HandScanError,
                            e.getMessage());
                    lStar = SomeUtils.isNotFromServiceError(e.getMessage());
                }
            }
        });
        sendCode.start();
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

                    if (!SomeUtils.isAllNumber(mContext, tBarcode)) {
                        MySound.errorSound();
                        ShowMessage.Show(mContext, "请扫描正确的物流码【" + tBarcode + "】");
                        et_barcode.setText("");
                        return true;
                    }
                    access_send(tBarcode);
                    et_barcode.setText("");

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


            Intent intent = new Intent(mContext,
                    QueryScanDetail.class);
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
            Intent intent = new Intent(mContext, SelectProductModelColor.class);
            startActivityForResult(intent, Lic_SelectModel);
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

