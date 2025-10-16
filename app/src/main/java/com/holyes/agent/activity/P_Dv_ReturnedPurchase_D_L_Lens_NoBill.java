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
import com.holyes.ccssend5.dao.ScanDataDao;
import com.holyes.ccssend5.entity.Para;
import com.holyes.ccssend5.lib.AccessWeb;
import com.holyes.ccssend5.lib.MySound;
import com.holyes.ccssend5.lib.ShowMessage;
import com.holyes.ccssend5.lib.SqliteDataHelper;
import com.holyes.ccssend5.lib.SysUserInfo;
import com.holyes.ccssend5.myview.MyProgressDialog;
import com.holyes.ccssend5.select.QueryScanDetail;
import com.holyes.ccssend5.select.QueryScanLensDetail;
import com.holyes.ccssend5.utils.PrintUtil;
import com.holyes.ccssend5.utils.SomeUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: P_Dv_ReturnedPurchase_D_L_Lens_NoBill
 * @Description: 镜片零售退货
 * @Author: lijin
 * @Date: 2024年6月18日17:15:03
 */
public class P_Dv_ReturnedPurchase_D_L_Lens_NoBill extends Activity {
    @SuppressWarnings("unused")
    private Context mContext;
    private Handler hand;
    private SysUserInfo sysUserInfo;
    private PrintUtil printbill;
    private MySound sound;
    private Para para = new Para();

    private TextView tv_title, tv_curqty, tv_totalqty, tv_product_id;
    private TextView tv_company_na, tvBillno, tv_model_colors;
    private EditText edtBarcode;

    private TextView tv_show_code;

    private String Scanbillno = "";
    private String curcount = "1", product_id = "", modelm = "", colors = "";
    private String mBillNo = "";
    private String company_id = "";
    private String company_na = "";
    private String socompany_id = "";
    private String contents = "";
    private String lastSuccessBarcode = "", lStar = "";
    private String Spherical= "", Cylinder = "",Refractivity="";//球镜柱镜折射率

    private List<Map<String, Object>> slist = new ArrayList<Map<String, Object>>();

    private String nScanCount = "0";//合计
    private int nSize = 0;//次数

//	private List<String> codesList= new ArrayList<String>();
//
//	Thread send ;

    // {{系统事件
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.p_dv_returnedpurchase_d_l_lens_nobill);
        hand = new handShowMsg();
        mContext = this;
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        sysUserInfo = new SysUserInfo(getApplicationContext());
        Scanbillno = sysUserInfo.getUserid() + "2T" + SomeUtils.RandomScanOrder();// 系统
        Intent gIntent = this.getIntent();

        ((Button) findViewById(R.id.btn_list))
                .setOnClickListener(new BtnListClick());
        ((Button) findViewById(R.id.btn_print))
                .setOnClickListener(new BtnPtintClick());
        ((Button) findViewById(R.id.btn_exit))
                .setOnClickListener(new BtnExitClick());
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_company_na = (TextView) findViewById(R.id.tv_company_na);
        tv_product_id = (TextView) findViewById(R.id.tv_product_id);
        tv_curqty = (TextView) findViewById(R.id.tv_curqty);
        tv_totalqty = (TextView) findViewById(R.id.tv_totalqty);
        tvBillno = (TextView) findViewById(R.id.lbl_billno);
        tv_model_colors = (TextView) findViewById(R.id.lbl_productinfo);
        tv_show_code = (TextView) findViewById(R.id.tv_show_code);

        edtBarcode = (EditText) findViewById(R.id.txt_barcode);
        edtBarcode.setOnKeyListener(new EdtBarcodeOnkey());

        socompany_id = sysUserInfo.getCompanyid();
        tv_totalqty.setText("0");
        tv_curqty.setText("0");
        tv_model_colors.setText("");
        tvBillno.setText("");

        printbill = new PrintUtil();


        try {

            SqliteDataHelper.getHelper(getApplicationContext()).execSQL("delete from newscandate");

        } catch (Exception e) {

            e.printStackTrace();
        }

//		send = new SendDatas();
//		send.start();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//		send.interrupt();
//		try {
//			send.join();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
    }

    private class handShowMsg extends Handler {
        @SuppressWarnings("unchecked")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ShowMessage.HandShowMessage: // 显示错误提示显示
                    ShowMessage.Show(P_Dv_ReturnedPurchase_D_L_Lens_NoBill.this, msg.obj.toString());
                    break;
                case ShowMessage.HandScanSuccess:
                    MySound.scanSound();
                    //MyProgressDialog.close();
                    //				addToList();

//                    tv_model_colors.setText(modelm + "-" + colors);
                    tv_model_colors.setText(Refractivity+ "  S"+Spherical + " C" + Cylinder);
                    tv_curqty.setText(curcount);
                    tv_totalqty.setText(nScanCount);

                    if (tvBillno != null) {
                        tvBillno.setText(mBillNo);
                    }
                    tv_product_id.setText("(" + product_id + ")");
                    tv_company_na.setText(company_na);
                    break;
                case ShowMessage.HandSuccess:
                    MySound.scanSound();
                    try {
                        ScanDataDao.updateDataAndUi(mContext, tv_model_colors, tv_curqty, tv_totalqty, tvBillno,tv_product_id, curcount, product_id, modelm, colors, mBillNo);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    tv_company_na.setText(company_na);
                    tv_product_id.setText("(" + product_id + ")");
                    break;
                case ShowMessage.HandScanError:
                    MySound.errorSound();
                    //MyProgressDialog.close();
                    ShowMessage.Show(P_Dv_ReturnedPurchase_D_L_Lens_NoBill.this, msg.obj.toString());
                    break;
                case 3:
                    //MyProgressDialog.close();
                    ShowMessage.Show(P_Dv_ReturnedPurchase_D_L_Lens_NoBill.this, "网络不给力，请稍后再试！");
                    break;
                case 4:
                    //MyProgressDialog.close();
                    ShowMessage.Show(P_Dv_ReturnedPurchase_D_L_Lens_NoBill.this, msg.obj.toString());
                    break;
                case 8:
                    MyProgressDialog.close();
                    String[] mark = new String[2];
                    mark[0] = "退货单：" + mBillNo;
                    mark[1] = "零售商：" + tv_company_na.getText().toString();

                    printbill.printLens(P_Dv_ReturnedPurchase_D_L_Lens_NoBill.this, "镜片零售退货", mark, slist, sysUserInfo.getUserid());

                    break;
                case 9:
                    MyProgressDialog.close();
                    ShowMessage.Show(P_Dv_ReturnedPurchase_D_L_Lens_NoBill.this, msg.obj.toString());
                    break;
                default:
                    break;
            }
            //			//MyProgressDialog.close();
            super.handleMessage(msg);
        }
    }


    // {{明细
    private class BtnListClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            List<Map<String, Object>> list = SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList(
                    "select modelm,colors,sum(curcount) as curcount from newscandate GROUP BY goodsid",
                    null);
            Intent intent = new Intent(P_Dv_ReturnedPurchase_D_L_Lens_NoBill.this,
                    QueryScanLensDetail.class);
            if (list.size() == 0) {
                intent.putExtra("mBillNo", Scanbillno);
            } else {
                intent.putExtra("mBillNo", "");
            }
            startActivity(intent);
        }
    }

    private class BtnPtintClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            MyProgressDialog.show(mContext, "正在打印...", true, true);

            Thread sendprint = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {

//                        List<Map<String, Object>> sacnDataList = SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList(
//                                "select modelm,colors,sum(curcount) as curcount from newscandate GROUP BY goodsid",
//                                null);
//                        if (sacnDataList.size() == 0) {
                            slist = AccessWeb.getHelper(mContext).GetDowLoadLensBillDetail(sysUserInfo.getLoginid(), Scanbillno);
//                        } else {
//                            slist = SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList(
//                                    "select modelm,colors,sum(curcount) as curcount from newscandate GROUP BY goodsid",
//                                    null);
//                        }
                        if (slist.size() == 0) {
                            ShowMessage.ShowMsg(hand, 9, "没有可打印的数据");
                            return;
                        }
                        ShowMessage.ShowMsg(hand, 8, "打印");
                    } catch (Exception e) {
                        ShowMessage.ShowMsg(hand, 9, e.getMessage());
                    }

                }
            });
            sendprint.start();


        }
    }

    private class BtnExitClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (SomeUtils.isDoubleClick(P_Dv_ReturnedPurchase_D_L_Lens_NoBill.this, true)) {
                finish();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:

                if (SomeUtils.isDoubleClick(P_Dv_ReturnedPurchase_D_L_Lens_NoBill.this, true)) {
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
    @SuppressLint("NewApi")
    private void access_send(final String contents) {
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {
                String result = "";
                try {
                    AccessWeb.getHelper(getApplicationContext()).mWebId = lStar + contents;
                    //！！！注意，这个服务和其他的调的参数有差异
                    //					  tLoginId：用户登录成功的LogId
                    //				      tWebId：当前扫描条码
                    //				      pBarcode：扫描的条码(必传)
                    //				      pSoCompid：代理商代号(必传)
                    //				      pCompid：零售商代号(不传)--->代理商代号
                    //				      pUserId：扫描人员代号(必传)
                    //				      pSN：扫描序号(必传)
                    //				      pScanBillNo：扫描单号(必传)
                    //				      mBillNo：退货单号(首次传空，其他情况传返回的退货单号)

//                    result = AccessWeb.getHelper(getApplicationContext()).P_Dv_ReturnedPurchase_D_L_NoBill(contents, socompany_id,
//                            socompany_id, String.valueOf(nSize), Scanbillno,
//                            mBillNo);

                    para.setBarcode(contents);
                    para.setSoCompId(socompany_id);
                    para.setDeCompId(socompany_id);
                    para.setOaSuserId(sysUserInfo.getUserid());
                    para.setScanSn(String.valueOf(nSize));
                    para.setScanBillNo(Scanbillno);
                    para.setBillNo(mBillNo);
                    para.setFirstDelivery("");
                    result = AccessWeb.getHelper(getApplicationContext()).P_Dv_ReturnedPurchase_D_L_Lens_NoBill(para.toJson());


                    if (result.isEmpty()) {
                        MySound.errorSound();
                        ShowMessage.ShowMsg(hand, 3, "");
                        return;
                    }
                    //代理商发货日期，零售商代号，零售商名称，产品编号,型号,色号,当前型号数量,发货单号
                    //2018-02-06,CS0011907550001,深圳零售测试1,a123C1,a123,C1,2,LT-CS001-18000010


                    String rest[] = result.split(",",-1);
                    nSize++;
//                    if (mBillNo.isEmpty()) {
//                        mBillNo = rest[7];
//                    }
                    company_id = rest[1].trim();
                    company_na = rest[2].trim();
                    product_id = rest[3].trim();

//发货时间+零售商代号+零售商名称+产品代号+折射率+球镜+柱镜+此产品球柱镜扫描数量+单据编号+单据扫描数量


                    Refractivity = rest[4].trim();
                    Spherical = rest[5].trim();
                    Cylinder = rest[6].trim();

                    curcount = rest[7].trim();
                    lastSuccessBarcode = contents;

                    if (mBillNo == null || mBillNo.isEmpty()) {
                        mBillNo = rest[8];
                    }
                    if (Integer.parseInt(nScanCount)<Integer.parseInt(rest[9].trim())) {
                        nScanCount = rest[9].trim();
                    }


//                    modelm = rest[4].trim();
//                    colors = rest[5].trim();
//                    curcount = rest[6].trim();
//
//                    lastSuccessBarcode = contents;
//
////                    if (rest.length > 8) {
//                    if (Integer.parseInt(nScanCount) < Integer.parseInt(rest[8].trim())) {
//                        nScanCount = rest[8].trim();
//                    }

                    ShowMessage.ShowMsg(hand, ShowMessage.HandScanSuccess, "");
//                    } else {
//                        ShowMessage.ShowMsg(hand, ShowMessage.HandSuccess, "");
//                    }

                    lStar = "";

                } catch (Exception e) {
                    ShowMessage.ShowMsg(hand, ShowMessage.HandScanError,
                            e.getMessage());
                    lStar = SomeUtils.isNotFromServiceError(e.getMessage());
                }
            }
        });
        sendCode.start();
    }


    private class EdtBarcodeOnkey implements View.OnKeyListener {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {

            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    String tBarcode = edtBarcode.getText().toString().trim();
                    if (edtBarcode.getText().toString().trim().indexOf("=") != -1||edtBarcode.getText().toString().trim().indexOf("http") != -1) {
                        //包含
                        tBarcode = SomeUtils.InterceptCode(mContext, edtBarcode.getText().toString().trim());
                    }else{
                        tBarcode=SomeUtils.UpdatefirstString(mContext,edtBarcode.getText().toString().trim());
                    }
                    if (edtBarcode.getText().toString().indexOf(" ") != -1) {
                        //包含
                        tBarcode = SomeUtils.AgentCode(mContext, edtBarcode.getText().toString());
                    }
                    tv_show_code.setText(tBarcode);

                    access_send(tBarcode);
                    edtBarcode.setText("");

                }
                return true;
            } else {
                return false;
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

