package com.holyes.ccssend5.select;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.ccssend5.R;
import com.holyes.ccssend5.activity.HelpActivity;
import com.holyes.ccssend5.lib.AccessWeb;
import com.holyes.ccssend5.lib.ShowMessage;
import com.holyes.ccssend5.lib.SortListMapComparator;
import com.holyes.ccssend5.lib.SqliteDataHelper;
import com.holyes.ccssend5.utils.DisplayUtil;
import com.holyes.ccssend5.utils.SomeUtils;
import com.holyes.headquarter.sendgoods_d.P_Dv_OutStock_Z_D_L_NoBill_BeInStock;
import com.holyes.headquarter.sendgoods_d.P_Dv_OutStock_Z_D_NoBill_BeInStock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: SelectCompanyD
 * @Description: 代理商选择
 * @Author: lijin
 * @Date: 2021/3/10 9:56
 */
public class SelectCompanyD extends Activity {

    private Handler hand;
    private Context mContext;
    private AccessWeb mAccessWeb;
    private PopupWindow mPopWindow;

    private ListView listview;
    private EditText et_search;
    private TextView tv_total;
    private TextView tv_help, tv_scan;//代理商帮助,扫码确定客户接口

    Map<String, Object> item;
    List<Map<String, Object>> list;
    List<Map<String, Object>> searchList = new ArrayList<Map<String, Object>>();

    private String lsv_aim = "";
    private String lsv_etStr = "", lsv_searchSql = "";

    private final int Lic_SelectSure = 3;
    private List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayUtil.setDefaultDisplay(this);
        setContentView(R.layout.new_select_company_d);
        hand = new handShowMsg();
        mContext = this;
        mAccessWeb = new AccessWeb(mContext);

        listview = (ListView) findViewById(R.id.listView1);
        et_search = (EditText) findViewById(R.id.et_search);
        tv_total = (TextView) findViewById(R.id.tv_total);
        listview.setOnItemClickListener(new listViewClick());
        lsv_aim = getIntent().getStringExtra("aim");
        list = SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList("select * from newcompany", null);
        initListView(list);
        et_search.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                // TODO Auto-generated method stub
                lsv_etStr = et_search.getText().toString().trim();
                if (lsv_etStr.trim().isEmpty()) {
                    initListView(list);
                    return;
                }
                lsv_searchSql = "select agentid,agentname,link,tel,corpaddr from newcompany where " +
                        "(agentid like '%%" + lsv_etStr + "%%' or " +
                        "agentname like '%%" + lsv_etStr + "%%' or " +
                        "link like '%%" + lsv_etStr + "%%' or " +
                        "tel like '%%" + lsv_etStr + "%%' or " +
                        "corpaddr like '%%" + lsv_etStr + "%%')";
                searchList.clear();
                searchList = SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList(lsv_searchSql, null);
                initListView(searchList);
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
        tv_help = (TextView) findViewById(R.id.company_help);
        SpannableString content = new SpannableString(tv_help.getText().toString());
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        content.setSpan(new ForegroundColorSpan(Color.parseColor("#FFCC80")), 0, content.length(), 0);
        tv_help.setText(content);
        tv_help.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent helpIntent = new Intent(SelectCompanyD.this, HelpActivity.class);
                helpIntent.putExtra("help", "companyD");
                startActivity(helpIntent);
            }
        });
        tv_scan = (TextView) findViewById(R.id.tv_scan);
//		if("P_Dv_ReturnedPurchase_Z_D_NoBill".equals(lsv_aim))
//		{
//			tv_scan.setVisibility(View.VISIBLE);
//		}
        tv_scan.setOnClickListener(new View.OnClickListener() {

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
                    //无单无入库代销发货
                    if ("P_Dv_OutStock_Z_D_NoBill_NoInStock".equals(lsv_aim)) {
                        intent = new Intent(SelectCompanyD.this, SelectStock.class);
                        intent.putExtra("aim", "P_Dv_OutStock_Z_D_NoBill_NoInStock");
                    }

                    //代理有入库无单发货/无单有入库的不用选仓库了》》》2018-04-25
                    else if ("P_Dv_OutStock_Z_D_NoBill_BeInStock".equals(lsv_aim)) {
                        //					intent = new Intent(SelectCompanyD.this,SelectStock.class);
                        intent = new Intent(SelectCompanyD.this, P_Dv_OutStock_Z_D_NoBill_BeInStock.class);
                        intent.putExtra("aim", "P_Dv_OutStock_Z_D_NoBill_BeInStock");
                    }

                    //代理无单退货
                    else if ("P_Dv_ReturnedPurchase_Z_D_NoBill".equals(lsv_aim)) {
                        intent = new Intent(SelectCompanyD.this, SelectStock.class);

                    } else if (lsv_aim.equals("P_Dv_OutStock_Z_D_L_NoBill_BeInStock")){
                        //无单有入库代发货
                        intent = new Intent(SelectCompanyD.this, P_Dv_OutStock_Z_D_L_NoBill_BeInStock.class);
                    }


                    //rfid代理发货撤销
                    if (lsv_aim.equals("P_RFID_OutStock_Goods_Z_D_NoBill_Cancel")) {
                        //					Intent intent = new Intent(Select_Company_D.this,Select_stock.class);
                        //暂时不需要选择仓库
                        //					intent = new Intent(SelectCompanyD.this,P_RFID_OutStock_Goods_Z_D_NoBill_Cancel.class);

                    }
                    intent.putExtra("company_name", (String) item.get("agentname"));
                    intent.putExtra("company_id", (String) item.get("agentid"));
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
            Intent sureIntent = new Intent(SelectCompanyD.this, SelectSureConfirm.class);
            sureIntent.putExtra("title", "确定选择代理商：" + (String) item.get("agentname") + "？");
            startActivityForResult(sureIntent, Lic_SelectSure);

        }

    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        lsv_etStr = et_search.getText().toString().trim();
        lsv_searchSql = "select agentid,agentname,link,tel,corpaddr from newcompany where " +
                "(agentid like '%%" + lsv_etStr + "%%' or " +
                "agentname like '%%" + lsv_etStr + "%%' or " +
                "link like '%%" + lsv_etStr + "%%' or " +
                "tel like '%%" + lsv_etStr + "%%' or " +
                "corpaddr like '%%" + lsv_etStr + "%%')";
        searchList.clear();
        searchList = SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList(lsv_searchSql, null);
        initListView(searchList);
    }

    private class handShowMsg extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ShowMessage.HandShowMessage: // 显示错误提示显示
                    ShowMessage.Show(SelectCompanyD.this, msg.obj.toString());
                    break;
                case ShowMessage.HandSuccess:

                    String company_name = "", company_id = "";
                    for (Map<String, Object> map : result) {
                        company_name = map.get("CompanyName").toString();
                        company_id = map.get("CompanyId").toString();
                    }
                    Intent intents = new Intent();
                    intents = new Intent(SelectCompanyD.this, SelectStock.class);
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

    public void initListView(List<Map<String, Object>> mList) {
        Collections.sort(mList, new SortListMapComparator("agentname"));
        SimpleAdapter adapter = new SimpleAdapter(this, mList, R.layout.new_list_company_d, new String[]
                {"agentname", "agentid", "link", "tel", "corpaddr"}, new int[]{R.id.agent_list1, R.id.agent_id, R.id.agent_list2, R.id.txt_tel, R.id.agent_list3});
        listview.setAdapter(adapter);
        tv_total.setText("（共 " + mList.size() + " 条）");

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
                    result = mAccessWeb.P_Dv_SeekCompanyInfo("代理", code);
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

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mPopWindow != null && mPopWindow.isShowing()) {
            return false;
        }
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
