package com.holyes.headquarter.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.ccssend5.R;
import com.holyes.ccssend5.entity.PackingScan;

import java.util.List;

/**
 * @ClassName: EliminateAdapter
 * @Description: java类作用描述
 * @Author: lijin
 * @Date: 2021/3/10 10:18
 */
public class EliminateAdapter extends BaseAdapter {

        private List<PackingScan> sList;
        private Context context;
        private LayoutInflater inflater;
        private PackingScan pScan;

        public EliminateAdapter(Context context,List<PackingScan> sList) {
            super();
            this.context = context;
            this.sList = sList;
            inflater = LayoutInflater.from(context);
        }


        @Override
        public int getCount() {

            return sList.size();
        }


        @Override
        public Object getItem(int position) {

            return sList.get(position);
        }


        @Override
        public long getItemId(int position) {

            return position;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if(convertView==null)
            {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.eliminate_listview_item, null);
                holder.tv_model_colors = (TextView) convertView.findViewById(R.id.tv_model_colors);
                holder.tv_barcode = (TextView) convertView.findViewById(R.id.tv_barcode);
                convertView.setTag(holder);

            }else{
                holder = (ViewHolder) convertView.getTag();
            }
            pScan = sList.get(position);
            holder.tv_model_colors.setText(pScan.getModelm()+"-"+pScan.getColors());
            holder.tv_barcode.setText(pScan.getBarcode());

            return convertView;
        }

        class ViewHolder
        {
            TextView tv_model_colors;
            TextView tv_barcode;
        }

    }
