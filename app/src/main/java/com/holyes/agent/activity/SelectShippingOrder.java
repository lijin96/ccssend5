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
import android.text.Editable;
import android.text.TextWatcher;
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
import com.holyes.ccssend5.select.SelectSureConfirm;
import com.holyes.ccssend5.utils.SomeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: SelectShippingOrder
 * @Description: 选择调货单
 * @Author: lijin
 * @Date: 2021/3/5 17:24
 */
public class SelectShippingOrder extends Activity implements View.OnClickListener {
    private Context mContext;
    private AccessWeb accessWeb;
    private SysUserInfo sysUserInfo;
    private Handler handler;
    private Thread downloadThread;

    private TextView tv_total;
    private ListView shipping_list;
    private EditText et_search;

    private Map<String, Object> item;
    private List<Map<String, Object>> list;
    private List<Map<String, Object>> searchList = new ArrayList<Map<String, Object>>();

    private String lsv_etStr = "", lsv_searchSql = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shippingorder);
        mContext = this;
        accessWeb = new AccessWeb(mContext);
        sysUserInfo = new SysUserInfo(mContext);
        handler = new handShowMsg();

        try {
            SqliteDataHelper.getHelper(getApplicationContext()).execSQL("delete from newtpurcheck");
        } catch (Exception e) {
            e.printStackTrace();
        }

        initView();
    }

    private void initView() {
        tv_total = (TextView) findViewById(R.id.tv_total);
        shipping_list = (ListView) findViewById(R.id.shipping_list);
        et_search = (EditText) findViewById(R.id.et_search);

        try {
            SqliteDataHelper.getHelper(getApplicationContext()).execSQL("delete from newtpurcheck");
        } catch (Exception e) {
            e.printStackTrace();
        }

        list = new ArrayList<Map<String, Object>>();
        shipping_list.setOnItemClickListener(new ListViewItemClik());

        MyProgressDialog.show(this, "正在获取数据...", false, false);

        downloadThread = new Thread(new DownLoadDataThread());
        downloadThread.start();

        et_search.addTextChangedListener(new TextWatcher() {

            @SuppressLint("NewApi")
            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                // TODO Auto-generated method stub
                lsv_etStr = et_search.getText().toString().trim();
                if (lsv_etStr.trim().isEmpty()) {
                    initListView(list);
                    return;
                } else {
                    lsv_searchSql = "select * from newtpurcheck where " +
                            "purchecklno like '%%" + lsv_etStr + "%%' or " +
                            "saplno like '%%" + lsv_etStr + "%%' or " +
                            "supplier_na like '%%" + lsv_etStr + "%%'";
                    searchList.clear();
                    searchList = SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList(lsv_searchSql, null);
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


    private class ListViewItemClik implements AdapterView.OnItemClickListener {


        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {

            ListView listView = (ListView) parent;
            item = (Map<String, Object>) listView.getItemAtPosition(position);
            Intent intent = new Intent(SelectShippingOrder.this, SelectSureConfirm.class);
            intent.putExtra("title", "确定选择调货单：" + item.get("purchecklno")
                    + "的产品吗？");
            startActivityForResult(intent, 0);
        }
    }


    private class DownLoadDataThread implements Runnable {
        @Override
        public void run() {
            try {
                //下载调货单函数
                list = accessWeb.GetDowLoadTransferBill(sysUserInfo.getCompanyid());
                if (list.size() == 0) {
                    ShowMessage.ShowMsg(handler, "暂无数据");
                    return;
                }
                ShowMessage.ShowMsg(handler, ShowMessage.HandSuccess, "success");
            } catch (Exception e) {
                ShowMessage.ShowMsg(handler, "下载出错" + e.getMessage());
            }
        }

    }


    public void initListView(List<Map<String, Object>> mList) {
        Collections.sort(list, new SortListMapComparator("purchecklno"));
        SimpleAdapter adapter = new SimpleAdapter(this, mList, R.layout.item_shippingorder,
                new String[]{"purchecklno", "saplno", "supplier_na"}, new int[]{R.id.item_number, R.id.item_date, R.id.item_cust});
        shipping_list.setAdapter(adapter);
        MyProgressDialog.close();
        tv_total.setText("（共 " + mList.size() + " 条）");
    }

    private class handShowMsg extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ShowMessage.HandShowMessage: // 显示错误提示显示
                    ShowMessage.Show(SelectShippingOrder.this, msg.obj.toString());
                    break;
                case ShowMessage.HandSuccess:
                    List<String> sqlList = new ArrayList<String>();
                    if (list.size() > 0) {
                        String TransferNo, InAgentName, TransferDate, sql;
                        for (Map<String, Object> map : list) {
                            TransferNo = (String) map.get("TransferNo");
                            InAgentName = (String) map.get("InAgentName");
                            TransferDate = (String) map.get("TransferDate");
                            sql = "insert into newtpurcheck(purchecklno,saplno,supplier_na" +
                                    ")values('" + TransferNo + "','" + TransferDate + "','" + InAgentName + "')";
                            sqlList.add(sql);
                        }
                        SqliteDataHelper.getHelper(getApplicationContext()).BatchOperation(sqlList);
                        String listsql = "select * from newtpurcheck";
                        list = SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList(listsql, null);
                        initListView(list);
                    }
                    break;
                default:
                    break;
            }
            MyProgressDialog.close();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnok:
                Intent intent = new Intent();

                intent.putExtra("TransferNo", (String) item.get("purchecklno"));
                intent.putExtra("TransferDate", (String) item.get("saplno"));
                intent.putExtra("InAgentName", (String) item.get("supplier_na"));

                setResult(RESULT_OK, intent);
                this.finish();
                break;
            case R.id.btncancle:
                setResult(RESULT_CANCELED);
                this.finish();
                break;
            default:
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 0) {
                Intent intent = null;
                intent = new Intent(mContext, P_Dv_TransferGoods_D.class);
                intent.putExtra("TransferNo", (String) item.get("purchecklno"));
                intent.putExtra("TransferDate", (String) item.get("saplno"));
                intent.putExtra("InAgentName", (String) item.get("supplier_na"));
                startActivity(intent);
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

