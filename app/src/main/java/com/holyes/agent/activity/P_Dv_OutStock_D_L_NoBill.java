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
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: P_Dv_OutStock_D_L_NoBill
 * @Description: 代理商发货给零售商
 * @Author: lijin
 * @Date: 2021/3/5 17:23
 */
public class P_Dv_OutStock_D_L_NoBill extends Activity {
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
    private Spinner mSpinner;
    private TextView tv_show_code;

    private String Scanbillno = "";
    private String curcount = "0", product_id = "", modelm = "", colors = "";
    private String mBillNo = "";
    private String company_id = "";
    private String company_na = "";
    private String socompany_id = "";
    private String contents = "";
    private String lastSuccessBarcode = "", lStar = "";

    private List<Map<String, Object>> slist = new ArrayList<Map<String, Object>>();

    private String nScanCount = "0";//合计
    private int nSize = 0;//次数

    private String Delivery_type = "0";//发货类型

//	private List<String> codesList= new ArrayList<String>();
//
//	Thread send ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.p_dv_outstock_d_l_nobill);
        mContext = this;
        hand = new handShowMsg();
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        sysUserInfo = new SysUserInfo(getApplicationContext());
        Scanbillno = sysUserInfo.getUserid() + "1F" + SomeUtils.RandomScanOrder();
//        Log.d("main---",Scanbillno);
        // 系统
        Intent gIntent = this.getIntent();

        mSpinner = (Spinner) findViewById(R.id.spinner_type);

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

        company_na = gIntent.getStringExtra("company_na");
        company_id = gIntent.getStringExtra("company_id");
        socompany_id = sysUserInfo.getCompanyid();

        tv_company_na.setText(company_na);
        tv_model_colors.setText("");
        tv_totalqty.setText("0");
        tv_curqty.setText("0");
        tvBillno.setText("");
        printbill = new PrintUtil();

        try {
            SqliteDataHelper.getHelper(getApplicationContext()).execSQL("delete from newscandate");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (sysUserInfo.getFirstStart().equals("01") ||
                sysUserInfo.getFirstStart().equals("11")) {
            mSpinner.setVisibility(View.VISIBLE);
        }
        String[] arr = {"补货", "首发"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.myspinner, arr);
        mSpinner.setAdapter(adapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //选择列表项的操作
                String type = (String) mSpinner.getItemAtPosition(position);//从spinner中获取被选择的数据
                if (type.equals("首发")) {
                    Delivery_type = "1";
                } else if (type.equals("补货")) {
                    Delivery_type = "0";
                }

                TextView tv = (TextView) view;
                tv.setTextSize(14.0f);    //设置大小
                tv.setGravity(android.view.Gravity.CENTER_HORIZONTAL);   //设置居中
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //未选中时候的操作
            }
        });
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
                    ShowMessage.Show(P_Dv_OutStock_D_L_NoBill.this, msg.obj.toString());
                    break;
                case ShowMessage.HandScanSuccess:
                    MySound.scanSound();
                    tv_model_colors.setText(modelm + "-" + colors);
                    tv_curqty.setText(curcount);
                    tv_totalqty.setText(nScanCount);

                    if (tvBillno != null) {
                        tvBillno.setText(mBillNo);
                    }
                    tv_product_id.setText("(" + product_id + ")");
                    break;
                case ShowMessage.HandSuccess:
                    MySound.scanSound();
                    try {
                        ScanDataDao.updateDataAndUi(mContext, tv_model_colors, tv_curqty, tv_totalqty, tvBillno,tv_product_id, curcount, product_id, modelm, colors, mBillNo);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    tv_product_id.setText("(" + product_id + ")");
                    break;

                case ShowMessage.HandScanError:
                    MySound.errorSound();
                    //MyProgressDialog.close();
                    ShowMessage.Show(P_Dv_OutStock_D_L_NoBill.this, msg.obj.toString());
                    break;
                case 3:
                    //MyProgressDialog.close();
                    ShowMessage.Show(P_Dv_OutStock_D_L_NoBill.this, "网络不给力，请稍后再试！");
                    break;
                case 4:
                    //MyProgressDialog.close();
                    ShowMessage.Show(P_Dv_OutStock_D_L_NoBill.this, msg.obj.toString());
                    break;
                case 8:
                    MyProgressDialog.close();

                    String[] mark = new String[2];
                    mark[0] = "发货单：" + mBillNo;
                    mark[1] = "零售商：" + tv_company_na.getText().toString();

                    printbill.print(P_Dv_OutStock_D_L_NoBill.this, "           零售发货", mark, slist, sysUserInfo.getUserid());
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


    // {{明细
    private class BtnListClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            List<Map<String, Object>> sacnDataList = SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList(
                    "select modelm,colors,sum(curcount) as curcount from newscandate GROUP BY goodsid",
                    null);
            Intent intent = new Intent(P_Dv_OutStock_D_L_NoBill.this,
                    QueryScanDetail.class);
            if (sacnDataList.size() == 0) {
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

                        List<Map<String, Object>> sacnDataList = SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList(
                                "select modelm,colors,sum(curcount) as curcount from newscandate GROUP BY goodsid",
                                null);
                        if (sacnDataList.size() == 0) {
                            slist = AccessWeb.getHelper(mContext).GetDowLoadBilldetail(sysUserInfo.getLoginid(), Scanbillno);
                        } else {
                            slist = SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList(
                                    "select modelm,colors,sum(curcount) as curcount from newscandate GROUP BY goodsid",
                                    null);
                        }
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
            if (SomeUtils.isDoubleClick(P_Dv_OutStock_D_L_NoBill.this, true)) {
                finish();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:

                if (SomeUtils.isDoubleClick(P_Dv_OutStock_D_L_NoBill.this, true)) {
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
//				Log.d("main","thread end"+e.getMessage());
//			}
//
//		}
//	}

    // {{自定义函数
    @SuppressLint("NewApi")
    private void access_send(final String contents) {
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {
                String result = "";
                try {
                    AccessWeb.getHelper(getApplicationContext()).mWebId = lStar + contents;
                    //！！！注意，这个服务和其他的调的参数有差异

                    if (sysUserInfo.getFirstStart().equals("01") ||
                            sysUserInfo.getFirstStart().equals("11")) {
                        //Barcode：扫描的条码(必传)
                        //SoCompId：代理商代号(必传)
                        //DeCompId：零售商代号(必传)
                        //OaSuserId：扫描人员代号(必传)
                        //ScanSn：扫描序号(必传)
                        //ScanBillNo：扫描单号(必传)
                        //BillNo：发货单号(首次扫码传空，成功再扫码时传返回的发货单号)
                        //FirstDelivery：是否首次发货(0-非首次；1-首次)
                        para.setBarcode(contents);
                        para.setSoCompId(socompany_id);
                        para.setDeCompId(company_id);
                        para.setOaSuserId(sysUserInfo.getUserid());
                        para.setScanSn(String.valueOf(nSize));
                        para.setScanBillNo(Scanbillno);
                        para.setBillNo(mBillNo);
                        para.setFirstDelivery(Delivery_type);
                        result = AccessWeb.getHelper(getApplicationContext()).P_Dv_OutStock_D_L_NoBillv1(para.toJson());
                    } else {
                        result = AccessWeb.getHelper(getApplicationContext()).P_Dv_OutStock_D_L_NoBill(contents, socompany_id,
                                company_id, String.valueOf(nSize), Scanbillno,
                                mBillNo);
                    }
                    //返回结果为空
                    if (result.isEmpty()) {
                        MySound.errorSound();
                        ShowMessage.ShowMsg(hand, 3, "");
                        return;
                    }
                    nSize++;
                    if (contents.startsWith("P")) {

                        JSONArray listjson = new JSONArray(result);


                        //                "GoodsId": "C00001",
                        //                "Modelm": "1357",
                        //                "Colors": "C01",
                        //                "CurNum": "80",
                        //                "PackNumber": "P200917000001",
                        //                "TranLno" :"DX-00-200000001"

                        for (int i = 0; i < listjson.length(); i++) {
                            JSONObject jsonObject1 = (JSONObject) listjson.opt(i);

                            product_id = jsonObject1.getString("GoodsId");
                            modelm = jsonObject1.getString("Modelm");
                            colors = jsonObject1.getString("Colors");
                            curcount = jsonObject1.getString("CurNum");
                            //					lastSuccessBarcode = rest[4].trim();
                            if (mBillNo == null || mBillNo.isEmpty()) {
                                mBillNo = jsonObject1.getString("TranLno");
                            }

                            nScanCount = String.valueOf(Integer.parseInt(nScanCount) + Integer.parseInt(jsonObject1.optString("CurNum","0")));
                        }
                        ShowMessage.ShowMsg(hand, ShowMessage.HandScanSuccess, "");
                    } else {
                        //CS170001,1.50非球面,+1.75+0.50,1,6222323912072181,DF-CS001-17000001
                        //产品编号,型号,色号,当前型号数量,当前扫描的条码,发货单号
                        String rest[] = result.split(",",-1);

                        if (rest.length < 6) {
                            MySound.errorSound();
                            ShowMessage.ShowMsg(hand, "sendgood参数个数不够，当前"
                                    + rest.length + "位！");

                            return;
                        }

                        if (mBillNo.isEmpty()) {
                            mBillNo = rest[5];
                        }

                        product_id = rest[0].trim();
                        modelm = rest[1].trim();
                        colors = rest[2].trim();
                        curcount = rest[3].trim();
                        lastSuccessBarcode = contents;

                        if (rest.length > 6) {
                            if (Integer.parseInt(nScanCount) < Integer.parseInt(rest[6].trim())) {
                                nScanCount = rest[6].trim();
                            }
                            //第五代返回结果处理
                            ShowMessage.ShowMsg(hand, ShowMessage.HandScanSuccess, "");
                        } else {
                            //第四代返回结果处理
                            ShowMessage.ShowMsg(hand, ShowMessage.HandSuccess, "");
                        }
                    }
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
                    }else {
                        tBarcode= SomeUtils.UpdatefirstString(mContext,edtBarcode.getText().toString().trim());
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

