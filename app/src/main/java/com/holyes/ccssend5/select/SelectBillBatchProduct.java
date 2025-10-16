package com.holyes.ccssend5.select;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.ccssend5.R;
import com.holyes.ccssend5.lib.AccessWeb;
import com.holyes.ccssend5.lib.ShowMessage;
import com.holyes.ccssend5.lib.SortListMapComparator;
import com.holyes.ccssend5.lib.SqliteDataHelper;
import com.holyes.ccssend5.lib.SysUserInfo;
import com.holyes.ccssend5.myview.MyProgressDialog;
import com.holyes.ccssend5.utils.DisplayUtil;
import com.holyes.ccssend5.utils.SomeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: SelectBillProduct
 * @Description: 型号色号选择网络数据(验收单明细)金大合需求显示批号
 * @Author: lijin
 * @Date: 2024年3月21日
 */
public class SelectBillBatchProduct extends Activity {

    private Handler hand;
    private AccessWeb accWeb;
    private Thread downloadDetail;
    private SysUserInfo sysUserInfo;

    private EditText et_search;
    private TextView tv_title, tv_total;
    private ListView listview;

    Map<String, Object> item;
    private List<Map<String, Object>> dataList, searchList;

    private String lsv_etStr, lsv_searchSql, lsv_type, orderno, lsv_aim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayUtil.setDefaultDisplay(this);
        setContentView(R.layout.new_select_bill_batch_product);
        sysUserInfo = new SysUserInfo(this);
        listview = (ListView) findViewById(R.id.listView1);
        et_search = (EditText) findViewById(R.id.et_search);
        tv_total = (TextView) findViewById(R.id.tv_total);
        tv_title = (TextView) findViewById(R.id.tv_title);

        listview.setOnItemClickListener(new listViewClick());

        accWeb = new AccessWeb(getApplicationContext());
        hand = new handShowMsg();
        lsv_type = getIntent().getStringExtra("type");
        orderno = getIntent().getStringExtra("orderno");
        lsv_aim = getIntent().getStringExtra("aim");

        if (lsv_aim != null && !lsv_aim.isEmpty()) {
            if ("P_Dv_OutStock_Z_L_Bill_BeInStock".equals(lsv_aim) ||
                    "P_Dv_OutStock_Z_D_Bill_BeInStock".equals(lsv_aim)||
                    "P_Dv_InStock_Z_ChangeStock_Bill".equals(lsv_aim)||
                    "P_Dv_OutStock_Z_D_L_Bill_BeInStock".equals(lsv_aim)) {
                tv_title.setText("查看单明细");
            }

        }


        dataList = new ArrayList<Map<String, Object>>();
        searchList = new ArrayList<Map<String, Object>>();

        if (sysUserInfo.getIsDownload()) {
            try {
                SqliteDataHelper.getHelper(getApplicationContext()).execSQL("delete from newtpeinomx");
            } catch (Exception e) {
                e.printStackTrace();
            }
            dataList.clear();
            MyProgressDialog.show(this, "正在下载单据明细……", true, false);
            downloadDetail = new Thread(new DownloadPeiDetailRunnable());
            downloadDetail.start();
//            sysUserInfo.setIsDownload(false);
        } else {
            dataList.addAll(SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList("select * from newtpeinomx", null));
            initListView(dataList);
        }
        et_search.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                // TODO Auto-generated method stub
                lsv_etStr = et_search.getText().toString().trim();
                if (lsv_etStr.trim().isEmpty()) {
                    initListView(dataList);
                    return;
                } else {
                    lsv_searchSql = "select * from newtpeinomx where " +
                            "goodsid like '%%" + lsv_etStr + "%%' or " +
                            "colors like '%%" + lsv_etStr + "%%' or " +
                            "modelm like '%%" + lsv_etStr + "%%' or " +
                            "batchno like '%%" + lsv_etStr + "%%'";
                    searchList.clear();
                    searchList.addAll(SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList(lsv_searchSql, null));
                    initListView(searchList);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub

            }
        });

    }

    private class listViewClick implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {

            ListView listView = (ListView) parent;

            item = (Map<String, Object>) listView.getItemAtPosition(position);
            Intent intent = new Intent();
            intent.putExtra("goodsid", (String) item.get("goodsid"));
            intent.putExtra("modelm", (String) item.get("modelm"));
            intent.putExtra("colors", (String) item.get("colors"));
            intent.putExtra("noscanqty", (String) item.get("noscanqty"));//数量
            intent.putExtra("amount", (String) item.get("amount"));//数量
            intent.putExtra("batchno", (String) item.get("batchno"));//批号
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK, null);
        finish();
    }

    private class DownloadPeiDetailRunnable implements Runnable {

        @Override
        public void run() {
            try {


                if (orderno == null || orderno.equalsIgnoreCase("")) {
                    ShowMessage.ShowMsg(hand, "单据为空");
                    return;
                }

                //品检单明细下载
                if (lsv_aim.equals("P_Dv_InStock_Bill")) {
                    dataList = accWeb.GetDowLoadPurCheckDetail(orderno);
                }
                //入库退回明细下载
                else if (lsv_aim.equals("P_Dv_ReturnedPurchase_Z_G_Bill")) {
                    dataList = accWeb.GetDowLoadPurOutDetail(orderno);
                }
                //配货单（代销）明细下载
                else if (lsv_aim.equals("P_Dv_OutStock_Z_D_Bill_BeInStock")||(lsv_aim.equals("P_Dv_OutStock_Z_D_L_Bill_BeInStock"))) {
                    dataList = accWeb.GetDowLoadAgentInvoiceDetail(orderno);
                }
                //配货单（直销分店）明细下载
                else if (lsv_aim.equals("P_Dv_OutStock_Z_L_S_HaveBill_BeInStock")) {
                    dataList = accWeb.GetDowLoadAgentInvoiceDetail(orderno);
                }

                //配货单（直销）明细下载
                else if (lsv_aim.equals("P_Dv_OutStock_Z_L_Bill_BeInStock")) {
                    dataList = accWeb.GetDowLoadDirectInvoiceDetail(orderno);
                }
                //仓库调拨下载明细
                else if (lsv_aim.equals("P_Dv_InStock_Z_ChangeStock_Bill")) {
                    dataList = accWeb.GetDowLoadAllotsdetail(orderno);
                }
                if (dataList.size() == 0) {
                    ShowMessage.ShowMsg(hand, "当前没有数据下载");
                    return;
                }
//                Log.d("main11",dataList.toString());
                ShowMessage.ShowMsg(hand, ShowMessage.HandSuccess, "success");
            } catch (Exception e) {
                ShowMessage.ShowMsg(hand, "下载出错" + e.getMessage());
            }
        }
    }


    private class handShowMsg extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ShowMessage.HandShowMessage: // 显示错误提示显示
                    ShowMessage.Show(SelectBillBatchProduct.this, msg.obj.toString());
                    break;
                case ShowMessage.HandSuccess:
                    initListView(dataList);
                    List<String> sqlList = new ArrayList<String>();
                    if (dataList.size() > 0) {
                        String goodsid, goodsdescription, colors, modelm, noscanqty, amount, sql,batchno;
                        for (Map<String, Object> map : dataList) {
                            goodsid = (String) map.get("goodsid");
                            goodsdescription = (String) map.get("goodsdescription");
                            modelm = (String) map.get("modelm");
                            colors = (String) map.get("colors");
                            noscanqty = (String) map.get("noscanqty");
                            amount = noscanqty;
                            batchno= (String) map.get("batchno");

                            sql = "insert into newtpeinomx(goodsid,goodsdescription,modelm,colors,noscanqty,amount,batchno)" +
                                    " values('" + goodsid + "','" + goodsdescription + "','" + modelm + "','" + colors + "','" + noscanqty + "','" + amount +"','" + batchno +  "')";
                            sqlList.add(sql);
                        }
                        SqliteDataHelper.getHelper(getApplicationContext()).BatchOperation(sqlList);
                    }
                    //		listview.refreshDrawableState();
                    break;
                default:
                    MyProgressDialog.close();
                    // ShowMessage.Show(Xundian.this,msg.obj.toString());
                    break;
            }

            MyProgressDialog.close();
        }

    }

    public void initListView(List<Map<String, Object>> list) {
        Collections.sort(list, new SortListMapComparator("goodsid"));
        SimpleAdapter adapter = new SimpleAdapter(SelectBillBatchProduct.this, list,
                R.layout.list_purcheck_batch_detail, new String[]{"goodsid", "modelm","batchno","colors", "noscanqty"},
                new int[]{R.id.txt_list1, R.id.txt_list2,R.id.txt_batch, R.id.txt_list3, R.id.txt_list4});
        listview.setAdapter(adapter);
        MyProgressDialog.close();
        tv_total.setText("（共" + list.size() + "条）");
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

