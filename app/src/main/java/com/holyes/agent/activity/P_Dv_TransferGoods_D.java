package com.holyes.agent.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
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
import com.holyes.ccssend5.utils.PrintUtil;
import com.holyes.ccssend5.utils.SomeUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: P_Dv_TransferGoods_D
 * @Description: 代理商调货单函数
 * @Author: lijin
 * @Date: 2021/3/5 17:24
 */
public class P_Dv_TransferGoods_D extends Activity {

    private Context mContext;
    private AccessWeb accWeb;//后台服务工具类
    private SysUserInfo sysUserInfo;//
    private Para para = new Para();
    private PrintUtil printbill;
    private MySound sound;
    private MyHandler handler;
    private Intent gIntent;

    private TextView tv_curqty, tv_totalqty, tv_source_billno,
            tv_model_colors, tv_stock_name, tv_title;
    private EditText et_barcode;
    private TextView tv_show_code;//显示上一次扫描的条码


    private List<Map<String, Object>> sacnDataList = new ArrayList<Map<String, Object>>();

    private String scanBillno = "", mBillNo = "", DocumentNo = "", TransferNo = "", InAgentName = "";
    private String curcount = "0", sourceBillNo;
    private String lastSuccessBarcode = "", lStar = "";
    private String modelm = "", colors = "";

    private final int Lic_SelectModel = 2;
    private String nScanCount = "0";//合计
    private int nSize = 0;//次数

//	private List<String> codesList= new ArrayList<String>();
//
//	private Thread send ;

    // {{系统事件
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.new_p_dv_transfergoods_d);

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
        tv_title.setText("【调货单发货】");
        ((Button) findViewById(R.id.btn_list))
                .setOnClickListener(new BtnListClick());
        ((Button) findViewById(R.id.btn_print))
                .setOnClickListener(new BtnPrintClick());
        ((Button) findViewById(R.id.btn_exit))
                .setOnClickListener(new BtnExitClick());

        tv_stock_name = (TextView) findViewById(R.id.tv_stock_name);
        tv_curqty = (TextView) findViewById(R.id.tv_curqty);
        tv_totalqty = (TextView) findViewById(R.id.tv_totalqty);
        tv_model_colors = (TextView) findViewById(R.id.tv_model_colors);
        tv_source_billno = (TextView) findViewById(R.id.tv_source_billno);

        tv_show_code = (TextView) findViewById(R.id.tv_show_code);

        et_barcode = (EditText) findViewById(R.id.et_barcode);
        et_barcode.setOnKeyListener(new EtBarodeOnkeyListener());

        TransferNo = gIntent.getStringExtra("TransferNo");
        InAgentName = gIntent.getStringExtra("InAgentName");

        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        scanBillno = sysUserInfo.getUserid() + "S" + sDateFormat.format(new java.util.Date());// 系统
        sysUserInfo.setIsDownload(true);

        tv_totalqty.setText("0");
        tv_curqty.setText("0");
        tv_stock_name.setText(InAgentName);
        tv_source_billno.setText(TransferNo);

//		send = new SendDatas();
//		send.start();

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

                    break;
                case ShowMessage.HandScanError:
                    MySound.errorSound();
                    ShowMessage.Show(mContext, msg.obj.toString());

                    break;
                case 8:
                    MyProgressDialog.close();
                    String[] mark = new String[2];
                    mark[0] = "调货单：" + TransferNo;
                    mark[1] = "客户：" + tv_stock_name.getText().toString();

                    printbill.print(P_Dv_TransferGoods_D.this, "            调货单发货", mark, sacnDataList, sysUserInfo.getUserid());
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
    @SuppressLint("NewApi")
    private void access_send(final String contents) {

        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {

                String result = "";
                try {

                    accWeb.mWebId = lStar + contents;
                    //		     Barcode：扫描的条码(必传)
                    //             OaSuserId：扫描人员代号(必传)
                    //             ScanSn：扫描序号(必传)
                    //             ScanBillNo：扫描单号(必传)
                    //             BillNo：调出单号(首次扫码传空，成功再扫码时传返回的调出单号)
                    //             DocumentNo：调入单号(首次扫码传空，成功再扫码时传返回的调入单号)
                    //             SourceBillNo：调货单号(必传)
                    para.setBarcode(contents);
                    para.setOaSuserId(sysUserInfo.getUserid());
                    para.setScanSn(String.valueOf(nSize));
                    para.setScanBillNo(scanBillno);
                    para.setBillNo(mBillNo);
                    para.setDocumentNo(DocumentNo);
                    para.setSourceBillNo(TransferNo);

                    result = accWeb.P_Dv_TransferGoods_D(para.toJson());

                    if (result == "") {
                        MySound.errorSound();
                        ShowMessage.ShowMsg(handler, "网络不给力，请稍后再试！");
                        return;
                    }
                    //true;产品编号,型号,色号,当前型号数量,调入单号,调出单号,已扫描总数量
                    String[] rest = result.split(",");
                    if (rest.length < 5) {
                        MySound.errorSound();
                        ShowMessage.ShowMsg(handler, "服务器返回参数不足，当前" + rest.length + "位！");
                        return;
                    }
                    nSize++;
                    //			goodsid = rest[0].trim();
                    modelm = rest[0].trim();
                    colors = rest[1].trim();
                    curcount = rest[2].trim();
                    DocumentNo = rest[3].trim();

                    if (mBillNo == null || mBillNo.isEmpty()) {
                        mBillNo = rest[4];
                    }
                    if (Integer.parseInt(nScanCount) < Integer.parseInt(rest[5].trim())) {
                        nScanCount = rest[5].trim();
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
                    String tBarcode = et_barcode.getText().toString().trim();
                    if (et_barcode.getText().toString().indexOf(" ") != -1) {
                        //包含
                        tBarcode = SomeUtils.AgentCode(mContext, et_barcode.getText().toString());
                    }
                    tv_show_code.setText(tBarcode);

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
            intent.putExtra("mBillNo", "DR" + scanBillno);
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
                        sacnDataList = accWeb.GetDowLoadBilldetail(sysUserInfo.getLoginid(), "DR" + scanBillno);
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
