package com.holyes.factoryscan.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.example.ccssend5.R;
import com.google.gson.Gson;
import com.holyes.ccssend5.entity.BoxPackDate;
import com.holyes.ccssend5.lib.AccessWeb;
import com.holyes.ccssend5.lib.ShowMessage;
import com.holyes.ccssend5.lib.SysUserInfo;
import com.holyes.ccssend5.myview.MyProgressDialog;
import com.holyes.ccssend5.utils.DisplayUtil;
import com.holyes.ccssend5.utils.SomeUtils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @ClassName: SelectAllFactoryBoxDetail
 * @Description: 所有用户装盒汇总
 * @Author: lijin
 * @Date: 2025年8月6日17:39:26
 */
public class SelectAllFactoryBoxDetail extends Activity {

    private Handler hand;
    private AccessWeb accWeb;

    private Context mContext;
    private SimpleAdapter adapter;
    private SysUserInfo sysUserInfo;
    private LayoutInflater inflater;

    private View alertView;//AlertDialog的布局view
    private ListView listview;
    private EditText et_search;
//        private TextView tv_total;

    private TextView tv_title;
    private TextView dp_start_date,dp_end_date;//开始时间，结束时间

    private Button btn_seach;

    private List<Map<String, Object>> dList = new ArrayList<Map<String, Object>>();
    private Map<String, Object> item;

    private final int Lic_SelectSure = 3;

    private String lsv_aim = "";
    private String company_id="";//代理商id

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        DisplayUtil.setDefaultDisplay(this);
        setContentView(R.layout.activity_select_allfactoryboxdetail);
        mContext = this;
        accWeb = new AccessWeb(getApplicationContext());
        hand = new handShowMsg();
        sysUserInfo = new SysUserInfo(getApplicationContext());
//        ((Button) findViewById(R.id.btn_exit))
//                .setOnClickListener(new BtnExitClickListener());
        listview = (ListView) findViewById(R.id.list_factoryscan_detail);
//            tv_total = (TextView) findViewById(R.id.tv_total);
        et_search = (EditText) findViewById(R.id.et_search);

        lsv_aim = getIntent().getStringExtra("lsv_aim");
        company_id=getIntent().getStringExtra("agentId");

//        tv_title=findViewById(R.id.tv_title);
//        tv_title.setText(lsv_aim);

//        et_search.addTextChangedListener(new EtTextWatcher());
        dp_start_date=findViewById(R.id.dp_start_date);
        dp_end_date=findViewById(R.id.dp_end_date);



        Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);      // 注意：Calendar的月份是0-11
        int day = calendar.get(Calendar.DAY_OF_MONTH);


        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String today = sdf.format(new Date());

        dp_start_date.setText(getLastMonthToday());
        dp_end_date.setText(today);

        dp_start_date.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View view) {
                DatePickerDialog.OnDateSetListener onDateSetListener =new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        month = month+1;
                        String date_str = year+"-"+month+"-"+day;//把日期变成字符串格式显示出来
                        dp_start_date.setText(date_str);//文本框显示的内容设置成经过逻辑处理后的日期
                    }
                };
                new DatePickerDialog(mContext,5,onDateSetListener,year,month-1,day).show();
            }
        });

        dp_end_date.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View view) {
                DatePickerDialog.OnDateSetListener onDateSetListener =new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        month = month+1;
                        String date_end_str = year+"-"+month+"-"+day;//把日期变成字符串格式显示出来
                        dp_end_date.setText(date_end_str);//文本框显示的内容设置成经过逻辑处理后的日期
                    }
                };
                new DatePickerDialog(mContext,5,onDateSetListener,year,month,day).show();
            }
        });

        et_search.setOnKeyListener(new EtBarodeOnkeyListener());

        btn_seach=findViewById(R.id.btn_seach);
        btn_seach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DownLoadDataThread();

            }
        });

//            listview.setOnItemClickListener(new ListViewItemClickListener());
//        dList=new ArrayList<>();

        DownLoadDataThread();

    }

    public String getLastMonthToday() {
        // 获取当前日期
        Calendar calendar = Calendar.getInstance();
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        // 减去一个月（注意月份从0开始）
        calendar.add(Calendar.MONTH, -1);

        // 处理跨月边界：若当前月天数 > 上个月总天数，则取上个月最后一天
        int lastMonthMaxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        if (currentDay > lastMonthMaxDay) {
            calendar.set(Calendar.DAY_OF_MONTH, lastMonthMaxDay);
        } else {
            calendar.set(Calendar.DAY_OF_MONTH, currentDay);
        }
        // 格式化日期
        return new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
    }




    public void initListView(List<Map<String, Object>> dataList) {
        adapter = new SimpleAdapter(this, dataList, R.layout.list_allfactoryboxdetail,
                new String[]{"BrandName", "Modelm", "BoxNum", "GoodsNum",
                }, new int[]{
                R.id.list_goods_date, R.id.list_goods_name, R.id.list_goods_boxnum,
                R.id.list_goods_num});
        listview.setAdapter(adapter);
//            tv_total.setText("（共 " + dataList.size() + " 条）");
    }

    private class handShowMsg extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ShowMessage.HandShowMessage: // 显示错误提示显示
                    MyProgressDialog.close();
                    ShowMessage.Show(SelectAllFactoryBoxDetail.this, msg.obj.toString());
                    break;
                case ShowMessage.HandSuccess: // 返回成功
                    MyProgressDialog.close();
                    if (dList.size()>0){
                        initListView(dList);
                    }else{
                        ShowMessage.Show(SelectAllFactoryBoxDetail.this, "未查询到数据");
                    }
                    break;

                default:

                    break;
            }
            super.handleMessage(msg);
        }
    }

    //下载明细
    private void DownLoadDataThread() {
        MyProgressDialog.show(mContext, "正在获取数据...", true, false);
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BoxPackDate boxPackDate=new BoxPackDate();
                    boxPackDate.setStartDate(dp_start_date.getText().toString().trim());
                    boxPackDate.setEndDate(dp_end_date.getText().toString().trim());
                    Gson gson=new Gson();
                    dList=new ArrayList<>();
                    dList = accWeb.GetAllPackingGather(gson.toJson(boxPackDate));
                    ShowMessage.ShowMsg(hand, ShowMessage.HandSuccess, "success");
                } catch (Exception e) {
                    ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage,"下载出错" + e.getMessage());
                }
            }
        });
        sendCode.start();
    }

    private class EtBarodeOnkeyListener implements View.OnKeyListener {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {

            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {

                    String tBarcode =et_search.getText().toString().trim();

                    if (tBarcode.equals("")) {
                        ShowMessage.Show(mContext, "请输入要查询的数据");
                        return true;
                    }

                    DownLoadDataThread();

                }
                return true;
            } else {
                return false;
            }
        }
    }
    private class BtnExitClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            finish();
        }
    }

    // item选择事件
    private class ListViewItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            ListView listView = (ListView) parent;

            item = (Map<String, Object>) listView.getItemAtPosition(position);

            Intent intent = new Intent();
            intent.putExtra("company_na", item.get("CustName").toString());
            intent.putExtra("company_id", item.get("CustId").toString());
            setResult(RESULT_OK, intent);
            finish();

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Lic_SelectSure:
//                    Intent intent = new Intent(this, P_Dv_OutStock_D_L_NoBill.class);
//                    if (lsv_aim.equals("P_Dv_OutStock_D_L_Lens_NoBill")){
//                        intent = new Intent(this, P_Dv_OutStock_D_L_Lens_NoBill.class);
//                    }else if (lsv_aim.equals("P_Dv_ReturnedPurchase_D_L_TransferGoods")){
//                        intent = new Intent(this, P_Dv_ReturnedPurchase_D_L_TransferGoods.class);
//                    }
//
//                    intent.putExtra("company_na", item.get("CustName").toString());
//                    intent.putExtra("company_id", item.get("CustId").toString());
//                    startActivity(intent);
                    break;
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }



    @Override
    protected void onResume() {
        super.onResume();
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

