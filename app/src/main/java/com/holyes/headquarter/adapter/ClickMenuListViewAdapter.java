package com.holyes.headquarter.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ccssend5.R;
import com.holyes.ccssend5.entity.MenuChild;
import com.holyes.ccssend5.entity.MenuGroup;
import com.holyes.ccssend5.lib.SysUserInfo;
import com.holyes.headquarter.activity.MenuListActivity;

import java.util.ArrayList;

/**
 * @ClassName: ClickMenuListViewAdapter
 * @Description: 点击菜单界面列表的ExpandableListView的适配器
 * @Author: lijin
 * @Date: 2021/3/10 10:18
 */
public class ClickMenuListViewAdapter extends BaseExpandableListAdapter implements ExpandableListView.OnChildClickListener{

        private Context context;
        private ArrayList<MenuGroup> menuGroups;
        private SysUserInfo sysUserInfo ;

        /**
         * 勾选菜单界面的ExpandableListView的适配器的构造方法
         * @param context:上下文；menuGroups:数据集合
         */
        public ClickMenuListViewAdapter(Context context, ArrayList<MenuGroup> menuGroups,SysUserInfo mSysUserInfo) {
            this.context = context;
            this.menuGroups = menuGroups;
            this.sysUserInfo=mSysUserInfo;
        }

        public Object getChild(int groupPosition, int childPosition) {
            return menuGroups.get(groupPosition).getChildrenItem(childPosition);
        }

        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        public int getChildrenCount(int groupPosition) {
            return menuGroups.get(groupPosition).getChildrenCount();
        }

        public Object getGroup(int groupPosition) {
            return menuGroups.get(groupPosition);
        }

        public int getGroupCount() {
            return menuGroups.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        public boolean hasStableIds() {
            return true;
        }

        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        /** 設定 MenuGroup 資料 */
        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            GroupHolder groupHolder;
            if (convertView == null) {
                LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.new_menu_group_layout, null);
                groupHolder=new GroupHolder();
                groupHolder.groupImage=(ImageView) convertView.findViewById(R.id.iv_group);
                groupHolder.groupTitle=(TextView) convertView.findViewById(R.id.tv_group_title);
                groupHolder.groupCheckBox=(CheckBox) convertView.findViewById(R.id.check_group);
                groupHolder.groupTvAll=(TextView) convertView.findViewById(R.id.tv_check_all);
                convertView.setTag(groupHolder);
            }else{
                groupHolder=(GroupHolder) convertView.getTag();
            }

            MenuGroup group = (MenuGroup) getGroup(groupPosition);
            groupHolder.groupTitle.setText(group.getTitle());
            groupHolder.groupCheckBox.setChecked(group.getChecked());

            if (isExpanded)
            {
                groupHolder.groupImage.setImageResource(R.drawable.group_buttom_g);
            }else{
                groupHolder.groupImage.setImageResource(R.drawable.group_right_g);
            }

            // 點擊 CheckBox 或这全选字体時，將狀態存起來
            //      groupHolder.groupCheckBox.setOnClickListener(new MenuGroupCheckBoxClick(groupPosition));
            //      groupHolder.groupTvAll.setOnClickListener(new MenuGroupCheckBoxClick(groupPosition));
            return convertView;
        }

        /** 勾選 MenuGroup CheckBox 時，存 MenuGroup CheckBox 的狀態，以及改變 Child CheckBox 的狀態 */
        class MenuGroupCheckBoxClick implements View.OnClickListener {
            private int groupPosition;

            MenuGroupCheckBoxClick(int groupPosition) {
                this.groupPosition = groupPosition;
            }

            public void onClick(View v) {
                menuGroups.get(groupPosition).toggle();
                // 將 Children 的 isChecked 全面設成跟 MenuGroup 一樣
                int childrenCount = menuGroups.get(groupPosition).getChildrenCount();
                boolean menuGroupIsChecked = menuGroups.get(groupPosition).getChecked();
                for (int i = 0; i < childrenCount; i++)
                {
                    menuGroups.get(groupPosition).getChildrenItem(i).setChecked(menuGroupIsChecked);
                }
                // 注意，一定要通知 ExpandableListView 資料已經改變，ExpandableListView 會重新產生畫面
                //          SelectFunctionActivity.lastMenuGroups = menuGroups;
                notifyDataSetChanged();
            }
        }

        /** 設定 Children 資料 */
        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            ChildHolder childHolder;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.new_menu_child_layout, null);
                childHolder=new ChildHolder();
                childHolder.childImage=(ImageView) convertView.findViewById(R.id.iv_child);
                childHolder.childTitle=(TextView) convertView.findViewById(R.id.tv_child_title);
                childHolder.childCheckBox=(CheckBox) convertView.findViewById(R.id.check_child);
                convertView.setTag(childHolder);
            }else{
                childHolder=(ChildHolder) convertView.getTag();
            }
            MenuChild child = menuGroups.get(groupPosition).getChildrenItem(childPosition);
            childHolder.childTitle.setText(child.getMenuname());
            childHolder.childCheckBox.setChecked(child.getChecked());

            //      if(child.getChecked())
            //      {
            //    	  childHolder.childImage.setImageResource(R.drawable.point_green);
            //      }else{
            //    	  childHolder.childImage.setImageResource(R.drawable.point_red);
            //      }
            childHolder.childImage.setImageResource(R.drawable.point_blue);
            // 點擊 CheckBox 時，將狀態存起來
            //      childHolder.childCheckBox.setOnClickListener(new ChildCheckBoxClick(groupPosition, childPosition));
            //      convertView.setOnClickListener(new ChildCheckBoxClick(groupPosition, childPosition));
            convertView.setBackgroundResource(R.drawable.expandable_listview_item_bg);
            convertView.setOnClickListener(new ChildItemClick(menuGroups.get(groupPosition).getChildrenItem(childPosition).getMenucode()));
            sysUserInfo= new SysUserInfo(context);
            sysUserInfo.setClassname(menuGroups.get(groupPosition).getChildrenItem(childPosition).getMenucode());
            return convertView;
        }

        class ChildItemClick implements View.OnClickListener
        {
            String menucode;
            public ChildItemClick(String menucode) {
                this.menucode = menucode;
            }

            @Override
            public void onClick(View v) {
                ((MenuListActivity)context).goToNextScanPage(menucode);
            }

        }

        /** 勾選 Child CheckBox 時，存 Child CheckBox 的狀態 */
        class ChildCheckBoxClick implements View.OnClickListener {
            private int groupPosition;
            private int childPosition;

            ChildCheckBoxClick(int groupPosition, int childPosition) {
                this.groupPosition = groupPosition;
                this.childPosition = childPosition;
            }

            public void onClick(View v) {
                handleClick(childPosition, groupPosition);
            }
        }

        public void handleClick(int childPosition, int groupPosition) {
            menuGroups.get(groupPosition).getChildrenItem(childPosition).toggle();

            // 檢查 Child CheckBox 是否有全部勾選，以控制 MenuGroup CheckBox
            int childrenCount = menuGroups.get(groupPosition).getChildrenCount();
            boolean childrenAllIsChecked = true;
            for (int i = 0; i < childrenCount; i++) {
                if (!menuGroups.get(groupPosition).getChildrenItem(i).getChecked()) {
                    childrenAllIsChecked = false;
                    break;
                }
            }

            menuGroups.get(groupPosition).setChecked(childrenAllIsChecked);

            //     SelectFunctionActivity.lastMenuGroups = menuGroups;
            // 注意，一定要通知 ExpandableListView 資料已經改變，ExpandableListView 會重新產生畫面
            notifyDataSetChanged();
        }

        @Override
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
            handleClick(childPosition, groupPosition);
            return true;
        }

        class GroupHolder
        {
            ImageView groupImage;
            TextView groupTitle;
            CheckBox groupCheckBox;
            TextView groupTvAll;
        }
        class ChildHolder
        {
            ImageView childImage;
            TextView  childTitle;
            CheckBox  childCheckBox;
        }

    }