package com.holyes.headquarter.other;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.ccssend5.R;
import com.holyes.ccssend5.lib.AccessWeb;
import com.holyes.ccssend5.lib.MySound;
import com.holyes.ccssend5.lib.ShowMessage;
import com.holyes.ccssend5.lib.SysUserInfo;
import com.holyes.ccssend5.utils.SomeUtils;

import java.util.List;
import java.util.Map;

/**
 * @ClassName: P_ProductLogist
 * @Description: 产品物流查询
 * @Author: lijin
 * @Date: 2021/3/10 14:01
 */
public class P_ProductLogist extends Activity {


    private Context mContext;
    private AccessWeb accessWeb;
    private SysUserInfo sysUserInfo;
//    private MySound sound;

    private RadioGroup radio_group;
    private EditText et_barcode;
    private TextView tv_message;

    private List<Map<String, Object>> Remarks;

    private final String lsc_Logistics = "1";//1-物流码
    private final String lsc_Security = "2";//2-表示防伪码
    private final String lsc_Integral = "3";//3-表示积分码

    private String tCodeType = lsc_Logistics;//1-物流码;2-表示防伪码;3-表示积分码
    private String tCodeValue = "";//扫描条码
    private String tUnitId;//用户所在公司代号(总公司用户为：00，代理商用户为所在代理商代号)
    private String message = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_p_productlogist);

        mContext = this;
        accessWeb = new AccessWeb(mContext);
        sysUserInfo = new SysUserInfo(mContext);
//        sound = MySound.getMySound(this);

        radio_group = (RadioGroup) findViewById(R.id.radio_group);
        et_barcode = (EditText) findViewById(R.id.et_barcode);
        tv_message = (TextView) findViewById(R.id.tv_message);

        tUnitId = sysUserInfo.getCompanyid();//总公司"00",大代理商模式就为代理商代号

        radio_group.setOnCheckedChangeListener(new RadioGroupChangeListener());
        et_barcode.setOnKeyListener(new EtBarodeOnkeyListener());
//        et_barcode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if(actionId == EditorInfo.IME_ACTION_SEND||actionId== EditorInfo.IME_ACTION_DONE ||event.getKeyCode()==KeyEvent.KEYCODE_ENTER&&v.getText()!=null&& event.getAction() == KeyEvent.ACTION_DOWN){
//                    if (et_barcode.getText().toString().trim().indexOf("=") != -1) {
//                        //包含
//                        tCodeValue = SomeUtils.InterceptCode(mContext, et_barcode.getText().toString().trim());
//                    } else {
//                        //不包含
//                        tCodeValue = et_barcode.getText().toString().trim();
//                    }
//
//                    if (!SomeUtils.isAllNumber(mContext, tCodeValue)) {
//                        ShowMessage.Show(mContext, "请扫描正确的物流码【" + tCodeValue + "】");
//                        et_barcode.requestFocus();
//                        et_barcode.setText("");
//                        return false;
//                    }
//                    access_send(tCodeType, tCodeValue, tUnitId);
//                    et_barcode.setText("");
//                    return false;
//                }
//                return false;//返回true，保留软键盘;false，隐藏软键盘
//            }
//        });

    }

    //输入框监听
    class EtBarodeOnkeyListener implements View.OnKeyListener {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {

            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
//					tCodeValue = et_barcode.getText().toString().trim();


                    if (et_barcode.getText().toString().trim().indexOf("=") != -1) {
                        //包含
                        tCodeValue = SomeUtils.InterceptCode(mContext, et_barcode.getText().toString().trim());
                    } else {
                        //不包含
                        tCodeValue = et_barcode.getText().toString().trim();
                    }


                    if (!SomeUtils.isAllNumber(mContext, tCodeValue)) {
                        ShowMessage.Show(mContext, "请扫描正确的物流码【" + tCodeValue + "】");
                        et_barcode.requestFocus();
                        et_barcode.setText("");
                        return true;
                    }
                    access_send(tCodeType, tCodeValue, tUnitId);
                    et_barcode.setText("");

                }
            }

            return false;
        }
    }

    /**
     * @param tCodeType  查码类型(其值为：1或2或3，说明：1-物流码;2-表示防伪码;3-表示积分码)
     * @param tCodeValue 查码内容,扫描条码
     * @param tUnitId    用户所在公司代号(总公司用户为：00，代理商用户为所在代理商代号)
     */
    public void access_send(final String tCodeType, final String tCodeValue, final String tUnitId) {

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    message = "";
                    Remarks = accessWeb.P_ProductLogist(tCodeType, tCodeValue, tUnitId);
                    for (Map<String, Object> m : Remarks) {
                        message += m.get("Remark") + "\r\n";
                    }

                    ShowMessage.ShowMsg(handler, ShowMessage.HandScanSuccess, "ok");
                } catch (Exception e) {
                    message = e.getMessage();
                    ShowMessage.ShowMsg(handler, ShowMessage.HandScanError, e.getMessage());
                }
            }
        }).start();
    }

    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case ShowMessage.HandScanSuccess:
//                    MySound.scanSound();
                    MySound.scanSound();

                    tv_message.setText(message);
                    et_barcode.requestFocus();
                    break;
                case ShowMessage.HandScanError:
//                    MySound.errorSound();
                    MySound.errorSound();
                    tv_message.setText(message);
                    et_barcode.setText("");
                    et_barcode.requestFocus();
                    break;
            }
        }
    };


    /**
     * 单选监听
     */
    class RadioGroupChangeListener implements RadioGroup.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {

            switch (checkedId) {
                case R.id.radio_logistics:
                    tCodeType = lsc_Logistics;


                    break;
                case R.id.radio_security:
                    tCodeType = lsc_Security;


                    break;
                case R.id.radio_integral:
                    tCodeType = lsc_Integral;
                    break;

                default:
                    break;
            }

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

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
//	                configuration.setToDefaults();
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

