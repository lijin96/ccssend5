package com.holyes.ccssend5.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Xml;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.ccssend5.R;
import com.holyes.agent.activity.DMainActivity;
import com.holyes.ccssend5.entity.UpdateInfo;
import com.holyes.ccssend5.entity.User;
import com.holyes.ccssend5.lib.ADevicesManager;
import com.holyes.ccssend5.lib.AccessWeb;
import com.holyes.ccssend5.lib.GetNetStateRunnable;
import com.holyes.ccssend5.lib.MySound;
import com.holyes.ccssend5.lib.ShowMessage;
import com.holyes.ccssend5.lib.SqliteDataHelper;
import com.holyes.ccssend5.lib.SysUserInfo;
import com.holyes.ccssend5.lib.UpdateManager;
import com.holyes.ccssend5.myhandler.BaseHandler;
import com.holyes.ccssend5.myhandler.UserLoginHandler;
import com.holyes.ccssend5.myview.Loading;
import com.holyes.ccssend5.utils.DisplayUtil;
import com.holyes.ccssend5.utils.SomeUtils;
import com.holyes.factoryscan.activity.FactoryMainActivity;
import com.holyes.headquarter.activity.MainActivity;

import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * @ClassName: LoginActivity
 * @Description: 登录界面
 * @Author: lijin
 * @Date: 2021/3/6 14:11
 */
public class LoginActivity extends Activity {
    private Context mContext;
    private Loading loading = null;
    private SysUserInfo sysUserInfo;
    private Thread loginThread;
    private Handler hand;
    private GetNetStateRunnable GetNetRun;

    private EditText et_username, et_password;
    private TextView tv_versionCode, tv_brand;//版本号,品牌
    private CheckBox checkBox;//是否记住密码
    private LinearLayout lin_login, lin_setting, lin_update, lin_evaluate;
    private TextView btn_login, btn_setting, btn_update, btn_evaluate;
    private ImageView ivNet;

    private String businessid = "";

    /**
     * 记录数的日期时间
     */
    private String maxDataTime;
    /**
     * 当前下载批数
     */
    private int curcount = 0;
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

        //用来判断当前Activity是否是第一个activity
        if (!this.isTaskRoot()) {
            Intent mainIntent = getIntent();
            String action = mainIntent.getAction();
            if (mainIntent.hasCategory(Intent.CATEGORY_LAUNCHER) && action.equals(Intent.ACTION_MAIN)) {
                finish();
                return;
            }
        }

        //设置窗体始终点亮
        this.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        DisplayUtil.setDefaultDisplay(this);


        setContentView(R.layout.login);
        mContext = this;
        sysUserInfo = new SysUserInfo(getApplicationContext());
        hand = new handShowMsg();

        MySound.getMySound(this);
//        sound=MySound.getMySound(mContext);

        et_username = (EditText) findViewById(R.id.et_username);
        et_password = (EditText) findViewById(R.id.et_password);
        checkBox = (CheckBox) findViewById(R.id.logincheckBox);
        tv_versionCode = (TextView) findViewById(R.id.tv_versionCode);
        tv_brand = ((TextView) findViewById(R.id.texBrand));
        lin_login = (LinearLayout) findViewById(R.id.lin_login);
        btn_login = (TextView) findViewById(R.id.btn_login);
        lin_setting = (LinearLayout) findViewById(R.id.lin_setting);
        btn_setting = (TextView) findViewById(R.id.btn_setting);
        lin_update = (LinearLayout) findViewById(R.id.lin_update);
        btn_update = (TextView) findViewById(R.id.btn_update);
        ivNet = (ImageView) findViewById(R.id.imageView1);

        // sysUserInfo.setCompanyid("HOLYES_TEST");//用于测试
        if (sysUserInfo.getIfrember()) {
            checkBox.setChecked(sysUserInfo.getIfrember());
            et_username.setText(sysUserInfo.getMobile());
            SomeUtils.moveFocus(et_username);
            et_password.setText(sysUserInfo.getLoginpwd());
            SomeUtils.moveFocus(et_password);
        }
        tv_brand.setText(sysUserInfo.getBrand());

        String pServerip = sysUserInfo.getServerip();
        if (!pServerip.isEmpty()) {
            btn_login.setText("登录");
        } else {
            btn_login.setEnabled(false);
        }

        //代理商模式
        if (!ADevicesManager.CorpCompany) //代理商模式
        {
            businessid = getIntent().getStringExtra("businessid");

            //如果不是同一个品牌且不是从品牌选择界面跳转过来的（Clientid和保存的不一致）就删除数据
            //			if(!StringUtils.isEmpty(businessid) && !businessid.equals(sysUserInfo.getLastLoginBusinessid()))
            //			{
            //				if(!SqliteDataHelper.getHelper(getApplicationContext()).CleanDataBase())
            //				{
            //					ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage,"删除数据失败");
            //				}
            //			}
        } else //总公司
        {
            tv_brand.setVisibility(View.GONE);
            ADevicesManager.SetKey(false);
        }

//        String Testresult="true;V17";
////           Boolean.parseBoolean(Testresult.split(";")[0]);
//        Log.d("main", Testresult.split(";")[0]);
//        Log.d("main", Testresult.split(";")[1]);


        //
        // 登录
        lin_login.setOnClickListener(new BtnLoginClick());

        // 修改域名
        lin_setting.setOnClickListener(new BtnSettingClick());

        // 升级
        lin_update.setOnClickListener(new BtnUpdateClick());

        ivNet.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                SomeUtils.goToSetting(LoginActivity.this);
            }
        });
        tv_versionCode.setText(SomeUtils.getSoftVer(this));

        lin_evaluate = (LinearLayout) findViewById(R.id.lin_evaluate);
        lin_evaluate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent webIntent = new Intent(mContext, WebActivity.class);
                startActivity(webIntent);
            }
        });
//		Log.d("main", "获取手机型号"+StringUtils.getSystemModel());
//		Log.d("main", "获取手机厂商"+StringUtils.getDeviceBrand());


    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onRestart() {
        isPaused = true;
        super.onRestart();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case 0: //点击关于，点击维护成功后
                if (ADevicesManager.CorpCompany) {
                    ADevicesManager.SetKey(true);
                }
                stopCheckNetState();
                finish();

                break;
            case 1:
                Intent intent = new Intent(LoginActivity.this, SysSettingActivity.class);
                startActivity(intent);
                break;
            case 2:// 从main主界面中返回
                GetNetRun.State = true;
                break;
            case 3:
                break;
        }
    }

    private class handShowMsg extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ShowMessage.HandShowMessage: // 显示错误提示显示
                    if (loading != null) loading.Close();
                    if (msg.obj.toString().equals("供应商登录")){
                        Intent intent = new Intent(LoginActivity.this, FactoryMainActivity.class);
                        startActivity(intent);
                    }else {
                        ShowMessage.Show(LoginActivity.this, msg.obj.toString());
                    }
                    break;
                case ShowMessage.HandFailed:
                    if (loading != null)
                        loading.Close();
                    ShowMessage.MessageBox(LoginActivity.this, "温馨提示", "登录异常，请检查网络是否可用",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    setnet();
                                }
                            });
                    break;
                case ShowMessage.HandSuccess: // 登录成功

//                    Log.d("mian-version", sysUserInfo.getAgentVersionNum());
                    if (loading != null)
                        loading.Close();
                    //登录成功之后设置品牌id
                    sysUserInfo.setLastLoginBusinessid(businessid);
                    //				stopCheckNetState();// 停止网络测试

                    if (sysUserInfo.getSoftType().equals("52")) {
                        Intent intent = new Intent(LoginActivity.this, DMainActivity.class);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                    break;
                case ShowMessage.HandCloseLoading:
                    loading.Close();
                    break;
                case ShowMessage.HandInitProgress:
                    loading.initProgress(Integer.parseInt(msg.obj.toString()));
                    break;
                case 5:
                    loading.setTipText(msg.obj.toString());
                    break;
                case 6:
                    loading.SetProgressValue(Integer.parseInt(msg.obj.toString()));
                    break;
                case 7:// 有更新版本
                    // btn_update.setTextColor(Color.RED);
                    // txtUpdate= new BadgeView(login.this,btn_update);
                    // txtUpdate.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
                    // txtUpdate.setText("1");
                    // txtUpdate.show();
                    // sysUserInfo.setUpdate(true);
                    // ShowMessage.MessageBox(login.this,"您好，本次软件有更新，请升级。并点击升级按钮进行升级");
                    // UpdateManager mUpdateManager = new
                    // UpdateManager(login.this);
                    // mUpdateManager.checkUpdateInfo();
                    break;
                case 8:
                    loading.Close();
                    btn_login.setText("登录");
                    ShowMessage.Show(LoginActivity.this, "注册成功,您现在可以登录了！");
                    break;

                case 9: // 更新版本信息
                    tv_versionCode.setText(msg.obj.toString());
                    break;
                case 10:// 强制升级 ：升级提示，按钮不可用
                    showNoticeDialog("软件版本更新");
                    break;
                case 11:// offline
                    ((ImageView) findViewById(R.id.imageView1))
                            .setImageResource(R.drawable.link_state_off);
                    break;
                case 12:// oneline
                    ((ImageView) findViewById(R.id.imageView1))
                            .setImageResource(R.drawable.link_state_on);
                    break;
                case 13:// oneline
                    loading.Close();
                    Toast.makeText(LoginActivity.this, "当前为总公司模式，代理商账号无法登录", Toast.LENGTH_LONG).show();
                    break;
                case 14:// oneline
                    loading.Close();
                    break;
                default:
                    // MyProgressDialog.close();
                    // ShowMessage.Show(Xundian.this,msg.obj.toString());
                    break;
            }
            super.handleMessage(msg);
        }

    }

    /**
     * 弹出升级提示对话框，询问升级
     */
    private void showNoticeDialog(final String title) {
        sysUserInfo.setUpdate(true);
        final UpdateManager mUpdateManager = new UpdateManager(LoginActivity.this);
        SysUserInfo sysinfo = new SysUserInfo(mContext);
        new AlertDialog.Builder(mContext)
                .setTitle(title)
                .setMessage(sysinfo.getUpdateMsg())
                .setPositiveButton("下载", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                        mUpdateManager.showDownloadDialog(title);
                        loading.Close();
                    }
                })
                .setNegativeButton("以后再说", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                        loading.Show();
                        loginThread = new Thread(new LoginThread());
                        loginThread.start();
                    }
                })
                .show();
    }


    private class BtnLoginClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            // 更新webservice URL

            AccessWeb.getHelper(getApplicationContext()).updateurl();
            curcount = 0;
            companycurcount = 0;
            isPaused = false;

            if (et_username.getText().toString().isEmpty()) {
                Toast.makeText(LoginActivity.this, "请输入账号！", Toast.LENGTH_SHORT)
                        .show();
                et_password.findFocus();
                return;
            }
            if (et_password.getText().toString().isEmpty()) {

                Toast.makeText(LoginActivity.this, "请输入用户密码！", Toast.LENGTH_SHORT).show();
                btn_login.findFocus();
                return;
            }

            loading = new Loading(LoginActivity.this, "正在下载...", new Loading.OnLoadingback() {
                @Override
                public void back(String name) {
                    if (loginThread != null && loginThread.isAlive()) {
                        isPaused = true;
                        loginThread.interrupt();
                        ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage, "登录被终止了");
                        loginThread = null;//>>>
                    }
                }
            });
            loading.Show();
            Thread cUpdateThread = new Thread(new CheckUpdateThread());
            cUpdateThread.start();

        }
    }

    private class BtnUpdateClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(LoginActivity.this, AboutActivity.class);
            startActivityForResult(intent, 0);
        }
    }


    private class BtnSettingClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(LoginActivity.this, PasswordActivity.class);
            intent.putExtra("password", "29822832");
            startActivityForResult(intent, 1);
        }
    }

    /**
     * 检测软件版本是否有可以升级的版本
     *
     * @return true:可升级 ；false：不可升级
     */
    private boolean IsNewSoftVer() {
        try {
            URL aURL = new URL(sysUserInfo.getUploadSoftUrl());
            HttpURLConnection conn = (HttpURLConnection) aURL.openConnection();
            conn.setConnectTimeout(5000);

            InputStream is = conn.getInputStream();
            UpdateInfo updateInfo = new UpdateInfo();
            updateInfo = getUpdataInfo(is);
            sysUserInfo.setUpdateMsg(updateInfo.getDescription());
            sysUserInfo.setApkUrl(updateInfo.getUrl());

            if (sysUserInfo.getBrand() == "") {
                sysUserInfo.setBrand(updateInfo.getBrand());
            }

            if (Integer.valueOf(updateInfo.getVersion()).intValue() > getVersionCode()) {
                return true;
            } else {
                sysUserInfo.setUpdate(false);
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
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
     * 检查版本信息线程
     */
    public class CheckUpdateThread implements Runnable {
        @Override
        public void run() {
            showTip("检测版本信息...");

            //检测版本升级
            if (IsNewSoftVer()) {
                sysUserInfo.setUpdate(true);
            }

            loginThread = new Thread(new LoginThread());
            loginThread.start();

        }

    }

    /**
     * 登录线程
     */
    private class LoginThread implements Runnable {
        @Override
        public void run() {
            try {
                showTip("创建数据库...");
                SqliteDataHelper.getHelper(getApplicationContext()).initDatabase();
                if (isPaused)
                    return;
                //				showTip("获取服务器时间...");
                //				checkSystemDatetime();
                showTip("验证用户身份...");
                if (sysUserInfo.getSoftType().equals("52")) {
//                    if (businessid == null || businessid.isEmpty()) {
//                        businessid = sysUserInfo.getEnterpriseId();
//                        if (businessid == null || businessid.isEmpty()) {
//                            businessid = "00";//测试
//                        }
//                        }
//                    }
//                    businessid="00";
                    String tEnterpriseId = sysUserInfo.getEnterpriseId();
//                    Log.d("main", tEnterpriseId);
                    String agentVersionNum = AccessWeb.getHelper(mContext).ReadBrand(tEnterpriseId);
//                    Log.d("main", agentVersionNum);
                    JSONObject jsonObject = new JSONObject(agentVersionNum);
                    String tVerNo = jsonObject.optString("VerNo");
                    if (tVerNo.equals("CCS7")){
                        //如果是新版本的CCS就更新域名和端口号
                        SqliteDataHelper.getHelper(getApplicationContext()).execSQL(String.format("update brandinfo set  serverccip =  '%1$s',port = '%2$s'" +" where businessid = '%3$s' ",jsonObject.optString("ServerCcIp"),jsonObject.optString("Port"),businessid));

                        sysUserInfo.setServerIp( jsonObject.optString("ServerCcIp"));
                        sysUserInfo.setServerport(jsonObject.optString("Port"));
                    }
                    sysUserInfo.setAgentVersionNum(tVerNo);
                }

                if (isPaused)
                    return;
                if (!userlogin()) {
                    return;
                }
                GetFirstDeliveryStatus();
                if (!sysUserInfo.getSoftType().equals("52")) {
                    showTip("下载菜单功能...");
                    if (isPaused)
                        return;

                    downloadMenus();
                    if (isPaused)
                        return;

                    setProgressBarMax(0);
                    // 下载供应商资料，
                    showTip("下载供应商资料...");
                    if (isPaused)
                        return;
                    downloadSupplier();

                    if (isPaused)
                        return;
                    // 下载代理商资料
                    setProgressBarMax(0);
                    setProgressBarValue(0);
                    showTip("下载代理商资料...");
                    downCompany_d();

                    if (isPaused)
                        return;
                    // 下载直营店资料
                    setProgressBarMax(0);
                    setProgressBarValue(0);
                    showTip("下载直营店资料...");
                    downCompany_zl();

                    if (isPaused)
                        return;
                    // 下载直营分销店资料
                    setProgressBarMax(0);
                    setProgressBarValue(0);
                    showTip("下载直营分销店资料...");
                    downloadAgentInfor();

                    if (isPaused)
                        return;
                    // 下载仓库资料
                    setProgressBarMax(0);
                    setProgressBarValue(0);
                    showTip("下载仓库资料...");
                    downStock();
                    if (isPaused)
                        return;
//                     下载更新产品资料
                    setProgressBarMax(0);
                    setProgressBarValue(0);
                    showTip("下载更新产品资料...");
                    downProduct();


                } else {
                    //代理商模式，下载零售商资料
                    showTip("下载零售店资料...");
                    downLoadTraderInfor();
                    //改变是否是新版本的，暂用来判断代理商模式是否要添加发货撤销退货撤销功能
                    //品牌代号除了海伦的之外其他的不下载分销店资料
                    //if("88".equals(businessid)||"00".equals(businessid))
                    //{
                    //下载分销店资料
                    showTip("下载分销店资料...");
                    downloadStoreInfor();
                    //}
                    if (sysUserInfo.getMode().equals("合作商")){
                        // 下载更新产品资料
                        setProgressBarMax(0);
                        setProgressBarValue(0);
                        showTip("下载更新产品资料...");
                        downProduct();
                    }
                }

                if (!isPaused) // 正常
                {
                    ShowMessage.ShowMsg(hand, ShowMessage.HandSuccess, "success");
                } else // 线程被停止a
                {
                    ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage, "登录被终止了");
                }
            } catch (Exception e) {
                ShowMessage.ShowMsg(hand, "登录错误" + e.getMessage());
            }
        }
    }

    /**
     * 下载更新产品资料
     *
     * @throws Exception
     * @version 创建时间：2016-12-22 下午3:14:39
     */
    private void downProduct() throws Exception {
        maxDataTime = getMaxUprecndate("newproduct");
        companycount = Integer.parseInt(AccessWeb.getHelper(getApplicationContext()).GetDownLoadGoodsRecord(maxDataTime));
//        Log.i("main", "下载产品---最大时间："+maxDataTime+"----这次下载的数量="+companycount);
        companycurcount = 0;
        setProgressBarMax(companycount);

        List<Map<String, Object>> map;
        String BrandName, GoodsId, GoodsDescription, Modelm, Colors, GoodsYear, ProdType, Uprecndate;
        for (maxDataTime = getMaxUprecndate("newproduct"); companycurcount < companycount; maxDataTime = getMaxUprecndate("newproduct")) {
            map = AccessWeb.getHelper(getApplicationContext()).GetDownLoadGoodsInfor(maxDataTime);

            if (map.size() == 0) {
                return;
            }
            for (Map<String, Object> map2 : map) {
                if (isPaused)
                    return;
                BrandName = (String) map2.get("BrandName");
                GoodsId = (String) map2.get("GoodsId");
                GoodsDescription = (String) map2.get("GoodsDescription");
                Modelm = (String) map2.get("Modelm");
                Colors = (String) map2.get("Colors");
                GoodsYear = (String) map2.get("GoodsYear");
                ProdType = (String) map2.get("ProdType");
                Uprecndate = (String) map2.get("Uprecndate");

//                Log.i("main", "下载产品时间："+Uprecndate);

                String sqlInsert = "insert into newproduct("
                        + "brandname, goodsid , goodsdescription, modelm ,colors,productyear, prodtype ,uprecndate)"
                        + " values(?,?,?,?,?,?,?,?)";
                String sqlUpdate = "update newproduct "
                        + "set goodsdescription =?,brandname=?,modelm = ? ,colors = ?,productyear = ?,prodtype = ?, uprecndate = ? where goodsid=? ";
                try {
                    String rest = "";
                    rest = SqliteDataHelper.getHelper(getApplicationContext()).execSQLString("select goodsid from newproduct where goodsid=?", new String[]{GoodsId});
                    if (rest == "") {
                        SqliteDataHelper.getHelper(getApplicationContext()).execSQL(sqlInsert,
                                new String[]{BrandName, GoodsId, GoodsDescription, Modelm, Colors,
                                        GoodsYear, ProdType, Uprecndate});
                    } else {
                        SqliteDataHelper.getHelper(getApplicationContext()).execSQL(sqlUpdate,
                                new String[]{GoodsDescription, BrandName, Modelm, Colors,
                                        GoodsYear, ProdType, Uprecndate, GoodsId});
                    }

                    setProgressBarValue(companycurcount++);
                    showTip("下载产品资料..." + companycurcount + "/" + companycount);
                } catch (Exception e) {
                    throw new Exception("新增或修改产品错误：" + e.getMessage());
                }

            }
        }

        //		Log.i("main", "下载了---"+companycurcount+"条");
    }


    /**
     * 零售店资料
     */
    private void downLoadTraderInfor() throws Exception {
        maxDataTime = getLSMaxUprecndate("newretail", sysUserInfo.getCompanyid());
//        Log.d("main",maxDataTime);
        companycurcount = 0;
        companycount = Integer.parseInt(AccessWeb.getHelper(getApplicationContext()).GetDownLoadTraderRecord(maxDataTime));
        setProgressBarMax(companycount);
        String TraderId, TraderName, Link, Tel, CorpAddr, ProviceName, CityName, AgentId, AgentName, Uprecndate;
        List<Map<String, Object>> map;
        for (maxDataTime = getLSMaxUprecndate("newretail", sysUserInfo.getCompanyid()); companycurcount < companycount; maxDataTime = getLSMaxUprecndate("newretail", sysUserInfo.getCompanyid())) {
            map = AccessWeb.getHelper(getApplicationContext()).GetDownLoadTraderInfor(maxDataTime);
//            Log.d("main",map.toString());
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
                    rest = SqliteDataHelper.getHelper(getApplicationContext()).execSQLString("select traderid from newretail where traderid=? and agentid=?",
                            new String[]{TraderId,AgentId});
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
//        Log.d("main-1", maxDataTime);
        companycount = Integer.parseInt(AccessWeb.getHelper(getApplicationContext()).GetDownLoadStoreRecord(maxDataTime));
        setProgressBarMax(companycount);
        String StoreId, StoreName, Link, Tel, CorpAddr, TraderId, Uprecndate;
        List<Map<String, Object>> map;
        for (maxDataTime = getMaxUprecndate("storeinfor"); companycurcount < companycount; maxDataTime = getMaxUprecndate("storeinfor")) {
//            Log.d("main-2", maxDataTime);
            map = AccessWeb.getHelper(mContext).GetDownLoadStoreInfor(maxDataTime);

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
            map = AccessWeb.getHelper(mContext).GetDownLoadStraightShopInfor(maxDataTime);
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


    /**
     * 下载分销店资料测试
     * @author van van.shu@magic-point.com
     * @version 创建时间：2017-12-1 上午10:58:31
     */
    //	public void downLoadDistributorTest()  throws Exception
    //	{
    //		maxDataTime=getMaxUprecndate("distributor");
    //		companycurcount = 0;
    //		//		companycount = Integer.parseInt(AccessWeb.getHelper(getApplicationContext()).GetDownLoadTraderRecord(maxDataTime));
    //		//		List<Map<String, Object>> map = AccessWeb.getHelper(getApplicationContext()).GetDownLoadTraderInfor(maxDataTime);
    //		List<Map<String, Object>> map = testAddDistributor();
    //		companycount = map.size();
    //		String Distributorid,Distributorname,Link,Tel,CorpAddr,Traderid,Uprecndate;
    //		for (Map<String, Object> map2 : map) {
    //			if(isPaused)
    //				return ;
    //
    //			Distributorid = (String) map2.get("Distributorid");
    //			Distributorname = (String) map2.get("Distributorname");
    //			Link = (String) map2.get("Link");
    //			Tel = (String) map2.get("Tel");
    //			CorpAddr = (String) map2.get("CorpAddr");
    //			Traderid = (String) map2.get("Traderid");
    //			Uprecndate = (String) map2.get("Uprecndate");
    //
    //			String sqlInsert = "insert into distributor("
    //					+ "distributorid,distributorname,link,tel,corpaddr,traderid,uprecndate)"
    //					+ " values(?,?,?,?,?,?,?)";
    //			String sqlUpdate = "update distributor "
    //					+ "set distributorname =?,link=?,tel=?,corpaddr=?," +
    //					" traderid=?,uprecndate=? where distributorid=? ";
    //			try {
    //				String rest = "";
    //				rest = SqliteDataHelper.getHelper(getApplicationContext()).execSQLString("select distributorid from distributor where distributorid=?",
    //						new String[] {Distributorid });
    //				if (rest == "") {
    //					SqliteDataHelper.getHelper(getApplicationContext()).execSQL(sqlInsert,
    //							new String[]{Distributorid,Distributorname,Link,Tel,CorpAddr,Traderid,
    //							Uprecndate});
    //				} else {
    //					SqliteDataHelper.getHelper(getApplicationContext()).execSQL(sqlUpdate,
    //							new String[]{Distributorname,Link,Tel,CorpAddr,Traderid,
    //							Uprecndate,Distributorid});
    //				}
    //
    //				setProgressBarValue(companycurcount++);
    //				showTip("下载分销店资料..."+companycurcount+"/"+companycount);
    //			} catch (Exception e) {
    //				throw new Exception("新增或修改数据错误分销店资料：" + e.getMessage());
    //			}
    //		}
    //	}

    //	public List<Map<String, Object>> testAddDistributor()
    //	{
    //		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
    //		Map<String, Object> map = new HashMap<String, Object>();
    //		map.put("Distributorid", "075501");
    //		map.put("Distributorname", "深圳分销测试1");
    //		map.put("Link", "深分1");
    //		map.put("Tel", "0755-1234567");
    //		map.put("CorpAddr", "深圳市龙华新区民治街道3号");
    //		map.put("Traderid", "CS0011907550001");
    //		map.put("Uprecndate", "1900-01-01 00:46:48.923");
    //
    //		list.add(map);
    //
    //		map = new HashMap<String, Object>();
    //		map.put("Distributorid", "075502");
    //		map.put("Distributorname", "深圳分销测试2");
    //		map.put("Link", "深分2");
    //		map.put("Tel", "0755-1234562");
    //		map.put("CorpAddr", "深圳市龙华新区民治街道4号");
    //		map.put("Traderid", "CS0011907550001");
    //		map.put("Uprecndate", "1900-01-01 00:46:48.924");
    //		list.add(map);
    //
    //		map = new HashMap<String, Object>();
    //		map.put("Distributorid", "075503");
    //		map.put("Distributorname", "深圳分销测试33332");
    //		map.put("Link", "3深分3");
    //		map.put("Tel", "0755-1234563");
    //		map.put("CorpAddr", "深圳市龙华新区民治街道33号");
    //		map.put("Traderid", "CS0011907550001");
    //		map.put("Uprecndate", "1900-01-01 00:46:48.923");
    //		list.add(map);
    //
    //		map = new HashMap<String, Object>();
    //		map.put("Distributorid", "001501");
    //		map.put("Distributorname", "北京分销测试1");
    //		map.put("Link", "北分1");
    //		map.put("Tel", "0100-1234562");
    //		map.put("CorpAddr", "北京市朝阳新区解放街道4号");
    //		map.put("Traderid", "CS001010100001");
    //		map.put("Uprecndate", "1900-01-01 00:46:48.924");
    //		list.add(map);
    //
    //		map = new HashMap<String, Object>();
    //		map.put("Distributorid", "001502");
    //		map.put("Distributorname", "北京分销测试2");
    //		map.put("Link", "北分2");
    //		map.put("Tel", "0100-1234562");
    //		map.put("CorpAddr", "北京市朝阳新区解放街道5号");
    //		map.put("Traderid", "CS001010100001");
    //		map.put("Uprecndate", "1900-01-01 00:46:48.925");
    //		list.add(map);
    //
    //		map = new HashMap<String, Object>();
    //		map.put("Distributorid", "001001");
    //		map.put("Distributorname", "天津分销测试01");
    //		map.put("Link", "天分1");
    //		map.put("Tel", "0100-1234566d2");
    //		map.put("CorpAddr", "天津市朝阳新区解放街道5号");
    //		map.put("Traderid", "CS001020220001");
    //		map.put("Uprecndate", "1900-01-01 00:46:48.915");
    //		list.add(map);
    //
    //		map = new HashMap<String, Object>();
    //		map.put("Distributorid", "001002");
    //		map.put("Distributorname", "天津分销测试02");
    //		map.put("Link", "天分22");
    //		map.put("Tel", "0100-123456642");
    //		map.put("CorpAddr", "天津市朝阳新区解放街道4号");
    //		map.put("Traderid", "CS001020220001");
    //		map.put("Uprecndate", "1900-01-01 00:46:48.916");
    //		list.add(map);
    //
    //		map = new HashMap<String, Object>();
    //		map.put("Distributorid", "001003");
    //		map.put("Distributorname", "天津分销测试03");
    //		map.put("Link", "天分33");
    //		map.put("Tel", "0100-123456672");
    //		map.put("CorpAddr", "天津市朝阳新区解放街道6号");
    //		map.put("Traderid", "CS001020220001");
    //		map.put("Uprecndate", "1900-01-01 00:46:48.917");
    //		list.add(map);
    //
    //		map = new HashMap<String, Object>();
    //		map.put("Distributorid", "001004");
    //		map.put("Distributorname", "天津分销测试04");
    //		map.put("Link", "天分44");
    //		map.put("Tel", "0100-123456682");
    //		map.put("CorpAddr", "天津市朝阳新区解放街道8号");
    //		map.put("Traderid", "CS001020220001");
    //		map.put("Uprecndate", "1900-01-01 00:46:48.918");
    //		list.add(map);
    //
    //		map = new HashMap<String, Object>();
    //		map.put("Distributorid", "003001");
    //		map.put("Distributorname", "上海分销测试1");
    //		map.put("Link", "上述");
    //		map.put("Tel", "13670001111");
    //		map.put("CorpAddr", "上海市青浦区新区解放街道1号");
    //		map.put("Traderid", "CS001090210001");
    //		map.put("Uprecndate", "1900-01-01 00:46:48.919");
    //		list.add(map);
    //
    //
    //		return list;
    //	}


    /**
     * 下载代理商资料
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
     * 下载供应商资料
     */
    private void downloadSupplier() throws Exception {
        maxDataTime = getMaxUprecndate("newsupplier");
        companycount = Integer.parseInt(AccessWeb.getHelper(getApplicationContext()).GetDownLoadSupplierRecord(maxDataTime));
        companycurcount = 0;
        setProgressBarMax(companycount);
        List<Map<String, Object>> map;
        String SupplierName, Uprecndate, SupplierId;
        for (maxDataTime = getMaxUprecndate("newsupplier"); companycurcount < companycount; maxDataTime = getMaxUprecndate("newsupplier")) {
            map = AccessWeb.getHelper(getApplicationContext()).GetDownLoadSupplierInfor(maxDataTime);
            if (map.size() == 0) {
                return;
            }
            for (Map<String, Object> map2 : map) {
                if (isPaused)
                    return;
                SupplierName = (String) map2.get("SupplierName");
                Uprecndate = (String) map2.get("Uprecndate");
                SupplierId = (String) map2.get("SupplierId");

                String sqlInsert = String.format("insert into newsupplier("
                        + "supplier_name,uprecndate,supplier_id)"
                        + " values('%1$s','%2$s','%3$s')", SupplierName, Uprecndate, SupplierId);

                String sqlUpdate = String.format("update newsupplier "
                                + "set supplier_name = '%1$s',uprecndate = '%2$s' where supplier_id='%3$s'",
                        SupplierName, Uprecndate, SupplierId);

                try {
                    String rest = "";
                    rest = SqliteDataHelper.getHelper(getApplicationContext()).execSQLString(
                            "select supplier_id from newsupplier where supplier_id=?",
                            new String[]{SupplierId});
                    if (rest == "") {
                        SqliteDataHelper.getHelper(getApplicationContext()).execSQL(sqlInsert);
                    } else {
                        SqliteDataHelper.getHelper(getApplicationContext()).execSQL(sqlUpdate);
                    }
                } catch (Exception e) {
                    throw new Exception("新增或修改数据供应商错误：" + e.getMessage());
                }

                setProgressBarValue(companycurcount++);
                showTip("下载供应商资料..." + companycurcount + "/" + companycount);

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
     * 下载仓库资料
     *
     * @throws Exception
     */
    private void downStock() throws Exception {
        maxDataTime = getMaxUprecndate("newstock");
        companycount = Integer.parseInt(AccessWeb.getHelper(getApplicationContext()).GetDownLoadStockRecord(maxDataTime));
        companycurcount = 0;
        setProgressBarMax(companycount);
        String StockName, Uprecndate, StockId;
        List<Map<String, Object>> map;
        for (maxDataTime = getMaxUprecndate("newstock"); companycurcount < companycount; maxDataTime = getMaxUprecndate("newstock")) {
            map = AccessWeb.getHelper(getApplicationContext()).GetDownLoadStockInfor(maxDataTime);
//            Log.d("main--stock",map.toString());

            if (map.size() == 0) {
                return;
            }
            for (Map<String, Object> map2 : map) {
                if (isPaused)
                    return;

                StockName = (String) map2.get("StockName");
                Uprecndate = (String) map2.get("Uprecndate");
                StockId = (String) map2.get("StockId");

                String sqlInsert = "insert into newstock("
                        + "stock_name,uprecndate,stock_id)" + " values(?,?,?)";
                String sqlUpdate = "update newstock "
                        + "set stock_name =?,uprecndate=? where stock_id=? ";
                try {
                    String rest = "";
                    rest = SqliteDataHelper.getHelper(getApplicationContext()).execSQLString(
                            "select stock_id from newstock where stock_id=?",
                            new String[]{StockId});
                    if (rest == "") {
                        SqliteDataHelper.getHelper(getApplicationContext()).execSQL(sqlInsert,
                                new String[]{StockName, Uprecndate, StockId});
                    } else {
                        SqliteDataHelper.getHelper(getApplicationContext()).execSQL(sqlUpdate,
                                new String[]{StockName, Uprecndate, StockId});
                    }

                    setProgressBarValue(companycurcount++);
                    showTip("下载仓库资料..." + companycurcount + "/" + companycount);
                } catch (Exception e) {
                    throw new Exception("新增或修改数据仓库错误：" + e.getMessage());
                }
            }
        }
    }
    /**
     * 判断当前品牌商是否有首发发货功能
     *
     * @throws Exception
     */
    private void GetFirstDeliveryStatus() {
        String result = "";
        try {
            result = AccessWeb.getHelper(getApplicationContext()).
                    GettFirstDeliveryStatus(sysUserInfo.getEnterpriseId());
            if (!(result.equals(""))) {
                sysUserInfo.setFirstStart(result);
            } else {
                sysUserInfo.setFirstStart("");
            }
        } catch (Exception e) {
            sysUserInfo.setFirstStart("");
            ShowMessage.ShowMsg(hand, "FirstStart---" + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean userlogin() throws Exception {
        String result = "";
        if (sysUserInfo.getAgentVersionNum().equals("CCS7")){
            //新客户调用新版登录接口
            result = AccessWeb.getHelper(getApplicationContext()).AgentUserLogin(et_username.getText().toString().trim(), et_password.getText().toString(),sysUserInfo.getEnterpriseId());

        }else {
            result = AccessWeb.getHelper(getApplicationContext()).UserLogin(et_username.getText().toString().trim(), et_password.getText().toString());
        }
//        Log.d("main--", result);
        return ParseData_login(result);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCheckNetState();
        sysUserInfo.setAgentVersionNum("");
        //		Log.i("main", "onResume--");
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopCheckNetState();
        //		Log.i("main", "onStop() --");
    }


    // 检查网络状态==============================
    private Thread netState;
    boolean bOnNetState = true;

    private void startCheckNetState() {
//        Log.i("main", "startCheckNetState");
        GetNetRun = new GetNetStateRunnable(true, mContext);
        GetNetRun.SetCallBack(new GetNetStateRunnable.GetNetState() {

            @Override
            public void netState_on() {
                online();
            }

            @Override
            public void netState_off() {
                offline();
            }
        });
        netState = new Thread(GetNetRun);
        netState.start();
        GetNetRun.State = true;
    }

    private void stopCheckNetState() {
        //		Log.i("main", "stopCheckNetState");
        GetNetRun.setState(false);
        bOnNetState = false;
        if (netState != null && netState.isAlive()) {
            netState.interrupt();
        }
    }

    private void offline() {
        ShowMessage.ShowMsg(hand, 11, "");
    }

    private void online() {
        ShowMessage.ShowMsg(hand, 12, "");
    }

    public boolean ParseData_login(String data) {
        if (isPaused) {
            return false;
        }

        BaseHandler handler = new UserLoginHandler();

        handler.parse(data);
        Vector<?> users = (Vector<?>) BaseHandler.hash.get("users");// hash很重要，所有的东西都在hash中

        if (users.size() == 0) {
            return false;
        }
        User user = (User) users.elementAt(0);
        if (user == null) {
            return false;
        }
        if (Boolean.parseBoolean(user.getP00()) == false) {
            // 显示错误信息
            ShowMessage.ShowMsg(hand, user.getP01());
            return false;
        }

        if (Boolean.parseBoolean(user.getP00()) == true) {
            //			Log.i("main", sysUserInfo.getServerip()+",sysUserInfo.getMobile()="+sysUserInfo.getMobile());
            if (!sysUserInfo.getLastLoginServerIp().equals(sysUserInfo.getServerip())) {//!et_username.getText().toString().equals(sysUserInfo.getMobile())||
                //万新的要求不重新下载资料
                //				if(!sysUserInfo.getServerip().equals("wx.emtong.com")&&
                //						!sysUserInfo.getServerip().equals("47.97.45.122"))
                //				{
                //				}
            }

//            if (sysUserInfo.getSoftType().equals("52")) {
////                if (!et_username.getText().toString().equals(sysUserInfo.getMobile())) {
//                    SqliteDataHelper.getHelper(getApplicationContext()).CleanStoreinfor();
////                }
//            }
            if (sysUserInfo.getSoftType().equals("52"))//代理商模式
            {
                if (user.getP05().equals("总公司")) {
                    ShowMessage.ShowMsg(hand, "当前为代理商发货模式，总公司账号无法登录，请用代理商仓库身份登录！");
                    return false;
                }
            } else //总公司模式
            {
                if (user.getP05().equals("代理商")) {
                    ShowMessage.ShowMsg(hand, "当前为总公司发货模式，代理商账号无法登录，请用总公司仓库身份登录！");
                    return false;
                }
            }
//            Log.d("main", user.toString());
            sysUserInfo.setUserid(user.getP01());
            sysUserInfo.setLoginid(user.getP02());
            //总公司 代号传00，代理商则代理商代号
            if ("总公司".equals(user.getP05())) {
                sysUserInfo.setCompanyid("00");
            } else {
                sysUserInfo.setCompanyid(user.getP06());
            }
            sysUserInfo.setStock(user.getP04());
            sysUserInfo.setMode(user.getP05());
            //sysUserInfo.setCompanyid(user.getP06());
            sysUserInfo.setMobile(checkBox.isChecked() ? et_username.getText().toString() : "");
            sysUserInfo.setLoginpwd(checkBox.isChecked() ? et_password.getText().toString() : "");
            sysUserInfo.setIfrember(checkBox.isChecked());
            sysUserInfo.setLastLoginServerIp(sysUserInfo.getServerip());
            try {
                SqliteDataHelper.getHelper(getApplicationContext()).execSQL(String.format("update brandinfo set  username =  '%1$s',pwd = '%2$s',IfRemember = '%3$s'  where businessid = '%4$s' ", et_username.getText().toString().trim(), et_password.getText().toString().trim(), String.valueOf(checkBox.isChecked()), sysUserInfo.getClientId()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (user.getP05().equals("供应商")){
                ShowMessage.ShowMsg(hand, "供应商登录");
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    public String byte2HexString(byte[] b) {
        StringBuffer sb = new StringBuffer(b.length);
        for (int i = 0; i < b.length; i++) {
            sb.append(String.format("%02X", b[i]));
        }
        return sb.toString();
    }

    public String getCondition(String table, String tCondition)
            throws Exception {
        String result = "";

        //result = Sqlite_DataHelp.getHelper(getApplicationContext()).execSQLString("select max(uprecndate) from  " + table + tCondition);
        Cursor cursor = SqliteDataHelper.getHelper(getApplicationContext()).getCursor("select max(uprecndate) from  " + table + tCondition);
        cursor.moveToFirst();
        if (cursor != null && cursor.getCount() > 0)
            result = cursor.getString(0);
        if (null == result)
            result = "";
        if (null != cursor) {
            cursor.close();
            cursor = null;
        }
        return result;
    }

    public String getConditiontest(String table, String tCondition) throws Exception {
        String result = "";
        result = SqliteDataHelper.getHelper(getApplicationContext()).execSQLString("select max(uprecndate) from  " + table + tCondition);
        return result;
    }

    private int getVersionCode() throws Exception {
        // 获取packagemanager的实例
        PackageManager packageManager = getPackageManager();
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(), 0);
        return packInfo.versionCode;
    }

    public String add(int a, int b) {
        int c = a + b;

        return String.valueOf(c);
    }

    /*
     * 用pull解析器解析服务器返回的xml文件 (xml封装了版本号)
     */
    public static UpdateInfo getUpdataInfo(InputStream is) throws Exception {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(is, "utf-8");// 设置解析的数据源
        int type = parser.getEventType();
        UpdateInfo info = new UpdateInfo();// 实体
        while (type != XmlPullParser.END_DOCUMENT) {
            switch (type) {
                case XmlPullParser.START_TAG:
                    if ("version".equals(parser.getName())) {
                        info.setVersion(parser.nextText()); // 获取版本号
                    } else if ("url".equals(parser.getName())) {
                        info.setUrl(parser.nextText()); // 获取要升级的APK文件
                    } else if ("description".equals(parser.getName())) {
                        info.setDescription(parser.nextText()); // 获取该文件的信息
                    } else if ("brand".equals(parser.getName())) {
                        //					info.setBrand(parser.nextText()); // 获取品牌
                    } else if ("ifupdate".equals(parser.getName())) {
                        info.setIfupdate(parser.nextText()); // 获取品牌
                    }
                    break;
            }
            type = parser.next();
        }
        return info;
    }


    // 打开网络设置
    public void setnet() {
        if (android.os.Build.VERSION.SDK_INT > 10) {
            // 3.0以上打开设置界面，也可以直接用ACTION_WIRELESS_SETTINGS打开到wifi界面
            startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
        } else {
            startActivity(new Intent(
                    android.provider.Settings.ACTION_WIRELESS_SETTINGS));
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:

                //暴龙或者总公司模式返回失效
                if (sysUserInfo.getClientId().equals("01") || ADevicesManager.CorpCompany) {
                    return false;
                }
                //			stopCheckNetState();
                setResult(RESULT_OK);
                this.finish();

            case KeyEvent.KEYCODE_MINUS:
            case KeyEvent.KEYCODE_HOME:
                return false;

            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    public String getSql(String Sql) {
        String result = "";

        try {
            result = SqliteDataHelper.getHelper(getApplicationContext()).execSQLString(Sql);

        } catch (Exception e) {
            e.printStackTrace();
            result = e.getMessage();
        }
        return result;
    }


    /**
     * 获取点击事件,是否隐藏键盘
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        SomeUtils.isNeedHideAndDo(this, ev);
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 添加测试菜单
     */
    private void addTestMenu() throws Exception {

        List<String> sqlList = new ArrayList<String>();
        //产品入库
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*产品入库','1','','','01')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*入库','1','','01','0101')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*有单入库','1','P_Dv_InStock_Bill','0101','010101')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*无单入库','1','P_Dv_InStock_NoBill','0101','010102')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*装盒入库','1','P_Dv_InStock_PackBox_List_NoBill','0101','010103')");

        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*镜片有单入库','1','P_Dv_InStock_Lens_Bill','0101','010104')");

        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*撤消','1','','01','0102')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*有单入库撤消','1','P_Dv_InStock_Bill_Cancel','0102','010201')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*无单入库撤消','1','P_Dv_InStock_NoBill_Cancel','0102','010202')");

        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*镜片有单入库撤消','1','P_Dv_InStock_Lens_Bill_Cancel','0102','010203')");

        //入库退回
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*入库退回','1','','','02')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*退回','1','','02','0201')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*有单入库退回','1','P_Dv_ReturnedPurchase_Z_G_Bill','0201','020101')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*无单入库退回','1','P_Dv_ReturnedPurchase_Z_G_NoBill','0201','020102')");

        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*无单镜片入库退回','1','P_Dv_ReturnedPurchase_Lens_Z_G_NoBill','0201','020103')");

        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*撤消','1','','02','0202')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*有单退回撤消','1','P_Dv_ReturnedPurchase_Z_G_Bill_Cancel','0202','020201')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*无单退回撤消','1','P_Dv_ReturnedPurchase_Z_G_NoBill_Cancel','0202','020202')");

        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*无单镜片退回撤消','1','P_Dv_ReturnedPurchase_Lens_Z_G_NoBill_Cancel','0202','020203')");

        //代销发货
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*代销发货','1','','','03')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*发货','1','','03','0301')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*有单有入库代销发货','1','P_Dv_OutStock_Z_D_Bill_BeInStock','0301','030101')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*有单无入库代销发货','1','P_Dv_OutStock_Z_D_Bill_NoInStock','0301','030102')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*无单有入库代销发货','1','P_Dv_OutStock_Z_D_NoBill_BeInStock','0301','030103')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*无单无入库代销发货','1','P_Dv_OutStock_Z_D_NoBill_NoInStock','0301','030104')");

        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*镜片有单有入库代销发货','1','P_Dv_OutStock_Lens_Z_D_Bill_BeInStock','0301','030105')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*镜片有单无入库代销发货','1','P_Dv_OutStock_Lens_Z_D_Bill_NoInStock','0301','030106')");

        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*撤消','1','','03','0302')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*有单代销发货撤消','1','P_Dv_OutStock_Z_D_Bill_Cancel','0302','030201')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*无单代销发货撤消','1','P_Dv_OutStock_Z_D_NoBill_Cancel','0302','030202')");

        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*镜片有单代销发货撤销','1','P_Dv_OutStock_Lens_Z_D_Bill_Cancel','0302','030203')");


        //代销退货
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*代销退货','1','','','04')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*退货','1','','04','0401')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*有单无明细代销退货','1','P_Dv_ReturnedPurchase_Z_D_Bill','0401','040101')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*有单有明细代销退货','1','P_Dv_ReturnedPurchase_Z_D_Bill_Detail','0401','040102')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*无单代销退货','1','P_Dv_ReturnedPurchase_Z_D_NoBill','0401','040103')");

        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*镜片有单无明细代销退货','1','P_Dv_ReturnedPurchase_Lens_Z_D_Bill','0401','040105')");

        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*撤消','1','','04','0402')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*有单无明细代销退回撤消','1','P_Dv_ReturnedPurchase_Z_D_Bill_Cancel','0402','040201')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*有单有明细代销退货撤消','1','P_Dv_ReturnedPurchase_Z_D_Bill_Detail_Cancel','0402','040202')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*无单代销退货撤消','1','P_Dv_ReturnedPurchase_Z_D_NoBill_Cancel','0402','040203')");

        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*镜片有单代销退货撤销','1','P_Dv_ReturnedPurchase_Lens_Z_D_Bill_Cancel','0402','040205')");

        //直销发货
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*直销发货','1','','','05')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*发货','1','','05','0501')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*有单有入库直销发货','1','P_Dv_OutStock_Z_L_Bill_BeInStock','0501','050101')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*有单无入库直销发货','1','P_Dv_OutStock_Z_L_Bill_NoInStock','0501','050102')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*无单有入库直销发货','1','P_Dv_OutStock_Z_L_NoBill_BeInStock','0501','050103')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*无单无入库直销发货','1','P_Dv_OutStock_Z_L_NoBill_NoInStock','0501','050104')");

        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*镜片有单有入库直销发货','1','P_Dv_OutStock_Lens_Z_L_Bill_BeInStock','0501','050106')");

        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*镜片有单无入库直销发货','1','P_Dv_OutStock_Lens_Z_L_Bill_NoInStock','0501','050108')");

        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*发货撤消','1','','05','0502')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*有单直销发货撤消','1','P_Dv_OutStock_Z_L_Bill_Cancel','0502','050201')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*无单直销发货撤消','1','P_Dv_OutStock_Z_L_NoBill_Cancel','0502','050202')");

        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*镜片有单直销发货撤销','1','P_Dv_OutStock_Lens_Z_L_Bill_Cancel','0502','050203')");

        //直销退货
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*直销退货','1','','','06')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*退货','1','','06','0601')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*有单无明细直销退货','1','P_Dv_ReturnedPurchase_Z_L_Bill','0601','060101')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*有单有明细直销退货','1','P_Dv_ReturnedPurchase_Z_L_Bill_Detail','0601','060102')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*无单直销退货','1','P_Dv_ReturnedPurchase_Z_L_NoBill','0601','060103')");

        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*镜片有单无明细直销退货','1','P_Dv_ReturnedPurchase_Lens_Z_L_Bill','0601','060105')");

        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*撤消','1','','06','0602')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*有单无明细退货撤消','1','P_Dv_ReturnedPurchase_Z_L_Bill_Cancel','0602','060201')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*有单有明细退货撤消','1','P_Dv_ReturnedPurchase_Z_L_Bill_Detail_Cancel','0602','060202')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*无单退货撤消','1','P_Dv_ReturnedPurchase_Z_L_NoBill_Cancel','0602','060203')");

        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*镜片有单直销退货撤销','1','P_Dv_ReturnedPurchase_Lens_Z_L_Bill_Cancel','0602','060205')");


        //其他功能
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*其他扫描','1','','','07')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*有单调拨','1','P_Dv_InStock_Z_ChangeStock_Bill','07','0701')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*无单调拨','1','P_Dv_InStock_Z_ChangeStock_NoBill','07','0702')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*产品换标','1','P_Dv_InStock_Z_ChangeCode','07','0703')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*吊牌回收','1','P_Dv_RecoverScan_Z','07','0704')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*入库换型号','1','P_Dv_InStock_Z_ChangeProduct','07','0705')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*退货直通车','1','P_Dv_L_Return_Z_Bill','07','0706')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*补打盒标','1','P_Dv_InStock_PackBox_Search','07','0707')");

        //物流查询
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*产品物流','1','','','08')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('*物流查询','1','P_ProductLogist','08','0801')");
        SqliteDataHelper.getHelper(getApplicationContext()).BatchOperation(sqlList);
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

//			max(date(字段名,'YYYY-MM-DD HH:MM:SS'))

            String sqlSelect = String.format(
                    "select max(uprecndate) from %1$s", table);

//			String sqlSelect = String.format(
//					"select max(date(uprecndate,'YYYY-MM-DD HH:MM:SS')) from %1$s", table);

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
            if (maxUprecndate == null||maxUprecndate.equals("null")) {
                maxUprecndate = "";
            }
        } catch (Exception e) {
            e.printStackTrace();
            maxUprecndate = "";
        }
        return maxUprecndate;
    }

    //
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

    /**
     * 下载菜单
     */
    public void downloadMenus() {

        try {

            if (SqliteDataHelper.getHelper(getApplicationContext()).execSQLInt("select count(*) from menus") == 0) {
                //从网络下载菜单
                List<Map<String, Object>> list = AccessWeb.getHelper(getApplicationContext()).GetDevMenuInfor(sysUserInfo.getEnterpriseId());

                List<String> sqlList = new ArrayList<String>();

                if (list.size() == 0) {
                    throw new Exception("菜单下载失败。");
                }
                //把菜单插入数据库
                String menucode, menuname, parentcode, showstatus, procedurename, sql = "";
                for (Map<String, Object> map : list) {
                    menucode = map.get("menucode").toString();
                    menuname = map.get("menuname").toString();
                    parentcode = map.get("parentcode").toString();
                    showstatus = map.get("showstatus").toString();
                    procedurename = map.get("procedurename").toString();
                    sql = String.format("insert into menus(menucode,menuname,parentcode,showstatus,procedurename) values " +
                            "('" + menucode + "','" + menuname + "','" + parentcode + "','" + showstatus + "','" + procedurename + "')");

                    sqlList.add(sql);

                }
                //把菜单批量插入数据库
                SqliteDataHelper.getHelper(getApplicationContext()).BatchOperation(sqlList);

            }

            //如果添加测试菜单，就把下载的菜单了删除，不然会一起存在。
            if (ADevicesManager.isAddTestMenu) {
                String sql = "delete from menus";
                SqliteDataHelper.getHelper(getApplicationContext()).execSQL(sql);
                addTestMenu();
            }
            //			List<Map<String, String>> allList = SqliteDataHelper.exeselect("select * from menus");
            //			Log.i("main", "allList----"+allList.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

