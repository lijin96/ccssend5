package com.holyes.factoryscan.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import androidx.annotation.Nullable;

import com.example.ccssend5.R;
import com.holyes.ccssend5.activity.HelpActivity;
import com.holyes.ccssend5.lib.AccessWeb;
import com.holyes.ccssend5.lib.ShowMessage;
import com.holyes.ccssend5.lib.SortListMapComparator;
import com.holyes.ccssend5.lib.SqliteDataHelper;
import com.holyes.ccssend5.lib.SysUserInfo;
import com.holyes.ccssend5.myview.MyProgressDialog;
import com.holyes.ccssend5.select.SelectCompanyD;
import com.holyes.ccssend5.select.SelectRetailStore;
import com.holyes.ccssend5.select.SelectStock;
import com.holyes.ccssend5.select.SelectSureConfirm;
import com.holyes.ccssend5.utils.DisplayUtil;
import com.holyes.ccssend5.utils.SomeUtils;
import com.holyes.headquarter.sendgoods_d.P_Dv_OutStock_Z_D_NoBill_BeInStock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: SelectFactoryGoods
 * @Description: 选择供应商-工厂产品
 * @Author: lijin
 * @Date: 2025/7/31 15:11
 */
public class SelectFactoryGoods extends Activity {

    private Handler hand;
    private Context mContext;
    private SimpleAdapter adapter;
    private SysUserInfo sysUserInfo;
    private LayoutInflater inflater;

    private AccessWeb accWeb;

    private View alertView;//AlertDialog的布局view
    private ListView listview;
    private EditText et_search;
    private TextView tv_total;

    private Button btn_seach;

    private List<Map<String, Object>> dList = new ArrayList<Map<String, Object>>();

    private Map<String, Object> item;

    private String lsv_etStr, provicename = "", cityname = "", sql = "";

    private final int Lic_SelectSure = 3;

    private String lsv_aim = "";
    private String company_id="";//代理商id

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        DisplayUtil.setDefaultDisplay(this);
        setContentView(R.layout.select_factorygoods);
        mContext = this;
        accWeb = new AccessWeb(getApplicationContext());
        hand = new handShowMsg();
        sysUserInfo = new SysUserInfo(getApplicationContext());
//        ((Button) findViewById(R.id.btn_exit))
//                .setOnClickListener(new BtnExitClickListener());
        listview = (ListView) findViewById(R.id.listView1);
        tv_total = (TextView) findViewById(R.id.tv_total);
        et_search = (EditText) findViewById(R.id.et_search);

        lsv_aim = getIntent().getStringExtra("aim");
//        et_search.addTextChangedListener(new EtTextWatcher());

        et_search.setOnKeyListener(new EtBarodeOnkeyListener());

        btn_seach=findViewById(R.id.btn_search);

//        ShowMessage.Show(mContext, "请输入要查询的数据");

        btn_seach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tBarcode =et_search.getText().toString().trim();
                if (tBarcode.equals("")) {
                    ShowMessage.Show(mContext, "请输入要查询的数据");
                }else{
                    DownLoadDataThread();
                }
            }
        });

        listview.setOnItemClickListener(new ListViewItemClickListener());

//        DownLoadDataThread();
    }


    public void initListView(List<Map<String, Object>> dataList) {
        adapter = new SimpleAdapter(this, dataList, R.layout.item_list_factorygoods,
                new String[]{"BrandName", "SeriesName", "Modelm", "Colors",
                }, new int[]{
                R.id.item_factorybrand, R.id.item_factoryseries, R.id.item_factorymodel,
                R.id.item_factorycolor});
        listview.setAdapter(adapter);
        tv_total.setText("（共 " + dataList.size() + " 条）");
    }

    private class handShowMsg extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ShowMessage.HandShowMessage: // 显示错误提示显示
                    MyProgressDialog.close();
                    ShowMessage.Show(SelectFactoryGoods.this, msg.obj.toString());
                    break;
                case ShowMessage.HandSuccess: // 返回成功
                    MyProgressDialog.close();
                    if (dList.size()>0){
                        initListView(dList);
                    }else{
                        ShowMessage.Show(SelectFactoryGoods.this, "未查询到数据");
                    }
                    break;

                default:
                    // MyProgressDialog.close();
                    // ShowMessage.Show(Xundian.this,msg.obj.toString());
                    break;
            }
            super.handleMessage(msg);
        }
    }

    //下载调拨单
    private void DownLoadDataThread() {
        MyProgressDialog.show(mContext, "正在获取数据...", true, false);
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    dList = accWeb.GetSearchGoodsInfor(et_search.getText().toString());
                    ShowMessage.ShowMsg(hand, ShowMessage.HandSuccess, "success");
                } catch (Exception e) {
                    ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage,"下载出错" + e.getMessage());
                }
            }
        });
        sendCode.start();
    }

    private class EtBarodeOnkeyListener implements View.OnKeyListener {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {

            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {

                    String tBarcode =et_search.getText().toString().trim();

                    if (tBarcode.equals("")) {
                        ShowMessage.Show(mContext, "请输入要查询的数据");
                        return true;
                    }

                    DownLoadDataThread();

                }
                return true;
            } else {
                return false;
            }
        }
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

            Intent intent = new Intent();
            intent.putExtra("Modelm", item.get("Modelm").toString());
            intent.putExtra("Colors", item.get("Colors").toString());
            intent.putExtra("GoodsId", item.get("GoodsId").toString());
            setResult(RESULT_OK, intent);
            finish();

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Lic_SelectSure:
//                    Intent intent = new Intent(this, P_Dv_OutStock_D_L_NoBill.class);
//                    if (lsv_aim.equals("P_Dv_OutStock_D_L_Lens_NoBill")){
//                        intent = new Intent(this, P_Dv_OutStock_D_L_Lens_NoBill.class);
//                    }else if (lsv_aim.equals("P_Dv_ReturnedPurchase_D_L_TransferGoods")){
//                        intent = new Intent(this, P_Dv_ReturnedPurchase_D_L_TransferGoods.class);
//                    }
//
//                    intent.putExtra("company_na", item.get("CustName").toString());
//                    intent.putExtra("company_id", item.get("CustId").toString());
//                    startActivity(intent);
                    break;
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }



    @Override
    protected void onResume() {
        super.onResume();
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

