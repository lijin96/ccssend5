package com.holyes.agent.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.ccssend5.R;
import com.holyes.ccssend5.lib.GetNetStateRunnable;
import com.holyes.ccssend5.lib.ShowMessage;
import com.holyes.ccssend5.lib.SysUserInfo;
import com.holyes.ccssend5.utils.SomeUtils;

import java.io.File;

/**
 * @ClassName: DMainActivity
 * @Description: 代理商模式主界面
 * @Author: lijin
 * @Date: 2021/3/5 17:21
 */
public class DMainActivity extends Activity {
    private Handler hand;
    private Context mContext;
    private GetNetStateRunnable GetNetRun;
    private TextView versionCode;
    private ImageView ivNet;
    boolean isExit = false;//退出标志

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.d_main_activity);
        hand = new handShowMsg();
        mContext = this;

        ((Button) findViewById(R.id.btn_sendgoods)).setOnClickListener(new BthSendGoodsClick());
        ((Button) findViewById(R.id.btn_backgoods)).setOnClickListener(new BthBackGoodsClick());
        ((Button) findViewById(R.id.btn_otherscan)).setOnClickListener(new BthOtherScanClick());
        ((Button) findViewById(R.id.btn_setting)).setOnClickListener(new BthSettingClick());

        ((TextView) findViewById(R.id.txt_versionCode)).setText(SomeUtils.getSoftVer(this));
        ivNet = (ImageView) findViewById(R.id.imageView1);
        ivNet.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                SomeUtils.goToSetting(DMainActivity.this);
            }
        });
        SysUserInfo sysinfo = new SysUserInfo(DMainActivity.this);
        String savePath = "/data/data/com.holyes.ccsdevs5/";
        String filepath = savePath + sysinfo.getClientId() + ".png";
        File file = new File(filepath);

        if (file.exists()) {
            Bitmap bm = BitmapFactory.decodeFile(filepath);
            //将图片显示到ImageView中
            ((ImageView) findViewById(R.id.logoimage)).setImageBitmap(bm);
        }


    }

    private class handShowMsg extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ShowMessage.HandShowMessage: // 显示错误提示显示
                    ShowMessage.Show(DMainActivity.this, msg.obj.toString());
                    break;
                case 11:
                    ((ImageView) findViewById(R.id.imageView1)).setImageResource(R.drawable.link_state_off);
                    break;
                case 12:
                    ((ImageView) findViewById(R.id.imageView1)).setImageResource(R.drawable.link_state_on);
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                //连续按返回按钮两次就退出界面
                if (SomeUtils.isDoubleClick(this, true)) {
                    finish();
                }
                return true;
            case KeyEvent.KEYCODE_MINUS:
        }
        return false;
    }

    //选择发货菜单
    private class BthSendGoodsClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(DMainActivity.this, DSendGoodsMenuListActivity.class); //mov by cai test
            startActivity(intent);
        }
    }

    //选择退货菜单
    private class BthBackGoodsClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(DMainActivity.this, DBackGoodsMenuListActivity.class);
            startActivity(intent);
        }
    }

    //其他扫描
    private class BthOtherScanClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(DMainActivity.this, DOtherScanMenu.class);
            startActivity(intent);
        }
    }

    //系统设置
    private class BthSettingClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            SomeUtils.goToSetting(DMainActivity.this);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        startCheckNetState();//开始检查网络状态
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopCheckNetState();
    }

    //检查网络状态
    //AccessWeb accWeb;
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
        ShowMessage.ShowMsg(hand, 11, "");
    }

    private void online() {
        ShowMessage.ShowMsg(hand, 12, "");
    }


    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        if (res.getConfiguration().fontScale != 1) {//非默认值
            Configuration newConfig = new Configuration();
            newConfig.setToDefaults();//设置默认
            res.updateConfiguration(newConfig, res.getDisplayMetrics());
        }
        return res;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}

