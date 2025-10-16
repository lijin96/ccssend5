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
import com.holyes.ccssend5.select.QueryScanBatchDetail;
import com.holyes.ccssend5.select.QueryScanDetail;
import com.holyes.ccssend5.select.SelectBillBatchProduct;
import com.holyes.ccssend5.select.SelectBillProduct;
import com.holyes.ccssend5.utils.BillProductUtil;
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
 * @ClassName: P_Dv_OutStock_Z_D_Bill_NoInStock
 * @Description: 总公司有单无入库代销发货
 * @Author: lijin
 * @Date: 2021/3/10 14:02
 */
public class P_Dv_OutStock_Z_D_Bill_NoInStock extends Activity {

    private Context mContext;
    private AccessWeb accWeb;//后台服务工具类
    private SysUserInfo sysUserInfo;//
    private Para para = new Para();
    private PrintUtil printbill;
    private MySound sound;
    private MyHandler handler;
    private Intent gIntent;

    private TextView tv_curqty, tv_totalqty, tv_company_name, tv_source_billno,
            tv_billno, tv_model_colors, tv_stock_name, tv_title, tv_goodsid,tv_instock_batch,tv_text_batch;
    private EditText et_barcode;

    private TextView tv_show_code;

    private List<Map<String, Object>> sacnDataList = new ArrayList<Map<String, Object>>();

    private String scanBillno = "", mBillNo = "", company_id = "", supplier_name = "";
    private String curcount = "0", goodsid = "", stock_id = "", stock_name, sourceBillNo;
    private String lastSuccessBarcode = "", lStar = "";
    private String modelm = "", colors = "";

    private final int Lic_SelectModel = 2;
    private String nScanCount = "0";//合计
    private int nSize = 0;//次数
    private int cSize = 0;//撤销必传参数-次数

    //撤销的型号色号，产品id，单号，当前型号数量，合计
    private String Cancelmodelm = "", Cancelcolors = "", Cancelgoodsid = "", CancelmBillNo = "",
            Cancelcurcount = "";

    private BillProductUtil billProductUtil;

//	private List<String> codesList= new ArrayList<String>();
//
//	Thread send ;

    private Button btn_revoke;

    private PopupWindow mPopWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        DisplayUtil.setDefaultDisplay(this);
        setContentView(R.layout.new_p_dv_outstock_z_d_bill_noinstock);

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
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_title.setText("【有单无入库代销发货】");
        ((Button) findViewById(R.id.btn_list))
                .setOnClickListener(new BtnListClick());
        ((Button) findViewById(R.id.btn_print))
                .setOnClickListener(new BtnPrintClick());
        ((Button) findViewById(R.id.btn_exit))
                .setOnClickListener(new BtnExitClick());

        //选择型号色号
        ((Button) findViewById(R.id.btn_select_goodsid))
                .setOnClickListener(new BtnSelectProductClick());

        tv_company_name = ((TextView) findViewById(R.id.tv_company_name));
        tv_stock_name = (TextView) findViewById(R.id.tv_stock_name);
        tv_curqty = (TextView) findViewById(R.id.tv_curqty);
        tv_totalqty = (TextView) findViewById(R.id.tv_totalqty);
        tv_billno = (TextView) findViewById(R.id.tv_billno);
        tv_model_colors = (TextView) findViewById(R.id.tv_model_colors);
        tv_source_billno = (TextView) findViewById(R.id.tv_source_billno);
        tv_goodsid = (TextView) findViewById(R.id.tv_goodsid);
        tv_instock_batch=findViewById(R.id.tv_instock_batch);
        tv_text_batch=findViewById(R.id.tv_text_batch);

        tv_show_code = (TextView) findViewById(R.id.tv_show_code);

        et_barcode = (EditText) findViewById(R.id.et_barcode);
        et_barcode.setOnKeyListener(new EtBarodeOnkeyListener());

        company_id = gIntent.getStringExtra("supplier_id");
        supplier_name = gIntent.getStringExtra("supplier_name");
        stock_id = gIntent.getStringExtra("stock_id");
        stock_name = gIntent.getStringExtra("stock_name");
        sourceBillNo = gIntent.getStringExtra("purchecklno");

        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        scanBillno = sysUserInfo.getUserid() + "DF" + SomeUtils.RandomScanOrder();// 系统
        sysUserInfo.setIsDownload(true);

//        if (sysUserInfo.getEnterpriseId().equals("00")||sysUserInfo.getEnterpriseId().equals("08")){
//            tv_instock_batch.setVisibility(View.VISIBLE);
//            tv_text_batch.setVisibility(View.VISIBLE);
//        }


        tv_totalqty.setText("0");
        tv_curqty.setText("0");
        tv_billno.setText("");
        tv_company_name.setText(supplier_name);
        tv_stock_name.setText(stock_name);
        tv_source_billno.setText(sourceBillNo);

        billProductUtil = new BillProductUtil(mContext);
        billProductUtil.downloadBillProduct(sourceBillNo);
//		send = new SendDatas();
//		send.start();

        btn_revoke = (Button) findViewById(R.id.btn_d_bill_noinstock_revoke);
        btn_revoke.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub

                showPopListView();

            }
        });
    }

    /**
     * 弹出撤销扫码的输入框
     */
    private void showPopListView() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View contentView = inflater.inflate(R.layout.select_pop, null);
        View list = LayoutInflater.from(this).inflate(
                R.layout.new_p_dv_outstock_z_d_bill_noinstock, null);
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


    @Override
    protected void onDestroy() {

        super.onDestroy();

        //		sysUserInfo.SaveConfigString("searchProductSql", "");
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
                case ShowMessage.HandScanSuccess:
                    MySound.scanSound();

                    tv_model_colors.setText(modelm + "-" + colors);
                    tv_curqty.setText(curcount);
                    tv_totalqty.setText(nScanCount);

                    if (tv_billno != null) {
                        tv_billno.setText(mBillNo);
                    }
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
                    String[] mark = new String[3];
                    mark[0] = "发货单：" + mBillNo;
                    mark[1] = "代理商：" + tv_company_name.getText().toString();
                    mark[2] = "仓   库 ：" + tv_stock_name.getText().toString();

                    printbill.print(P_Dv_OutStock_Z_D_Bill_NoInStock.this, "    有单无入库代销发货", mark, sacnDataList, sysUserInfo.getUserid());
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

        if (goodsid.equals("")) {
            MySound.errorSound();
            ShowMessage.ShowMsg(handler, "请先扫描产品ID或者手动选择产品");
            return;

        }
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {

                String result = "";
                try {

                    accWeb.mWebId = lStar + contents;

                    //					 Barcode：扫描的条码(必传)
                    //	                 GoodsId：退回的产品(必传)
                    //	                 SoCompId：总公司代号(固定值：00)
                    //	                 DeCompId：代理商代号(必传)
                    //	                 OaSuserId：扫描人员代号(必传)
                    //	                 StockId：仓库代号(必传)
                    //	                 ScanSn：扫描序号(必传)
                    //	                 ScanBillNo：扫描单号(必传)
                    //	                 BillNo：发货单号(首次扫码传空，成功再扫码时传返回的发货单号)
                    //	                 SourceBillNo：来源单号(销售配货单号)(必传)

                    para.setBarcode(contents);
                    para.setGoodsId(goodsid);
                    para.setSoCompId("00");
                    para.setDeCompId(company_id);
                    para.setOaSuserId(sysUserInfo.getUserid());
                    para.setStockId(stock_id);
                    para.setScanSn(String.valueOf(nSize));
                    para.setScanBillNo(scanBillno);
                    para.setBillNo(mBillNo);
                    para.setSourceBillNo(sourceBillNo);

                    result = accWeb.P_Dv_Scan("P_Dv_OutStock_Z_D_Bill_NoInStock", para.toJson());

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
                    }else {
                        //true;产品编号,型号,色号,当前型号数量,当前扫描的条码,入库单号
                        String[] rest = result.split(",",-1);

                        if (rest.length < 6) {
                            MySound.errorSound();
                            ShowMessage.ShowMsg(handler, "服务器返回参数不足，当前" + rest.length + "位！");
                            return;
                        }
                        //true;产品编号,型号,色号,当前型号数量,当前扫描的条码,发货单号
                        //goodsid = rest[0].trim();
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
                    ShowMessage.ShowMsg(handler, ShowMessage.HandScanSuccess, "ok");
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

                    if (sysUserInfo.getEnterpriseId().equals("37")) {

                        if (tBarcode.length() >= 8) {
                            try {


                                Map<String, String> map = SqliteDataHelper.getHelper(getApplicationContext()).QueryDbMap("select modelm,colors,goodsid from newproduct where goodsid = '" + tBarcode + "'",
                                        null);

                                if (map.size() < 1) {
                                    String code1 = tBarcode.substring(0, 7);

                                    Map<String, String> map1 = SqliteDataHelper.getHelper(getApplicationContext()).QueryDbMap("select modelm,colors,goodsid from newtpeinomx where goodsid = '" + code1 + "'",
                                            null);

                                    if (map1.size() < 1) {

                                        String code2 = tBarcode.substring(0, 8);
                                        Map<String, String> map2 = SqliteDataHelper.getHelper(getApplicationContext()).QueryDbMap("select modelm,colors,goodsid from newtpeinomx where goodsid = '" + code2 + "'",
                                                null);

                                        if (map2.size() < 1) {

                                            String code3 = tBarcode.substring(0, 9);
                                            Map<String, String> map3 = SqliteDataHelper.getHelper(getApplicationContext()).QueryDbMap("select modelm,colors,goodsid from newtpeinomx where goodsid = '" + code3 + "'",
                                                    null);

                                            if (map3.size() < 1) {
                                                String code4 = tBarcode.substring(0, 12);
                                                Map<String, String> map4 = SqliteDataHelper.getHelper(getApplicationContext()).QueryDbMap("select modelm,colors,goodsid from newtpeinomx where goodsid = '" + code4 + "'",
                                                        null);

                                                if (map4.size() < 1) {
                                                    if (tBarcode.length() == 16) {

                                                        if (billProductUtil.productIsInBillNo(tBarcode)) {
                                                            goodsid = tBarcode;
                                                            modelm = billProductUtil.getModelColors(goodsid)[0];
                                                            colors = billProductUtil.getModelColors(goodsid)[1];
                                                            String productinfo = modelm + "-" + colors;
                                                            tv_model_colors.setText(productinfo);
                                                            tv_goodsid.setText("(" + goodsid + ")");
                                                            et_barcode.setText("");
                                                            return true;
                                                        }

                                                        if (!SomeUtils.isAllNumber(mContext, tBarcode)) {
                                                            MySound.errorSound();
                                                            ShowMessage.Show(mContext, "请扫描正确的物流码【" + tBarcode + "】");
                                                            et_barcode.setText("");
                                                            return true;
                                                        }
                                                        if (!tBarcode.isEmpty()) {

                                                            access_send(tBarcode);
                                                            et_barcode.setText("");

                                                        }
                                                    } else {
                                                        tv_model_colors.setText("");
                                                        modelm = "";
                                                        colors = "";
                                                        goodsid = "";
                                                        tv_goodsid.setText("");
                                                        MySound.errorSound();
                                                        et_barcode.setText("");
                                                        ShowMessage.Show(mContext, "当前配货单没有该产品");
                                                    }
                                                } else {
                                                    modelm = map4.get("modelm");
                                                    colors = map4.get("colors");
                                                    goodsid = map4.get("goodsid");
                                                    tv_model_colors.setText(modelm + "-" + colors);
                                                    tv_goodsid.setText(map4.get("goodsid"));
                                                    et_barcode.setText("");
                                                }
                                            } else {
                                                modelm = map3.get("modelm");
                                                colors = map3.get("colors");
                                                goodsid = map3.get("goodsid");
                                                tv_model_colors.setText(modelm + "-" + colors);
                                                tv_goodsid.setText(map3.get("goodsid"));
                                                et_barcode.setText("");
                                            }

                                        } else {
                                            modelm = map2.get("modelm");
                                            colors = map2.get("colors");
                                            goodsid = map2.get("goodsid");
                                            tv_model_colors.setText(modelm + "-" + colors);
                                            tv_goodsid.setText(map2.get("goodsid"));
                                            et_barcode.setText("");
                                        }
                                    } else {
                                        modelm = map1.get("modelm");
                                        colors = map1.get("colors");
                                        goodsid = map1.get("goodsid");
                                        tv_model_colors.setText(modelm + "-" + colors);
                                        tv_goodsid.setText(map1.get("goodsid"));
                                        et_barcode.setText("");
                                    }

                                } else {
                                    modelm = map.get("modelm");
                                    colors = map.get("colors");
                                    goodsid = map.get("goodsid");
                                    tv_model_colors.setText(modelm + "-" + colors);
                                    tv_goodsid.setText(map.get("goodsid"));
                                    et_barcode.setText("");

                                }
                            } catch (StringIndexOutOfBoundsException e) {
                                et_barcode.setText("");
                                e.printStackTrace();
                                ShowMessage.Show(mContext, "产品条码不存在---截取出错" + e.getMessage().toString());
                            }
                        } else {
                            ShowMessage.Show(mContext, "当前条码有误，请重新扫码");
                        }


                    } else {
                        if (billProductUtil.productIsInBillNo(tBarcode)) {
                            goodsid = tBarcode;
                            modelm = billProductUtil.getModelColors(goodsid)[0];
                            colors = billProductUtil.getModelColors(goodsid)[1];
                            String productinfo = modelm + "-" + colors;
                            tv_model_colors.setText(productinfo);
                            tv_goodsid.setText("(" + goodsid + ")");
                            et_barcode.setText("");
                            return true;
                        }


                        if (!SomeUtils.isAllNumber(mContext, tBarcode)) {
                            MySound.errorSound();
                            ShowMessage.Show(mContext, "请扫描正确的物流码【" + tBarcode + "】");
                            et_barcode.setText("");
                            return true;
                        }


                        if (!tBarcode.isEmpty()) {
                            access_send(tBarcode);
                            et_barcode.setText("");
                        }
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


//            Intent intent = new Intent(mContext,
//                    QueryScanDetail.class);
            Intent intent = null;
//            if (sysUserInfo.getEnterpriseId().equals("00")||sysUserInfo.getEnterpriseId().equals("08")){
//                intent = new Intent(mContext, QueryScanBatchDetail.class);
//            }else{
            intent = new Intent(mContext,QueryScanDetail.class);
//            }
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
            sysUserInfo.setIsDownload(true);//每次选择确认是否下载配货单明细数据
//            Intent intent = new Intent(mContext, SelectBillProduct.class);
            Intent intent = null;
//            if (sysUserInfo.getEnterpriseId().equals("00")||sysUserInfo.getEnterpriseId().equals("08")){
//                intent = new Intent(mContext, SelectBillBatchProduct.class);
//            }else{
                intent = new Intent(mContext, SelectBillProduct.class);
//            }
            intent.putExtra("orderno", sourceBillNo);
            intent.putExtra("aim", "P_Dv_OutStock_Z_D_Bill_BeInStock");
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

