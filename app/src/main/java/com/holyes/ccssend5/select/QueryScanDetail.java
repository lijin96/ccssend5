package com.holyes.ccssend5.select;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
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
 * @ClassName: QueryScanDetail
 * @Description: 扫描明细查看
 * @Author: lijin
 * @Date: 2021/3/10 9:55
 */
public class QueryScanDetail extends Activity {
    AccessWeb accWeb;
    private Thread getDateThread;
    private Handler hand;
    private SysUserInfo sysUserInfo;

    private ListView listview;
    private EditText et_search;
    private TextView tv_total;

    List<Map<String, Object>> slist, searchList;

    private String lsv_searchSql = "", lsv_etStr = "";

    private String mBillNo = "";


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayUtil.setDefaultDisplay(this);
        setContentView(R.layout.new_query_scan_detail);
        hand = new handShowMsg();
        accWeb = new AccessWeb(this);
        sysUserInfo = new SysUserInfo(this);
        mBillNo = getIntent().getStringExtra("mBillNo");
        listview = (ListView) findViewById(R.id.listView1);

        tv_total = (TextView) findViewById(R.id.tv_total);
//		et_search=(EditText) findViewById(R.id.et_search);
//		et_search.addTextChangedListener(new EditTextChnangeListener());
        slist = new ArrayList<Map<String, Object>>();
        searchList = new ArrayList<Map<String, Object>>();
        MyProgressDialog.show(QueryScanDetail.this, "", true, true);
        getDateThread = new Thread(new GetDateRunnable());
        getDateThread.start();
    }

    private class GetDateRunnable implements Runnable {
        @Override
        public void run() {
            try {

                if (mBillNo == null || mBillNo.isEmpty()) {
                    slist = SqliteDataHelper.getHelper(getApplicationContext())
                            .QueryDbList(
                                    "select goodsid,modelm,colors,sum(curcount) as curcount from newscandate GROUP BY goodsid",
                                    null);
                } else {
                    slist = accWeb.GetDowLoadBilldetail(sysUserInfo.getLoginid(), mBillNo);
                }

                if (slist.size() == 0) {
                    ShowMessage.ShowMsg(hand,
                            "当前没有扫描数据");
                    MyProgressDialog.close();
                    return;
                }
//                Log.d("main",slist.toString());
                ShowMessage.ShowMsg(hand, ShowMessage.HandScanSuccess, "success");
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
                    MyProgressDialog.close();
                    ShowMessage.Show(QueryScanDetail.this, msg.obj.toString());
                    break;
                case ShowMessage.HandScanSuccess:
                    MyProgressDialog.close();
                    initListView(slist);

                    break;
                default:
                    break;
            }
        }
    }

    /**
     * @param mList
     */
    private void initListView(List<Map<String, Object>> mList) {
        //把集合数据先排序
        Collections.sort(mList, new SortListMapComparator("modelm"));

        SimpleAdapter adapter = new SimpleAdapter(QueryScanDetail.this, mList,
                R.layout.list_purcheck_detail, new String[]{"goodsid", "modelm", "colors", "curcount"},

                new int[]{R.id.txt_list1, R.id.txt_list2, R.id.txt_list3, R.id.txt_list4});
        listview.setAdapter(adapter);
        tv_total.setText("（共 " + mList.size() + " 条）");
        MyProgressDialog.close();
    }

    class EditTextChnangeListener implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            lsv_etStr = et_search.getText().toString().trim();
            lsv_searchSql = "select goodsid,modelm,colors,sum(curcount) as curcount from newscandate where " +
                    "goodsid like '%%" + lsv_etStr + "%%' or " +
                    "modelm like '%%" + lsv_etStr + "%%' or " +
                    "colors like '%%" + lsv_etStr + "%%' GROUP BY goodsid";
            searchList.clear();
            searchList = SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList(lsv_searchSql, null);
            initListView(searchList);
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

