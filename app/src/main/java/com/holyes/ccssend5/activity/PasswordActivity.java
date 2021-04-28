package com.holyes.ccssend5.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.example.ccssend5.R;
import com.holyes.ccssend5.lib.ShowMessage;
import com.holyes.ccssend5.utils.DisplayUtil;
import com.holyes.ccssend5.utils.SomeUtils;

/**
 * @ClassName: PasswordActivity
 * @Description: 输入密码
 * @Author: lijin
 * @Date: 2021/3/6 14:11
 */
public class PasswordActivity extends Activity {


    //{{自定义变量
    @SuppressWarnings("unused")
    private Handler hand;
    //}}

    //{{系统事件
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        DisplayUtil.setDefaultDisplay(this);
        setContentView(R.layout.password);
        ((Button) findViewById(R.id.btnOk)).setOnClickListener(new btnOk_click());
        ((Button) findViewById(R.id.btnEsc)).setOnClickListener(new btnEsc_click());
    }
    //}}


    //{{按钮事件
    private class btnOk_click implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            String psw = ((EditText) findViewById(R.id.txtPassword)).getText().toString();
            String checkPsw = getIntent().getStringExtra("password");

            //"29822303";
            //			 setResult(RESULT_OK,new Intent());
            //			 finish();

            if (psw.equals(checkPsw)) {
                setResult(RESULT_OK, new Intent());
                finish();
            } else {
                ShowMessage.Show(PasswordActivity.this, "密码不正确。");
                return;
            }

        }
    }

    private class btnEsc_click implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            finish();
        }
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

