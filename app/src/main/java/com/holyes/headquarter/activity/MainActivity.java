package com.holyes.headquarter.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.example.ccssend5.R;
import com.holyes.ccssend5.lib.GetNetStateRunnable;
import com.holyes.ccssend5.lib.NetManager;
import com.holyes.ccssend5.lib.ShowMessage;
import com.holyes.ccssend5.lib.SqliteDataHelper;
import com.holyes.ccssend5.lib.SysUserInfo;
import com.holyes.ccssend5.lib.UpdateManager;
import com.holyes.ccssend5.myview.Loading;
import com.holyes.ccssend5.utils.DisplayUtil;
import com.holyes.ccssend5.utils.SomeUtils;
import com.holyes.headquarter.other.OtherMenuListActivity;
import com.holyes.headquarter.other.P_ProductLogist;

import java.util.List;
import java.util.Map;

/**
 * @ClassName: MainActivity
 * @Description: 新版主界面
 * @Author: lijin
 * @Date: 2021/3/10 10:16
 */
public class MainActivity extends Activity implements View.OnClickListener {

    private Context mContext;
    private GetNetStateRunnable GetNetRun;
    private Handler hand;
    private Loading loading = null;
    private Thread updateThread;
    private SysUserInfo sysUserInfo;

    private Button btn_instock_in, btn_instock_back, btn_sendgoods_zy, btn_backgoods_zy,
            btn_sendgoods_d, btn_backgoods_d, btn_product_logist, btn_otherscan, btn_set, btn_update, btn_function;
    private ImageView ivNet;

    private String parentCode = "01";

    private final int HandUpdateNewVer = 3;
    private final int HandUpdateOriginalVer = 4;

    private boolean isGoToNext = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayUtil.setDefaultDisplay(this);
        setContentView(R.layout.new_main_layout);

        mContext = this;
        sysUserInfo = new SysUserInfo(mContext);
        hand = new handShowMsg();

        btn_instock_in = (Button) findViewById(R.id.btn_instock_in);
        btn_instock_back = (Button) findViewById(R.id.btn_instock_back);
        btn_sendgoods_zy = (Button) findViewById(R.id.btn_sendgoods_zy);
        btn_backgoods_zy = (Button) findViewById(R.id.btn_backgoods_zy);
        btn_sendgoods_d = (Button) findViewById(R.id.btn_sendgoods_d);
        btn_backgoods_d = (Button) findViewById(R.id.btn_backgoods_d);
        btn_product_logist = (Button) findViewById(R.id.btn_product_logist);
        btn_otherscan = (Button) findViewById(R.id.btn_otherscan);
        btn_set = (Button) findViewById(R.id.btn_set);
        btn_update = (Button) findViewById(R.id.btn_update);
        btn_function = (Button) findViewById(R.id.btn_function);
        ivNet = (ImageView) findViewById(R.id.imageView1);

        btn_instock_in.setOnClickListener(this);
        btn_instock_back.setOnClickListener(this);
        btn_sendgoods_zy.setOnClickListener(this);
        btn_backgoods_zy.setOnClickListener(this);
        btn_sendgoods_d.setOnClickListener(this);
        btn_backgoods_d.setOnClickListener(this);
        btn_product_logist.setOnClickListener(this);
        btn_otherscan.setOnClickListener(this);
        btn_set.setOnClickListener(this);
        btn_update.setOnClickListener(this);
        btn_function.setOnClickListener(this);
        ivNet.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                SomeUtils.goToSetting(MainActivity.this);
            }
        });
    }


    @Override
    public void onClick(View v) {
        Intent intent = new Intent(MainActivity.this, MenuListActivity.class);
        switch (v.getId()) {
            case R.id.btn_instock_in:
                intent.putExtra("aim", "instock_in");
                parentCode = "01";
                isGoToNext = isHasFunctions(parentCode);
                break;
            case R.id.btn_instock_back:
                intent.putExtra("aim", "instock_back");
                parentCode = "02";
                isGoToNext = isHasFunctions(parentCode);
                break;
            case R.id.btn_sendgoods_d:
                intent.putExtra("aim", "sendgoods_d");
                parentCode = "03";
                isGoToNext = isHasFunctions(parentCode);
                break;
            case R.id.btn_backgoods_d:
                intent.putExtra("aim", "backgoods_d");
                parentCode = "04";
                isGoToNext = isHasFunctions(parentCode);
                break;
            case R.id.btn_sendgoods_zy:
                intent.putExtra("aim", "sendgoods_zy");
                parentCode = "05";
                isGoToNext = isHasFunctions(parentCode);
                break;
            case R.id.btn_backgoods_zy:
                intent.putExtra("aim", "backgoods_zy");
                parentCode = "06";
                isGoToNext = isHasFunctions(parentCode);
                break;

            case R.id.btn_otherscan:
                intent = new Intent(mContext, OtherMenuListActivity.class);
                intent.putExtra("aim", "otherscan");
                parentCode = "07";
                isGoToNext = isHasFunctions(parentCode);
                break;
            case R.id.btn_product_logist:
                //产品物流查询直接跳转
                intent = new Intent(mContext, P_ProductLogist.class);
                isGoToNext = true;
                break;
            case R.id.btn_set:
                SomeUtils.goToSetting(mContext);
                return;
            case R.id.btn_function:
//			intent = new Intent(mContext,CheckingFunctionActivity.class);
                isGoToNext = false;//暂时不允许用户勾选菜单功能
                break;
            case R.id.btn_update:
                updateApp();
                return;
            default:
                break;
        }

        if (isGoToNext) {
            startActivity(intent);
        } else {
            SomeUtils.showToask(mContext, "抱歉，该功能暂未给您开通！");
        }

    }

    /**
     * 菜单功能下面是否还有功能
     */
    public boolean isHasFunctions(String parentCode) {
        String sql = "select * from menus where parentcode ='" + parentCode + "' and showstatus = '1' ";
        List<Map<String, Object>> functionList = SqliteDataHelper.getHelper(mContext).QueryDbList(sql, null);
        if (functionList.size() > 0) {
            return true;
        } else {
            return false;
        }

    }

    private class handShowMsg extends Handler {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what) {
                case 1:// offline
                    ((ImageView) findViewById(R.id.imageView1))
                            .setImageResource(R.drawable.link_state_off);
                    break;
                case 2:// oneline
                    ((ImageView) findViewById(R.id.imageView1))
                            .setImageResource(R.drawable.link_state_on);
                    break;

                case HandUpdateNewVer:
                    loading.Close();
                    sysUserInfo.setUpdate(true);
                    UpdateManager mUpdateManager = new UpdateManager(mContext);
                    mUpdateManager.checkUpdateInfo("软件版本更新");
                    break;
                case HandUpdateOriginalVer:
                    loading.Close();
                    UpdateManager mManager = new UpdateManager(mContext);
                    mManager.checkUpdateInfo("软件重新下载安装");
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    }


    public void updateApp() {
        if (NetManager.getNetType(mContext) == null) {
            return;
        }
        loading = new Loading(mContext, "正在检查最新版本...", new Loading.OnLoadingback() {
            @Override
            public void back(String name) {


                if (updateThread != null && updateThread.isAlive()) {
                    updateThread.interrupt();
                }
            }
        });
        loading.Show();

        updateThread = new Thread(new UpdateThread());

        updateThread.start();
    }

    class UpdateThread implements Runnable {
        @Override
        public void run() {
            try {

                ShowMessage.ShowMsg(hand, HandUpdateOriginalVer, "对不起当前没有可升级的版本。");

            } catch (Exception e) {
                ShowMessage.ShowMsg(hand, "版本检查错误" + e.getMessage());
            }
        }
    }

    /**
     * 检查菜单是否可行
     *
     * @param menucode
     * @return
     */
    public boolean checkEnableOpen(String menucode) {

        try {
            String sql = "select showstatus from menus where menucode = '" + menucode + "'";
            String showstatus = SqliteDataHelper.getHelper(getApplicationContext()).execSQLString(sql);
            if ("1".equals(showstatus)) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCheckNetState();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopCheckNetState();
    }

    // 检查网络状态==============================
    private Thread netState;
    boolean bOnNetState = true;

    private void startCheckNetState() {
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
        GetNetRun.setState(false);
        bOnNetState = false;
        if (netState != null && netState.isAlive()) {
            netState.interrupt();
        }
    }

    private void offline() {
        ShowMessage.ShowMsg(hand, 1, "");
    }

    private void online() {
        ShowMessage.ShowMsg(hand, 2, "");
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

