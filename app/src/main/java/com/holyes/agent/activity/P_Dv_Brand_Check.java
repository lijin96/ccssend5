package com.holyes.agent.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ccssend5.R;
import com.holyes.ccssend5.activity.PasswordActivity;
import com.holyes.ccssend5.lib.ADevicesManager;
import com.holyes.ccssend5.lib.AccessWeb;
import com.holyes.ccssend5.lib.ShowMessage;
import com.holyes.ccssend5.lib.SqliteDataHelper;
import com.holyes.ccssend5.myview.MyProgressDialog;
import com.holyes.ccssend5.utils.ImageUtils;
import com.holyes.ccssend5.utils.SomeUtils;

import java.io.FileNotFoundException;
import java.util.Map;

/**
 * @ClassName: P_Dv_Brand_Check
 * @Description: 品牌验证
 * @Author: lijin
 * @Date: 2021/3/5 17:22
 */
public class P_Dv_Brand_Check extends Activity implements View.OnClickListener {
    Bitmap bm;
    Thread down_image ;
    private Handler hand;

    private Button btn_checkcode,btn_import,btn_fix,btn_system;
    private ImageView imageview;
    private EditText edt_code;
    private TextView tv_versioncode;

    String businessid,brandname,serverccip,servernetip,port,logurl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.d_brand_check);

        imageview = (ImageView)findViewById(R.id.imageView);

        edt_code = (EditText)findViewById(R.id.edtcheck_code);
        tv_versioncode = (TextView) findViewById(R.id.tv_versioncode);

        btn_checkcode = (Button)findViewById(R.id.btn_check);

//		btn_import = (Button)findViewById(R.id.btn_Import);

        btn_fix= (Button)findViewById(R.id.btn_fix);

        btn_system = (Button)findViewById(R.id.btnSystemRepair);

        btn_checkcode.setOnClickListener(this);

//		btn_import.setOnClickListener(this);

        btn_fix.setOnClickListener(this);

        btn_system.setOnClickListener(this);

        hand = new handShowMsg();
        tv_versioncode.setText(SomeUtils.getSoftVer(this));
    }


    private class handShowMsg extends Handler {

        @Override
        public void handleMessage(Message msg) {

            switch(msg.what)
            {
                case 0: //成功
                    imageview.setImageBitmap(bm);

                    String  savePath = "/sdcard/com.holyes.ccssend5/";
                    String Image_ico =savePath + businessid  + ".png";

                    try {
                        if(!ImageUtils.SaveBitmapToFile(Image_ico, bm))
                        {
                            ShowMessage.ShowMsg(hand, ShowMessage.HandScanSuccess, "保存图片失败");
                        }
                    } catch (FileNotFoundException e1) {

                        e1.printStackTrace();
                    }
                    edt_code.setText("");
                    ShowMessage.MessageBox( P_Dv_Brand_Check.this, "添加品牌成功");
                    break;

                case 1: //星系
                    ShowMessage.Show(P_Dv_Brand_Check.this, msg.obj.toString());
                    break;

                case 2:
                    new Thread(new Runnable() {

                        @Override
                        public void run() {

                            try {

                                bm = ImageUtils.UrlToBitmap(logurl);

                                hand.sendEmptyMessage(0);

                            } catch (Exception e) {

                                e.printStackTrace();
                            }

                        }
                    }).start();;

                    break;
                case 3:
                    break;
            }
            MyProgressDialog.close();
            super.handleMessage(msg);
        }
    }


    @Override
    public void onClick(View v) {

        switch(v.getId())
        {

            case R.id.btn_check:

                if(edt_code.getText().toString().equals(""))
                {
                    ShowMessage.MessageBox( P_Dv_Brand_Check.this, "请输入品牌验证码");
                    return;
                }

                MyProgressDialog.show(this, "正在检查品牌验证码，请稍候...", true, false);

                down_image = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        AccessWeb access = new AccessWeb(getApplicationContext());

                        Map<String, Object> reault = null;

                        try {
                            reault = access.AccreditCodeLeadingV2(edt_code.getText().toString().trim(), ADevicesManager.getDeviceId());
                            businessid=reault.get("businessid").toString();
                            brandname=reault.get("brandname").toString();
                            serverccip=reault.get("serverccip").toString();
                            servernetip=reault.get("servernetip").toString();
                            port=reault.get("port").toString();
                            logurl=reault.get("logurl").toString();
                        } catch (Exception e1) {
                            ShowMessage.ShowMsg( hand, ShowMessage.HandScanSuccess, "验证错误"+e1.getMessage());
                            return;
                        }
                        try {

                            if(SqliteDataHelper.getHelper(getApplicationContext()).execSQLInt("select count(*) from brandinfo where businessid = '"+businessid+"'") > 0)
                            {
                                ShowMessage.ShowMsg(hand, ShowMessage.HandScanSuccess, "此品牌已经添加，无需重复添加");
                                return;
                            }

                            if(SqliteDataHelper.getHelper(getApplicationContext()).execSQLInt("select count(*) from brandinfo where softType <> '52'") > 0)
                            {
                                ShowMessage.ShowMsg(hand, ShowMessage.HandScanSuccess, "当前为总公司模式，不支持多品牌");
                                return;
                            }

                            String sql = "insert into brandinfo(businessid,brandname,serverccip,servernetip,port,logurl,softtype,enterpriseid)values('"+businessid+"','"+brandname+"','"+serverccip+"','"+servernetip+"','"+port+"','"+logurl+"','"+"52"+"','"+businessid+"')";

                            SqliteDataHelper.getHelper(getApplicationContext()).execSQL(sql);
                            //
                            hand.sendEmptyMessage(2);

                        } catch (Exception e) {

                            ShowMessage.ShowMsg(hand, ShowMessage.HandScanSuccess, "保存数据失败"+e.getMessage());

                            return;
                        }
                    }
                });
                down_image.start();
                break;
//		case R.id.btn_Import:
//			ShowMessage.ShowMsg(hand, ShowMessage.HandScanSuccess, "此功能暂未开通");

//			break;

            case R.id.btn_fix:

                Intent intent = new Intent(P_Dv_Brand_Check.this, PasswordActivity.class);
                intent.putExtra("password", "29822832");
                startActivityForResult(intent,0);
                break;
            case R.id.btnSystemRepair://系统设置
                Intent intent1 = new Intent("/");
                ComponentName cm = new ComponentName("com.android.settings","com.android.settings.Settings");
                intent1.setComponent(cm);
                intent1.setAction("android.intent.action.VIEW");
                startActivity( intent1 );
                break;

        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode)
        {
            case 0:
                if (resultCode == RESULT_OK)
                {

                    setResult(1);
                    this.finish();

                }
                break;
            case 1:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void onBackPressed() {

        setResult(RESULT_OK);

        finish();
        super.onBackPressed();
    }

    /**
     * 获取点击事件,是否隐藏键盘
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        SomeUtils.isNeedHideAndDo(this,ev);
        return super.dispatchTouchEvent(ev);
    }

    //设置字体为默认大小，不随系统字体大小改而改变
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.fontScale != 1)//非默认值
            getResources();
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

