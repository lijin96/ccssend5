package com.holyes.agent.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ccssend5.R;
import com.holyes.ccssend5.entity.Retailer;
import com.holyes.ccssend5.entity.StoreInfor;
import com.holyes.ccssend5.select.SelectDistribution;

import java.util.List;

/**
 * @ClassName: SelectAgentDistributorAdapter
 * @Description: 点击菜单界面列表的ExpandableListView的适配器
 * @Author: lijin
 * @Date: 2021/3/5 17:25
 */
public class SelectAgentDistributorAdapter extends BaseExpandableListAdapter implements ExpandableListView.OnChildClickListener {

        private Context context;
        private List<Retailer> menuGroups;

        /**
         * 勾选菜单界面的ExpandableListView的适配器的构造方法
         * @param context:上下文；menuGroups:数据集合
         */
        public SelectAgentDistributorAdapter(Context context, List<Retailer> menuGroups) {
            this.context = context;
            this.menuGroups = menuGroups;
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
                convertView = infalInflater.inflate(R.layout.select_distributor_lingshou_item, null);
                groupHolder=new GroupHolder();
                groupHolder.iv_arrow = (ImageView) convertView.findViewById(R.id.iv_arrow);
                groupHolder.tvTraderId=(TextView) convertView.findViewById(R.id.tv_TraderId);
                groupHolder.tvTraderName=(TextView) convertView.findViewById(R.id.tv_TraderName);
                convertView.setTag(groupHolder);
            }else{
                groupHolder=(GroupHolder) convertView.getTag();
            }

            Retailer retailer = (Retailer) getGroup(groupPosition);
            groupHolder.tvTraderId.setText("("+retailer.getTraderId()+")");
            groupHolder.tvTraderName.setText(retailer.getTraderName());

            if (isExpanded)
            {
                groupHolder.iv_arrow.setImageResource(R.drawable.s_down);
            }else{
                groupHolder.iv_arrow.setImageResource(R.drawable.s_right);
            }

            return convertView;
        }


        /** 設定 Children 資料 */
        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            ChildHolder childHolder;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.select_distributor_fenxiao_item, null);
                childHolder=new ChildHolder();
                childHolder.tvDistributorId=(TextView) convertView.findViewById(R.id.company_id);
                childHolder.tvDistributorName=(TextView) convertView.findViewById(R.id.companyName);
                childHolder.tvLink=(TextView) convertView.findViewById(R.id.companyLink);
                childHolder.tvTel=(TextView) convertView.findViewById(R.id.companyTel);
                childHolder.tvCorpAddr =(TextView) convertView.findViewById(R.id.corpaddr);

                convertView.setTag(childHolder);
            }else{
                childHolder=(ChildHolder) convertView.getTag();
            }
            StoreInfor storeInfor = menuGroups.get(groupPosition).getChildrenItem(childPosition);
            childHolder.tvDistributorId.setText(storeInfor.getStoreId());
            childHolder.tvDistributorName.setText(storeInfor.getStoreName());
            childHolder.tvLink.setText(storeInfor.getLink());
            childHolder.tvTel.setText(storeInfor.getTel());
            childHolder.tvCorpAddr.setText(storeInfor.getCorpAddr());

            convertView.setBackgroundResource(R.drawable.expandable_listview_item_bg);
            convertView.setOnClickListener(new ChildItemClick(groupPosition, childPosition));
            return convertView;
        }

        class ChildItemClick implements View.OnClickListener
        {
            int groupPosition,childPosition;
            public ChildItemClick(int groupPosition,int childPosition) {
                this.groupPosition = groupPosition;
                this.childPosition = childPosition;
            }

            @Override
            public void onClick(View v) {
                StoreInfor storeInfor = (StoreInfor) getChild(groupPosition, childPosition);
                ((SelectDistribution)context).goToOutStock(storeInfor);
            }

        }

        @Override
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

            return true;
        }

        class GroupHolder
        {
            ImageView iv_arrow;
            TextView tvTraderId;
            TextView tvTraderName;
        }
        class ChildHolder
        {
            TextView  tvDistributorId;
            TextView  tvDistributorName;
            TextView  tvLink;
            TextView  tvTel;
            TextView  tvCorpAddr;
            TextView  tvTraderId;

        }

    }