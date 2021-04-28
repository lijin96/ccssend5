package com.holyes.ccssend5.activity;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.device.PrinterManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.example.ccssend5.R;

import java.text.SimpleDateFormat;

/**
 * @ClassName: PrintActivity
 * @Description: 小票打印+测试打印
 * @Author: lijin
 * @Date: 2021/3/6 14:11
 */
public class PrintActivity extends Activity {

    private Button btn_print_hb, btn_print_xp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print);
        initView();
    }

    private void initView() {
        btn_print_hb = (Button) findViewById(R.id.btn_print_hb);
        btn_print_xp = (Button) findViewById(R.id.btn_print_xp);
        btn_print_hb.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

            }
        });
        btn_print_xp.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                PrinterManager printer = new PrinterManager();
                try {
                    printer.prn_open();
                    printer.prn_setupPage(384, -1);
                    int ret = printer.prn_drawTextEx("        产品发货单", 0, 0, -1, -1,
                            "arial", 38, 0, 0, 0);
                    ret += printer.prn_drawTextEx("店名：" + "深圳合力思科技", 0, ret, -1, -1,
                            "arial", 25, 0, 0, 0);
                    ret += printer.prn_drawTextEx("DX-000000001", 0, ret, -1, -1, "arial", 25,
                            0, 0, 0);
                    ret += printer.prn_drawTextEx(
                            "-------------------------------------", 0, ret, -1, -1,
                            "arial", 25, 0, 0, 0);
                    printer.prn_drawTextEx("        型号-色号 ", 0, ret, -1, -1, "arial",
                            25, 0, 0, 0);
                    ret += printer.prn_drawTextEx("数量", 300, ret, -1, -1, "arial", 25,
                            0, 0, 0);
                    ret += printer.prn_drawTextEx(
                            "-------------------------------------", 0, ret, -1, -1,
                            "arial", 25, 0, 0, 0);
                    int sum = 0;
                    String product = "";


                    printer.prn_drawTextEx("sp1-c1", 0, ret, -1, -1, "arial",
                            25, 0, 0, 0);

                    ret += printer.prn_drawTextEx("5",
                            300, ret, -1, -1, "arial", 25, 0, 0, 0);


                    ret += printer.prn_drawTextEx(
                            "-------------------------------------", 0, ret, -1, -1,
                            "arial", 25, 0, 0, 0);
                    ret += printer.prn_drawTextEx("合计：" + sum + "      打单:" + "张三",
                            0, ret, -1, -1, "arial", 25, 0, 0, 0);
                    SimpleDateFormat sDateFormat = new SimpleDateFormat(
                            "yyyy-MM-dd HH:mm:ss");
                    ret += printer.prn_drawTextEx(
                            "日期时间：" + sDateFormat.format(new java.util.Date()), 0, ret,
                            -1, -1, "arial", 25, 0, 0, 0);
                    ret += printer.prn_drawTextEx(" ", 0, ret, -1, -1, "arial", 25, 0,
                            0, 0);
                    ret += printer.prn_drawTextEx(" ", 0, ret, -1, -1, "arial", 25, 0,
                            0, 0);
                    ret += printer.prn_drawTextEx(" ", 0, ret, -1, -1, "arial", 25, 0,
                            0, 0);
                    ret += printer.prn_drawTextEx(" ", 0, ret, -1, -1, "arial", 25, 0,
                            0, 0);
                    printer.prn_printPage(0);
                    printer.prn_close();
                } catch (Exception e) {

                }

            }
        });
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

