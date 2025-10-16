package com.holyes.agent.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ccssend5.R;
import com.holyes.ccssend5.activity.LoginActivity;
import com.holyes.ccssend5.lib.ADevicesManager;
import com.holyes.ccssend5.lib.SqliteDataHelper;
import com.holyes.ccssend5.lib.SysUserInfo;
import com.holyes.ccssend5.myview.AlwaysMarqueeTextView;
import com.holyes.ccssend5.utils.SomeUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.functions.Consumer;

/**
 * @ClassName: P_Dv_Select_Brand
 * @Description: 代理商选择品牌
 * @Author: lijin
 * @Date: 2021/3/5 17:24
 */
public class P_Dv_Select_Brand extends Activity {
    SysUserInfo sysinfo;

    Button btn;
    RelativeLayout layout;
    TextView tv_versionCode;
    RelativeLayout.LayoutParams lp1;

    List<Map<String, Object>> Brand = null;

    int i;
    int count = 0;
    int width =0;
    int height=0;

    private boolean isGetpermissions = true;//获取读写权限


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_p_dv_select_brand);
        sysinfo = new SysUserInfo(getApplicationContext());
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        width = dm.widthPixels;
        height = dm.heightPixels;

//            layout = new RelativeLayout(this);
        layout=findViewById(R.id.relat_brand);
//            layout.setBackgroundResource(R.drawable.login);

//            SharePrefenceUtils.getAndCopyCcs4Brand(this);

        tv_versionCode = new TextView(this);
        tv_versionCode.setText(SomeUtils.getSoftVer(this));
        tv_versionCode.setTextColor(Color.WHITE);
        tv_versionCode.setTextSize(18);
        lp1 = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        // 设置额外参数规则，位于父View顶部
        lp1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lp1.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        lp1.setMargins(10, 10, 20, 20);
        Refresh_Ui();
        ADevicesManager.SetKey(false);

        rxPermissionWRITE();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //品牌验证,更新配置,成功后更新UI
        if(requestCode == 0)
        {
            if(resultCode == RESULT_OK)
            {
                Refresh_Ui();
            }
            else if(resultCode == RESULT_CANCELED)
            {

            }
            else if(resultCode == 1)
            {
                ADevicesManager.SetKey(true);

                System.exit(0);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                return true;
            case KeyEvent.KEYCODE_MINUS:
            case KeyEvent.KEYCODE_HOME:
                return false;
        }
        return false;
    }


    @SuppressLint("NewApi")
    private void Refresh_Ui()
    {
        //查找本地所有注册的品牌资料
        Brand = SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList("select * from brandinfo", null);
//            Log.d("brand",Brand.toString());
//
//        Map<String, Object> test= new HashMap<String,Object>();
//        test.put("brandname","HOLYES");
//        test.put("username","ckasd");
//        test.put("logurl","http://www.4006889521.cn/softupdate/Android/Dev/image/p00.png");
//        test.put("IfRemember",true);
//        test.put("servernetip","ccs.4006889521.cn");
//        test.put("pwd","a12345678");
//        test.put("port","9520");
//        test.put("businessid","98");
//        test.put("enterpriseid","00,");
//        test.put("softType","52");
//        test.put("serverccip","test.4006889521.cn");
//        for (int i=0;i<13;i++){
//            Brand.add(test);
//        }


        layout.removeAllViews();
        //这里创建16个按钮，每行放置3个按钮
        RelativeLayout.LayoutParams btParams1 = new RelativeLayout.LayoutParams (width,100);  //设置按钮的宽度和高度
//            btParams1.bottomMargin=50;
        btParams1.leftMargin = 20;   //横坐标定位
        btParams1.topMargin = 20;   //纵坐标定位

        AlwaysMarqueeTextView txtmsg = new AlwaysMarqueeTextView(this);

        if(Brand.size() == 0)
        {
            txtmsg.setText("温馨提示：您当前没有添加任何品牌信息，请点击《添加品牌》添加");
        }
        else
        {
            txtmsg.setText(String.format("温馨提示：您当前已经添加了 %1$s个品牌 ，请选择其中一个，点击进入相应品牌功能，长按则修改品牌配置信息", Brand.size()));
        }

        txtmsg.setTextSize(20);
        txtmsg.setSingleLine(true);
        txtmsg.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        txtmsg.setMarqueeRepeatLimit(-1);
        layout.addView(txtmsg,btParams1);   //将按钮放入layout组件
        count  = Brand.size();
        Button Btn[] = new Button[Brand.size()];
        int j = -1;
        for  (   i=0; i<Brand.size(); i++) {
            Btn[i]=new Button(this);
            Btn[i].setId(2000+i);
            Btn[i].setTag(i);

            String filepath = "/sdcard/com.holyes.ccssend5/" + Brand.get(i).get("businessid").toString() + ".png";
            File file = new File(filepath);

            if (file.exists())
            {
                Bitmap bm = BitmapFactory.decodeFile(filepath);

                //将图片显示到ImageView中
                Btn[i].setBackground( new BitmapDrawable(bm));
            }
            // 单击进入
            Btn[i].setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    int count = Integer.parseInt(v.getTag().toString());
                    //ClientId,BrandName,service_ip,port,softtype ,enterpriseid,username , pwd ,IfRemember
                    //						sysinfo.setClientId( Brand.get(count ).get("ClientId").toString());
                    sysinfo.setBrand( Brand.get(count).get("brandname").toString());
                    sysinfo.setServerIp( Brand.get(count ).get("serverccip").toString());
                    sysinfo.setServerport( Brand.get(count ).get("port").toString());
                    sysinfo.setSoftType( Brand.get(count).get("softType").toString());
                    sysinfo.setEnterpriseId(Brand.get(count).get("enterpriseid").toString());

                    sysinfo.setMobile(Brand.get(count).get("username").toString());
                    sysinfo.setLoginpwd( Brand.get(count).get("pwd").toString());
                    sysinfo.setIfrember( Boolean.parseBoolean(Brand.get(count).get("IfRemember").toString()));

                    Intent intent = new Intent(P_Dv_Select_Brand.this, LoginActivity.class);

                    //					if(!sysinfo.getClientId().equalsIgnoreCase(Brand.get(count ).get("businessid").toString()) )
                    //					{
                    sysinfo.setClientId( Brand.get(count ).get("businessid").toString());
                    intent.putExtra("businessid", Brand.get(count ).get("businessid").toString());
                    //					}

                    startActivityForResult(intent, 0);
                }
            });

            //更新配置
            Btn[i].setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int count = Integer.parseInt(v.getTag().toString());
                    Intent intent = new Intent(P_Dv_Select_Brand.this,P_Dv_Brand_Config.class);

                    intent.putExtra("businessid", Brand.get(count).get("businessid").toString());
                    intent.putExtra("service_ip", Brand.get(count).get("serverccip").toString());
                    intent.putExtra("port", Brand.get(count).get("port").toString());
                    intent.putExtra("softtype", Brand.get(count).get("softType").toString());
                    intent.putExtra("enterpriseid", Brand.get(count).get("enterpriseid").toString());

                    startActivityForResult(intent,0);
                    return true;
                }
            });

            RelativeLayout.LayoutParams btParams = new RelativeLayout.LayoutParams ((width-200)/3,120);  //设置按钮的宽度和高度
            if (i%3 == 0) {
                j++;
            }
            btParams.leftMargin = 60+ ((width-100)/3+10)*(i%3);   //横坐标定位
            btParams.topMargin = 130 + 150*j;   //纵坐标定位

            layout.addView(Btn[i],btParams);   //将按钮放入layout组件
        }

        btn = new Button(this);
        btn.setBackgroundResource(R.drawable.button_background);
        btn.setText("添加品牌");
        btn.setTextSize(20);

        RelativeLayout.LayoutParams btParams = new RelativeLayout.LayoutParams ((width-100)/3,120);  //设置按钮的宽度和高度
        if (i%3 == 0) {
            j++;
        }
        btParams.leftMargin = 30+ ((width-100)/3+10)*(i%3);   //横坐标定位
        btParams.topMargin = 130 + 150*j;   //纵坐标定位

        layout.addView(btn,btParams);   //将按钮放入layout组件
        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (isGetpermissions) {

                    Intent itent = new Intent(P_Dv_Select_Brand.this, P_Dv_Brand_Check.class);
                    startActivityForResult(itent, 0);
                }else {
                    rxPermissionWRITE();
                }
            }
        });
        layout.addView(tv_versionCode,lp1);
//            this.setContentView(layout);
        //		if(count == 1)
        //		{
        //			sysinfo.setServerIp( Brand.get(0 ).get("serverccip").toString());
        //			sysinfo.setServerport(Brand.get(0 ).get("port").toString());
        //			sysinfo.setBrand( Brand.get(0).get("brandname").toString());
        //			sysinfo.setSoftType(Brand.get(0).get("softType").toString());
        //			sysinfo.setMobile(Brand.get(0).get("username").toString());
        //			sysinfo.setLoginpwd(Brand.get(0).get("pwd").toString());
        //			sysinfo.setIfrember(Boolean.valueOf(Brand.get(0).get("IfRemember").toString()));
        //			sysinfo.setEnterpriseId(Brand.get(0).get("enterpriseid").toString());
        //
        //			//针对暴龙品牌修改
        //			if(Brand.get(0 ).get("businessid").toString().equals("01"))
        //			{
        //				btn.setText("退出");
        //
        //
        //				btn.setOnClickListener(new OnClickListener() {
        //
        //					@Override
        //					public void onClick(View v) {
        //						ADevicesManager.SetKey(true);
        //						finish();
        //					}
        //				});
        //			}
        //
        //			Intent intent = new Intent(P_Dv_Select_Brand.this,LoginActivity.class);
        //
        //			//本次进入的品牌和上次进入的品牌是不一样时清除数据
        //			if(!sysinfo.getClientId().equalsIgnoreCase(Brand.get(0 ).get("businessid").toString()) )
        //			{
        //				sysinfo.setClientId( Brand.get(0 ).get("businessid").toString());
        //				intent.putExtra("businessid", Brand.get(0 ).get("businessid").toString());
        //			}
        //
        //			startActivityForResult(intent, 0);
        //		}

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

    private void rxPermissionWRITE() {
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE).subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean granted) throws Exception {
                isGetpermissions = granted;
                if (granted) {
                    // 同意权限
                } else {
                    // 权限被拒绝
                    Toast.makeText(P_Dv_Select_Brand.this, "未授权权限，功能将无法使用", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}

