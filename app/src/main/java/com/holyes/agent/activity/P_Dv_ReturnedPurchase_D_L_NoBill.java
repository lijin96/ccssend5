package com.holyes.agent.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.example.ccssend5.R;
import com.holyes.ccssend5.dao.ScanDataDao;
import com.holyes.ccssend5.lib.AccessWeb;
import com.holyes.ccssend5.lib.MySound;
import com.holyes.ccssend5.lib.ShowMessage;
import com.holyes.ccssend5.lib.SqliteDataHelper;
import com.holyes.ccssend5.lib.SysUserInfo;
import com.holyes.ccssend5.myview.MyProgressDialog;
import com.holyes.ccssend5.select.QueryScanDetail;
import com.holyes.ccssend5.utils.PrintUtil;
import com.holyes.ccssend5.utils.SomeUtils;
import com.holyes.headquarter.activity.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @ClassName: P_Dv_ReturnedPurchase_D_L_NoBill
 * @Description: 零售退货
 * @Author: lijin
 * @Date: 2021/3/5 17:23
 */
public class P_Dv_ReturnedPurchase_D_L_NoBill extends Activity {
    @SuppressWarnings("unused")
    private Context mContext;
    private Handler hand;
    private SysUserInfo sysUserInfo;
    private PrintUtil printbill;
    private MySound sound;

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

    private List<Map<String, Object>> slist = new ArrayList<Map<String, Object>>();

    private String nScanCount = "0";//合计
    private int nSize = 0;//次数

    private AlertDialog alertDialog1; //选择客户打印框


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
        setContentView(R.layout.p_dv_returnedpurchase_d_l_nobill);
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
        @RequiresApi(api = Build.VERSION_CODES.N)
        @SuppressWarnings("unchecked")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ShowMessage.HandShowMessage: // 显示错误提示显示
                    ShowMessage.Show(P_Dv_ReturnedPurchase_D_L_NoBill.this, msg.obj.toString());
                    break;
                case ShowMessage.HandScanSuccess:
                    MySound.scanSound();
                    //MyProgressDialog.close();
                    //				addToList();

                    tv_model_colors.setText(modelm + "-" + colors);
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
                    ShowMessage.Show(P_Dv_ReturnedPurchase_D_L_NoBill.this, msg.obj.toString());
                    break;
                case 3:
                    //MyProgressDialog.close();
                    ShowMessage.Show(P_Dv_ReturnedPurchase_D_L_NoBill.this, "网络不给力，请稍后再试！");
                    break;
                case 4:
                    //MyProgressDialog.close();
                    ShowMessage.Show(P_Dv_ReturnedPurchase_D_L_NoBill.this, msg.obj.toString());
                    break;
                case 8:
                    MyProgressDialog.close();

//
//                    String[] mark = new String[2];
//                    mark[0] = "退货单：" + mBillNo;
//                    mark[1] = "零售商：" + tv_company_na.getText().toString();
//
//                    printbill.print(P_Dv_ReturnedPurchase_D_L_NoBill.this, "           零售退货", mark, slist, sysUserInfo.getUserid());
                    if (sysUserInfo.getEnterpriseId().equals("62")){
                        //佰莱德单独判断退货打印的功能
                        String[] mark = new String[2];
                        mark[0] = "退货单：" + mBillNo;
                        mark[1] = "零售商：" + tv_company_na.getText().toString();
                        printbill.print(P_Dv_ReturnedPurchase_D_L_NoBill.this, "零售退货", mark, slist, sysUserInfo.getUserid());
                    }else {
                        //其他品牌继续多客户退货打印功能
                        GroupPrintingMap(slist);
                    }

                    break;
                case 9:
                    MyProgressDialog.close();
                    ShowMessage.Show(P_Dv_ReturnedPurchase_D_L_NoBill.this, msg.obj.toString());
                    break;
                default:
                    break;
            }
            //			//MyProgressDialog.close();
            super.handleMessage(msg);
        }
    }

    //选择要打印哪一个客户的小票
    public void showCustList(List<List<Map<String, Object>>> CustDataList){
//        String[] items=new String[CustDataList.size()];
        ArrayList<String> CustNameList=new ArrayList<>();
        for (int i = 0; i < CustDataList.size(); i++){
            CustNameList.add(CustDataList.get(i).get(0).get("custname").toString());
        }
        String[]  items=CustNameList.toArray(new String[CustNameList.size()]);
//        final String[] items = {"列表1", "列表2", "列表3", "列表4"};
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_LIGHT);
        alertBuilder.setTitle("请选择要打印的客户小票");
        alertBuilder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
//                Log.d("main", CustDataList.get(i).toString());
//                Toast.makeText(mContext, items[i], Toast.LENGTH_SHORT).show();
                printbill.returnprint(P_Dv_ReturnedPurchase_D_L_NoBill.this, "零售退货",  CustDataList.get(i), sysUserInfo.getUserid());
                alertDialog1.dismiss();
            }
        });
        alertDialog1 = alertBuilder.create();
        alertDialog1.show();
    }

    private void GroupPrintingMap(List<Map<String, Object>> dataList){
//      / 按 CustId 分组（兼容低版本Android）
        Map<String, List<Map<String, Object>>> groupedData = new HashMap<>();
        for (Map<String, Object> item : dataList) {
            // 获取分组键（CustId）
            String custId = (String) item.get("custid");
            // 检查是否已存在该分组
            if (!groupedData.containsKey(custId)) {
                groupedData.put(custId, new ArrayList<Map<String, Object>>());
            }
            // 将当前项添加到对应分组
            groupedData.get(custId).add(item);
        }

        // 获取分组结果（每个分组一个List）
        List<List<Map<String, Object>>> dataresult = new ArrayList<>(groupedData.values());
        if (dataresult.size()>1){
            showCustList(dataresult);
        }else{
            printbill.returnprint(P_Dv_ReturnedPurchase_D_L_NoBill.this, "零售退货",  dataresult.get(0), sysUserInfo.getUserid());
        }
    }



    // {{明细
    private class BtnListClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            List<Map<String, Object>> list = SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList(
                    "select modelm,colors,sum(curcount) as curcount from newscandate GROUP BY goodsid",
                    null);
            Intent intent = new Intent(P_Dv_ReturnedPurchase_D_L_NoBill.this,
                    QueryScanDetail.class);
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
            if (SomeUtils.isDoubleClick(P_Dv_ReturnedPurchase_D_L_NoBill.this, true)) {
                finish();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:

                if (SomeUtils.isDoubleClick(P_Dv_ReturnedPurchase_D_L_NoBill.this, true)) {
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

                    result = AccessWeb.getHelper(getApplicationContext()).P_Dv_ReturnedPurchase_D_L_NoBill(contents, socompany_id,
                            socompany_id, String.valueOf(nSize), Scanbillno,
                            mBillNo);
                    if (result.isEmpty()) {
                        MySound.errorSound();
                        ShowMessage.ShowMsg(hand, 3, "");
                        return;
                    }
//                    Log.d("main","零售退货参数"+result.toString());
                    //代理商发货日期，零售商代号，零售商名称，产品编号,型号,色号,当前型号数量,发货单号
                    //2018-02-06,CS0011907550001,深圳零售测试1,a123C1,a123,C1,2,LT-CS001-18000010
                    String rest[] = result.split(",",-1);
                    nSize++;
//                    if (mBillNo.isEmpty()) {
                    mBillNo = rest[7];
//                    }
                    company_id = rest[1].trim();
                    company_na = rest[2].trim();
                    product_id = rest[3].trim();
                    modelm = rest[4].trim();
                    colors = rest[5].trim();
                    curcount = rest[6].trim();

                    lastSuccessBarcode = contents;

                    if (rest.length > 8) {
                        if (Integer.parseInt(nScanCount) < Integer.parseInt(rest[8].trim())) {
                            nScanCount = rest[8].trim();
                        }
                        ShowMessage.ShowMsg(hand, ShowMessage.HandScanSuccess, "");
                    } else {
                        ShowMessage.ShowMsg(hand, ShowMessage.HandSuccess, "");
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

