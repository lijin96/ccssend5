package com.holyes.headquarter.instock_in;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;

import com.example.ccssend5.R;
import com.holyes.ccssend5.dao.PackingScanDao;
import com.holyes.ccssend5.entity.PackingScan;
import com.holyes.ccssend5.lib.ShowMessage;
import com.holyes.ccssend5.utils.SomeUtils;
import com.holyes.headquarter.adapter.EliminateAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: EliminateActivity
 * @Description: 装盒入库剔除界面
 * @Author: lijin
 * @Date: 2021/3/10 13:58
 */
public class EliminateActivity extends Activity {

    private Context mContext;
    private EliminateAdapter eAdapter;

    private EditText et_barcode;
    private ListView listView;
    private Button btn_finish;

    private List<PackingScan> list;
    private ArrayList<String> eliminateBarcodeList;

    private String barcode = "", product_id = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activty_eliminate);
        mContext = this;

        et_barcode = (EditText) findViewById(R.id.et_barcode);
        listView = (ListView) findViewById(R.id.listview);
        btn_finish = (Button) findViewById(R.id.btn_finish);

        et_barcode.setOnKeyListener(new EtBarcodeOnKeyListener());
        btn_finish.setOnClickListener(new BtnFinishClick());

        product_id = getIntent().getStringExtra("product_id");

        eliminateBarcodeList = new ArrayList<String>();
        list = new ArrayList<PackingScan>();
        eAdapter = new EliminateAdapter(mContext, list);
        listView.setAdapter(eAdapter);
    }


    private class EtBarcodeOnKeyListener implements View.OnKeyListener {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {

            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    barcode = et_barcode.getText().toString().trim();
                    et_barcode.setText("");
                    deleteLocalPacking(mContext, barcode);

                }


                return true;
            } else {
                return false;
            }

        }
    }

    class BtnFinishClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            SomeUtils.clickKeyBack();
        }

    }

    /**
     * 删除本地临时数据
     *
     * @param context
     * @param barcode
     */
    public void deleteLocalPacking(Context context, String barcode) {
        if (PackingScanDao.barcodeExistLocal(context, barcode, false)) {
            PackingScan packingScan = PackingScanDao.getPackingByBarcode(context, barcode);
            eliminateBarcodeList.add(barcode);
            list.add(packingScan);
            eAdapter.notifyDataSetChanged();
            if (PackingScanDao.deletePackingByCode(context, barcode)) {
                ShowMessage.Show(mContext, barcode + "剔除成功！");
            }

        } else {
            ShowMessage.Show(mContext, barcode + "不在本地临时数据中,不用剔除");
        }
    }

    @Override
    public void onBackPressed() {
        if (SomeUtils.isDoubleClick(mContext, true)) {
            if (eliminateBarcodeList.size() > 0) {
                Intent intent = new Intent();
                intent.putStringArrayListExtra("eliminateBarcodeList", eliminateBarcodeList);
                setResult(RESULT_OK, intent);
            }
            finish();
        }
    }


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

}


