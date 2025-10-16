package com.holyes.headquarter.backgoods_zy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.holyes.ccssend5.utils.DisplayUtil;
import com.holyes.ccssend5.utils.PrintUtil;
import com.holyes.ccssend5.utils.SomeUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: P_Dv_ReturnedMendLabel_HaveNoBill_Cancel
 * @Description: java类作用描述
 * @Author: lijin
 * @Date: 2021/3/10 10:23
 */
public class P_Dv_ReturnedMendLabel_HaveNoBill_Cancel extends Activity {

    private Context mContext;
    private AccessWeb accWeb;//后台服务工具类
    private SysUserInfo sysUserInfo;//
    private Para para = new Para();
    private PrintUtil printbill;
    private MySound sound;
    private MyHandler handler;

    private TextView tv_curqty, tv_totalqty,
            tv_billno, tv_model_colors, tv_title, tv_goodsid;
    private EditText et_barcode;
    private TextView tv_show_code;

    private List<Map<String, Object>> sacnDataList = new ArrayList<Map<String, Object>>();

    private String scanBillno = "", mBillNo = "";
    private String curcount = "0", goodsid = "";
    private String lastSuccessBarcode = "", lStar = "";
    private String modelm = "", colors = "";

    private final int Lic_SelectModel = 2;
    private String nScanCount = "0";//合计
    private int nSize = 0;//次数

    private List<String> codesList = new ArrayList<String>();

    Thread send;

    private String aim;//直销补标撤销和代销补标撤销是同一个界面

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        DisplayUtil.setDefaultDisplay(this);
        setContentView(R.layout.activity_p_dv_returnedmendlabel_havenobill_cancel);
        mContext = this;
        accWeb = new AccessWeb(this);
        handler = new MyHandler();
        printbill = new PrintUtil();

        sysUserInfo = new SysUserInfo(getApplicationContext());
        aim = getIntent().getStringExtra("aim");

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

        tv_curqty = (TextView) findViewById(R.id.tv_curqty);
        tv_totalqty = (TextView) findViewById(R.id.tv_totalqty);
        tv_billno = (TextView) findViewById(R.id.tv_billno);
        tv_model_colors = (TextView) findViewById(R.id.tv_model_colors);
        tv_title = (TextView) findViewById(R.id.tv_title);
        if (aim.equals("060204")) {
            tv_title.setText("【直销补标撤消】");
        } else if (aim.equals("040204")) {
            tv_title.setText("【代销补标撤消】");
        }

        tv_goodsid = (TextView) findViewById(R.id.tv_goodsid);

        tv_show_code = (TextView) findViewById(R.id.tv_show_code);

        et_barcode = (EditText) findViewById(R.id.et_barcode);
        et_barcode.setOnKeyListener(new EtBarodeOnkeyListener());

        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        scanBillno = sysUserInfo.getUserid() + "C1T" + SomeUtils.RandomScanOrder();// 系统

        tv_totalqty.setText("0");
        tv_curqty.setText("0");
        tv_billno.setText("");

        send = new SendDatas();
        send.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //		sysUserInfo.SaveConfigString("searchProductSql", "");
        send.interrupt();
        try {
            send.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
                    tv_goodsid.setText("(" + goodsid + ")");
                    break;
                case ShowMessage.HandScanError:
                    MySound.errorSound();
                    ShowMessage.Show(mContext, msg.obj.toString());

                    break;
                case 8:
                    MyProgressDialog.close();
                    String[] mark = new String[1];
                    mark[0] = "发货单：" + mBillNo;

                    printbill.print(P_Dv_ReturnedMendLabel_HaveNoBill_Cancel.this, "     无单直销退货撤销", mark, sacnDataList, sysUserInfo.getUserid());

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
                    break;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    private class SendDatas extends Thread {
        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    if (codesList.size() > 0) {
                        if (access_send(codesList.get(0).toString())) {
                            codesList.remove(0);
                        } else {
                            codesList.remove(0);
                        }
                    }

                }

            } catch (Exception e) {
                Log.d("main", "thread end");
            }

        }
    }


    // 请求服务
    private boolean access_send(String contents) {
        //		if (!SomeUtils.TextJudgmentSize(contents)) {
        //			MySound.errorSound();
        //			ShowMessage.ShowMsg(handler, "请先扫描正确的条码");
        //			return false;
        //		}
        String result = "";
        try {

            accWeb.mWebId = lStar + contents;

            //Barcode：扫描的条码(必传)
            //GoodsId：退回的产品(不传)
            //SoCompId：总公司代号(不传)
            //DeCompId：直营店代号(不传)
            //OaSuserId：扫描人员代号(必传)
            //StockId：仓库代号(不传)
            //ScanSn：扫描序号(必传)
            //ScanBillNo：扫描单号(必传)
            //BillNo：退货单号(不传)
            //SourceBillNo：来源单号(不传)

            para.setBarcode(contents);
            para.setGoodsId("");
            para.setSoCompId("");
            para.setDeCompId("");
            para.setOaSuserId(sysUserInfo.getUserid());
            para.setStockId("");
            para.setScanSn(String.valueOf(nSize));
            para.setScanBillNo(scanBillno);
            para.setBillNo(mBillNo);
            para.setSourceBillNo("");

            result = accWeb.P_Dv_Scan("P_Dv_ReturnedMendLabel_HaveNoBill_Cancel", para.toJson());

            if (result == "") {
                MySound.errorSound();
                ShowMessage.ShowMsg(handler, "网络不给力，请稍后再试！");
                return false;
            }
            //true;产品编号,型号,色号,当前型号数量,当前扫描的条码,退货单号
            String[] rest = result.split(",",-1);

            if (rest.length < 6) {
                MySound.errorSound();
                ShowMessage.ShowMsg(handler, "服务器返回参数不足，当前" + rest.length + "位！");
                return false;
            }
            nSize++;
            //true;产品编号,型号,色号,当前型号数量,当前扫描的条码,发货单号
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


            ShowMessage.ShowMsg(handler, ShowMessage.HandScanSuccess, "ok");
            lStar = "";
            return true;
        } catch (Exception e) {
            ShowMessage.ShowMsg(handler, ShowMessage.HandScanError,
                    e.getMessage());
            lStar = SomeUtils.isNotFromServiceError(e.getMessage());
            return false;
        }

    }

    /**
     * 输入框监听
     */
    private class EtBarodeOnkeyListener implements View.OnKeyListener {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {

            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    tv_show_code.setText(et_barcode.getText().toString().trim());
                    if (!SomeUtils.isAllNumber(mContext, et_barcode.getText().toString().trim())) {
                        MySound.errorSound();
                        ShowMessage.Show(mContext, "请扫描正确的物流码【" + et_barcode.getText().toString().trim() + "】");
                        et_barcode.setText("");
                        return true;
                    }
                    if (codesList.size() >= 10) {
                        ShowMessage.ShowMsg(handler, 9, "当前扫描速度过快，请稍后。。。");
                        et_barcode.setText("");
                    } else {
                        codesList.add(et_barcode.getText().toString().trim());
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
//		                configuration.setToDefaults();
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

