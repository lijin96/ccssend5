package com.holyes.ccssend5.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Xml;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.ccssend5.R;
import com.holyes.ccssend5.entity.UpdateInfo;
import com.holyes.ccssend5.lib.ADevicesManager;
import com.holyes.ccssend5.lib.NetManager;
import com.holyes.ccssend5.lib.ShowMessage;
import com.holyes.ccssend5.lib.SysUserInfo;
import com.holyes.ccssend5.lib.UpdateManager;
import com.holyes.ccssend5.myview.BadgeView;
import com.holyes.ccssend5.myview.Loading;
import com.holyes.ccssend5.utils.DisplayUtil;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.xmlpull.v1.XmlPullParser;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import io.reactivex.functions.Consumer;

/**
 * @ClassName: AboutActivity
 * @Description: 升级，关于界面
 * @Author: lijin
 * @Date: 2021/3/6 14:10
 */
public class AboutActivity extends Activity {

    SysUserInfo sysinfo;
    private Thread updateThread;
    private Loading loading = null;
    private Handler hand;

    private final int HandUpdateNewVer = 1;
    private final int HandUpdateOriginalVer = 2;

    private boolean isGetpermissions = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        DisplayUtil.setDefaultDisplay(this);
        setContentView(R.layout.about);
        sysinfo = new SysUserInfo(getApplicationContext());
        hand = new handShowMsg();
        initView("关于");
        rxPermissionWRITE();
    }


    private void initView(String title) {
        TextView titletext = (TextView) findViewById(R.id.titletext);

        titletext.setText(title);

        Button btnBack, btnUpdate, buttombtn1, btnRepair;

        btnBack = (Button) findViewById(R.id.titlebtn1);

        btnUpdate = (Button) findViewById(R.id.titlebtn2);

        buttombtn1 = (Button) findViewById(R.id.titlebotom_btn1);

        btnRepair = (Button) findViewById(R.id.titlebotom_btn2);

        btnUpdate.setText("升级");

        btnRepair.setText("维护");


        /**
         * 暴龙代理商模式
         * */
        if (!ADevicesManager.CorpCompany && !sysinfo.getClientId().equals("01")) //代理商模式
        {

            btnRepair.setVisibility(View.GONE);
        }

        buttombtn1.setVisibility(View.GONE);

        if (sysinfo.getUpdate()) {

            BadgeView txtUpdate = new BadgeView(AboutActivity.this, btnUpdate);
            txtUpdate.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
            txtUpdate.setText("1");
            txtUpdate.show();
        }
        //返回按钮监听
        btnBack.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                finish();
            }
        });

        //升级按钮监听
        btnUpdate.setOnClickListener(new UpdateOnClick());

        //维护按钮监听
        btnRepair.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AboutActivity.this, PasswordActivity.class);
                intent.putExtra("password", "29822832");
                startActivityForResult(intent, 0);
            }
        });
    }

    /**
     * 更新按钮监听类
     */
    class UpdateOnClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (isGetpermissions) {
                if (NetManager.getNetType(AboutActivity.this) == null) {
                    return;
                }

                loading = new Loading(AboutActivity.this, "正在检查最新版本...", new Loading.OnLoadingback() {
                    @Override
                    public void back(String name) {


                        if (updateThread != null && updateThread.isAlive()) {
                            updateThread.interrupt();
                        }
                    }
                });
                loading.Show();

                updateThread = new Thread(new CheckUpdateThread());

                updateThread.start();
            } else {
                rxPermissionWRITE();
            }


        }
    }

    private void rxPermissionWRITE() {
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean granted) throws Exception {
                isGetpermissions = granted;
//                if (granted) {
//                    // 同意权限
//                } else {
//                    // 权限被拒绝
//                }
            }
        });
    }

    private class handShowMsg extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ShowMessage.HandShowMessage: // 显示错误提示显示
                    loading.Close();
                    ShowMessage.Show(AboutActivity.this, msg.obj.toString());
                    break;
                case HandUpdateNewVer:
                    loading.Close();
                    sysinfo.setUpdate(true);
                    UpdateManager mUpdateManager = new UpdateManager(AboutActivity.this);
                    mUpdateManager.checkUpdateInfo("软件版本更新");
                    break;

                case HandUpdateOriginalVer:
                    loading.Close();
                    UpdateManager mManager = new UpdateManager(AboutActivity.this);
                    mManager.checkUpdateInfo("软件重新下载安装");

                    break;
                default:
                    //MyProgressDialog.close();
                    // ShowMessage.Show(Xundian.this,msg.obj.toString());
                    break;
            }
            super.handleMessage(msg);
        }

    }

    /**
     * 检查是否有新的版本的线程
     */
    private class CheckUpdateThread implements Runnable {
        @Override
        public void run() {
            try {
//				showTip("检测版本信息...");
//				有新版本，下载新版本更新
                if (IsNewSoftVer()) {
                    ShowMessage.ShowMsg(hand, HandUpdateNewVer, "可升级");
                    return;
                }
//				//没有新版本，原版本下载更新，针对出问题的客户，当客户那边没有数据线之类的不好安装的时候就让他们下载更新。
                else {
                    ShowMessage.ShowMsg(hand, HandUpdateOriginalVer, "对不起当前没有可升级的版本。");
                }
            } catch (Exception e) {
                ShowMessage.ShowMsg(hand, "版本检查错误" + e.getMessage());
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 0:
                if (resultCode == RESULT_OK) {
                    setResult(RESULT_OK, new Intent());
                    finish();
                }
                break;
            case 1:
                break;
        }
    }

    private void showTip(String msg) {
        ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage, msg);
    }

    /**
     * 是否有新的版本
     *
     * @return
     */
    private boolean IsNewSoftVer() {
        try {
            URL aURL = new URL(sysinfo.getUploadSoftUrl());
            HttpURLConnection conn = (HttpURLConnection) aURL.openConnection();
            conn.setConnectTimeout(5000);

            InputStream is = conn.getInputStream();
            UpdateInfo updateInfo = new UpdateInfo();
            updateInfo = getUpdataInfo(is);
            sysinfo.setUpdateMsg(updateInfo.getDescription());
            sysinfo.setApkUrl(updateInfo.getUrl());

            if (sysinfo.getBrand() == "") {
                sysinfo.setBrand(updateInfo.getBrand());
            }
            //			return true;
            if (Integer.valueOf(updateInfo.getVersion()).intValue() > getVersionCode()) {
                return true;
            } else {
                sysinfo.setUpdate(false);
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

    /**
     * 用pull解析器解析服务器返回的xml文件 (xml封装了版本号)
     */
    public static UpdateInfo getUpdataInfo(InputStream is) throws Exception {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(is, "utf-8");//设置解析的数据源
        int type = parser.getEventType();
        UpdateInfo info = new UpdateInfo();//实体
        while (type != XmlPullParser.END_DOCUMENT) {
            switch (type) {
                case XmlPullParser.START_TAG:
                    if ("version".equals(parser.getName())) {
                        info.setVersion(parser.nextText());    //获取版本号
                    } else if ("url".equals(parser.getName())) {
                        info.setUrl(parser.nextText());    //获取要升级的APK文件
                    } else if ("description".equals(parser.getName())) {
                        info.setDescription(parser.nextText());    //获取该文件的信息
                    } else if ("brand".equals(parser.getName())) {
                        info.setBrand(parser.nextText());    //获取品牌
                    } else if ("ifupdate".equals(parser.getName())) {
                        info.setIfupdate(parser.nextText());    //获取更新
                    }
                    break;
            }
            type = parser.next();
        }
        return info;
    }

    /**
     * 获取版本号
     */
    private int getVersionCode() throws Exception {
        // 获取packagemanager的实例
        PackageManager packageManager = getPackageManager();
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(),
                0);
        return packInfo.versionCode;
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

