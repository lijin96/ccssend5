package com.holyes.headquarter.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.ccssend5.R;
import com.holyes.ccssend5.entity.MenuChild;
import com.holyes.ccssend5.entity.MenuGroup;
import com.holyes.ccssend5.lib.SortMenuListComparator;
import com.holyes.ccssend5.lib.SqliteDataHelper;
import com.holyes.ccssend5.lib.SysUserInfo;
import com.holyes.ccssend5.select.SelectCompanyD;
import com.holyes.ccssend5.select.SelectCompanyZY;
import com.holyes.ccssend5.select.SelectDistribution;
import com.holyes.ccssend5.select.SelectPeiBill;
import com.holyes.ccssend5.select.SelectPurcheck;
import com.holyes.ccssend5.select.SelectSupplier;
import com.holyes.ccssend5.utils.DisplayUtil;
import com.holyes.ccssend5.utils.SomeUtils;
import com.holyes.headquarter.adapter.ClickMenuListViewAdapter;
import com.holyes.headquarter.backgoods_d.P_Dv_ReturnedPurchase_Z_D_Bill_Cancel;
import com.holyes.headquarter.backgoods_d.P_Dv_ReturnedPurchase_Z_D_Bill_Detail_Cancel;
import com.holyes.headquarter.backgoods_d.P_Dv_ReturnedPurchase_Z_D_NoBill_Cancel;
import com.holyes.headquarter.backgoods_zy.P_Dv_ReturnedMendLabel_HaveNoBill_Cancel;
import com.holyes.headquarter.backgoods_zy.P_Dv_ReturnedPurchase_Z_L_Bill_Cancel;
import com.holyes.headquarter.backgoods_zy.P_Dv_ReturnedPurchase_Z_L_Bill_Detail_Cancel;
import com.holyes.headquarter.backgoods_zy.P_Dv_ReturnedPurchase_Z_L_NoBill_Cancel;
import com.holyes.headquarter.instock_back.P_Dv_ReturnedPurchase_Z_G_Bill_Cancel;
import com.holyes.headquarter.instock_back.P_Dv_ReturnedPurchase_Z_G_NoBill;
import com.holyes.headquarter.instock_back.P_Dv_ReturnedPurchase_Z_G_NoBill_Cancel;
import com.holyes.headquarter.instock_in.P_Dv_InStock_Bill_Cancel;
import com.holyes.headquarter.instock_in.P_Dv_InStock_NoBill_Cancel;
import com.holyes.headquarter.sendgoods_d.P_Dv_OutStock_Z_D_Bill_Cancel;
import com.holyes.headquarter.sendgoods_d.P_Dv_OutStock_Z_D_NoBill_Cancel;
import com.holyes.headquarter.sendgoods_zy.P_Dv_OutStock_Z_L_Bill_Cancel;
import com.holyes.headquarter.sendgoods_zy.P_Dv_OutStock_Z_L_NoBill_Cancel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: MenuListActivity
 * @Description: 菜单列表界面
 * @Author: lijin
 * @Date: 2021/3/10 10:16
 */
public class MenuListActivity extends Activity {

    private Context mContext;
    private SysUserInfo sysUserInfo;
    private ClickMenuListViewAdapter menuListViewAdapter;

    private Button btn_back, btn_confirm;
    private ExpandableListView eListView;
    private TextView tv_menu_title;

    private ArrayList<MenuGroup> groups;

    private String lsv_aim, parentCode = "01";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayUtil.setDefaultDisplay(this);
        setContentView(R.layout.new_menu_list_layout);
        mContext = this;
        sysUserInfo = new SysUserInfo(this);
        eListView = (ExpandableListView) findViewById(R.id.menu_listview);
        lsv_aim = getIntent().getStringExtra("aim");
        tv_menu_title = (TextView) findViewById(R.id.tv_menu_title);
        tv_menu_title.setText(getTitleTvText(lsv_aim) + "功能菜单");

        btn_back = (Button) findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                finish();
            }
        });
        btn_confirm = (Button) findViewById(R.id.btn_confirm);

        //获取数据
        groups = ininListViewData(lsv_aim);

        eListView.setGroupIndicator(null);//把menuListView的默认箭头去掉
        menuListViewAdapter = new ClickMenuListViewAdapter(MenuListActivity.this, groups, sysUserInfo);
        eListView.setAdapter(menuListViewAdapter);

        //默认全部展开
        int groupCount = eListView.getCount();
        for (int i = 0; i < groupCount; i++) {
            eListView.expandGroup(i);
        }


    }


    /**
     * 跳转到下一个扫描界面
     *
     * @param menucode 菜单编号
     */
    public void goToNextScanPage(String menucode) {
        //		Log.i("main", "menucode----"+menucode);
        Intent intent = null;
        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>---产品入库--->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        //有单入库
        if ("010101".equals(menucode)) {
            //先选验收单
            intent = new Intent(mContext, SelectPurcheck.class);
            intent.putExtra("aim", "P_Dv_InStock_Bill");
        }
        //无单入库
        else if ("010102".equals(menucode)) {
            //先选供应商，再选仓库
            intent = new Intent(mContext, SelectSupplier.class);
            intent.putExtra("aim", "P_Dv_InStock_NoBill");
        }
        //装盒入库
        else if ("010103".equals(menucode)) {
            //先选供应商，再选仓库
            intent = new Intent(mContext, SelectSupplier.class);
            //			intent.putExtra("aim", "P_Dv_InStock_PackBox_NoBill");
            intent.putExtra("aim", "P_Dv_InStock_PackBox_List_NoBill");

        }

        //有单入库撤销
        else if ("010201".equals(menucode)) {
            intent = new Intent(mContext, P_Dv_InStock_Bill_Cancel.class);
        }
        //无单入库撤销
        else if ("010202".equals(menucode)) {
            intent = new Intent(mContext, P_Dv_InStock_NoBill_Cancel.class);
        }
        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>---入库退回--->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        //入库退回（有单）
        else if ("020101".equals(menucode)) {
            intent = new Intent(mContext, SelectPurcheck.class);
            intent.putExtra("aim", "P_Dv_ReturnedPurchase_Z_G_Bill");
        }
        //入库退回（无单）
        else if ("020102".equals(menucode)) {
            //选择供应商，再选择仓库
            //			intent = new Intent(mContext,SelectSupplier.class);
            intent = new Intent(mContext, P_Dv_ReturnedPurchase_Z_G_NoBill.class);
            intent.putExtra("aim", "P_Dv_ReturnedPurchase_Z_G_NoBill");
        }

        //有单入库退回撤销
        else if ("020201".equals(menucode)) {
            intent = new Intent(mContext, P_Dv_ReturnedPurchase_Z_G_Bill_Cancel.class);
        }


        //入库退回撤销（无单）
        else if ("020202".equals(menucode)) {
            intent = new Intent(mContext, P_Dv_ReturnedPurchase_Z_G_NoBill_Cancel.class);
        }
        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>---代销发货--->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        //有单有入库(代销)发货
        else if ("030101".equals(menucode)) {
            intent = new Intent(mContext, SelectPeiBill.class);
            intent.putExtra("aim", "P_Dv_OutStock_Z_D_Bill_BeInStock");
        }
        //有单无入库(代销)发货
        else if ("030102".equals(menucode)) {
            intent = new Intent(mContext, SelectPeiBill.class);
            intent.putExtra("aim", "P_Dv_OutStock_Z_D_Bill_NoInStock");

        }
        //无单有入库代销发货>>>>不选仓库了2018-04-25
        else if ("030103".equals(menucode)) {
            intent = new Intent(mContext, SelectCompanyD.class);
            intent.putExtra("aim", "P_Dv_OutStock_Z_D_NoBill_BeInStock");
        }


        //无单无入库代销发货
        else if ("030104".equals(menucode)) {
            intent = new Intent(mContext, SelectCompanyD.class);
            intent.putExtra("aim", "P_Dv_OutStock_Z_D_NoBill_NoInStock");
        }

        //有单发货撤销
        else if ("030201".equals(menucode)) {
            intent = new Intent(mContext, P_Dv_OutStock_Z_D_Bill_Cancel.class);
            intent.putExtra("aim", "P_Dv_OutStock_Z_D_Bill_Cancel");
        }

        //无单代销发货撤消
        else if ("030202".equals(menucode)) {
            intent = new Intent(mContext, P_Dv_OutStock_Z_D_NoBill_Cancel.class);
        }


        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>---代销退货--->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

        //有单无明细代销退货
        else if ("040101".equals(menucode)) {
            intent = new Intent(mContext, SelectPeiBill.class);
            intent.putExtra("aim", "P_Dv_ReturnedPurchase_Z_D_Bill");
        }
        //有单有明细代销退货
        else if ("040102".equals(menucode)) {
            intent = new Intent(mContext, SelectPeiBill.class);
            intent.putExtra("aim", "P_Dv_ReturnedPurchase_Z_D_Bill_Detail");
        }

        //无单代销退货
        else if ("040103".equals(menucode)) {
            intent = new Intent(mContext, SelectCompanyD.class);
            intent.putExtra("aim", "P_Dv_ReturnedPurchase_Z_D_NoBill");
        }
        //代销退货补标
        else if ("040104".equals(menucode)) {
            intent = new Intent(mContext, SelectPeiBill.class);
            intent.putExtra("aim", "P_Dv_ReturnedMendLabel_Z_D_HaveNoBill");
        }
        //有单无明细代销退货撤销
        else if ("040201".equals(menucode)) {
            intent = new Intent(mContext, P_Dv_ReturnedPurchase_Z_D_Bill_Cancel.class);
            intent.putExtra("aim", "P_Dv_ReturnedPurchase_Z_D_Bill_Cancel");
        }


        //有单有明细代销退货撤销
        else if ("040202".equals(menucode)) {
            intent = new Intent(mContext, P_Dv_ReturnedPurchase_Z_D_Bill_Detail_Cancel.class);
            intent.putExtra("aim", "P_Dv_ReturnedPurchase_Z_D_Bill_Detail_Cancel");
        }
        //无单代销退货撤销
        else if ("040203".equals(menucode)) {
            intent = new Intent(mContext, P_Dv_ReturnedPurchase_Z_D_NoBill_Cancel.class);
        }

        //代销补标撤消
        else if ("040204".equals(menucode)) {
            intent = new Intent(mContext, P_Dv_ReturnedMendLabel_HaveNoBill_Cancel.class);
            intent.putExtra("aim", "040204");
        }


        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>---直销发货--->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

        //直营有入库有单发货
        else if ("050101".equals(menucode)) {
            intent = new Intent(mContext, SelectPeiBill.class);
            intent.putExtra("aim", "P_Dv_OutStock_Z_L_Bill_BeInStock");
        }
        //直营无入库有单发货
        else if ("050102".equals(menucode)) {
            intent = new Intent(mContext, SelectPeiBill.class);
            intent.putExtra("aim", "P_Dv_OutStock_Z_L_Bill_NoInStock");
        }
        //直营有入库无单发货>>>>不选仓库了2018-04-25
        else if ("050103".equals(menucode)) {
            intent = new Intent(mContext, SelectCompanyZY.class);
            intent.putExtra("aim", "P_Dv_OutStock_Z_L_NoBill_BeInStock");
        }
        //直营有入库无单发货分销
        else if ("050105".equals(menucode)) {
            intent = new Intent(mContext, SelectDistribution.class);
            intent.putExtra("aim", "P_Dv_OutStock_Z_L_S_NoBill");
        }
        //直营无入库无单发货
        else if ("050104".equals(menucode)) {
            intent = new Intent(mContext, SelectCompanyZY.class);
            intent.putExtra("aim", "P_Dv_OutStock_Z_L_NoBill_NoInStock");
        }

        //有单直销发货撤消
        else if ("050201".equals(menucode)) {
            intent = new Intent(mContext, P_Dv_OutStock_Z_L_Bill_Cancel.class);
        }

        //直营撤销发货（无单）
        else if ("050202".equals(menucode)) {
            intent = new Intent(mContext, P_Dv_OutStock_Z_L_NoBill_Cancel.class);
            intent.putExtra("aim", "P_Dv_OutStock_Z_L_NoBill_Cancel");
        }

        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>---直销退货--->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>


        //直营有单无明细退货
        else if ("060101".equals(menucode)) {
            intent = new Intent(mContext, SelectPeiBill.class);
            intent.putExtra("aim", "P_Dv_ReturnedPurchase_Z_L_Bill");
        }
        //有单有明细直销退货
        else if ("060102".equals(menucode)) {
            intent = new Intent(mContext, SelectPeiBill.class);
            intent.putExtra("aim", "P_Dv_ReturnedPurchase_Z_L_Bill_Detail");
        }

        //无单直销退货
        else if ("060103".equals(menucode)) {
            intent = new Intent(mContext, SelectCompanyZY.class);
            intent.putExtra("aim", "P_Dv_ReturnedPurchase_Z_L_NoBill");
        }
        //		//无单(直营分店)退货
        //		else if("060104".equals(menucode))
        //		{
        //			intent = new Intent(mContext,SelectDistribution.class);
        //			intent.putExtra("aim", "P_Dv_ReturnedPurchase_Z_S_NoBill");
        //		}

        //直销退货补标
        else if ("060104".equals(menucode)) {
            intent = new Intent(mContext, SelectPeiBill.class);
            intent.putExtra("aim", "P_Dv_ReturnedMendLabel_Z_L_HaveNoBill");
        }


        //直营有单无明细退货撤销
        else if ("060201".equals(menucode)) {
            intent = new Intent(mContext, P_Dv_ReturnedPurchase_Z_L_Bill_Cancel.class);
            intent.putExtra("aim", "P_Dv_ReturnedPurchase_Z_L_Bill_Cancel");
        }
        //直营有单有明细退货撤销
        else if ("060202".equals(menucode)) {
            intent = new Intent(mContext, P_Dv_ReturnedPurchase_Z_L_Bill_Detail_Cancel.class);
            intent.putExtra("aim", "P_Dv_ReturnedPurchase_Z_L_Bill_Detail_Cancel");
        }

        //无单直销退货撤销
        else if ("060203".equals(menucode)) {
            intent = new Intent(mContext, P_Dv_ReturnedPurchase_Z_L_NoBill_Cancel.class);
        }

        //直销补标撤消
        else if ("060204".equals(menucode)) {
            intent = new Intent(mContext, P_Dv_ReturnedMendLabel_HaveNoBill_Cancel.class);
            intent.putExtra("aim", "060204");
        }

        //>>>>>>>>>>>>>>>---其他扫描（下面的功能在另外的OtherMenuListActivity里面--->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        //总公司仓库有单调拨
        //		else if("0701".equals(menucode))
        //		{
        //			intent = new Intent(mContext,SelectAllots.class);
        //			intent.putExtra("aim", "P_Dv_InStock_Z_ChangeStock_Bill");
        //		}
        //		//总公司仓库无单调拨
        //		else if("0702".equals(menucode))
        //		{
        //			intent = new Intent(mContext,SelectOutStock.class);
        //			intent.putExtra("aim", "P_Dv_InStock_Z_ChangeStock_NoBill");
        //		}
        //
        //
        //		//产品换标
        //		else if("0703".equals(menucode))
        //		{
        //			intent = new Intent(mContext,P_Dv_InStock_Z_ChangeCode.class);
        //		}
        //
        //
        //		//总公司吊牌回收
        //		else if("0704".equals(menucode))
        //		{
        //			intent = new Intent(mContext,P_Dv_RecoverScan_Z.class);
        //			intent.putExtra("aim", "P_Dv_RecoverScan_Z");
        //		}
        //
        //		//入库换型号
        //		else if("0705".equals(menucode))
        //		{
        //			intent = new Intent(mContext,SelectSupplier.class);
        //			intent.putExtra("aim", "P_Dv_InStock_Z_ChangeProduct");
        //		}
        //
        //		//退货直通车
        //		else if("0706".equals(menucode))
        //		{
        //			intent = new Intent(mContext,SelectPeiBill.class);
        //			intent.putExtra("aim", "P_Dv_L_Return_Z_Bill");
        //		}
        //
        //		//补打盒标
        //		else if("0707".equals(menucode))
        //		{
        //			intent = new Intent(mContext,P_Dv_InStock_PackBox_Search.class);
        //		}
        //
        //		//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>---物流查询--->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        //
        //		//产品物流查询
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
        String menuNameArray[] = SomeUtils.mapListToStringArray(getMenuTitle(lsv_aim), "menuname");
        String menuCodeArray[] = SomeUtils.mapListToStringArray(getMenuTitle(lsv_aim), "menucode");
        String parentCode;
        //下面的是子选项数据
        for (int i = 0; i < menuNameArray.length; i++) {
            try {
                parentCode = menuCodeArray[i];
                menuGroup = new MenuGroup(parentCode, menuNameArray[i], false);
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
        //下载的数据可能顺序不对，可能导致菜单位置不对，先排序一下
        Collections.sort(groupDatas, new SortMenuListComparator());
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
    public List<Map<String, Object>> getMenuTitle(String aim) {


        String sql = "select menuname,menucode from menus where parentcode = '" + parentCode + "' and showstatus ='1'";
        List<Map<String, Object>> list = SqliteDataHelper.getHelper(mContext).QueryDbList(sql, null);

        return list;
    }

    public String getTitleTvText(String aim) {
        String title = "";
        if ("instock_in".equals(aim)) {
            title = "产品入库";
            parentCode = "01";

        } else if ("instock_back".equals(aim)) {
            title = "入库退回";
            parentCode = "02";
        } else if ("sendgoods_d".equals(aim)) {
            title = "代销发货";
            parentCode = "03";
        } else if ("backgoods_d".equals(aim)) {
            title = "代销退货";
            parentCode = "04";

        } else if ("sendgoods_zy".equals(aim)) {
            title = "直销发货";
            parentCode = "05";

        } else if ("backgoods_zy".equals(aim)) {
            title = "直销退货";
            parentCode = "06";
        } else if ("otherscan".equals(aim)) {
            title = "其他扫描";
            parentCode = "07";
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

