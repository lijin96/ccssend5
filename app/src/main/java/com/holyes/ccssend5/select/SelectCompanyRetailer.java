package com.holyes.ccssend5.select;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.ccssend5.R;
import com.holyes.agent.activity.P_Dv_OutStock_D_L_Lens_NoBill;
import com.holyes.agent.activity.P_Dv_OutStock_D_L_NoBill;
import com.holyes.agent.activity.P_Dv_ReturnedPurchase_D_L_TransferGoods;
import com.holyes.ccssend5.activity.HelpActivity;
import com.holyes.ccssend5.lib.ShowMessage;
import com.holyes.ccssend5.lib.SqliteDataHelper;
import com.holyes.ccssend5.lib.SysUserInfo;
import com.holyes.ccssend5.utils.DisplayUtil;
import com.holyes.ccssend5.utils.SomeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: SelectCompanyRetailer
 * @Description: 代理商版本 选择零售商
 * @Author: lijin
 * @Date: 2021/3/10 9:56
 */
public class SelectCompanyRetailer extends Activity {


    private Handler hand;
    private Context mContext;
    private SimpleAdapter adapter;
    private SysUserInfo sysUserInfo;
    private LayoutInflater inflater;
    private SimpleAdapter spinnerProviceAdapter, spinnerCityAdapter;


    private View alertView;//AlertDialog的布局view
    private ListView listview;
    private EditText et_search;
    private TextView tv_total;
    private TextView company_retailer_help;//零售商帮助
    private Spinner spinnerProvince, spinnerCity;
    private Button btn_filter;

    private List<Map<String, Object>> dList = new ArrayList<Map<String, Object>>();
    private List<Map<String, Object>> listProvice = new ArrayList<Map<String, Object>>();
    private List<Map<String, Object>> listCity = new ArrayList<Map<String, Object>>();
    private Map<String, Object> addMap = new HashMap<String, Object>();
    private Map<String, Object> item;

    private String lsv_etStr, provicename = "", cityname = "", sql = "";

    private int provicePosition = 0, cityPosition = 0, lastProvicePosition = 0;//选择的省，市。上次选择的省
    private final int Lic_SelectSure = 3;

    private String lsv_aim = "";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        DisplayUtil.setDefaultDisplay(this);
        setContentView(R.layout.select_company_retailer);
        mContext = this;
        hand = new handShowMsg();
        sysUserInfo = new SysUserInfo(getApplicationContext());
        ((Button) findViewById(R.id.btn_exit))
                .setOnClickListener(new BtnExitClickListener());
        listview = (ListView) findViewById(R.id.lst_company);
        tv_total = (TextView) findViewById(R.id.tv_total);
        et_search = (EditText) findViewById(R.id.et_search);
        btn_filter = (Button) findViewById(R.id.btn_filter);

        lsv_aim = getIntent().getStringExtra("aim");

        et_search.addTextChangedListener(new EtTextWatcher());
        btn_filter.setOnClickListener(new BtnFilterClick());


        addMap.put("provicename", "全部");
        addMap.put("cityname", "全部");
        getEtStrFreshListView();
        listview.setOnItemClickListener(new ListViewItemClickListener());

        company_retailer_help = (TextView) findViewById(R.id.company_retailer_help);
        SpannableString content = new SpannableString(company_retailer_help.getText().toString());
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        content.setSpan(new ForegroundColorSpan(Color.parseColor("#FFCC80")), 0, content.length(), 0);
        company_retailer_help.setText(content);
        company_retailer_help.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent helpIntent = new Intent(SelectCompanyRetailer.this, HelpActivity.class);
                helpIntent.putExtra("help", "companyRetailer");
                startActivity(helpIntent);
            }
        });
    }


    public void initListView(String sql) {
        dList = SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList(sql, null);
        Map<String, Object> TestMap = new HashMap<String, Object>();
//        TestMap.put("tradername","测试零售店");
//        TestMap.put("link","测试联系人");
//        TestMap.put("tel","测试电话");
//        TestMap.put("traderid","测试代号");
//        TestMap.put("corpaddr","测试地址");
//        dList.add(TestMap);
        adapter = new SimpleAdapter(this, dList, R.layout.lst_company_item,
                new String[]{"tradername", "link", "tel", "traderid", "corpaddr",
                }, new int[]{
                R.id.companyName, R.id.companyLink, R.id.companyTel,
                R.id.company_id, R.id.corpaddr});
        listview.setAdapter(adapter);
        tv_total.setText("（共 " + dList.size() + " 条）");
    }

    private class handShowMsg extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ShowMessage.HandShowMessage: // 显示错误提示显示
                    ShowMessage.Show(SelectCompanyRetailer.this, msg.obj.toString());
                    break;
                default:
                    // MyProgressDialog.close();
                    // ShowMessage.Show(Xundian.this,msg.obj.toString());
                    break;
            }
            super.handleMessage(msg);
        }
    }

    class EtTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            getEtStrFreshListView();
        }

    }


    public void getEtStrFreshListView() {
        if (provicename.equals("全部")) {
            provicename = "";
        }
        if (cityname.equals("全部")) {
            cityname = "";
        }
        lsv_etStr = et_search.getText().toString().trim();
        if (lsv_etStr.isEmpty()) {
            sql = "select * from newretail where agentid='" + sysUserInfo.getCompanyid() + "' and provicename like '%%" + provicename + "%%' and " +
                    "cityname like '%%" + cityname + "%%' ";
        } else {
            sql = "select traderid,tradername,link,tel,corpaddr" +
                    " from newretail where agentid='" + sysUserInfo.getCompanyid() + "' and " +
                    "provicename like '%%" + provicename + "%%' and " +
                    "cityname like '%%" + cityname + "%%' and " +

                    "(traderid like '%%" + lsv_etStr + "%%' or " +
                    "tradername like '%%" + lsv_etStr + "%%' or " +
                    "link like '%%" + lsv_etStr + "%%' or " +
                    "tel like '%%" + lsv_etStr + "%%' or " +
                    "corpaddr like '%%" + lsv_etStr + "%%') ";
        }
        initListView(sql);

    }

    //筛选按钮
    class BtnFilterClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            showFilterDialog();
        }

    }

    /**
     * 弹出筛选dialog
     *
     * @author van van.shu@magic-point.com
     * @version 创建时间：2018-1-15 上午11:15:00
     */
    public void showFilterDialog() {

        initSpinner();
        TextView tv_title = new TextView(mContext);
        tv_title.setPadding(10, 10, 10, 10);
        tv_title.setText("省份城市筛选");
        tv_title.setGravity(Gravity.CENTER);
        tv_title.setTextSize(25);
        tv_title.setTextColor(Color.parseColor("#30C0FF"));
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, AlertDialog.THEME_HOLO_LIGHT);
        //		builder.setTitle("省份城市筛选")
        builder.setCustomTitle(tv_title)
                .setView(alertView)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getEtStrFreshListView();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private class BtnExitClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            finish();
        }
    }


    // item选择事件
    private class ListViewItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            ListView listView = (ListView) parent;

            item = (Map<String, Object>) listView.getItemAtPosition(position);

            Intent sureIntent = new Intent(SelectCompanyRetailer.this, SelectSureConfirm.class);
            sureIntent.putExtra("title", "确定选择零售商：" + (String) item.get("tradername") + "？");
            startActivityForResult(sureIntent, Lic_SelectSure);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Lic_SelectSure:
                    Intent intent = new Intent(this, P_Dv_OutStock_D_L_NoBill.class);
                    if (lsv_aim.equals("P_Dv_OutStock_D_L_Lens_NoBill")){
                        intent = new Intent(this, P_Dv_OutStock_D_L_Lens_NoBill.class);
                    }else if (lsv_aim.equals("P_Dv_ReturnedPurchase_D_L_TransferGoods")){
                        intent = new Intent(this, P_Dv_ReturnedPurchase_D_L_TransferGoods.class);
                    }

                    intent.putExtra("company_na", item.get("tradername").toString());
                    intent.putExtra("company_id", item.get("traderid").toString());
                    startActivity(intent);
                    break;
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //初始化Spinner
    public void initSpinner() {
        inflater = LayoutInflater.from(mContext);
        alertView = inflater.inflate(R.layout.select_retailer_filter, null);
        spinnerProvince = (Spinner) alertView.findViewById(R.id.spinner_province_filfter);
        spinnerCity = (Spinner) alertView.findViewById(R.id.spinner_city_filfter);
        spinnerProvince.setOnItemSelectedListener(new SpinnerProviceListener());
        spinnerCity.setOnItemSelectedListener(new SpinnerCityListener());

        spinnerProvince.setPrompt("\t\t\t\t\t\t省份选择");
        spinnerCity.setPrompt("\t\t\t\t\t\t城市选择");


        listProvice.clear();
        listProvice.add(addMap);
        listProvice.addAll(SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList("select provicename from newretail where agentid='" + sysUserInfo.getCompanyid() + "' group by provicename order by provicename", null));
        spinnerProviceAdapter = new SimpleAdapter(mContext, listProvice, R.layout.select_model_filter_spinner_item, new String[]{"provicename"}, new int[]{R.id.tv_spinner_item});
        spinnerProvince.setAdapter(spinnerProviceAdapter);
        spinnerProvince.setSelection(provicePosition);

    }

    //省份
    class SpinnerProviceListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int position, long id) {
            provicePosition = position;
            Map<String, Object> map = (Map<String, Object>) parent.getItemAtPosition(position);
            provicename = (String) map.get("provicename");

            String sql = "";
            if ("全部".equals(provicename)) {
                sql = "select  cityname from newretail where agentid='" + sysUserInfo.getCompanyid() + "' group by cityname ";
            } else {
                sql = "select  cityname from newretail where agentid='" + sysUserInfo.getCompanyid() + "' and provicename ='" + provicename + "' group by cityname ";
            }
            listCity.clear();
            listCity.add(addMap);
            listCity.addAll(SqliteDataHelper.getHelper(mContext).QueryDbList(sql, null));
            spinnerCityAdapter = new SimpleAdapter(mContext, listCity,
                    R.layout.select_model_filter_spinner_item, new String[]{"cityname"}, new int[]{R.id.tv_spinner_item});
            spinnerCity.setAdapter(spinnerCityAdapter);
            if (position == lastProvicePosition) {
                spinnerCity.setSelection(cityPosition);
            } else {

                spinnerCity.setSelection(0);
            }
            lastProvicePosition = position;
            spinnerCityAdapter.notifyDataSetChanged();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }

    }

    //城市
    class SpinnerCityListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int position, long id) {
            cityPosition = position;
            Map<String, Object> map = (Map<String, Object>) parent.getItemAtPosition(position);
            cityname = (String) map.get("cityname");

        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        getEtStrFreshListView();
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

