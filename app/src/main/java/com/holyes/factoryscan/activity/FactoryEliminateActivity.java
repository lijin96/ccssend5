package com.holyes.factoryscan.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.ccssend5.R;
import com.google.gson.JsonObject;
import com.holyes.ccssend5.dao.PackingScanDao;
import com.holyes.ccssend5.entity.PackingScan;
import com.holyes.ccssend5.lib.AccessWeb;
import com.holyes.ccssend5.lib.ShowMessage;
import com.holyes.ccssend5.lib.SysUserInfo;
import com.holyes.ccssend5.myview.MyProgressDialog;
import com.holyes.ccssend5.select.SelectRetailStore;
import com.holyes.ccssend5.utils.SomeUtils;
import com.holyes.headquarter.adapter.EliminateAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: FactoryEliminateActivity
 * @Description: 供应商装盒入库剔除界面
 * @Author: lijin
 * @Date: 2025年7月31日17:44:55
 */
public class FactoryEliminateActivity extends Activity {

    private Handler hand;
    private AccessWeb accWeb;
    private SysUserInfo sysUserInfo;

    private Context mContext;
    //    private EliminateAdapter eAdapter;
    private SimpleAdapter adapter;

    private EditText et_barcode,et_box_barcode;
    private TextView tv_box_barcode;
    private ListView listView;
    private Button btn_finish;

//    private List<PackingScan> list;
//    private ArrayList<String> eliminateBarcodeList;

    private String scanBillno = "";//扫描单号

    private List<Map<String, Object>> dList = new ArrayList<Map<String, Object>>();

    private String barcode = "", BoxNoCode = "";

    private String lStar = "";

    private String GoodsNum="",TotalNum="",BoxNum="",BoxSetNum="",BoxActNum="";

    private Boolean IsEliminate=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activty_factoryeliminate);

        mContext = this;
        accWeb = new AccessWeb(getApplicationContext());
        hand = new handShowMsg();
        sysUserInfo=new SysUserInfo(mContext);

        tv_box_barcode=findViewById(R.id.tv_box_barcode);
        et_barcode = (EditText) findViewById(R.id.et_barcode);

        listView = (ListView) findViewById(R.id.listview);
        btn_finish = (Button) findViewById(R.id.btn_finish);

        et_barcode.setOnKeyListener(new EtBarcodeOnKeyListener());

        btn_finish.setOnClickListener(new BtnFinishClick());

//        scanBillno = sysUserInfo.getUserid() + "S" + SomeUtils.RandomScanOrder();// 系统
        scanBillno = getIntent().getStringExtra("ScanBillno");

        BoxNoCode = getIntent().getStringExtra("PackBoxNoCode");
        tv_box_barcode.setText(BoxNoCode);

        if (!BoxNoCode.equals("")){
            DownLoadBoxNoCodeThread(BoxNoCode);
        }

    }

    private class EtBarcodeOnKeyListener implements View.OnKeyListener {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {

            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {


                    String barcodeStr = "";

                    if (et_barcode.getText().toString().trim().indexOf("=") != -1||et_barcode.getText().toString().trim().indexOf("http") != -1) {
                        //包含
                        barcodeStr = SomeUtils.InterceptCode(mContext, et_barcode.getText().toString().trim());
                    } else {
                        //不包含
                        barcodeStr =SomeUtils.UpdatefirstString(mContext,et_barcode.getText().toString().trim());
                    }

                    et_barcode.setText("");
                    RemoveoCodeThread(barcodeStr);
                }
                return true;
            } else {
                return false;
            }

        }
    }

    //读取已装盒标码信息
    private void DownLoadBoxNoCodeThread(String tBoxNoCode) {
        MyProgressDialog.show(mContext, "正在获取数据...", true, false);
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    accWeb.mWebId = lStar + tBoxNoCode;

                    JsonObject FillBoxInforObj = new JsonObject();
                    FillBoxInforObj.addProperty("BoxNo", tBoxNoCode);
                    FillBoxInforObj.addProperty("SoCompId", sysUserInfo.getCompanyid());
                    FillBoxInforObj.addProperty("OaSuserId", sysUserInfo.getUserid());

                    String  FillBoxInfor = accWeb.Holyes_Dv_Factory_GetFillBoxInfor(FillBoxInforObj.toString());

                    JSONArray listjson = new JSONArray(FillBoxInfor);
                    dList = new ArrayList<Map<String, Object>>();
                    for (int i = 0; i < listjson.length(); i++) {
                        JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
                        Map<String, Object> map1 = new HashMap<String, Object>();
                        map1.put("BoxNo", jsonObject2.getString("BoxNo"));
                        map1.put("SetNum", jsonObject2.getString("SetNum"));
                        map1.put("ActNum", jsonObject2.getString("ActNum"));
                        map1.put("Modelm", jsonObject2.getString("Modelm"));
                        map1.put("Colors", jsonObject2.getString("Colors"));
                        map1.put("GoodsId", jsonObject2.getString("GoodsId"));
                        dList.add(map1);
                    }

                    ShowMessage.ShowMsg(hand, ShowMessage.HandSuccess, "success");
                    lStar = "";
                } catch (Exception e) {
                    ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage,"盒标码获取明细报错" + e.getMessage());
                    lStar = SomeUtils.isNotFromServiceError(e.getMessage());

                }
            }
        });
        sendCode.start();
    }

    //剔除
    private void RemoveoCodeThread(String tBarcode) {
        MyProgressDialog.show(mContext, "正在剔除...", true, false);
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    accWeb.mWebId = lStar + BoxNoCode;
//                    BoxNo：盒标码(必传)
//                    Barcode：物流码(必传)
//                    SoCompId：供应商代号(必传)
//                    OaSuserId：操作员代码(必传)
//                    ScanBillNo：扫描号(唯一性，必传)

                    JsonObject WeedOutObj = new JsonObject();
                    WeedOutObj.addProperty("BoxNo", BoxNoCode);
                    WeedOutObj.addProperty("Barcode", tBarcode);
                    WeedOutObj.addProperty("SoCompId", sysUserInfo.getCompanyid());
                    WeedOutObj.addProperty("OaSuserId", sysUserInfo.getUserid());
                    WeedOutObj.addProperty("ScanBillNo", scanBillno);

                    String  WeedOutResult = accWeb.Holyes_Dv_Factory_WeedOutFillBox(WeedOutObj.toString());
//                    Log.d("main", "剔除"+WeedOutResult);

                    JSONArray listjson = new JSONArray(WeedOutResult);
                    List<Map<String, Object>> WeedOutList = new ArrayList<Map<String, Object>>();
                    for (int i = 0; i < listjson.length(); i++) {
                        JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
                        Map<String, Object> map1 = new HashMap<String, Object>();
                        map1.put("BoxNo", jsonObject2.optString("BoxNo"));
                        map1.put("Barcode", jsonObject2.optString("Barcode"));

                        map1.put("Modelm", jsonObject2.optString("Modelm"));
                        map1.put("Colors", jsonObject2.optString("Colors"));

                        map1.put("GoodsNum", jsonObject2.optString("GoodsNum"));
                        map1.put("TotalNum", jsonObject2.optString("TotalNum"));
                        map1.put("BoxNum", jsonObject2.optString("BoxNum"));

                        map1.put("SetNum", jsonObject2.optString("SetNum"));//设置盒装数
                        map1.put("ActNum", jsonObject2.optString("ActNum"));//当前盒已装数


                        WeedOutList.add(map1);
                    }

                    if (WeedOutList.size()>0){
                        GoodsNum=WeedOutList.get(0).get("GoodsNum").toString();
                        TotalNum=WeedOutList.get(0).get("TotalNum").toString();
                        BoxNum=WeedOutList.get(0).get("BoxNum").toString();

                        BoxSetNum=WeedOutList.get(0).get("SetNum").toString();
                        BoxActNum=WeedOutList.get(0).get("ActNum").toString();

                        ShowMessage.ShowMsg(hand, ShowMessage.HandScanSuccess, "success");

                    }else{
                        ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage, "剔除未返回数据"+WeedOutResult);
                    }

                    lStar = "";
                } catch (Exception e) {
                    ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage,"剔除报错" + e.getMessage());
                    lStar = SomeUtils.isNotFromServiceError(e.getMessage());

                }
            }
        });
        sendCode.start();
    }

    private class handShowMsg extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ShowMessage.HandShowMessage: // 显示错误提示显示
                    MyProgressDialog.close();
                    ShowMessage.Show(FactoryEliminateActivity.this, msg.obj.toString());
                    break;
                case ShowMessage.HandSuccess: // 盒标码返回成功
                    MyProgressDialog.close();

                    if (dList.size()>0){
                        initListView(dList);
                    }else{
                        ShowMessage.Show(FactoryEliminateActivity.this, "未查询到数据");
                    }
                    break;
                case ShowMessage.HandScanSuccess: // 剔除成功
                    MyProgressDialog.close();
                    IsEliminate=true;
                    DownLoadBoxNoCodeThread(BoxNoCode);
                    break;

                default:
                    // ShowMessage.Show(Xundian.this,msg.obj.toString());
                    break;
            }
            MyProgressDialog.close();
            super.handleMessage(msg);
        }
    }


    class BtnFinishClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            SomeUtils.clickKeyBack();
        }

    }

    public void initListView(List<Map<String, Object>> dataList) {
        adapter = new SimpleAdapter(this, dataList, R.layout.factoryeliminate_listview_item,
                new String[]{"Modelm", "Colors", "SetNum", "ActNum",
                }, new int[]{
                R.id.tv_model,R.id.tv_colors, R.id.tv_packingnum, R.id.tv_actnum});
        listView.setAdapter(adapter);
    }


    @Override
    public void onBackPressed() {
        if (SomeUtils.isDoubleClick(mContext, true)) {
            if (IsEliminate) {
                Intent intent = new Intent();
                intent.putExtra("GoodsNum",GoodsNum);
                intent.putExtra("TotalNum",TotalNum);
                intent.putExtra("BoxNum", BoxNum);

//                intent.putExtra("BoxSetNum", BoxSetNum);
                intent.putExtra("BoxActNum", BoxActNum);

//                BoxSetNum=WeedOutList.get(0).get("SetNum").toString();
//                BoxActNum=WeedOutList.get(0).get("ActNum").toString();

                setResult(RESULT_OK, intent);
                finish();
            }else {
                finish();
            }
        }
    }


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

}


