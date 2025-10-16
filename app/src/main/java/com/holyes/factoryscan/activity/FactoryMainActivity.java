package com.holyes.factoryscan.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.example.ccssend5.R;
import com.holyes.ccssend5.lib.bluetooth.BluetoothManager;

/**
 * @ClassName: FactoryMainActivity
 * @Description: 供应商进入主界面
 * @Author: lijin
 * @Date: 2025/7/31 13:39
 */
public class FactoryMainActivity extends Activity {
    private Context mContext;

    private Button btn_scan_box,btn_my_box,btn_box_details,btn_box_summary;//装盒扫描，我的装盒，装盒明细，装盒汇总

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_factorymain);

        mContext=this;

        btn_scan_box=findViewById(R.id.btn_scan_box);
        btn_my_box=findViewById(R.id.btn_my_box);
        btn_box_details=findViewById(R.id.btn_box_details);
        btn_box_summary=findViewById(R.id.btn_box_summary);

        btn_scan_box.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(mContext,Holyes_Dv_Factory_PackBox_NoBill.class);
                startActivity(intent);
            }
        });

        btn_box_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(mContext,SelectAllFactoryScanDetail.class);
                intent.putExtra("lsv_aim", "装盒明细");
                startActivity(intent);
            }
        });

        btn_my_box.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(mContext,SelectFactoryScanDetail.class);
                intent.putExtra("lsv_aim", "我的装盒");
                startActivity(intent);
            }
        });
        btn_box_summary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(mContext,SelectAllFactoryBoxDetail.class);
                intent.putExtra("lsv_aim", "装盒汇总");
                startActivity(intent);
            }
        });

    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // 返回主界面时断开蓝牙连接
        BluetoothManager bluetoothManager = BluetoothManager.getInstance();
        if (bluetoothManager.isBluetoothConnected()) {
            bluetoothManager.disconnect();
        }
    }
}
