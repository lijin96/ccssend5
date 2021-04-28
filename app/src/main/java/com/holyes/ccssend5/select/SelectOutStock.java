package com.holyes.ccssend5.select;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
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
import com.holyes.ccssend5.lib.ShowMessage;
import com.holyes.ccssend5.lib.SortListMapComparator;
import com.holyes.ccssend5.lib.SqliteDataHelper;
import com.holyes.ccssend5.utils.DisplayUtil;
import com.holyes.ccssend5.utils.SomeUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: SelectOutStock
 * @Description: 调出仓
 * @Author: lijin
 * @Date: 2021/3/10 9:57
 */
public class SelectOutStock extends Activity {
    private Handler hand;
    private Intent getIntent;
    private SimpleAdapter adapter;

    private ListView listview;
    private TextView tv_title, tv_total;
    private EditText et_query_stock;

    private List<Map<String, Object>> list;
    private Map<String, Object> item;

    private String etStr = "";
    private String lsv_aim = "";
    private String sql = "", searchSql = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        DisplayUtil.setDefaultDisplay(this);
        setContentView(R.layout.new_select_stock);

        getIntent = getIntent();
        lsv_aim = getIntent.getStringExtra("aim");

        hand = new handShowMsg();
        listview = (ListView) findViewById(R.id.listView1);
        et_query_stock = (EditText) findViewById(R.id.et_query_stock);
        tv_total = (TextView) findViewById(R.id.tv_total);
        tv_title = (TextView) findViewById(R.id.txt_tile);
        tv_title.setText("请选择调出仓");

        sql = "select stock_id,stock_name from newstock ";

        list = SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList(sql, null);
        Collections.sort(list, new SortListMapComparator("stock_id"));
        adapter = new SimpleAdapter(this, list, R.layout.new_list_select_stock, new String[]

                {"stock_id", "stock_name"}, new int[]{R.id.txt_list1, R.id.txt_list2});
        listview.setAdapter(adapter);
        tv_total.setText("（共 " + list.size() + " 条）");
        listview.setOnItemClickListener(new listViewClick());
        et_query_stock.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                // TODO Auto-generated method stub
                etStr = et_query_stock.getText().toString().trim();
                if (etStr.isEmpty()) {
                    freshListView(sql);
                } else {
                    searchSql = "select stock_id,stock_name from newstock where " +
                            "stock_id like '%%" + etStr + "%%' or " +
                            "stock_name like '%%" + etStr + "%%'";
                    freshListView(searchSql);
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

            if (lsv_aim == null || lsv_aim.isEmpty()) {
                return;
            }
            ListView listView = (ListView) parent;

            item = (Map<String, Object>) listView.getItemAtPosition(position);

            Intent intent = null;
            intent = new Intent(SelectOutStock.this, SelectInStock.class);
            intent.putExtra("aim", lsv_aim);
            intent.putExtra("outstock_id", (String) item.get("stock_id"));
            intent.putExtra("outstock_name", (String) item.get("stock_name"));
            startActivity(intent);
        }

    }

    private class handShowMsg extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ShowMessage.HandShowMessage: // 显示错误提示显示
                    ShowMessage.Show(SelectOutStock.this, msg.obj.toString());
                    break;

                default:
                    // MyProgressDialog.close();
                    // ShowMessage.Show(Xundian.this,msg.obj.toString());
                    break;
            }
        }
    }

    public void freshListView(String msql) {
        list.clear();
        list.addAll(SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList(sql, null));
        Collections.sort(list, new SortListMapComparator("stock_id"));
        adapter.notifyDataSetChanged();
        tv_total.setText("（共 " + list.size() + " 条）");
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

