package com.holyes.agent.activity;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.ccssend5.R;
import com.holyes.ccssend5.dao.ScanDataDao;
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
 * @ClassName: P_Dv_RecoverScan_D
 * @Description: 代理商吊牌回收
 * @Author: lijin
 * @Date: 2021/3/5 17:23
 */
public class P_Dv_RecoverScan_D extends Activity {

    private Context mContext;
    private AccessWeb accWeb;//后台服务工具类
    private SysUserInfo sysUserInfo;//
    private Para para = new Para();
    private PrintUtil printbill;
    private MySound sound;
    private MyHandler handler;

    private TextView tv_curqty, tv_totalqty,
            tv_billno, tv_model_colors, tv_goodsid;
    private EditText et_barcode;
    private TextView tv_show_code;

    private List<Map<String, Object>> slist = new ArrayList<Map<String, Object>>();

    private String scanBillno = "", mBillNo = "";
    private String curcount = "0", goodsid = "";
    private String contents = "", lastSuccessBarcode = "", lStar = "";
    private String modelm = "", colors = "";

    private final int Lic_SelectModel = 2;
    private String nScanCount = "0";//合计
    private int nSize = 0;//次数

//	private List<String> codesList= new ArrayList<String>();
//
//	Thread send ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.p_dv_recoverscan_d);

        mContext = this;
        accWeb = new AccessWeb(this);
        handler = new MyHandler();
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

        tv_curqty = (TextView) findViewById(R.id.tv_curqty);
        tv_totalqty = (TextView) findViewById(R.id.tv_totalqty);
        tv_model_colors = (TextView) findViewById(R.id.tv_model_colors);
        tv_goodsid = (TextView) findViewById(R.id.tv_goodsid);

        tv_show_code = (TextView) findViewById(R.id.tv_show_code);

        et_barcode = (EditText) findViewById(R.id.et_barcode);
        et_barcode.setOnKeyListener(new EtBarodeOnkeyListener());


        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        scanBillno = sysUserInfo.getUserid() + "S" + SomeUtils.RandomScanOrder();// 系统

        tv_totalqty.setText("0");
        tv_curqty.setText("0");
//		send = new SendDatas();
//		send.start();

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
                case ShowMessage.HandScanSuccess:
                    MySound.scanSound();

//				tv_goodsid.setText("("+goodsid+")");
//				tv_model_colors.setText(modelm + "-" + colors);
//				tv_curqty.setText(curcount);
//				tv_totalqty.setText(nScanCount);
                    try {
                        ScanDataDao.updateDataAndUi(mContext, tv_model_colors, tv_curqty, tv_totalqty, tv_billno,tv_goodsid, curcount, goodsid, modelm, colors, mBillNo);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        ShowMessage.Show(mContext, e.getMessage());
                    }

                    break;
                case ShowMessage.HandScanError:
                    MySound.errorSound();
                    ShowMessage.Show(mContext, msg.obj.toString());

                    break;

                case 8:
                    MyProgressDialog.close();
                    String[] mark = new String[0];
                    //			mark[0] = "发货单："+mBillNo;
                    //			mark[1] = "供应商："+tv_company_name.getText().toString();
                    //			mark[2] = "仓   库 ："+tv_stock_name.getText().toString();

                    printbill.print(P_Dv_RecoverScan_D.this, "       代理商吊牌回收", mark, slist, sysUserInfo.getUserid());
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

//
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
//							codesList.clear();
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
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {
                String result = "";
                try {

                    accWeb.mWebId = lStar + contents;

                    //	   Barcode：扫描的条码(必传)
                    //	                 SoCompId：代理商代号(必传)
                    //	                 DeCompId：零售商代号(不传)
                    //	                 OaSuserId：扫描人员代号(必传)
                    //	                 ScanBillNo：扫描单号(必传)
                    para.setBarcode(contents);
                    para.setSoCompId(sysUserInfo.getCompanyid());
                    para.setDeCompId("");
                    para.setOaSuserId(sysUserInfo.getUserid());
                    para.setScanBillNo(scanBillno);

                    result = accWeb.P_Dv_Scan("P_Dv_RecoverScan_D", para.toJson());

                    if (result == "") {
                        MySound.errorSound();
                        ShowMessage.ShowMsg(handler, "网络不给力，请稍后再试！");
                        return;
                    }
                    String[] rest = result.split(",",-1);
                    // true;当前扫描的条码,产品编号,型号,色号,当前型号数量
                    //					true;6247114519411722,HY170001,1.50非球面,+1.00+1.50,1
                    lastSuccessBarcode = rest[0].trim();
                    goodsid = rest[1].trim();
                    modelm = rest[2].trim();
                    colors = rest[3].trim();
                    curcount = rest[4].trim();

                    //			if (Integer.parseInt(nScanCount)<Integer.parseInt(rest[5].trim())) {
                    //				nScanCount=rest[5].trim() ;
                    //			}
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
                    if (et_barcode.getText().toString().trim().indexOf("=") != -1||et_barcode.getText().toString().trim().indexOf("http") != -1) {
                        //包含
                        tBarcode = SomeUtils.InterceptCode(mContext, et_barcode.getText().toString().trim());
                    }else{
                        tBarcode= SomeUtils.UpdatefirstString(mContext,et_barcode.getText().toString().trim());
                    }
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
            List<Map<String, Object>> sacnDataList = SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList(
                    "select modelm,colors,sum(curcount) as curcount from newscandate GROUP BY goodsid",
                    null);
            Intent intent = new Intent(P_Dv_RecoverScan_D.this,
                    QueryScanDetail.class);
            if (sacnDataList.size() == 0) {
                intent.putExtra("mBillNo", scanBillno);
            } else {
                intent.putExtra("mBillNo", "");
            }

            startActivity(intent);
        }
    }

    /**
     * 打印按钮监听类
     */
    private class BtnPrintClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            MyProgressDialog.show(mContext, "正在打印...", true, true);

            Thread sendprint = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        List<Map<String, Object>> sacnDataList = SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList(
                                "select modelm,colors,sum(curcount) as curcount from newscandate GROUP BY goodsid",
                                null);
                        if (sacnDataList.size() == 0) {
                            slist = AccessWeb.getHelper(mContext).GetDowLoadBilldetail(sysUserInfo.getLoginid(), scanBillno);
                        } else {
                            slist = SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList(
                                    "select modelm,colors,sum(curcount) as curcount from newscandate GROUP BY goodsid",
                                    null);
                        }
                        if (slist.size() == 0) {
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
