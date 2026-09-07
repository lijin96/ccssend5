package com.holyes.ccssend5.select;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.ccssend5.R;
import com.holyes.ccssend5.lib.AccessWeb;
import com.holyes.ccssend5.lib.MySound;
import com.holyes.ccssend5.lib.ShowMessage;
import com.holyes.ccssend5.lib.SqliteDataHelper;
import com.holyes.ccssend5.lib.SysUserInfo;
import com.holyes.ccssend5.myview.MyProgressDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: SelectSetmealDetails
 * @Description: 套餐明细列表（套标发货：从本地 packdressboxscan 展示；装盒出货为仅查看，不可点选）
 * @Author: lijin
 * @Date: 2026/5/11 17:34
 */
public class SelectSetmealDetails extends Activity {

    private Context mContext;
    private AccessWeb accWeb;
    private Thread downloadThread;
    private Handler hand;
    private MySound sound;
    private SysUserInfo sysUserInfo;

    private String PackId = "";

    /** 从扫描页进入：不清空本地扫描表，只展示本地数据 */
    private boolean useLocalDetail;

    /** 仅查看列表，不可点击选择型号色号 */
    private boolean viewOnly;

    private List<Map<String, Object>> list;

    private ListView PackDressBox_list;
    private TextView tv_total;
    private TextView txt_tile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_select_setmeal_details);
        initView();
    }

    private void initView() {
        accWeb = new AccessWeb(getApplicationContext());
        hand = new handShowMsg();
        mContext = this;
        sound = MySound.getMySound(this);
        sysUserInfo = new SysUserInfo(mContext);

        PackDressBox_list = findViewById(R.id.PackDressBox_list);
        tv_total = findViewById(R.id.tv_total);
        txt_tile = findViewById(R.id.txt_tile);

        PackId = getIntent().getStringExtra("SetmealId");
        useLocalDetail = getIntent().getBooleanExtra("use_local_detail", false);
        viewOnly = getIntent().getBooleanExtra("view_only", false);
        if (viewOnly && txt_tile != null) {
            txt_tile.setText("套餐明细（仅查看）");
        }

        if (useLocalDetail) {
            list = SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList("select * from packdressboxscan", null);
            if (list != null && !list.isEmpty()) {
                initListView(list);
                // 装盒出货：仅查看，不绑定点选
                // bindPickListener();
                return;
            }
        }

        if (!useLocalDetail) {
            try {
                SqliteDataHelper.getHelper(getApplicationContext()).execSQL("delete from packdressboxscan");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        MyProgressDialog.show(this, "正在获取数据...", false, false);
        downloadThread = new Thread(new DownLoadDataThread());
        downloadThread.start();
    }

    /**
     * 点击列表项选择型号色号并返回（装盒出货已改为仅查看，默认不调用）
     */
    private void bindPickListener() {
        if (!useLocalDetail || viewOnly) {
            return;
        }
        // PackDressBox_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        //     @Override
        //     public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //         Map<String, Object> row = (Map<String, Object>) parent.getItemAtPosition(position);
        //         Intent ret = new Intent();
        //         ret.putExtra("goodsid", str(row.get("goodsid")));
        //         ret.putExtra("modelm", str(row.get("modelm")));
        //         ret.putExtra("colors", str(row.get("colors")));
        //         ret.putExtra("packnum", str(row.get("packnum")));
        //         setResult(RESULT_OK, ret);
        //         finish();
        //     }
        // });
    }

    private static String str(Object o) {
        return o == null ? "" : o.toString();
    }

    private class DownLoadDataThread implements Runnable {
        @Override
        public void run() {
            try {
                SqliteDataHelper.getHelper(getApplicationContext()).execSQL("delete from packdressboxscan");
                list = accWeb.GetPackMealDetail(PackId);
                if (list == null) {
                    list = new ArrayList<Map<String, Object>>();
                }
                ShowMessage.ShowMsg(hand, ShowMessage.HandSuccess, "success");
            } catch (Exception e) {
                ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage, "下载出错" + e.getMessage());
            }
        }
    }

    private class handShowMsg extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ShowMessage.HandShowMessage:
                    ShowMessage.Show(mContext, msg.obj.toString());
                    break;
                case ShowMessage.HandSuccess:
                    List<String> sqlList = new ArrayList<String>();
                    if (list != null && list.size() > 0) {
                        String GoodsId, BrandName, GoodsType, SeriesName, Modelm, Colors, PackNum, sql;
                        for (Map<String, Object> map : list) {
                            GoodsId = (String) map.get("GoodsId");
                            GoodsType = (String) map.get("GoodsType");
                            BrandName = (String) map.get("BrandName");
                            SeriesName = (String) map.get("SeriesName");
                            Modelm = (String) map.get("Modelm");
                            Colors = (String) map.get("Colors");
                            PackNum = (String) map.get("PackNum");

                            sql = "insert into packdressboxscan(goodsid,brandname,goodstype,seriesname,modelm,colors,packnum,scannum)values('" + GoodsId + "','" + BrandName + "','"
                                    + GoodsType + "','" + SeriesName + "','" + Modelm + "','" + Colors + "','" + PackNum + "','0')";
                            sqlList.add(sql);
                        }
                        SqliteDataHelper.getHelper(getApplicationContext()).BatchOperation(sqlList);
                        String listsql = "select * from packdressboxscan";
                        list = SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList(listsql, null);
                        initListView(list);
                        // bindPickListener();
                    }

                    break;

                default:
                    break;
            }

            MyProgressDialog.close();
        }
    }

    public void initListView(List<Map<String, Object>> mList) {
        SimpleAdapter adapter = new SimpleAdapter(this, mList, R.layout.item_packdressboxcheck,
                new String[]{"goodsid", "modelm", "colors", "packnum", "scannum"},
                new int[]{R.id.item_PackDressBox_id, R.id.item_PackDressBox_Modelm, R.id.item_PackDressBox_Color, R.id.item_PackDressBox_total, R.id.item_PackDressBox_scannum});
        PackDressBox_list.setAdapter(adapter);
        tv_total.setText("（共 " + mList.size() + " 条）");
        MyProgressDialog.close();
    }

}
