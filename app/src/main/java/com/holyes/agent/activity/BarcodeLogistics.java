package com.holyes.agent.activity;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
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
 * @ClassName: BarcodeLogistics
 * @Description: 代理商物流查询
 * @Author: lijin
 * @Date: 2021/3/5 16:09
 */
public class BarcodeLogistics extends Activity {
    private Context mContext;
    private AccessWeb accessWeb;
    private SysUserInfo sysUserInfo;
    private MySound sound;

    private RadioGroup radio_group;
    private EditText et_barcode;
    private TextView tv_message;

    private List<Map<String, Object>> Remarks;

    private boolean tBcOrAc = true;//True-物流码;False-表示防伪码
    private String tCode = "";//扫描条码
    private String tCompnay;//用户所在公司代号(总公司用户为：00，代理商用户为所在代理商代号)
    private String message = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.barcodelogistics);

        mContext = this;
        accessWeb = new AccessWeb(mContext);
        sysUserInfo = new SysUserInfo(mContext);
        sound = MySound.getMySound(this);

        radio_group = (RadioGroup) findViewById(R.id.radio_group);
        et_barcode = (EditText) findViewById(R.id.et_barcode);
        tv_message = (TextView) findViewById(R.id.tv_message);

        tCompnay = sysUserInfo.getCompanyid();//总公司"00",大代理商模式就为代理商代号

        radio_group.setOnCheckedChangeListener(new RadioGroupChangeListener());
        et_barcode.setOnKeyListener(new EtBarodeOnkeyListener());
    }

    //输入框监听
    class EtBarodeOnkeyListener implements View.OnKeyListener {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {

            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    tCode=SomeUtils.UpdatefirstString(mContext,et_barcode.getText().toString().trim());
                    if (et_barcode.getText().toString().indexOf(" ") != -1) {
                        //包含
                        tCode = SomeUtils.AgentCode(mContext, et_barcode.getText().toString());
                    }

//					if(!SomeUtils.isAllNumber(mContext,tCode))
//					{
//						ShowMessage.Show(mContext,  "请扫描正确的物流码【"+tCode+"】");
//						et_barcode.requestFocus();
//						et_barcode.setText("");
//						return true;
//					}
                    access_send(tCode, tBcOrAc, tCompnay);
                    et_barcode.setText("");

                }
            }

            return false;
        }
    }

    /**
     * @param tBcOrAc  查码类型(其值为：True-物流码;False-表示防伪码)
     * @param tCode    查码内容,扫描条码
     * @param tCompnay 用户所在公司代号(总公司用户为：00，代理商用户为所在代理商代号)
     */
    public void access_send(final String tCode, final boolean tBcOrAc, final String tCompnay) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    message = "";
                    Remarks = accessWeb.BarcodeLogistics(tCode, tBcOrAc, tCompnay);
                    for (Map<String, Object> m : Remarks) {
                        message += m.get("Remark") + "\r\n";
                    }

                    ShowMessage.ShowMsg(handler, ShowMessage.HandScanSuccess, "ok");
                } catch (Exception e) {
                    ShowMessage.ShowMsg(handler, ShowMessage.HandScanError, e.getMessage());
                }
            }
        }).start();
    }

    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case ShowMessage.HandScanSuccess:
                    MySound.scanSound();
                    tv_message.setText(message);
                    et_barcode.requestFocus();
                    break;
                case ShowMessage.HandScanError:
                    MySound.errorSound();
                    ShowMessage.Show(mContext, msg.obj.toString());
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
                    tBcOrAc = true;


                    break;
                case R.id.radio_security:
                    tBcOrAc = false;


                    break;

                default:
                    break;
            }

        }

    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
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
}

