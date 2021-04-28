package com.holyes.ccssend5.select;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.ccssend5.R;
import com.holyes.ccssend5.dao.PackingScanDao;
import com.holyes.ccssend5.lib.AccessWeb;
import com.holyes.ccssend5.lib.ShowMessage;
import com.holyes.ccssend5.lib.SortListMapComparator;
import com.holyes.ccssend5.myview.MyProgressDialog;
import com.holyes.ccssend5.utils.DisplayUtil;
import com.holyes.ccssend5.utils.SomeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: SelectPackingScanDetail1
 * @Description: 装盒入库扫描明细查看
 * @Author: lijin
 * @Date: 2021/3/10 9:58
 */
public class SelectPackingScanDetail1 extends Activity {

    private ListView listview;
    AccessWeb accWeb;
    private Thread getDateThread;
    private Handler hand;
    List<Map<String, Object>> slist, searchList;
    private EditText et_search;
    private Context mContext;
    private String lsv_searchSql = "", lsv_etStr = "";
    private TextView tv_total;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayUtil.setDefaultDisplay(this);
        setContentView(R.layout.activity_packing_model_detail);
        hand = new handShowMsg();
        mContext = this;
        listview = (ListView) findViewById(R.id.listView1);

        tv_total = (TextView) findViewById(R.id.tv_total);
        et_search = (EditText) findViewById(R.id.et_search);
        slist = new ArrayList<Map<String, Object>>();
        searchList = new ArrayList<Map<String, Object>>();
        getDateThread = new Thread(new GetDateRunnable());
        getDateThread.start();
        et_search.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

                lsv_etStr = et_search.getText().toString().trim();
                searchList = PackingScanDao.getPackModelColorDetail(mContext, lsv_etStr);
                initListView(searchList);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {


            }

            @Override
            public void afterTextChanged(Editable arg0) {


            }
        });
    }

    private class GetDateRunnable implements Runnable {
        @Override
        public void run() {
            try {
                slist = PackingScanDao.getPackModelColorDetail(mContext, "");
                if (slist.size() == 0) {
                    ShowMessage.ShowMsg(hand,
                            "当前没有装盒扫描数据");
                    return;
                }
                ShowMessage.ShowMsg(hand, ShowMessage.HandScanSuccess, "success");
            } catch (Exception e) {
                ShowMessage.ShowMsg(hand, "查找数据出错" + e.getMessage());
            }

        }

    }

    private class handShowMsg extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ShowMessage.HandShowMessage: // 显示错误提示显示
                    ShowMessage.Show(SelectPackingScanDetail1.this, msg.obj.toString());
                    break;
                case ShowMessage.HandScanSuccess:

                    initListView(slist);

                    break;
                default:
                    // MyProgressDialog.close();
                    // ShowMessage.Show(Xundian.this,msg.obj.toString());
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

        SimpleAdapter adapter = new SimpleAdapter(SelectPackingScanDetail1.this, mList,
                R.layout.list_purcheck_detail, new String[]{"product_id", "modelm", "colors", "curcount"},

                new int[]{R.id.txt_list1, R.id.txt_list2, R.id.txt_list3, R.id.txt_list4});
        listview.setAdapter(adapter);
        tv_total.setText("（共 " + mList.size() + " 条）");
        MyProgressDialog.close();
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

