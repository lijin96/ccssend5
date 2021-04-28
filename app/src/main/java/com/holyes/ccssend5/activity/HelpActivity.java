package com.holyes.ccssend5.activity;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.example.ccssend5.R;
import com.holyes.ccssend5.lib.AccessWeb;
import com.holyes.ccssend5.lib.ShowMessage;
import com.holyes.ccssend5.lib.SqliteDataHelper;
import com.holyes.ccssend5.lib.SysUserInfo;
import com.holyes.ccssend5.myview.Loading;
import com.holyes.ccssend5.utils.DisplayUtil;

import java.util.List;
import java.util.Map;

import uk.co.senab.photoview.PhotoView;

/**
 * @ClassName: HelpActivity
 * @Description: 帮助界面
 * @Author: lijin
 * @Date: 2021/3/6 14:10
 */
public class HelpActivity extends Activity implements View.OnClickListener {

    private Button btn_obtain;//重新获取数据
    private LinearLayout lin_problem;//点击跳转在线word界面
    private PhotoView photo_one, photo_two, photo_three, photo_four;

    private Handler hand;
    private SysUserInfo info;
    private Loading loading = null;
    private Thread DownloadThread;//下载对应数据

    //不同界面跳转传的参数
    private String tag;
    /**
     * 记录数的日期时间
     */
    private String maxDataTime;
    /**
     * 总共下载数
     */
    private int companycount = 0;
    /**
     * 当前下载数
     */
    private int companycurcount = 0;

    volatile boolean isPaused = true;// 暂停标志


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayUtil.setDefaultDisplay(this);
        setContentView(R.layout.activity_help);
        tag = getIntent().getExtras().getString("help");
        info = new SysUserInfo(this);
        hand = new handShowMsg();
        initView();
    }

    private void initView() {
        btn_obtain = (Button) findViewById(R.id.btn_obtain);
        btn_obtain.setOnClickListener(this);

        photo_one = (PhotoView) findViewById(R.id.ex_one);
        photo_two = (PhotoView) findViewById(R.id.ex_two);
        photo_three = (PhotoView) findViewById(R.id.ex_two);
        photo_four = (PhotoView) findViewById(R.id.ex_four);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_obtain:
                isPaused = false;
                DataRecapture();
                break;

            default:
                break;
        }
    }

    //重新获取客户数据
    public void DataRecapture() {
        if (tag.equals("companyD")) {
            //清除代理商表数据
            try {
                SqliteDataHelper.getHelper(this).execSQL("delete from newcompany");
            } catch (Exception e) {
                ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage, "删除失败");
                e.printStackTrace();
            }
        } else if (tag.equals("companyZy")) {
            //清除直营店表数据
            try {
                SqliteDataHelper.getHelper(this).execSQL("delete from newdirect");
            } catch (Exception e) {
                ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage, "删除失败");
                e.printStackTrace();
            }
        } else if (tag.equals("companyRetailer")) {
            //清除零售店表数据
            try {
                SqliteDataHelper.getHelper(this).execSQL("delete from newretail");
            } catch (Exception e) {
                ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage, "删除失败");
                e.printStackTrace();
            }
        } else if (tag.equals("companyStore")) {
            //清除分销店表数据
            try {
                SqliteDataHelper.getHelper(this).execSQL("delete from storeinfor");
            } catch (Exception e) {
                ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage, "删除失败");
                e.printStackTrace();
            }
        } else if (tag.equals("distributor")) {
            //清除分销店表数据
            try {
                SqliteDataHelper.getHelper(this).execSQL("delete from agentinfor");
            } catch (Exception e) {
                ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage, "删除失败");
                e.printStackTrace();
            }
        }
        loading = new Loading(HelpActivity.this, "正在下载...", new Loading.OnLoadingback() {
            @Override
            public void back(String name) {
                if (DownloadThread != null && DownloadThread.isAlive()) {
                    isPaused = true;
                    DownloadThread.interrupt();
                    ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage, "下载被中止了");
                    DownloadThread = null;
                }
            }
        });
        loading.Show();
        DownCompany_d();
    }

    private void DownCompany_d() {
        DownloadThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (tag.equals("companyD")) {
                        downCompany_d();
                    } else if (tag.equals("companyZy")) {
                        downCompany_zl();
                    } else if (tag.equals("companyRetailer")) {
                        downLoadTraderInfor();
                    } else if (tag.equals("companyStore")) {
                        downloadStoreInfor();
                    } else if (tag.equals("distributor")) {
                        downloadAgentInfor();
                    }
                    ShowMessage.ShowMsg(hand, ShowMessage.HandSuccess, "更新成功");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        DownloadThread.start();
    }


    private class handShowMsg extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ShowMessage.HandShowMessage: // 显示错误提示显示
                    ShowMessage.Show(HelpActivity.this, msg.obj.toString());
                    break;
                case ShowMessage.HandFailed:
                    ShowMessage.Show(HelpActivity.this, msg.obj.toString());
                    break;
                case ShowMessage.HandCloseLoading:
                    loading.Close();
                    break;
                case 5:
                    loading.setTipText(msg.obj.toString());
                    break;
                case 6:
                    loading.SetProgressValue(Integer.parseInt(msg.obj.toString()));
                    break;
                case ShowMessage.HandSuccess: // 登录成功
                    if (loading != null)
                        loading.Close();
                    ShowMessage.Show(HelpActivity.this, msg.obj.toString());
                    break;

            }
            super.handleMessage(msg);
        }

    }

    /**
     * 代理商数据下载
     */
    private void downCompany_d() throws Exception {
        maxDataTime = getMaxUprecndate("newcompany");
        companycount = Integer.parseInt(AccessWeb.getHelper(getApplicationContext()).GetDownLoadAgentRecord(maxDataTime));
        companycurcount = 0;
        setProgressBarMax(companycount);
        List<Map<String, Object>> map;
        String AgentId, AgentName, Link, Tel, CorpAddr, Uprecndate;
        for (maxDataTime = getMaxUprecndate("newcompany"); companycurcount < companycount; maxDataTime = getMaxUprecndate("newcompany")) {

            map = AccessWeb.getHelper(getApplicationContext()).GetDownLoadAgentInfor(maxDataTime);
            if (map.size() == 0) {
                return;
            }
            for (Map<String, Object> map2 : map) {
                if (isPaused)
                    return;

                AgentId = (String) map2.get("AgentId");
                AgentName = (String) map2.get("AgentName");
                Link = (String) map2.get("Link");
                Tel = (String) map2.get("Tel");
                CorpAddr = (String) map2.get("CorpAddr");
                Uprecndate = (String) map2.get("Uprecndate");

                String sqlInsert = "insert into newcompany("
                        + "agentid,agentname,link,tel,corpaddr,uprecndate)"
                        + " values(?,?,?,?,?,?)";
                String sqlUpdate = "update newcompany "
                        + "set agentname=?,link=?,tel=?,corpaddr=?,uprecndate=? where agentid=? ";
                try {

                    String rest = "";

                    rest = SqliteDataHelper.getHelper(getApplicationContext()).execSQLString(
                            "select agentid from newcompany where agentid=?",
                            new String[]{AgentId});
                    if (rest == "") {
                        SqliteDataHelper.getHelper(getApplicationContext()).execSQL(sqlInsert,
                                new String[]{AgentId, AgentName, Link, Tel, CorpAddr, Uprecndate});
                    } else {
                        SqliteDataHelper.getHelper(getApplicationContext()).execSQL(sqlUpdate,
                                new String[]{AgentName, Link, Tel, CorpAddr, Uprecndate, AgentId});
                    }

                    setProgressBarValue(companycurcount++);
                    showTip("下载代理商资料..." + companycurcount + "/" + companycount);
                } catch (Exception e) {
                    throw new Exception("新增或修改数据错误代理商资料：" + e.getMessage());
                }
            }
        }
    }


    /**
     * 直营店数据下载
     */
    private void downCompany_zl() throws Exception {
        maxDataTime = getMaxUprecndate("newdirect");
        companycount = Integer.parseInt(AccessWeb.getHelper(getApplicationContext()).GetDownLoadDirectRecord(maxDataTime));
        companycurcount = 0;
        setProgressBarMax(companycount);
        List<Map<String, Object>> map;
        String TraderId, TraderName, Link, Tel, CorpAddr, ProviceName, CityName, AgentId, AgentName, Uprecndate;
        for (maxDataTime = getMaxUprecndate("newdirect"); companycurcount < companycount; maxDataTime = getMaxUprecndate("newdirect")) {
            map = AccessWeb.getHelper(getApplicationContext()).GetDownLoadDirectInfor(maxDataTime);
            if (map.size() == 0) {
                return;
            }
            for (Map<String, Object> map2 : map) {
                if (isPaused)
                    return;

                TraderId = (String) map2.get("TraderId");
                TraderName = (String) map2.get("TraderName");
                Link = (String) map2.get("Link");
                Tel = (String) map2.get("Tel");
                CorpAddr = (String) map2.get("CorpAddr");
                ProviceName = (String) map2.get("ProviceName");
                CityName = (String) map2.get("CityName");
                AgentId = (String) map2.get("AgentId");
                AgentName = (String) map2.get("AgentName");
                Uprecndate = (String) map2.get("Uprecndate");

                String sqlInsert = "insert into newdirect("
                        + "traderid,tradername,link,tel,corpaddr,provicename,cityname,agentid,agentname,uprecndate)"
                        + " values(?,?,?,?,?,?,?,?,?,?)";
                String sqlUpdate = "update newdirect "
                        + " set tradername =?,link=?,tel=?,corpaddr=?,provicename=?,cityname=?,agentid=?,agentname=?," +
                        "uprecndate=? where traderid=? ";
                try {
                    String rest = "";

                    rest = SqliteDataHelper.getHelper(getApplicationContext()).execSQLString("select traderid from newdirect where traderid=?",
                            new String[]{TraderId});
                    if (rest == "") {
                        SqliteDataHelper.getHelper(getApplicationContext()).execSQL(sqlInsert,
                                new String[]{TraderId, TraderName, Link, Tel, CorpAddr,
                                        ProviceName, CityName, AgentId, AgentName, Uprecndate});
                    } else {
                        SqliteDataHelper.getHelper(getApplicationContext()).execSQL(sqlUpdate,
                                new String[]{TraderName, Link, Tel, CorpAddr,
                                        ProviceName, CityName, AgentId, AgentName, Uprecndate, TraderId});
                    }

                    setProgressBarValue(companycurcount++);
                    showTip("下载直营店资料..." + companycurcount + "/" + companycount);
                } catch (Exception e) {
                    throw new Exception("新增或修改数据错误直营店资料：" + e.getMessage());
                }
            }
        }
    }

    /**
     * 零售店资料
     */
    private void downLoadTraderInfor() throws Exception {
        maxDataTime = getLSMaxUprecndate("newretail", info.getCompanyid());
        companycurcount = 0;
        companycount = Integer.parseInt(AccessWeb.getHelper(getApplicationContext()).GetDownLoadTraderRecord(maxDataTime));
        setProgressBarMax(companycount);
        String TraderId, TraderName, Link, Tel, CorpAddr, ProviceName, CityName, AgentId, AgentName, Uprecndate;
        List<Map<String, Object>> map;
        for (maxDataTime = getLSMaxUprecndate("newretail", info.getCompanyid()); companycurcount < companycount; maxDataTime = getLSMaxUprecndate("newretail", info.getCompanyid())) {
            map = AccessWeb.getHelper(getApplicationContext()).GetDownLoadTraderInfor(maxDataTime);
            if (map.size() == 0) {
                return;
            }
            for (Map<String, Object> map2 : map) {
                if (isPaused)
                    return;

                TraderId = (String) map2.get("TraderId");
                TraderName = (String) map2.get("TraderName");
                Link = (String) map2.get("Link");
                Tel = (String) map2.get("Tel");
                CorpAddr = (String) map2.get("CorpAddr");
                ProviceName = (String) map2.get("ProviceName");
                CityName = (String) map2.get("CityName");
                AgentId = (String) map2.get("AgentId");
                AgentName = (String) map2.get("AgentName");
                Uprecndate = (String) map2.get("Uprecndate");

                String sqlInsert = "insert into newretail("
                        + "traderid,tradername,link,tel,corpaddr,provicename,cityname,agentid,agentname,uprecndate)"
                        + " values(?,?,?,?,?,?,?,?,?,?)";
                String sqlUpdate = "update newretail "
                        + "set tradername =?,link=?,tel=?,corpaddr=?,provicename=?,cityname=?,agentid=?,agentname=?," +
                        "uprecndate=? where traderid=? ";
                try {
                    String rest = "";
                    rest = SqliteDataHelper.getHelper(getApplicationContext()).execSQLString("select traderid from newretail where traderid=?",
                            new String[]{TraderId});
                    if (rest == "") {
                        SqliteDataHelper.getHelper(getApplicationContext()).execSQL(sqlInsert,
                                new String[]{TraderId, TraderName, Link, Tel, CorpAddr, ProviceName,
                                        CityName, AgentId, AgentName, Uprecndate});
                    } else {
                        SqliteDataHelper.getHelper(getApplicationContext()).execSQL(sqlUpdate,
                                new String[]{TraderName, Link, Tel, CorpAddr, ProviceName,
                                        CityName, AgentId, AgentName, Uprecndate, TraderId});
                    }

                    setProgressBarValue(companycurcount++);
                    showTip("下载零售店资料..." + companycurcount + "/" + companycount);
                } catch (Exception e) {
                    throw new Exception("新增或修改数据错误零售店资料：" + e.getMessage());
                }
            }

        }

    }

    /**
     * 下载分销店资料
     *
     * @throws Exception
     * @throws NumberFormatException
     */
    public void downloadStoreInfor() throws Exception {
        maxDataTime = getMaxUprecndate("storeinfor");
        companycurcount = 0;
        companycount = Integer.parseInt(AccessWeb.getHelper(getApplicationContext()).GetDownLoadStoreRecord(maxDataTime));
        setProgressBarMax(companycount);
        String StoreId, StoreName, Link, Tel, CorpAddr, TraderId, Uprecndate;
        List<Map<String, Object>> map;
        for (maxDataTime = getMaxUprecndate("storeinfor"); companycurcount < companycount; maxDataTime = getMaxUprecndate("storeinfor")) {
            map = AccessWeb.getHelper(this).GetDownLoadStoreInfor(maxDataTime);
            if (map.size() == 0) {
                return;
            }
            for (Map<String, Object> map2 : map) {
                if (isPaused)
                    return;

                StoreId = (String) map2.get("StoreId");
                StoreName = (String) map2.get("StoreName");
                Link = (String) map2.get("Link");
                Tel = (String) map2.get("Tel");
                CorpAddr = (String) map2.get("CorpAddr");
                TraderId = (String) map2.get("TraderId");
                Uprecndate = (String) map2.get("Uprecndate");

                String sqlInsert = "insert into storeinfor("
                        + "storeid,storename,link,tel,corpaddr,traderid,uprecndate)"
                        + " values(?,?,?,?,?,?,?)";
                String sqlUpdate = "update storeinfor "
                        + "set storename =?,link=?,tel=?,corpaddr=?,traderid=?," +
                        "uprecndate=? where storeid=? ";
                try {
                    String rest = "";
                    rest = SqliteDataHelper.getHelper(getApplicationContext()).execSQLString("select storeid from storeinfor where storeid=?",
                            new String[]{StoreId});
                    if (rest == "") {
                        SqliteDataHelper.getHelper(getApplicationContext()).execSQL(sqlInsert,
                                new String[]{StoreId, StoreName, Link, Tel, CorpAddr, TraderId, Uprecndate});
                    } else {
                        SqliteDataHelper.getHelper(getApplicationContext()).execSQL(sqlUpdate,
                                new String[]{StoreName, Link, Tel, CorpAddr, TraderId, Uprecndate, StoreId});
                    }

                    setProgressBarValue(companycurcount++);
                    showTip("下载分销店资料..." + companycurcount + "/" + companycount);
                } catch (Exception e) {
                    throw new Exception("新增或修改数据错误分销店资料：" + e.getMessage());
                }
            }
        }
    }


    /**
     * 下载直营分销店资料
     *
     * @throws Exception
     * @throws NumberFormatException
     */
    public void downloadAgentInfor() throws Exception {
        maxDataTime = getMaxUprecndate("agentinfor");
        companycurcount = 0;
        companycount = Integer.parseInt(AccessWeb.getHelper(getApplicationContext()).GetDownLoadStraightShopRecord(maxDataTime));
        setProgressBarMax(companycount);
        String StoreId, StoreName, Link, Tel, CorpAddr, TraderId, Uprecndate;
        List<Map<String, Object>> map;
        for (maxDataTime = getMaxUprecndate("agentinfor"); companycurcount < companycount; maxDataTime = getMaxUprecndate("storeinfor")) {
            map = AccessWeb.getHelper(this).GetDownLoadStraightShopInfor(maxDataTime);
            if (map.size() == 0) {
                return;
            }
            for (Map<String, Object> map2 : map) {
                if (isPaused)
                    return;

                StoreId = (String) map2.get("StoreId");
                StoreName = (String) map2.get("StoreName");
                Link = (String) map2.get("Link");
                Tel = (String) map2.get("Tel");
                CorpAddr = (String) map2.get("CorpAddr");
                TraderId = (String) map2.get("TraderId");
                Uprecndate = (String) map2.get("Uprecndate");

                String sqlInsert = "insert into agentinfor("
                        + "storeid,storename,link,tel,corpaddr,traderid,uprecndate)"
                        + " values(?,?,?,?,?,?,?)";
                String sqlUpdate = "update agentinfor "
                        + "set storename =?,link=?,tel=?,corpaddr=?,traderid=?," +
                        "uprecndate=? where storeid=? ";
                try {
                    String rest = "";
                    rest = SqliteDataHelper.getHelper(getApplicationContext()).execSQLString("select storeid from agentinfor where storeid=?",
                            new String[]{StoreId});
                    if (rest == "") {
                        SqliteDataHelper.getHelper(getApplicationContext()).execSQL(sqlInsert,
                                new String[]{StoreId, StoreName, Link, Tel, CorpAddr, TraderId, Uprecndate});
                    } else {
                        SqliteDataHelper.getHelper(getApplicationContext()).execSQL(sqlUpdate,
                                new String[]{StoreName, Link, Tel, CorpAddr, TraderId, Uprecndate, StoreId});
                    }

                    setProgressBarValue(companycurcount++);
                    showTip("下载直营分销店资料..." + companycurcount + "/" + companycount);
                } catch (Exception e) {
                    throw new Exception("新增或修改数据错误直营分销店资料：" + e.getMessage());
                }
            }
        }
    }

    private void showTip(String msg) {
        ShowMessage.ShowMsg(hand, 5, msg);
    }

    private void setProgressBarValue(int value) {
        ShowMessage.ShowMsg(hand, 6, String.valueOf(value));
    }

    private void setProgressBarMax(int value) {
        ShowMessage.ShowMsg(hand, ShowMessage.HandInitProgress, String.valueOf(value));
    }

    /**
     * 获取最大的uprecndate
     *
     * @param table 表名
     * @return
     */
    public String getMaxUprecndate(String table) {
        String maxUprecndate = "";
        try {

            String sqlSelect = String.format(
                    "select max(uprecndate) from %1$s", table);
            Cursor cursor = SqliteDataHelper.getHelper(getApplicationContext())
                    .getCursor(sqlSelect);
            cursor.moveToFirst();
            if (cursor != null && cursor.getCount() > 0) {
                maxUprecndate = cursor.getString(0);
            }
            if (null != cursor) {
                cursor.close();
                cursor = null;
            }
            if (maxUprecndate == null) {
                maxUprecndate = "";
            }
        } catch (Exception e) {
            e.printStackTrace();
            maxUprecndate = "";
        }
        return maxUprecndate;
    }

    public String getLSMaxUprecndate(String table, String id) {
        String maxUprecndate = "";
        try {

            String sqlSelect = String.format(
                    "select max(uprecndate) uprecndate from %1$s where agentid ='%2$s'", table, id);
            Cursor cursor = SqliteDataHelper.getHelper(getApplicationContext())
                    .getCursor(sqlSelect);
            cursor.moveToFirst();
            if (cursor != null && cursor.getCount() > 0) {
                maxUprecndate = cursor.getString(0);
            }
            if (null != cursor) {
                cursor.close();
                cursor = null;
            }
            if (maxUprecndate == null) {
                maxUprecndate = "";
            }
        } catch (Exception e) {
            e.printStackTrace();
            maxUprecndate = "";
        }
        return maxUprecndate;
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

