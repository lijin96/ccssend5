package com.holyes.ccssend5.select;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
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
import com.holyes.ccssend5.lib.SortListMapComparator;
import com.holyes.ccssend5.lib.SqliteDataHelper;
import com.holyes.ccssend5.utils.DisplayUtil;
import com.holyes.ccssend5.utils.SomeUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: SelectSupplier
 * @Description: 选择供应商
 * @Author: lijin
 * @Date: 2021/3/10 9:59
 */
public class SelectSupplier extends Activity {

    private SimpleAdapter adapter;

    private ListView listview;
    private EditText et_query_supplier;//搜索输入框
    private TextView tv_total;

    private List<Map<String, Object>> list;
    private Map<String, Object> item;

    private String sql = "", searchSql = "";
    private String etStr = "";
    private String lsv_aim = "";//目的地，rfid的activity与其他的跳转不同

    private final int Lic_SelectSure = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        DisplayUtil.setDefaultDisplay(this);
        setContentView(R.layout.new_select_supplier);

        Intent getIntent = getIntent();
        lsv_aim = getIntent.getStringExtra("aim");
        listview = (ListView) findViewById(R.id.listView1);

        et_query_supplier = (EditText) findViewById(R.id.et_query_supplier);
        tv_total = ((TextView) findViewById(R.id.tv_total));

        sql = "select supplier_id,supplier_name,uprecndate from newsupplier";

        list = SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList(sql, null);
        Collections.sort(list, new SortListMapComparator("supplier_id"));
        adapter = new SimpleAdapter(this, list, R.layout.new_list_select_supplier, new String[]

                {"supplier_id", "supplier_name"}, new int[]

                {R.id.txt_list1, R.id.txt_list2});
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new listViewClick());
        tv_total.setText("（共 " + list.size() + " 条）");
        et_query_supplier.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                // TODO Auto-generated method stub
                etStr = et_query_supplier.getText().toString().trim();
                if (etStr.isEmpty()) {
                    freshListView(sql);
                } else {
                    searchSql = "select supplier_id,supplier_name from newsupplier where " +
                            "supplier_id like '%%" + etStr + "%%' or " +
                            "supplier_name like '%%" + etStr + "%%'";
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

            ListView listView = (ListView) parent;

            item = (Map<String, Object>) listView.getItemAtPosition(position);

            Intent sureIntent = new Intent(SelectSupplier.this, SelectSureConfirm.class);
            sureIntent.putExtra("title", "确定选择供应商：" + (String) item.get("supplier_name") + "？");
            startActivityForResult(sureIntent, Lic_SelectSure);

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Lic_SelectSure:
                    if (lsv_aim == null || lsv_aim.isEmpty()) {
                        return;
                    }
                    Intent intent = new Intent(this, SelectStock.class);
                    //装盒入库或者入库退回（无单）
                    if ("P_Dv_InStock_PackBox_List_NoBill".equals(lsv_aim)
                            || "P_Dv_ReturnedPurchase_Z_G_NoBill".equals(lsv_aim)) {
                        intent = new Intent(SelectSupplier.this, SelectStock.class);
                    }
                    //无单入库撤销
                    else if ("P_RFID_InStock_Goods_NoBill_Cancel".equals(lsv_aim)) {//
//					intent = new Intent(SelectSupplier.this,P_RFID_InStock_Goods_NoBill_Cancel.class);
                    }
                    //入库换型号
                    else if ("P_Dv_InStock_Z_ChangeProduct".equals(lsv_aim)) {//
                        intent = new Intent(SelectSupplier.this, SelectStock.class);
                    }
                    intent.putExtra("supplier_id", (String) item.get("supplier_id"));
                    intent.putExtra("supplier_name", (String) item.get("supplier_name"));
                    intent.putExtra("aim", lsv_aim);
                    startActivity(intent);
                    break;
                default:
                    break;
            }

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void freshListView(String msql) {
        list.clear();
        list.addAll(SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList(msql, null));
        Collections.sort(list, new SortListMapComparator("supplier_id"));
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

