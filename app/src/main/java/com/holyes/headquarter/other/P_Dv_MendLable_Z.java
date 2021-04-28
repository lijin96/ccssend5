package com.holyes.headquarter.other;

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
import com.holyes.ccssend5.select.SelectProductModelColor;
import com.holyes.ccssend5.utils.PrintUtil;
import com.holyes.ccssend5.utils.SomeUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: P_Dv_MendLable_Z
 * @Description: 仓库补标
 * @Author: lijin
 * @Date: 2021/3/10 14:01
 */
public class P_Dv_MendLable_Z extends Activity {

    private Context mContext;
    private AccessWeb accWeb;//后台服务工具类
    private SysUserInfo sysUserInfo;//
    private Para para = new Para();
    private PrintUtil printbill;
    private MySound sound;
    private MyHandler handler;
    private Intent gIntent;

    private TextView tv_curqty, tv_totalqty, tv_company_name, tv_source_billno,
            tv_billno, tv_model_colors, tv_stock_name, tv_title, tv_goodsid;
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

//
//	private List<String> codesList= new ArrayList<String>();
//
//	Thread send ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_p_dv_mendlable_z);

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
        tv_title.setText("【仓库补标】");
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

        tv_show_code = (TextView) findViewById(R.id.tv_show_code);

        et_barcode = (EditText) findViewById(R.id.et_barcode);
        et_barcode.setOnKeyListener(new EtBarodeOnkeyListener());

        company_id = gIntent.getStringExtra("supplier_id");
        supplier_name = gIntent.getStringExtra("supplier_name");
        stock_id = gIntent.getStringExtra("stock_id");
        stock_name = gIntent.getStringExtra("stock_name");
        sourceBillNo = gIntent.getStringExtra("purchecklno");

        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        scanBillno = sysUserInfo.getUserid() + "S" + sDateFormat.format(new java.util.Date());// 系统
        sysUserInfo.setIsDownload(true);

        tv_totalqty.setText("0");
        tv_curqty.setText("0");
        tv_billno.setText("");
        tv_company_name.setText(supplier_name);
        tv_stock_name.setText(stock_name);
        tv_source_billno.setText(sourceBillNo);

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
                    try {
                        ScanDataDao.updateDataAndUi(mContext, tv_model_colors, tv_curqty, tv_totalqty, tv_billno, curcount, goodsid, modelm, colors, mBillNo);
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
                    String[] mark = new String[1];
//				mark[0] = "发货单："+mBillNo;
//				mark[1] = "代理商："+tv_company_name.getText().toString();
                    mark[0] = "仓   库 ：" + tv_stock_name.getText().toString();

                    printbill.print(P_Dv_MendLable_Z.this, "           仓库补标", mark, sacnDataList, sysUserInfo.getUserid());
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
                    //            Barcode：扫描的条码(必传)
                    //            GoodsId：新产品编号(必传)
                    //            SoCompId：供应商代号(不传)
                    //            DeCompId：总公司代号(固定值：00)
                    //            OaSuserId：扫描人员代号(必传)
                    //            StockId：仓库代号(必传)
                    //            ScanSn：扫描序号(必传)
                    //            ScanBillNo：扫描单号(必传)
                    //            BillNo：(不传)
                    //            SourceBillNo：来源单号(不传)

                    para.setBarcode(contents);
                    para.setGoodsId(goodsid);
                    para.setSoCompId("");
                    para.setDeCompId("00");
                    para.setOaSuserId(sysUserInfo.getUserid());
                    para.setStockId(stock_id);
                    para.setScanSn(String.valueOf(nSize));
                    para.setScanBillNo(scanBillno);
                    para.setBillNo("");
                    para.setSourceBillNo("");

                    result = accWeb.P_Dv_Scan("P_Dv_MendLable_Z", para.toJson());

                    if (result == "") {
                        MySound.errorSound();
                        ShowMessage.ShowMsg(handler, "网络不给力，请稍后再试！");
                        return;
                    }
                    // true;产品编号,型号,色号,当前型号数量,当前扫描的条码,单号(该单号值不用显示)
                    String[] rest = result.split(",");

                    //			if (rest.length < 6)
                    //			{
                    //				MySound.errorSound();
                    //				ShowMessage.ShowMsg(handler, "服务器返回参数不足，当前"+ rest.length + "位！");
                    //				return false;
                    //			}
                    nSize++;
                    goodsid = rest[0].trim();
                    modelm = rest[1].trim();
                    colors = rest[2].trim();
                    curcount = rest[3].trim();
                    lastSuccessBarcode = rest[4].trim();

                    //			if(mBillNo==null||mBillNo.isEmpty())
                    //			{
                    //				mBillNo = rest[5];
                    //			}

                    //			if (Integer.parseInt(nScanCount)<Integer.parseInt(rest[6].trim())) {
                    //				nScanCount=rest[6].trim() ;
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
                    String tBarcode = "";
                    if (et_barcode.getText().toString().trim().indexOf("=") != -1) {
                        //包含
                        tBarcode = SomeUtils.InterceptCode(mContext, et_barcode.getText().toString().trim());
                    } else {
                        //不包含
                        tBarcode = et_barcode.getText().toString().trim();
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

