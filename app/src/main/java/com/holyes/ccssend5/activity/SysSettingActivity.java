package com.holyes.ccssend5.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.ccssend5.R;
import com.holyes.ccssend5.lib.ADevicesManager;
import com.holyes.ccssend5.lib.AccessWeb;
import com.holyes.ccssend5.lib.ShowMessage;
import com.holyes.ccssend5.lib.SqliteDataHelper;
import com.holyes.ccssend5.lib.SysUserInfo;
import com.holyes.ccssend5.lib.UpdateManager2;
import com.holyes.ccssend5.myview.MyProgressDialog;
import com.holyes.ccssend5.utils.DisplayUtil;
import com.holyes.ccssend5.utils.PrintUtil;
import com.holyes.ccssend5.utils.SomeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: SysSettingActivity
 * @Description: 设置界面
 * @Author: lijin
 * @Date: 2021/3/6 14:12
 */
public class SysSettingActivity extends Activity {

    @SuppressWarnings("unused")
    private Context mContext;
    private Handler hand;
    private SysUserInfo sysUserInfo;

    private EditText et_ip, et_port, et_SoftType, et_EnterpriseID;


    //{{系统事件
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        DisplayUtil.setDefaultDisplay(this);
        setContentView(R.layout.syssetting2);
        mContext = this;
        hand = new handShowMsg();

        ((Button) findViewById(R.id.btn_testnet)).setOnClickListener(new btn_testnet_click());
        ((Button) findViewById(R.id.btn_clear)).setOnClickListener(new btn_getclear_click());
        ((Button) findViewById(R.id.btn_ok)).setOnClickListener(new btn_ok_click());
        ((Button) findViewById(R.id.btn_exit)).setOnClickListener(new btn_exit_click());
        ((Button) findViewById(R.id.btn_print)).setOnClickListener(new btn_print_click());
        ((Button) findViewById(R.id.btn_getmenu)).setOnClickListener(new btn_getmenuclick());
        ((Button) findViewById(R.id.btn_to_v4)).setOnClickListener(new BtnToV4Click());
        ((Button) findViewById(R.id.btn_cleanbrand)).setOnClickListener(new btn_Clean_brand_click());

        sysUserInfo = new SysUserInfo(this);
        et_ip = (EditText) findViewById(R.id.txtIP);
        et_port = (EditText) findViewById(R.id.txtport);
        et_SoftType = (EditText) findViewById(R.id.txtSoftType);
        et_EnterpriseID = (EditText) findViewById(R.id.txtEnterpriseID);


        LoadInitParament();
    }

    private class handShowMsg extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ShowMessage.HandShowMessage: // 显示错误提示显示
                    ShowMessage.Show(SysSettingActivity.this, msg.obj.toString());
                    break;
                default:
                    // MyProgressDialog.close();
                    // ShowMessage.Show(Xundian.this,msg.obj.toString());
                    break;
            }
            super.handleMessage(msg);
            MyProgressDialog.close();
        }
    }

    // {{按钮事件
    private class btn_testnet_click implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            Uri content_url = Uri.parse("http://www.baidu.com");
            intent.setData(content_url);
            startActivity(intent);
        }
    }

    private class btn_print_click implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            PrintUtil printbill = new PrintUtil();
            String[] mark = new String[3];
            mark[0] = "零售单：DF-BillNo12345";
            mark[1] = "分销单：LF-BillNo12345-87654321";
            mark[2] = "分销店：广东省深圳市龙华新区民治街道泰明工业区一栋三层B区分销测试店";

            List<Map<String, Object>> sacnDataList = new ArrayList<Map<String, Object>>();

            Map<String, Object> map = new HashMap<String, Object>();
//			 map.put("modelm", "B67100");
//			 map.put("colors", "P01");
//			 map.put("curcount", "5");
//			 map.put("product_id", "B67100-P01");
//			 slist.add(0, map);

            for (int i = 0; i < 5; i++) {
                map = new HashMap<String, Object>();
                map.put("modelm", "B67100" + i);
                map.put("colors", "B67100-P01");
                map.put("curcount", "3" + i);
                map.put("product_id", "B67100-P012");
                sacnDataList.add(map);
            }
            printbill.print(SysSettingActivity.this, "    无单无入库直销发货", mark, sacnDataList, sysUserInfo.getUserid());


        }
//			Intent printIntent =new Intent(SysSettingActivity.this, PrintActivity.class);
//			startActivity(printIntent);
//			PrinterManager printer = new PrinterManager();
//			try {
//				printer.prn_open();
//				printer.prn_setupPage(384, -1);
//				int ret = printer.prn_drawTextEx("        产品发货单", 0, 0, -1, -1,
//						"arial", 38, 0, 0, 0);
//				ret += printer.prn_drawTextEx("店名：" + "深圳合力思科技", 0, ret, -1, -1,
//						"arial", 25, 0, 0, 0);
//				ret += printer.prn_drawTextEx("DX-000000001", 0, ret, -1, -1, "arial", 25,
//						0, 0, 0);
//				ret += printer.prn_drawTextEx(
//						"-------------------------------------", 0, ret, -1, -1,
//						"arial", 25, 0, 0, 0);
//				printer.prn_drawTextEx("        型号-色号 ", 0, ret, -1, -1, "arial",
//						25, 0, 0, 0);
//				ret += printer.prn_drawTextEx("数量", 300, ret, -1, -1, "arial", 25,
//						0, 0, 0);
//				ret += printer.prn_drawTextEx(
//						"-------------------------------------", 0, ret, -1, -1,
//						"arial", 25, 0, 0, 0);
//				int sum = 0;
//				String product = "";
//
//
//				printer.prn_drawTextEx("sp1-c1", 0, ret, -1, -1, "arial",
//						25, 0, 0, 0);
//
//				ret += printer.prn_drawTextEx("5",
//						300, ret, -1, -1, "arial", 25, 0, 0, 0);
//
//
//				ret += printer.prn_drawTextEx(
//						"-------------------------------------", 0, ret, -1, -1,
//						"arial", 25, 0, 0, 0);
//				ret += printer.prn_drawTextEx("合计：" + sum + "      打单:" + "张三",
//						0, ret, -1, -1, "arial", 25, 0, 0, 0);
//				SimpleDateFormat sDateFormat = new SimpleDateFormat(
//						"yyyy-MM-dd HH:mm:ss");
//				ret += printer.prn_drawTextEx(
//						"日期时间：" + sDateFormat.format(new java.util.Date()), 0, ret,
//						-1, -1, "arial", 25, 0, 0, 0);
//				ret += printer.prn_drawTextEx(" ", 0, ret, -1, -1, "arial", 25, 0,
//						0, 0);
//				ret += printer.prn_drawTextEx(" ", 0, ret, -1, -1, "arial", 25, 0,
//						0, 0);
//				ret += printer.prn_drawTextEx(" ", 0, ret, -1, -1, "arial", 25, 0,
//						0, 0);
//				ret += printer.prn_drawTextEx(" ", 0, ret, -1, -1, "arial", 25, 0,
//						0, 0);
//				printer.prn_printPage(0);
//				printer.prn_close();
//			} catch (Exception e) {
//
//			}
//
//		}
    }


    //清除数据
    private class btn_getclear_click implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            //			SQLiteWork dtWork = new SQLiteWork(getApplicationContext());

            if (!SqliteDataHelper.getHelper(getApplicationContext()).CleanDataBase()) {
                ShowMessage.Show(SysSettingActivity.this, "清除失败！");
            } else {
                ShowMessage.Show(SysSettingActivity.this, "清除完成！");
            }

        }
    }

    private class btn_ok_click implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            if (!(et_EnterpriseID).getText().toString().equals(sysUserInfo.getEnterpriseId())) {
                //				SQLiteWork dtWork = new SQLiteWork(getApplicationContext());

                try {
                    SqliteDataHelper.getHelper(getApplicationContext()).execSQL("delete from company");

                    SqliteDataHelper.getHelper(getApplicationContext()).execSQL("delete  from stock");

                    SqliteDataHelper.getHelper(getApplicationContext()).execSQL("delete  from product");


                } catch (Exception e) {

                    e.printStackTrace();
                }
            }

            sysUserInfo.setServerIp(et_ip.getText().toString());
            sysUserInfo.setServerport(et_port.getText().toString());
            sysUserInfo.setSoftType(et_SoftType.getText().toString());
            sysUserInfo.setEnterpriseId(et_EnterpriseID.getText().toString());
            sysUserInfo.setClientId(et_EnterpriseID.getText().toString());//>>>
            finish();
        }
    }

    private class btn_exit_click implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (SomeUtils.isDoubleClick(SysSettingActivity.this, true)) {
                finish();
            }
        }
    }

    private class btn_Clean_brand_click implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            sysUserInfo.setClientId("");
            try {
                SqliteDataHelper.getHelper(getApplicationContext()).execSQL("delete  from brandinfo");

                SqliteDataHelper.getHelper(getApplicationContext()).execSQL("delete  from stock");

                SqliteDataHelper.getHelper(getApplicationContext()).execSQL("delete  from product");

                SqliteDataHelper.getHelper(getApplicationContext()).execSQL("delete  from company");

                ShowMessage.ShowMsg(hand, "清除品牌成功");

            } catch (Exception e) {

                ShowMessage.ShowMsg(hand, e.getMessage());
            }
        }

    }


    private class BtnToV4Click implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Toast.makeText(mContext,"该功能已禁用",Toast.LENGTH_SHORT).show();
//            UpdateManager2 uManager2 = new UpdateManager2(SysSettingActivity.this);
//            if ("01".equals(sysUserInfo.getSoftType())) {
//                uManager2.checkUpdateInfo("确认下载第四代的总公司发货通？", "注意！总公司模式不能轻易转换，请跟客服联系后方可下载安装！");
//            } else {
//                uManager2.checkUpdateInfo("确认下载第四代的代理商发货通？", "");
//            }

        }
    }

    private class btn_getmenuclick implements View.OnClickListener {
        @Override
        public void onClick(View v) {


            MyProgressDialog.show(SysSettingActivity.this, "正在下载菜单数据，请稍候……", true, false);
            Thread getmenu = new Thread(new Runnable() {

                @Override
                public void run() {

                    try {
                        AccessWeb accessWeb = new AccessWeb(mContext);
                        List<Map<String, Object>> list = accessWeb.GetDevMenuInfor(sysUserInfo.getEnterpriseId());

                        if (list.size() < 1) {
                            ShowMessage.ShowMsg(hand, "菜单下载失败！");
                            return;
                        }

                        SqliteDataHelper.getHelper(getApplicationContext()).execSQL("delete from menus");
                        //把菜单插入数据库
                        List<String> sqlList = new ArrayList<String>();
                        String menucode, menuname, parentcode, showstatus, procedurename, sql = "";

                        for (Map<String, Object> map : list) {
                            menucode = map.get("menucode").toString();
                            menuname = map.get("menuname").toString();
                            parentcode = map.get("parentcode").toString();
                            showstatus = map.get("showstatus").toString();
                            procedurename = map.get("procedurename").toString();
                            sql = String.format("insert into menus(menucode,menuname,parentcode,showstatus,procedurename) values " +
                                    "('" + menucode + "','" + menuname + "','" + parentcode + "','" + showstatus + "','" + procedurename + "')");

                            sqlList.add(sql);

                        }
                        //把菜单批量插入数据库
                        SqliteDataHelper.getHelper(getApplicationContext()).BatchOperation(sqlList);

                        ShowMessage.ShowMsg(hand, "下载成功");

                    } catch (Exception e) {

                        ShowMessage.ShowMsg(hand, e.getMessage().toString());
                        return;
                    }
                }
            });

            getmenu.start();

        }
    }

    // }}

    //{{自定义函数
    private void LoadInitParament() {
        et_ip.setText(sysUserInfo.getServerip());
        SomeUtils.moveFocus(et_ip);//>>>>
        et_port.setText(sysUserInfo.getServerport());
        SomeUtils.moveFocus(et_port);
        et_SoftType.setText(sysUserInfo.getSoftType());
        SomeUtils.moveFocus(et_SoftType);
        et_EnterpriseID.setText(sysUserInfo.getEnterpriseId());
        SomeUtils.moveFocus(et_EnterpriseID);
        if (ADevicesManager.CorpCompany) {
            et_ip.setEnabled(true);
            et_port.setEnabled(true);
            et_SoftType.setEnabled(true);
            et_EnterpriseID.setEnabled(true);

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

