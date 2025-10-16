package com.holyes.agent.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.ccssend5.R;
import com.holyes.ccssend5.lib.ShowMessage;
import com.holyes.ccssend5.lib.SqliteDataHelper;
import com.holyes.ccssend5.select.SelectSureConfirm;
import com.holyes.ccssend5.utils.SomeUtils;

import java.io.File;

/**
 * @ClassName: P_Dv_Brand_Config
 * @Description: 品牌配置信息
 * @Author: lijin
 * @Date: 2021/3/5 17:22
 */
public class P_Dv_Brand_Config extends Activity implements View.OnClickListener {
    Handler hand;
    Bitmap bm = null;

    private ImageView image;
    private Button btn_save,btn_delete;
    private EditText edt_ip ,edt_port,edt_softtype,edt_enterpriseid = null;

    String filepath="";
    private String businessid = "";
    private final int Lic_SelectSure = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.brand_config);

        edt_ip = (EditText)findViewById(R.id.edt_serviceip);
        edt_port = (EditText)findViewById(R.id.EditText01);
        edt_softtype = (EditText)findViewById(R.id.EditText02);
        edt_enterpriseid = (EditText)findViewById(R.id.EditText03);

        btn_save = (Button)findViewById(R.id.button1);
        btn_delete = (Button)findViewById(R.id.button2);
        image = (ImageView)findViewById(R.id.imageView1);

        businessid = getIntent().getStringExtra("businessid");

        edt_ip.setText(getIntent().getStringExtra("service_ip"));
        edt_port.setText(getIntent().getStringExtra("port"));
        edt_softtype.setText(getIntent().getStringExtra("softtype"));
        edt_enterpriseid.setText(getIntent().getStringExtra("enterpriseid"));
//		edt_softtype.setKeyListener(null);

        SomeUtils.moveFocus(edt_ip);

        String  savePath = "/data/data/com.example.ccssend5/";
        filepath = savePath + businessid + ".png";
        File file = new File(filepath);

        hand = new Handler(){

            @Override
            public void dispatchMessage(Message msg) {

                switch(msg.what)
                {
                    case 0:
                        image.setImageBitmap(bm);
                        break;
                }
                super.dispatchMessage(msg);
            }
        };

        if (file.exists())
        {
            Bitmap bm = BitmapFactory.decodeFile(filepath);
            //将图片显示到ImageView中
            image.setImageBitmap(bm);
        }
        else
        {
        }

        btn_save.setOnClickListener(this);
        btn_delete.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.button1://保存
                //sql = "Create table  brandinfo(ClientId nvarchar(20),BrandName nvarchar(30),service_ip nvarchar(20),port nvarchar(20),softtype nvarchar(10),enterpriseid,username nvarchar(20),pwd nvarchar(20) ,IfRemember nvarchar(5))";

                try {
                    SqliteDataHelper.getHelper(getApplicationContext()).execSQL(String.format("update brandinfo set  serverccip =  '%1$s',port = '%2$s'," +
                                    "softtype = '%3$s',enterpriseid = '%4$s' where businessid = '%5$s' ",
                            edt_ip.getText().toString().trim(),edt_port.getText().toString().trim(),edt_softtype.getText().toString().trim(),
                            edt_enterpriseid.getText().toString().trim(),businessid));
                    ShowMessage.Show(P_Dv_Brand_Config.this, "数据保存成功！");
                    setResult(RESULT_OK);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case R.id.button2://删除

                String brandName="";
                try {
                    brandName = SqliteDataHelper.getHelper(getApplicationContext()).execSQLString("select BrandName from brandinfo where businessid ='"+businessid+"'");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(P_Dv_Brand_Config.this, SelectSureConfirm.class);
                intent.putExtra("title", "确定要删除【"+brandName+"】这个品牌吗？");
                intent.putExtra("imgPath", filepath);
                startActivityForResult(intent, Lic_SelectSure);

                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode==RESULT_OK)
        {
            switch (requestCode)
            {
                case Lic_SelectSure:
                    SqliteDataHelper.getHelper(getApplicationContext()).DeleteDB("delete  from brandinfo where businessid = ?", new String[]{ businessid});
                    ShowMessage.Show(P_Dv_Brand_Config.this, "删除品牌成功！");
                    setResult(RESULT_OK);
                    finish();

                    break;

                default:
                    break;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
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

