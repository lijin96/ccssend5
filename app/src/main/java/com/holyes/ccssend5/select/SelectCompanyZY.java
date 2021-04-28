package com.holyes.ccssend5.select;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.ccssend5.R;
import com.holyes.ccssend5.activity.HelpActivity;
import com.holyes.ccssend5.lib.AccessWeb;
import com.holyes.ccssend5.lib.ShowMessage;
import com.holyes.ccssend5.lib.SortCompanyZyComparator;
import com.holyes.ccssend5.lib.SqliteDataHelper;
import com.holyes.ccssend5.myview.MyProgressDialog;
import com.holyes.ccssend5.utils.DisplayUtil;
import com.holyes.ccssend5.utils.SomeUtils;
import com.holyes.headquarter.sendgoods_zy.P_Dv_OutStock_Z_L_NoBill_BeInStock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: SelectCompanyZY
 * @Description: 直营店选择
 * @Author: lijin
 * @Date: 2021/3/10 9:57
 */
public class SelectCompanyZY extends Activity {

    private Handler hand;
    private Context mContext;
    private AccessWeb mAccessWeb;
    private PopupWindow mPopWindow;

    private ListView listview;
    private EditText et_search;
    private TextView tv_total, tv_zy_scan;
    private TextView companyzy_help;//直营店帮助

    List<Map<String, Object>> list;
    Map<String, Object> item;
    List<Map<String, Object>> searchList = new ArrayList<Map<String, Object>>();

    private String lsv_aim = "";
    private String lsv_etStr = "", lsv_searchSql = "", sql = "";

    private final int Lic_SelectSure = 3;

    private View alertView;//AlertDialog的布局view
    private Button btn_filter;
    private Spinner spinnerProvince, spinnerCity;
    private SimpleAdapter spinnerProviceAdapter, spinnerCityAdapter;

    private LayoutInflater inflater;
    private List<Map<String, Object>> dList = new ArrayList<Map<String, Object>>();
    private List<Map<String, Object>> listProvice = new ArrayList<Map<String, Object>>();
    private List<Map<String, Object>> listCity = new ArrayList<Map<String, Object>>();
    private List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
    private Map<String, Object> addMap = new HashMap<String, Object>();

    private String provicename = "", cityname = "";
    private int provicePosition = 0, cityPosition = 0, lastProvicePosition = 0;//选择的省，市。上次选择的省


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayUtil.setDefaultDisplay(this);
        setContentView(R.layout.new_select_company_zy);
        //		dtWork = new SQLiteWork(getApplicationContext());
        hand = new handShowMsg();
        mContext = this;
        mAccessWeb = new AccessWeb(mContext);
        listview = (ListView) findViewById(R.id.listView1);
        et_search = (EditText) findViewById(R.id.et_search);
        tv_total = (TextView) findViewById(R.id.tv_total);

        btn_filter = (Button) findViewById(R.id.btn_filter);

        listview.setOnItemClickListener(new listViewClick());
        btn_filter.setOnClickListener(new BtnFilterClick());

        lsv_aim = getIntent().getStringExtra("aim");
        sql = "select * from newdirect";

        addMap.put("provicename", "全部");
        addMap.put("cityname", "全部");
        getEtStrFreshListView();

        et_search.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {


            }

            @Override
            public void afterTextChanged(Editable arg0) {
                getEtStrFreshListView();

            }
        });
        companyzy_help = (TextView) findViewById(R.id.companyzy_help);
        SpannableString content = new SpannableString(companyzy_help.getText().toString());
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        content.setSpan(new ForegroundColorSpan(Color.parseColor("#FFCC80")), 0, content.length(), 0);
        companyzy_help.setText(content);
        companyzy_help.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent helpIntent = new Intent(SelectCompanyZY.this, HelpActivity.class);
                helpIntent.putExtra("help", "companyZy");
                startActivity(helpIntent);
            }
        });

        tv_zy_scan = (TextView) findViewById(R.id.tv_zy_scan);
        if ("P_Dv_ReturnedPurchase_Z_L_NoBill".equals(lsv_aim)) {
            tv_zy_scan.setVisibility(View.VISIBLE);
        }
        tv_zy_scan.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showPopListView();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (lsv_aim == null || lsv_aim.isEmpty()) {
            return;
        }
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Lic_SelectSure:

                    Intent intent = null;
                    //无单有入库的不用选仓库了》》》2018-04-25
                    if ("P_Dv_OutStock_Z_L_NoBill_BeInStock".equals(lsv_aim)) {
                        intent = new Intent(mContext, P_Dv_OutStock_Z_L_NoBill_BeInStock.class);
                    } else {
                        intent = new Intent(mContext, SelectStock.class);
                    }
                    intent.putExtra("company_name", (String) item.get("tradername"));
                    intent.putExtra("company_id", (String) item.get("traderid"));
                    //				intent.putExtra("disc", (String)item.get("disc"));
                    intent.putExtra("aim", lsv_aim);
                    startActivity(intent);
                    break;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private class listViewClick implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {

            ListView listView = (ListView) parent;

            item = (Map<String, Object>) listView.getItemAtPosition(position);

            Intent sureIntent = new Intent(SelectCompanyZY.this, SelectSureConfirm.class);
            sureIntent.putExtra("title", "确定选择直营店：" + (String) item.get("tradername") + "？");
            startActivityForResult(sureIntent, Lic_SelectSure);

        }

    }

    private class handShowMsg extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ShowMessage.HandShowMessage: // 显示错误提示显示
                    ShowMessage.Show(SelectCompanyZY.this, msg.obj.toString());
                    break;

                case ShowMessage.HandSuccess:
                    String company_name = "", company_id = "";
                    for (Map<String, Object> map : result) {
                        company_name = map.get("CompanyName").toString();
                        company_id = map.get("CompanyId").toString();
                    }
                    Intent intents = new Intent();
                    intents = new Intent(SelectCompanyZY.this, SelectStock.class);
                    intents.putExtra("company_name", company_name);
                    intents.putExtra("company_id", company_id);
                    intents.putExtra("aim", lsv_aim);
                    startActivity(intents);

                    WindowManager.LayoutParams lp = getWindow().getAttributes();
                    lp.alpha = 1f;
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                    getWindow().setAttributes(lp);
                    mPopWindow.dismiss();
                    break;

                default:
                    // MyProgressDialog.close();
                    // ShowMessage.Show(Xundian.this,msg.obj.toString());
                    break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getEtStrFreshListView();
    }

    public void initListView(String msql) {
        list = SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList(msql, null);
        Collections.sort(list, new SortCompanyZyComparator("tradername"));
        SimpleAdapter adapter = new SimpleAdapter(this, list, R.layout.new_list_company_zy, new String[]
                {"tradername", "traderid", "link", "tel", "agentname", "agentid"},
                new int[]{R.id.txt_list1, R.id.txt_id, R.id.txt_list2, R.id.txt_tel, R.id.txt_list3, R.id.txt_trader});
        listview.setAdapter(adapter);
        tv_total.setText("（共 " + list.size() + " 条）");
        MyProgressDialog.close();

    }

    ///////////////////////////筛选/////////////////////////////////
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

    public void getEtStrFreshListView() {
        if (provicename.equals("全部")) {
            provicename = "";
        }
        if (cityname.equals("全部")) {
            cityname = "";
        }
        lsv_etStr = et_search.getText().toString().trim();
        if (lsv_etStr.isEmpty()) {
            sql = "select * from newdirect where provicename like '%%" + provicename + "%%' and " +
                    "cityname like '%%" + cityname + "%%'  GROUP BY traderid";
        } else {
            sql = "select *  from newdirect where " +
                    "provicename like '%%" + provicename + "%%' and " +
                    "cityname like '%%" + cityname + "%%' and " +
                    "(traderid like '%%" + lsv_etStr + "%%' or " +
                    "tradername like '%%" + lsv_etStr + "%%' or " +
                    "link like '%%" + lsv_etStr + "%%' or " +
                    "tel like '%%" + lsv_etStr + "%%' or " +
                    "agentname like '%%" + lsv_etStr + "%%' or " +
                    "agentid like '%%" + lsv_etStr + "%%' or " +
                    "corpaddr like '%%" + lsv_etStr + "%%') " +
                    " GROUP BY traderid";
        }
        initListView(sql);
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
        listProvice.addAll(SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList("select provicename from newdirect group by provicename order by provicename", null));
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
                sql = "select  cityname from newdirect group by cityname ";
            } else {
                sql = "select  cityname from newdirect where provicename ='" + provicename + "' group by cityname ";
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

    private void showPopListView() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View contentView = inflater.inflate(R.layout.select_pop, null);
        View list = LayoutInflater.from(this).inflate(
                R.layout.new_select_company_d, null);
        if (mPopWindow == null) {
            mPopWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
        mPopWindow.setFocusable(true);
        mPopWindow.setOutsideTouchable(false);
        mPopWindow.setBackgroundDrawable(null);
        mPopWindow.getContentView().setFocusable(true); // 这个很重要
        mPopWindow.getContentView().setFocusableInTouchMode(true);
        ColorDrawable dw = new ColorDrawable(0x00000000);
        //设置SelectPicPopupWindow弹出窗体的背景
        mPopWindow.setBackgroundDrawable(dw);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getWindow().setAttributes(lp);

        mPopWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        mPopWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        mPopWindow.showAtLocation(
                list,
                Gravity.CENTER, 0, 0);
        mPopWindow.update();
        et_search.setFocusable(false);
        final EditText ed_code = (EditText) contentView.findViewById(R.id.ed_code);
        ed_code.setFocusable(true);
        ed_code.setFocusableInTouchMode(true);
        ed_code.requestFocus();
        Button btn_cancel = (Button) contentView.findViewById(R.id.bt_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1f;
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                getWindow().setAttributes(lp);
                mPopWindow.dismiss();
                et_search.setFocusable(true);
                et_search.setFocusableInTouchMode(true);
            }
        });

        ed_code.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        String code = ed_code.getText().toString().trim();
                        if (code.equals("")) {
                            ShowMessage.Show(mContext, "请重新扫描条码");
                            ed_code.setText("");
                        } else if (!SomeUtils.TextJudgmentSize(code)) {
                            ShowMessage.Show(mContext, "请扫描正确的条码");
                            ed_code.setText("");
                        } else {
                            startThreadCheckCode(code);
                            ed_code.setText("");
                        }
                    }
                    return true;
                }

                return false;
            }

        });

    }

    private void startThreadCheckCode(final String code) {
        Thread startCode = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    result = mAccessWeb.P_Dv_SeekCompanyInfo("直营", code);
                    if (result.equals("")) {
                        ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage, "请重新扫描");
                        return;
                    }
                    ShowMessage.ShowMsg(hand, ShowMessage.HandSuccess, result);
                } catch (Exception e) {
                    ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage, e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        startCode.start();
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

