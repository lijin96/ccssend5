package com.holyes.ccssend5.select;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;


import com.example.ccssend5.R;
import com.holyes.ccssend5.lib.AccessWeb;
import com.holyes.ccssend5.lib.ShowMessage;
import com.holyes.ccssend5.lib.SysUserInfo;
import com.holyes.ccssend5.myview.MyProgressDialog;
import com.holyes.headquarter.adapter.MyGridViewAdapter;
import com.holyes.headquarter.instock_in.P_Dv_InStock_Lens_Bill;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: CheckLensDegree
 * @Description: 查看镜片度数表格
 * @Author: lijin
 * @Date: 2024/3/8 10:01
 */
public class CheckLensDegree extends Activity {

    private Context mContext;
    private AccessWeb accWeb;
    private Handler hand;
    private SysUserInfo sysUserInfo;

    private CylDataAdapter sphDataAdapter;
    private MyGridViewAdapter  cylDataAdapter;

    private ListView listView;
    private GridView gridView;
    private RadioGroup lens_radio_group;//切换球柱镜方向
    private RadioButton radio_mirror;//球镜在前
    private RadioButton radio_cylinder;//柱镜在前
    private TextView tv_listview_title,tv_gridview_title;

    private Boolean IsMirrorBefore;//true 球镜在前 false 柱镜在前

    private String Cyldegrees="";//选择的球镜度数

    private List<Map<String, Object>> CylLensList= new ArrayList<Map<String, Object>>();;//柱镜集合
    private List<Map<String, Object>> SphLensList= new ArrayList<Map<String, Object>>();;//球镜集合

    private String lsv_aim = "";
    private String  supplier_id = "", supplier_name = "",Product_id="",refractive="";
    private String goodsid = "", stock_id = "", stock_name, saplno="", sourceBillNo;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lensdegree);
        mContext=this;
        lsv_aim = getIntent().getStringExtra("aim");
        accWeb = new AccessWeb(getApplicationContext());
        hand = new handShowMsg();
        sysUserInfo=new SysUserInfo(mContext);

        IsMirrorBefore=sysUserInfo.getIsMirrorBefore();//默认是柱镜在前

//        supplier_id = getIntent().getStringExtra("supplier_id");
//        supplier_name = getIntent().getStringExtra("supplier_name");
//        stock_id = getIntent().getStringExtra("stock_id");
//        stock_name = getIntent().getStringExtra("stock_name");
        sourceBillNo = getIntent().getStringExtra("purchecklno");
//        saplno = getIntent().getStringExtra("saplno");
        Product_id = getIntent().getStringExtra("Product_id");
        refractive = getIntent().getStringExtra("refractive");

        setView();


    }
    private void setView(){

        //切换球柱镜展示方向
        radio_mirror=findViewById(R.id.radio_mirror);
        radio_cylinder=findViewById(R.id.radio_cylinder);

        tv_listview_title=findViewById(R.id.tv_listview_title);
        tv_gridview_title=findViewById(R.id.tv_gridview_title);

        if (IsMirrorBefore){
            radio_mirror.setChecked(true);
            tv_listview_title.setText("球镜");
            tv_gridview_title.setText("柱镜");
        }else{
            radio_cylinder.setChecked(true);
            tv_listview_title.setText("柱镜");
            tv_gridview_title.setText("球镜");
        }

        lens_radio_group=findViewById(R.id.lens_radio_group);
        lens_radio_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton rb_temp  = findViewById(radioGroup.getCheckedRadioButtonId());

                if (rb_temp.getText().toString().equals("球镜在前")){
                    sysUserInfo.setIsMirrorBefore(true);
                    IsMirrorBefore=true;
                    tv_listview_title.setText("球镜");
                    tv_gridview_title.setText("柱镜");
                    GetScsPurOrderDiopter();
                }else if (rb_temp.getText().toString().equals("柱镜在前")){
                    sysUserInfo.setIsMirrorBefore(false);
                    IsMirrorBefore=false;
                    tv_listview_title.setText("柱镜");
                    tv_gridview_title.setText("球镜");
                    GetScsPurOrderDiopter();
                }
            }
        });


        listView = (ListView) findViewById(R.id.Listview_Cyl);

        //ListView项的点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                //以前是先柱镜后球镜，后面海伦改成先球镜后柱镜，现在在调整接口顺序
                sphDataAdapter.setDefSelect(position);
                if (IsMirrorBefore) {
                    //球镜在前 选择的就是球镜
                    Cyldegrees = (String) SphLensList.get(position).get("Diopter");
                    GetScsPurOrderAstigmatism((String) SphLensList.get(position).get("Diopter"));
                }else{
                    //柱镜在前 选择的就是柱镜
                    Cyldegrees = (String) CylLensList.get(position).get("Astigmatism");
                    GetScsPurOrderAstigmatism((String) CylLensList.get(position).get("Astigmatism"));
                }
            }
        });

        gridView=findViewById(R.id.Gridview_Sph);
//        gridView.setAdapter(new MyGridViewAdapter(mContext,SphLensList));
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Map<String, Object> sphdata;

//                Log.d("main", "onItemClick: "+sphdata);
//                sphDataAdapter.setSeclection(i);
//                sphDataAdapter.notifyDataSetChanged();
                Intent intent  = new Intent(mContext, P_Dv_InStock_Lens_Bill.class);

//                intent.putExtra("purchecklno", sourceBillNo);
//                intent.putExtra("saplno", saplno);
//                intent.putExtra("supplier_name", supplier_name);
//                intent.putExtra("stock_name", stock_name);
//                intent.putExtra("supplier_id", supplier_id);
//                intent.putExtra("stock_id", stock_id);
//                intent.putExtra("scanBillno",  getIntent().getStringExtra(("scanBillno")));
//                intent.putExtra("GoodsCode",  getIntent().getStringExtra(("GoodsCode")));
                intent.putExtra("Product_id", getIntent().getStringExtra("Product_id"));
                if (IsMirrorBefore){
                   //如果球镜在前，选择的就是柱镜
                    sphdata= CylLensList.get(i);
                    intent.putExtra("Astigmatism",sphdata.get("Astigmatism").toString() );//柱镜
                    intent.putExtra("Diopter",Cyldegrees);//球镜
                }else{
                    //如果柱镜在前，选择的就是球镜
                    sphdata= SphLensList.get(i);
                    intent.putExtra("Astigmatism", Cyldegrees );//柱镜
                    intent.putExtra("Diopter",sphdata.get("Diopter").toString());//球镜
                }

                intent.putExtra("PurQty", sphdata.get("PurQty").toString());//采购订单数
                intent.putExtra("ScanQty", sphdata.get("ScanQty").toString());//已扫描数量
                intent.putExtra("aim", lsv_aim);
//                startActivity(intent);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    //获取柱镜
    private void GetScsPurOrderAstigmatism(final String diopter) {
        MyProgressDialog.show(this, "正在获取数据...", true, false);
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (IsMirrorBefore){
                        //球镜在前 就获取柱镜
                        CylLensList = accWeb.GetPurCheckAstigmatism(sourceBillNo,Product_id,diopter);
                    }else{
                        //柱镜在前 就获取球镜
                        SphLensList = accWeb.GetPurCheckDiopter(sourceBillNo,Product_id,diopter);
                    }
                    ShowMessage.ShowMsg(hand, ShowMessage.HandSuccess, "success");
                } catch (Exception e) {
                    ShowMessage.ShowMsg(hand, "下载出错" + e.getMessage());
                }
            }
        });
        sendCode.start();
    }


    //进来获取球镜
    private void GetScsPurOrderDiopter() {
        MyProgressDialog.show(this, "正在获取数据...", true, false);
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (IsMirrorBefore){
                        //球镜在前 读取球镜
                        SphLensList = accWeb.GetPurCheckDiopter(sourceBillNo,Product_id,"");
                    }else{
                        //柱镜在前 读取柱镜
                        CylLensList = accWeb.GetPurCheckAstigmatism(sourceBillNo,Product_id,"");
                    }

                    ShowMessage.ShowMsg(hand, ShowMessage.HandScanSuccess, "success");
                } catch (Exception e) {
                    ShowMessage.ShowMsg(hand, "下载出错" + e.getMessage());
                }
            }
        });
        sendCode.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        Log.d("main",Cyldegrees);
//        if (!Cyldegrees.equals("")) {
//            GetScsPurOrderDiopter(Cyldegrees);
//        }
//        if (sysUserInfo.getIsMirrorBefore()) {
            //球镜在前
            GetScsPurOrderDiopter();
//        }else{
//            //柱镜在前
//
//        }
    }

    private class handShowMsg extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ShowMessage.HandShowMessage: // 显示错误提示显示
                    ShowMessage.Show(mContext, msg.obj.toString());
                    break;

                case ShowMessage.HandSuccess:


                    //获取柱镜
//                    if (CylLensList.size()>0) {
//                        cylDataAdapter = new CylDataAdapter(mContext, CylLensList);
//                        listView.setAdapter(cylDataAdapter);
//                        cylDataAdapter.setDefSelect(0);//设置默认选中第一项
//                        Cyldegrees=(String) CylLensList.get(0).get("Astigmatism");
//                        GetScsPurOrderDiopter((String) CylLensList.get(0).get("Astigmatism"));
//                    }else{
//                        ShowMessage.Show(mContext, "暂无柱镜数据");
//                    }
                    if (IsMirrorBefore) {
                        //先获取球镜再获取柱镜
                        if (CylLensList.size() > 0) {
                            cylDataAdapter = new MyGridViewAdapter(mContext, CylLensList);
                            gridView.setAdapter(cylDataAdapter);
                        } else {
                            ShowMessage.Show(mContext, "暂无柱镜数据");
                        }
//                        Log.d("main","柱镜="+CylLensList.toString());
                    }else {
                        //先获取柱镜再获取球镜
                        if (SphLensList.size() > 0) {
                            cylDataAdapter = new MyGridViewAdapter(mContext, SphLensList);
                            gridView.setAdapter(cylDataAdapter);
                        } else {
                            ShowMessage.Show(mContext, "暂无球镜数据");
                        }
//                        Log.d("main","球镜="+SphLensList.toString());
                    }

                    break;
                case ShowMessage.HandScanSuccess:
                    if (IsMirrorBefore){
                        //获取球镜
                        if (SphLensList.size()>0) {
                            sphDataAdapter = new CylDataAdapter(mContext, SphLensList);
                            listView.setAdapter(sphDataAdapter);
                            sphDataAdapter.setDefSelect(0);//设置默认选中第一项
                            Cyldegrees=(String) SphLensList.get(0).get("Diopter");
                            GetScsPurOrderAstigmatism((String) SphLensList.get(0).get("Diopter"));
                        }else{
                            ShowMessage.Show(mContext, "暂无球镜数据");
                        }
                    }else{
                        //获取柱镜
                        if (CylLensList.size()>0) {
                            sphDataAdapter = new CylDataAdapter(mContext, CylLensList);
                            listView.setAdapter(sphDataAdapter);
                            sphDataAdapter.setDefSelect(0);//设置默认选中第一项
                            Cyldegrees=(String) CylLensList.get(0).get("Astigmatism");
//                            (String) SphLensList.get(0).get("Astigmatism")
                            GetScsPurOrderAstigmatism((String) CylLensList.get(0).get("Astigmatism"));
                        }else{
                            ShowMessage.Show(mContext, "暂无柱镜数据");
                        }
                    }
                    break;


                default:
                    break;
            }
            MyProgressDialog.close();
        }
    }



    class CylDataAdapter extends BaseAdapter {

        private Context context;
        private List<Map<String, Object>> cylList;
        private ViewHolder holder;
        private int defItem;//声明默认选中的项
        private SysUserInfo sysUserInfo;


        public CylDataAdapter(Context context, List<Map<String, Object>> persons) {
            super();
            this.context = context;
            this.cylList = persons;
            sysUserInfo=new SysUserInfo(context);
        }




        @Override
        public int getCount() {
            return cylList.size();
        }

        @Override
        public Object getItem(int position) {
            return cylList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
        /**
         适配器中添加这个方法
         */
        public void setDefSelect(int position) {
            this.defItem = position;
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.lens_listview_item, null);
                holder = new ViewHolder();
                holder.item1 = (TextView) convertView.findViewById(R.id.tv_lens_items);
                holder.item2 = (TextView) convertView.findViewById(R.id.tv_lens_num);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            if (defItem == position) {
                convertView.setBackgroundResource(R.color.orange);
            } else {
                convertView.setBackgroundResource(android.R.color.transparent);
            }
            //绑定数据

            if (sysUserInfo.getIsMirrorBefore()){
                //显示球镜
                holder.item1.setText((String)cylList.get(position).get("Diopter"));
            }else{
                //显示柱镜
                holder.item1.setText((String)cylList.get(position).get("Astigmatism"));
            }


            holder.item2.setText((String)cylList.get(position).get("ScanQty")+"/"+(String)cylList.get(position).get("PurQty"));

            return convertView;
        }

        class ViewHolder {
            TextView item1;
            TextView item2;
        }

    }
}
