package com.holyes.ccssend5.select;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.ccssend5.R;
import com.holyes.agent.adapter.SelectAgentDistributorAdapter;
import com.holyes.ccssend5.activity.HelpActivity;
import com.holyes.ccssend5.entity.Retailer;
import com.holyes.ccssend5.entity.StoreInfor;
import com.holyes.ccssend5.lib.ShowMessage;
import com.holyes.ccssend5.lib.SqliteDataHelper;
import com.holyes.ccssend5.lib.SysUserInfo;
import com.holyes.ccssend5.utils.SomeUtils;
import com.holyes.headquarter.sendgoods_zy.P_Dv_OutStock_Z_S_NoBill_InStock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: SelectDistribution
 * @Description: 选择直营分销店
 * @Author: lijin
 * @Date: 2021/3/10 9:57
 */
public class SelectDistribution extends Activity {

    private Handler hand;
    private Context mContext;
    private SysUserInfo sysUserInfo;
    private SelectAgentDistributorAdapter adapter;
    private StoreInfor selectedStoreInfor;


    private EditText et_search;
    private TextView tv_total, company_store_help;//点击查看帮助
    private ExpandableListView elistview_distributor;


    private List<Map<String, Object>> allRetailList = new ArrayList<Map<String, Object>>();
    private List<Map<String, Object>> searchRetailList = new ArrayList<Map<String, Object>>();
    private List<Map<String, Object>> searchDistributorList = new ArrayList<Map<String, Object>>();
    private Map<String, Object> item;


    private String lsv_etStr, lsv_searchSql, aim;

    private final int Lic_SelectSure = 3;
    private int distributorCount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.new_select_distribution);
        mContext = this;
        hand = new handShowMsg();
        aim = getIntent().getStringExtra("aim");
        sysUserInfo = new SysUserInfo(mContext);
        tv_total = (TextView) findViewById(R.id.tv_total);
        et_search = (EditText) findViewById(R.id.et_search);
        elistview_distributor = (ExpandableListView) findViewById(R.id.elistview_distributor);
        elistview_distributor.setGroupIndicator(null);

        //查出全部的直营店
        String sql = "select * from newdirect";
        allRetailList = SqliteDataHelper.getHelper(mContext).QueryDbList(sql, null);
        //根据全部的零售商查出全部的分销店，封装成适合适配器的格式适配
        initExpandListView(getAllDistributorByRetailer(allRetailList), false);

        et_search.addTextChangedListener(new TextWatcher() {
            @SuppressLint("NewApi")
            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

                lsv_etStr = et_search.getText().toString().trim();
                if (lsv_etStr.isEmpty()) {
                    initExpandListView(getAllDistributorByRetailer(allRetailList), false);
                    return;
                } else {
                    lsv_searchSql = "select * from newdirect where " +
                            "traderid like '%%" + lsv_etStr + "%%' or " +
                            "tradername like '%%" + lsv_etStr + "%%' or " +
                            "link like '%%" + lsv_etStr + "%%' or " +
                            "tel like '%%" + lsv_etStr + "%%' " +
                            //							" or corpaddr like '%%" + lsv_etStr + "%%'" +
                            " GROUP BY traderid";
                    searchRetailList.clear();
                    //查找符合条件的零售商
                    searchRetailList = SqliteDataHelper.getHelper(mContext).QueryDbList(
                            lsv_searchSql, null);

                    lsv_searchSql = "select * from agentinfor where " +
                            " storeid like '%%" + lsv_etStr + "%%' or " +
                            "storename like '%%" + lsv_etStr + "%%' or " +
                            "link like '%%" + lsv_etStr + "%%' or " +
                            "tel like '%%" + lsv_etStr + "%%' " +
                            " or corpaddr like '%%" + lsv_etStr + "%%'" +
                            " GROUP BY traderid,storeid";
                    searchDistributorList.clear();
                    //查找符合条件的分销店
                    searchDistributorList = SqliteDataHelper.getHelper(mContext).QueryDbList(
                            lsv_searchSql, null);
                    initExpandListView(getSearchDistributorList(searchRetailList, searchDistributorList), true);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {


            }

            @Override
            public void afterTextChanged(Editable arg0) {


            }
        });

        company_store_help = (TextView) findViewById(R.id.company_store_help);
        SpannableString content = new SpannableString(company_store_help.getText().toString());
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        content.setSpan(new ForegroundColorSpan(Color.parseColor("#FFCC80")), 0, content.length(), 0);
        company_store_help.setText(content);
        //跳转到帮助界面
        company_store_help.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent helpIntent = new Intent(SelectDistribution.this, HelpActivity.class);
                helpIntent.putExtra("help", "distributor");
                startActivity(helpIntent);
            }
        });
    }


    /**
     * 适配ExpandListView
     *
     * @param menuGroups 零售下面有分销店的集合
     * @param isOpen     是否展开ExpandListView，全部数据时默认不展开，模糊查找时默认展开
     */
    public void initExpandListView(List<Retailer> menuGroups, boolean isOpen) {
        distributorCount = 0;
        for (Retailer retailer : menuGroups) {
            distributorCount += retailer.getStoreInforList().size();
        }
        adapter = new SelectAgentDistributorAdapter(mContext, menuGroups);
        elistview_distributor.setAdapter(adapter);
        if (isOpen) {
            //默认全部展开
            int groupCount = elistview_distributor.getCount();
            for (int i = 0; i < groupCount; i++) {
                elistview_distributor.expandGroup(i);
            }
        }
        tv_total.setText("（直营共 " + menuGroups.size() + " 条，分销共 " + distributorCount + " 条）");

    }

    /**
     * 跳转到分销发货界面
     *
     * @param storeInfor
     */
    public void goToOutStock(StoreInfor storeInfor) {
        selectedStoreInfor = storeInfor;
        Intent sureIntent = new Intent(mContext, SelectSureConfirm.class);
        sureIntent.putExtra("title", "确定选择直营分销店：" + selectedStoreInfor.getStoreName() + "？");
        startActivityForResult(sureIntent, Lic_SelectSure);
    }

    /**
     * 根据零售商集合查找分销店集合封装成适合适配器的格式
     *
     * @param rlist 零售商集合
     * @return
     */
    public List<Retailer> getAllDistributorByRetailer(List<Map<String, Object>> rlist) {
        List<Retailer> dlist = new ArrayList<Retailer>();
        List<StoreInfor> storeInforList = new ArrayList<StoreInfor>();
        List<Map<String, Object>> fList = new ArrayList<Map<String, Object>>();
        String TraderId, TraderName;
        String StoreId, StoreName, Link, Tel, CorpAddr;//分销店代号
        Retailer retailer;
        StoreInfor storeInfor;
        for (Map<String, Object> map : rlist) {

            TraderId = map.get("traderid").toString();
            TraderName = map.get("tradername").toString() +
                    "（" + map.get("link").toString() + ":" + map.get("tel").toString() + "）";

            String sql = "select * from agentinfor where traderid ='" + TraderId + "'";
            fList = SqliteDataHelper.getHelper(mContext).QueryDbList(sql, null);
            storeInforList = new ArrayList<StoreInfor>();
            for (Map<String, Object> map2 : fList) {
                StoreId = map2.get("storeid").toString();
                StoreName = map2.get("storename").toString();
                Link = map2.get("link").toString();
                Tel = map2.get("tel").toString();
                CorpAddr = map2.get("corpaddr").toString();
                storeInfor = new StoreInfor(StoreId, StoreName, Link,
                        Tel, CorpAddr, TraderId);
                storeInforList.add(storeInfor);
            }

            if (storeInforList.size() > 0) {
                retailer = new Retailer(TraderId, TraderName, storeInforList);
                dlist.add(retailer);
            }
        }

        return dlist;
    }

    /**
     * 查找分销店集合
     *
     * @param searchRetailList      搜索出的零售商集合
     * @param searchDistributorList 搜索出的分销店集合
     *                              由于需要适配的数据是零售下有分销的形式，所以查出符合条件的零售下面的所有分销店，并且加上符合条件的分销店集合，
     *                              根据分销店再查出零售商，最后的集合。
     * @return
     */
    public List<Retailer> getSearchDistributorList(List<Map<String, Object>> searchRetailList,
                                                   List<Map<String, Object>> searchDistributorList) {
        List<Retailer> searchRetailerList = new ArrayList<Retailer>();
        //先把符合条件的零售商下面的全部分销店加入集合，后面再加入符合条件的分销店
        searchRetailerList.addAll(getAllDistributorByRetailer(searchRetailList));
        List<Map<String, Object>> tempList = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> dmap : searchDistributorList) {
            for (Map<String, Object> rmap : searchRetailList) {
                //如果查出的分销店集合里面的数据在查出的零售商集合里面则添加进临时集合，后面删除
                //不能直接删除，否则会报错，要先加入临时集合再删除
                if (rmap.containsValue(dmap.get("traderid").toString())) {
                    tempList.add(dmap);
                    break;
                }
            }
        }
        //从符合条件的分销店集合里面删除既是符合条件的分销店，又是符合条件零售商下面的分销店的店
        searchDistributorList.removeAll(tempList);

        String StoreId, StoreName, Link, Tel, CorpAddr, TraderId,
                tempTraderId = "",//零售保存零售商代号，用来判断当前的这个和上一个是否是同一个零售商，不是就要新建。
                TraderName, sql;
        StoreInfor storeInfor;
        List<StoreInfor> storeInforList = new ArrayList<StoreInfor>();
        Map<String, String> rmap;
        Retailer retailer;
        for (Map<String, Object> dmap2 : searchDistributorList) {
            TraderId = dmap2.get("traderid").toString();
            StoreId = dmap2.get("storeid").toString();
            StoreName = dmap2.get("storename").toString();
            Link = dmap2.get("link").toString();
            Tel = dmap2.get("tel").toString();
            CorpAddr = dmap2.get("corpaddr").toString();
            //得出一个分销店的信息
            storeInfor = new StoreInfor(StoreId, StoreName, Link,
                    Tel, CorpAddr, TraderId);

            sql = "select * from newdirect where traderid = '" + TraderId + "'";
            rmap = SqliteDataHelper.getHelper(mContext).QueryDbMap(sql, null);
            TraderName = rmap.get("tradername").toString() +
                    "（" + rmap.get("link").toString() + ":" + rmap.get("tel").toString() + "）";
            //如果不是同一个零售商的分销店，就要重新建一个分销店集合
            if (!TraderId.equals(tempTraderId)) {
                storeInforList = new ArrayList<StoreInfor>();
            }
            //把分销店加入分销店集合
            storeInforList.add(storeInfor);
            //如果不是同一个零售商的就要重新建一个零售商
            if (!TraderId.equals(tempTraderId)) {
                retailer = new Retailer(TraderId, TraderName, storeInforList);
                searchRetailerList.add(retailer);
            }
            //零售保存零售商代号，用来判断当前的这个和上一个是否是同一个零售商，不是就要新建。
            tempTraderId = TraderId;
        }
        return searchRetailerList;
    }


    private class handShowMsg extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ShowMessage.HandShowMessage: // 显示错误提示显示
                    ShowMessage.Show(SelectDistribution.this, msg.obj.toString());
                    break;
                default:
                    // MyProgressDialog.close();
                    // ShowMessage.Show(Xundian.this,msg.obj.toString());
                    break;
            }
            super.handleMessage(msg);
        }
    }


    // item选择事件

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Lic_SelectSure:
                    Intent intent = null;
                    if (aim.equals("P_Dv_OutStock_Z_L_S_NoBill")) {
                        intent = new Intent(this, P_Dv_OutStock_Z_S_NoBill_InStock.class);
                    } else if (aim.equals("P_Dv_ReturnedPurchase_Z_S_NoBill")) {
                        intent = new Intent(this, SelectStock.class);
                        intent.putExtra("aim", "P_Dv_ReturnedPurchase_Z_S_NoBill");
                    }
                    intent.putExtra("selectedStoreInfor", selectedStoreInfor);
                    startActivity(intent);
                    break;
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        //查出全部的直营店
        String sql = "select * from newdirect";
        allRetailList = SqliteDataHelper.getHelper(mContext).QueryDbList(sql, null);
        //根据全部的零售商查出全部的分销店，封装成适合适配器的格式适配
        initExpandListView(getAllDistributorByRetailer(allRetailList), false);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
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
}
