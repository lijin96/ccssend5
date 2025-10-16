package com.holyes.ccssend5.select;

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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;


import com.example.ccssend5.R;
import com.holyes.ccssend5.lib.AccessWeb;
import com.holyes.ccssend5.lib.ShowMessage;
import com.holyes.ccssend5.lib.SysUserInfo;
import com.holyes.ccssend5.myview.MyProgressDialog;
import com.holyes.ccssend5.utils.DisplayUtil;
import com.holyes.ccssend5.utils.SomeUtils;
import com.holyes.headquarter.instock_in.P_Dv_InStock_Lens_Bill;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: SelectPurOrderLensDetail
 * @Description: 获取镜片采购订单明细折射率
 * @Author: lijin
 * @Date: 2024/3/15 14:02
 */
public class SelectPurOrderLensDetail extends Activity {

    private Context mContext;
    private AccessWeb accWeb;
    private Handler hand;
    private SysUserInfo sysUserInfo;

    private ListView listview;
    private EditText et_search;
    private TextView tv_total;
    private TextView txt_monomial;
    private Button btn_seach,btn_ChooseLensGoods;

    private List<Map<String, Object>> list;
    private List<Map<String, Object>> searchList = new ArrayList<Map<String, Object>>();
    private Map<String, Object> item;

    private String  supplier_id = "", supplier_name = "";
    private String goodsid = "", stock_id = "", stock_name, saplno="", sourceBillNo,scanBillno="";

    private String lsv_aim = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayUtil.setDefaultDisplay(this);
        setContentView(R.layout.activity_select_lens_detail);
        lsv_aim = getIntent().getStringExtra("aim");

        accWeb = new AccessWeb(getApplicationContext());
        hand = new handShowMsg();
        mContext = this;
        sysUserInfo=new SysUserInfo(mContext);

        supplier_id = getIntent().getStringExtra("supplier_id");
        supplier_name = getIntent().getStringExtra("supplier_name");
        stock_id = getIntent().getStringExtra("stock_id");
        stock_name = getIntent().getStringExtra("stock_name");
        sourceBillNo = getIntent().getStringExtra("purchecklno");
        saplno = getIntent().getStringExtra("saplno");

        listview = (ListView) findViewById(R.id.listView1);
        tv_total = (TextView) findViewById(R.id.tv_total);
//        txt_monomial = (TextView) findViewById(R.id.txt_1);

        list = new ArrayList<Map<String, Object>>();

        listview.setOnItemClickListener(new ListViewItemClik());
        et_search = (EditText) findViewById(R.id.et_search);

        DownLoadDataThread();

        scanBillno = sysUserInfo.getUserid() + "ZR" + SomeUtils.RandomScanOrder();// 系统

        btn_seach=findViewById(R.id.btn_seach);
        btn_seach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                list.clear();
                DownLoadDataThread();
            }
        });

        btn_ChooseLensGoods=findViewById(R.id.btn_ChooseLensGoods);
        btn_ChooseLensGoods.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(mContext,SelectLensProduct.class);
                intent.putExtra("purchecklno", sourceBillNo);
                intent.putExtra("saplno", saplno);
                intent.putExtra("supplier_name", supplier_name);
                intent.putExtra("stock_name", stock_name);
                intent.putExtra("supplier_id", supplier_id);
                intent.putExtra("stock_id", stock_id);
                intent.putExtra("scanBillno", scanBillno);
                startActivity(intent);
            }
        });

        et_search.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        list.clear();
                        DownLoadDataThread();
                        return true;
                    }
                    return false;
                } else {
                    return false;
                }
            }
        });

//        MyProgressDialog.show(this, "正在获取数据...", false, false);
//        downloadThread = new Thread(new DownLoadDataThread());
//
//        downloadThread.start();
        //输入查询品检单
//        et_search.addTextChangedListener(new TextWatcher() {
//
//            @Override
//            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
//                // TODO Auto-generated method stub
//                lsv_etStr = et_search.getText().toString().trim();
//                if (lsv_etStr.trim().isEmpty()) {
//                    initListView(list);
//                    return;
//                } else {
//                    lsv_searchSql = "select * from newtpurcheck where " +
//                            "purchecklno like '%%" + lsv_etStr + "%%' or " +
//                            "saplno like '%%" + lsv_etStr + "%%' or " +
//                            "supplier_na like '%%" + lsv_etStr + "%%' or " +
//                            "stock_name like '%%" + lsv_etStr + "%%' or " +
//                            "supplier_id like '%%" + lsv_etStr + "%%' or " +
//                            "stock_id like '%%" + lsv_etStr + "%%'";
//                    searchList.clear();
//                    searchList = SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList(lsv_searchSql, null);
//                    initListView(searchList);
//                }
//            }
//
//            @Override
//            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
//                                          int arg3) {
//                // TODO Auto-generated method stub
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable arg0) {
//                // TODO Auto-generated method stub
//
//            }
//        });
    }


    // 请求服务
    private void DownLoadDataThread() {


        MyProgressDialog.show(this, "正在获取数据...", false, false);
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                        list = accWeb.GetDowLoadPurCheckLensDetail(sourceBillNo,et_search.getText().toString().trim());


                    ShowMessage.ShowMsg(hand, ShowMessage.HandSuccess, "success");
                } catch (Exception e) {
                    ShowMessage.ShowMsg(hand, "下载出错" + e.getMessage());
                }
            }
        });
        sendCode.start();
    }


    private class ListViewItemClik implements AdapterView.OnItemClickListener {


        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {

            ListView listView = (ListView) parent;
            item = (Map<String, Object>) listView.getItemAtPosition(position);
//            Intent intent  = new Intent(mContext, CheckLensDegree.class);
            Intent intent  = new Intent(mContext, P_Dv_InStock_Lens_Bill.class);

            intent.putExtra("purchecklno", sourceBillNo);
            intent.putExtra("saplno", saplno);
            intent.putExtra("supplier_name", supplier_name);
            intent.putExtra("stock_name", stock_name);
            intent.putExtra("supplier_id", supplier_id);
            intent.putExtra("stock_id", stock_id);
            intent.putExtra("Product_id", (String) item.get("Product_id"));
//            intent.putExtra("GoodsCode", (String) item.get("Product_id"));
            intent.putExtra("scanBillno", scanBillno);

            intent.putExtra("refractive", (String) item.get("RefractiveIndex"));

            intent.putExtra("aim", lsv_aim);
            startActivity(intent);
        }
    }

//    private class DownLoadDataThread implements Runnable {
//        @Override
//        public void run() {
//            if (lsv_aim == null || lsv_aim.isEmpty()) {
//                return;
//            }
//            try {
//                //有单入库
//                if ("P_Dv_InStock_Bill".equals(lsv_aim)) {
//                    txt_monomial.setText("品检验收单");
//                    list = accWeb.GetDowLoadPurCheckBill();
//                }
//                //有单入库退回,有单入库退回撤销
//                else if (lsv_aim.equals("P_Dv_ReturnedPurchase_Z_G_Bill")) {
//                    txt_monomial.setText("品检退货单");
//                    list = accWeb.GetDowLoadPurOutBill();
//                }
//                ShowMessage.ShowMsg(hand, ShowMessage.HandSuccess, "success");
//            } catch (Exception e) {
//                ShowMessage.ShowMsg(hand, "下载出错" + e.getMessage());
//            }
//        }
//
//    }


    public void initListView(List<Map<String, Object>> mList) {
//        Collections.sort(mList, new SortListMapComparator("PurOrderNo"));

        if (mList.size()==0){
            ShowMessage.Show(mContext,"暂无数据");
        }

        SimpleAdapter adapter = new SimpleAdapter(this, mList, R.layout.list_purcheck_lens_detail,
                new String[]{"Product_id", "productdescription", "RefractiveIndex", "NoInGoodQty"}, new int[]{R.id.txt_list1, R.id.txt_list2, R.id.txt_refractivity, R.id.txt_list4});
        listview.setAdapter(adapter);
//        MyProgressDialog.close();
        tv_total.setText("（共 " + mList.size() + " 条）");
    }

    private class handShowMsg extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ShowMessage.HandShowMessage: // 显示错误提示显示
                    ShowMessage.Show(mContext, msg.obj.toString());
                    break;
                case ShowMessage.HandSuccess:

                    initListView(list);

                    break;
                default:
                    break;
            }
            MyProgressDialog.close();
        }
    }


//    @Override
//    public void onClick(View v) {
//
//        switch (v.getId()) {
//            case R.id.btnok:
//                Intent intent = new Intent();
//
//                intent.putExtra("purchecklno", (String) item.get("purchecklno"));
//                intent.putExtra("saplno", (String) item.get("saplno"));
//                intent.putExtra("supplier_name", (String) item.get("supplier_na"));
//                intent.putExtra("stock_name", (String) item.get("stock_name"));
//                intent.putExtra("supplier_id", (String) item.get("supplier_id"));
//                intent.putExtra("stock_id", (String) item.get("stock_id"));
//
//                setResult(RESULT_OK, intent);
//                this.finish();
//                break;
//            case R.id.btncancle:
//                setResult(RESULT_CANCELED);
//                this.finish();
//                break;
//            default:
//                break;
//        }
//    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (lsv_aim == null || lsv_aim.isEmpty()) {
                return;
            }
            if (requestCode == 0) {
                Intent intent = null;
//                有单入库
//                if ("P_Dv_InStock_Bill".equals(lsv_aim)) {
//                    //如果入库单的仓库id不存在就需要先去选择仓库
//                    if (item.get("StockId")== null || item.get("StockId").equals("")){
//                        intent = new Intent(mContext, SelectStock.class);
//                    }else{
                        intent = new Intent(mContext, CheckLensDegree.class);
//                intent = new Intent(mContext, P_Dv_InStock_Lens_Bill.class);
//                    }
//
//                }
//                //有单入库撤销
//                else if ("P_Dv_InStock_Cancel_Bill".equals(lsv_aim)) {
//                    intent = new Intent(SelectPurcheck.this, P_Dv_InStock_Bill_Cancel.class);
//                }
//                //有单入库退回
//                else if (lsv_aim.equals("P_Dv_ReturnedPurchase_Z_G_Bill")) {
//                    intent = new Intent(SelectPurcheck.this, P_Dv_ReturnedPurchase_Z_G_Bill.class);
//                }
//                //入库退回撤销（有单）
//                else if (lsv_aim.equals("P_Dv_ReturnedPurchase_Z_G_Cancel_Bill")) {
//                    intent = new Intent(SelectPurcheck.this, P_Dv_ReturnedPurchase_Z_G_Bill_Cancel.class);
//                }

                intent.putExtra("purchecklno", (String) item.get("PurOrderNo"));
                intent.putExtra("saplno", (String) item.get("SapLno"));
                intent.putExtra("supplier_name", (String) item.get("SupplierName"));
                intent.putExtra("stock_name", (String) item.get("StockName"));
                intent.putExtra("supplier_id", (String) item.get("SupplierId"));
                intent.putExtra("stock_id", (String) item.get("StockId"));
                intent.putExtra("aim", lsv_aim);
                intent.putExtra("scanBillno", scanBillno);

                startActivity(intent);
            }

        }


    }

    @Override       //这里是实现了自动更新
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
//        Page=1;
//        list=new ArrayList<Map<String, Object>>();
//        DownLoadDataThread();
        list.clear();
        DownLoadDataThread();
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