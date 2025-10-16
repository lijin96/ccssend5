package com.holyes.headquarter.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.example.ccssend5.R;
import com.holyes.ccssend5.lib.SysUserInfo;

import java.util.List;
import java.util.Map;

/**
 * @ClassName: MyGridViewAdapter
 * @Description: java类作用描述
 * @Author: lijin
 * @Date: 2024/3/8 11:47
 */
public  class MyGridViewAdapter extends BaseAdapter {
    //声明引用
    private Context mContext;   //这个Context类型的变量用于第三方图片加载时用到
    private LayoutInflater mlayoutInflater;
    private List<Map<String, Object>> sphgridData;
    private SysUserInfo sysUserInfo;
    //创建一个构造函数
    public MyGridViewAdapter(Context context, List<Map<String, Object>> msphgridData ){
        this.mContext=context;
        //利用LayoutInflate把控件所在的布局文件加载到当前类中
        this.sphgridData=msphgridData;
        mlayoutInflater=LayoutInflater.from(context);
        sysUserInfo=new SysUserInfo(context);
    }

//    private int clickTemp = -1;
//    //标识选择的Item
//    public void setSeclection(int position) {
//        clickTemp = position;
//    }

    @Override
    public int getCount() {
        return sphgridData.size(); //GridView的数目总共10个
    }

    @Override
    public Object getItem(int position) {
        return sphgridData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    //写一个静态的class,把layout_grid_item的控件转移过来使用
    static class ViewHolder{
        public TextView Grid_titleview;
        public TextView Grid_textview;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(convertView == null){
            //填写ListView的图标和标题等控件的来源，来自于layout_list_item这个布局文件
            //把控件所在的布局文件加载到当前类中
            convertView = mlayoutInflater.inflate(R.layout.gridview_lensdegree,null);
            //生成一个ViewHolder的对象
            holder = new ViewHolder();
            //获取控件对象
            holder.Grid_titleview=convertView.findViewById(R.id.grid_title);
            holder.Grid_textview=convertView.findViewById(R.id.grid_num);
            convertView.setTag(holder);

        }else{
            holder = (ViewHolder) convertView.getTag();
        }

//        if (clickTemp == position) {
//            convertView.setBackgroundResource(R.drawable.gridview_selector_view);
//        } else {
//            convertView.setBackgroundResource(R.drawable.gridview_selector_view);
//        }
        //修改空间属性
        if (sysUserInfo.getIsMirrorBefore()){
            //柱镜
            holder.Grid_titleview.setText((String)sphgridData.get(position).get("Astigmatism"));
        }else{

            //球镜
            holder.Grid_titleview.setText((String)sphgridData.get(position).get("Diopter"));

        }

        holder.Grid_textview.setText((String)sphgridData.get(position).get("ScanQty")+"/"+(String)sphgridData.get(position).get("PurQty"));
//        //加载第三方网络图片
//        Glide.with(mContext).load("http://pic.yesky.com/uploadImages/2013/203/37F142RUD672.jpg").into(holder.Grid_imageview);
        return convertView;
    }

}
