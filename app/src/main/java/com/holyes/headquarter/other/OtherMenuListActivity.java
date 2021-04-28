package com.holyes.headquarter.other;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;

import com.example.ccssend5.R;
import com.holyes.ccssend5.entity.MenuChild;
import com.holyes.ccssend5.entity.MenuGroup;
import com.holyes.ccssend5.lib.SqliteDataHelper;
import com.holyes.ccssend5.lib.SysUserInfo;
import com.holyes.ccssend5.select.SelectAllots;
import com.holyes.ccssend5.select.SelectOutStock;
import com.holyes.ccssend5.select.SelectPeiBill;
import com.holyes.ccssend5.select.SelectSetmeal;
import com.holyes.ccssend5.select.SelectStock;
import com.holyes.ccssend5.utils.DisplayUtil;
import com.holyes.headquarter.adapter.OtherMenuListAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: OtherMenuListActivity
 * @Description: 菜单列表界面
 * @Author: lijin
 * @Date: 2021/3/10 13:59
 */
public class OtherMenuListActivity extends Activity {


    private Context mContext;
    private SysUserInfo sysUserInfo;
    private OtherMenuListAdapter menuListViewAdapter;

    private Button btn_back, btn_confirm;
    private ListView eListView;

    private ArrayList<MenuGroup> groups;

    private String lsv_aim;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayUtil.setDefaultDisplay(this);
        setContentView(R.layout.other_menu_list_layout);
        mContext = this;
        sysUserInfo = new SysUserInfo(this);
        eListView = (ListView) findViewById(R.id.menu_listview);
        lsv_aim = getIntent().getStringExtra("aim");
        //获取数据
        groups = ininListViewData(lsv_aim);
        menuListViewAdapter = new OtherMenuListAdapter(OtherMenuListActivity.this, groups);
        eListView.setAdapter(menuListViewAdapter);
        eListView.setOnItemClickListener(new OnItemClick());
        btn_back = (Button) findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                finish();
            }
        });
        btn_confirm = (Button) findViewById(R.id.btn_confirm);


    }

    class OnItemClick implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            MenuGroup menuGroup = (MenuGroup) parent.getItemAtPosition(position);
            goToNextScanPage(menuGroup.getMenuCode());
        }

    }

    /**
     * 跳转到下一个扫描界面
     *
     * @param menucode 菜单编号
     */
    public void goToNextScanPage(String menucode) {
        Intent intent = null;
        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>---其他扫描--->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        //总公司仓库有单调拨
        if ("0701".equals(menucode)) {
            intent = new Intent(mContext, SelectAllots.class);
            intent.putExtra("aim", "P_Dv_InStock_Z_ChangeStock_Bill");
        }
        //总公司仓库无单调拨
        else if ("0702".equals(menucode)) {
            intent = new Intent(mContext, SelectOutStock.class);
            intent.putExtra("aim", "P_Dv_InStock_Z_ChangeStock_NoBill");
        }


        //产品换标
        else if ("0703".equals(menucode)) {
            intent = new Intent(mContext, P_Dv_InStock_Z_ChangeCode.class);
        }


        //总公司吊牌回收
        else if ("0704".equals(menucode)) {
            intent = new Intent(mContext, P_Dv_RecoverScan_Z.class);
            intent.putExtra("aim", "P_Dv_RecoverScan_Z");
        }

        //入库换型号
        else if ("0705".equals(menucode)) {
//			intent = new Intent(mContext,SelectSupplier.class);
            intent = new Intent(mContext, P_Dv_InStock_Z_ChangeProduct.class);
            intent.putExtra("aim", "P_Dv_InStock_Z_ChangeProduct");
        }

        //退货直通车
        else if ("0706".equals(menucode)) {
            intent = new Intent(mContext, SelectPeiBill.class);
            intent.putExtra("aim", "P_Dv_L_Return_Z_Bill");
        }
        //补打盒标
        else if ("0707".equals(menucode)) {
            intent = new Intent(mContext, P_Dv_InStock_PackBox_Search.class);
        } else if ("0708".equals(menucode)) {
            intent = new Intent(mContext, SelectStock.class);
            intent.putExtra("aim", "P_Dv_MendLable_Z");
        }
        //选择套餐
        else if ("0709".equals(menucode)) {
            intent = new Intent(mContext, SelectSetmeal.class);
            intent.putExtra("aim", "SelectSetmeal");
        }

        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>---物流查询--->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

        //产品物流查询
//		else if("0801".equals(menucode))
//		{
//			intent = new Intent(mContext,P_ProductLogist.class);
//		}


        if (intent != null) {
            startActivity(intent);
        }

    }


    /**
     * 获取适配菜单列表的数据
     *
     * @param aim
     * @return
     * @author van van.shu@magic-point.com
     * @version 创建时间：2017-9-14 下午5:20:15
     */
    public ArrayList<MenuGroup> ininListViewData(String aim) {

        /**存放MenuGroup数据的集合*/
        ArrayList<MenuGroup> groupDatas = new ArrayList<MenuGroup>();
        MenuGroup menuGroup;//选项头
        MenuChild child;//子选项
        boolean isChildChecked = false;//子选项是否已经勾选
        List<Map<String, Object>> oneGroupMenuList;//一个主菜单下的子菜单集合
        //菜单小标题数据
        String title[] = getMenuTitle(aim);
        String parentCode;
        //下面的是子选项数据
        for (int i = 0; i < title.length; i++) {
            try {
                parentCode = SqliteDataHelper.getHelper(getApplicationContext()).execSQLString("select menucode from menus where menuname ='" + title[i] + "'");
                menuGroup = new MenuGroup(parentCode, title[i], false);
                oneGroupMenuList = SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList("select  menucode,menuname,procedurename from menus where parentcode " +
                        " = '" + parentCode + "' and showstatus = '1' ", null);
                for (Map<String, Object> map : oneGroupMenuList) {
                    child = new MenuChild(map.get("menucode").toString(), map.get("menuname").toString(), isChildChecked);
                    menuGroup.addChildrenItem(child);
                }
                groupDatas.add(menuGroup);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        return groupDatas;

    }

    /**
     * 获取小标题的内容
     *
     * @param aim
     * @return
     * @author van van.shu@magic-point.com
     * @version 创建时间：2017-9-14 上午11:34:53
     */
    public String[] getMenuTitle(String aim) {

        String parentCode = "07";
        if ("otherscan".equals(aim)) {
            //			title = new String[]{"仓库调拨","其他"};
        }

        String sql = "select menuname,menucode from menus where parentcode = '" + parentCode + "' and showstatus ='1'";
        List<Map<String, String>> list = SqliteDataHelper.getHelper(mContext).exeselect(sql);
        String title[] = new String[list.size()];
        for (int i = 0; i < title.length; i++) {
            title[i] = list.get(i).get("menuname").toString();
        }
        return title;
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

