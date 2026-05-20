package com.holyes.agent.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.annotation.NonNull;

import com.example.ccssend5.R;
import com.holyes.ccssend5.lib.ShowMessage;
import com.holyes.ccssend5.lib.SysUserInfo;
import com.holyes.ccssend5.utils.SomeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: DOtherScanMenu
 * @Description: 代理商模式--》其它扫描
 * @Author: lijin
 * @Date: 2021/3/5 17:21
 */
public class DOtherScanMenu extends Activity {
    //{{自定义变量
    @SuppressWarnings("unused")
    private Context mContext;
    private Handler hand;
    private ListView listview;
    String[] menuArray = new String[]{"物流查询", "吊牌回收", "调货单"};//
    private Intent intent = null;
    private SysUserInfo sysUserInfo;


    //{{系统事件
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.otherscan_menu6);
        mContext = this;
        sysUserInfo=new SysUserInfo(mContext);
        hand = new handShowMsg();
        ((Button) findViewById(R.id.btn_exit)).setOnClickListener(new btn_exit_click());
        listview = (ListView) findViewById(R.id.lst_menu);
        List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < menuArray.length; i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("icon", R.drawable.state2);
            map.put("menu", menuArray[i]);
            data.add(map);
        }
        SimpleAdapter adapter = new SimpleAdapter(this, data, R.layout.listrow, new String[]{"icon", "menu"}, new int[]{R.id.listrowico, R.id.listrowmenu});
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new listViewClick());

    }

    private class handShowMsg extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ShowMessage.HandShowMessage: // 显示错误提示显示
                    ShowMessage.Show(DOtherScanMenu.this, msg.obj.toString());
                    break;
                default:
                    // MyProgressDialog.close();
                    // ShowMessage.Show(Xundian.this,msg.obj.toString());
                    break;
            }
            super.handleMessage(msg);
        }
    }
    //}}

    //{{按钮事件

    private class btn_exit_click implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (SomeUtils.isDoubleClick(DOtherScanMenu.this, true)) {
                finish();
            }
        }
    }

    private class listViewClick implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {


            ListView listView = (ListView) parent;

            Map<String, Object> map = (Map<String, Object>) listView
                    .getItemAtPosition(position);


//            if (sysUserInfo.getAgentVersionNum().equals("V17")){
//                ShowMessage.Show(mContext,"第七代代理商功能正在开发中");
//            }else {
                String menuName = map.get("menu").toString();
                if (menuArray[0].equals(menuName)) {
                    //物流查询
                    intent = new Intent(mContext, BarcodeLogistics.class);
                } else if (menuArray[1].equals(menuName)) {
                    //吊牌回收
                    intent = new Intent(mContext, P_Dv_RecoverScan_D.class);
                } else if (menuArray[2].equals(menuName)) {
                    //调货单
                    intent = new Intent(mContext, SelectShippingOrder.class);
                }
                if (intent != null) {
                    startActivity(intent);
                }
//            }
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

