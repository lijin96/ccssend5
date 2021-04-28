package com.holyes.headquarter.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.ccssend5.R;
import com.holyes.ccssend5.entity.MenuGroup;

import java.util.ArrayList;

/**
 * @ClassName: OtherMenuListAdapter
 * @Description: java类作用描述
 * @Author: lijin
 * @Date: 2021/3/10 10:18
 */
public class OtherMenuListAdapter extends BaseAdapter {

        private Context context;
        private ArrayList<MenuGroup> groups;
        private LayoutInflater layoutInflater;
        private MenuGroup menuGroup;



        public OtherMenuListAdapter(Context context,ArrayList<MenuGroup> groups)
        {
            this.context = context;
            this.groups = groups;
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return groups.size();
        }

        @Override
        public Object getItem(int position) {
            return groups.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            menuGroup = groups.get(position);
            if(convertView==null)
            {
                holder = new ViewHolder();
                convertView = layoutInflater.inflate(R.layout.menu_list_item_layout, null);
                holder.tv_menuname = (TextView) convertView.findViewById(R.id.tv_menuname);

                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tv_menuname.setText(menuGroup.getTitle());

            return convertView;
        }

        class ViewHolder
        {
            TextView tv_menuname;
        }

    }
