package com.holyes.headquarter.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;

import androidx.annotation.NonNull;

import com.example.ccssend5.R;
import com.holyes.ccssend5.entity.MenuChild;
import com.holyes.ccssend5.entity.MenuGroup;
import com.holyes.ccssend5.lib.SqliteDataHelper;
import com.holyes.ccssend5.utils.DisplayUtil;
import com.holyes.ccssend5.utils.SomeUtils;
import com.holyes.headquarter.adapter.CheckMenuListViewAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: CheckingFunctionActivity
 * @Description: 勾选菜单界面
 * @Author: lijin
 * @Date: 2021/3/10 10:16
 */
public class CheckingFunctionActivity extends Activity implements View.OnClickListener {

    /**
     * 装菜单数据的集合，用来适配newmenulistViewAdapter
     */
    private ArrayList<MenuGroup> groups = new ArrayList<MenuGroup>();
    private ExpandableListView menuListView;
    private CheckMenuListViewAdapter menuListViewAdapter;
    private Button btn_back, btn_confirm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayUtil.setDefaultDisplay(this);
        setContentView(R.layout.checking_fuction);
        btn_confirm = (Button) findViewById(R.id.btn_confirm);
        btn_back = (Button) findViewById(R.id.btn_back);
        menuListView = (ExpandableListView) findViewById(R.id.menu_listview);
        menuListView.setGroupIndicator(null);//把menuListView的默认箭头去掉

        btn_confirm.setOnClickListener(this);
        btn_back.setOnClickListener(this);
        //获取数据
        groups = initDatas();

        menuListViewAdapter = new CheckMenuListViewAdapter(CheckingFunctionActivity.this, groups);
        menuListView.setAdapter(menuListViewAdapter);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_confirm:
                showConfirmDialog();
                break;
            case R.id.btn_back:
                finish();
                break;

            default:
                break;
        }
    }


    /**
     * 初始化选项头和子选项的数据
     *
     * @return groupDatas 存放MenuGroup数据的集合，给menuListViewAdapter适配数据用
     */
    public ArrayList<MenuGroup> initDatas() {
        /**存放MenuGroup数据的集合*/
        ArrayList<MenuGroup> groupDatas = new ArrayList<MenuGroup>();
        MenuGroup menuGroup;//选项头
        MenuChild child;//子选项
        boolean isChildChecked = false;//子选项是否已经勾选
        List<Map<String, Object>> oneGroupMenuList;//一个主菜单下的子菜单集合
        String selectMenuTitlesql = "select * from menus where parentcode =''";
        List<Map<String, Object>> menuTitleList = SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList(selectMenuTitlesql, null);
        //选项头数据
//		String title[]=new String[]{"产品入库","入库退回","代销发货","代销退货","直销发货","直销退货","其他扫描","物流查询"};
        String title[] = SomeUtils.mapListToStringArray(menuTitleList, "menuname");

        //下面的是子选项数据
        for (int i = 0; i < title.length; i++) {
            int liv_checkedNum = 0;//已经勾选的子选项菜单数
            menuGroup = new MenuGroup("0" + (i + 1), title[i], false);
            if (i == (title.length - 1) || i == (title.length - 2)) {
                oneGroupMenuList = SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList("select * from menus where parentcode = '0" + (i + 1) + "'", null);
            } else {
                String selMenuSql = "select * from menus where parentcode in(select menucode from menus where parentcode ='0" + (i + 1) + "')";
                oneGroupMenuList = SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList(selMenuSql, null);
            }
            for (Map<String, Object> map : oneGroupMenuList) {
                isChildChecked = false;
                if (map.get("showstatus").equals("1")) {
                    isChildChecked = true;
                    liv_checkedNum++;
                }
                child = new MenuChild(map.get("menucode").toString(), map.get("menuname").toString(), isChildChecked);
                menuGroup.addChildrenItem(child);
            }

            //目的：如果已经勾选的子选项菜单数等于其对应的主菜单集合的大小，则把主菜单的全选勾上
            if (liv_checkedNum == oneGroupMenuList.size()) {
                menuGroup.setChecked(true);
            }
            groupDatas.add(menuGroup);
        }

        return groupDatas;
    }

    /**
     * @version 创建时间：2016-12-28 下午3:53:33
     * 获取勾选菜单情况的checkMenuMap
     * checkMenuMap保存了两个集合
     * checkMenuMap.get("checked"):勾选了的菜单classname集合
     * checkMenuMap.get("unchecked"):未勾选的菜单classname集合
     */
    public Map<String, List<String>> getMenusMap() {
        Map<String, List<String>> checkMenuMap = new HashMap<String, List<String>>();
        List<String> checkedMenuClassNameList = new ArrayList<String>();
        List<String> unCheckedMenuClassNameList = new ArrayList<String>();
        for (int i = 0; i < groups.size(); i++) {
            for (int j = 0; j < groups.get(i).getChildrenCount(); j++) {
                //勾选了的加入checkedMenuClassNameList
                if (groups.get(i).getChildrenItem(j).getChecked()) {
                    checkedMenuClassNameList.add(groups.get(i).getChildrenItem(j).getMenucode());
                }
                //未勾选的加入unCheckedMenuClassNameList
                else {
                    unCheckedMenuClassNameList.add(groups.get(i).getChildrenItem(j).getMenucode());
                }
            }
        }
        //已选择选项集合
        checkMenuMap.put("checked", checkedMenuClassNameList);
        //未选择选项集合
        checkMenuMap.put("unchecked", unCheckedMenuClassNameList);

        return checkMenuMap;
    }

    /**
     * 根据getMenusMap()获取到的勾选菜单checkMenuMap改变menulist数据表的状态,
     * 使菜单显示或隐藏
     * checkMenuMap.get("checked"):设对应的classname的ifshow为1，表示显示该菜单
     * checkMenuMap.get("unchecked"):设对应的classname的ifshow为0，表示隐藏该菜单
     */
    public void changeMenulist(Map<String, List<String>> checkMenuMap) {
        //存放sql语句，后面调用BatchOperation批量操作
        ArrayList<String> sqlList = new ArrayList<String>();

        //设置newmenulist表中classname是勾选了的菜单对应的ifshow = '1'
        for (int i = 0; i < checkMenuMap.get("checked").size(); i++) {
            sqlList.add("update menus set showstatus = '1' where menucode='" + checkMenuMap.get("checked").get(i) + "'");
        }

        //设置newmenulist表中classname是未勾选的菜单对应的ifshow = '0'
        for (int i = 0; i < checkMenuMap.get("unchecked").size(); i++) {
            sqlList.add("update menus set showstatus = '0' where menucode='" + checkMenuMap.get("unchecked").get(i) + "'");
        }

        //批量操作，这里是批量修改newmenulist数据表
        SqliteDataHelper.getHelper(getApplicationContext()).BatchOperation(sqlList);
    }

    /**
     * 弹出确定对话框，让用户选择是否确定修改功能菜单
     */
    public void showConfirmDialog() {
        /**
         * 注意：这里的确定和取消按钮监听事件互换了，因为设备系统版本属性的问题，
         * android4.0以后官方的风格左NegativeButton，右PositiveButton，
         * 而2.x的版本PositiveButton是在左边的。
         * 开发时默认PositiveButton是在左边，处于习惯考虑把PositiveButton放在右边
         */
        new AlertDialog.Builder(CheckingFunctionActivity.this)
                .setTitle("\t\t\t注\t\t\t\t意！")
                .setIcon(R.drawable.attention)
                .setMessage("\t\t\t\t确定修改功能菜单？")
                .setPositiveButton("取\t\t消", null)
                .setNegativeButton("确\t\t定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //获取勾选菜单情况
                        Map<String, List<String>> map = getMenusMap();
                        //根据勾选情况改变newmenulist数据表
                        changeMenulist(map);
                        SomeUtils.showToask(CheckingFunctionActivity.this, "修改菜单成功！");
                        CheckingFunctionActivity.this.finish();
                    }
                })
                .show();
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

