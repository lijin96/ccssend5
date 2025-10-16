package com.holyes.agent.activity;

import android.app.Activity;
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
import com.holyes.ccssend5.select.SelectCompanyRetailer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: DBackGoodsMenuListActivity
 * @Description: 退货功能菜单列表
 * @Author: lijin
 * @Date: 2021/3/5 17:21
 */
public class DBackGoodsMenuListActivity extends Activity {
    private Handler hand;
    private ListView listview;
    private String[] menu = new String[]{"零售退货", "退货撤销"};//,"分销退货","分销退货撤销" ,"零售退货直通车"
    private SysUserInfo sysUserInfo;

    //{{系统事件
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.new_p_dv_select_return);
        hand = new handShowMsg();
        sysUserInfo = new SysUserInfo(this);
        //不是新版本就没有撤销功能
//        if (!sysUserInfo.getIsNewVerSoft()) {
//            if(sysUserInfo.getEnterpriseId().equals("00")||sysUserInfo.getEnterpriseId().equals("19")||sysUserInfo.getEnterpriseId().equals("88")){
//                menu = new String[]{"零售退货","镜片零售退货"};//,"零售退货直通车"
//            }else{
//                menu = new String[]{"零售退货"};//,"零售退货直通车"
//            }
//        }else{
        if(sysUserInfo.getEnterpriseId().equals("00")||sysUserInfo.getEnterpriseId().equals("19")||sysUserInfo.getEnterpriseId().equals("88")){
            menu = new String[]{"零售退货","镜片零售退货", "退货撤销","镜片退货撤销"};//,"零售退货直通车"
        }else{
            menu = new String[]{"零售退货", "退货撤销"};//,"零售退货直通车"
        }
//        }

        ((Button) findViewById(R.id.btn_exit)).setOnClickListener(new btn_exit_click());
        listview = (ListView) findViewById(R.id.lst_menu);


        List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < menu.length; i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("icon", R.drawable.state2);
            map.put("menu", menu[i]);
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
                    ShowMessage.Show(DBackGoodsMenuListActivity.this, msg.obj.toString());
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
            finish();
        }
    }

    private class listViewClick implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            ListView listView = (ListView) parent;
            Map<String, Object> map = (Map<String, Object>) listView
                    .getItemAtPosition(position);
            if (sysUserInfo.getAgentVersionNum().equals("V17")){
                ShowMessage.Show(DBackGoodsMenuListActivity.this,"第七代代理商功能正在开发中");
            }else {
                String menuName = map.get("menu").toString();
                if (menuName.equals("零售退货")) // 零售退货
                {
                    Intent itent = new Intent(DBackGoodsMenuListActivity.this, P_Dv_ReturnedPurchase_D_L_NoBill.class);
                    startActivity(itent);
                } else if (menuName.equals("退货撤销")) // 退货撤销（零售）
                {
                    Intent itent = new Intent(DBackGoodsMenuListActivity.this, P_Dv_ReturnedPurchase_D_L_NoBill_Cancel.class);
                    startActivity(itent);
                } else if (menuName.equals("镜片零售退货")) //镜片零售退货
                {
                    Intent itent = new Intent(DBackGoodsMenuListActivity.this, P_Dv_ReturnedPurchase_D_L_Lens_NoBill.class);
                    startActivity(itent);
                } else if (menuName.equals("镜片退货撤销")) //镜片退货撤销
                {
                    Intent itent = new Intent(DBackGoodsMenuListActivity.this, P_Dv_ReturnedPurchase_D_L_Lens_NoBill_Cancel.class);
                    startActivity(itent);
                } else if (menuName.equals("零售退货直通车")) //零售退货直通车
                {
                    Intent intent = new Intent(DBackGoodsMenuListActivity.this, SelectCompanyRetailer.class);
                    intent.putExtra("aim", "P_Dv_ReturnedPurchase_D_L_TransferGoods");//零售退货直通车
                    startActivity(intent);
                }
            }



//			else if (menuName.equals(menu[2])) // 分销退货
//			{
//				Intent itent = new Intent(DBackGoodsMenuListActivity.this, P_Dv_ReturnedPurchase_D_F_NoBill.class);
//				startActivity(itent);
//			}
//			else if (menuName.equals(menu[3])) // 分销退货撤销
//			{
//				Intent itent = new Intent(DBackGoodsMenuListActivity.this, P_Dv_ReturnedPurchase_D_F_NoBill_Cancel.class);
//				startActivity(itent);
//			}
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


