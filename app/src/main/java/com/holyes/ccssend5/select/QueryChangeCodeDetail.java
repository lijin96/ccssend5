package com.holyes.ccssend5.select;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.ccssend5.R;
import com.holyes.ccssend5.lib.AccessWeb;
import com.holyes.ccssend5.lib.SqliteDataHelper;
import com.holyes.ccssend5.myview.MyProgressDialog;
import com.holyes.ccssend5.utils.DisplayUtil;
import com.holyes.ccssend5.utils.SomeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: QueryChangeCodeDetail
 * @Description: 产品换标扫描明细查看
 * @Author: lijin
 * @Date: 2021/3/10 9:55
 */
public class QueryChangeCodeDetail extends Activity {

    AccessWeb accWeb;

    private TextView tv_total;
    private ListView listview;
    private EditText et_search;

    List<Map<String, Object>> slist;
    List<Map<String, Object>> searchList = new ArrayList<Map<String, Object>>();

    private String lsv_searchSql = "", lsv_etStr = "", sql;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayUtil.setDefaultDisplay(this);
        setContentView(R.layout.new_query_change_code_detail);
        listview = (ListView) findViewById(R.id.listView_detail);
        et_search = (EditText) findViewById(R.id.et_search);
        tv_total = (TextView) findViewById(R.id.tv_total);
        sql = "select * from newchangecode";
        slist = SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList(sql, null);
        initListView(slist);
        et_search.addTextChangedListener(new EditTextChnangeListener());

    }

    /**
     * @param mList
     */
    private void initListView(List<Map<String, Object>> mList) {
        SimpleAdapter adapter = new SimpleAdapter(QueryChangeCodeDetail.this, mList,
                R.layout.new_list_change_code_detail, new String[]{"newcode", "oldcode", "oldcodetype"},
                new int[]{R.id.list_new_code_title, R.id.list_old_code_title, R.id.list_old_code_type_title});
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
            if (lsv_etStr.trim().isEmpty()) {
                initListView(slist);
                return;
            } else {
                lsv_searchSql = "select newcode,oldcode, oldcodetype from newchangecode where " +
                        "newcode like '%%" + lsv_etStr + "%%' or " +
                        "oldcode like '%%" + lsv_etStr + "%%' or " +
                        "oldcodetype like '%%" + lsv_etStr + "%%'";
                searchList.clear();
                searchList = SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList(lsv_searchSql, null);
                initListView(searchList);
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

