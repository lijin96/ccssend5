package com.holyes.ccssend5.select;

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
import com.holyes.ccssend5.lib.SqliteDataHelper;
import com.holyes.ccssend5.myview.MyProgressDialog;
import com.holyes.ccssend5.utils.DisplayUtil;
import com.holyes.ccssend5.utils.SomeUtils;
import com.holyes.headquarter.other.P_Dv_PackDressBoxCheck;
import com.holyes.headquarter.sendgoods_d.P_Dv_OutStock_Z_D_PackMeal_NoBill_NoInStock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: SelectSetmeal
 * @Description: 选择套餐界面
 * @Author: lijin
 * @Date: 2021/3/10 9:58
 */
public class SelectSetmeal extends Activity implements View.OnClickListener {

    private Context mContext;
    private AccessWeb accWeb;
    private Thread downloadThread;
    private Handler hand;
    private Intent getIntent;

    private ListView listview;
    private EditText et_search;
    private TextView tv_total;

    private List<Map<String, Object>> list;
    private List<Map<String, Object>> searchList = new ArrayList<Map<String, Object>>();
    private Map<String, Object> item;

    private String lsv_aim = "";
    private String lsv_etStr = "", lsv_searchSql = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        DisplayUtil.setDefaultDisplay(this);
        setContentView(R.layout.new_select_setmeal);
        accWeb = new AccessWeb(getApplicationContext());
        hand = new handShowMsg();
        mContext = this;
        listview = (ListView) findViewById(R.id.listView1);
        tv_total = (TextView) findViewById(R.id.tv_total);

        getIntent = getIntent();
        lsv_aim = getIntent().getStringExtra("aim");

        try {
            SqliteDataHelper.getHelper(getApplicationContext()).execSQL("delete from packmealset");
        } catch (Exception e) {
            e.printStackTrace();
        }


        list = new ArrayList<Map<String, Object>>();

        listview.setOnItemClickListener(new ListViewItemClik());
        et_search = (EditText) findViewById(R.id.et_query_stock);

        MyProgressDialog.show(this, "正在获取数据...", false, false);
        downloadThread = new Thread(new DownLoadDataThread());

        downloadThread.start();
        et_search.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                // TODO Auto-generated method stub
                lsv_etStr = et_search.getText().toString().trim();
                if (lsv_etStr.trim().isEmpty()) {
                    initListView(list);
                    return;
                } else {
                    lsv_searchSql = "select * from packmealset where " +
                            "PackId like '%%" + lsv_etStr + "%%' or " +
                            "PackName like '%%" + lsv_etStr + "%%'";
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

            Intent intent = new Intent(mContext, P_Dv_PackDressBoxCheck.class);

            if (lsv_aim.equals("P_Dv_OutStock_Z_D_PackMeal_NoBill_NoInStock")||lsv_aim.equals("P_Dv_OutStock_Z_L_PackMeal_NoBill_NoInStock")) {
                intent = new Intent(mContext, P_Dv_OutStock_Z_D_PackMeal_NoBill_NoInStock.class);
                //无单无入库代销套餐装盒出货
                intent.putExtra("aim", lsv_aim);
                intent.putExtra("supplier_id", getIntent.getStringExtra("supplier_id"));
                intent.putExtra("supplier_name", getIntent.getStringExtra("supplier_name"));

                intent.putExtra("stock_id", getIntent.getStringExtra("stock_id"));
                intent.putExtra("stock_name",getIntent.getStringExtra("stock_name"));
                //代理或直营
                intent.putExtra("company_name", getIntent.getStringExtra("company_name"));
                intent.putExtra("company_id", getIntent.getStringExtra("company_id"));
            }else if (lsv_aim.equals("P_Dv_OutStock_Z_L_PackMeal_NoBill_NoInStock")){
                //无单无入库直销套餐装盒出货
                intent.putExtra("aim", lsv_aim);
                intent.putExtra("supplier_id", getIntent.getStringExtra("supplier_id"));
                intent.putExtra("supplier_name", getIntent.getStringExtra("supplier_name"));

                intent.putExtra("stock_id", getIntent.getStringExtra("stock_id"));
                intent.putExtra("stock_name",getIntent.getStringExtra("stock_name"));
                //代理或直营
                intent.putExtra("company_name", getIntent.getStringExtra("company_name"));
                intent.putExtra("company_id", getIntent.getStringExtra("company_id"));
            }

            intent.putExtra("PackId", (String) item.get("PackId"));
            intent.putExtra("PackName", (String) item.get("PackName"));

            startActivity(intent);

//
//            Intent intent = new Intent(SelectSetmeal.this, SelectSureConfirm.class);
//
//            intent.putExtra("title", "确定选择套餐：" + item.get("PackName")
//                    + "吗？");

//            startActivityForResult(intent, 0);
        }
    }

    private class DownLoadDataThread implements Runnable {
        @Override
        public void run() {

            try {

                list = accWeb.GetPackMealSet();

                ShowMessage.ShowMsg(hand, ShowMessage.HandSuccess, "success");
            } catch (Exception e) {
                ShowMessage.ShowMsg(hand, "下载出错" + e.getMessage());
            }
        }

    }


    public void initListView(List<Map<String, Object>> mList) {
//		Collections.sort(list,new SortListMapComparator("purchecklno"));
        SimpleAdapter adapter = new SimpleAdapter(this, mList, R.layout.list_select_stock,
                new String[]{"PackId", "PackName"}, new int[]{R.id.txt_list1, R.id.txt_list2});
        listview.setAdapter(adapter);
        MyProgressDialog.close();
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
                    List<String> sqlList = new ArrayList<String>();
                    if (list.size() > 0) {
                        String PackId, PackName, sql;
                        for (Map<String, Object> map : list) {
                            PackId = (String) map.get("PackId");
                            PackName = (String) map.get("PackName");

                            sql = "insert into packmealset(PackId,PackName)values('" + PackId + "','" + PackName + "')";
                            sqlList.add(sql);
                        }
                        SqliteDataHelper.getHelper(getApplicationContext()).BatchOperation(sqlList);
                        String listsql = "select * from packmealset";
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

                intent.putExtra("PackId", (String) item.get("PackId"));
                intent.putExtra("PackName", (String) item.get("PackName"));


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

